/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.data;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.AttributesCoercion;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.VR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrimISO2020CharacterSetAttributesCoercion
implements AttributesCoercion {
    static final Logger LOG = LoggerFactory.getLogger(TrimISO2020CharacterSetAttributesCoercion.class);
    private AttributesCoercion next;

    public TrimISO2020CharacterSetAttributesCoercion(AttributesCoercion next) {
        this.next = next;
    }

    @Override
    public String remapUID(String uid) {
        return this.next != null ? this.next.remapUID(uid) : uid;
    }

    @Override
    public void coerce(Attributes attrs, Attributes modified) throws Exception {
        LOG.info("ISO 2022 Character Set trimmed by coercion {}", (Object)this);
        String[] codes = attrs.getStrings(524293);
        if (SpecificCharacterSet.trimISO2022(codes)) {
            Object originalValue = attrs.setString(524293, VR.CS, codes);
            if (modified != null) {
                modified.setValue(524293, VR.CS, originalValue);
            }
        }
        if (this.next != null) {
            this.next.coerce(attrs, modified);
        }
    }
}

