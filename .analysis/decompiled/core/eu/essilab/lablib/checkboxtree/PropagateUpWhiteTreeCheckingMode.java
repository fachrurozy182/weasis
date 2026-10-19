/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.DefaultTreeCheckingModel;
import eu.essilab.lablib.checkboxtree.TreeCheckingMode;
import javax.swing.tree.TreePath;

public class PropagateUpWhiteTreeCheckingMode
extends TreeCheckingMode {
    PropagateUpWhiteTreeCheckingMode(DefaultTreeCheckingModel model) {
        super(model);
    }

    @Override
    public void checkPath(TreePath path) {
        this.model.checkSubTree(path);
        TreePath[] parents = new TreePath[path.getPathCount()];
        parents[0] = path;
        TreePath parentPath = path;
        while ((parentPath = parentPath.getParentPath()) != null) {
            this.model.updatePathGreyness(parentPath);
        }
    }

    @Override
    public void uncheckPath(TreePath path) {
        this.model.uncheckSubTree(path);
        TreePath parentPath = path;
        while ((parentPath = parentPath.getParentPath()) != null) {
            this.model.removeFromCheckedPathsSet(parentPath);
            this.model.updatePathGreyness(parentPath);
        }
    }

    @Override
    public void updateCheckAfterChildrenInserted(TreePath parent) {
        if (this.model.isPathChecked(parent)) {
            this.checkPath(parent);
        } else {
            this.uncheckPath(parent);
        }
    }

    @Override
    public void updateCheckAfterChildrenRemoved(TreePath parent) {
        if (!this.model.isPathChecked(parent) && this.model.getChildrenPath(parent).length != 0 && !this.model.pathHasChildrenWithValue(parent, false)) {
            this.checkPath(parent);
        }
        this.model.updatePathGreyness(parent);
        this.model.updateAncestorsGreyness(parent);
    }

    @Override
    public void updateCheckAfterStructureChanged(TreePath parent) {
        if (this.model.isPathChecked(parent)) {
            this.checkPath(parent);
        } else {
            this.uncheckPath(parent);
        }
    }
}

