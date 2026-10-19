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
import org.dcm4che3.util.StreamUtils;

public class Bzip2FileDetector
extends FileTypeDetector {
    public static final String APPLICATION_X_BZIP2 = "application/x-bzip2";
    public static final String APPLICATION_PRS_VCFBZIP = "application/prs.vcfbzip";

    @Override
    public String probeContentType(Path path) throws IOException {
        byte[] b = new byte[3];
        try (InputStream in = Files.newInputStream(path, new OpenOption[0]);){
            String string = StreamUtils.readAvailable(in, b, 0, 3) == 3 && b[0] == 66 && b[1] == 90 && b[2] == 104 ? (Bzip2FileDetector.endsWith(path.getFileName().toString().toLowerCase(), ".vcf.bz2", ".vcfbzip2", ".vcfbz2") ? APPLICATION_PRS_VCFBZIP : APPLICATION_X_BZIP2) : null;
            return string;
        }
    }

    private static boolean endsWith(String filename, String ... exts) {
        for (String ext : exts) {
            if (!filename.endsWith(ext)) continue;
            return true;
        }
        return false;
    }
}

