/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.api.net.auth.AuthProvider
 *  org.weasis.core.api.net.auth.AuthRegistration
 *  org.weasis.core.api.net.auth.DefaultAuthMethod
 *  org.weasis.core.api.net.auth.OAuth2ServiceFactory
 *  org.weasis.core.api.service.BundlePreferences
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.pref.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.AuthProvider;
import org.weasis.core.api.net.auth.AuthRegistration;
import org.weasis.core.api.net.auth.DefaultAuthMethod;
import org.weasis.core.api.net.auth.OAuth2ServiceFactory;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;

public class AuthenticationPersistence {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationPersistence.class);
    private static final String FILENAME = "authenticationNodes.xml";
    private static final String T_NODES = "methods";
    private static final String T_NODE = "method";
    private static final String T_UID = "uid";
    private static final String T_CODE = "code";
    private static final String T_REGISTRATION = "registration";
    private static final String T_PROVIDER = "provider";
    private static final String T_CLIENT_ID = "id";
    private static final String T_CLIENT_SECRET = "secret";
    private static final String T_SCOPE = "scope";
    private static final String T_NAME = "name";
    private static final String T_AUTH_URI = "auth";
    private static final String T_TOKEN_URI = "token";
    private static final String T_REVOKE_URI = "revoke";
    private static final String T_OPENID = "openid";
    private static final String T_AUDIENCE = "audience";
    private static final String T_GRANT_TYPE = "grantType";
    private static final Map<String, AuthMethod> methods = new HashMap<String, AuthMethod>();

    private AuthenticationPersistence() {
    }

    public static AuthMethod getAuthMethod(String serviceId) {
        if (methods.isEmpty()) {
            AuthenticationPersistence.loadMethods();
        }
        return methods.getOrDefault(serviceId, (AuthMethod)OAuth2ServiceFactory.NO_AUTH);
    }

    public static Map<String, AuthMethod> getMethods() {
        if (methods.isEmpty()) {
            AuthenticationPersistence.loadMethods();
        }
        return methods;
    }

    public static Collection<AuthMethod> loadMethods() {
        if (methods.isEmpty()) {
            ArrayList<AuthMethod> list = new ArrayList<AuthMethod>();
            AuthenticationPersistence.loadMethods(list, ResourceUtil.getResource((String)FILENAME), false);
            BundleContext context = AppProperties.getBundleContext(AbstractDicomNode.class);
            AuthenticationPersistence.loadMethods(list, BundlePreferences.getFileInDataFolder((BundleContext)context, (String)FILENAME).toFile(), true);
            for (AuthMethod m : list) {
                methods.put(m.getUid(), m);
            }
            return list;
        }
        return methods.values();
    }

    public static void loadMethods(JComboBox<AuthMethod> comboBox) {
        comboBox.addItem((AuthMethod)OAuth2ServiceFactory.NO_AUTH);
        Collection<AuthMethod> list = AuthenticationPersistence.loadMethods();
        for (AuthMethod node : list) {
            comboBox.addItem(node);
        }
    }

    public static void addOrUpdateMethod(AuthMethod method) {
        if (method == null || !StringUtil.hasText((CharSequence)method.getUid())) {
            return;
        }
        if (methods.isEmpty()) {
            AuthenticationPersistence.loadMethods();
        }
        methods.put(method.getUid(), method);
        OAuth2ServiceFactory.invalidateService((String)method.getUid());
        AuthenticationPersistence.saveMethod();
    }

    public static void removeMethod(AuthMethod method) {
        if (method == null || !StringUtil.hasText((CharSequence)method.getUid())) {
            return;
        }
        if (methods.isEmpty()) {
            AuthenticationPersistence.loadMethods();
        }
        if (methods.remove(method.getUid()) != null) {
            OAuth2ServiceFactory.invalidateService((String)method.getUid());
            AuthenticationPersistence.saveMethod();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void saveMethod() {
        XMLStreamWriter writer = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        BundleContext context = AppProperties.getBundleContext(AbstractDicomNode.class);
        try {
            writer = factory.createXMLStreamWriter(new FileOutputStream(BundlePreferences.getFileInDataFolder((BundleContext)context, (String)FILENAME).toFile()), "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement(T_NODES);
            for (AuthMethod node : methods.values()) {
                if (!node.isLocal()) continue;
                writer.writeStartElement(T_NODE);
                AuthenticationPersistence.writeNode(writer, T_UID, node.getUid());
                AuthenticationPersistence.writeNode(writer, T_CODE, node.getCode());
                AuthProvider p = node.getAuthProvider();
                writer.writeStartElement(T_PROVIDER);
                AuthenticationPersistence.writeNode(writer, T_NAME, p.name());
                AuthenticationPersistence.writeNode(writer, T_AUTH_URI, p.authorizationUri());
                AuthenticationPersistence.writeNode(writer, T_TOKEN_URI, p.tokenUri());
                AuthenticationPersistence.writeNode(writer, T_REVOKE_URI, p.revokeTokenUri());
                writer.writeAttribute(T_OPENID, Boolean.toString(p.openId()));
                writer.writeEndElement();
                AuthRegistration reg = node.getAuthRegistration();
                writer.writeStartElement(T_REGISTRATION);
                AuthenticationPersistence.writeNode(writer, T_CLIENT_ID, reg.clientId());
                AuthenticationPersistence.writeNode(writer, T_CLIENT_SECRET, reg.clientSecret());
                AuthenticationPersistence.writeNode(writer, T_SCOPE, reg.scope());
                AuthenticationPersistence.writeNode(writer, T_AUDIENCE, reg.audience());
                AuthenticationPersistence.writeNode(writer, T_GRANT_TYPE, reg.getAuthorizationGrantType());
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            StreamUtil.safeClose((XMLStreamWriter)writer);
        }
        catch (Exception e) {
            LOGGER.error("Error on writing DICOM node file", (Throwable)e);
        }
        finally {
            StreamUtil.safeClose(writer);
        }
    }

    private static void writeNode(XMLStreamWriter writer, String key, String value) throws XMLStreamException {
        writer.writeAttribute(key, StringUtil.hasText((CharSequence)value) ? value : "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void loadMethods(List<AuthMethod> list, File prefs, boolean local) {
        if (prefs.canRead()) {
            XMLStreamReader xmler = null;
            try {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
                factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
                xmler = factory.createXMLStreamReader(new FileInputStream(prefs));
                while (xmler.hasNext()) {
                    int eventType = xmler.next();
                    if (eventType != 1) continue;
                    AuthenticationPersistence.readMethods(xmler, list, local);
                }
                StreamUtil.safeClose((XMLStreamReader)xmler);
            }
            catch (Exception e) {
                LOGGER.error("Error on reading DICOM node file", (Throwable)e);
            }
            finally {
                StreamUtil.safeClose(xmler);
            }
        }
    }

    private static void readMethods(XMLStreamReader xmler, List<AuthMethod> list, boolean local) throws XMLStreamException {
        String key = xmler.getName().getLocalPart();
        if (T_NODES.equals(key)) {
            while (xmler.hasNext()) {
                int eventType = xmler.next();
                if (eventType != 1) continue;
                AuthenticationPersistence.readMethod(xmler, list, local);
            }
        }
    }

    private static void readMethod(XMLStreamReader xmler, List<AuthMethod> list, boolean local) {
        String key = xmler.getName().getLocalPart();
        if (T_NODE.equals(key)) {
            try {
                String uid = xmler.getAttributeValue(null, T_UID);
                String code = xmler.getAttributeValue(null, T_CODE);
                AuthProviderBuilder providerBuilder = new AuthProviderBuilder();
                AuthRegistrationBuilder registrationBuilder = new AuthRegistrationBuilder();
                while (xmler.hasNext()) {
                    int eventType = xmler.next();
                    if (eventType == 1) {
                        AuthenticationPersistence.readSubElements(xmler, providerBuilder, registrationBuilder);
                        continue;
                    }
                    if (eventType != 2 || !T_REGISTRATION.equals(xmler.getName().getLocalPart())) continue;
                    break;
                }
                AuthProvider provider = providerBuilder.build();
                AuthRegistration registration = registrationBuilder.build();
                DefaultAuthMethod node = new DefaultAuthMethod(uid, provider, registration);
                node.setCode(code);
                node.setLocal(local);
                list.add((AuthMethod)node);
            }
            catch (Exception e) {
                LOGGER.error("Cannot read DicomNode", (Throwable)e);
            }
        }
    }

    private static void readSubElements(XMLStreamReader xmler, AuthProviderBuilder providerBuilder, AuthRegistrationBuilder registrationBuilder) {
        String key = xmler.getName().getLocalPart();
        if (T_PROVIDER.equals(key)) {
            providerBuilder.name = xmler.getAttributeValue(null, T_NAME);
            providerBuilder.authorizationUri = xmler.getAttributeValue(null, T_AUTH_URI);
            providerBuilder.tokenUri = xmler.getAttributeValue(null, T_TOKEN_URI);
            providerBuilder.revokeTokenUri = xmler.getAttributeValue(null, T_REVOKE_URI);
            providerBuilder.openId = LangUtil.emptyToFalse((String)xmler.getAttributeValue(null, T_OPENID));
        } else if (T_REGISTRATION.equals(key)) {
            registrationBuilder.clientId = xmler.getAttributeValue(null, T_CLIENT_ID);
            registrationBuilder.clientSecret = xmler.getAttributeValue(null, T_CLIENT_SECRET);
            registrationBuilder.scope = xmler.getAttributeValue(null, T_SCOPE);
            registrationBuilder.audience = xmler.getAttributeValue(null, T_AUDIENCE);
            registrationBuilder.grantType = xmler.getAttributeValue(null, T_GRANT_TYPE);
        }
    }

    private static class AuthProviderBuilder {
        String name;
        String authorizationUri;
        String tokenUri;
        String revokeTokenUri;
        boolean openId;

        private AuthProviderBuilder() {
        }

        AuthProvider build() {
            return new AuthProvider(this.name, this.authorizationUri, this.tokenUri, this.revokeTokenUri, this.openId);
        }
    }

    private static class AuthRegistrationBuilder {
        String clientId;
        String clientSecret;
        String scope;
        String audience;
        String grantType;

        private AuthRegistrationBuilder() {
        }

        AuthRegistration build() {
            return new AuthRegistration(this.clientId, this.clientSecret, this.scope, this.audience, null, this.grantType);
        }
    }
}

