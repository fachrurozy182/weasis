/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatNativeLibrary;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Rectangle;
import java.awt.Window;

public class FlatNativeMacLibrary {
    private static int API_VERSION_MACOS = 2004;
    public static final int BUTTONS_SPACING_DEFAULT = 0;
    public static final int BUTTONS_SPACING_MEDIUM = 1;
    public static final int BUTTONS_SPACING_LARGE = 2;
    public static final int FC_canChooseFiles = 1;
    public static final int FC_canChooseDirectories = 2;
    public static final int FC_resolvesAliases = 4;
    public static final int FC_allowsMultipleSelection = 8;
    public static final int FC_accessoryViewDisclosed = 16;
    public static final int FC_showsTagField = 256;
    public static final int FC_canCreateDirectories = 512;
    public static final int FC_canSelectHiddenExtension = 1024;
    public static final int FC_showsHiddenFiles = 2048;
    public static final int FC_extensionHidden = 4096;
    public static final int FC_allowsOtherFileTypes = 8192;
    public static final int FC_treatsFilePackagesAsDirectories = 16384;
    public static final int FC_showSingleFilterField = 0x1000000;

    public static boolean isLoaded() {
        return SystemInfo.isMacOS && FlatNativeLibrary.isLoaded(API_VERSION_MACOS);
    }

    public static native boolean setWindowAppearance(Window var0, String var1);

    public static native boolean setWindowRoundedBorder(Window var0, float var1, float var2, int var3);

    public static native boolean setWindowButtonsSpacing(Window var0, int var1);

    public static native Rectangle getWindowButtonsBounds(Window var0);

    public static native boolean isWindowFullScreen(Window var0);

    public static native boolean toggleWindowFullScreen(Window var0);

    public static native String[] showFileChooser(Window var0, int var1, boolean var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, int var10, int var11, FileChooserCallback var12, int var13, String[] var14, int[] var15);

    public static native int showMessageDialog(long var0, int var2, String var3, String var4, int var5, String ... var6);

    public static interface FileChooserCallback {
        public boolean approve(String[] var1, int var2, long var3);
    }
}

