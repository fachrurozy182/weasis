/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiUtils
 */
package org.weasis.dicom.qr;

import java.util.function.BooleanSupplier;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;

final class GracefulCancel {
    private static final Logger LOGGER = LoggerFactory.getLogger(GracefulCancel.class);
    public static final String CANCEL_GRACE = "dicom.qr.cancel.grace";
    private static final int DEFAULT_CANCEL_GRACE_MS = 1500;
    private static final int ABORT_GRACE_MS = 1500;

    private GracefulCancel() {
    }

    private static int cancelGraceMs() {
        return GuiUtils.getUICore().getSystemPreferences().getIntProperty(CANCEL_GRACE, 1500);
    }

    static void request(BooleanSupplier isDone, Runnable sendCancel, Runnable sendAbort, Runnable abortTask) {
        sendCancel.run();
        GracefulCancel.schedule(GracefulCancel.cancelGraceMs(), () -> {
            if (isDone.getAsBoolean()) {
                return;
            }
            LOGGER.info("The archive has not answered the C-CANCEL, aborting the association");
            sendAbort.run();
            GracefulCancel.schedule(1500, () -> {
                if (!isDone.getAsBoolean()) {
                    LOGGER.warn("The retrieve did not stop on abort, cancelling the task");
                    abortTask.run();
                }
            });
        });
    }

    private static void schedule(int delayMs, Runnable action) {
        Timer timer = new Timer(delayMs, actionEvent -> action.run());
        timer.setRepeats(false);
        timer.start();
    }
}

