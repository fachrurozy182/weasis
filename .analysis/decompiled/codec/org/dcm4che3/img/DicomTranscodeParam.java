/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.dcm4che3.img;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.DicomJpegWriteParam;
import org.dcm4che3.img.DicomOutputData;
import org.dcm4che3.img.op.MaskArea;
import org.weasis.core.util.StringUtil;

public class DicomTranscodeParam {
    private static final String WILDCARD_MASK_KEY = "*";
    private final DicomImageReadParam readParam;
    private final DicomJpegWriteParam writeJpegParam;
    private final String outputTsuid;
    private final Map<String, MaskArea> maskMap;
    private boolean outputFmi;

    public DicomTranscodeParam(String dstTsuid) {
        this(null, dstTsuid);
    }

    public DicomTranscodeParam(DicomImageReadParam readParam, String dstTsuid) {
        this.outputTsuid = DicomTranscodeParam.validateTransferSyntax(dstTsuid);
        this.readParam = readParam != null ? readParam : new DicomImageReadParam();
        this.maskMap = new HashMap<String, MaskArea>();
        this.writeJpegParam = DicomOutputData.isNativeSyntax(dstTsuid) ? null : DicomJpegWriteParam.buildDicomImageWriteParam(dstTsuid);
        this.outputFmi = false;
    }

    private static String validateTransferSyntax(String tsuid) {
        if (!StringUtil.hasText((CharSequence)tsuid)) {
            throw new IllegalArgumentException("Transfer syntax UID cannot be null or empty");
        }
        return tsuid;
    }

    public DicomImageReadParam getReadParam() {
        return this.readParam;
    }

    public DicomJpegWriteParam getWriteJpegParam() {
        return this.writeJpegParam;
    }

    public boolean isOutputFmi() {
        return this.outputFmi;
    }

    public void setOutputFmi(boolean outputFmi) {
        this.outputFmi = outputFmi;
    }

    public String getOutputTsuid() {
        return this.outputTsuid;
    }

    public void addMaskMap(Map<? extends String, ? extends MaskArea> maskMap) {
        Objects.requireNonNull(maskMap, "Mask map cannot be null");
        this.maskMap.putAll(maskMap);
    }

    public MaskArea getMask(String key) {
        String exactKey = Objects.requireNonNullElse(key, WILDCARD_MASK_KEY);
        return Optional.ofNullable(this.maskMap.get(exactKey)).orElse(this.maskMap.get(WILDCARD_MASK_KEY));
    }

    public void addMask(String stationName, MaskArea maskArea) {
        String key = Objects.requireNonNullElse(stationName, WILDCARD_MASK_KEY);
        if (maskArea != null) {
            this.maskMap.put(key, maskArea);
        } else {
            this.maskMap.remove(key);
        }
    }

    public Map<String, MaskArea> getMaskMap() {
        return Map.copyOf(this.maskMap);
    }

    public boolean isCompressionEnabled() {
        return this.writeJpegParam != null;
    }

    public boolean hasMasks() {
        return !this.maskMap.isEmpty();
    }

    public void clearMasks() {
        this.maskMap.clear();
    }

    public boolean removeMask(String stationName) {
        String key = Objects.requireNonNullElse(stationName, WILDCARD_MASK_KEY);
        return this.maskMap.remove(key) != null;
    }

    public boolean hasMask(String stationName) {
        return this.getMask(stationName) != null;
    }

    public DicomTranscodeParam copy() {
        DicomTranscodeParam copy = new DicomTranscodeParam(this.readParam, this.outputTsuid);
        copy.outputFmi = this.outputFmi;
        copy.maskMap.putAll(this.maskMap);
        return copy;
    }

    public String toString() {
        return "DicomTranscodeParam{outputTsuid='" + this.outputTsuid + "', compression=" + (this.writeJpegParam != null ? "enabled" : "disabled") + ", outputFmi=" + this.outputFmi + ", masks=" + this.maskMap.size() + "}";
    }
}

