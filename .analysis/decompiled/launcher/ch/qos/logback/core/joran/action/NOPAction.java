/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import org.xml.sax.Attributes;

public class NOPAction
extends Action {
    @Override
    public void begin(SaxEventInterpretationContext ec, String name, Attributes attributes) {
    }

    @Override
    public void end(SaxEventInterpretationContext ec, String name) {
    }
}

