/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.ComponentModel;

public class SequenceNumberGeneratorModel
extends ComponentModel {
    private static final long serialVersionUID = 4109015583434648277L;

    @Override
    protected SequenceNumberGeneratorModel makeNewInstance() {
        return new SequenceNumberGeneratorModel();
    }
}

