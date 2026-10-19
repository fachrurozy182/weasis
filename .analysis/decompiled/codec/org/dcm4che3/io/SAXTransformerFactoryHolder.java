/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.dcm4che3.util.StringUtils;

public class SAXTransformerFactoryHolder {
    public static final SAXTransformerFactory factory = (SAXTransformerFactory)TransformerFactory.newInstance();

    static {
        try {
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", !"false".equalsIgnoreCase(StringUtils.getPropertyOrEnv("javax.xml.featureSecureProcessing", "JAVAX_XML_FEATURE_SECURE_PROCESSING", null)));
        }
        catch (TransformerConfigurationException e) {
            throw new AssertionError("All implementations are required to support the XMLConstants.FEATURE_SECURE_PROCESSING feature", e);
        }
        factory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", StringUtils.getPropertyOrEnv("javax.xml.accessExternalStylesheet", "JAVAX_XML_ACCESS_EXTERNAL_STYLESHEET", "file"));
    }
}

