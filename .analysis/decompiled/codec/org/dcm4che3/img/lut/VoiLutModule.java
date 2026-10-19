/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.annotations.Generated
 *  org.weasis.opencv.data.LookupTableCV
 */
package org.dcm4che3.img.lut;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.DoubleStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.img.util.LookupTableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.annotations.Generated;
import org.weasis.opencv.data.LookupTableCV;

public final class VoiLutModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiLutModule.class);
    private final List<Double> windowCenter;
    private final List<Double> windowWidth;
    private final List<String> lutExplanation;
    private final List<LookupTableCV> lut;
    private final List<String> windowCenterWidthExplanation;
    private final String voiLutFunction;

    public VoiLutModule(Attributes dcm) {
        Objects.requireNonNull(dcm);
        double[] windowCenterArray = dcm.getDoubles(2625616);
        double[] windowWidthArray = dcm.getDoubles(2625617);
        this.windowCenter = windowCenterArray != null ? DoubleStream.of(windowCenterArray).boxed().toList() : List.of();
        this.windowWidth = windowWidthArray != null ? DoubleStream.of(windowWidthArray).boxed().toList() : List.of();
        this.voiLutFunction = this.hasWindowValues() ? dcm.getString(2625622) : null;
        this.windowCenterWidthExplanation = this.hasWindowValues() ? VoiLutModule.extractStringList(dcm, 2625621) : List.of();
        Sequence voiSeq = dcm.getSequence(2633744);
        if (voiSeq != null && !voiSeq.isEmpty()) {
            this.lutExplanation = VoiLutModule.extractLutExplanations(voiSeq);
            this.lut = VoiLutModule.extractLookupTables(voiSeq);
        } else {
            this.lutExplanation = List.of();
            this.lut = List.of();
        }
        if (LOGGER.isDebugEnabled()) {
            this.validateConsistency();
        }
    }

    private boolean hasWindowValues() {
        return !this.windowCenter.isEmpty() && !this.windowWidth.isEmpty();
    }

    private static List<String> extractStringList(Attributes dcm, int tag) {
        String[] values = DicomUtils.getStringArrayFromDicomElement(dcm, tag);
        return values != null ? List.of(values) : List.of();
    }

    private static List<String> extractLutExplanations(Sequence voiSeq) {
        return voiSeq.stream().map(item -> item.getString(2633731, "")).toList();
    }

    private static List<LookupTableCV> extractLookupTables(Sequence voiSeq) {
        return voiSeq.stream().map(LookupTableUtils::createLut).flatMap(Optional::stream).toList();
    }

    @Generated
    private void validateConsistency() {
        if (this.windowCenter.isEmpty() && !this.windowWidth.isEmpty()) {
            LOGGER.debug("VOI Window Center is required if Window Width is present");
        } else if (!this.windowCenter.isEmpty() && this.windowWidth.isEmpty()) {
            LOGGER.debug("VOI Window Width is required if Window Center is present");
        } else if (this.windowWidth.size() != this.windowCenter.size()) {
            LOGGER.debug("VOI Window Center and Width attributes have different number of values : {} => {}", (Object)this.windowCenter.size(), (Object)this.windowWidth.size());
        }
    }

    public List<Double> getWindowCenter() {
        return this.windowCenter;
    }

    public List<Double> getWindowWidth() {
        return this.windowWidth;
    }

    public List<String> getLutExplanation() {
        return this.lutExplanation;
    }

    public List<LookupTableCV> getLut() {
        return this.lut;
    }

    public List<String> getWindowCenterWidthExplanation() {
        return this.windowCenterWidthExplanation;
    }

    public Optional<String> getVoiLutFunction() {
        return Optional.ofNullable(this.voiLutFunction);
    }
}

