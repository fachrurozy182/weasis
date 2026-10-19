/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemInfo
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.net;

import com.formdev.flatlaf.util.SystemInfo;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import org.weasis.core.util.StringUtil;

public final class URIUtils {
    private URIUtils() {
    }

    public static URI getURI(String pathOrUri) throws URISyntaxException {
        Objects.requireNonNull(pathOrUri, "pathOrUri");
        try {
            return new URI(pathOrUri);
        }
        catch (URISyntaxException e) {
            return URIUtils.convertPathToURI(pathOrUri, e);
        }
    }

    private static URI convertPathToURI(String path, URISyntaxException originalException) throws URISyntaxException {
        try {
            return Path.of(path, new String[0]).toUri();
        }
        catch (Exception ex) {
            originalException.addSuppressed(ex);
            throw originalException;
        }
    }

    public static boolean isHttpURI(URI uri) {
        return URIUtils.isProtocol(uri, "http") || URIUtils.isProtocol(uri, "https");
    }

    public static boolean isFileURI(URI uri) {
        return URIUtils.isProtocol(uri, "file") || URIUtils.isSchemelessPath(uri);
    }

    private static boolean isSchemelessPath(URI uri) {
        return uri.getScheme() == null && uri.getPath() != null;
    }

    public static boolean isProtocol(URI uri, String protocol) {
        if (uri == null || !StringUtil.hasText((CharSequence)protocol)) {
            throw new IllegalArgumentException("URI and protocol cannot be null");
        }
        return protocol.equalsIgnoreCase(uri.getScheme());
    }

    public static Path toPath(URI uri) {
        Objects.requireNonNull(uri, "uri");
        if (uri.getScheme() == null) {
            return Path.of(uri.getPath(), new String[0]);
        }
        if (uri.getAuthority() != null && SystemInfo.isWindows) {
            return Path.of("\\\\" + uri.getAuthority() + uri.getPath(), new String[0]);
        }
        return Path.of(uri);
    }

    public static File toFile(URI uri) {
        return URIUtils.toPath(uri).toFile();
    }

    public static Path getAbsolutePath(URI uri) {
        Objects.requireNonNull(uri, "uri");
        return URIUtils.isFileURI(uri) ? URIUtils.toPath(uri).toAbsolutePath() : null;
    }
}

