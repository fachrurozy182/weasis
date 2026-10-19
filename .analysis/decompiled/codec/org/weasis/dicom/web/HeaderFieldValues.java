/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.weasis.core.util.StringUtil;

public final class HeaderFieldValues {
    private final List<Map<String, String>> parsedValues;

    public HeaderFieldValues(String headerFieldValue) {
        Objects.requireNonNull(headerFieldValue, "Header field value cannot be null");
        this.parsedValues = this.parseHeaderFieldValue(headerFieldValue);
    }

    public List<Map<String, String>> getValues() {
        return Collections.unmodifiableList(this.parsedValues);
    }

    public boolean hasKey(String key) {
        return this.parsedValues.stream().anyMatch(map -> map.containsKey(key));
    }

    public String getValue(String key) {
        return this.parsedValues.stream().map(map -> (String)map.get(key)).filter(StringUtil::hasText).findFirst().orElse(null);
    }

    public List<String> getValues(String key) {
        return this.parsedValues.stream().map(map -> (String)map.get(key)).filter(StringUtil::hasText).collect(Collectors.toList());
    }

    private List<Map<String, String>> parseHeaderFieldValue(String content) {
        if (!StringUtil.hasText((CharSequence)content)) {
            return new ArrayList<Map<String, String>>();
        }
        FieldValueParser parser = new FieldValueParser(content);
        return parser.parseAll();
    }

    private static class FieldValueParser {
        private char[] chars;
        private int position;
        private int start;
        private int end;

        FieldValueParser(String content) {
            this.chars = content.toCharArray();
            this.position = 0;
        }

        List<Map<String, String>> parseAll() {
            String[] elements;
            ArrayList<Map<String, String>> results = new ArrayList<Map<String, String>>();
            for (String element : elements = this.splitPreservingQuotes()) {
                Map<String, String> params = this.parseElement(element);
                if (params.isEmpty()) continue;
                results.add(params);
            }
            return results;
        }

        private String[] splitPreservingQuotes() {
            return new String(this.chars).split(",(?=(?:[^\"]*\"[^\"]*\")*+[^\"]*$)");
        }

        private Map<String, String> parseElement(String element) {
            HashMap<String, String> params = new HashMap<String, String>();
            this.chars = element.toCharArray();
            this.position = 0;
            while (this.hasMoreCharacters()) {
                String name = this.parseValue();
                String value = null;
                if (this.hasMoreCharacters() && this.chars[this.position] == '=') {
                    ++this.position;
                    value = this.parseQuotedValue();
                }
                this.skipSeparator();
                if (!StringUtil.hasText((CharSequence)name)) continue;
                params.put(name.toLowerCase(Locale.ROOT), value);
            }
            return params;
        }

        private boolean hasMoreCharacters() {
            return this.position < this.chars.length;
        }

        private String parseValue() {
            char c;
            this.start = this.position;
            this.end = this.position;
            while (this.hasMoreCharacters() && (c = this.chars[this.position]) != '=' && c != ';') {
                ++this.end;
                ++this.position;
            }
            return this.extractValue(false);
        }

        private String parseQuotedValue() {
            this.start = this.position;
            this.end = this.position;
            boolean quoted = false;
            boolean escaped = false;
            while (this.hasMoreCharacters()) {
                char c = this.chars[this.position];
                if (!quoted && c == ';') break;
                if (!escaped && c == '\"') {
                    quoted = !quoted;
                }
                escaped = !escaped && c == '\\';
                ++this.end;
                ++this.position;
            }
            return this.extractValue(true);
        }

        private String extractValue(boolean quoted) {
            while (this.start < this.end && Character.isWhitespace(this.chars[this.start])) {
                ++this.start;
            }
            while (this.end > this.start && Character.isWhitespace(this.chars[this.end - 1])) {
                --this.end;
            }
            if (quoted && this.end - this.start >= 2 && this.chars[this.start] == '\"' && this.chars[this.end - 1] == '\"') {
                ++this.start;
                --this.end;
            }
            return this.end > this.start ? new String(this.chars, this.start, this.end - this.start) : null;
        }

        private void skipSeparator() {
            if (this.hasMoreCharacters() && this.chars[this.position] == ';') {
                ++this.position;
            }
        }
    }
}

