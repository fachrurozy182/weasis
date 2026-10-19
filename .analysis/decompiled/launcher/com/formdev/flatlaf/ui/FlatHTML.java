/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.LabelView;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class FlatHTML {
    static BiConsumer<JComponent, String> testUpdateRenderer;
    private static final Set<String> absoluteSizeKeywordsSet;

    private FlatHTML() {
    }

    public static void updateRendererCSSFontBaseSize(JComponent c) {
        int insertIndex;
        String text;
        View view = (View)c.getClientProperty("html");
        if (view == null) {
            return;
        }
        Document doc = view.getDocument();
        if (!(doc instanceof HTMLDocument)) {
            return;
        }
        StyleSheet styleSheet = ((HTMLDocument)doc).getStyleSheet();
        Font font = c.getFont();
        if (styleSheet.getPointSize(7) != 36.0f || font == null || styleSheet.getPointSize(4) == (float)font.getSize()) {
            return;
        }
        if (!FlatHTML.usesAbsoluteSizeKeywordForFontSize(view)) {
            return;
        }
        if (c instanceof JLabel) {
            text = ((JLabel)c).getText();
        } else if (c instanceof AbstractButton) {
            text = ((AbstractButton)c).getText();
        } else if (c instanceof JToolTip) {
            text = ((JToolTip)c).getTipText();
        } else {
            return;
        }
        if (text == null || !BasicHTML.isHTMLString(text)) {
            return;
        }
        String style = "<style>BASE_SIZE " + font.getSize() + "</style>";
        String openTag = "";
        String closeTag = "";
        int headIndex = FlatHTML.indexOfTag(text, "head", true);
        if (headIndex >= 0) {
            insertIndex = headIndex;
        } else {
            int styleIndex = FlatHTML.indexOfTag(text, "style", false);
            if (styleIndex >= 0) {
                insertIndex = styleIndex;
            } else {
                insertIndex = "<html>".length();
                openTag = "<head>";
                closeTag = "</head>";
            }
        }
        String newText = text.substring(0, insertIndex) + openTag + style + closeTag + text.substring(insertIndex);
        BasicHTML.updateRenderer(c, newText);
        if (testUpdateRenderer != null) {
            testUpdateRenderer.accept(c, newText);
        }
    }

    private static int indexOfTag(String html, String tag, boolean endIndex) {
        int tagLength = tag.length();
        int maxLength = html.length() - tagLength - 2;
        char lastTagChar = tag.charAt(tagLength - 1);
        for (int i = "<html>".length(); i < maxLength; ++i) {
            if (html.charAt(i) != '<' || Character.toLowerCase(html.charAt(i + tagLength)) != lastTagChar) continue;
            for (int j = tagLength - 2; j >= 0 && Character.toLowerCase(html.charAt(i + 1 + j)) == tag.charAt(j); --j) {
                if (j != 0) continue;
                return endIndex ? html.indexOf(62, i + tagLength) + 1 : i;
            }
        }
        return -1;
    }

    private static boolean usesAbsoluteSizeKeywordForFontSize(View view) {
        Object fontSize;
        AttributeSet attributes = view.getAttributes();
        if (attributes != null && (fontSize = attributes.getAttribute(CSS.Attribute.FONT_SIZE)) != null && absoluteSizeKeywordsSet.contains(fontSize.toString())) {
            return true;
        }
        int viewCount = view.getViewCount();
        for (int i = 0; i < viewCount; ++i) {
            if (!FlatHTML.usesAbsoluteSizeKeywordForFontSize(view.getView(i))) continue;
            return true;
        }
        return false;
    }

    public static void updateRendererCSSForeground(View view, Color foreground) {
        Document doc = view.getDocument();
        if (!(doc instanceof HTMLDocument) || foreground == null) {
            return;
        }
        Style bodyStyle = ((HTMLDocument)doc).getStyle("body");
        if (bodyStyle == null) {
            StyleSheet styleSheet = ((HTMLDocument)doc).getStyleSheet();
            styleSheet.addRule(String.format("body { color: #%06x; }", foreground.getRGB() & 0xFFFFFF));
            FlatHTML.clearViewCaches(view);
        } else if (!foreground.equals(bodyStyle.getAttribute(StyleConstants.Foreground))) {
            bodyStyle.addAttribute(StyleConstants.Foreground, foreground);
            FlatHTML.clearViewCaches(view);
        }
    }

    private static void clearViewCaches(View view) {
        if (view instanceof LabelView) {
            ((LabelView)view).changedUpdate(null, null, null);
        }
        int viewCount = view.getViewCount();
        for (int i = 0; i < viewCount; ++i) {
            FlatHTML.clearViewCaches(view.getView(i));
        }
    }

    public static PropertyChangeListener createPropertyChangeListener(PropertyChangeListener superListener) {
        return e -> {
            if (superListener != null) {
                superListener.propertyChange(e);
            }
            FlatHTML.propertyChange(e);
        };
    }

    public static void propertyChange(PropertyChangeEvent e) {
        if ("html".equals(e.getPropertyName()) && e.getNewValue() instanceof View) {
            FlatHTML.updateRendererCSSFontBaseSize((JComponent)e.getSource());
        }
    }

    static {
        absoluteSizeKeywordsSet = new HashSet<String>(Arrays.asList("xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large"));
    }
}

