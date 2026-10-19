/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core$MinMaxLocResult
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.opencv.data.LookupTableCV
 */
package org.dcm4che3.img.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.img.data.EmbeddedOverlay;
import org.dcm4che3.img.data.OverlayData;
import org.dcm4che3.img.lut.ModalityLutModule;
import org.dcm4che3.img.lut.VoiLutModule;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.img.util.PaletteColorUtils;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.ref.AnatomicRegion;
import org.weasis.opencv.data.LookupTableCV;

public final class ImageDescriptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageDescriptor.class);
    private final int rows;
    private final int columns;
    private final int frames;
    private final int samples;
    private final PhotometricInterpretation photometricInterpretation;
    private final int bitsAllocated;
    private final int bitsStored;
    private final int bitsCompressed;
    private final int highBit;
    private final int pixelRepresentation;
    private final int planarConfiguration;
    private final String pixelPresentation;
    private final LookupTableCV paletteColorLookupTable;
    private final String sopClassUID;
    private final String seriesInstanceUID;
    private final String modality;
    private final String stationName;
    private final AnatomicRegion anatomicRegion;
    private final List<EmbeddedOverlay> embeddedOverlay;
    private final List<OverlayData> overlayData;
    private final String presentationLUTShape;
    private final Integer pixelPaddingValue;
    private final Integer pixelPaddingRangeLimit;
    private final ModalityLutModule modalityLUT;
    private final VoiLutModule voiLUT;
    private final List<Core.MinMaxLocResult> minMaxPixelValues;
    private final List<VoiLutModule> voiLutPerFrame;
    private final List<ModalityLutModule> modalityLutPerFrame;

    public ImageDescriptor(Attributes dcm) {
        this(dcm, 0);
    }

    public ImageDescriptor(Attributes dcm, int bitsCompressed) {
        Objects.requireNonNull(dcm, "DICOM attributes cannot be null");
        this.rows = Math.max(dcm.getInt(2621456, 0), 0);
        this.columns = Math.max(dcm.getInt(2621457, 0), 0);
        this.frames = Math.max(dcm.getInt(0x280008, 1), 1);
        PixelAttributes pixelAttributes = this.initializePixelAttributes(dcm, bitsCompressed);
        this.samples = pixelAttributes.samples();
        this.photometricInterpretation = pixelAttributes.photometricInterpretation();
        this.bitsAllocated = pixelAttributes.bitsAllocated();
        this.bitsStored = pixelAttributes.bitsStored();
        this.bitsCompressed = pixelAttributes.bitsCompressed();
        this.highBit = pixelAttributes.highBit();
        this.pixelRepresentation = pixelAttributes.pixelRepresentation();
        this.planarConfiguration = pixelAttributes.planarConfiguration();
        this.pixelPresentation = pixelAttributes.pixelPresentation();
        this.paletteColorLookupTable = this.createPaletteColorLookupTable(dcm);
        DicomMetadata metadata = this.createDicomMetadata(dcm);
        this.sopClassUID = metadata.sopClassUID();
        this.seriesInstanceUID = metadata.seriesInstanceUID();
        this.modality = metadata.modality();
        this.stationName = metadata.stationName();
        this.anatomicRegion = metadata.anatomicRegion();
        OverlayDataContainer overlayContainer = this.createOverlayData(dcm);
        this.embeddedOverlay = overlayContainer.embeddedOverlays();
        this.overlayData = overlayContainer.overlays();
        this.presentationLUTShape = overlayContainer.presentationLUTShape();
        LutDataContainer lutData = this.createLutData(dcm);
        this.pixelPaddingValue = lutData.pixelPaddingValue();
        this.pixelPaddingRangeLimit = lutData.pixelPaddingRangeLimit();
        this.modalityLUT = lutData.modalityLUT();
        this.voiLUT = lutData.voiLUT();
        this.minMaxPixelValues = ImageDescriptor.createNullFilledList(this.frames);
        this.voiLutPerFrame = ImageDescriptor.createNullFilledList(this.frames);
        this.modalityLutPerFrame = ImageDescriptor.createNullFilledList(this.frames);
    }

    private LookupTableCV createPaletteColorLookupTable(Attributes dcm) {
        if (this.hasPaletteColorLookupTable()) {
            LookupTableCV lookup = PaletteColorUtils.getPaletteColorLookupTable(dcm);
            if (lookup != null) {
                return lookup;
            }
            LOGGER.warn("No palette color lookup table found in DICOM attributes");
        }
        return null;
    }

    private PixelAttributes initializePixelAttributes(Attributes dcm, int bitsCompressed) {
        int numSamples = Math.max(dcm.getInt(0x280002, 1), 1);
        PhotometricInterpretation pixelColorModel = PhotometricInterpretation.fromString(dcm.getString(2621444, "MONOCHROME2"));
        int maxBitsAllocated = Math.max(dcm.getInt(2621696, 8), 1);
        int bitsStoredValue = Math.min(Math.max(dcm.getInt(2621697, maxBitsAllocated), 1), maxBitsAllocated);
        int highBitValue = Math.min(dcm.getInt(2621698, bitsStoredValue - 1), bitsStoredValue - 1);
        int finalBitsCompressed = bitsCompressed > 0 ? Math.min(bitsCompressed, maxBitsAllocated) : bitsStoredValue;
        int pixelRepValue = dcm.getInt(2621699, 0);
        int planarConfig = dcm.getInt(2621446, 0);
        String presentationType = dcm.getString(561669);
        return new PixelAttributes(numSamples, pixelColorModel, maxBitsAllocated, bitsStoredValue, finalBitsCompressed, highBitValue, pixelRepValue, planarConfig, presentationType);
    }

    private DicomMetadata createDicomMetadata(Attributes dcm) {
        return new DicomMetadata(dcm.getString(524310), dcm.getString(0x20000E), dcm.getString(524384), dcm.getString(528400), AnatomicRegion.read(dcm));
    }

    private OverlayDataContainer createOverlayData(Attributes dcm) {
        String lutShape = dcm.getString(0x20500020);
        List<EmbeddedOverlay> embeddedOverlays = EmbeddedOverlay.getEmbeddedOverlay(dcm);
        List<OverlayData> overlays = OverlayData.getOverlayData(dcm, 65535);
        return new OverlayDataContainer(embeddedOverlays, overlays, lutShape);
    }

    private LutDataContainer createLutData(Attributes dcm) {
        Integer paddingValue = DicomUtils.getIntegerFromDicomElement(dcm, 2621728, null);
        Integer paddingRangeLimit = DicomUtils.getIntegerFromDicomElement(dcm, 2621729, null);
        return new LutDataContainer(paddingValue, paddingRangeLimit, new ModalityLutModule(dcm), new VoiLutModule(dcm));
    }

    private static <T> List<T> createNullFilledList(int size) {
        return new ArrayList<Object>(Collections.nCopies(size, null));
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    public int getFrames() {
        return this.frames;
    }

    public int getSamples() {
        return this.samples;
    }

    public boolean isMultiframe() {
        return this.frames > 1;
    }

    public PhotometricInterpretation getPhotometricInterpretation() {
        return this.photometricInterpretation;
    }

    public int getBitsAllocated() {
        return this.bitsAllocated;
    }

    public int getBitsStored() {
        return this.bitsStored;
    }

    public int getBitsCompressed() {
        return this.bitsCompressed;
    }

    public int getHighBit() {
        return this.highBit;
    }

    public int getPixelRepresentation() {
        return this.pixelRepresentation;
    }

    public int getPlanarConfiguration() {
        return this.planarConfiguration;
    }

    public String getPixelPresentation() {
        return this.pixelPresentation;
    }

    public boolean isSigned() {
        return this.pixelRepresentation != 0;
    }

    public boolean isBanded() {
        return this.planarConfiguration != 0;
    }

    public LookupTableCV getPaletteColorLookupTable() {
        return this.paletteColorLookupTable;
    }

    public boolean hasPaletteColorLookupTable() {
        return this.photometricInterpretation == PhotometricInterpretation.PALETTE_COLOR || this.photometricInterpretation.isMonochrome() && this.pixelPresentation != null && this.pixelPresentation.contains("COLOR");
    }

    public boolean isFloatPixelData() {
        return this.bitsAllocated == 32 && !"RTDOSE".equals(this.modality) || this.bitsAllocated == 64;
    }

    public int getFrameLength() {
        return this.rows * this.columns * this.samples * this.bitsAllocated / 8;
    }

    public int getLength() {
        return this.getFrameLength() * this.frames;
    }

    public String getSopClassUID() {
        return this.sopClassUID;
    }

    public String getSeriesInstanceUID() {
        return this.seriesInstanceUID;
    }

    public String getModality() {
        return this.modality;
    }

    public String getStationName() {
        return this.stationName;
    }

    public AnatomicRegion getAnatomicRegion() {
        return this.anatomicRegion;
    }

    public List<EmbeddedOverlay> getEmbeddedOverlay() {
        return Collections.unmodifiableList(this.embeddedOverlay);
    }

    public List<OverlayData> getOverlayData() {
        return Collections.unmodifiableList(this.overlayData);
    }

    public boolean isMultiframeWithEmbeddedOverlays() {
        return this.isMultiframe() && !this.embeddedOverlay.isEmpty();
    }

    public String getPresentationLUTShape() {
        return this.presentationLUTShape;
    }

    public Optional<Integer> getPixelPaddingValue() {
        return Optional.ofNullable(this.pixelPaddingValue);
    }

    public Optional<Integer> getPixelPaddingRangeLimit() {
        return Optional.ofNullable(this.pixelPaddingRangeLimit);
    }

    public ModalityLutModule getModalityLUT() {
        return this.modalityLUT;
    }

    public VoiLutModule getVoiLUT() {
        return this.voiLUT;
    }

    public Core.MinMaxLocResult getMinMaxPixelValue(int frame) {
        return this.isValidFrameIndex(frame) ? this.minMaxPixelValues.get(frame) : null;
    }

    public void setMinMaxPixelValue(int frame, Core.MinMaxLocResult minMaxPixelValue) {
        if (this.isValidFrameIndex(frame)) {
            this.minMaxPixelValues.set(frame, minMaxPixelValue);
        }
    }

    public VoiLutModule getVoiLutForFrame(int frame) {
        return ImageDescriptor.getLutModule(this.voiLutPerFrame, this.voiLUT, frame);
    }

    public void setVoiLutForFrame(int frame, VoiLutModule voiLut) {
        if (this.isValidFrameIndex(frame)) {
            this.voiLutPerFrame.set(frame, voiLut);
        }
    }

    public ModalityLutModule getModalityLutForFrame(int frame) {
        return ImageDescriptor.getLutModule(this.modalityLutPerFrame, this.modalityLUT, frame);
    }

    public void setModalityLutForFrame(int frame, ModalityLutModule modalityLut) {
        if (this.isValidFrameIndex(frame)) {
            this.modalityLutPerFrame.set(frame, modalityLut);
        }
    }

    private boolean isValidFrameIndex(int frame) {
        return frame >= 0 && frame < this.frames;
    }

    private static <T> T getLutModule(List<T> list, T baseLut, int frame) {
        if (frame < 0 || frame >= list.size()) {
            return baseLut;
        }
        T frameLut = list.get(frame);
        return frameLut != null ? frameLut : baseLut;
    }

    public void resetModalityLutForFrame(int frame) {
        if (this.isValidFrameIndex(frame)) {
            ModalityLutModule current = this.getModalityLutForFrame(frame);
            Double slope = current.getRescaleSlope().isPresent() ? Double.valueOf(current.getRescaleSlope().getAsDouble()) : null;
            Double intercept = current.getRescaleIntercept().isPresent() ? Double.valueOf(current.getRescaleIntercept().getAsDouble()) : null;
            this.modalityLutPerFrame.set(frame, ModalityLutModule.getResetInstance(slope, intercept, current.getRescaleType().orElse(null)));
        }
    }

    private record PixelAttributes(int samples, PhotometricInterpretation photometricInterpretation, int bitsAllocated, int bitsStored, int bitsCompressed, int highBit, int pixelRepresentation, int planarConfiguration, String pixelPresentation) {
    }

    private record DicomMetadata(String sopClassUID, String seriesInstanceUID, String modality, String stationName, AnatomicRegion anatomicRegion) {
    }

    private record OverlayDataContainer(List<EmbeddedOverlay> embeddedOverlays, List<OverlayData> overlays, String presentationLUTShape) {
    }

    private record LutDataContainer(Integer pixelPaddingValue, Integer pixelPaddingRangeLimit, ModalityLutModule modalityLUT, VoiLutModule voiLUT) {
    }
}

