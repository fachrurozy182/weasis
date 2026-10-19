/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.util.OptionHelper;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JNDIUtil {
    static final String RESTRICTION_MSG = "JNDI name must start with java: but was ";

    public static Context getInitialContext() throws NamingException {
        return new InitialContext();
    }

    public static Context getInitialContext(Hashtable<?, ?> props) throws NamingException {
        return new InitialContext(props);
    }

    public static Object lookupObject(Context ctx, String name) throws NamingException {
        if (ctx == null) {
            return null;
        }
        if (OptionHelper.isNullOrEmptyOrAllSpaces(name)) {
            return null;
        }
        JNDIUtil.jndiNameSecurityCheck(name);
        Object lookup = ctx.lookup(name);
        return lookup;
    }

    public static void jndiNameSecurityCheck(String name) throws NamingException {
        if (!name.startsWith("java:")) {
            throw new NamingException(RESTRICTION_MSG + name);
        }
    }

    public static String lookupString(Context ctx, String name) throws NamingException {
        Object lookup = JNDIUtil.lookupObject(ctx, name);
        return (String)lookup;
    }
}

