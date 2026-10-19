/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import org.weasis.dicom.param.AttributeEditor;

public final class CstoreParams {
    private static final String EMPTY_NEGOTIATION_MSG = "Extended negotiation is enabled but no configuration URL provided";
    private static final List<AttributeEditor> EMPTY_EDITORS = List.of();
    private final List<AttributeEditor> editors;
    private final boolean extendNegotiation;
    private final URL extendSopClassesURL;

    public CstoreParams(List<AttributeEditor> editors, boolean extendNegotiation, URL extendSopClassesURL) {
        CstoreParams.validateConfiguration(extendNegotiation, extendSopClassesURL);
        this.editors = editors != null ? List.copyOf(editors) : EMPTY_EDITORS;
        this.extendNegotiation = extendNegotiation;
        this.extendSopClassesURL = extendSopClassesURL;
    }

    public CstoreParams(List<AttributeEditor> editors) {
        this(editors, false, null);
    }

    public List<AttributeEditor> getDicomEditors() {
        return this.editors;
    }

    public boolean hasDicomEditors() {
        return !this.editors.isEmpty();
    }

    public boolean isExtendNegotiation() {
        return this.extendNegotiation;
    }

    public URL getExtendSopClassesURL() {
        return this.extendSopClassesURL;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CstoreParams)) return false;
        CstoreParams other = (CstoreParams)obj;
        if (!Objects.equals(this.editors, other.editors)) return false;
        if (this.extendNegotiation != other.extendNegotiation) return false;
        if (!Objects.equals(this.extendSopClassesURL, other.extendSopClassesURL)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.editors, this.extendNegotiation, this.extendSopClassesURL);
    }

    public String toString() {
        return "CstoreParams{editorsCount=" + this.editors.size() + ", extendNegotiation=" + this.extendNegotiation + ", extendSopClassesURL=" + String.valueOf(this.extendSopClassesURL) + "}";
    }

    private static void validateConfiguration(boolean extendNegotiation, URL extendSopClassesURL) {
        if (extendNegotiation && extendSopClassesURL == null) {
            throw new IllegalArgumentException(EMPTY_NEGOTIATION_MSG);
        }
    }
}

