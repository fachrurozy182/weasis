/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MockInitialContext
extends InitialContext {
    public Map<String, Object> map = new HashMap<String, Object>();

    @Override
    public Object lookup(String name) throws NamingException {
        if (name == null) {
            return null;
        }
        return this.map.get(name);
    }
}

