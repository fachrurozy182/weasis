/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.mf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.MathUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.mf.ManifestNode;
import org.weasis.dicom.mf.ManifestSerializer;
import org.weasis.dicom.mf.SopInstance;

public class Series
implements ManifestNode,
Comparable<Series> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Series.class);
    private static final int MIN_COMPRESSION = 0;
    private static final int MAX_COMPRESSION = 100;
    private static final String ATTR_DIRECT_DOWNLOAD_THUMBNAIL = "DirectDownloadThumbnail";
    private static final String ATTR_WADO_TRANSFER_SYNTAX_UID = "WadoTransferSyntaxUID";
    private static final String ATTR_WADO_COMPRESSION_RATE = "WadoCompressionRate";
    private static final String KEY_SERIES_INSTANCE_UID = ManifestNode.keyword(0x20000E);
    private static final String KEY_SERIES_DESCRIPTION = ManifestNode.keyword(528446);
    private static final String KEY_SERIES_NUMBER = ManifestNode.keyword(0x200011);
    private static final String KEY_MODALITY = ManifestNode.keyword(524384);
    private final String seriesInstanceUID;
    private final Map<String, SopInstance> sopInstanceMap;
    private String seriesDescription;
    private String modality;
    private String seriesNumber;
    private String wadoTransferSyntaxUID;
    private int wadoCompression = 0;
    private String thumbnail;

    public Series(String seriesInstanceUID) {
        this.seriesInstanceUID = Objects.requireNonNull(seriesInstanceUID, "Series Instance UID cannot be null");
        this.sopInstanceMap = new HashMap<String, SopInstance>();
    }

    public String getSeriesInstanceUID() {
        return this.seriesInstanceUID;
    }

    public String getSeriesDescription() {
        return this.seriesDescription;
    }

    public void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    public String getSeriesNumber() {
        return this.seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = StringUtil.hasText((CharSequence)seriesNumber) ? seriesNumber.trim() : null;
    }

    public String getModality() {
        return this.modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getWadoTransferSyntaxUID() {
        return this.wadoTransferSyntaxUID;
    }

    public void setWadoTransferSyntaxUID(String wadoTransferSyntaxUID) {
        this.wadoTransferSyntaxUID = wadoTransferSyntaxUID;
    }

    public int getWadoCompression() {
        return this.wadoCompression;
    }

    public void setWadoCompression(int wadoCompression) {
        this.wadoCompression = MathUtil.clamp((int)wadoCompression, (int)0, (int)100);
    }

    public void setWadoCompression(String wadoCompression) {
        if (!StringUtil.hasText((CharSequence)wadoCompression)) {
            return;
        }
        try {
            this.setWadoCompression(Integer.parseInt(wadoCompression.trim()));
        }
        catch (NumberFormatException e) {
            LOGGER.warn("Invalid compression value '{}', keeping current value: {}", (Object)wadoCompression, (Object)this.wadoCompression);
        }
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void addSopInstance(SopInstance sopInstance) {
        SopInstance.addSopInstance(this.sopInstanceMap, sopInstance);
    }

    public SopInstance removeSopInstance(String sopUID, Integer instanceNumber) {
        return SopInstance.removeSopInstance(this.sopInstanceMap, sopUID, instanceNumber);
    }

    public SopInstance getSopInstance(String sopUID, Integer instanceNumber) {
        return SopInstance.getSopInstance(this.sopInstanceMap, sopUID, instanceNumber);
    }

    public Collection<SopInstance> getSopInstances() {
        return this.sopInstanceMap.values();
    }

    public Set<Map.Entry<String, SopInstance>> getEntrySet() {
        return this.sopInstanceMap.entrySet();
    }

    public boolean isEmpty() {
        return this.sopInstanceMap.isEmpty();
    }

    @Override
    public void write(ManifestSerializer serializer) throws IOException {
        serializer.beginObject(ManifestNode.Level.SERIES.getTagName());
        serializer.attribute(KEY_SERIES_INSTANCE_UID, this.seriesInstanceUID);
        serializer.attribute(KEY_SERIES_DESCRIPTION, this.seriesDescription);
        serializer.attribute(KEY_SERIES_NUMBER, this.seriesNumber);
        serializer.attribute(KEY_MODALITY, this.modality);
        serializer.attribute(ATTR_DIRECT_DOWNLOAD_THUMBNAIL, this.thumbnail);
        serializer.attribute(ATTR_WADO_TRANSFER_SYNTAX_UID, this.wadoTransferSyntaxUID);
        serializer.attribute(ATTR_WADO_COMPRESSION_RATE, this.getCompressionRateString());
        this.writeSopInstances(serializer);
        serializer.endObject();
    }

    @Override
    public int compareTo(Series other) {
        int numberComparison = Series.compareSeriesNumbers(this.seriesNumber, other.seriesNumber);
        return numberComparison != 0 ? numberComparison : this.seriesInstanceUID.compareTo(other.seriesInstanceUID);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Series other = (Series)obj;
        return this.seriesInstanceUID.equals(other.seriesInstanceUID);
    }

    public int hashCode() {
        return Objects.hash(this.seriesInstanceUID);
    }

    private void writeSopInstances(ManifestSerializer serializer) throws IOException {
        if (this.sopInstanceMap.isEmpty()) {
            return;
        }
        ArrayList<SopInstance> sortedInstances = new ArrayList<SopInstance>(this.sopInstanceMap.values());
        Collections.sort(sortedInstances);
        serializer.beginArray(ManifestNode.Level.INSTANCE.getTagName());
        for (SopInstance instance : sortedInstances) {
            instance.write(serializer);
        }
        serializer.endArray();
    }

    private String getCompressionRateString() {
        return this.wadoCompression > 0 ? String.valueOf(this.wadoCompression) : null;
    }

    private static int compareSeriesNumbers(String number1, String number2) {
        Integer val1 = StringUtil.getInteger((String)number1);
        Integer val2 = StringUtil.getInteger((String)number2);
        if (val1 != null && val2 != null) {
            return val1.compareTo(val2);
        }
        if (val1 == null && val2 == null) {
            return 0;
        }
        return val1 == null ? 1 : -1;
    }
}

