/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import eu.essilab.lablib.checkboxtree.CheckboxTree;
import eu.essilab.lablib.checkboxtree.DefaultCheckboxTreeCellRenderer;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.weasis.core.ui.util.SegRegionCellRenderer;

public class TreeBuilder {
    private TreeBuilder() {
    }

    public static DefaultCheckboxTreeCellRenderer buildNoIconCheckboxTreeCellRenderer() {
        DefaultCheckboxTreeCellRenderer renderer = new DefaultCheckboxTreeCellRenderer();
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setLeafIcon(null);
        return renderer;
    }

    public static SegRegionCellRenderer buildSegRegionCellRenderer() {
        return new SegRegionCellRenderer();
    }

    public static void expandTree(JTree tree, DefaultMutableTreeNode start) {
        TreeBuilder.expandTree(tree, start, 5);
    }

    public static void expandTree(JTree tree, DefaultMutableTreeNode start, int maxDeep) {
        if (maxDeep > 1) {
            Enumeration<TreeNode> children = start.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode dtm;
                TreeNode child = children.nextElement();
                if (!(child instanceof DefaultMutableTreeNode) || (dtm = (DefaultMutableTreeNode)child).isLeaf()) continue;
                TreePath tp = new TreePath(dtm.getPath());
                tree.expandPath(tp);
                TreeBuilder.expandTree(tree, dtm, maxDeep - 1);
            }
        }
    }

    public static void setPathSelection(CheckboxTree tree, TreePath path, boolean selected) {
        if (tree == null || path == null) {
            return;
        }
        if (selected) {
            tree.addCheckingPath(path);
        } else {
            tree.removeCheckingPath(path);
        }
    }
}

