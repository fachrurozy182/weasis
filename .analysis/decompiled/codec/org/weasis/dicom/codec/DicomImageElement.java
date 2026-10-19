/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 *  org.opencv.core.Core$MinMaxLocResult
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.ActionW
 *  org.weasis.core.api.image.ImageOpNode
 *  org.weasis.core.api.image.OpManager
 *  org.weasis.core.api.image.SimpleOpManager
 *  org.weasis.core.api.image.WindowOp
 *  org.weasis.core.api.image.util.Unit
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.util.MathUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.lut.LutParameters
 *  org.weasis.opencv.op.lut.LutShape
 *  org.weasis.opencv.op.lut.PresentationStateLut
 *  org.weasis.opencv.op.lut.WlParams
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.weasis.dicom.codec;

import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.img.DicomImageAdapter;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.DicomOutputData;
import org.dcm4che3.img.ImageRendering;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.lut.PresetWindowLevel;
import org.dcm4che3.img.stream.BytesWithImageDescriptor;
import org.dcm4che3.img.stream.ImageAdapter;
import org.dcm4che3.img.util.DicomUtils;
import org.joml.Vector3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.image.WindowOp;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.util.MathUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.display.OverlayOp;
import org.weasis.dicom.codec.display.ShutterOp;
import org.weasis.dicom.codec.display.WindowAndPresetsOp;
import org.weasis.dicom.codec.geometry.GeometryOfSlice;
import org.weasis.dicom.codec.utils.Ultrasound;
import org.weasis.dicom.param.AttributeEditorContext;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.lut.LutParameters;
import org.weasis.opencv.op.lut.LutShape;
import org.weasis.opencv.op.lut.PresentationStateLut;
import org.weasis.opencv.op.lut.WlParams;
import org.weasis.opencv.op.lut.WlPresentation;

public class DicomImageElement
extends ImageElement
implements DicomElement {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomImageElement.class);
    public static final List<LutShape> DEFAULT_LUT_FUNCTIONS = List.of(LutShape.LINEAR, LutShape.SIGMOID, LutShape.SIGMOID_NORM, LutShape.LOG, LutShape.LOG_INV);
    private DicomImageAdapter adapter = null;
    private Collection<LutShape> lutShapeCollection = null;

    public DicomImageElement(DcmMediaReader mediaIO, Object key) {
        super((MediaReader)mediaIO, key);
        this.initPixelConfiguration();
    }

    public void initPixelConfiguration() {
        this.pixelSizeX = 1.0;
        this.pixelSizeY = 1.0;
        this.pixelSpacingUnit = Unit.PIXEL;
        String modality = TagD.getTagValue((TagReadable)this.mediaIO, 524384, String.class);
        double[] val = TagD.getTagValue((TagReadable)this.mediaIO, 2621488, double[].class);
        if (val == null || val.length != 2) {
            boolean magFactor = "CR".equals(modality) || "DX".equals(modality) || "IO".equals(modality) || "MG".equals(modality) || "PX".equals(modality) || "RF".equals(modality) || "XA".equals(modality);
            val = this.getMagnifiedPixelSpacing(magFactor);
        } else {
            this.pixelSizeCalibrationDescription = TagD.getTagValue((TagReadable)this.mediaIO, 2624004, String.class);
        }
        if (val == null || val.length != 2) {
            val = TagD.getTagValue((TagReadable)this.mediaIO, 1581072, double[].class);
            String string = this.pixelSizeCalibrationDescription = val == null ? null : "At scanner";
        }
        if (val != null && val.length == 2 && val[0] > 0.0 && val[1] > 0.0) {
            this.setPixelSize(val[1], val[0]);
            this.pixelSpacingUnit = Unit.MILLIMETER;
            this.pixelSizeModifiedByUser = false;
        }
        this.initPixelValueUnit(modality);
        if (val == null) {
            this.initPixelAspectRatio();
        }
    }

    private void initPixelValueUnit(String modality) {
        Attributes spatialCalibration;
        this.pixelValueUnit = TagD.getTagValue((TagReadable)this, 2625620, String.class);
        if (this.pixelValueUnit == null) {
            this.pixelValueUnit = TagD.getTagValue((TagReadable)this, 5509121, String.class);
        }
        if (this.pixelValueUnit == null && "CT".equals(modality)) {
            String sopClass = TagD.getTagValue((TagReadable)this.mediaIO, 524310, String.class);
            if (StringUtil.hasText((CharSequence)sopClass) && !sopClass.startsWith("1.2.840.10008.5.1.4.1.1.7")) {
                this.pixelValueUnit = "HU";
            }
        } else if (this.pixelSpacingUnit == Unit.PIXEL && "US".equals(modality) && (spatialCalibration = Ultrasound.getUniqueSpatialRegion(this.getMediaReader().getDicomObject())) != null) {
            Double calibX = DicomUtils.getDoubleFromDicomElement(spatialCalibration, 1597484, null);
            Double calibY = DicomUtils.getDoubleFromDicomElement(spatialCalibration, 1597486, null);
            if (calibX != null && calibY != null) {
                calibX = Math.abs(calibX);
                calibY = Math.abs(calibY);
                if (MathUtil.isEqual((double)calibX, (double)calibY)) {
                    this.setPixelSize(calibX, calibY);
                    this.pixelSpacingUnit = Unit.CENTIMETER;
                }
            }
        }
    }

    private void initPixelAspectRatio() {
        int[] aspects = TagD.getTagValue((TagReadable)this.mediaIO, 2621492, int[].class);
        if (aspects != null && aspects.length == 2 && aspects[0] != aspects[1]) {
            if (aspects[1] < aspects[0]) {
                this.setPixelSize(1.0, (double)aspects[0] / (double)aspects[1]);
            } else {
                this.setPixelSize((double)aspects[1] / (double)aspects[0], 1.0);
            }
        }
    }

    private double[] getMagnifiedPixelSpacing(boolean useMagnificationFactor) {
        double[] val = TagD.getTagValue((TagReadable)this.mediaIO, 1577316, double[].class);
        String string = this.pixelSizeCalibrationDescription = val == null ? null : "At Detector";
        if (useMagnificationFactor && val != null && val.length == 2 && val[0] > 0.0 && val[1] > 0.0) {
            Double estimatedFactor = TagD.getTagValue((TagReadable)this.mediaIO, 0x181114, Double.class);
            if (estimatedFactor == null) {
                Double distanceSourceToDetector = TagD.getTagValue((TagReadable)this.mediaIO, 0x181110, Double.class);
                Double distanceSourceToPatient = TagD.getTagValue((TagReadable)this.mediaIO, 0x181111, Double.class);
                if (distanceSourceToDetector != null && distanceSourceToPatient != null) {
                    estimatedFactor = distanceSourceToDetector / distanceSourceToPatient;
                }
            }
            if (estimatedFactor != null && estimatedFactor > 0.0) {
                val[0] = val[0] / estimatedFactor;
                val[1] = val[1] / estimatedFactor;
                this.pixelSizeCalibrationDescription = "Magnified";
            }
        }
        return val;
    }

    public double getMinValue(WlPresentation wlp) {
        if (this.isImageInitialized()) {
            return this.adapter.getMinValue(wlp);
        }
        return 0.0;
    }

    public double getMaxValue(WlPresentation wlp) {
        if (this.isImageInitialized()) {
            return this.adapter.getMaxValue(wlp);
        }
        return 0.0;
    }

    protected boolean isGrayImage(RenderedImage source) {
        Boolean val = (Boolean)this.getTagValue(TagW.MonoChrome);
        return val == null || val != false;
    }

    public boolean isPixelRepresentationSigned() {
        if (this.isImageInitialized()) {
            return this.adapter.getImageDescriptor().isSigned();
        }
        Integer pixelRepresentation = TagD.getTagValue((TagReadable)this, 2621699, Integer.class);
        return pixelRepresentation != null && pixelRepresentation != 0;
    }

    public int getBitsStored() {
        if (this.isImageInitialized()) {
            return this.adapter.getBitsStored();
        }
        return TagD.getTagValue((TagReadable)this, 2621697, Integer.class);
    }

    public int getBitsAllocated() {
        if (this.isImageInitialized()) {
            return this.adapter.getImageDescriptor().getBitsAllocated();
        }
        return TagD.getTagValue((TagReadable)this, 2621696, Integer.class);
    }

    public String toString() {
        return TagD.getTagValue((TagReadable)this, 524312, String.class);
    }

    @Override
    public DcmMediaReader getMediaReader() {
        return (DcmMediaReader)super.getMediaReader();
    }

    public Number pixelToRealValue(Number pixelValue, WlPresentation wlp) {
        if (pixelValue != null && this.isImageInitialized()) {
            return this.adapter.pixelToRealValue(pixelValue, wlp);
        }
        return pixelValue;
    }

    public PhotometricInterpretation getPhotometricInterpretation() {
        if (this.isImageInitialized()) {
            return this.adapter.getImageDescriptor().getPhotometricInterpretation();
        }
        return null;
    }

    protected boolean isImageInitialized() {
        if (this.adapter == null) {
            this.getImage(null, true);
        }
        return this.adapter != null;
    }

    public PlanarImage getModalityLutImage(OpManager manager, DicomImageReadParam params) {
        PlanarImage image = this.getImage(manager, this.adapter == null);
        if (image == null || this.adapter == null) {
            return image;
        }
        return ImageRendering.getModalityLutImage(image, this.adapter, params);
    }

    public synchronized PlanarImage getUncachedModalityLutImage(DicomImageReadParam params) {
        PlanarImage raw;
        try {
            raw = this.loadImage();
        }
        catch (Exception e) {
            LOGGER.error("Cannot read image: {}", (Object)this, (Object)e);
            return null;
        }
        if (raw == null || raw.width() <= 0) {
            return null;
        }
        this.findMinMaxValues(raw, true);
        if (this.adapter == null) {
            return raw;
        }
        PlanarImage modImage = ImageRendering.getModalityLutImage(raw, this.adapter, params);
        if (modImage != raw) {
            raw.release();
        }
        return modImage;
    }

    public LutParameters getModalityLutParameters(boolean pixelPadding, LookupTableCV mLUTSeq, boolean inversePaddingMLUT, PrDicomObject pr) {
        if (this.isImageInitialized()) {
            return this.adapter.getLutParameters(pixelPadding, mLUTSeq, inversePaddingMLUT, pr);
        }
        return null;
    }

    public boolean isPhotometricInterpretationInverse(PresentationStateLut pr) {
        if (this.isImageInitialized()) {
            return this.adapter.isPhotometricInterpretationInverse(pr);
        }
        return false;
    }

    public LookupTableCV getVOILookup(WlParams wl) {
        if (this.isImageInitialized()) {
            return this.adapter.getVOILookup(wl);
        }
        return null;
    }

    public PresetWindowLevel getDefaultPreset(WlPresentation wlp) {
        if (this.isImageInitialized()) {
            return this.adapter.getDefaultPreset(wlp);
        }
        return null;
    }

    public boolean containsPreset(PresetWindowLevel preset) {
        List<PresetWindowLevel> collection;
        if (preset != null && this.isImageInitialized() && this.adapter.getPresetCollectionSize() > 0 && (collection = this.adapter.getPresetList(null)) != null) {
            return collection.contains(preset);
        }
        return false;
    }

    public synchronized Collection<LutShape> getLutShapeCollection(WlPresentation wlp) {
        List<PresetWindowLevel> presetList;
        if (this.lutShapeCollection != null) {
            return this.lutShapeCollection;
        }
        this.lutShapeCollection = new LinkedHashSet<LutShape>();
        if (this.isImageInitialized() && (presetList = this.adapter.getPresetList(wlp)) != null) {
            for (PresetWindowLevel preset : presetList) {
                this.lutShapeCollection.add(preset.getLutShape());
            }
        }
        this.lutShapeCollection.addAll(DEFAULT_LUT_FUNCTIONS);
        return this.lutShapeCollection;
    }

    public List<PresetWindowLevel> getPresetList(WlPresentation wl) {
        return this.getPresetList(wl, false);
    }

    public List<PresetWindowLevel> getPresetList(WlPresentation wl, boolean reload) {
        if (this.isImageInitialized()) {
            return this.adapter.getPresetList(wl, reload);
        }
        return Collections.emptyList();
    }

    protected void findMinMaxValues(PlanarImage img, boolean exclude8bitImage) {
        DicomMetaData meta;
        if (img != null && !this.isImageAvailable() && img.width() > 0 && (meta = this.getMediaReader().getDicomMetaData()) != null) {
            int frameIndex = 0;
            Object object = this.getKey();
            if (object instanceof Integer) {
                Integer intVal = (Integer)object;
                frameIndex = intVal;
            }
            this.adapter = new DicomImageAdapter(img, meta.getImageDescriptor(), frameIndex);
            Core.MinMaxLocResult val = this.adapter.getMinMax();
            if (val != null) {
                this.minPixelValue = val.minVal;
                this.maxPixelValue = val.maxVal;
            }
        }
    }

    public double[] getDisplayPixelSize() {
        return new double[]{this.pixelSizeX, this.pixelSizeY};
    }

    public LutShape getDefaultShape(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = this.getDefaultPreset(wlp);
        return defaultPreset != null ? defaultPreset.getLutShape() : super.getDefaultShape(null);
    }

    public double getDefaultWindow(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = this.getDefaultPreset(wlp);
        return defaultPreset != null ? defaultPreset.getWindow() : super.getDefaultWindow(null);
    }

    public double getDefaultLevel(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = this.getDefaultPreset(wlp);
        return defaultPreset != null ? defaultPreset.getLevel() : super.getDefaultLevel(null);
    }

    public PlanarImage getRenderedImage(PlanarImage imageSource, Map<String, Object> params) {
        if (imageSource == null) {
            return null;
        }
        DicomMetaData meta = this.getMediaReader().getDicomMetaData();
        if (meta != null) {
            DicomImageReadParam readParams = new DicomImageReadParam();
            if (params != null) {
                readParams.setPresentationState((PrDicomObject)params.get("pr.element"));
                readParams.setWindowCenter((Double)params.get(ActionW.LEVEL.cmd()));
                readParams.setWindowWidth((Double)params.get(ActionW.WINDOW.cmd()));
                readParams.setLevelMin((Double)params.get(ActionW.LEVEL_MIN.cmd()));
                readParams.setLevelMax((Double)params.get(ActionW.LEVEL_MAX.cmd()));
                readParams.setVoiLutShape((LutShape)params.get(ActionW.LUT_SHAPE.cmd()));
                readParams.setApplyPixelPadding((Boolean)params.get(ActionW.IMAGE_PIX_PADDING.cmd()));
                readParams.setApplyWindowLevelToColorImage((Boolean)params.get("weasis.color.wl.apply"));
                readParams.setInverseLut((Boolean)params.get("weasis.level.inverse"));
                readParams.setFillOutsideLutRange((Boolean)params.get("fill.outside.lut"));
            }
            if (this.isImageInitialized()) {
                return ImageRendering.getVoiLutImage(imageSource, this.adapter, readParams);
            }
        }
        return null;
    }

    public GeometryOfSlice getSliceGeometry() {
        return this.getGeometry(true);
    }

    public GeometryOfSlice getRawSliceGeometry() {
        return this.getGeometry(false);
    }

    private GeometryOfSlice getGeometry(boolean isDisplay) {
        double[] pos;
        double[] imgOr = TagD.getTagValue((TagReadable)this, 2097207, double[].class);
        if (imgOr != null && imgOr.length == 6 && (pos = TagD.getTagValue((TagReadable)this, 0x200032, double[].class)) != null && pos.length == 3) {
            double[] dArray;
            Double sliceTickness = TagD.getTagValue((TagReadable)this, 1572944, Double.class);
            if (sliceTickness == null) {
                sliceTickness = this.getPixelSize();
            }
            if (isDisplay) {
                double[] dArray2 = new double[2];
                dArray2[0] = this.getPixelSize();
                dArray = dArray2;
                dArray2[1] = this.getPixelSize();
            } else {
                dArray = this.getDisplayPixelSize();
            }
            double[] pixSize = dArray;
            Vector3d spacing = new Vector3d(pixSize[0], pixSize[1], sliceTickness.doubleValue());
            Integer rows = TagD.getTagValue((TagReadable)this, 2621456, Integer.class);
            Integer columns = TagD.getTagValue((TagReadable)this, 2621457, Integer.class);
            if (rows != null && columns != null && rows > 0 && columns > 0) {
                Vector3d dimensions = isDisplay ? new Vector3d((double)rows.intValue() * this.getRescaleY(), (double)columns.intValue() * this.getRescaleX(), 1.0) : new Vector3d((double)rows.intValue(), (double)columns.intValue(), 1.0);
                return new GeometryOfSlice(new Vector3d(imgOr[0], imgOr[1], imgOr[2]), new Vector3d(imgOr[3], imgOr[4], imgOr[5]), new Vector3d(pos), spacing, sliceTickness, dimensions);
            }
        }
        return null;
    }

    @Override
    public Attributes saveToFile(File output, DicomElement.DicomExportParameters params) {
        boolean hasTransformation;
        boolean bl = hasTransformation = params.dicomEditors() != null && !params.dicomEditors().isEmpty();
        if (!hasTransformation && params.syntax() == null) {
            super.saveToFile(output);
            return new Attributes();
        }
        DicomMetaData metaData = this.getMediaReader().getDicomMetaData();
        String outputTsuid = params.syntax() == null ? metaData.getTransferSyntaxUID() : params.syntax().getTransferSyntaxUID();
        outputTsuid = DicomImageElement.getOutputTransferSyntax(params.onlyRaw(), metaData.getTransferSyntaxUID(), outputTsuid);
        ImageAdapter.AdaptTransferSyntax adaptTransferSyntax = new ImageAdapter.AdaptTransferSyntax(metaData.getTransferSyntaxUID(), outputTsuid);
        adaptTransferSyntax.setJpegQuality(params.compressionQuality());
        adaptTransferSyntax.setCompressionRatioFactor(params.compressionRatioFactor());
        Attributes attributes = new Attributes(metaData.getDicomObject());
        AttributeEditorContext context = new AttributeEditorContext(adaptTransferSyntax.getOriginal(), null, null);
        if (hasTransformation) {
            params.dicomEditors().forEach(e -> e.apply(attributes, context));
        }
        BytesWithImageDescriptor desc = ImageAdapter.imageTranscode(attributes, adaptTransferSyntax, context);
        if (ImageAdapter.writeDicomFile(attributes, adaptTransferSyntax, context.getEditable(), desc, output.toPath())) {
            return attributes;
        }
        LOGGER.error("Cannot export DICOM file: {}", this.getFileCache().getOriginalFile().orElse(null));
        return null;
    }

    public SimpleOpManager buildSimpleOpManager(boolean img16, boolean padding, boolean shutter, boolean overlay, double ratio) {
        SimpleOpManager manager = new SimpleOpManager();
        PlanarImage image = this.getImage(null);
        if (image != null) {
            if (img16) {
                DicomImageReadParam params = new DicomImageReadParam();
                params.setApplyPixelPadding(padding);
                image = this.getModalityLutImage(null, params);
                if (CvType.depth((int)image.type()) == 3) {
                    ImageCV dstImg = new ImageCV();
                    image.toImageCV().convertTo((Mat)dstImg, CvType.CV_16UC((int)image.channels()), 1.0, 32768.0);
                    image = dstImg;
                }
            } else {
                manager.addImageOperationAction((ImageOpNode)new WindowAndPresetsOp());
                manager.setParamValue(WindowOp.OP_NAME, "img.element", (Object)this);
                manager.setParamValue(WindowOp.OP_NAME, ActionW.IMAGE_PIX_PADDING.cmd(), (Object)padding);
                manager.setParamValue(WindowOp.OP_NAME, ActionW.DEFAULT_PRESET.cmd(), (Object)true);
            }
            if (shutter) {
                manager.addImageOperationAction((ImageOpNode)new ShutterOp());
                manager.setParamValue(ShutterOp.OP_NAME, "img.element", (Object)this);
                manager.setParamValue(ShutterOp.OP_NAME, "show", (Object)true);
                manager.setParamValue(ShutterOp.OP_NAME, "shape", this.getTagValue(TagW.ShutterFinalShape));
                manager.setParamValue(ShutterOp.OP_NAME, "ps.value", this.getTagValue(TagW.ShutterPSValue));
                manager.setParamValue(ShutterOp.OP_NAME, "rgb.color", this.getTagValue(TagW.ShutterRGBColor));
            }
            if (overlay) {
                manager.addImageOperationAction((ImageOpNode)new OverlayOp());
                manager.setParamValue(OverlayOp.OP_NAME, "img.element", (Object)this);
                manager.setParamValue(OverlayOp.OP_NAME, "overlay", (Object)true);
            }
            manager.addImageOperationAction((ImageOpNode)this.buildZoomOp(ratio));
            manager.setFirstNode(image);
        }
        return manager;
    }

    private static String getOutputTransferSyntax(boolean onlyRaw, String originalTsuid, String outputTsuid) {
        if (outputTsuid == null) {
            return originalTsuid;
        }
        if (onlyRaw && !DicomUtils.isNative(originalTsuid) && !"1.2.840.10008.1.2.5".equals(originalTsuid)) {
            return originalTsuid;
        }
        if (DicomOutputData.isSupportedSyntax(outputTsuid) && DicomImageReader.isSupportedSyntax(originalTsuid)) {
            return outputTsuid;
        }
        if ("1.2.840.10008.1.2.5".equals(originalTsuid) || "1.2.840.10008.1.2".equals(originalTsuid) || "1.2.840.10008.1.2.2".equals(originalTsuid)) {
            return "1.2.840.10008.1.2.1";
        }
        return originalTsuid;
    }
}

