/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileToBufferUtil {
    public static void readIntoList(File file, List<String> stringList) throws IOException {
        if (file.getName().endsWith(".gz")) {
            FileToBufferUtil.gzFileReadIntoList(file, stringList);
        } else if (file.getName().endsWith(".zip")) {
            FileToBufferUtil.zipFileReadIntoList(file, stringList);
        } else {
            FileToBufferUtil.regularReadIntoList(file, stringList);
        }
    }

    private static void zipFileReadIntoList(File file, List<String> stringList) throws IOException {
        System.out.println("Reading zip file [" + String.valueOf(file) + "]");
        try (ZipFile zipFile = new ZipFile(file);){
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry = entries.nextElement();
            FileToBufferUtil.readInputStream(zipFile.getInputStream(entry), stringList);
        }
    }

    static void readInputStream(InputStream is, List<String> stringList) throws IOException {
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        while ((line = in.readLine()) != null) {
            stringList.add(line);
        }
        in.close();
    }

    public static void regularReadIntoList(File file, List<String> stringList) throws IOException {
        String line;
        FileInputStream fis = new FileInputStream(file);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));
        while ((line = in.readLine()) != null) {
            stringList.add(line);
        }
        in.close();
    }

    public static void gzFileReadIntoList(File file, List<String> stringList) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        GZIPInputStream gzis = new GZIPInputStream(fis);
        FileToBufferUtil.readInputStream(gzis, stringList);
    }
}

