/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Taggable
 */
package org.weasis.dicom.codec;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.dicom.codec.TagD;

public class TagManager {
    private final EnumMap<TagD.Level, List<TagW>> levelMap = new EnumMap(TagD.Level.class);

    public TagManager() {
        this.levelMap.put(TagD.Level.PATIENT, new ArrayList());
        this.levelMap.put(TagD.Level.STUDY, new ArrayList());
        this.levelMap.put(TagD.Level.SERIES, new ArrayList());
        this.levelMap.put(TagD.Level.INSTANCE, new ArrayList());
        this.levelMap.put(TagD.Level.FRAME, new ArrayList());
    }

    public void addTag(int tagID, TagD.Level level) {
        this.addTag(TagD.get(tagID), level);
    }

    public void addTag(TagW tag, TagD.Level level) {
        if (tag == null || level == null) {
            return;
        }
        List<TagW> list = this.levelMap.get((Object)level);
        if (list != null && !list.contains(tag)) {
            list.add(tag);
        }
    }

    public boolean contains(TagW tag, TagD.Level level) {
        if (tag == null || level == null) {
            return false;
        }
        List<TagW> list = this.levelMap.get((Object)level);
        return list != null && list.contains(tag);
    }

    public void readTags(TagD.Level level, Attributes header, Taggable tags) {
        if (level == null || header == null || tags == null) {
            return;
        }
        List<TagW> list = this.levelMap.get((Object)level);
        if (list != null) {
            for (TagW tagW : list) {
                tagW.readValue((Object)header, tags);
            }
        }
    }
}

