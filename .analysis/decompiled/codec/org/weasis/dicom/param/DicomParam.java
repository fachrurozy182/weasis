/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import java.util.Arrays;
import java.util.Objects;
import org.dcm4che3.data.ElementDictionary;

public class DicomParam {
    private final int tag;
    private final String[] values;
    private final int[] parentSeqTags;

    public DicomParam(int tag, String ... values) {
        this(null, tag, values);
    }

    public DicomParam(int[] parentSeqTags, int tag, String ... values) {
        this.tag = tag;
        this.values = values != null ? (String[])values.clone() : new String[]{};
        this.parentSeqTags = parentSeqTags != null ? (int[])parentSeqTags.clone() : null;
    }

    public int getTag() {
        return this.tag;
    }

    public String[] getValues() {
        return (String[])this.values.clone();
    }

    public int[] getParentSeqTags() {
        return this.parentSeqTags != null ? (int[])this.parentSeqTags.clone() : null;
    }

    public String getTagName() {
        return ElementDictionary.keywordOf(this.tag, null);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        DicomParam that = (DicomParam)obj;
        return this.tag == that.tag && Arrays.equals(this.values, that.values) && Arrays.equals(this.parentSeqTags, that.parentSeqTags);
    }

    public int hashCode() {
        return Objects.hash(this.tag, Arrays.hashCode(this.values), Arrays.hashCode(this.parentSeqTags));
    }

    public String toString() {
        return "DicomParam{tag=" + String.format("0x%08X", this.tag) + ", tagName='" + this.getTagName() + "', values=" + Arrays.toString(this.values) + ", parentSeqTags=" + Arrays.toString(this.parentSeqTags) + "}";
    }
}

