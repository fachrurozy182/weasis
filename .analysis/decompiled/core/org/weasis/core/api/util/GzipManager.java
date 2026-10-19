/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.core.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StreamUtil;

public class GzipManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GzipManager.class);
    private static final String ERROR_CTX = "Cannot gzip uncompress";

    private GzipManager() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean gzipCompress(InputStream in, String gzipFilename) {
        try {
            boolean bl;
            FileOutputStream out = new FileOutputStream(gzipFilename);
            try {
                bl = GzipManager.gzipCompress(in, out);
            }
            catch (Throwable throwable) {
                try {
                    try {
                        out.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    LOGGER.error("Cannot gzip compress", (Throwable)e);
                    boolean bl2 = false;
                    return bl2;
                }
            }
            out.close();
            return bl;
        }
        finally {
            StreamUtil.safeClose((AutoCloseable)in);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean gzipCompress(InputStream in, OutputStream out) throws IOException {
        try {
            boolean bl;
            GZIPOutputStream gzipOut = new GZIPOutputStream(out);
            try {
                int offset;
                byte[] buf = new byte[1024];
                while ((offset = in.read(buf)) > 0) {
                    gzipOut.write(buf, 0, offset);
                }
                gzipOut.finish();
                bl = true;
            }
            catch (Throwable throwable) {
                try {
                    try {
                        gzipOut.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    LOGGER.error("Cannot gzip compress", (Throwable)e);
                    boolean bl2 = false;
                    return bl2;
                }
            }
            gzipOut.close();
            return bl;
        }
        finally {
            StreamUtil.safeClose((AutoCloseable)in);
        }
    }

    private static boolean gzipUncompress(InputStream inputStream, OutputStream out) throws IOException {
        try (GZIPInputStream in = new GZIPInputStream(inputStream);){
            int offset;
            byte[] buf = new byte[1024];
            while ((offset = in.read(buf)) > 0) {
                out.write(buf, 0, offset);
            }
            boolean bl = true;
            return bl;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static boolean gzipUncompressToFile(File inputFile, File outFilename) {
        try (FileInputStream inputStream = new FileInputStream(inputFile);){
            boolean bl;
            try (FileOutputStream outputStream = new FileOutputStream(outFilename);){
                bl = GzipManager.gzipUncompress(inputStream, outputStream);
            }
            return bl;
        }
        catch (IOException e) {
            LOGGER.error(ERROR_CTX, (Throwable)e);
            return false;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static boolean gzipUncompressToFile(URL url, File outFilename) {
        try (InputStream input = url.openStream();){
            boolean bl;
            try (FileOutputStream outputStream = new FileOutputStream(outFilename);){
                bl = GzipManager.gzipUncompress(input, outputStream);
            }
            return bl;
        }
        catch (IOException e) {
            LOGGER.error(ERROR_CTX, (Throwable)e);
            return false;
        }
    }

    public static byte[] gzipUncompressToByte(byte[] bytes) throws IOException {
        if (GzipManager.isGzip(bytes)) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
                byte[] byArray;
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);){
                    GzipManager.gzipUncompress(inputStream, outputStream);
                    byArray = outputStream.toByteArray();
                }
                return byArray;
            }
        }
        return bytes;
    }

    public static byte[] gzipCompressToByte(byte[] bytes) throws IOException {
        return GzipManager.gzipCompressToByte(bytes, 1);
    }

    public static byte[] gzipCompressToByte(byte[] bytes, int requiredByteNumber) throws IOException {
        if (bytes.length >= requiredByteNumber) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
                byte[] byArray;
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);){
                    GzipManager.gzipCompress((InputStream)inputStream, outputStream);
                    byArray = outputStream.toByteArray();
                }
                return byArray;
            }
        }
        return bytes;
    }

    public static boolean isGzip(byte[] bytes) {
        if (bytes != null && bytes.length >= 4) {
            int head = bytes[0] & 0xFF | bytes[1] << 8 & 0xFF00;
            return 35615 == head;
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean gzipUncompressToFile(byte[] bytes, File outFilename) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);){
            if (GzipManager.isGzip(bytes)) {
                try (FileOutputStream outputStream = new FileOutputStream(outFilename);){
                    boolean bl = GzipManager.gzipUncompress(inputStream, outputStream);
                    return bl;
                }
            }
            return FileUtil.writeStream((InputStream)inputStream, (Path)outFilename.toPath()) == -1;
        }
        catch (IOException e) {
            LOGGER.error(ERROR_CTX, (Throwable)e);
            return false;
        }
    }
}

