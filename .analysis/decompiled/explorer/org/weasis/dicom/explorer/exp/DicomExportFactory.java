/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.explorer.exp;

import java.util.Hashtable;
import org.weasis.dicom.explorer.exp.ExportDicom;

public interface DicomExportFactory {
    public ExportDicom createDicomExportPage(Hashtable<String, Object> var1);
}

