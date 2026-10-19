/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import org.dcm4che3.data.Attributes;

public interface AttributesCoercion {
    public static final AttributesCoercion NONE = new AttributesCoercion(){

        @Override
        public void coerce(Attributes attrs, Attributes modified) {
        }

        @Override
        public String remapUID(String uid) {
            return uid;
        }
    };

    public void coerce(Attributes var1, Attributes var2) throws Exception;

    public String remapUID(String var1);
}

