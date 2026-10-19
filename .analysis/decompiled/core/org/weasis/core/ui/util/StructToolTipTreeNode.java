/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.seg.RegionAttributes
 */
package org.weasis.core.ui.util;

import java.awt.Color;
import java.util.Objects;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.opencv.seg.RegionAttributes;

public abstract class StructToolTipTreeNode
extends DefaultMutableTreeNode {
    public StructToolTipTreeNode(SegRegion<?> userObject, boolean allowsChildren) {
        super(Objects.requireNonNull(userObject), allowsChildren);
    }

    public abstract String getToolTipText();

    public RegionAttributes getRegion() {
        return (RegionAttributes)this.getUserObject();
    }

    public String getDisplayLabel() {
        return this.getRegion().getLabel();
    }

    @Override
    public String toString() {
        return StructToolTipTreeNode.getColorBullet(this.getRegion().getColor(), this.getDisplayLabel());
    }

    protected static String getColorBullet(Color c, String label) {
        StringBuilder buf = new StringBuilder("<html><font color='rgb(");
        buf.append(c.getRed());
        buf.append(",");
        buf.append(c.getGreen());
        buf.append(",");
        buf.append(c.getBlue());
        buf.append(")'> \u2588 </font>");
        buf.append(label);
        buf.append("</html>");
        return buf.toString();
    }

    public static String buildHorizontalLutBar(Color color, int steps, String minLabel, String maxLabel) {
        int n = Math.max(2, steps);
        StringBuilder gradient = new StringBuilder();
        gradient.append("<table cellspacing='0' cellpadding='0' border='0'><tr>");
        for (int i = 0; i < n; ++i) {
            float t = (float)i / (float)(n - 1);
            int r = Math.round(255.0f * (1.0f - t) + (float)color.getRed() * t);
            int g = Math.round(255.0f * (1.0f - t) + (float)color.getGreen() * t);
            int b = Math.round(255.0f * (1.0f - t) + (float)color.getBlue() * t);
            gradient.append(String.format("<td bgcolor='#%02x%02x%02x' width='8' height='12'>&nbsp;</td>", r, g, b));
        }
        gradient.append("</tr></table>");
        if (minLabel == null && maxLabel == null) {
            return gradient.toString();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table cellspacing='2' cellpadding='0' border='0'><tr>");
        sb.append("<td valign='middle'>").append(minLabel == null ? "" : minLabel).append("</td>");
        sb.append("<td valign='middle'>").append((CharSequence)gradient).append("</td>");
        sb.append("<td valign='middle'>").append(maxLabel == null ? "" : maxLabel).append("</td>");
        sb.append("</tr></table>");
        return sb.toString();
    }

    public static String getSegItemToolTipText(TreePath curPath) {
        Object object;
        if (curPath != null && (object = curPath.getLastPathComponent()) instanceof StructToolTipTreeNode) {
            StructToolTipTreeNode treeNode = (StructToolTipTreeNode)object;
            return treeNode.getToolTipText();
        }
        return null;
    }
}

