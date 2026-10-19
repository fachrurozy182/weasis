/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.tool.storescu;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.util.StringUtils;

public final class RelatedGeneralSOPClasses {
    private final Map<String, CommonExtendedNegotiation> commonExtNegs = new ConcurrentHashMap<String, CommonExtendedNegotiation>();

    public void init(Properties props) {
        if (props == null) {
            return;
        }
        props.stringPropertyNames().forEach(cuid -> {
            String[] relatedUIDs = StringUtils.split(props.getProperty((String)cuid), ',');
            CommonExtendedNegotiation negotiation = new CommonExtendedNegotiation((String)cuid, "1.2.840.10008.4.2", relatedUIDs);
            this.commonExtNegs.put((String)cuid, negotiation);
        });
    }

    public CommonExtendedNegotiation getCommonExtendedNegotiation(String cuid) {
        return this.commonExtNegs.getOrDefault(cuid, new CommonExtendedNegotiation(cuid, "1.2.840.10008.4.2", new String[0]));
    }

    public void clear() {
        this.commonExtNegs.clear();
    }

    public int size() {
        return this.commonExtNegs.size();
    }

    public boolean isEmpty() {
        return this.commonExtNegs.isEmpty();
    }
}

