/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras;

import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

public class FlatDesktop {
    public static boolean isSupported(Action action) {
        if (SystemInfo.isJava_9_orLater) {
            try {
                return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Enum.valueOf(Desktop.Action.class, action.name()));
            }
            catch (Exception ex) {
                LoggingFacade.INSTANCE.logSevere(null, ex);
                return false;
            }
        }
        return SystemInfo.isMacOS;
    }

    public static void setAboutHandler(Runnable aboutHandler) {
        String handlerClassName;
        if (!FlatDesktop.isSupported(Action.APP_ABOUT)) {
            return;
        }
        if (SystemInfo.isJava_9_orLater) {
            handlerClassName = "java.awt.desktop.AboutHandler";
        } else if (SystemInfo.isMacOS) {
            handlerClassName = "com.apple.eawt.AboutHandler";
        } else {
            return;
        }
        FlatDesktop.setHandler("setAboutHandler", handlerClassName, aboutHandler);
    }

    public static void setPreferencesHandler(Runnable preferencesHandler) {
        String handlerClassName;
        if (!FlatDesktop.isSupported(Action.APP_PREFERENCES)) {
            return;
        }
        if (SystemInfo.isJava_9_orLater) {
            handlerClassName = "java.awt.desktop.PreferencesHandler";
        } else if (SystemInfo.isMacOS) {
            handlerClassName = "com.apple.eawt.PreferencesHandler";
        } else {
            return;
        }
        FlatDesktop.setHandler("setPreferencesHandler", handlerClassName, preferencesHandler);
    }

    private static void setHandler(String setHandlerMethodName, String handlerClassName, Runnable handler) {
        try {
            Object desktopOrApplication = FlatDesktop.getDesktopOrApplication();
            Class<?> handlerClass = Class.forName(handlerClassName);
            Method m = desktopOrApplication.getClass().getMethod(setHandlerMethodName, handlerClass);
            m.invoke(desktopOrApplication, Proxy.newProxyInstance(FlatDesktop.class.getClassLoader(), new Class[]{handlerClass}, (proxy, method, args) -> {
                EventQueue.invokeLater(() -> handler.run());
                return null;
            }));
        }
        catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    public static void setQuitHandler(Consumer<QuitResponse> quitHandler) {
        String handlerClassName;
        if (!FlatDesktop.isSupported(Action.APP_QUIT_HANDLER)) {
            return;
        }
        if (SystemInfo.isJava_9_orLater) {
            handlerClassName = "java.awt.desktop.QuitHandler";
        } else if (SystemInfo.isMacOS) {
            handlerClassName = "com.apple.eawt.QuitHandler";
        } else {
            return;
        }
        try {
            Object desktopOrApplication = FlatDesktop.getDesktopOrApplication();
            Class<?> handlerClass = Class.forName(handlerClassName);
            Method m = desktopOrApplication.getClass().getMethod("setQuitHandler", handlerClass);
            m.invoke(desktopOrApplication, Proxy.newProxyInstance(FlatDesktop.class.getClassLoader(), new Class[]{handlerClass}, (proxy, method, args) -> {
                final Object response = args[1];
                final String responseClass = SystemInfo.isJava_9_orLater ? "java.awt.desktop.QuitResponse" : "com.apple.eawt.QuitResponse";
                quitHandler.accept(new QuitResponse(){

                    @Override
                    public void performQuit() {
                        try {
                            Class.forName(responseClass).getMethod("performQuit", new Class[0]).invoke(response, new Object[0]);
                        }
                        catch (Exception ex) {
                            LoggingFacade.INSTANCE.logSevere(null, ex);
                        }
                    }

                    @Override
                    public void cancelQuit() {
                        try {
                            Class.forName(responseClass).getMethod("cancelQuit", new Class[0]).invoke(response, new Object[0]);
                        }
                        catch (Exception ex) {
                            LoggingFacade.INSTANCE.logSevere(null, ex);
                        }
                    }
                });
                return null;
            }));
        }
        catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    private static Object getDesktopOrApplication() throws Exception {
        if (SystemInfo.isJava_9_orLater) {
            return Desktop.getDesktop();
        }
        if (SystemInfo.isMacOS) {
            try {
                Class<?> cls = Class.forName("com.apple.eawt.Application");
                return cls.getMethod("getApplication", new Class[0]).invoke(null, new Object[0]);
            }
            catch (Exception ex) {
                LoggingFacade.INSTANCE.logSevere(null, ex);
                throw new UnsupportedOperationException();
            }
        }
        throw new UnsupportedOperationException();
    }

    public static interface QuitResponse {
        public void performQuit();

        public void cancelQuit();
    }

    public static enum Action {
        APP_ABOUT,
        APP_PREFERENCES,
        APP_QUIT_HANDLER;

    }
}

