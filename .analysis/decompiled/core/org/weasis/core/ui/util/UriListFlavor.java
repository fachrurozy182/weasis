/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.util;

import java.awt.datatransfer.DataFlavor;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;

public final class UriListFlavor {
    private static final Logger LOGGER = LoggerFactory.getLogger(UriListFlavor.class);
    public static final DataFlavor flavor = UriListFlavor.createDataFlavor();
    private static final DataFlavor[] FLAVORS = new DataFlavor[]{DataFlavor.javaFileListFlavor, flavor};

    private UriListFlavor() {
    }

    private static DataFlavor createDataFlavor() {
        try {
            return new DataFlavor("text/uri-list;class=java.lang.String", null, UriListFlavor.class.getClassLoader());
        }
        catch (Exception e) {
            LOGGER.error("Failed to create URI data flavor", (Throwable)e);
            return null;
        }
    }

    public static List<Path> textURIListToPathList(String uriList) {
        return uriList.lines().map(String::trim).filter(line -> !line.isEmpty() && !line.startsWith("#")).map(UriListFlavor::uriToPath).flatMap(Optional::stream).toList();
    }

    private static Optional<Path> uriToPath(String uriString) {
        try {
            return Optional.of(Paths.get(new URI(uriString)));
        }
        catch (Exception e) {
            LOGGER.error("Failed to convert URI to Path: {}", (Object)uriString, (Object)e);
            return Optional.empty();
        }
    }

    public static boolean isValidURI(String uriString) {
        if (!StringUtil.hasText((CharSequence)uriString)) {
            return false;
        }
        try {
            new URI(uriString);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static DataFlavor[] getTransferDataFlavors() {
        return Arrays.copyOf(FLAVORS, FLAVORS.length);
    }

    public static boolean isDataFlavorSupported(DataFlavor flavor) {
        return Arrays.stream(FLAVORS).anyMatch(flavor::equals);
    }
}

