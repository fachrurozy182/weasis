/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.NativeLibrary;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;

class FlatNativeLibrary {
    private static boolean initialized;
    private static NativeLibrary nativeLibrary;

    FlatNativeLibrary() {
    }

    private static native int getApiVersion();

    static synchronized boolean isLoaded(int apiVersion) {
        FlatNativeLibrary.initialize(apiVersion);
        return nativeLibrary != null ? nativeLibrary.isLoaded() : false;
    }

    private static void initialize(int apiVersion) {
        String ext;
        String classifier;
        if (initialized) {
            return;
        }
        initialized = true;
        if (!FlatSystemProperties.getBoolean("flatlaf.useNativeLibrary", true)) {
            return;
        }
        boolean unknownArch = false;
        if (SystemInfo.isWindows_10_orLater && (SystemInfo.isX86 || SystemInfo.isX86_64 || SystemInfo.isAARCH64)) {
            classifier = SystemInfo.isAARCH64 ? "windows-arm64" : (SystemInfo.isX86_64 ? "windows-x86_64" : "windows-x86");
            ext = "dll";
        } else if (SystemInfo.isMacOS_10_14_Mojave_orLater && (SystemInfo.isAARCH64 || SystemInfo.isX86_64)) {
            classifier = SystemInfo.isAARCH64 ? "macos-arm64" : "macos-x86_64";
            ext = "dylib";
        } else if (SystemInfo.isLinux) {
            classifier = SystemInfo.isAARCH64 ? "linux-arm64" : (SystemInfo.isX86_64 ? "linux-x86_64" : "linux-" + FlatNativeLibrary.sanitize(System.getProperty("os.arch")));
            ext = "so";
            unknownArch = !SystemInfo.isX86_64 && !SystemInfo.isAARCH64;
            FlatNativeLibrary.loadJAWT();
        } else {
            return;
        }
        NativeLibrary nativeLibrary = FlatNativeLibrary.createNativeLibrary(classifier, ext, unknownArch);
        if (!nativeLibrary.isLoaded()) {
            return;
        }
        try {
            int actualApiVersion = FlatNativeLibrary.getApiVersion();
            if (actualApiVersion != apiVersion) {
                LoggingFacade.INSTANCE.logSevere("FlatLaf: Wrong API version in native library (expected " + apiVersion + ", actual " + actualApiVersion + "). Ignoring native library.", null);
                return;
            }
        }
        catch (Throwable ex) {
            LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to get API version of native library. Ignoring native library.", ex);
            return;
        }
        FlatNativeLibrary.nativeLibrary = nativeLibrary;
    }

    private static NativeLibrary createNativeLibrary(String classifier, String ext, boolean unknownArch) {
        File libraryFile;
        String libraryName = "flatlaf-" + classifier;
        String libraryPath = System.getProperty("flatlaf.nativeLibraryPath");
        if (libraryPath != null) {
            if ("system".equals(libraryPath)) {
                NativeLibrary library = new NativeLibrary(libraryName, true);
                if (library.isLoaded()) {
                    return library;
                }
                if (!unknownArch) {
                    LoggingFacade.INSTANCE.logSevere("FlatLaf: Native library '" + System.mapLibraryName(libraryName) + "' not found in java.library.path '" + System.getProperty("java.library.path") + "'. Using extracted native library instead.", null);
                }
            } else {
                File libraryFile2;
                libraryFile = new File(libraryPath, System.mapLibraryName(libraryName));
                if (libraryFile.exists()) {
                    return new NativeLibrary(libraryFile, true);
                }
                String libraryName2 = null;
                File jarFile = FlatNativeLibrary.getJarFile();
                if (jarFile != null && (libraryFile2 = new File(libraryPath, libraryName2 = FlatNativeLibrary.buildLibraryName(jarFile, classifier, ext))).exists()) {
                    return new NativeLibrary(libraryFile2, true);
                }
                if (!unknownArch) {
                    LoggingFacade.INSTANCE.logSevere("FlatLaf: Native library '" + libraryFile.getName() + (libraryName2 != null ? "' or '" + libraryName2 : "") + "' not found in '" + libraryFile.getParentFile().getAbsolutePath() + "'. Using extracted native library instead.", null);
                }
            }
        }
        if ((libraryFile = FlatNativeLibrary.findLibraryBesideJar(classifier, ext)) != null) {
            return new NativeLibrary(libraryFile, true);
        }
        return new NativeLibrary("com/formdev/flatlaf/natives/" + libraryName, null, !unknownArch);
    }

    private static File findLibraryBesideJar(String classifier, String ext) {
        File[] dirs;
        File versionDir;
        File jarFile = FlatNativeLibrary.getJarFile();
        if (jarFile == null) {
            return null;
        }
        String libraryName = FlatNativeLibrary.buildLibraryName(jarFile, classifier, ext);
        File jarDir = jarFile.getParentFile();
        File libraryFile = new File(jarDir, libraryName);
        if (libraryFile.isFile()) {
            return libraryFile;
        }
        if (jarDir.getName().equalsIgnoreCase("lib") && (libraryFile = new File(jarDir.getParentFile(), "bin/" + libraryName)).isFile()) {
            return libraryFile;
        }
        String path = jarDir.getAbsolutePath().replace('\\', '/');
        if (path.contains("/.gradle/caches/") && libraryName.contains((versionDir = jarDir.getParentFile()).getName()) && (dirs = versionDir.listFiles()) != null) {
            for (File dir : dirs) {
                libraryFile = new File(dir, libraryName);
                if (!libraryFile.isFile()) continue;
                return libraryFile;
            }
        }
        return null;
    }

    private static File getJarFile() {
        try {
            File jarFile;
            URL jarUrl;
            CodeSource codeSource = FlatNativeLibrary.class.getProtectionDomain().getCodeSource();
            URL uRL = jarUrl = codeSource != null ? codeSource.getLocation() : null;
            if (jarUrl == null) {
                return null;
            }
            if (!"file".equals(jarUrl.getProtocol())) {
                return null;
            }
            try {
                jarFile = new File(jarUrl.toURI());
            }
            catch (URISyntaxException ex) {
                jarFile = new File(jarUrl.getPath());
            }
            if (!jarFile.isFile()) {
                return null;
            }
            return jarFile;
        }
        catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere(ex.getMessage(), ex);
            return null;
        }
    }

    private static String buildLibraryName(File jarFile, String classifier, String ext) {
        String jarName = jarFile.getName();
        String jarBasename = jarName.substring(0, jarName.lastIndexOf(46));
        jarBasename = StringUtils.removeTrailing(jarBasename, "-no-natives");
        return jarBasename + (jarBasename.contains("flatlaf") ? "" : "-flatlaf") + '-' + classifier + '.' + ext;
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private static void loadJAWT() {
        try {
            System.loadLibrary("jawt");
        }
        catch (UnsatisfiedLinkError ex) {
            String message = ex.getMessage();
            if (message == null || !message.contains("already loaded in another classloader")) {
                LoggingFacade.INSTANCE.logSevere(message, ex);
            }
        }
        catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere(ex.getMessage(), ex);
        }
    }
}

