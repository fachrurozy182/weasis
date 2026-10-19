/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import org.dcm4che3.data.Attributes;
import org.weasis.dicom.param.AttributeEditorContext;

@FunctionalInterface
public interface AttributeEditor {
    public void apply(Attributes var1, AttributeEditorContext var2);
}

