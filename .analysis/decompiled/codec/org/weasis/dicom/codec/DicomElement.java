/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.codec;

import java.io.File;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.TransferSyntax;
import org.weasis.dicom.param.AttributeEditor;

public interface DicomElement {
    public DcmMediaReader getMediaReader();

    public Attributes saveToFile(File var1, DicomExportParameters var2);

    public record DicomExportParameters(TransferSyntax syntax, boolean onlyRaw, List<AttributeEditor> dicomEditors, int compressionQuality, int compressionRatioFactor) {
    }
}

