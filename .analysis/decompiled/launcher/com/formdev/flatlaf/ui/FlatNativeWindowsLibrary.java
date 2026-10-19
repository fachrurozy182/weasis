/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatNativeLibrary;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.awt.Window;

public class FlatNativeWindowsLibrary {
    private static int API_VERSION_WINDOWS = 1003;
    private static long osBuildNumber = Long.MIN_VALUE;
    public static final int DWMWCP_DEFAULT = 0;
    public static final int DWMWCP_DONOTROUND = 1;
    public static final int DWMWCP_ROUND = 2;
    public static final int DWMWCP_ROUNDSMALL = 3;
    public static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
    public static final int DWMWA_BORDER_COLOR = 34;
    public static final int DWMWA_CAPTION_COLOR = 35;
    public static final int DWMWA_TEXT_COLOR = 36;
    public static final int DWMWA_COLOR_DEFAULT = -1;
    public static final int DWMWA_COLOR_NONE = -2;
    public static final Color COLOR_NONE = new Color(0, true);
    public static final int FOS_OVERWRITEPROMPT = 2;
    public static final int FOS_STRICTFILETYPES = 4;
    public static final int FOS_NOCHANGEDIR = 8;
    public static final int FOS_PICKFOLDERS = 32;
    public static final int FOS_FORCEFILESYSTEM = 64;
    public static final int FOS_ALLNONSTORAGEITEMS = 128;
    public static final int FOS_NOVALIDATE = 256;
    public static final int FOS_ALLOWMULTISELECT = 512;
    public static final int FOS_PATHMUSTEXIST = 2048;
    public static final int FOS_FILEMUSTEXIST = 4096;
    public static final int FOS_CREATEPROMPT = 8192;
    public static final int FOS_SHAREAWARE = 16384;
    public static final int FOS_NOREADONLYRETURN = 32768;
    public static final int FOS_NOTESTFILECREATE = 65536;
    public static final int FOS_HIDEMRUPLACES = 131072;
    public static final int FOS_HIDEPINNEDPLACES = 262144;
    public static final int FOS_NODEREFERENCELINKS = 0x100000;
    public static final int FOS_OKBUTTONNEEDSINTERACTION = 0x200000;
    public static final int FOS_DONTADDTORECENT = 0x2000000;
    public static final int FOS_FORCESHOWHIDDEN = 0x10000000;
    public static final int FOS_DEFAULTNOMINIMODE = 0x20000000;
    public static final int FOS_FORCEPREVIEWPANEON = 0x40000000;
    public static final int FOS_SUPPORTSTREAMABLEITEMS = Integer.MIN_VALUE;

    public static boolean isLoaded() {
        return SystemInfo.isWindows && FlatNativeLibrary.isLoaded(API_VERSION_WINDOWS);
    }

    public static long getOSBuildNumber() {
        if (osBuildNumber == Long.MIN_VALUE) {
            osBuildNumber = FlatNativeWindowsLibrary.getOSBuildNumberImpl();
        }
        return osBuildNumber;
    }

    private static native long getOSBuildNumberImpl();

    public static native long getHWND(Window var0);

    public static native boolean setWindowCornerPreference(long var0, int var2);

    public static native boolean dwmSetWindowAttributeBOOL(long var0, int var2, boolean var3);

    public static native boolean dwmSetWindowAttributeDWORD(long var0, int var2, int var3);

    public static boolean dwmSetWindowAttributeCOLORREF(long hwnd, int attribute, Color color) {
        int rgb = color == COLOR_NONE ? -2 : (color != null ? color.getRed() | color.getGreen() << 8 | color.getBlue() << 16 : -1);
        return FlatNativeWindowsLibrary.dwmSetWindowAttributeDWORD(hwnd, attribute, rgb);
    }

    public static native String[] showFileChooser(Window var0, boolean var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, int var10, int var11, FileChooserCallback var12, int var13, String[] var14, int[] var15);

    public static native int showMessageDialog(long var0, int var2, String var3, String var4, int var5, String ... var6);

    public static native int showMessageBox(long var0, String var2, String var3, int var4);

    public static interface FileChooserCallback {
        public boolean approve(String[] var1, int var2, long var3);
    }
}

