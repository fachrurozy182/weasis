/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.ComponentModel;

public class ImplicitModel
extends ComponentModel {
    private static final long serialVersionUID = 5507123447813692833L;

    @Override
    protected ImplicitModel makeNewInstance() {
        return new ImplicitModel();
    }
}

