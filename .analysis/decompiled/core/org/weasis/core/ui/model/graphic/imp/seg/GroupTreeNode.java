/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.graphic.imp.seg;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class GroupTreeNode
extends DefaultMutableTreeNode {
    private boolean selected = true;

    public GroupTreeNode(Object userObject) {
        this(userObject, true);
    }

    public GroupTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isParentVisible() {
        if (!this.selected) {
            return false;
        }
        TreeNode node = this.getParent();
        while (node instanceof GroupTreeNode) {
            GroupTreeNode group = (GroupTreeNode)node;
            if (!group.isSelected()) {
                return false;
            }
            node = node.getParent();
        }
        return true;
    }
}

