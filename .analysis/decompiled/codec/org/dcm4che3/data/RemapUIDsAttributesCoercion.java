/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.util.Map;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.AttributesCoercion;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

public class RemapUIDsAttributesCoercion
implements AttributesCoercion {
    private final Map<String, String> uidMap;
    private final AttributesCoercion next;

    public RemapUIDsAttributesCoercion(Map<String, String> uidMap, AttributesCoercion next) {
        this.uidMap = uidMap;
        this.next = next;
    }

    @Override
    public String remapUID(String uid) {
        String remappedUID = this.uidMap != null ? StringUtils.maskNull(this.uidMap.get(uid), uid) : uid;
        return this.next != null ? this.next.remapUID(remappedUID) : remappedUID;
    }

    @Override
    public void coerce(Attributes attrs, Attributes modified) throws Exception {
        if (this.uidMap != null && !this.uidMap.isEmpty()) {
            UIDUtils.remapUIDs(attrs, this.uidMap);
        }
        if (this.next != null) {
            this.next.coerce(attrs, modified);
        }
    }
}

