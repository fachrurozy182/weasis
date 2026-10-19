/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.EndEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.CAI_WithLocatorSupport;
import ch.qos.logback.core.joran.spi.ElementPath;
import ch.qos.logback.core.joran.spi.EventPlayer;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class SaxEventInterpreter {
    private static Action NOP_ACTION_SINGLETON = new NOPAction();
    private final RuleStore ruleStore;
    private final SaxEventInterpretationContext interpretationContext;
    private Supplier<Action> implicitActionSupplier;
    private final CAI_WithLocatorSupport cai;
    private ElementPath elementPath;
    Locator locator;
    EventPlayer eventPlayer;
    Context context;
    Stack<Action> actionStack;
    ElementPath skip = null;

    public SaxEventInterpreter(Context context, RuleStore rs, ElementPath initialElementPath, List<SaxEvent> saxEvents) {
        this.context = context;
        this.cai = new CAI_WithLocatorSupport(context, this);
        this.ruleStore = rs;
        this.interpretationContext = new SaxEventInterpretationContext(context, this);
        this.elementPath = initialElementPath;
        this.actionStack = new Stack();
        this.eventPlayer = new EventPlayer(this, saxEvents);
    }

    public EventPlayer getEventPlayer() {
        return this.eventPlayer;
    }

    public ElementPath getCopyOfElementPath() {
        return this.elementPath.duplicate();
    }

    public SaxEventInterpretationContext getSaxEventInterpretationContext() {
        return this.interpretationContext;
    }

    public void startDocument() {
    }

    public void startElement(StartEvent se) {
        this.setDocumentLocator(se.getLocator());
        this.startElement(se.namespaceURI, se.localName, se.qName, se.attributes);
    }

    private void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        String tagName = this.getTagName(localName, qName);
        this.elementPath.push(tagName);
        if (this.skip != null) {
            this.pushEmptyActionOntoActionStack();
            return;
        }
        Action applicableAction = this.getApplicableAction(this.elementPath, atts);
        if (applicableAction != null) {
            this.actionStack.add(applicableAction);
            this.callBeginAction(applicableAction, tagName, atts);
        } else {
            this.pushEmptyActionOntoActionStack();
            String errMsg = "no applicable action for [" + tagName + "], current ElementPath  is [" + String.valueOf(this.elementPath) + "]";
            this.cai.addError(errMsg);
        }
    }

    private void pushEmptyActionOntoActionStack() {
        this.actionStack.push(NOP_ACTION_SINGLETON);
    }

    public void characters(BodyEvent be) {
        this.setDocumentLocator(be.locator);
        String body = be.getText();
        Action applicableAction = this.actionStack.peek();
        if (body != null && (body = body.trim()).length() > 0) {
            this.callBodyAction(applicableAction, body);
        }
    }

    public void endElement(EndEvent endEvent) {
        this.setDocumentLocator(endEvent.locator);
        this.endElement(endEvent.namespaceURI, endEvent.localName, endEvent.qName);
    }

    private void endElement(String namespaceURI, String localName, String qName) {
        Action applicableAction = this.actionStack.pop();
        if (this.skip != null) {
            if (this.skip.equals(this.elementPath)) {
                this.skip = null;
            }
        } else if (applicableAction != NOP_ACTION_SINGLETON) {
            this.callEndAction(applicableAction, this.getTagName(localName, qName));
        }
        this.elementPath.pop();
    }

    public Locator getLocator() {
        return this.locator;
    }

    public void setDocumentLocator(Locator l) {
        this.locator = l;
    }

    String getTagName(String localName, String qName) {
        String tagName = localName;
        if (tagName == null || tagName.length() < 1) {
            tagName = qName;
        }
        return tagName;
    }

    public void setImplicitActionSupplier(Supplier<Action> actionSupplier) {
        this.implicitActionSupplier = actionSupplier;
    }

    Action getApplicableAction(ElementPath elementPath, Attributes attributes) {
        Supplier<Action> applicableActionSupplier = this.ruleStore.matchActions(elementPath);
        if (applicableActionSupplier != null) {
            Action applicableAction = applicableActionSupplier.get();
            applicableAction.setContext(this.context);
            return applicableAction;
        }
        Action implicitAction = this.implicitActionSupplier.get();
        implicitAction.setContext(this.context);
        return implicitAction;
    }

    void callBeginAction(Action applicableAction, String tagName, Attributes atts) {
        if (applicableAction == null) {
            return;
        }
        try {
            applicableAction.begin(this.interpretationContext, tagName, atts);
        }
        catch (ActionException e) {
            this.skip = this.elementPath.duplicate();
            this.cai.addError("ActionException in Action for tag [" + tagName + "]", e);
        }
        catch (RuntimeException e) {
            this.skip = this.elementPath.duplicate();
            this.cai.addError("RuntimeException in Action for tag [" + tagName + "]", e);
        }
    }

    private void callBodyAction(Action applicableAction, String body) {
        if (applicableAction == null) {
            return;
        }
        try {
            applicableAction.body(this.interpretationContext, body);
        }
        catch (ActionException ae) {
            this.cai.addError("Exception in body() method for action [" + String.valueOf(applicableAction) + "]", ae);
        }
    }

    private void callEndAction(Action applicableAction, String tagName) {
        if (applicableAction == null) {
            return;
        }
        try {
            applicableAction.end(this.interpretationContext, tagName);
        }
        catch (ActionException ae) {
            this.cai.addError("ActionException in Action for tag [" + tagName + "]", ae);
        }
        catch (RuntimeException e) {
            this.cai.addError("RuntimeException in Action for tag [" + tagName + "]", e);
        }
    }

    public RuleStore getRuleStore() {
        return this.ruleStore;
    }
}

