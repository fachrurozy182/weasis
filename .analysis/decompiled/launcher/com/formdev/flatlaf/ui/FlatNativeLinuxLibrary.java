/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatNativeLibrary;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class FlatNativeLinuxLibrary {
    private static int API_VERSION_LINUX = 3004;
    static final int SIZE_TOPLEFT = 0;
    static final int SIZE_TOP = 1;
    static final int SIZE_TOPRIGHT = 2;
    static final int SIZE_RIGHT = 3;
    static final int SIZE_BOTTOMRIGHT = 4;
    static final int SIZE_BOTTOM = 5;
    static final int SIZE_BOTTOMLEFT = 6;
    static final int SIZE_LEFT = 7;
    static final int MOVE = 8;
    private static Boolean isXWindowSystem;
    private static Boolean isGtk3Available;
    public static final int FC_select_folder = 1;
    public static final int FC_select_multiple = 2;
    public static final int FC_show_hidden = 4;
    public static final int FC_local_only = 8;
    public static final int FC_do_overwrite_confirmation = 16;
    public static final int FC_create_folders = 32;

    public static boolean isLoaded() {
        return SystemInfo.isLinux && FlatNativeLibrary.isLoaded(API_VERSION_LINUX);
    }

    private static boolean isXWindowSystem() {
        if (isXWindowSystem == null) {
            isXWindowSystem = Toolkit.getDefaultToolkit().getClass().getName().endsWith(".XToolkit");
        }
        return isXWindowSystem;
    }

    static boolean isWMUtilsSupported(Window window) {
        return FlatNativeLinuxLibrary.hasCustomDecoration(window) && FlatNativeLinuxLibrary.isXWindowSystem() && FlatNativeLinuxLibrary.isLoaded();
    }

    static boolean moveOrResizeWindow(Window window, MouseEvent e, int direction) {
        Point pt = FlatNativeLinuxLibrary.swingToX11Coordinates(window, e.getLocationOnScreen());
        return FlatNativeLinuxLibrary.xMoveOrResizeWindow(window, pt.x, pt.y, direction);
    }

    static boolean showWindowMenu(Window window, MouseEvent e) {
        Point pt = FlatNativeLinuxLibrary.swingToX11Coordinates(window, e.getLocationOnScreen());
        return FlatNativeLinuxLibrary.xShowWindowMenu(window, pt.x, pt.y);
    }

    private static Point swingToX11Coordinates(Window window, Point pt) {
        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        if (gc == null) {
            return pt;
        }
        AffineTransform transform = gc.getDefaultTransform();
        if (SystemInfo.isJetBrainsJVM && SystemInfo.isJava_17_orLater) {
            Rectangle bounds = gc.getBounds();
            int x = (int)Math.round((double)(pt.x - bounds.x) * transform.getScaleX()) + bounds.x;
            int y = (int)Math.round((double)(pt.y - bounds.y) * transform.getScaleY()) + bounds.y;
            return new Point(x, y);
        }
        int x = (int)Math.round((double)pt.x * transform.getScaleX());
        int y = (int)Math.round((double)pt.y * transform.getScaleY());
        return new Point(x, y);
    }

    private static native boolean xMoveOrResizeWindow(Window var0, int var1, int var2, int var3);

    private static native boolean xShowWindowMenu(Window var0, int var1, int var2);

    private static boolean hasCustomDecoration(Window window) {
        return window instanceof JFrame && JFrame.isDefaultLookAndFeelDecorated() && ((JFrame)window).isUndecorated() || window instanceof JDialog && JDialog.isDefaultLookAndFeelDecorated() && ((JDialog)window).isUndecorated();
    }

    public static boolean isGtk3Available() {
        if (isGtk3Available == null) {
            isGtk3Available = FlatNativeLinuxLibrary.isLibAvailable("libgtk-3.so.0") || FlatNativeLinuxLibrary.isLibAvailable("libgtk-3.so");
        }
        return isGtk3Available;
    }

    private static native boolean isLibAvailable(String var0);

    public static native String[] showFileChooser(Window var0, int var1, boolean var2, String var3, String var4, String var5, String var6, int var7, int var8, FileChooserCallback var9, int var10, String[] var11, int[] var12);

    public static native int showMessageDialog(long var0, int var2, String var3, String var4, int var5, String ... var6);

    public static interface FileChooserCallback {
        public boolean approve(String[] var1, int var2, long var3);
    }
}

