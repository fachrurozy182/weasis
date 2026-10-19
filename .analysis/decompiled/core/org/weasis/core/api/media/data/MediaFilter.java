/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import org.weasis.core.api.media.data.MediaSeriesGroup;

@FunctionalInterface
public interface MediaFilter {
    public boolean accept(MediaSeriesGroup var1);
}

