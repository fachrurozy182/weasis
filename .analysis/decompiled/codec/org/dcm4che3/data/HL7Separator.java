/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

public enum HL7Separator {
    FIELD("|", "\\F\\"),
    COMPONENT("^", "\\S\\"),
    SUBCOMPONENT("&", "\\T\\"),
    REPETITION("~", "\\R\\"),
    ESCAPE("\\", "\\E\\");

    public final String separator;
    public final String escapeSequence;

    private HL7Separator(String separator, String escapeSequence) {
        this.separator = separator;
        this.escapeSequence = escapeSequence;
    }

    public String escape(String s) {
        return s.replace(this.separator, this.escapeSequence);
    }

    public String unescape(String s) {
        return s.replace(this.escapeSequence, this.separator);
    }

    public static String escapeAll(String s) {
        return FIELD.escape(COMPONENT.escape(SUBCOMPONENT.escape(REPETITION.escape(ESCAPE.escape(s)))));
    }

    public static String unescapeAll(String s) {
        return ESCAPE.unescape(REPETITION.unescape(SUBCOMPONENT.unescape(COMPONENT.unescape(FIELD.unescape(s)))));
    }
}

