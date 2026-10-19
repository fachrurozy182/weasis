/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;

public class SimpleTaggable
implements Taggable {
    private final Map<TagW, Object> tags;

    public SimpleTaggable() {
        this.tags = new HashMap<TagW, Object>();
    }

    public SimpleTaggable(Map<TagW, Object> tags) {
        this.tags = tags;
    }

    @Override
    public void setTag(TagW tag, Object value) {
        if (tag != null) {
            this.tags.put(tag, value);
        }
    }

    @Override
    public boolean containTagKey(TagW tag) {
        return this.tags.containsKey(tag);
    }

    @Override
    public void setTagNoNull(TagW tag, Object value) {
        if (tag != null && value != null) {
            this.tags.put(tag, value);
        }
    }

    @Override
    public Object getTagValue(TagW tag) {
        return tag == null ? null : this.tags.get(tag);
    }

    @Override
    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator() {
        return this.tags.entrySet().iterator();
    }
}

