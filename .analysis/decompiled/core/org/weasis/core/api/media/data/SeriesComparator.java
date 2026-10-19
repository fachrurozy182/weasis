/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import java.util.Collections;
import java.util.Comparator;

public abstract class SeriesComparator<T>
implements Comparator<T> {
    private Comparator<T> inverse;

    public final Comparator<T> getReversOrderComparator() {
        if (this.inverse == null) {
            this.inverse = Collections.reverseOrder(this);
        }
        return this.inverse;
    }
}

