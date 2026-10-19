/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitedTextField
extends JTextField {
    public LimitedTextField(int limit) {
        this.setDocument(new LimitedDocument(limit));
    }

    private static class LimitedDocument
    extends PlainDocument {
        private final int limit;

        LimitedDocument(int limit) {
            this.limit = limit;
        }

        @Override
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) {
                return;
            }
            if (this.getLength() + str.length() <= this.limit) {
                super.insertString(offset, str, attr);
            }
        }
    }
}

