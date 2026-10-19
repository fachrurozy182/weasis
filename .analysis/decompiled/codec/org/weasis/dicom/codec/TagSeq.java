/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Taggable
 */
package org.weasis.dicom.codec;

import java.util.function.Predicate;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.dicom.codec.TagD;

public class TagSeq
extends TagD {
    public TagSeq(int tagID, String keyword, String displayedName, String privateCreatorID, VR vr, int vmMin, int vmMax, Object defaultValue, boolean retired) {
        super(tagID, keyword, displayedName, privateCreatorID, vr, vmMin, vmMax, defaultValue, retired);
    }

    public void readValue(Object data, Taggable taggable) {
        if (data instanceof MacroSeqData) {
            Sequence seq;
            MacroSeqData macro = (MacroSeqData)data;
            Object val = this.getValue(macro.getAttributes());
            if (val instanceof Sequence && !(seq = (Sequence)val).isEmpty()) {
                for (Object item : seq) {
                    Attributes dataset;
                    if (!(item instanceof Attributes) || !this.processAttributes(dataset = (Attributes)item, taggable, macro)) continue;
                    return;
                }
            } else if (val instanceof Attributes) {
                Attributes dataset = (Attributes)val;
                this.processAttributes(dataset, taggable, macro);
            }
        }
    }

    private boolean processAttributes(Attributes dataset, Taggable taggable, MacroSeqData macro) {
        Predicate<? super Attributes> predicate = macro.getApplicable();
        if (predicate == null || predicate.test(dataset)) {
            for (TagW tag : macro.getTags()) {
                if (tag == null) continue;
                tag.readValue((Object)dataset, taggable);
            }
            return true;
        }
        return false;
    }

    public static class MacroSeqData {
        private final Attributes attributes;
        private final TagW[] tags;
        private final Predicate<? super Attributes> applicable;

        public MacroSeqData(Attributes attributes, TagW[] tags) {
            this(attributes, tags, null);
        }

        public MacroSeqData(Attributes attributes, TagW[] tags, Predicate<? super Attributes> applicable) {
            this.attributes = attributes;
            this.tags = tags;
            this.applicable = applicable;
        }

        public Attributes getAttributes() {
            return this.attributes;
        }

        public TagW[] getTags() {
            return this.tags;
        }

        public Predicate<? super Attributes> getApplicable() {
            return this.applicable;
        }
    }
}

