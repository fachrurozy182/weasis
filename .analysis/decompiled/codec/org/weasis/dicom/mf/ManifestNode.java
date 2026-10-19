/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.mf;

import java.io.IOException;
import java.io.Writer;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.mf.ManifestSerializer;
import org.weasis.dicom.mf.XmlManifestSerializer;

public interface ManifestNode {
    public static final Logger LOGGER = LoggerFactory.getLogger(ManifestNode.class);

    public void write(ManifestSerializer var1) throws IOException;

    default public void toXml(Writer writer) throws IOException {
        this.write(new XmlManifestSerializer(writer));
    }

    public static String keyword(int tagID) {
        String keyword = ElementDictionary.getStandardElementDictionary().keywordOf(tagID);
        if (keyword == null) {
            LOGGER.error("Cannot find keyword for DICOM tag ID {}", (Object)TagUtils.toString(tagID));
        }
        return keyword;
    }

    public static enum Level {
        PATIENT("Patient"),
        STUDY("Study"),
        SERIES("Series"),
        INSTANCE("Instance"),
        FRAME("Frame");

        private final String tagName;

        private Level(String tagName) {
            this.tagName = tagName;
        }

        public String getTagName() {
            return this.tagName;
        }

        public String toString() {
            return this.tagName;
        }
    }
}

