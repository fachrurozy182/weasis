/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.JTextComponent;

public class JavaCompatibility2 {
    private static boolean getUIMethodInitialized;
    private static MethodHandle getUIMethod;

    public static ComponentUI getUI(JComponent c) {
        try {
            if (SystemInfo.isJava_9_orLater) {
                if (!getUIMethodInitialized) {
                    getUIMethodInitialized = true;
                    try {
                        MethodType mt = MethodType.methodType(ComponentUI.class, new Class[0]);
                        getUIMethod = MethodHandles.publicLookup().findVirtual(JComponent.class, "getUI", mt);
                    }
                    catch (Exception ex) {
                        LoggingFacade.INSTANCE.logSevere(null, ex);
                    }
                }
                if (getUIMethod != null) {
                    return getUIMethod.invoke(c);
                }
            }
            if (c instanceof JPanel) {
                return ((JPanel)c).getUI();
            }
            if (c instanceof JList) {
                return ((JList)c).getUI();
            }
            if (c instanceof JTable) {
                return ((JTable)c).getUI();
            }
            if (c instanceof JTree) {
                return ((JTree)c).getUI();
            }
            if (c instanceof JTextComponent) {
                return ((JTextComponent)c).getUI();
            }
            Method m = c.getClass().getMethod("getUI", new Class[0]);
            return (ComponentUI)m.invoke(c, new Object[0]);
        }
        catch (Throwable ex) {
            return null;
        }
    }

    public static File[] getChooserShortcutPanelFiles(FileSystemView fsv) {
        try {
            if (SystemInfo.isJava_12_orLater) {
                Method m = fsv.getClass().getMethod("getChooserShortcutPanelFiles", new Class[0]);
                File[] files = (File[])m.invoke(fsv, new Object[0]);
                if (files.length == 1 && files[0].equals(new File(System.getProperty("user.home")))) {
                    files = new File[]{};
                }
                return files;
            }
            if (SystemInfo.isWindows) {
                Class<?> cls = Class.forName("sun.awt.shell.ShellFolder");
                Method m = cls.getMethod("get", String.class);
                return (File[])m.invoke(null, "fileChooserShortcutPanelFolders");
            }
        }
        catch (IllegalAccessException cls) {
        }
        catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
        return new File[0];
    }

    public static File[] getChooserComboBoxFiles(FileSystemView fsv) {
        try {
            if (SystemInfo.isJava_9_orLater) {
                Method m = fsv.getClass().getMethod("getChooserComboBoxFiles", new Class[0]);
                return (File[])m.invoke(fsv, new Object[0]);
            }
            Class<?> cls = Class.forName("sun.awt.shell.ShellFolder");
            Method m = cls.getMethod("get", String.class);
            return (File[])m.invoke(null, "fileChooserComboBoxFolders");
        }
        catch (IllegalAccessException cls) {
        }
        catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
        return new File[0];
    }
}

