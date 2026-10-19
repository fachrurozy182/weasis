/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.web;

import java.nio.charset.StandardCharsets;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.web.HeaderFieldValues;

public final class BoundaryExtractor {
    private BoundaryExtractor() {
    }

    public static byte[] extractBoundary(String contentType, String requiredType) {
        if (!StringUtil.hasText((CharSequence)contentType)) {
            return null;
        }
        HeaderFieldValues parser = new HeaderFieldValues(contentType);
        String boundaryValue = parser.getValue("boundary");
        if (boundaryValue == null) {
            return null;
        }
        if (requiredType != null && !BoundaryExtractor.hasRequiredType(parser, requiredType)) {
            return null;
        }
        return boundaryValue.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static boolean hasRequiredType(HeaderFieldValues parser, String requiredType) {
        return parser.hasKey(requiredType) || parser.getValues().stream().anyMatch(map -> map.containsValue(requiredType));
    }
}

