/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;

public interface MediaSeriesGroup
extends Taggable {
    public TagW getTagID();

    public boolean matchIdValue(Object var1);

    public TagW getTagElement(int var1);

    public void dispose();
}

