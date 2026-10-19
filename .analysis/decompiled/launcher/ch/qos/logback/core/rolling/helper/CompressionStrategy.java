/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.spi.ContextAware;

public interface CompressionStrategy
extends ContextAware {
    public void compress(String var1, String var2, String var3);
}

