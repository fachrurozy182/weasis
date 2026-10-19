/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.ResourceAction;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.ResourceModel;

public class IncludeAction
extends ResourceAction {
    @Override
    protected ResourceModel makeNewResourceModel() {
        return new IncludeModel();
    }
}

