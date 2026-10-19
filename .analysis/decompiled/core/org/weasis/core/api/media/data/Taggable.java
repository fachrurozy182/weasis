/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;

public interface Taggable
extends TagReadable {
    public void setTag(TagW var1, Object var2);

    public void setTagNoNull(TagW var1, Object var2);

    default public void copyTags(TagW[] tagList, MediaElement media, boolean allowNullValue) {
        if (tagList != null && media != null) {
            for (TagW tag : tagList) {
                Object value = media.getTagValue(tag);
                if (!allowNullValue && value == null) continue;
                this.setTag(tag, value);
            }
        }
    }
}

