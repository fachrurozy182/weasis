/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.AttributesCoercion;

public class MergeAttributesCoercion
implements AttributesCoercion {
    private final Attributes newAttrs;
    private AttributesCoercion next;

    public MergeAttributesCoercion(Attributes mergedAttrs, AttributesCoercion next) {
        this.newAttrs = mergedAttrs;
        this.next = next;
    }

    @Override
    public String remapUID(String uid) {
        return this.next != null ? this.next.remapUID(uid) : uid;
    }

    @Override
    public void coerce(Attributes attrs, Attributes modified) throws Exception {
        Attributes.unifyCharacterSets(attrs, this.newAttrs);
        if (modified != null) {
            attrs.update(Attributes.UpdatePolicy.OVERWRITE, this.newAttrs, modified);
        } else {
            attrs.addAll(this.newAttrs);
        }
        if (this.next != null) {
            this.next.coerce(attrs, modified);
        }
    }
}

