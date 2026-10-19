/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.media.data;

import java.util.stream.Stream;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.util.StringUtil;

public class TagView {
    private final TagW[] tag;
    private final String format;

    public TagView(TagW ... tag) {
        this((String)null, tag);
    }

    public TagView(String format, TagW ... tag) {
        this.tag = tag;
        this.format = format;
    }

    public TagW[] getTag() {
        return this.tag;
    }

    public String getFormat() {
        return this.format;
    }

    public boolean containsTag(TagW tag) {
        for (TagW tagW : this.tag) {
            if (!tagW.equals(tag)) continue;
            return true;
        }
        return false;
    }

    public String getFormattedText(boolean anonymize, TagReadable ... taggable) {
        TagReadable readable = Stream.of(taggable).filter(t -> t.containTagKey(TagW.Timezone)).findFirst().orElse(null);
        for (TagW t2 : this.tag) {
            if (anonymize && t2.getAnonymizationType() == 1) continue;
            String f = t2.addGMTOffset(this.format, readable);
            String str = t2.getFormattedTagValue(TagUtil.getTagValue(t2, taggable), f);
            if (!StringUtil.hasText((CharSequence)str)) continue;
            return str;
        }
        return "";
    }
}

