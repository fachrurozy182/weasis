/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;

public class PrintOptions {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintOptions.class);
    private boolean showingAnnotations = true;
    private boolean center = true;
    private boolean colorPrint = true;
    private DotPerInches dpi = DotPerInches.DPI_150;

    public boolean isShowingAnnotations() {
        return this.showingAnnotations;
    }

    public void setShowingAnnotations(boolean showingAnnotations) {
        this.showingAnnotations = showingAnnotations;
    }

    public boolean isCenter() {
        return this.center;
    }

    public void setCenter(boolean center) {
        this.center = center;
    }

    public boolean isColorPrint() {
        return this.colorPrint;
    }

    public void setColorPrint(boolean colorPrint) {
        this.colorPrint = colorPrint;
    }

    public DotPerInches getDpi() {
        return this.dpi;
    }

    public void setDpi(DotPerInches dpi) {
        this.dpi = dpi == null ? DotPerInches.DPI_150 : dpi;
    }

    public static enum DotPerInches {
        DPI_100(100),
        DPI_150(150),
        DPI_200(200),
        DPI_250(250),
        DPI_300(300);

        private final int dpi;

        private DotPerInches(int dpi) {
            this.dpi = dpi;
        }

        public int getDpi() {
            return this.dpi;
        }

        public String toString() {
            return String.valueOf(this.dpi);
        }

        public static DotPerInches getInstance(String val, DotPerInches defaultValue) {
            if (StringUtil.hasText((CharSequence)val)) {
                try {
                    return DotPerInches.valueOf(val);
                }
                catch (Exception e) {
                    LOGGER.error("Cannot find DotPerInches: {}", (Object)val, (Object)e);
                }
            }
            return defaultValue;
        }
    }
}

