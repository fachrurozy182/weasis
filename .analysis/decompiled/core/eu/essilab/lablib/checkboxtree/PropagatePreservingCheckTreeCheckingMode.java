/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.DefaultTreeCheckingModel;
import eu.essilab.lablib.checkboxtree.TreeCheckingMode;
import javax.swing.tree.TreePath;

public class PropagatePreservingCheckTreeCheckingMode
extends TreeCheckingMode {
    PropagatePreservingCheckTreeCheckingMode(DefaultTreeCheckingModel model) {
        super(model);
    }

    @Override
    public void checkPath(TreePath path) {
        this.model.checkSubTree(path);
        TreePath[] parents = new TreePath[path.getPathCount()];
        parents[0] = path;
        boolean uncheckAll = false;
        boolean greyAll = false;
        block5: for (int i = 1; i < parents.length; ++i) {
            parents[i] = parents[i - 1].getParentPath();
            if (uncheckAll) {
                this.model.removeFromCheckedPathsSet(parents[i]);
                if (greyAll) {
                    this.model.addToGreyedPathsSet(parents[i]);
                    continue;
                }
                if (this.model.pathHasUncheckedChildren(parents[i])) {
                    this.model.addToGreyedPathsSet(parents[i]);
                    greyAll = true;
                    continue;
                }
                this.model.removeFromGreyedPathsSet(parents[i]);
                continue;
            }
            switch (this.model.getChildrenChecking(parents[i])) {
                case HALF_CHECKED: {
                    this.model.removeFromCheckedPathsSet(parents[i]);
                    this.model.addToGreyedPathsSet(parents[i]);
                    uncheckAll = true;
                    greyAll = true;
                    continue block5;
                }
                case ALL_UNCHECKED: {
                    this.model.removeFromCheckedPathsSet(parents[i]);
                    this.model.removeFromGreyedPathsSet(parents[i]);
                    uncheckAll = true;
                    continue block5;
                }
                case ALL_CHECKED: {
                    this.model.addToCheckedPathsSet(parents[i]);
                    this.model.removeFromGreyedPathsSet(parents[i]);
                    continue block5;
                }
                default: {
                    System.err.println("This should not happen (PropagatePreservingCheckTreeCheckingMode)");
                }
            }
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

