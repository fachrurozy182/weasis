/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.net;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HardenedObjectInputStream
extends ObjectInputStream {
    private final List<String> whitelistedClassNames;
    private static final String[] JAVA_CLASSES = new String[]{"java.lang.Boolean", "java.lang.Byte", "java.lang.Character", "java.lang.Double", "java.lang.Float", "java.lang.Integer", "java.lang.Long", "java.lang.Number", "java.lang.Short", "java.lang.String", "java.lang.Throwable", "java.util.ArrayList", "java.util.Collections$EmptyMap", "java.util.Collections$UnmodifiableMap", "java.util.concurrent.CopyOnWriteArrayList", "java.util.HashMap"};
    private static final int DEPTH_LIMIT = 16;
    private static final int ARRAY_LIMIT = 10000;
    private static final int ERROR_COUNT_LIMIT = 10;
    private final ContextAwareImpl contextAware;
    private final HashMap<String, Integer> errorMap = new HashMap();

    public HardenedObjectInputStream(Context context, InputStream in, String[] whitelistStrings) throws IOException {
        this(context, in, Arrays.asList(whitelistStrings));
    }

    public HardenedObjectInputStream(Context context, InputStream in, List<String> whitelist) throws IOException {
        super(in);
        this.contextAware = context != null ? new ContextAwareImpl(context, this) : null;
        this.initObjectFilter();
        this.whitelistedClassNames = new ArrayList<String>();
        this.whitelistedClassNames.addAll(whitelist);
    }

    private void initObjectFilter() {
        this.setObjectInputFilter(ObjectInputFilter.Config.createFilter("maxarray=10000;maxdepth=16;"));
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass anObjectStreamClass) throws IOException, ClassNotFoundException {
        String incomingClassName = anObjectStreamClass.getName();
        if (!this.isWhitelisted(incomingClassName)) {
            throw new InvalidClassException("Unauthorized deserialization attempt", anObjectStreamClass.getName());
        }
        return super.resolveClass(anObjectStreamClass);
    }

    @Override
    protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        throw new InvalidClassException("Unauthorized deserialization attempt ", Arrays.toString(interfaces));
    }

    private boolean isWhitelisted(String incomingClassName) {
        for (String javaClass : JAVA_CLASSES) {
            if (!incomingClassName.equals(javaClass)) continue;
            return true;
        }
        for (String whiteListed : this.whitelistedClassNames) {
            if (!incomingClassName.equals(whiteListed)) continue;
            return true;
        }
        int errorCount = this.errorMap.getOrDefault(incomingClassName, 0) + 1;
        this.errorMap.put(incomingClassName, errorCount);
        if (this.contextAware != null && errorCount < 10) {
            this.contextAware.addError("Unauthorized deserialization attempt for class [" + incomingClassName + "]");
            this.contextAware.addError("If you deem the class to be legitimate, please contact the project maintainers to have it whitelisted.");
        }
        return false;
    }

    protected void addToWhitelist(List<String> additionalAuthorizedClasses) {
        this.whitelistedClassNames.addAll(additionalAuthorizedClasses);
    }
}

