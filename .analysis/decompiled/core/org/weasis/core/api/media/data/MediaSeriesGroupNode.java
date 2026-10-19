/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.weasis.core.Messages;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;

public class MediaSeriesGroupNode
implements MediaSeriesGroup {
    public static final MediaSeriesGroup rootNode = new MediaSeriesGroupNode(TagW.RootElement, "__ROOT__", null);
    private final TagW tagID;
    private final TagView displayTag;
    private final HashMap<TagW, Object> tags = new HashMap();

    public MediaSeriesGroupNode(TagW tagID, Object identifier, TagView displayTag) {
        this.tagID = Objects.requireNonNull(tagID);
        this.tags.put(tagID, Objects.requireNonNull(identifier));
        this.displayTag = displayTag == null ? new TagView(tagID) : displayTag;
    }

    @Override
    public TagW getTagID() {
        return this.tagID;
    }

    @Override
    public boolean containTagKey(TagW tag) {
        return this.tags.containsKey(tag);
    }

    @Override
    public boolean matchIdValue(Object valueID) {
        return Objects.equals(this.tags.get(this.tagID), valueID);
    }

    public String toString() {
        String val = this.displayTag.getFormattedText(false, this);
        return val == null ? Messages.getString("MediaSeriesGroupNode.no_val") : val;
    }

    public int hashCode() {
        return Objects.hash(this.tags.get(this.tagID));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.tags.get(this.tagID), ((MediaSeriesGroup)o).getTagValue(this.tagID));
    }

    @Override
    public void setTag(TagW tag, Object value) {
        if (tag != null && !tag.equals(this.tagID)) {
            this.tags.put(tag, value);
        }
    }

    public void removeTag(TagW tag) {
        if (tag != null && !tag.equals(this.tagID)) {
            this.tags.remove(tag);
        }
    }

    @Override
    public void setTagNoNull(TagW tag, Object value) {
        if (tag != null && value != null && !tag.equals(this.tagID)) {
            this.tags.put(tag, value);
        }
    }

    @Override
    public Object getTagValue(TagW tag) {
        return tag == null ? null : this.tags.get(tag);
    }

    @Override
    public TagW getTagElement(int id) {
        for (TagW e : this.tags.keySet()) {
            if (e.getId() != id) continue;
            return e;
        }
        return null;
    }

    @Override
    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator() {
        return this.tags.entrySet().iterator();
    }

    @Override
    public void dispose() {
    }
}

