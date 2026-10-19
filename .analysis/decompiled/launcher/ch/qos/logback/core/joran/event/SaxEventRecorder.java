/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.event;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.EndEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.spi.ElementPath;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareImpl;
import ch.qos.logback.core.status.Status;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxEventRecorder
extends DefaultHandler
implements ContextAware {
    final ContextAwareImpl contextAwareImpl;
    final ElementPath elementPath;
    List<SaxEvent> saxEventList = new ArrayList<SaxEvent>();
    Locator locator;

    public SaxEventRecorder(Context context) {
        this(context, new ElementPath());
    }

    public SaxEventRecorder(Context context, ElementPath elementPath) {
        this.contextAwareImpl = new ContextAwareImpl(context, this);
        this.elementPath = elementPath;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        this.addWarn("Document Type Declaration (DOCTYPE) with external file reference is");
        this.addWarn("disallowed to prevent Server-Side Request Forgery (SSRF) attacks.");
        this.addWarn("returning contents of SYSTEM " + systemId + " as a white space");
        return new InputSource(new ByteArrayInputStream(" ".getBytes()));
    }

    public final void recordEvents(InputStream inputStream) throws JoranException {
        this.recordEvents(new InputSource(inputStream));
    }

    public void recordEvents(InputSource inputSource) throws JoranException {
        SAXParser saxParser = this.buildSaxParser();
        try {
            saxParser.parse(inputSource, (DefaultHandler)this);
            return;
        }
        catch (IOException ie) {
            this.handleError("I/O error occurred while parsing xml file", ie);
        }
        catch (SAXException se) {
            throw new JoranException("Problem parsing XML document. See previously reported errors.", se);
        }
        catch (Exception ex) {
            this.handleError("Unexpected exception while parsing XML document.", ex);
        }
        throw new IllegalStateException("This point can never be reached");
    }

    private void handleError(String errMsg, Throwable t) throws JoranException {
        this.addError(errMsg, t);
        throw new JoranException(errMsg, t);
    }

    private SAXParser buildSaxParser() throws JoranException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setNamespaceAware(true);
            return spf.newSAXParser();
        }
        catch (ParserConfigurationException pce) {
            String errMsg = "Error during SAX paser configuration. See https://logback.qos.ch/codes.html#saxParserConfiguration";
            this.addError(errMsg, pce);
            throw new JoranException(errMsg, pce);
        }
        catch (SAXException pce) {
            String errMsg = "Error during parser creation or parser configuration";
            this.addError(errMsg, pce);
            throw new JoranException(errMsg, pce);
        }
    }

    @Override
    public void startDocument() {
    }

    public Locator getLocator() {
        return this.locator;
    }

    @Override
    public void setDocumentLocator(Locator l) {
        this.locator = l;
    }

    protected boolean shouldIgnoreForElementPath(String tagName) {
        return false;
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        String tagName = this.getTagName(localName, qName);
        if (!this.shouldIgnoreForElementPath(tagName)) {
            this.elementPath.push(tagName);
        }
        ElementPath current = this.elementPath.duplicate();
        this.saxEventList.add(new StartEvent(current, namespaceURI, localName, qName, atts, this.getLocator()));
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String bodyStr = new String(ch, start, length);
        SaxEvent lastEvent = this.getLastEvent();
        if (lastEvent instanceof BodyEvent) {
            BodyEvent be = (BodyEvent)lastEvent;
            be.append(bodyStr);
        } else if (!this.isSpaceOnly(bodyStr)) {
            this.saxEventList.add(new BodyEvent(bodyStr, this.getLocator()));
        }
    }

    boolean isSpaceOnly(String bodyStr) {
        String bodyTrimmed = bodyStr.trim();
        return bodyTrimmed.length() == 0;
    }

    SaxEvent getLastEvent() {
        if (this.saxEventList.isEmpty()) {
            return null;
        }
        int size = this.saxEventList.size();
        return this.saxEventList.get(size - 1);
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) {
        this.saxEventList.add(new EndEvent(namespaceURI, localName, qName, this.getLocator()));
        String tagName = this.getTagName(localName, qName);
        if (!this.shouldIgnoreForElementPath(tagName)) {
            this.elementPath.pop();
        }
    }

    String getTagName(String localName, String qName) {
        String tagName = localName;
        if (tagName == null || tagName.length() < 1) {
            tagName = qName;
        }
        return tagName;
    }

    @Override
    public void error(SAXParseException spe) throws SAXException {
        this.addError("XML_PARSING - Parsing error on line " + spe.getLineNumber() + " and column " + spe.getColumnNumber());
        this.addError(spe.toString());
    }

    @Override
    public void fatalError(SAXParseException spe) throws SAXException {
        this.addError("XML_PARSING - Parsing fatal error on line " + spe.getLineNumber() + " and column " + spe.getColumnNumber());
        this.addError(spe.toString());
    }

    @Override
    public void warning(SAXParseException spe) throws SAXException {
        this.addWarn("XML_PARSING - Parsing warning on line " + spe.getLineNumber() + " and column " + spe.getColumnNumber(), spe);
    }

    @Override
    public void addError(String msg) {
        this.contextAwareImpl.addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        this.contextAwareImpl.addError(msg, ex);
    }

    @Override
    public void addInfo(String msg) {
        this.contextAwareImpl.addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        this.contextAwareImpl.addInfo(msg, ex);
    }

    @Override
    public void addStatus(Status status) {
        this.contextAwareImpl.addStatus(status);
    }

    @Override
    public void addWarn(String msg) {
        this.contextAwareImpl.addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        this.contextAwareImpl.addWarn(msg, ex);
    }

    @Override
    public Context getContext() {
        return this.contextAwareImpl.getContext();
    }

    @Override
    public void setContext(Context context) {
        this.contextAwareImpl.setContext(context);
    }

    public List<SaxEvent> getSaxEventList() {
        return this.saxEventList;
    }
}

