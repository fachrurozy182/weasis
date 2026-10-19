/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import java.util.Iterator;
import java.util.Map;
import org.weasis.core.api.media.data.TagW;

public interface TagReadable {
    public boolean containTagKey(TagW var1);

    public Object getTagValue(TagW var1);

    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator();
}

