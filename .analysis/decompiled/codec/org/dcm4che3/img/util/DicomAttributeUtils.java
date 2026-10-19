/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.img.util;

import java.io.IOException;
import java.util.Optional;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DicomAttributeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomAttributeUtils.class);

    private DicomAttributeUtils() {
    }

    public static String getModality(Attributes dcm) {
        return DicomAttributeUtils.findInHierarchy(dcm, 524384);
    }

    public static Optional<byte[]> getByteData(Attributes dicom, int tag) {
        return DicomAttributeUtils.getByteData(dicom, null, tag);
    }

    public static Optional<byte[]> getByteData(Attributes dicom, String privateCreator, int tag) {
        if (dicom == null || !dicom.containsValue(privateCreator, tag)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(dicom.getBytes(tag)).filter(data -> ((byte[])data).length > 0);
        }
        catch (IOException e) {
            LOGGER.error("Error extracting byte data from {}", (Object)TagUtils.toString(tag), (Object)e);
            return Optional.empty();
        }
    }

    private static String findInHierarchy(Attributes attributes, int tag) {
        for (Attributes current = attributes; current != null; current = current.getParent()) {
            String value = current.getString(tag);
            if (value == null || value.isEmpty()) continue;
            return value;
        }
        return null;
    }
}

