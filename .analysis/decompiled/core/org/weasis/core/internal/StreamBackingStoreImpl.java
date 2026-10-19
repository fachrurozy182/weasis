/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.felix.prefs.BackingStore
 *  org.apache.felix.prefs.BackingStoreManager
 *  org.apache.felix.prefs.PreferencesDescription
 *  org.apache.felix.prefs.PreferencesImpl
 *  org.osgi.framework.Bundle
 *  org.osgi.framework.BundleContext
 *  org.osgi.service.prefs.BackingStoreException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.EscapeChars
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.felix.prefs.BackingStore;
import org.apache.felix.prefs.BackingStoreManager;
import org.apache.felix.prefs.PreferencesDescription;
import org.apache.felix.prefs.PreferencesImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.net.ClosableURLConnection;
import org.weasis.core.api.net.NetworkUtil;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.service.UICore;
import org.weasis.core.util.EscapeChars;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;

public class StreamBackingStoreImpl
implements BackingStore {
    private static final String PREFS_TAG = "preferences";
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamBackingStoreImpl.class);
    protected final BundleContext bundleContext;
    private final File prefRootDirectory;

    public StreamBackingStoreImpl(BundleContext context, String prefDir) {
        this.bundleContext = context;
        this.prefRootDirectory = new File(prefDir);
        this.prefRootDirectory.mkdirs();
    }

    protected void checkAccess() throws BackingStoreException {
        if (this.prefRootDirectory == null || !this.prefRootDirectory.exists()) {
            throw new BackingStoreException("Saving of data files to the bundle context is currently not supported.");
        }
    }

    protected OutputStream getOutputStream(PreferencesDescription desc) throws IOException {
        File file = this.getFile(desc);
        if (file == null) {
            throw new IOException("Cannot find the bundle " + desc.getBundleId());
        }
        FileUtil.prepareToWriteFile((Path)file.toPath());
        return new FileOutputStream(file);
    }

    public Long[] availableBundles() {
        return new Long[0];
    }

    protected PreferencesDescription getDescription(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".xml")) {
            Bundle[] bundles;
            String key = fileName.substring(0, fileName.length() - 4);
            for (Bundle bundle : bundles = this.bundleContext.getBundles()) {
                if (!bundle.getSymbolicName().equals(key)) continue;
                return new PreferencesDescription(Long.valueOf(bundle.getBundleId()), AppProperties.WEASIS_USER);
            }
        }
        return null;
    }

    public void remove(Long bundleId) throws BackingStoreException {
    }

    public PreferencesImpl[] loadAll(BackingStoreManager manager, Long bundleId) throws BackingStoreException {
        PreferencesImpl[] preferencesImplArray;
        this.checkAccess();
        PreferencesImpl pref = this.load(manager, new PreferencesDescription(bundleId, AppProperties.WEASIS_USER));
        if (pref == null) {
            preferencesImplArray = new PreferencesImpl[]{};
        } else {
            PreferencesImpl[] preferencesImplArray2 = new PreferencesImpl[1];
            preferencesImplArray = preferencesImplArray2;
            preferencesImplArray2[0] = pref;
        }
        return preferencesImplArray;
    }

    protected File getFile(PreferencesDescription desc) {
        Bundle bundle = this.bundleContext.getBundle(desc.getBundleId().longValue());
        if (bundle == null) {
            return null;
        }
        return new File(this.prefRootDirectory, bundle.getSymbolicName() + ".xml");
    }

    protected String getURL(PreferencesDescription desc, String prefUrl) throws UnsupportedEncodingException {
        Bundle bundle = this.bundleContext.getBundle(desc.getBundleId().longValue());
        if (bundle == null) {
            return null;
        }
        if (StringUtil.hasText((CharSequence)prefUrl)) {
            return String.format("%s?user=%s&profile=%s&module=%s", prefUrl, UICore.getUrlEncoding(AppProperties.WEASIS_USER), UICore.getUrlEncoding(AppProperties.WEASIS_PROFILE), UICore.getUrlEncoding(bundle.getSymbolicName()));
        }
        return null;
    }

    public PreferencesImpl load(BackingStoreManager manager, PreferencesDescription desc) {
        PreferencesImpl remotePrefs;
        PreferencesImpl localPrefs;
        block4: {
            localPrefs = null;
            remotePrefs = null;
            try {
                localPrefs = this.loadFromFile(manager, desc);
                remotePrefs = this.loadFromService(manager, desc);
            }
            catch (BackingStoreException e) {
                LOGGER.error("Error on preferences loading\n", (Throwable)e);
                if (e.getCause() instanceof BackingStoreException) break block4;
                LOGGER.error("", e.getCause());
            }
        }
        if (remotePrefs != null) {
            if (localPrefs == null) {
                localPrefs = new PreferencesImpl(desc, manager);
            }
            PreferencesImpl updatePrefs = remotePrefs.getOrCreateNode(localPrefs.absolutePath());
            StreamBackingStoreImpl.update(localPrefs, updatePrefs);
        }
        return localPrefs;
    }

    protected static void update(PreferencesImpl prefs, PreferencesImpl uprefs) {
        for (Map.Entry entry : uprefs.getProperties().entrySet()) {
            String val = (String)prefs.getProperties().get(entry.getKey());
            if (Objects.equals(val, entry.getValue())) continue;
            prefs.put((String)entry.getKey(), (String)entry.getValue());
        }
        HashMap<String, PreferencesImpl> children = new HashMap<String, PreferencesImpl>();
        for (PreferencesImpl child : prefs.getChildren()) {
            children.put(child.name(), child);
        }
        Collection collection = uprefs.getChildren();
        if (uprefs.getProperties().size() < prefs.getProperties().size() || collection.size() < children.size()) {
            prefs.getChangeSet().propertyChanged(prefs.getProperties().keySet().stream().findFirst().orElse(""));
        }
        for (PreferencesImpl uchild : collection) {
            String name = uchild.name();
            if (!StreamBackingStoreImpl.isValidElementName(name)) {
                LOGGER.warn("Skip merging remote preference node with invalid XML name: {}", (Object)name);
                continue;
            }
            children.computeIfAbsent(name, arg_0 -> ((PreferencesImpl)prefs).getOrCreateNode(arg_0));
            StreamBackingStoreImpl.update((PreferencesImpl)children.get(name), uchild);
        }
    }

    static boolean isValidElementName(String name) {
        if (!StringUtil.hasText((CharSequence)name) || !StreamBackingStoreImpl.isNameStartChar(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < name.length(); ++i) {
            if (StreamBackingStoreImpl.isNameChar(name.charAt(i))) continue;
            return false;
        }
        return true;
    }

    private static boolean isNameStartChar(char c) {
        return c == ':' || c == '_' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '\u00c0' && c <= '\u00d6' || c >= '\u00d8' && c <= '\u00f6' || c >= '\u00f8' && c <= '\u02ff' || c >= '\u0370' && c <= '\u037d' || c >= '\u037f' && c <= '\u1fff' || c >= '\u200c' && c <= '\u200d' || c >= '\u2070' && c <= '\u218f' || c >= '\u2c00' && c <= '\u2fef' || c >= '\u3001' && c <= '\ud7ff' || c >= '\uf900' && c <= '\ufdcf' || c >= '\ufdf0' && c <= '\ufffd';
    }

    private static boolean isNameChar(char c) {
        return StreamBackingStoreImpl.isNameStartChar(c) || c == '-' || c == '.' || c >= '0' && c <= '9' || c == '\u00b7' || c >= '\u0300' && c <= '\u036f' || c >= '\u203f' && c <= '\u2040';
    }

    protected static void clearAllChangeSet(PreferencesImpl prefs) {
        prefs.getChangeSet().clear();
        for (PreferencesImpl child : prefs.getChildren()) {
            StreamBackingStoreImpl.clearAllChangeSet(child);
        }
    }

    /*
     * Loose catch block
     */
    protected PreferencesImpl loadFromFile(BackingStoreManager manager, PreferencesDescription desc) throws BackingStoreException {
        this.checkAccess();
        File file = this.getFile(desc);
        if (file != null && file.exists()) {
            XMLStreamReader xmler = null;
            FileInputStream fileReader = new FileInputStream(file);
            PreferencesImpl rootPref = new PreferencesImpl(desc, manager);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
            factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
            xmler = factory.createXMLStreamReader(fileReader);
            PreferencesImpl preferencesImpl = this.readStream(rootPref, xmler);
            fileReader.close();
            StreamUtil.safeClose((XMLStreamReader)xmler);
            return preferencesImpl;
            {
                catch (Throwable throwable) {
                    try {
                        try {
                            try {
                                fileReader.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                            throw throwable;
                        }
                        catch (IOException | XMLStreamException e) {
                            throw new BackingStoreException("Unable to load preferences from File: " + file.getPath(), (Throwable)e);
                        }
                    }
                    catch (Throwable throwable3) {
                        StreamUtil.safeClose(xmler);
                        throw throwable3;
                    }
                }
            }
        }
        return null;
    }

    private static URLParameters getURLParameters(boolean post) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (post) {
            map.put("Content-Type", "text/plain; charset=UTF-8");
        } else {
            map.put("Accept", "text/plain");
        }
        return new URLParameters(map, post);
    }

    protected PreferencesImpl loadFromService(BackingStoreManager manager, PreferencesDescription desc) throws BackingStoreException {
        String prefUrl = GuiUtils.getUICore().getPrefServiceUrl();
        if (StringUtil.hasText((CharSequence)prefUrl) && (!GuiUtils.getUICore().isLocalSession() || GuiUtils.getUICore().isStoreLocalSession())) {
            String serviceURL;
            try {
                serviceURL = this.getURL(desc, prefUrl);
            }
            catch (UnsupportedEncodingException e) {
                throw new BackingStoreException("Unable to load preferences from Service: " + prefUrl, (Throwable)e);
            }
            ClosableURLConnection conn = NetworkUtil.getUrlConnection(serviceURL, StreamBackingStoreImpl.getURLParameters(false));
            try {
                PreferencesImpl preferencesImpl = this.readRemotePref(new PreferencesImpl(desc, manager), conn);
                if (conn != null) {
                    conn.close();
                }
                return preferencesImpl;
            }
            catch (Throwable throwable) {
                try {
                    if (conn != null) {
                        try {
                            conn.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new BackingStoreException("Unable to load preferences from Service: " + serviceURL, (Throwable)e);
                }
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private PreferencesImpl readRemotePref(PreferencesImpl rootPref, ClosableURLConnection conn) throws BackingStoreException, IOException {
        PreferencesImpl preferencesImpl;
        InputStream fileReader;
        XMLStreamReader xmler;
        block12: {
            xmler = null;
            fileReader = conn.getInputStream();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
            factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
            xmler = factory.createXMLStreamReader(fileReader);
            preferencesImpl = this.readStream(rootPref, xmler);
            if (fileReader == null) break block12;
            fileReader.close();
        }
        StreamUtil.safeClose((XMLStreamReader)xmler);
        return preferencesImpl;
        {
            catch (Throwable factory) {
                try {
                    try {
                        if (fileReader != null) {
                            try {
                                fileReader.close();
                            }
                            catch (Throwable throwable) {
                                factory.addSuppressed(throwable);
                            }
                        }
                        throw factory;
                    }
                    catch (XMLStreamException e) {
                        boolean isHttpNoContent = false;
                        URLConnection uRLConnection = conn.urlConnection();
                        if (uRLConnection instanceof HttpURLConnection) {
                            HttpURLConnection httpURLConnection = (HttpURLConnection)uRLConnection;
                            boolean bl = isHttpNoContent = 204 == httpURLConnection.getResponseCode();
                        }
                        if (!isHttpNoContent) {
                            throw new BackingStoreException("Unable to read remote preferences from Service: " + String.valueOf(conn.urlConnection().getURL()), (Throwable)e);
                        }
                        StreamUtil.safeClose(xmler);
                    }
                }
                catch (Throwable throwable) {
                    StreamUtil.safeClose(xmler);
                    throw throwable;
                }
            }
        }
        return null;
    }

    private PreferencesImpl readStream(PreferencesImpl root, XMLStreamReader xmler) throws XMLStreamException {
        while (xmler.hasNext()) {
            String key;
            int eventType = xmler.next();
            if (eventType != 1 || !PREFS_TAG.equals(key = xmler.getName().getLocalPart())) continue;
            while (xmler.hasNext()) {
                eventType = xmler.next();
                if (eventType != 1) continue;
                this.read(root, xmler, xmler.getName().getLocalPart());
            }
        }
        root.getChangeSet().clear();
        return root;
    }

    public void store(PreferencesImpl prefs) throws BackingStoreException {
        if (!this.hasChanges(prefs)) {
            return;
        }
        PreferencesImpl rootPrefs = prefs.getRoot();
        try {
            this.storeInStream(rootPrefs);
        }
        catch (IOException e) {
            LOGGER.error("Cannot store preference file", (Throwable)e);
        }
        String prefUrl = GuiUtils.getUICore().getPrefServiceUrl();
        if (prefUrl != null) {
            try {
                this.remoteStore(rootPrefs, prefUrl);
            }
            catch (IOException e) {
                LOGGER.error("Cannot store preference file\n", (Throwable)e);
            }
        }
    }

    private void remoteStore(PreferencesImpl prefs, String prefUrl) throws IOException {
        File file;
        if ((!GuiUtils.getUICore().isLocalSession() || GuiUtils.getUICore().isStoreLocalSession()) && (file = this.getFile(prefs.getDescription())) != null && file.exists()) {
            URLParameters urlParams = StreamBackingStoreImpl.getURLParameters(true);
            ClosableURLConnection http = NetworkUtil.getUrlConnection(this.getURL(prefs.getDescription(), prefUrl), urlParams);
            try (OutputStream out = http.getOutputStream();){
                StreamBackingStoreImpl.writeStream(new FileInputStream(file), out);
            }
            URLConnection uRLConnection = http.urlConnection();
            if (uRLConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection)uRLConnection;
                NetworkUtil.readResponse(httpURLConnection, urlParams.headers());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void writeStream(InputStream inputStream, OutputStream out) throws IOException {
        try {
            int offset;
            byte[] buf = new byte[4096];
            while ((offset = inputStream.read(buf)) > 0) {
                out.write(buf, 0, offset);
            }
            out.flush();
        }
        finally {
            StreamUtil.safeClose((AutoCloseable)inputStream);
        }
    }

    protected void storeInStream(PreferencesImpl prefs) throws BackingStoreException, IOException {
        PreferencesImpl rootPrefs = prefs.getRoot();
        XMLStreamWriter writer = null;
        try (OutputStream output = this.getOutputStream(rootPrefs.getDescription());){
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            writer = factory.createXMLStreamWriter(output, "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement(PREFS_TAG);
            this.write(rootPrefs, writer);
            writer.writeEndElement();
            writer.writeEndDocument();
            StreamBackingStoreImpl.clearAllChangeSet(rootPrefs);
        }
        catch (XMLStreamException e) {
            try {
                throw new BackingStoreException("Unable to store preferences.", (Throwable)e);
            }
            catch (Throwable throwable) {
                StreamUtil.safeClose(writer);
                throw throwable;
            }
        }
        StreamUtil.safeClose((XMLStreamWriter)writer);
    }

    protected boolean hasChanges(PreferencesImpl prefs) {
        if (prefs.getChangeSet().hasChanges()) {
            return true;
        }
        for (PreferencesImpl current : prefs.getChildren()) {
            if (!this.hasChanges(current)) continue;
            return true;
        }
        return false;
    }

    public void update(PreferencesImpl prefs) throws BackingStoreException {
    }

    protected void write(PreferencesImpl prefs, XMLStreamWriter writer) throws XMLStreamException {
        int size = prefs.getProperties().size();
        if (size > 0) {
            this.writePreferences(prefs, writer);
        }
        Collection children = prefs.getChildren();
        for (Object o : children) {
            PreferencesImpl child = (PreferencesImpl)o;
            String name = child.name();
            if (!StreamBackingStoreImpl.isValidElementName(name)) {
                LOGGER.warn("Skip preference node with invalid XML name: {}", (Object)name);
                continue;
            }
            writer.writeStartElement(name);
            this.write(child, writer);
            writer.writeEndElement();
        }
        writer.flush();
    }

    protected void read(PreferencesImpl prefs, XMLStreamReader xmler, String startKey) throws XMLStreamException {
        while (xmler.hasNext()) {
            int eventType = xmler.next();
            switch (eventType) {
                case 4: {
                    prefs.getProperties().put(startKey, xmler.getText());
                    break;
                }
                case 1: {
                    PreferencesImpl impl = prefs.getOrCreateNode(startKey);
                    this.read(impl, xmler, xmler.getName().getLocalPart());
                    impl.getChangeSet().clear();
                    break;
                }
                case 2: {
                    if (prefs.getProperties().isEmpty() && prefs.getChildren().isEmpty()) {
                        prefs.getOrCreateNode(startKey);
                    }
                    if (!startKey.equals(xmler.getName().getLocalPart())) break;
                    return;
                }
            }
        }
    }

    protected void writePreferences(PreferencesImpl prefs, XMLStreamWriter writer) throws XMLStreamException {
        for (Map.Entry stringStringEntry : prefs.getProperties().entrySet()) {
            String key = (String)stringStringEntry.getKey();
            if (!StreamBackingStoreImpl.isValidElementName(key)) {
                LOGGER.warn("Skip preference with invalid XML name: {}", (Object)key);
                continue;
            }
            writer.writeStartElement(key);
            writer.writeCharacters(EscapeChars.forXML((String)((String)stringStringEntry.getValue())));
            writer.writeEndElement();
        }
        writer.flush();
    }
}

