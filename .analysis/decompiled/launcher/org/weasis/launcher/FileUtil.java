/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.felix.framework.util.Util
 */
package org.weasis.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.felix.framework.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.PlatformCertificateLoader;
import org.weasis.launcher.Utils;

public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private static final int ZIP_MAX_ENTRIES = 100000;
    private static final long ZIP_MAX_TOTAL_SIZE = 0x40000000L;
    private static final double ZIP_MAX_COMPRESSION_RATIO = 100.0;

    private FileUtil() {
    }

    public static void safeClose(AutoCloseable object) {
        if (object != null) {
            try {
                object.close();
            }
            catch (Exception e) {
                LOGGER.warn("Cannot close AutoCloseable", e);
            }
        }
    }

    public static void safeClose(XMLStreamReader xmler) {
        if (xmler != null) {
            try {
                xmler.close();
            }
            catch (XMLStreamException e) {
                LOGGER.warn("Cannot close XMLStreamReader", e);
            }
        }
    }

    public static void recursiveDelete(Path rootDir, boolean deleteRoot) {
        if (rootDir == null || !Files.isDirectory(rootDir, new LinkOption[0])) {
            return;
        }
        try (Stream<Path> stream = Files.list(rootDir);){
            stream.forEach(path -> {
                if (Files.isDirectory(path, new LinkOption[0])) {
                    FileUtil.recursiveDelete(path, true);
                } else {
                    FileUtil.deleteQuietly(path);
                }
            });
        }
        catch (IOException e) {
            LOGGER.warn("Failed to delete directory contents: {}", (Object)rootDir, (Object)e);
        }
        if (deleteRoot) {
            FileUtil.deleteQuietly(rootDir);
        }
    }

    public static void deleteDirectoryContents(Path directory, int deleteDirLevel, int level) {
        if (directory == null || !Files.isDirectory(directory, new LinkOption[0])) {
            return;
        }
        try (Stream<Path> stream = Files.list(directory);){
            stream.forEach(path -> {
                if (Files.isDirectory(path, new LinkOption[0])) {
                    FileUtil.deleteDirectoryContents(path, deleteDirLevel, level + 1);
                } else {
                    FileUtil.deleteQuietly(path);
                }
            });
        }
        catch (IOException e) {
            LOGGER.warn("Failed to delete directory contents: {}", (Object)directory, (Object)e);
        }
        if (level >= deleteDirLevel) {
            FileUtil.deleteQuietly(directory);
        }
    }

    private static boolean deleteQuietly(Path path) {
        try {
            return Files.deleteIfExists(path);
        }
        catch (IOException e) {
            LOGGER.error("Cannot delete: {}", (Object)path, (Object)e);
            return false;
        }
    }

    public static boolean delete(Path path) {
        block10: {
            if (path == null || !Files.exists(path, new LinkOption[0])) {
                return false;
            }
            if (Files.isDirectory(path, new LinkOption[0])) {
                try (Stream<Path> walk = Files.walk(path, new FileVisitOption[0]);){
                    walk.sorted(Comparator.reverseOrder()).forEach(FileUtil::deleteQuietly);
                    break block10;
                }
                catch (IOException e) {
                    LOGGER.error("Cannot delete: {}", (Object)path, (Object)e);
                    return false;
                }
            }
            return FileUtil.deleteQuietly(path);
        }
        return !Files.exists(path, new LinkOption[0]);
    }

    public static void prepareToWriteFile(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent, new LinkOption[0])) {
            Files.createDirectories(parent, new FileAttribute[0]);
        }
    }

    public static Properties loadProperties(Path path, Properties target) {
        Properties properties;
        Properties properties2 = properties = target != null ? target : new Properties();
        if (path != null && Files.exists(path, new LinkOption[0]) && Files.isReadable(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
                properties.load(reader);
                LOGGER.trace("Loaded {} properties from: {}", (Object)properties.size(), (Object)path);
            }
            catch (IOException e) {
                LOGGER.error("Failed to load properties from file: {}", (Object)path, (Object)e);
            }
        } else {
            LOGGER.debug("Properties file not found or not readable: {}", (Object)path);
        }
        return properties;
    }

    public static void storeProperties(Path path, Properties properties, String comments) {
        Objects.requireNonNull(path, "Path cannot be null");
        Objects.requireNonNull(properties, "Properties cannot be null");
        try {
            FileUtil.prepareToWriteFile(path);
        }
        catch (IOException e) {
            LOGGER.error("Failed to create parent directories for: {}", (Object)path, (Object)e);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);){
            properties.store(writer, comments);
            LOGGER.trace("Stored {} properties to: {}", (Object)properties.size(), (Object)path);
        }
        catch (IOException e) {
            LOGGER.error("Failed to store properties to file: {}", (Object)path, (Object)e);
        }
    }

    public static String writeResources(String srcPath, Path cacheDir, String date) throws IOException {
        String fileDate = null;
        URLConnection urlConnection = FileUtil.getAdaptedConnection(new URL(srcPath), false);
        long last = urlConnection.getLastModified();
        if (last != 0L) {
            fileDate = Long.toString(last);
        }
        if (date == null || !date.equals(fileDate) || FileUtil.isFolderEmpty(cacheDir)) {
            FileUtil.recursiveDelete(cacheDir, false);
            FileUtil.unzip(urlConnection.getInputStream(), cacheDir);
        }
        return fileDate;
    }

    public static boolean isFolderEmpty(Path path) throws IOException {
        if (path == null || !Files.exists(path, new LinkOption[0])) {
            return false;
        }
        if (Files.isDirectory(path, new LinkOption[0])) {
            try (Stream<Path> entries = Files.list(path);){
                boolean bl = entries.findAny().isEmpty();
                return bl;
            }
        }
        return false;
    }

    public static URLConnection getAdaptedConnection(URL url, boolean useCaches) throws IOException {
        String auth;
        if ("https".equalsIgnoreCase(url.getProtocol())) {
            PlatformCertificateLoader.awaitReady();
        }
        URLConnection connection = url.openConnection();
        connection.setUseCaches(useCaches);
        String protocol = url.getProtocol();
        String pauth = System.getProperty("http.proxyAuth", null);
        if (FileUtil.hasProxyProperty(pauth, protocol)) {
            String base64 = Util.base64Encode((String)pauth);
            connection.setRequestProperty("Proxy-Authorization", "Basic " + base64);
        }
        if (FileUtil.hasProxyProperty(auth = System.getProperty("http.authorization", null), protocol)) {
            connection.setRequestProperty("Authorization", auth);
        }
        return connection;
    }

    private static boolean hasProxyProperty(String propertyValue, String protocol) {
        return Utils.hasText(propertyValue) && ("http".equals(protocol) || "https".equals(protocol));
    }

    public static String humanReadableByte(long bytes, boolean si) {
        long absBytes;
        int unit = si ? 1000 : 1024;
        long l = absBytes = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absBytes < (long)unit) {
            return bytes + " B";
        }
        int exp = (int)(Math.log(absBytes) / Math.log(unit));
        long th = (long)Math.ceil(Math.pow(unit, exp) * ((double)unit - 0.05));
        if (exp < 6 && absBytes >= th - (long)((th & 0xFFFL) == 3328L ? 51 : 0)) {
            ++exp;
        }
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        if (exp > 4) {
            bytes /= (long)unit;
            --exp;
        }
        return String.format("%.1f %sB", (double)bytes / Math.pow(unit, exp), pre);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void unzip(InputStream inputStream, Path targetDir) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        if (targetDir == null) {
            throw new IllegalArgumentException("Target directory cannot be null");
        }
        Files.createDirectories(targetDir, new FileAttribute[0]);
        try (BufferedInputStream bufInStream = new BufferedInputStream(inputStream);
             ZipInputStream zis = new ZipInputStream(bufInStream);){
            FileUtil.extractZipEntries(zis, targetDir);
        }
        finally {
            FileUtil.safeClose(inputStream);
        }
    }

    private static void extractZipEntries(ZipInputStream zis, Path targetPath) throws IOException {
        ZipEntry entry;
        int entryCount = 0;
        long remainingBudget = 0x40000000L;
        while ((entry = zis.getNextEntry()) != null) {
            double ratio;
            if (++entryCount > 100000) {
                throw new IOException("ZIP archive contains too many entries (max allowed: 100000)");
            }
            long compressedSize = entry.getCompressedSize();
            long uncompressedSize = entry.getSize();
            if (compressedSize > 0L && uncompressedSize > 0L && (ratio = (double)uncompressedSize / (double)compressedSize) > 100.0) {
                throw new IOException("ZIP entry \"" + entry.getName() + "\" has a suspicious compression ratio: " + ratio);
            }
            long bytesWritten = FileUtil.extractEntry(zis, entry, targetPath, remainingBudget);
            remainingBudget -= bytesWritten;
        }
    }

    private static long extractEntry(InputStream inputStream, ZipEntry entry, Path targetPath, long maxBytes) throws IOException {
        Path entryPath = targetPath.resolve(entry.getName()).normalize();
        if (!entryPath.startsWith(targetPath)) {
            throw new IOException("Entry is outside the target directory: " + entry.getName());
        }
        if (entry.isDirectory()) {
            Files.createDirectories(entryPath, new FileAttribute[0]);
            return 0L;
        }
        Path parent = entryPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent, new FileAttribute[0]);
        }
        try (OutputStream out = Files.newOutputStream(entryPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);){
            long l = FileUtil.copyStream(inputStream, out, maxBytes);
            return l;
        }
    }

    private static long copyStream(InputStream in, OutputStream out, long maxBytes) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[8192];
        long total = 0L;
        while ((bytesRead = in.read(buffer)) != -1) {
            if ((total += (long)bytesRead) > maxBytes) {
                throw new IOException("ZIP archive exceeds maximum allowed uncompressed size (" + FileUtil.humanReadableByte(0x40000000L, true) + ")");
            }
            out.write(buffer, 0, bytesRead);
        }
        return total;
    }
}

