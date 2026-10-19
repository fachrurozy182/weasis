/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiUtils$IconColor
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.tag;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.tag.AbstractTagSearchPanel;
import org.weasis.dicom.explorer.tag.DicomFieldsView;

public class TagSearchDocumentPanel
extends AbstractTagSearchPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagSearchDocumentPanel.class);
    private static final Highlighter.HighlightPainter searchHighlightPainter = new DicomFieldsView.SearchHighlightPainter(GuiUtils.IconColor.ACTIONS_YELLOW.color);
    private static final Highlighter.HighlightPainter searchResultHighlightPainter = new DicomFieldsView.SearchHighlightPainter(GuiUtils.IconColor.ACTIONS_BLUE.color);
    private final List<Integer> searchPositions = new ArrayList<Integer>();
    private final JTextComponent textComponent;
    private int currentSearchIndex = 0;
    private String currentSearchPattern;

    public TagSearchDocumentPanel(JTextComponent textComponent) {
        this.textComponent = textComponent;
        textComponent.addKeyListener(this);
        textComponent.setFocusable(true);
    }

    @Override
    protected void previous() {
        if (!this.searchPositions.isEmpty()) {
            this.currentSearchIndex = this.currentSearchIndex <= 0 ? this.searchPositions.size() - 1 : this.currentSearchIndex - 1;
            this.showCurrentSearch(this.currentSearchPattern);
        }
    }

    @Override
    protected void next() {
        if (!this.searchPositions.isEmpty()) {
            this.currentSearchIndex = this.currentSearchIndex >= this.searchPositions.size() - 1 ? 0 : this.currentSearchIndex + 1;
            this.showCurrentSearch(this.currentSearchPattern);
        }
    }

    public void highlight(String pattern) {
        this.removeHighlights(this.textComponent);
        this.searchPositions.clear();
        if (StringUtil.hasText((CharSequence)pattern)) {
            try {
                Highlighter highlighter = this.textComponent.getHighlighter();
                Document doc = this.textComponent.getDocument();
                String text = doc.getText(0, doc.getLength()).toUpperCase();
                String patternUp = pattern.toUpperCase();
                int pos = 0;
                while ((pos = text.indexOf(patternUp, pos)) >= 0) {
                    if (this.searchPositions.isEmpty()) {
                        highlighter.addHighlight(pos, pos + patternUp.length(), searchHighlightPainter);
                    } else {
                        highlighter.addHighlight(pos, pos + patternUp.length(), searchResultHighlightPainter);
                    }
                    this.searchPositions.add(pos);
                    pos += patternUp.length();
                }
            }
            catch (BadLocationException e) {
                LOGGER.error("Highlight result of search", (Throwable)e);
            }
        }
    }

    public void removeHighlights(JTextComponent textComponent) {
        Highlighter highlighter = textComponent.getHighlighter();
        for (Highlighter.Highlight highlight : highlighter.getHighlights()) {
            if (!(highlight.getPainter() instanceof DicomFieldsView.SearchHighlightPainter)) continue;
            highlighter.removeHighlight(highlight);
        }
    }

    public void showCurrentSearch(String pattern) {
        if (!this.searchPositions.isEmpty() && StringUtil.hasText((CharSequence)pattern)) {
            this.removeHighlights(this.textComponent);
            try {
                if (this.currentSearchIndex < 0 || this.currentSearchIndex >= this.searchPositions.size()) {
                    this.currentSearchIndex = 0;
                }
                int curPos = this.searchPositions.get(this.currentSearchIndex);
                Highlighter highlighter = this.textComponent.getHighlighter();
                for (Integer pos : this.searchPositions) {
                    if (pos == curPos) {
                        highlighter.addHighlight(pos, pos + pattern.length(), searchHighlightPainter);
                        continue;
                    }
                    highlighter.addHighlight(pos, pos + pattern.length(), searchResultHighlightPainter);
                }
                this.textComponent.scrollRectToVisible(this.textComponent.modelToView(curPos));
            }
            catch (BadLocationException e) {
                LOGGER.error("Highlight result of search", (Throwable)e);
            }
        }
    }

    @Override
    protected void filter() {
        this.currentSearchPattern = this.textFieldSearch.getText().trim();
        this.highlight(this.currentSearchPattern);
    }
}

