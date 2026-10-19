/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.media;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.internal.mime.InvalidMagicMimeEntryException;
import org.weasis.core.internal.mime.MagicMimeEntry;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;

public class MimeInspector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MimeInspector.class);
    public static final String UNKNOWN_MIME_TYPE = "application/x-unknown-mime-type";
    private static final Properties mimeTypes = new Properties();
    private static final ArrayList<MagicMimeEntry> mMagicMimeEntries = new ArrayList();

    private MimeInspector() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean isMatchingMimeTypeFromMagicNumber(File file, String mimeType) {
        if (file == null) return false;
        if (mimeType == null) return false;
        if (!file.canRead()) {
            return false;
        }
        if (file.isDirectory()) {
            return "application/directory".equals(mimeType);
        }
        MagicMimeEntry me = MimeInspector.getMagicMimeEntry(mimeType);
        if (me == null) return false;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");){
            if (!mimeType.equals(me.getMatch(raf))) return false;
            boolean bl = true;
            return bl;
        }
        catch (IOException e) {
            LOGGER.error("", (Throwable)e);
        }
        return false;
    }

    public static boolean isMatchingMimeTypeFromMagicNumber(byte[] byteArray, String mimeType) {
        if (byteArray == null || byteArray.length == 0 || mimeType == null) {
            return false;
        }
        MagicMimeEntry me = MimeInspector.getMagicMimeEntry(mimeType);
        if (me != null) {
            try {
                if (mimeType.equals(me.getMatch(byteArray))) {
                    return true;
                }
            }
            catch (IOException e) {
                LOGGER.error("", (Throwable)e);
            }
        }
        return false;
    }

    public static String getMimeTypeFromMagicNumber(Path file) {
        if (file == null || !Files.isReadable(file)) {
            return null;
        }
        if (Files.isDirectory(file, new LinkOption[0])) {
            return "application/directory";
        }
        String mimeType = null;
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r");){
            mimeType = MimeInspector.getMagicMimeType(raf);
        }
        catch (IOException e) {
            LOGGER.error("Error when getting mime-type", (Throwable)e);
        }
        return mimeType;
    }

    public static String getMimeType(Path file) {
        String mimeType;
        String extension;
        if (file == null || !Files.isReadable(file)) {
            return null;
        }
        String fileName = file.getFileName().toString();
        int lastPos = fileName.lastIndexOf(46);
        String string = extension = lastPos > 0 ? fileName.substring(lastPos + 1).trim() : null;
        if (extension != null && extension.length() > 0 && extension.length() < 5 && (mimeType = mimeTypes.getProperty(extension.toLowerCase())) != null) {
            String[] mimes = mimeType.split(",");
            if (mimes.length > 1) {
                return Optional.ofNullable(MimeInspector.getMimeTypeFromMagicNumber(file)).orElse(mimes[0]);
            }
            return mimeType;
        }
        return MimeInspector.getMimeTypeFromMagicNumber(file);
    }

    private static void parse(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        ArrayList<String> sequence = new ArrayList<String>();
        String line = br.readLine();
        block0: while (line != null) {
            if ((line = line.trim()).isEmpty() || line.charAt(0) == '#') {
                line = br.readLine();
                continue;
            }
            sequence.add(line);
            while (true) {
                if ((line = br.readLine()) == null) {
                    MimeInspector.addEntry(sequence);
                    sequence.clear();
                    continue block0;
                }
                if ((line = line.trim()).isEmpty() || line.charAt(0) == '#') continue;
                if (line.charAt(0) != '>') {
                    MimeInspector.addEntry(sequence);
                    sequence.clear();
                    continue block0;
                }
                sequence.add(line);
            }
        }
        if (!sequence.isEmpty()) {
            MimeInspector.addEntry(sequence);
        }
    }

    private static void addEntry(ArrayList<String> aStringArray) {
        try {
            MagicMimeEntry magicEntry = new MagicMimeEntry(aStringArray);
            mMagicMimeEntries.add(magicEntry);
        }
        catch (InvalidMagicMimeEntryException e) {
            LOGGER.error("Error when adding mime {}", aStringArray, (Object)e);
        }
    }

    private static String getMagicMimeType(RandomAccessFile raf) throws IOException {
        for (MagicMimeEntry me : mMagicMimeEntries) {
            String mtype = me.getMatch(raf);
            if (mtype == null) continue;
            return mtype;
        }
        return null;
    }

    private static MagicMimeEntry getMagicMimeEntry(String mimeType) {
        if (mimeType != null) {
            for (MagicMimeEntry m : mMagicMimeEntries) {
                if (!mimeType.equals(m.getMimeType())) continue;
                return m;
            }
        }
        return null;
    }

    public static String getMajorComponent(String mimeType) {
        if (mimeType == null) {
            return "";
        }
        int offset = mimeType.indexOf(47);
        if (offset == -1) {
            return mimeType;
        }
        return mimeType.substring(0, offset);
    }

    public static String getMinorComponent(String mimeType) {
        if (mimeType == null) {
            return "";
        }
        int offset = mimeType.indexOf(47);
        if (offset == -1) {
            return mimeType;
        }
        return mimeType.substring(offset + 1);
    }

    public static String getFileExtension(String fileName) {
        int lastPos;
        if (fileName == null || (lastPos = fileName.lastIndexOf(46)) < 0) {
            return null;
        }
        String extension = fileName.substring(lastPos + 1);
        if (extension.contains(File.separator)) {
            return null;
        }
        return extension;
    }

    public static String getExtensions(String mime) {
        if (StringUtil.hasText((CharSequence)mime)) {
            Set<Map.Entry<Object, Object>> entries = mimeTypes.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                String[] mimes;
                String key = (String)entry.getKey();
                String val = (String)entry.getValue();
                if (!StringUtil.hasText((CharSequence)val)) continue;
                for (String m : mimes = val.split(",")) {
                    if (!mime.equals(m)) continue;
                    return key;
                }
            }
        }
        return null;
    }

    static {
        InputStream fileStream = null;
        try {
            fileStream = MimeInspector.class.getResourceAsStream("/mime-types.properties");
            mimeTypes.load(fileStream);
        }
        catch (IOException e) {
            LOGGER.error("Error when reading mime-types", (Throwable)e);
        }
        finally {
            StreamUtil.safeClose((AutoCloseable)fileStream);
        }
        InputStream is = MimeInspector.class.getResourceAsStream("/magic.mime");
        if (is != null) {
            try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);){
                MimeInspector.parse(streamReader);
            }
            catch (Exception e) {
                LOGGER.error("Parse magic mime-types", (Throwable)e);
            }
        }
    }
}

