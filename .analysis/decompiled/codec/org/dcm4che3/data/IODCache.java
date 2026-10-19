/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.IOException;
import java.util.HashMap;
import org.dcm4che3.data.IOD;

public class IODCache {
    private final HashMap<String, IOD> map = new HashMap();

    public void clear() {
        this.map.clear();
    }

    public IOD get(String uri) throws IOException {
        IOD iod = this.map.get(uri);
        if (iod == null) {
            iod = IOD.load(uri);
            this.map.put(uri, iod);
        }
        return iod;
    }
}

