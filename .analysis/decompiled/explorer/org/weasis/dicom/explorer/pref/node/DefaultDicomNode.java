/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.AttributeSource
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.XmlAttributeSource
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.TransferSyntax
 *  org.weasis.dicom.param.DicomNode
 *  org.weasis.dicom.param.TlsOptions
 */
package org.weasis.dicom.explorer.pref.node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.weasis.core.api.media.data.AttributeSource;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.XmlAttributeSource;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TransferSyntax;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.TlsOptions;

public class DefaultDicomNode
extends AbstractDicomNode {
    protected static final String T_AETITLE = "aeTitle";
    protected static final String T_HOST = "hostname";
    protected static final String T_PORT = "port";
    protected String aeTitle;
    protected String hostname;
    protected int port;
    protected TlsOptions tlsOptions;

    public DefaultDicomNode(String description, String aeTitle, String hostname, Integer port, AbstractDicomNode.UsageType usageType) {
        super(description, AbstractDicomNode.Type.DICOM, usageType);
        this.hostname = hostname;
        this.setAeTitle(aeTitle);
        this.setPort(port);
    }

    @Override
    public String getToolTips() {
        return "<html>\n  %s<br>\n  %s: <b>%s@%s:%d</b>\n</html>\n".formatted(new Object[]{this, this.getType().toString(), this.aeTitle, this.hostname, this.port});
    }

    public String getAeTitle() {
        return this.aeTitle;
    }

    public void setAeTitle(String aeTitle) {
        if (!StringUtil.hasText((CharSequence)aeTitle)) {
            throw new IllegalArgumentException("Missing AET");
        }
        if (aeTitle.length() > 16) {
            throw new IllegalArgumentException("AET has more than 16 characters");
        }
        this.aeTitle = aeTitle.trim();
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        if (port != null && (port < 1 || port > 65535)) {
            throw new IllegalArgumentException("Port out of bound");
        }
        this.port = port == null ? 104 : port;
    }

    public TlsOptions getTlsOptions() {
        return this.tlsOptions;
    }

    public void setTlsOptions(TlsOptions tlsOptions) {
        this.tlsOptions = tlsOptions;
    }

    public DicomNode getDicomNode() {
        return new DicomNode(this.aeTitle, this.hostname, Integer.valueOf(this.port));
    }

    public DicomNode getDicomNodeWithOnlyAET() {
        return new DicomNode(this.aeTitle);
    }

    @Override
    public void saveDicomNode(XMLStreamWriter writer) throws XMLStreamException {
        super.saveDicomNode(writer);
        writer.writeAttribute(T_AETITLE, this.aeTitle);
        writer.writeAttribute(T_HOST, this.hostname);
        writer.writeAttribute(T_PORT, Integer.toString(this.port));
    }

    public static DefaultDicomNode buildDicomNodeEx(XMLStreamReader xmler) {
        DefaultDicomNode node = new DefaultDicomNode(xmler.getAttributeValue(null, "description"), xmler.getAttributeValue(null, T_AETITLE), xmler.getAttributeValue(null, T_HOST), TagUtil.getIntegerTagAttribute((AttributeSource)new XmlAttributeSource(xmler), (String)T_PORT, (Integer)104), AbstractDicomNode.UsageType.valueOf(xmler.getAttributeValue(null, "usageType")));
        node.setTsuid(TransferSyntax.getTransferSyntax((String)xmler.getAttributeValue(null, "tsuid")));
        return node;
    }
}

