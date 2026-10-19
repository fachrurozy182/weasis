/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.AppenderBase;

public class NPEAppender<E>
extends AppenderBase<E> {
    @Override
    protected void append(E eventObject) {
        throw new NullPointerException();
    }
}

