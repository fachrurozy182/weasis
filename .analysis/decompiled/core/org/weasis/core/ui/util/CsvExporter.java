/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import org.weasis.core.util.StringUtil;

public class CsvExporter {
    public static final char separator = ',';
    public static final char quote = '\"';
    private final StringBuilder sb = new StringBuilder();

    public void addQuotedNameAndSeparator(String name) {
        this.addQuotedName(name);
        this.addSeparator();
    }

    public void addQuotedName(String name) {
        if (StringUtil.hasText((CharSequence)name)) {
            this.sb.append('\"');
            this.sb.append(name);
            this.sb.append('\"');
        }
    }

    public void adName(String name) {
        if (StringUtil.hasText((CharSequence)name)) {
            this.sb.append(name);
        }
    }

    public StringBuilder getBuilder() {
        return this.sb;
    }

    public void addSeparator() {
        this.sb.append(',');
    }

    public void addEndOfLine() {
        this.sb.append(System.lineSeparator());
    }

    public void copyToClipboard() {
        StringSelection stringSelection = new StringSelection(this.sb.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}

