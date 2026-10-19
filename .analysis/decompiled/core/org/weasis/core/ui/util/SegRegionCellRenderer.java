/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import eu.essilab.lablib.checkboxtree.DefaultCheckboxTreeCellRenderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.util.SegRegionTree;
import org.weasis.core.ui.util.StructToolTipTreeNode;

public class SegRegionCellRenderer
extends DefaultCheckboxTreeCellRenderer {
    private final SwatchIcon swatch = new SwatchIcon();

    public SegRegionCellRenderer() {
        this.setOpenIcon(null);
        this.setClosedIcon(null);
        this.setLeafIcon(null);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        StructToolTipTreeNode node;
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value instanceof StructToolTipTreeNode && (node = (StructToolTipTreeNode)value).getRegion().getColor() != null) {
            SegRegionTree segTree;
            this.swatch.color = new Color(node.getRegion().getColor().getRGB());
            this.swatch.hovered = tree instanceof SegRegionTree && (segTree = (SegRegionTree)tree).isSwatchHovered(row);
            this.label.setIcon(this.swatch);
            this.label.setText(node.getDisplayLabel());
        }
        return this;
    }

    public Rectangle getSwatchBounds(Rectangle cellBounds) {
        Icon icon = this.label.getIcon();
        if (icon == null || cellBounds == null) {
            return null;
        }
        int x = cellBounds.x + this.checkBox.getPreferredSize().width + this.label.getInsets().left;
        return new Rectangle(x, cellBounds.y, icon.getIconWidth(), cellBounds.height);
    }

    private static final class SwatchIcon
    implements Icon {
        private final int chipSize = GuiUtils.getScaleLength(12);
        private final int margin = GuiUtils.getScaleLength(3);
        private final int arc = GuiUtils.getScaleLength(4);
        private Color color = Color.GRAY;
        private boolean hovered;

        private SwatchIcon() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.setColor(this.color);
                g2.fillRoundRect(x + this.margin, y + this.margin, this.chipSize, this.chipSize, this.arc, this.arc);
                if (this.hovered) {
                    int size = this.getIconWidth();
                    g2.setColor(SwatchIcon.focusColor());
                    g2.setStroke(new BasicStroke(GuiUtils.getScaleLength(2)));
                    g2.drawRoundRect(x + 1, y + 1, size - 3, size - 3, this.arc + this.margin, this.arc + this.margin);
                }
            }
            finally {
                g2.dispose();
            }
        }

        private static Color focusColor() {
            Color c = UIManager.getColor("Component.focusedBorderColor");
            if (c == null) {
                c = UIManager.getColor("Tree.selectionBackground");
            }
            return c == null ? Color.GRAY : c;
        }

        @Override
        public int getIconWidth() {
            return this.chipSize + 2 * this.margin;
        }

        @Override
        public int getIconHeight() {
            return this.getIconWidth();
        }
    }
}

