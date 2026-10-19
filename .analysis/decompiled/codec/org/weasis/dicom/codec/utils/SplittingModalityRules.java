/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.display.Modality;

public class SplittingModalityRules {
    private final Modality modality;
    private final List<Rule> singleFrameTags;
    private final List<Rule> multiFrameTags;
    private final SplittingModalityRules extendRules;

    public SplittingModalityRules(Modality modality) {
        this(modality, null);
    }

    public SplittingModalityRules(Modality modality, SplittingModalityRules extendRules) {
        this.modality = modality;
        this.extendRules = extendRules;
        this.singleFrameTags = extendRules == null ? new ArrayList<Rule>() : new ArrayList<Rule>(extendRules.getSingleFrameRules());
        this.multiFrameTags = extendRules == null ? new ArrayList<Rule>() : new ArrayList<Rule>(extendRules.getMultiFrameRules());
    }

    public Modality getModality() {
        return this.modality;
    }

    public List<Rule> getSingleFrameRules() {
        return this.singleFrameTags;
    }

    public List<Rule> getMultiFrameRules() {
        return this.multiFrameTags;
    }

    public SplittingModalityRules getExtendRules() {
        return this.extendRules;
    }

    public void addSingleFrameTags(TagW tag, Condition condition) {
        if (tag != null) {
            this.singleFrameTags.add(new Rule(tag, condition));
            DicomMediaIO.tagManager.addTag(tag, TagD.Level.INSTANCE);
        }
    }

    public void addMultiFrameTags(TagW tag, Condition condition) {
        if (tag != null) {
            this.multiFrameTags.add(new Rule(tag, condition));
            DicomMediaIO.tagManager.addTag(tag, TagD.Level.FRAME);
        }
    }

    public void addSingleFrameTags(int tagID, Condition condition) {
        this.addSingleFrameTags(TagD.getNullable(tagID), condition);
    }

    public void addMultiFrameTags(int tagID, Condition condition) {
        this.addMultiFrameTags(TagD.getNullable(tagID), condition);
    }

    public static class Rule {
        protected final TagW tag;
        protected final Condition condition;

        public Rule(TagW tag, Condition condition) {
            this.tag = tag;
            this.condition = condition;
        }

        public TagW getTag() {
            return this.tag;
        }

        public Condition getCondition() {
            return this.condition;
        }

        public boolean isTagValueMatching(MediaElement seriesMedia, MediaElement newMedia) {
            Object val2;
            Object val1 = seriesMedia.getTagValue(this.tag);
            if (TagUtil.isEquals((Object)val1, (Object)(val2 = newMedia.getTagValue(this.tag)))) {
                return true;
            }
            if (this.condition != null) {
                return !this.condition.match(newMedia);
            }
            return false;
        }
    }

    public static abstract class Condition {
        protected boolean not;

        public final Condition not() {
            this.not = !this.not;
            return this;
        }

        public abstract boolean match(MediaElement var1);

        public void addChild(Condition child) {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty() {
            return false;
        }

        public static enum Type {
            equals,
            notEquals,
            equalsIgnoreCase,
            notEqualsIgnoreCase,
            contains,
            notContains,
            containsIgnoreCase,
            notContainsIgnoreCase;

        }
    }

    public static class DefaultCondition
    extends Condition {
        final TagW tag;
        final Condition.Type type;
        final Object object;

        public DefaultCondition(TagW tag, Condition.Type type, String value) {
            this.tag = tag;
            this.type = type;
            this.object = this.readValue(value);
        }

        private Object readValue(String value) {
            if (!StringUtil.hasText((CharSequence)value)) {
                return null;
            }
            return this.tag.getValue((Object)value);
        }

        @Override
        public boolean match(MediaElement media) {
            Object value = media.getTagValue(this.tag);
            if (Condition.Type.equals.equals((Object)this.type)) {
                return TagUtil.isEquals((Object)value, (Object)this.object);
            }
            if (Condition.Type.notEquals.equals((Object)this.type)) {
                return !TagUtil.isEquals((Object)value, (Object)this.object);
            }
            if (Condition.Type.equalsIgnoreCase.equals((Object)this.type)) {
                return TagUtil.isEquals((Object)value, (Object)this.object, (boolean)true);
            }
            if (Condition.Type.notEqualsIgnoreCase.equals((Object)this.type)) {
                return !TagUtil.isEquals((Object)value, (Object)this.object, (boolean)true);
            }
            String str = null;
            if (this.object != null) {
                str = this.object.getClass().isArray() && Array.getLength(this.object) > 0 ? Array.get(this.object, 0).toString() : this.object.toString();
            }
            switch (this.type.ordinal()) {
                case 4: {
                    return TagUtil.isContaining((Object)value, str, (boolean)false);
                }
                case 5: {
                    return !TagUtil.isContaining((Object)value, str, (boolean)false);
                }
                case 6: {
                    return TagUtil.isContaining((Object)value, str, (boolean)true);
                }
                case 7: {
                    return !TagUtil.isContaining((Object)value, str, (boolean)true);
                }
            }
            return false;
        }
    }

    public static class Or
    extends CompositeCondition {
        @Override
        public boolean match(MediaElement media) {
            for (Condition child : this.childs) {
                if (!child.match(media)) continue;
                return !this.not;
            }
            return this.not;
        }
    }

    public static class And
    extends CompositeCondition {
        @Override
        public boolean match(MediaElement media) {
            for (Condition child : this.childs) {
                if (child.match(media)) continue;
                return this.not;
            }
            return !this.not;
        }
    }

    static abstract class CompositeCondition
    extends Condition {
        protected final ArrayList<Condition> childs = new ArrayList();

        CompositeCondition() {
        }

        @Override
        public void addChild(Condition child) {
            this.childs.add(child);
        }

        @Override
        public boolean isEmpty() {
            return this.childs.isEmpty();
        }
    }
}

