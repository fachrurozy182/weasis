/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.LoggingFacade;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
class LoggingFacadeImpl
implements LoggingFacade {
    private static final Logger LOG = Logger.getLogger(FlatLaf.class.getName());

    LoggingFacadeImpl() {
    }

    @Override
    public void logSevere(String message, Throwable t) {
        LOG.log(Level.SEVERE, message, t);
    }

    @Override
    public void logConfig(String message, Throwable t) {
        LOG.log(Level.CONFIG, message, t);
    }
}

