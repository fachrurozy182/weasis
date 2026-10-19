/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MultipartHeaderParser {
    private MultipartHeaderParser() {
    }

    public static Map<String, String> parseHeaders(String headerContent) {
        Objects.requireNonNull(headerContent, "Header content cannot be null");
        HashMap<String, String> headers = new HashMap<String, String>();
        String[] lines = headerContent.split("\r\n");
        String currentFieldName = null;
        StringBuilder currentField = new StringBuilder();
        for (String line : lines) {
            int colonIndex;
            if (line.isEmpty()) break;
            if (line.startsWith(" ") || line.startsWith("\t")) {
                if (currentFieldName == null) continue;
                currentField.append(" ").append(line.trim());
                continue;
            }
            if (currentFieldName != null) {
                headers.merge(currentFieldName, currentField.toString(), (oldVal, newVal) -> oldVal + "," + newVal);
            }
            if ((colonIndex = line.indexOf(58)) > 0) {
                currentFieldName = line.substring(0, colonIndex).trim();
                currentField = new StringBuilder(line.substring(colonIndex + 1).trim());
                continue;
            }
            currentFieldName = null;
            currentField.setLength(0);
        }
        if (currentFieldName != null) {
            headers.merge(currentFieldName, currentField.toString(), (oldVal, newVal) -> oldVal + "," + newVal);
        }
        return headers;
    }

    private static String combineExistingValue(Map<String, String> headers, String fieldName, String newValue) {
        String existingValue = headers.get(fieldName);
        return existingValue != null ? existingValue + "," + newValue : newValue;
    }
}

