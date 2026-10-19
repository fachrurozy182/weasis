/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;

public class DicomFileDetector
extends FileTypeDetector {
    public static final String APPLICATION_DICOM = "application/dicom";

    @Override
    public String probeContentType(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path, new OpenOption[0]);){
            byte[] b134 = new byte[134];
            int rlen = StreamUtils.readAvailable(in, b134, 0, 134);
            String string = rlen >= 8 && (DicomFileDetector.isPart10(b134, rlen) || DicomFileDetector.isIVR_LE(b134, rlen) || DicomFileDetector.isEVR(b134, rlen)) ? APPLICATION_DICOM : null;
            return string;
        }
    }

    private static boolean isPart10(byte[] b134, int rlen) {
        return rlen == 134 && b134[128] == 68 && b134[129] == 73 && b134[130] == 67 && b134[131] == 77 && b134[132] == 2 && b134[133] == 0;
    }

    private static boolean isIVR_LE(byte[] b134, int rlen) {
        int tag = ByteUtils.bytesToTagLE(b134, 0);
        int vlen = ByteUtils.bytesToIntLE(b134, 4);
        return TagUtils.isGroupLength(tag) ? vlen == 4 : ElementDictionary.getStandardElementDictionary().vrOf(tag) != VR.UN && 16 + vlen <= rlen;
    }

    private static boolean isEVR(byte[] b134, int rlen) {
        int tagLE = ByteUtils.bytesToTagLE(b134, 0);
        int tagBE = ByteUtils.bytesToTagBE(b134, 0);
        VR vr = VR.valueOf(ByteUtils.bytesToVR(b134, 4));
        return vr != null && vr == ElementDictionary.getStandardElementDictionary().vrOf(tagLE >= 0 && tagLE < tagBE ? tagLE : tagBE);
    }
}

