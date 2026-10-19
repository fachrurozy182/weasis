/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core$MinMaxLocResult
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageAnalyzer
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.ImageTransformer
 *  org.weasis.opencv.op.lut.DefaultWlPresentation
 *  org.weasis.opencv.op.lut.LutShape
 *  org.weasis.opencv.op.lut.WlParams
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.weasis.core.api.media.data;

import java.awt.Point;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.image.WindowOp;
import org.weasis.core.api.image.ZoomOp;
import org.weasis.core.api.image.cv.CvUtil;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.NativeCache;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.ResourceMonitor;
import org.weasis.core.api.util.SystemMemory;
import org.weasis.core.api.util.ThreadUtil;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageAnalyzer;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.ImageTransformer;
import org.weasis.opencv.op.lut.DefaultWlPresentation;
import org.weasis.opencv.op.lut.LutShape;
import org.weasis.opencv.op.lut.WlParams;
import org.weasis.opencv.op.lut.WlPresentation;

public class ImageElement
extends MediaElement {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageElement.class);
    public static final ExecutorService IMAGE_LOADER = ThreadUtil.newManagedImageIOThreadPool("ImageLoader");
    private static final NativeCache<ImageElement, PlanarImage> mCache = new NativeCache<ImageElement, PlanarImage>(SystemMemory.getNativeMemoryBudget()){

        @Override
        protected void afterEntryRemove(ImageElement key, PlanarImage img) {
            if (key != null) {
                key.setTag(TagW.ImageCache, false);
                MediaReader<? extends MediaElement> reader = key.getMediaReader();
                if (reader != null) {
                    reader.close();
                }
            }
            if (img != null) {
                img.release();
            }
        }
    };
    protected boolean readable = true;
    protected double pixelSizeX = 1.0;
    protected double pixelSizeY = 1.0;
    protected Unit pixelSpacingUnit = Unit.PIXEL;
    protected String pixelSizeCalibrationDescription = null;
    protected boolean pixelSizeModifiedByUser = false;
    protected String pixelValueUnit = null;
    protected Double minPixelValue;
    protected Double maxPixelValue;

    public ImageElement(MediaReader<? extends MediaElement> mediaIO, Object key) {
        super(mediaIO, key);
    }

    public static int getNumberOfImagesInCache() {
        return mCache.size();
    }

    protected void findMinMaxValues(PlanarImage img, boolean exclude8bitImage) throws OutOfMemoryError {
        if (img != null && !this.isImageAvailable()) {
            Core.MinMaxLocResult res = ImageAnalyzer.findRawMinMaxValues((PlanarImage)img, (boolean)exclude8bitImage);
            this.minPixelValue = res.minVal;
            this.maxPixelValue = res.maxVal;
        }
    }

    public boolean isImageAvailable() {
        return this.maxPixelValue != null && this.minPixelValue != null;
    }

    public void resetImageAvailable() {
        this.maxPixelValue = null;
        this.minPixelValue = null;
    }

    protected boolean isGrayImage(RenderedImage source) {
        return source.getSampleModel().getNumBands() <= 1 && !(source.getColorModel() instanceof IndexColorModel);
    }

    public LutShape getDefaultShape(WlPresentation wlp) {
        return LutShape.LINEAR;
    }

    public double getDefaultWindow(WlPresentation wlp) {
        return this.getMaxValue(wlp) - this.getMinValue(wlp);
    }

    public double getDefaultLevel(WlPresentation wlp) {
        if (this.isImageAvailable()) {
            double min = this.getMinValue(wlp);
            return min + (this.getMaxValue(wlp) - min) / 2.0;
        }
        return 0.0;
    }

    public double getMaxValue(WlPresentation wlp) {
        return this.getPixelMax();
    }

    public double getMinValue(WlPresentation wlp) {
        return this.getPixelMin();
    }

    public double getPixelMax() {
        return this.maxPixelValue == null ? 0.0 : this.maxPixelValue;
    }

    public double getPixelMin() {
        return this.minPixelValue == null ? 0.0 : this.minPixelValue;
    }

    public int getRescaleWidth(int width) {
        return (int)Math.ceil((double)width * this.getRescaleX() - 0.5);
    }

    public int getRescaleHeight(int height) {
        return (int)Math.ceil((double)height * this.getRescaleY() - 0.5);
    }

    public double getRescaleX() {
        return this.pixelSizeX <= this.pixelSizeY ? 1.0 : this.pixelSizeX / this.pixelSizeY;
    }

    public double getRescaleY() {
        return this.pixelSizeY <= this.pixelSizeX ? 1.0 : this.pixelSizeY / this.pixelSizeX;
    }

    public double getPixelSize() {
        return Math.min(this.pixelSizeX, this.pixelSizeY);
    }

    public void setPixelSize(double pixelSize) {
        if (MathUtil.isEqual((double)this.pixelSizeX, (double)this.pixelSizeY)) {
            this.setPixelSize(pixelSize, pixelSize);
        } else if (this.pixelSizeX < this.pixelSizeY) {
            this.setPixelSize(pixelSize, this.pixelSizeY / this.pixelSizeX * pixelSize);
        } else {
            this.setPixelSize(this.pixelSizeX / this.pixelSizeY * pixelSize, pixelSize);
        }
    }

    public void setPixelSize(double pixelSizeX, double pixelSizeY) {
        this.pixelSizeX = pixelSizeX <= 0.0 ? 1.0 : pixelSizeX;
        this.pixelSizeY = pixelSizeY <= 0.0 ? 1.0 : pixelSizeY;
    }

    public ZoomOp getRectifyAspectRatioZoomOp() {
        if (MathUtil.isDifferent((double)this.pixelSizeX, (double)this.pixelSizeY)) {
            ZoomOp node = new ZoomOp();
            node.setName("rectifyAspectRatio");
            node.setParam("ratio.x", this.getRescaleX());
            node.setParam("ratio.y", this.getRescaleY());
            return node;
        }
        return null;
    }

    public void setPixelValueUnit(String pixelValueUnit) {
        this.pixelValueUnit = pixelValueUnit;
    }

    public Unit getPixelSpacingUnit() {
        return this.pixelSpacingUnit;
    }

    public void setPixelSpacingUnit(Unit pixelSpacingUnit) {
        this.pixelSpacingUnit = pixelSpacingUnit;
    }

    public String getPixelValueUnit() {
        return this.pixelValueUnit;
    }

    public String getPixelSizeCalibrationDescription() {
        return this.pixelSizeCalibrationDescription;
    }

    public void setPixelSizeCalibrationDescription(String pixelSizeCalibrationDescription) {
        this.pixelSizeCalibrationDescription = pixelSizeCalibrationDescription;
    }

    public boolean isPixelSizeModifiedByUser() {
        return this.pixelSizeModifiedByUser;
    }

    public void setPixelSizeModifiedByUser(boolean pixelSizeModifiedByUser) {
        this.pixelSizeModifiedByUser = pixelSizeModifiedByUser;
    }

    public Number pixelToRealValue(Number pixelValue, WlPresentation wlp) {
        return pixelValue;
    }

    public LookupTableCV getVOILookup(WlParams wl) {
        return null;
    }

    public MeasurementsAdapter getMeasurementAdapter(Unit displayUnit, Point offset) {
        Unit unit = displayUnit;
        if (unit == null || this.pixelSpacingUnit == null || this.pixelSpacingUnit.equals((Object)Unit.PIXEL)) {
            unit = Unit.PIXEL;
        }
        double unitRatio = unit.equals((Object)Unit.PIXEL) ? 1.0 : this.getPixelSize() * unit.getConversionRatio(this.pixelSpacingUnit.getFactorToMeters());
        int offsetx = offset == null ? 0 : -offset.x;
        int offsety = offset == null ? 0 : -offset.y;
        return new MeasurementsAdapter(unitRatio, offsetx, offsety, false, 0, unit.getAbbreviation());
    }

    public boolean isImageInCache() {
        return mCache.get(this) != null;
    }

    public void removeImageFromCache() {
        mCache.remove(this);
    }

    public void pinInCache() {
        mCache.pin(this);
    }

    public void unpinFromCache() {
        mCache.unpin(this);
    }

    public boolean isPinnedInCache() {
        return mCache.isPinned(this);
    }

    public boolean hasSameSize(ImageElement image) {
        if (image != null) {
            PlanarImage img = this.getImage();
            PlanarImage img2 = image.getImage();
            return img != null && img2 != null && this.getRescaleWidth(img.width()) == image.getRescaleWidth(img2.width()) && this.getRescaleHeight(img.height()) == image.getRescaleHeight(img2.height());
        }
        return false;
    }

    protected PlanarImage loadImage() throws Exception {
        return this.mediaIO.getImageFragment(this);
    }

    public PlanarImage getRenderedImage(PlanarImage imageSource) {
        return this.getRenderedImage(imageSource, null);
    }

    public PlanarImage getRenderedImage(PlanarImage imageSource, Map<String, Object> params) {
        if (imageSource == null) {
            return null;
        }
        Double window = params == null ? null : (Double)params.get(ActionW.WINDOW.cmd());
        Double level = params == null ? null : (Double)params.get(ActionW.LEVEL.cmd());
        Boolean pixelPadding = params == null ? null : (Boolean)params.get(ActionW.IMAGE_PIX_PADDING.cmd());
        pixelPadding = pixelPadding == null ? Boolean.TRUE : pixelPadding;
        DefaultWlPresentation pr = new DefaultWlPresentation(null, pixelPadding.booleanValue());
        window = window == null ? this.getDefaultWindow((WlPresentation)pr) : window.doubleValue();
        level = level == null ? this.getDefaultLevel((WlPresentation)pr) : level.doubleValue();
        return ImageElement.getDefaultRenderedImage(this, imageSource, window, level, pixelPadding);
    }

    public static PlanarImage getDefaultRenderedImage(ImageElement image, PlanarImage source, double window, double level, boolean pixelPadding) {
        if (image == null || source == null) {
            return null;
        }
        if (ImageConversion.convertToDataType((int)source.type()) == 0 && MathUtil.isEqual((double)window, (double)255.0) && (MathUtil.isEqual((double)level, (double)127.5) || MathUtil.isEqual((double)level, (double)127.0))) {
            return source;
        }
        double high = level + window / 2.0;
        double low = level - window / 2.0;
        double range = high - low;
        if (range < 1.0) {
            range = 1.0;
        }
        double slope = 255.0 / range;
        double yInt = 255.0 - slope * high;
        return ImageTransformer.rescaleToByte((Mat)source.toMat(), (double)slope, (double)yInt);
    }

    public SimpleOpManager buildSimpleOpManager(boolean img16, boolean padding, boolean shutter, boolean overlay, double ratio) {
        return this.buildSimpleOpManager(img16, padding, ratio);
    }

    public SimpleOpManager buildSimpleOpManager(boolean img16, boolean padding, double ratio) {
        SimpleOpManager manager = new SimpleOpManager();
        PlanarImage image = this.getImage(null);
        if (image != null) {
            if (img16) {
                if (CvType.depth((int)image.type()) == 3) {
                    ImageCV dstImg = new ImageCV();
                    image.toImageCV().convertTo((Mat)dstImg, CvType.CV_16UC((int)image.channels()), 1.0, 32768.0);
                    image = dstImg;
                }
            } else {
                manager.addImageOperationAction(new WindowOp());
                manager.setParamValue(WindowOp.OP_NAME, "img.element", this);
                manager.setParamValue(WindowOp.OP_NAME, ActionW.IMAGE_PIX_PADDING.cmd(), padding);
            }
            manager.addImageOperationAction(this.buildZoomOp(ratio));
            manager.setFirstNode(image);
        }
        return manager;
    }

    protected ZoomOp buildZoomOp(double ratio) {
        ZoomOp node = new ZoomOp();
        node.setParam("ratio.x", this.getRescaleX() * ratio);
        node.setParam("ratio.y", this.getRescaleY() * ratio);
        node.setParam("interpolation", (Object)ZoomOp.Interpolation.BICUBIC);
        return node;
    }

    public static PlanarImage getDefaultRenderedImage(ImageElement image, PlanarImage source, WlParams wl) {
        return ImageElement.getDefaultRenderedImage(image, source, image.getDefaultWindow((WlPresentation)wl), image.getDefaultLevel((WlPresentation)wl), true);
    }

    public PlanarImage getImage(OpManager manager) {
        return this.getImage(manager, true);
    }

    public String toString() {
        URI uri = this.getMediaURI();
        if (uri == null) {
            return "Memory image";
        }
        return uri.toString();
    }

    public synchronized PlanarImage getImage(OpManager manager, boolean findMinMax) {
        try {
            return this.getCacheImage(this.startImageLoading(), manager, findMinMax, true);
        }
        catch (OutOfMemoryError e1) {
            ResourceMonitor.getInstance().recordOutOfMemory();
            mCache.expungeStaleEntries();
            CvUtil.runGarbageCollectorAndWait(100L);
            try {
                return this.getCacheImage(this.startImageLoading(), manager, findMinMax, true);
            }
            catch (OutOfMemoryError e) {
                LOGGER.warn("Reading image data: {}", (Object)this, (Object)e1);
                return null;
            }
        }
    }

    private PlanarImage getCacheImage(PlanarImage cacheImage, OpManager manager, boolean findMinMax, boolean reloadIfReleased) {
        if (reloadIfReleased && ImageElement.isBufferReleased(cacheImage)) {
            LOGGER.warn("Image buffer released during concurrent access, reloading: {}", (Object)this);
            mCache.remove(this);
            return this.getCacheImage(this.startImageLoading(), manager, findMinMax, false);
        }
        if (findMinMax) {
            try {
                this.findMinMaxValues(cacheImage, true);
            }
            catch (Exception e) {
                mCache.remove(this);
                if (ImageElement.isBufferReleased(cacheImage)) {
                    LOGGER.warn("Image buffer released during concurrent access: {}", (Object)this);
                }
                this.readable = false;
                LOGGER.error("Cannot read image: {}", (Object)this, (Object)e);
            }
        }
        if (manager != null && cacheImage != null) {
            PlanarImage img = manager.getLastNodeOutputImage().orElse(null);
            if (manager.getFirstNodeInputImage().orElse(null) != cacheImage || manager.needProcessing()) {
                manager.setFirstNode(cacheImage);
                img = manager.process().orElse(null);
                this.resetImageAvailable();
                this.findMinMaxValues(img, true);
            }
            if (img != null) {
                return img;
            }
        }
        return cacheImage;
    }

    private static boolean isBufferReleased(PlanarImage image) {
        return image != null && image.width() <= 0;
    }

    public PlanarImage getImage() {
        return this.getImage(null);
    }

    private PlanarImage startImageLoading() throws OutOfMemoryError {
        PlanarImage cacheImage = mCache.get(this);
        if (cacheImage == null && this.readable && this.setAsLoading()) {
            LOGGER.debug("Asking for reading image: {}", (Object)this);
            Load ref = new Load(this);
            Future<PlanarImage> future = IMAGE_LOADER.submit(ref);
            PlanarImage img = null;
            try {
                img = future.get(45L, TimeUnit.SECONDS);
            }
            catch (InterruptedException | TimeoutException e) {
                Thread.currentThread().interrupt();
                future.cancel(true);
            }
            catch (ExecutionException e) {
                Throwable throwable = e.getCause();
                if (throwable instanceof OutOfMemoryError) {
                    OutOfMemoryError memoryError = (OutOfMemoryError)throwable;
                    this.setAsLoaded();
                    throw memoryError;
                }
                this.readable = false;
                LOGGER.error("Cannot read pixel data!: {}", (Object)this, (Object)e);
            }
            if (img != null) {
                boolean bl = this.readable = img.width() > 0;
                if (this.readable) {
                    mCache.put(this, img);
                    ResourceMonitor.getInstance().recordImageLoaded(img.physicalBytes());
                    cacheImage = img;
                    this.setTag(TagW.ImageCache, true);
                }
            }
            this.setAsLoaded();
        }
        return cacheImage;
    }

    public boolean isReadable() {
        return this.readable;
    }

    class Load
    implements Callable<PlanarImage> {
        final /* synthetic */ ImageElement this$0;

        Load(ImageElement this$0) {
            ImageElement imageElement = this$0;
            Objects.requireNonNull(imageElement);
            this.this$0 = imageElement;
        }

        @Override
        public PlanarImage call() throws Exception {
            return this.this$0.loadImage();
        }
    }
}

