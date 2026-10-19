/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.spi;

import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

public interface PropertyContainer {
    public void addSubstitutionProperty(String var1, String var2);

    public String getProperty(String var1);

    public Map<String, String> getCopyOfPropertyMap();

    default public void addSubstitutionProperties(Properties props) {
        if (props == null) {
            return;
        }
        props.forEach((BiConsumer<? super Object, ? super Object>)((BiConsumer<Object, Object>)(k, v) -> this.addSubstitutionProperty((String)k, (String)v)));
    }
}

