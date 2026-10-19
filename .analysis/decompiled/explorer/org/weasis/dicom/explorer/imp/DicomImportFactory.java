/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.explorer.imp;

import java.util.Hashtable;
import org.weasis.dicom.explorer.imp.ImportDicom;

public interface DicomImportFactory {
    public ImportDicom createDicomImportPage(Hashtable<String, Object> var1);
}

