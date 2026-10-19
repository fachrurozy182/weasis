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

public class GenozipFileDetector
extends FileTypeDetector {
    public static final String APPLICATION_VND_GENOZIP = "application/vnd.genozip";

    @Override
    public String probeContentType(Path path) throws IOException {
        byte[] b = new byte[4];
        try (InputStream in = Files.newInputStream(path, new OpenOption[0]);){
            String string = StreamUtils.readAvailable(in, b, 0, 4) == 4 && b[0] == 39 && b[1] == 5 && b[2] == 32 && b[3] == 18 ? APPLICATION_VND_GENOZIP : null;
            return string;
        }
    }
}

