/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.TransferSyntax
 */
package org.weasis.dicom.explorer.pref.node;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TransferSyntax;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.AuthenticationPersistence;

public class DicomWebNode
extends AbstractDicomNode {
    public static final String T_URL = "url";
    public static final String T_WEB_TYPE = "webtype";
    public static final String T_HEADER = "headers";
    public static final String T_AUTH = "auth";
    private URL url;
    private WebType webType;
    private final Map<String, String> headers;
    private String authMethodUid;

    public DicomWebNode(String description, WebType webType, URL url, AbstractDicomNode.UsageType usageType) {
        super(description, AbstractDicomNode.Type.WEB, usageType);
        this.url = url;
        this.webType = webType;
        this.headers = new HashMap<String, String>();
        this.authMethodUid = "5aa85854-8f1f-11eb-b339-d3daace59a05";
    }

    @Override
    public String getToolTips() {
        return "<html>\n  %s<br>\n  %s: <b>%s</b>\n</html>\n".formatted(new Object[]{this, this.webType.toString(), this.url});
    }

    public String getAuthMethodUid() {
        return this.authMethodUid;
    }

    public void setAuthMethodUid(String authMethodUid) {
        this.authMethodUid = StringUtil.hasText((CharSequence)authMethodUid) ? authMethodUid : "5aa85854-8f1f-11eb-b339-d3daace59a05";
    }

    public AuthMethod getAuthMethod() {
        return AuthenticationPersistence.getAuthMethod(this.authMethodUid);
    }

    public WebType getWebType() {
        return this.webType;
    }

    public void setWebType(WebType webType) {
        this.webType = webType;
    }

    public URL getUrl() {
        return this.url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void addHeader(String key, String value) {
        if (StringUtil.hasText((CharSequence)key)) {
            this.headers.put(key, value);
        }
    }

    public void removeHeader(String key) {
        if (StringUtil.hasText((CharSequence)key)) {
            this.headers.remove(key);
        }
    }

    @Override
    public void saveDicomNode(XMLStreamWriter writer) throws XMLStreamException {
        super.saveDicomNode(writer);
        writer.writeAttribute(T_URL, this.url.toString());
        writer.writeAttribute(T_WEB_TYPE, StringUtil.getEmptyStringIfNullEnum((Enum)this.webType));
        writer.writeAttribute(T_AUTH, this.authMethodUid);
        String val = this.headers.entrySet().stream().map(map -> (String)map.getKey() + ":" + (String)map.getValue()).collect(Collectors.joining("\n"));
        writer.writeAttribute(T_HEADER, Base64.getEncoder().encodeToString(val.getBytes(StandardCharsets.UTF_8)));
    }

    public static AbstractDicomNode.UsageType getUsageType(WebType webType) {
        if (WebType.DICOMWEB.equals((Object)webType)) {
            return AbstractDicomNode.UsageType.BOTH;
        }
        if (WebType.STOWRS.equals((Object)webType)) {
            return AbstractDicomNode.UsageType.STORAGE;
        }
        return AbstractDicomNode.UsageType.RETRIEVE;
    }

    public static DicomWebNode buildDicomWebNode(XMLStreamReader xmler) throws MalformedURLException {
        WebType webType = WebType.valueOf(xmler.getAttributeValue(null, T_WEB_TYPE));
        DicomWebNode node = new DicomWebNode(xmler.getAttributeValue(null, "description"), webType, new URL(xmler.getAttributeValue(null, T_URL)), DicomWebNode.getUsageType(webType));
        node.setTsuid(TransferSyntax.getTransferSyntax((String)xmler.getAttributeValue(null, "tsuid")));
        node.setAuthMethodUid(xmler.getAttributeValue(null, T_AUTH));
        String val = xmler.getAttributeValue(null, T_HEADER);
        if (StringUtil.hasText((CharSequence)val)) {
            String[] entry;
            String result = new String(Base64.getDecoder().decode(val), StandardCharsets.UTF_8);
            for (String s : entry = result.split("[\\n]+")) {
                String[] kv = s.split(":", 2);
                if (kv.length != 2) continue;
                node.addHeader(kv[0].trim(), kv[1].trim());
            }
        }
        return node;
    }

    public static enum WebType {
        DICOMWEB("DICOMweb (all RESTful services)"),
        QIDORS("QIDO-RS (query)"),
        STOWRS("STOW-RS (store)"),
        WADO("WADO-URI (non-RS)"),
        WADORS("WADO-RS (retrieve)");

        final String title;

        private WebType(String title) {
            this.title = title;
        }

        public String toString() {
            return this.title;
        }
    }
}

