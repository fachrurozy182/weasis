/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.AttributesCoercion;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;

public class NullifyAttributesCoercion
implements AttributesCoercion {
    private final int[] nullifyTags;
    private AttributesCoercion next;

    public NullifyAttributesCoercion(int[] nullifyTags, AttributesCoercion next) {
        this.nullifyTags = Objects.requireNonNull(nullifyTags);
        this.next = next;
    }

    public static AttributesCoercion valueOf(int[] nullifyTags, AttributesCoercion next) {
        return nullifyTags != null && nullifyTags.length > 0 ? new NullifyAttributesCoercion(nullifyTags, next) : next;
    }

    @Override
    public String remapUID(String uid) {
        return this.next != null ? this.next.remapUID(uid) : uid;
    }

    @Override
    public void coerce(Attributes attrs, Attributes modified) throws Exception {
        VR.Holder vr = new VR.Holder();
        for (int nullifyTag : this.nullifyTags) {
            Object value = attrs.getValue(nullifyTag, vr);
            if (value == null || value == Value.NULL) continue;
            Object originalValue = attrs.setNull(nullifyTag, vr.vr);
            if (modified == null) continue;
            modified.setValue(nullifyTag, vr.vr, originalValue);
        }
        if (this.next != null) {
            this.next.coerce(attrs, modified);
        }
    }
}

