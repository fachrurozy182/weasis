/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.ComponentModel;

public class ShutdownHookModel
extends ComponentModel {
    private static final long serialVersionUID = 8886561840058239494L;

    @Override
    protected ShutdownHookModel makeNewInstance() {
        return new ShutdownHookModel();
    }
}

