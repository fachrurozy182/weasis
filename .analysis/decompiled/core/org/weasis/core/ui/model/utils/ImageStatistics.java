/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.utils;

import org.weasis.core.Messages;
import org.weasis.core.ui.model.utils.bean.Measurement;

public interface ImageStatistics {
    public static final Measurement IMAGE_MIN = new Measurement(Messages.getString("measure.min"), 1, false, true, false);
    public static final Measurement IMAGE_MAX = new Measurement(Messages.getString("measure.max"), 2, false, true, false);
    public static final Measurement IMAGE_MEAN = new Measurement(Messages.getString("measure.mean"), 3, false, true, true);
    public static final Measurement IMAGE_STD = new Measurement(Messages.getString("measure.stdev"), 4, false, true, false);
    public static final Measurement IMAGE_SKEW = new Measurement(Messages.getString("measure.skew"), 5, false, true, false);
    public static final Measurement IMAGE_KURTOSIS = new Measurement(Messages.getString("measure.kurtosis"), 6, false, true, false);
    public static final Measurement IMAGE_PIXELS = new Measurement(Messages.getString("ImageStatistics.pixels"), 7, false, true, false);
    public static final Measurement IMAGE_MEDIAN = new Measurement(Messages.getString("ImageStatistics.median"), 8, false, true, false);
    public static final Measurement IMAGE_ENTROPY = new Measurement(Messages.getString("ImageStatistics.entropy"), 9, false, true, false);
    public static final Measurement[] ALL_MEASUREMENTS = new Measurement[]{IMAGE_PIXELS, IMAGE_MIN, IMAGE_MAX, IMAGE_MEDIAN, IMAGE_MEAN, IMAGE_STD, IMAGE_SKEW, IMAGE_KURTOSIS, IMAGE_ENTROPY};
}

