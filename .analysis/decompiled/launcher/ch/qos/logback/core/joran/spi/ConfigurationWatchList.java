/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.joran.spi.HttpUtil;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.MD5Util;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationWatchList
extends ContextAwareBase {
    public static final String HTTPS_PROTOCOL_STR = "https";
    public static final String HTTP_PROTOCOL_STR = "http";
    public static final String FILE_PROTOCOL_STR = "file";
    static final String[] WATCHABLE_PROTOCOLS = new String[]{"file", "https", "http"};
    static final byte[] BUF_ZERO = new byte[]{0};
    URL topURL;
    List<File> fileWatchList = new ArrayList<File>();
    List<URL> urlWatchList = new ArrayList<URL>();
    List<byte[]> lastHashList = new ArrayList<byte[]>();
    List<Long> lastModifiedList = new ArrayList<Long>();

    public ConfigurationWatchList buildClone() {
        ConfigurationWatchList out = new ConfigurationWatchList();
        out.topURL = this.topURL;
        out.fileWatchList = new ArrayList<File>(this.fileWatchList);
        out.lastModifiedList = new ArrayList<Long>(this.lastModifiedList);
        out.lastHashList = new ArrayList<byte[]>(this.lastHashList);
        return out;
    }

    public void clear() {
        this.topURL = null;
        this.lastModifiedList.clear();
        this.fileWatchList.clear();
        this.urlWatchList.clear();
        this.lastHashList.clear();
    }

    public void setTopURL(URL topURL) {
        this.topURL = topURL;
        if (topURL != null) {
            this.addAsFileToWatch(topURL);
        }
    }

    public boolean watchPredicateFulfilled() {
        if (this.hasMainURLAndNonEmptyFileList()) {
            return true;
        }
        if (this.urlListContainsProperties()) {
            return true;
        }
        return this.fileWatchListContainsProperties();
    }

    private boolean urlListContainsProperties() {
        return this.urlWatchList.stream().anyMatch(url -> url.toString().endsWith(".properties"));
    }

    private boolean hasMainURLAndNonEmptyFileList() {
        return this.topURL != null && !this.fileWatchList.isEmpty();
    }

    private boolean fileWatchListContainsProperties() {
        return this.fileWatchList.stream().anyMatch(file -> file.getName().endsWith(".properties"));
    }

    private void addAsFileToWatch(URL url) {
        File file = this.convertToFile(url);
        if (file != null) {
            this.fileWatchList.add(file);
            this.lastModifiedList.add(file.lastModified());
        }
    }

    private boolean isHTTP_Or_HTTPS(URL url) {
        String protocolStr = url.getProtocol();
        return this.isHTTP_Or_HTTPS(protocolStr);
    }

    private boolean isHTTP_Or_HTTPS(String protocolStr) {
        return protocolStr.equals(HTTP_PROTOCOL_STR) || protocolStr.equals(HTTPS_PROTOCOL_STR);
    }

    private void addAsHTTP_or_HTTPS_URLToWatch(URL url) {
        if (this.isHTTP_Or_HTTPS(url)) {
            this.urlWatchList.add(url);
            this.lastHashList.add(BUF_ZERO);
        }
    }

    public void addToWatchList(URL url) {
        String protocolStr = url.getProtocol();
        if (protocolStr.equals(FILE_PROTOCOL_STR)) {
            this.addAsFileToWatch(url);
        } else if (this.isHTTP_Or_HTTPS(protocolStr)) {
            this.addAsHTTP_or_HTTPS_URLToWatch(url);
        }
    }

    public URL getTopURL() {
        return this.topURL;
    }

    public List<File> getCopyOfFileWatchList() {
        return new ArrayList<File>(this.fileWatchList);
    }

    public boolean emptyWatchLists() {
        if (this.fileWatchList != null && !this.fileWatchList.isEmpty()) {
            return false;
        }
        return this.urlWatchList == null || this.urlWatchList.isEmpty();
    }

    public File changeDetectedInFile() {
        int len = this.fileWatchList.size();
        for (int i = 0; i < len; ++i) {
            File file;
            long actualModificationDate;
            long lastModified = this.lastModifiedList.get(i);
            if (lastModified == (actualModificationDate = (file = this.fileWatchList.get(i)).lastModified())) continue;
            this.lastModifiedList.set(i, actualModificationDate);
            return file;
        }
        return null;
    }

    public URL changeDetectedInURL() {
        int i = 0;
        int len = this.urlWatchList.size();
        if (i < len) {
            byte[] lastHash = this.lastHashList.get(i);
            URL url = this.urlWatchList.get(i);
            HttpUtil httpGetUtil = new HttpUtil(HttpUtil.RequestMethod.GET, url);
            HttpURLConnection getConnection = httpGetUtil.connectTextTxt();
            String response = httpGetUtil.readResponse(getConnection);
            byte[] hash = this.computeHash(response);
            if (lastHash == BUF_ZERO) {
                this.lastHashList.set(i, hash);
                return null;
            }
            if (Arrays.equals(lastHash, hash)) {
                return null;
            }
            this.lastHashList.set(i, hash);
            return url;
        }
        return null;
    }

    private byte[] computeHash(String response) {
        if (response == null || response.trim().length() == 0) {
            return null;
        }
        try {
            MD5Util md5Util = new MD5Util();
            byte[] hashBytes = md5Util.md5Hash(response);
            return hashBytes;
        }
        catch (NoSuchAlgorithmException e) {
            this.addError("missing MD5 algorithm", e);
            return null;
        }
    }

    File convertToFile(URL url) {
        String protocol = url.getProtocol();
        if (FILE_PROTOCOL_STR.equals(protocol)) {
            return new File(URLDecoder.decode(url.getFile()));
        }
        this.addInfo("URL [" + String.valueOf(url) + "] is not of type file");
        return null;
    }

    public boolean hasAtLeastOneWatchableFile() {
        return !this.fileWatchList.isEmpty();
    }

    public static boolean isWatchableProtocol(URL url) {
        if (url == null) {
            return false;
        }
        String protocolStr = url.getProtocol();
        return ConfigurationWatchList.isWatchableProtocol(protocolStr);
    }

    public static boolean isWatchableProtocol(String protocolStr) {
        return Arrays.stream(WATCHABLE_PROTOCOLS).anyMatch(protocol -> protocol.equalsIgnoreCase(protocolStr));
    }

    public String getUrlWatchListAsStr() {
        String urlWatchListStr = this.urlWatchList.stream().map(URL::toString).collect(Collectors.joining(", "));
        return urlWatchListStr;
    }

    public String getFileWatchListAsStr() {
        return this.fileWatchList.stream().map(File::getPath).collect(Collectors.joining(", "));
    }
}

