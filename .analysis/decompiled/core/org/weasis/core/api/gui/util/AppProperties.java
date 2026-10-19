/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemInfo
 *  org.osgi.framework.Bundle
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.FrameworkUtil
 *  org.osgi.framework.ServiceReference
 *  org.osgi.framework.Version
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.gui.util;

import com.formdev.flatlaf.util.SystemInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
import java.util.regex.Pattern;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GhostGlassPane;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StringUtil;

public final class AppProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class);
    private static final String UNKNOWN = "unknown";
    private static final Pattern UNSAFE_PATH_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");
    public static final String WEASIS_VERSION = System.getProperty("weasis.version", "0.0.0");
    public static final String WEASIS_NAME = System.getProperty("weasis.name", "Weasis");
    public static final String WEASIS_USER = AppProperties.getUserName();
    public static final String WEASIS_PROFILE = System.getProperty("weasis.profile", "default");
    public static final String WEASIS_USER_AGENT = System.getProperty("http.agent");
    public static final Path APP_TEMP_DIR = AppProperties.initializeTempDirectory();
    public static final Path WEASIS_PATH = AppProperties.initializeWeasisPath();
    public static final String CACHE_NAME = "cache";
    public static final Path FILE_CACHE_DIR = AppProperties.buildAccessibleTempDirectory("cache");
    public static final GhostGlassPane glassPane = new GhostGlassPane();

    private AppProperties() {
    }

    private static String getUserName() {
        String user = System.getProperty("weasis.user", UNKNOWN).trim();
        return SystemInfo.isWindows ? user.toUpperCase() : user;
    }

    private static Path initializeTempDirectory() {
        Path tempDir = AppProperties.getTempDirectoryPath();
        Path appTempDir = tempDir.resolve("weasis-" + AppProperties.toSafePathElement(System.getProperty("user.name"), "tmp") + "." + AppProperties.toSafePathElement(System.getProperty("weasis.source.id"), UNKNOWN));
        System.setProperty("weasis.tmp.dir", appTempDir.toAbsolutePath().toString());
        try {
            FileUtil.deleteDirectoryContents((Path)appTempDir, (int)3, (int)0);
        }
        catch (Exception e) {
            LOGGER.error("Error cleaning temporary files", (Throwable)e);
        }
        return appTempDir;
    }

    private static String toSafePathElement(String value, String defaultValue) {
        if (!StringUtil.hasText((CharSequence)value)) {
            return defaultValue;
        }
        return UNSAFE_PATH_CHARS.matcher(value.strip()).replaceAll("_");
    }

    private static Path getTempDirectoryPath() {
        String tempDir = System.getProperty("java.io.tmpdir");
        if (tempDir == null || tempDir.length() == 1) {
            String userHome = System.getProperty("user.home", "");
            return Paths.get(userHome, new String[0]);
        }
        return Paths.get(tempDir, new String[0]);
    }

    private static Path initializeWeasisPath() {
        String weasisPath = System.getProperty("weasis.path", System.getProperty("user.home") + File.separator + ".weasis");
        return Paths.get(weasisPath, new String[0]);
    }

    public static BundleContext getBundleContext() {
        return AppProperties.getBundleContext(AppProperties.class);
    }

    public static BundleContext getBundleContext(Class<?> cl) {
        return Optional.ofNullable(FrameworkUtil.getBundle(cl)).map(Bundle::getBundleContext).orElse(null);
    }

    public static BundleContext getBundleContext(ServiceReference<?> sRef) {
        if (sRef == null) {
            return null;
        }
        Bundle bundle = sRef.getBundle();
        return bundle != null ? bundle.getBundleContext() : AppProperties.getBundleContext();
    }

    public static Path getBundleDataFolder(BundleContext context) {
        if (context == null) {
            return null;
        }
        return WEASIS_PATH.resolve("data").resolve(context.getBundle().getSymbolicName());
    }

    public static Path buildAccessibleTempDirectory(String ... subFolderNames) {
        if (subFolderNames == null || subFolderNames.length == 0) {
            return APP_TEMP_DIR;
        }
        Path result = APP_TEMP_DIR;
        for (String folder : subFolderNames) {
            result = result.resolve(folder);
        }
        try {
            Files.createDirectories(result, new FileAttribute[0]);
            return result;
        }
        catch (Exception e) {
            LOGGER.error("Cannot build directory: {}", (Object)result, (Object)e);
            return APP_TEMP_DIR;
        }
    }

    public static Path createTempFile(String subDirectory, String prefix, String suffix) throws IOException {
        Path tempDir = AppProperties.buildAccessibleTempDirectory(subDirectory);
        return Files.createTempFile(tempDir, prefix, suffix, new FileAttribute[0]);
    }

    public static Version getVersion(String version) {
        if (!StringUtil.hasText((CharSequence)version)) {
            return new Version("0.0.0");
        }
        String cleanVersion = AppProperties.extractVersionNumber(version);
        return new Version(cleanVersion);
    }

    private static String extractVersionNumber(String version) {
        int start = version.startsWith("v") ? 1 : 0;
        int end = version.indexOf(45);
        if (end < 0) {
            end = version.length();
        }
        return end > start ? version.substring(start, end) : "0.0.0";
    }
}

