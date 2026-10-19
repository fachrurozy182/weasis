/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.param;

import java.util.List;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.param.AttributeEditor;
import org.weasis.dicom.param.AttributeEditorContext;
import org.weasis.dicom.util.Hmac;

public class DefaultAttributeEditor
implements AttributeEditor {
    protected static final List<Integer> SUPPORTED_UID_TAGS = List.of(Integer.valueOf(0x20000D), Integer.valueOf(0x20000E), Integer.valueOf(524312), Integer.valueOf(4096), Integer.valueOf(524376), Integer.valueOf(131075), Integer.valueOf(528725), Integer.valueOf(267537), Integer.valueOf(4097), Integer.valueOf(528743));
    private final boolean generateUIDs;
    private final Attributes tagToOverride;
    private final Hmac hmac;

    public DefaultAttributeEditor(Attributes tagToOverride) {
        this(false, tagToOverride);
    }

    public DefaultAttributeEditor(boolean generateUIDs, Attributes tagToOverride) {
        this(generateUIDs, null, tagToOverride);
    }

    public DefaultAttributeEditor(boolean generateUIDs, String globalKey, Attributes tagToOverride) {
        this.generateUIDs = generateUIDs;
        this.tagToOverride = tagToOverride;
        this.hmac = generateUIDs ? this.createHmac(globalKey) : null;
    }

    private Hmac createHmac(String globalKey) {
        byte[] key = StringUtil.hasText((CharSequence)globalKey) ? Hmac.hexToByte(globalKey) : Hmac.generateRandomKey();
        return new Hmac(key);
    }

    public boolean isGenerateUIDs() {
        return this.generateUIDs;
    }

    public Attributes getTagToOverride() {
        return this.tagToOverride;
    }

    public Hmac getHmac() {
        return this.hmac;
    }

    @Override
    public void apply(Attributes data, AttributeEditorContext context) {
        if (data == null) {
            return;
        }
        if (this.generateUIDs) {
            this.generateNewUIDs(data);
        }
        if (this.shouldOverrideAttributes()) {
            data.update(Attributes.UpdatePolicy.OVERWRITE, this.tagToOverride, null);
        }
    }

    private void generateNewUIDs(Attributes data) {
        UidVisitor visitor = new UidVisitor(this.hmac);
        try {
            data.accept(visitor, true);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to generate UIDs", e);
        }
    }

    private boolean shouldOverrideAttributes() {
        return this.tagToOverride != null && !this.tagToOverride.isEmpty();
    }

    private static class UidVisitor
    implements Attributes.Visitor {
        private final Hmac hmac;

        UidVisitor(Hmac hmac) {
            this.hmac = Objects.requireNonNull(hmac, "HMAC cannot be null");
        }

        @Override
        public boolean visit(Attributes attrs, int tag, VR vr, Object val) {
            if (this.isUidAttribute(vr, val, tag)) {
                this.processUidValue(attrs, tag, val);
            }
            return true;
        }

        private boolean isUidAttribute(VR vr, Object val, int tag) {
            return vr == VR.UI && val != Value.NULL && SUPPORTED_UID_TAGS.contains(tag);
        }

        private void processUidValue(Attributes attrs, int tag, Object val) {
            if (val instanceof byte[]) {
                this.processStringArrayValue(attrs, tag);
            } else if (val instanceof String[]) {
                String[] stringArray = (String[])val;
                this.processStringArray(stringArray);
            } else if (val instanceof String) {
                String singleString = (String)val;
                this.processSingleString(attrs, tag, singleString);
            } else {
                this.processSingleString(attrs, tag, val.toString());
            }
        }

        private void processStringArrayValue(Attributes attrs, int tag) {
            String[] strings = attrs.getStrings(tag);
            if (strings != null) {
                if (strings.length == 1) {
                    this.processSingleString(attrs, tag, strings[0]);
                } else {
                    this.processStringArray(strings);
                }
            }
        }

        private void processStringArray(String[] strings) {
            for (int i = 0; i < strings.length; ++i) {
                String hashedUid = this.hmac.uidHash(strings[i]);
                if (hashedUid == null) continue;
                strings[i] = hashedUid;
            }
        }

        private void processSingleString(Attributes attrs, int tag, String uid) {
            String hashedUid = this.hmac.uidHash(uid);
            if (hashedUid != null) {
                attrs.setString(tag, VR.UI, hashedUid);
            }
        }
    }
}

