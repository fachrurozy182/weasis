/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.spi.ContextAwareImpl;
import org.xml.sax.Locator;

class CAI_WithLocatorSupport
extends ContextAwareImpl {
    CAI_WithLocatorSupport(Context context, SaxEventInterpreter interpreter) {
        super(context, interpreter);
    }

    @Override
    protected Object getOrigin() {
        SaxEventInterpreter i = (SaxEventInterpreter)super.getOrigin();
        Locator locator = i.locator;
        if (locator != null) {
            return SaxEventInterpreter.class.getName() + "@" + locator.getLineNumber() + ":" + locator.getColumnNumber();
        }
        return SaxEventInterpreter.class.getName() + "@NA:NA";
    }
}

