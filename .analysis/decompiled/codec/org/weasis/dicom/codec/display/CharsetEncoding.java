/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.codec.display;

import org.dcm4che3.data.SpecificCharacterSet;
import org.weasis.dicom.codec.Messages;

public enum CharsetEncoding {
    ISO_IR_192("ISO_IR 192", "UTF-8", Messages.getString("default.unicode")),
    ASCII("", "US-ASCII", "ASCII"),
    ISO_IR_100("ISO_IR 100", "ISO-8859-1", Messages.getString("latin.1")),
    ISO_IR_101("ISO_IR 101", "ISO-8859-2", Messages.getString("latin.2")),
    ISO_IR_109("ISO_IR 109", "ISO-8859-3", Messages.getString("latin.3")),
    ISO_IR_110("ISO_IR 110", "ISO-8859-4", Messages.getString("latin.4")),
    ISO_IR_144("ISO_IR 144", "ISO-8859-5", Messages.getString("cyrillic")),
    ISO_IR_127("ISO_IR 127", "ISO-8859-6", Messages.getString("arabic")),
    ISO_IR_126("ISO_IR 126", "ISO-8859-7", Messages.getString("greek")),
    ISO_IR_138("ISO_IR 138", "ISO-8859-8", Messages.getString("hebrew")),
    ISO_IR_148("ISO_IR 148", "ISO-8859-9", Messages.getString("turkish")),
    ISO_IR_13("ISO_IR 13", "JIS_X0201", Messages.getString("japanese.kana")),
    ISO_IR_166("ISO_IR 166", "TIS-620", Messages.getString("thai")),
    ISO_2022_IR_87("ISO 2022 IR 87", "x-JIS0208", Messages.getString("japanese.kanji")),
    ISO_2022_IR_159("ISO 2022 IR 159", "JIS_X0212-1990", Messages.getString("japanese.kanji.supplement")),
    ISO_2022_IR_149("ISO 2022 IR 149", "EUC-KR", Messages.getString("korean")),
    ISO_2022_IR_58("ISO 2022 IR 58", "GB2312", Messages.getString("simplified.chinese")),
    GB18030("GB18030", "GB18030", Messages.getString("chinese"));

    private final String label;
    private final String code;
    private final String readableText;

    private CharsetEncoding(String label, String code, String readableText) {
        this.label = label;
        this.code = code;
        this.readableText = readableText;
    }

    public String getLabel() {
        return this.label;
    }

    public String getCode() {
        return this.code;
    }

    public String getReadableText() {
        return this.readableText;
    }

    public SpecificCharacterSet getSpecificCharacterSet() {
        return SpecificCharacterSet.valueOf(this.label);
    }

    public static CharsetEncoding fromLabel(String label) {
        for (CharsetEncoding encoding : CharsetEncoding.values()) {
            if (!encoding.label.equals(label)) continue;
            return encoding;
        }
        return null;
    }

    public static CharsetEncoding fromCode(String code) {
        for (CharsetEncoding encoding : CharsetEncoding.values()) {
            if (!encoding.code.equals(code)) continue;
            return encoding;
        }
        return ISO_IR_192;
    }

    public String toString() {
        return this.readableText + " (" + this.code + ")";
    }
}

