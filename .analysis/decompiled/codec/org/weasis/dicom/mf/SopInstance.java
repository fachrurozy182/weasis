/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.mf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.mf.ManifestNode;
import org.weasis.dicom.mf.ManifestSerializer;

public class SopInstance
implements ManifestNode,
Comparable<SopInstance> {
    private static final String KEY_SEPARATOR = "?";
    private static final String ATTR_DIRECT_DOWNLOAD_FILE = "DirectDownloadFile";
    private static final String KEY_SOP_INSTANCE_UID = ManifestNode.keyword(524312);
    private static final String KEY_SOP_CLASS_UID = ManifestNode.keyword(524310);
    private static final String KEY_TRANSFER_SYNTAX_UID = ManifestNode.keyword(131088);
    private static final String KEY_IMAGE_COMMENTS = ManifestNode.keyword(0x204000);
    private static final String KEY_INSTANCE_NUMBER = ManifestNode.keyword(2097171);
    private final String sopInstanceUID;
    private final String sopClassUID;
    private final Integer instanceNumber;
    private String imageComments;
    private String transferSyntaxUID;
    private String directDownloadFile;
    private Object graphicModel;

    public SopInstance(String sopInstanceUID, Integer instanceNumber) {
        this(sopInstanceUID, null, instanceNumber);
    }

    public SopInstance(String sopInstanceUID, String sopClassUID, Integer instanceNumber) {
        this.sopInstanceUID = Objects.requireNonNull(sopInstanceUID, "SOP Instance UID cannot be null");
        this.sopClassUID = sopClassUID;
        this.instanceNumber = instanceNumber;
    }

    public String getSopInstanceUID() {
        return this.sopInstanceUID;
    }

    public String getSopClassUID() {
        return this.sopClassUID;
    }

    public Integer getInstanceNumber() {
        return this.instanceNumber;
    }

    public String getStringInstanceNumber() {
        return this.instanceNumber != null ? this.instanceNumber.toString() : null;
    }

    public String getTransferSyntaxUID() {
        return this.transferSyntaxUID;
    }

    public void setTransferSyntaxUID(String transferSyntaxUID) {
        this.transferSyntaxUID = StringUtil.hasText((CharSequence)transferSyntaxUID) ? transferSyntaxUID.trim() : null;
    }

    public String getImageComments() {
        return this.imageComments;
    }

    public void setImageComments(String imageComments) {
        this.imageComments = imageComments;
    }

    public String getDirectDownloadFile() {
        return this.directDownloadFile;
    }

    public void setDirectDownloadFile(String directDownloadFile) {
        this.directDownloadFile = directDownloadFile;
    }

    public Object getGraphicModel() {
        return this.graphicModel;
    }

    public void setGraphicModel(Object graphicModel) {
        this.graphicModel = graphicModel;
    }

    @Override
    public void write(ManifestSerializer serializer) throws IOException {
        serializer.beginLeaf(ManifestNode.Level.INSTANCE.getTagName());
        serializer.attribute(KEY_SOP_INSTANCE_UID, this.sopInstanceUID);
        serializer.attribute(KEY_SOP_CLASS_UID, this.sopClassUID);
        serializer.attribute(KEY_TRANSFER_SYNTAX_UID, this.transferSyntaxUID);
        serializer.attribute(KEY_IMAGE_COMMENTS, this.imageComments);
        serializer.attribute(KEY_INSTANCE_NUMBER, this.getStringInstanceNumber());
        serializer.attribute(ATTR_DIRECT_DOWNLOAD_FILE, this.directDownloadFile);
        serializer.endLeaf();
    }

    @Override
    public int compareTo(SopInstance other) {
        int instanceComparison = SopInstance.compareInstanceNumbers(this.instanceNumber, other.instanceNumber);
        if (instanceComparison != 0) {
            return instanceComparison;
        }
        return SopInstance.compareNormalizedUIDs(this.sopInstanceUID, other.sopInstanceUID);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        SopInstance other = (SopInstance)obj;
        return Objects.equals(this.instanceNumber, other.instanceNumber) && this.sopInstanceUID.equals(other.sopInstanceUID);
    }

    public int hashCode() {
        return Objects.hash(this.sopInstanceUID, this.instanceNumber);
    }

    public static void addSopInstance(Map<String, SopInstance> sopInstanceMap, SopInstance sopInstance) {
        if (sopInstance != null && sopInstanceMap != null) {
            String key = SopInstance.buildMapKey(sopInstance.getSopInstanceUID(), sopInstance.getInstanceNumber());
            sopInstanceMap.put(key, sopInstance);
        }
    }

    public static SopInstance getSopInstance(Map<String, SopInstance> sopInstanceMap, String sopUID, Integer instanceNumber) {
        if (sopUID == null || sopInstanceMap == null) {
            return null;
        }
        return sopInstanceMap.get(SopInstance.buildMapKey(sopUID, instanceNumber));
    }

    public static SopInstance removeSopInstance(Map<String, SopInstance> sopInstanceMap, String sopUID, Integer instanceNumber) {
        if (sopUID == null || sopInstanceMap == null) {
            return null;
        }
        return sopInstanceMap.remove(SopInstance.buildMapKey(sopUID, instanceNumber));
    }

    private static int compareInstanceNumbers(Integer num1, Integer num2) {
        if (num1 != null && num2 != null) {
            return num1.compareTo(num2);
        }
        if (num1 == null && num2 != null) {
            return 1;
        }
        if (num1 != null) {
            return -1;
        }
        return 0;
    }

    private static int compareNormalizedUIDs(String uid1, String uid2) {
        int length2;
        int length1 = uid1.length();
        if (length1 < (length2 = uid2.length())) {
            return SopInstance.normalizeUID(uid1, length2 - length1).compareTo(uid2);
        }
        if (length1 > length2) {
            return uid1.compareTo(SopInstance.normalizeUID(uid2, length1 - length2));
        }
        return uid1.compareTo(uid2);
    }

    private static String normalizeUID(String uid, int paddingLength) {
        char[] padding = new char[paddingLength];
        Arrays.fill(padding, '0');
        int lastDotIndex = uid.lastIndexOf(46) + 1;
        return uid.substring(0, lastDotIndex) + new String(padding) + uid.substring(lastDotIndex);
    }

    private static String buildMapKey(String sopUID, Integer instanceNumber) {
        return instanceNumber != null ? sopUID + KEY_SEPARATOR + instanceNumber : sopUID;
    }
}

