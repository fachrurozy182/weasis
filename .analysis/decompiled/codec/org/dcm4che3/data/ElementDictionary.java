/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.VR;

public abstract class ElementDictionary {
    private static final ServiceLoader<ElementDictionary> loader = ServiceLoader.load(ElementDictionary.class);
    private static final Map<String, ElementDictionary> map = new HashMap<String, ElementDictionary>();
    private final String privateCreator;
    private final Class<?> tagClass;

    protected ElementDictionary(String privateCreator, Class<?> tagClass) {
        this.privateCreator = privateCreator;
        this.tagClass = tagClass;
    }

    public static ElementDictionary getStandardElementDictionary() {
        return StandardElementDictionary.INSTANCE;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ElementDictionary getElementDictionary(String privateCreator) {
        if (privateCreator != null) {
            ElementDictionary dict1 = map.get(privateCreator);
            if (dict1 != null) {
                return dict1;
            }
            if (!map.containsKey(privateCreator)) {
                ServiceLoader<ElementDictionary> serviceLoader = loader;
                synchronized (serviceLoader) {
                    for (ElementDictionary dict : loader) {
                        map.putIfAbsent(dict.getPrivateCreator(), dict);
                        if (!privateCreator.equals(dict.getPrivateCreator())) continue;
                        return dict;
                    }
                    map.put(privateCreator, null);
                }
            }
        }
        return ElementDictionary.getStandardElementDictionary();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void reload() {
        ServiceLoader<ElementDictionary> serviceLoader = loader;
        synchronized (serviceLoader) {
            loader.reload();
        }
    }

    public static VR vrOf(int tag, String privateCreator) {
        return ElementDictionary.getElementDictionary(privateCreator).vrOf(tag);
    }

    public static String keywordOf(int tag, String privateCreator) {
        return ElementDictionary.getElementDictionary(privateCreator).keywordOf(tag);
    }

    public static int tagForKeyword(String keyword, String privateCreatorID) {
        return ElementDictionary.getElementDictionary(privateCreatorID).tagForKeyword(keyword);
    }

    public final String getPrivateCreator() {
        return this.privateCreator;
    }

    public abstract VR vrOf(int var1);

    public abstract String keywordOf(int var1);

    public int tmTagOf(int daTag) {
        return 0;
    }

    public int daTagOf(int tmTag) {
        return 0;
    }

    public int tagForKeyword(String keyword) {
        if (this.tagClass != null) {
            try {
                return this.tagClass.getField(keyword).getInt(null);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return -1;
    }
}

