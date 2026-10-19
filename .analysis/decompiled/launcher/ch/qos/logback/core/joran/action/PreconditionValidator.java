/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;
import java.util.Arrays;
import org.xml.sax.Attributes;

public class PreconditionValidator
extends ContextAwareBase {
    boolean valid = true;
    SaxEventInterpretationContext seic;
    Attributes attributes;
    String tag;

    public PreconditionValidator(ContextAware origin, SaxEventInterpretationContext seic, String name, Attributes attributes) {
        super(origin);
        this.setContext(origin.getContext());
        this.seic = seic;
        this.tag = name;
        this.attributes = attributes;
    }

    public PreconditionValidator validateZeroAttributes() {
        if (this.attributes == null) {
            return this;
        }
        if (this.attributes.getLength() != 0) {
            this.addError("Element [" + this.tag + "] should have no attributes, near line " + Action.getLineNumber(this.seic));
            this.valid = false;
        }
        return this;
    }

    public PreconditionValidator validateClassAttribute() {
        return this.validateGivenAttribute("class");
    }

    public PreconditionValidator validateNameAttribute() {
        return this.validateGivenAttribute("name");
    }

    public PreconditionValidator validateValueAttribute() {
        return this.validateGivenAttribute("value");
    }

    public PreconditionValidator validateRefAttribute() {
        return this.validateGivenAttribute("ref");
    }

    public PreconditionValidator validateOneAndOnlyOneAttributeProvided(String ... names) {
        int validCount = 0;
        for (String name : names) {
            boolean invalid = this.isInvalidAttribute(name);
            if (invalid) continue;
            ++validCount;
        }
        if (validCount == 1) {
            this.valid = true;
        } else {
            this.valid = false;
            this.addError("Element [" + this.tag + "] should have at least one of [" + Arrays.toString(names) + "] as an attribute, near line " + Action.getLineNumber(this.seic));
        }
        return this;
    }

    public boolean isInvalidAttribute(String attributeName) {
        String attributeValue = this.attributes.getValue(attributeName);
        return OptionHelper.isNullOrEmptyOrAllSpaces(attributeValue);
    }

    public boolean isValidAttribute(String attributeName) {
        return !this.isInvalidAttribute(attributeName);
    }

    public PreconditionValidator validateGivenAttribute(String attributeName) {
        boolean invalid = this.isInvalidAttribute(attributeName);
        if (invalid) {
            this.addMissingAttributeError(attributeName);
            this.valid = false;
        }
        return this;
    }

    @Deprecated
    public PreconditionValidator generic(String attributeName) {
        return this.validateGivenAttribute(attributeName);
    }

    public void addMissingAttributeError(String attributeName) {
        this.addError("Missing attribute [" + attributeName + "]. " + this.getLocationSuffix());
    }

    public String getLocationSuffix() {
        return "See element [" + this.tag + "] near line " + Action.getLineNumber(this.seic);
    }

    public boolean isValid() {
        return this.valid;
    }
}

