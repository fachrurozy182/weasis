/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.dcm4che3.util.StringUtils;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class SAXParserFactoryHolder {
    public static final SAXParserFactory factory = SAXParserFactory.newInstance();

    static {
        try {
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", !"false".equalsIgnoreCase(StringUtils.getPropertyOrEnv("javax.xml.featureSecureProcessing", "JAVAX_XML_FEATURE_SECURE_PROCESSING", null)));
        }
        catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new AssertionError("All implementations are required to support the XMLConstants.FEATURE_SECURE_PROCESSING feature", e);
        }
    }
}

