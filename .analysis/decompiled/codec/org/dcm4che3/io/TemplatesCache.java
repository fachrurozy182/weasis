/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.util.HashMap;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.dcm4che3.io.SAXTransformer;

public class TemplatesCache {
    private static TemplatesCache defaultCache;
    private final HashMap<String, Templates> map = new HashMap();

    public static synchronized TemplatesCache getDefault() {
        if (defaultCache == null) {
            defaultCache = new TemplatesCache();
        }
        return defaultCache;
    }

    public static synchronized void setDefault(TemplatesCache cache) {
        if (cache == null) {
            throw new NullPointerException();
        }
        defaultCache = cache;
    }

    public void clear() {
        this.map.clear();
    }

    public Templates get(String uri) throws TransformerConfigurationException {
        Templates tpl = this.map.get(uri);
        if (tpl == null) {
            tpl = SAXTransformer.newTemplates(new StreamSource(uri));
            this.map.put(uri, tpl);
        }
        return tpl;
    }
}

