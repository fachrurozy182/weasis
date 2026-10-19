/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.weasis.core.ui.model.layer.imp;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.image.AffineTransformOp;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.OpEventListener;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.image.WindowOp;
import org.weasis.core.api.image.ZoomOp;
import org.weasis.core.api.image.cv.CvUtil;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.editor.image.Canvas;
import org.weasis.core.ui.model.layer.Layer;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.ImageLayerChangeListener;
import org.weasis.core.ui.model.utils.imp.DefaultUUID;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.lut.WlPresentation;

public class RenderedImageLayer<E extends ImageElement>
extends DefaultUUID
implements Layer,
ImageLayer<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderedImageLayer.class);
    private final SimpleOpManager disOpManager;
    private final List<ImageLayerChangeListener<E>> listenerList;
    private final List<OpEventListener> opListeners;
    private OpManager preprocessing;
    private E sourceImage;
    private Optional<PlanarImage> displayImage;
    private Boolean visible = true;
    private boolean enableDispOperations = true;
    private Point offset;
    private transient Supplier<List<MeasurableLayer>> secondaryLayersSupplier;

    public RenderedImageLayer() {
        this((SimpleOpManager)null);
    }

    public RenderedImageLayer(SimpleOpManager disOpManager) {
        this.disOpManager = Optional.ofNullable(disOpManager).orElseGet(SimpleOpManager::new);
        this.listenerList = new ArrayList<ImageLayerChangeListener<E>>();
        this.opListeners = new ArrayList<OpEventListener>();
        this.displayImage = Optional.empty();
        this.addEventListener(this.disOpManager);
    }

    @Override
    public E getSourceImage() {
        return this.sourceImage;
    }

    @Override
    public PlanarImage getSourceRenderedImage() {
        if (this.sourceImage != null) {
            return ((ImageElement)this.sourceImage).getImage(this.preprocessing);
        }
        return null;
    }

    @Override
    public PlanarImage getDisplayImage() {
        return this.displayImage.orElse(null);
    }

    public OpManager getPreprocessing() {
        return this.preprocessing;
    }

    public void setPreprocessing(OpManager preprocessing) {
        this.setImage(this.sourceImage, preprocessing);
    }

    @Override
    public SimpleOpManager getDisplayOpManager() {
        return this.disOpManager;
    }

    @Override
    public synchronized boolean isEnableDispOperations() {
        return this.enableDispOperations;
    }

    @Override
    public synchronized void setEnableDispOperations(boolean enabled) {
        this.enableDispOperations = enabled;
        if (enabled) {
            this.updateDisplayOperations();
        }
    }

    @Override
    public void setVisible(Boolean visible) {
        this.visible = Optional.ofNullable(visible).orElse(this.getType().getVisible());
    }

    @Override
    public Boolean getVisible() {
        return this.visible;
    }

    @Override
    public void setType(LayerType type) {
    }

    @Override
    public void setName(String graphicLayerName) {
    }

    @Override
    public String getName() {
        return this.getType().getDefaultName();
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public Integer getLevel() {
        return this.getType().getLevel();
    }

    @Override
    public void setLevel(Integer i) {
    }

    @Override
    public AffineTransform getTransform() {
        return null;
    }

    @Override
    public void setTransform(AffineTransform transform) {
    }

    @Override
    public LayerType getType() {
        return LayerType.IMAGE;
    }

    @Override
    public boolean hasContent() {
        return this.getSourceImage() != null;
    }

    @Override
    public void setImage(E image, OpManager preprocessing) {
        ZoomOp node;
        boolean init = image != null && !image.equals(this.sourceImage) || image == null && this.sourceImage != null;
        this.sourceImage = image;
        this.preprocessing = preprocessing;
        if (this.sourceImage != null && (node = ((ImageElement)this.sourceImage).getRectifyAspectRatioZoomOp()) != null) {
            SimpleOpManager process = new SimpleOpManager();
            process.addImageOperationAction(node);
            if (preprocessing != null) {
                for (ImageOpNode op : preprocessing.getOperations()) {
                    if (node.getName().equals(op.getName())) continue;
                    process.addImageOperationAction(op);
                }
            }
            this.preprocessing = process;
        }
        if (preprocessing != null || init) {
            this.disOpManager.setFirstNode(this.getSourceRenderedImage());
            this.updateDisplayOperations();
        }
    }

    public void drawImage(Graphics2D g2d) {
        if (!this.visible.booleanValue() || this.displayImage.isEmpty()) {
            return;
        }
        PlanarImage currentImage = this.displayImage.get();
        Shape clip = g2d.getClip();
        if (clip instanceof Rectangle2D) {
            Rectangle2D rect = new Rectangle2D.Double(0.0, 0.0, (double)currentImage.width() - 1.0, (double)currentImage.height() - 1.0);
            if ((rect = rect.createIntersection((Rectangle2D)clip)).isEmpty()) {
                return;
            }
            g2d.setClip(rect);
        }
        try {
            if (g2d.getDeviceConfiguration().getDefaultTransform().getScaleX() > 1.0) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            }
            g2d.drawImage((Image)ImageConversion.toBufferedImage((PlanarImage)currentImage), 0, 0, null);
        }
        catch (Exception e) {
            LOGGER.error("Cannot draw the image", (Throwable)e);
            if ("java.io.IOException: closed".equals(e.getMessage())) {
                if (((ImageElement)this.sourceImage).isImageInCache()) {
                    ((ImageElement)this.sourceImage).removeImageFromCache();
                }
                this.disOpManager.setFirstNode(this.getSourceRenderedImage());
                this.updateDisplayOperations();
            }
        }
        catch (OutOfMemoryError e) {
            LOGGER.error("Cannot draw the image", (Throwable)e);
            CvUtil.runGarbageCollectorAndWait(200L);
        }
        g2d.setClip(clip);
    }

    public void drawImageForPrinter(Graphics2D g2d, double viewScale, Canvas canvas) {
        if (!this.visible.booleanValue() || this.displayImage.isEmpty()) {
            return;
        }
        PlanarImage currentImage = this.displayImage.get();
        Shape clip = g2d.getClip();
        if (clip instanceof Rectangle2D) {
            Rectangle2D rect = new Rectangle2D.Double(0.0, 0.0, (double)currentImage.width() - 1.0, (double)currentImage.height() - 1.0);
            if ((rect = rect.createIntersection((Rectangle2D)clip)).isEmpty()) {
                return;
            }
            g2d.setClip(rect);
        }
        List matrix = this.disOpManager.getParamValue(AffineTransformOp.OP_NAME, "affine.matrix").orElse(null);
        Rectangle2D bound = this.disOpManager.getParamValue(AffineTransformOp.OP_NAME, "dest.bounds", Rectangle2D.class).orElse(null);
        if (matrix == null || bound == null) {
            return;
        }
        ArrayList<Double> m = new ArrayList<Double>(matrix);
        double ratioX = (Double)m.get(0);
        double ratioY = (Double)m.get(4);
        double offsetX = (Double)m.get(2);
        double offsetY = (Double)m.get(5);
        double imageResX = viewScale;
        double imageResY = viewScale;
        imageResX = Math.max(imageResX, ratioX);
        imageResY = Math.max(imageResY, ratioY);
        m.set(0, imageResX);
        m.set(4, imageResY);
        double rx = ratioX / imageResX;
        double ry = ratioY / imageResY;
        Rectangle2D.Double b = new Rectangle2D.Double(bound.getX() / rx, bound.getY() / ry, bound.getWidth() / rx, bound.getHeight() / ry);
        m.set(2, offsetX / rx);
        m.set(5, offsetY / ry);
        this.disOpManager.setParamValue(AffineTransformOp.OP_NAME, "affine.matrix", m);
        this.disOpManager.setParamValue(AffineTransformOp.OP_NAME, "dest.bounds", b);
        if (!bound.equals(b)) {
            this.disOpManager.process();
        }
        m.set(0, ratioX);
        m.set(4, ratioY);
        m.set(2, offsetX);
        m.set(5, offsetY);
        this.disOpManager.setParamValue(AffineTransformOp.OP_NAME, "affine.matrix", matrix);
        this.disOpManager.setParamValue(AffineTransformOp.OP_NAME, "dest.bounds", bound);
        if (g2d.getDeviceConfiguration().getDefaultTransform().getScaleX() > 1.0) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        g2d.drawImage(ImageConversion.toBufferedImage((PlanarImage)this.displayImage.orElse(null)), AffineTransform.getScaleInstance(rx, ry), null);
        g2d.setClip(clip);
    }

    public void dispose() {
        this.sourceImage = null;
        this.displayImage = Optional.empty();
        this.listenerList.clear();
        this.opListeners.clear();
    }

    public void addLayerChangeListener(ImageLayerChangeListener<E> listener) {
        if (listener != null && !this.listenerList.contains(listener)) {
            this.listenerList.add(listener);
        }
    }

    public void removeLayerChangeListener(ImageLayerChangeListener<E> listener) {
        if (listener != null) {
            this.listenerList.remove(listener);
        }
    }

    public void fireImageChanged() {
        if (this.displayImage.isEmpty()) {
            Optional<PlanarImage> imgSource = this.disOpManager.getFirstNodeInputImage();
            this.disOpManager.clearNodeIOCache();
            this.disOpManager.setFirstNode(imgSource.orElse(null));
        }
        this.fireLayerChanged();
    }

    public void fireLayerChanged() {
        for (ImageLayerChangeListener<E> eImageLayerChangeListener : this.listenerList) {
            eImageLayerChangeListener.handleLayerChanged(this);
        }
    }

    public synchronized void addEventListener(OpEventListener listener) {
        if (listener != null && !this.opListeners.contains(listener)) {
            this.opListeners.add(listener);
        }
    }

    public synchronized void removeEventListener(OpEventListener listener) {
        if (listener != null) {
            this.opListeners.remove(listener);
        }
    }

    public synchronized void fireOpEvent(ImageOpEvent event) {
        for (OpEventListener opListener : this.opListeners) {
            opListener.handleImageOpEvent(event);
        }
    }

    @Override
    public void updateDisplayOperations() {
        if (this.isEnableDispOperations()) {
            Optional<PlanarImage> source = this.disOpManager.getFirstNodeInputImage();
            if (source.isPresent() && source.get().toMat().empty()) {
                this.disOpManager.setFirstNode(this.getSourceRenderedImage());
            }
            this.displayImage = this.disOpManager.process();
            this.fireImageChanged();
        }
    }

    @Override
    public MeasurementsAdapter getMeasurementAdapter(Unit displayUnit) {
        if (this.hasContent()) {
            return ((ImageElement)this.getSourceImage()).getMeasurementAdapter(displayUnit, this.offset);
        }
        return null;
    }

    @Override
    public AffineTransform getShapeTransform() {
        return null;
    }

    @Override
    public Object getSourceTagValue(TagW tagW) {
        E imageElement = this.getSourceImage();
        if (imageElement != null) {
            return ((MediaElement)imageElement).getTagValue(tagW);
        }
        return null;
    }

    @Override
    public String getPixelValueUnit() {
        E imageElement = this.getSourceImage();
        if (imageElement != null) {
            return ((ImageElement)imageElement).getPixelValueUnit();
        }
        return null;
    }

    @Override
    public double pixelToRealValue(Number pixelValue) {
        Number val = pixelValue;
        E imageElement = this.getSourceImage();
        if (imageElement != null) {
            ImageOpNode imageOpNode;
            WlPresentation wlp = null;
            Optional<ImageOpNode> wlOp = this.disOpManager.getNode(WindowOp.OP_NAME);
            if (wlOp.isPresent() && (imageOpNode = wlOp.get()) instanceof WindowOp) {
                WindowOp windowOp = (WindowOp)imageOpNode;
                wlp = windowOp.getWlPresentation();
            }
            val = ((ImageElement)imageElement).pixelToRealValue(pixelValue, wlp);
        }
        if (val != null) {
            return val.doubleValue();
        }
        return 0.0;
    }

    @Override
    public Point getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(Point offset) {
        this.offset = offset;
    }

    @Override
    public double getPixelMin() {
        E imageElement = this.getSourceImage();
        if (imageElement != null) {
            return ((ImageElement)imageElement).getPixelMin();
        }
        return 0.0;
    }

    @Override
    public double getPixelMax() {
        E imageElement = this.getSourceImage();
        if (imageElement != null) {
            return ((ImageElement)imageElement).getPixelMax();
        }
        return 0.0;
    }

    public void setSecondaryLayersSupplier(Supplier<List<MeasurableLayer>> supplier) {
        this.secondaryLayersSupplier = supplier;
    }

    @Override
    public List<MeasurableLayer> getSecondaryLayers() {
        return this.secondaryLayersSupplier == null ? List.of() : this.secondaryLayersSupplier.get();
    }
}

