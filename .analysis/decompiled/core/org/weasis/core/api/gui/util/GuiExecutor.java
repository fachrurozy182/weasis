/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.api.gui.util;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiExecutor.class);

    private GuiExecutor() {
    }

    public static void execute(Runnable r) {
        if (r == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public static void invokeAndWait(Runnable r) {
        if (r == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            }
            catch (InterruptedException e) {
                LOGGER.warn("Interrupted Exception of {}", (Object)r);
                Thread.currentThread().interrupt();
            }
            catch (InvocationTargetException e) {
                LOGGER.error("EDT invokeAndWait()", (Throwable)e);
            }
        }
    }
}

