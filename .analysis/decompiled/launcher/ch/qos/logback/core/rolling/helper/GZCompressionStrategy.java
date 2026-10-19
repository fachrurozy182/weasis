/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.CompressionStrategyBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.WarnStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GZCompressionStrategy
extends CompressionStrategyBase {
    @Override
    public void compress(String originalFileName, String compressedFileName, String innerEntryName) {
        File gzedFile;
        File file2gz = new File(originalFileName);
        if (!file2gz.exists()) {
            this.addStatus(new WarnStatus("The file to compress named [" + originalFileName + "] does not exist.", this));
            return;
        }
        if (!((String)compressedFileName).endsWith(CompressionMode.GZ_SUFFIX)) {
            compressedFileName = (String)compressedFileName + CompressionMode.GZ_SUFFIX;
        }
        if ((gzedFile = new File((String)compressedFileName)).exists()) {
            this.addWarn("The target compressed file named [" + (String)compressedFileName + "] exist already. Aborting file compression.");
            return;
        }
        this.addInfo("GZ compressing [" + String.valueOf(file2gz) + "] as [" + String.valueOf(gzedFile) + "]");
        this.createMissingTargetDirsIfNecessary(gzedFile);
        boolean compressionSucceeded = false;
        try (FileInputStream fis = new FileInputStream(originalFileName);
             GZIPOutputStream gzos = new GZIPOutputStream((OutputStream)new FileOutputStream((String)compressedFileName), 65536);){
            int n;
            byte[] inbuf = new byte[65536];
            while ((n = fis.read(inbuf)) != -1) {
                gzos.write(inbuf, 0, n);
            }
            compressionSucceeded = true;
            this.addInfo("Done GZ compressing [" + String.valueOf(file2gz) + "] as [" + String.valueOf(gzedFile) + "]");
        }
        catch (Exception e) {
            this.addStatus(new ErrorStatus("Error occurred while compressing [" + originalFileName + "] into [" + (String)compressedFileName + "].", this, e));
        }
        if (compressionSucceeded) {
            if (!file2gz.delete()) {
                this.addStatus(new WarnStatus("Could not delete [" + originalFileName + "].", this));
            }
        } else {
            this.addStatus(new WarnStatus("Compression of [" + originalFileName + "] failed. Original file left intact.", this));
        }
    }
}

