/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.spi.ContextAwareBase;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public abstract class Action
extends ContextAwareBase {
    public static final String NAME_ATTRIBUTE = "name";
    public static final String KEY_ATTRIBUTE = "key";
    public static final String VALUE_ATTRIBUTE = "value";
    public static final String FILE_ATTRIBUTE = "file";
    public static final String CLASS_ATTRIBUTE = "class";
    public static final String PATTERN_ATTRIBUTE = "pattern";
    public static final String SCOPE_ATTRIBUTE = "scope";
    public static final String ACTION_CLASS_ATTRIBUTE = "actionClass";

    public abstract void begin(SaxEventInterpretationContext var1, String var2, Attributes var3) throws ActionException;

    public void body(SaxEventInterpretationContext intercon, String body) throws ActionException {
    }

    public abstract void end(SaxEventInterpretationContext var1, String var2) throws ActionException;

    public String toString() {
        return this.getClass().getName();
    }

    protected int getColumnNumber(SaxEventInterpretationContext intercon) {
        SaxEventInterpreter interpreter = intercon.getSaxEventInterpreter();
        if (interpreter == null) {
            return -1;
        }
        Locator locator = interpreter.getLocator();
        if (locator != null) {
            return locator.getColumnNumber();
        }
        return -1;
    }

    public static int getLineNumber(SaxEventInterpretationContext intercon) {
        SaxEventInterpreter interpreter = intercon.getSaxEventInterpreter();
        if (interpreter == null) {
            return -1;
        }
        Locator locator = interpreter.getLocator();
        if (locator != null) {
            return locator.getLineNumber();
        }
        return -1;
    }

    protected String getLineColStr(SaxEventInterpretationContext intercon) {
        return "line: " + Action.getLineNumber(intercon) + ", column: " + this.getColumnNumber(intercon);
    }

    protected String atLine(SaxEventInterpretationContext intercon) {
        return "At line " + Action.getLineNumber(intercon);
    }

    protected String nearLine(SaxEventInterpretationContext intercon) {
        return "Near line " + Action.getLineNumber(intercon);
    }
}

