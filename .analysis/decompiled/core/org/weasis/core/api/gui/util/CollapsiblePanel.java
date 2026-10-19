/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatLineBorder
 *  com.formdev.flatlaf.ui.FlatUIUtils
 */
package org.weasis.core.api.gui.util;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.util.FontItem;

public class CollapsiblePanel
extends JPanel {
    private final JComponent content;
    private final JButton header;
    private final String persistenceKey;
    private boolean expanded;

    public CollapsiblePanel(String title, JComponent content, boolean defaultExpanded) {
        this(title, content, defaultExpanded, null);
    }

    public CollapsiblePanel(String title, JComponent content, boolean defaultExpanded, String persistenceKey) {
        super(new BorderLayout());
        this.content = Objects.requireNonNull(content);
        this.persistenceKey = persistenceKey;
        this.expanded = this.restoreState(defaultExpanded);
        this.header = new JButton(title);
        this.header.setFont(FontItem.DEFAULT_SEMIBOLD.getFont());
        this.header.setHorizontalAlignment(10);
        this.header.setHorizontalTextPosition(11);
        this.header.setIconTextGap(GuiUtils.getScaleLength(6));
        this.header.setFocusable(false);
        this.header.putClientProperty("JButton.buttonType", "borderless");
        this.header.addActionListener(e -> this.setExpanded(!this.expanded));
        this.setAlignmentX(0.0f);
        this.setBorder(CollapsiblePanel.buildSectionBorder());
        this.add((Component)this.header, "North");
        this.add((Component)content, "Center");
        this.applyState();
    }

    private static Border buildSectionBorder() {
        Color line = FlatUIUtils.getUIColor((String)"Component.borderColor", (Color)Color.GRAY);
        FlatLineBorder card = new FlatLineBorder(new Insets(2, 2, 2, 2), line, 1.0f, 8);
        return new CompoundBorder(GuiUtils.getEmptyBorder(6, 3, 2, 3), (Border)card);
    }

    private boolean restoreState(boolean defaultExpanded) {
        if (this.persistenceKey == null) {
            return defaultExpanded;
        }
        return GuiUtils.getUICore().getLocalPersistence().getBooleanProperty(this.persistenceKey, defaultExpanded);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = this.getPreferredSize();
        dim.width = Integer.MAX_VALUE;
        return dim;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public JComponent getContent() {
        return this.content;
    }

    public void setExpanded(boolean expand) {
        if (expand == this.expanded) {
            return;
        }
        this.expanded = expand;
        if (this.persistenceKey != null) {
            GuiUtils.getUICore().getLocalPersistence().putBooleanProperty(this.persistenceKey, expand);
        }
        this.applyState();
        this.revalidate();
        this.repaint();
    }

    private void applyState() {
        this.content.setVisible(this.expanded);
        this.header.setIcon(CollapsiblePanel.disclosureIcon(this.expanded));
    }

    private static Icon disclosureIcon(boolean expanded) {
        return UIManager.getIcon(expanded ? "Tree.expandedIcon" : "Tree.collapsedIcon");
    }
}

