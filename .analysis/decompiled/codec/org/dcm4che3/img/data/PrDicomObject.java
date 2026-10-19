/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.op.lut.PresentationStateLut
 */
package org.dcm4che3.img.data;

import java.awt.Color;
import java.awt.geom.Area;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.data.OverlayData;
import org.dcm4che3.img.lut.ModalityLutModule;
import org.dcm4che3.img.lut.VoiLutModule;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.util.DicomObjectUtil;
import org.dcm4che3.img.util.LookupTableUtils;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.op.lut.PresentationStateLut;

public class PrDicomObject
implements PresentationStateLut {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrDicomObject.class);
    private static final String IDENTITY_LUT_SHAPE = "IDENTITY";
    private final Attributes dcmPR;
    private final ModalityLutModule modalityLUT;
    private final List<OverlayData> overlays;
    private final List<OverlayData> shutterOverlays;
    private final VoiLutModule voiLUT;
    private final LookupTableCV prLut;
    private final String prLutExplanation;
    private final String prLUTShapeMode;
    private final PresentationStateType presentationStateType;

    public PrDicomObject(Attributes dcmPR) {
        this(dcmPR, null);
    }

    public PrDicomObject(Attributes dcmPR, ImageDescriptor desc) {
        this.dcmPR = Objects.requireNonNull(dcmPR);
        this.presentationStateType = this.validateAndGetPresentationStateType();
        this.modalityLUT = desc == null ? new ModalityLutModule(dcmPR) : desc.getModalityLUT();
        this.voiLUT = PrDicomObject.buildVoiLut(dcmPR);
        this.overlays = OverlayData.getPrOverlayData(dcmPR, -1);
        this.shutterOverlays = desc == null ? OverlayData.getOverlayData(dcmPR, 65535) : desc.getOverlayData();
        PresentationLutData lutData = this.initializePresentationLut();
        this.prLut = lutData.lut().orElse(null);
        this.prLutExplanation = lutData.explanation().orElse(null);
        this.prLUTShapeMode = lutData.shapeMode().orElse(null);
    }

    private PresentationStateType validateAndGetPresentationStateType() {
        String sopClassUid = this.dcmPR.getString(524310, "");
        Optional<PresentationStateType> type = PresentationStateType.fromUid(sopClassUid);
        if (type.isEmpty()) {
            throw new IllegalStateException("SOPClassUID '" + sopClassUid + "' does not match any supported DICOM Presentation State");
        }
        PresentationGroup group = type.get().group;
        if (group != PresentationGroup.BASIC_GRAYSCALE && group != PresentationGroup.COLOR_BASED) {
            LOGGER.warn("Presentation State '{}' is not fully supported", (Object)type.get().name());
        }
        return type.get();
    }

    private PresentationLutData initializePresentationLut() {
        Attributes dcmLut = this.dcmPR.getNestedDataset(542113808);
        return dcmLut != null ? this.createPresentationLutFromSequence(dcmLut) : this.createPresentationLutFromShape();
    }

    private PresentationLutData createPresentationLutFromSequence(Attributes dcmLut) {
        return new PresentationLutData(LookupTableUtils.createLut(dcmLut), Optional.ofNullable(this.dcmPR.getString(2633731)), Optional.of(IDENTITY_LUT_SHAPE));
    }

    private PresentationLutData createPresentationLutFromShape() {
        return new PresentationLutData(Optional.empty(), Optional.empty(), Optional.ofNullable(this.dcmPR.getString(0x20500020)));
    }

    private static VoiLutModule buildVoiLut(Attributes dcmPR) {
        Attributes seqDcm = dcmPR.getNestedDataset(2634000);
        return seqDcm == null ? null : new VoiLutModule(seqDcm);
    }

    private boolean isApplicableByReference(int childTag, String seriesInstanceUID, String sopInstanceUID, int referenceNumber) {
        if (!StringUtil.hasText((CharSequence)seriesInstanceUID)) {
            return false;
        }
        return this.getReferencedSeriesSequence().stream().filter(refSeriesSeq -> seriesInstanceUID.equals(refSeriesSeq.getString(0x20000E))).anyMatch(refSeriesSeq -> this.isImageFrameApplicable((Attributes)refSeriesSeq, childTag, sopInstanceUID, referenceNumber));
    }

    private boolean isImageFrameApplicable(Attributes refSeriesSeq, int childTag, String sopInstanceUID, int referenceNumber) {
        List<Attributes> refImgSeq = DicomObjectUtil.getSequence(refSeriesSeq, 528704);
        return DicomObjectUtil.isImageFrameApplicableToReferencedImageSequence(refImgSeq, childTag, sopInstanceUID, referenceNumber, true);
    }

    public Attributes getDicomObject() {
        return this.dcmPR;
    }

    public LocalDateTime getPresentationCreationDateTime() {
        return DicomObjectUtil.dateTime(this.dcmPR, 7340162, 7340163);
    }

    public Optional<LookupTableCV> getPrLut() {
        return Optional.ofNullable(this.prLut);
    }

    public Optional<String> getPrLutExplanation() {
        return Optional.ofNullable(this.prLutExplanation);
    }

    public Optional<String> getPrLutShapeMode() {
        return Optional.ofNullable(this.prLUTShapeMode);
    }

    public PresentationStateType getPresentationStateType() {
        return this.presentationStateType;
    }

    public PresentationGroup getPresentationGroup() {
        return this.presentationStateType.getGroup();
    }

    public String getSopClassDescription() {
        return this.presentationStateType.getDescription();
    }

    public ModalityLutModule getModalityLutModule() {
        return this.modalityLUT;
    }

    public Optional<VoiLutModule> getVoiLUT() {
        return Optional.ofNullable(this.voiLUT);
    }

    public List<OverlayData> getOverlays() {
        return this.overlays;
    }

    public List<OverlayData> getShutterOverlays() {
        return this.shutterOverlays;
    }

    public String getPrContentLabel() {
        return this.dcmPR.getString(0x700080, "PR " + this.dcmPR.getInt(2097171, 0));
    }

    public boolean hasOverlay() {
        return !this.overlays.isEmpty();
    }

    public List<Attributes> getReferencedSeriesSequence() {
        return DicomObjectUtil.getSequence(this.dcmPR, 528661);
    }

    public List<Attributes> getGraphicAnnotationSequence() {
        return DicomObjectUtil.getSequence(this.dcmPR, 0x700001);
    }

    public List<Attributes> getGraphicLayerSequence() {
        return DicomObjectUtil.getSequence(this.dcmPR, 0x700060);
    }

    public Area getShutterShape() {
        return DicomObjectUtil.getShutterShape(this.dcmPR);
    }

    public Color getShutterColor() {
        return DicomObjectUtil.getShutterColor(this.dcmPR);
    }

    public boolean supportsVolumetricRendering() {
        return this.presentationStateType.getGroup() == PresentationGroup.VOLUMETRIC;
    }

    public boolean supportsAdvancedBlending() {
        return this.presentationStateType.getGroup() == PresentationGroup.ADVANCED_BLENDING;
    }

    public static PrDicomObject getPresentationState(String prPath) throws IOException {
        return PrDicomObject.getPresentationState(Path.of(prPath, new String[0]));
    }

    public static PrDicomObject getPresentationState(Path prPath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(prPath, new OpenOption[0]);){
            PrDicomObject prDicomObject;
            try (DicomInputStream dis = new DicomInputStream(inputStream);){
                prDicomObject = new PrDicomObject(dis.readDataset());
            }
            return prDicomObject;
        }
    }

    public boolean isImageFrameApplicable(String seriesInstanceUID, String sopInstanceUID, int frame) {
        return this.isApplicableByReference(528736, seriesInstanceUID, sopInstanceUID, frame);
    }

    public boolean isSegmentationSegmentApplicable(String seriesInstanceUID, String sopInstanceUID, int segment) {
        return this.isApplicableByReference(6422539, seriesInstanceUID, sopInstanceUID, segment);
    }

    public static enum PresentationStateType {
        GRAYSCALE_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.1", "Grayscale Softcopy Presentation State Storage", PresentationGroup.BASIC_GRAYSCALE),
        COLOR_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.2", "Color Softcopy Presentation State Storage", PresentationGroup.COLOR_BASED),
        PSEUDO_COLOR_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.3", "Pseudo-Color Softcopy Presentation State Storage", PresentationGroup.COLOR_BASED),
        BLENDING_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.4", "Blending Softcopy Presentation State Storage", PresentationGroup.COLOR_BASED),
        XA_XRF_GRAYSCALE_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.5", "XA/XRF Grayscale Softcopy Presentation State Storage", PresentationGroup.SPECIALIZED),
        GRAYSCALE_PLANAR_MPR_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.6", "Grayscale Planar MPR Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),
        COMPOSITING_PLANAR_MPR_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.7", "Compositing Planar MPR Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),
        ADVANCED_BLENDING("1.2.840.10008.5.1.4.1.1.11.8", "Advanced Blending Presentation State Storage", PresentationGroup.ADVANCED_BLENDING),
        VOLUME_RENDERING_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.9", "Volume Rendering Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),
        SEGMENTED_VOLUME_RENDERING_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.10", "Segmented Volume Rendering Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),
        MULTIPLE_VOLUME_RENDERING_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.11", "Multiple Volume Rendering Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),
        VARIABLE_MODALITY_LUT_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.12", "Variable Modality LUT Softcopy Presentation State Storage", PresentationGroup.ADVANCED_LUT);

        private final String uid;
        private final String description;
        private final PresentationGroup group;

        private PresentationStateType(String uid, String description, PresentationGroup group) {
            this.uid = uid;
            this.description = description;
            this.group = group;
        }

        public String getUid() {
            return this.uid;
        }

        public String getDescription() {
            return this.description;
        }

        public PresentationGroup getGroup() {
            return this.group;
        }

        public static Optional<PresentationStateType> fromUid(String uid) {
            for (PresentationStateType type : PresentationStateType.values()) {
                if (!type.uid.equals(uid)) continue;
                return Optional.of(type);
            }
            return Optional.empty();
        }

        public static Set<PresentationStateType> getByGroup(PresentationGroup group) {
            return Set.of(PresentationStateType.values()).stream().filter(type -> type.group == group).collect(Collectors.toSet());
        }
    }

    private record PresentationLutData(Optional<LookupTableCV> lut, Optional<String> explanation, Optional<String> shapeMode) {
    }

    public static enum PresentationGroup {
        BASIC_GRAYSCALE("Basic grayscale presentation with standard windowing"),
        COLOR_BASED("Color and pseudo-color presentation states"),
        SPECIALIZED("Specialized modality-specific presentation states"),
        VOLUMETRIC("3D volumetric rendering presentation states"),
        ADVANCED_BLENDING("Advanced blending and compositing"),
        ADVANCED_LUT("Advanced LUT manipulation and variable modality LUT");

        private final String description;

        private PresentationGroup(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
}

