/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.DefaultTreeCheckingModel;
import eu.essilab.lablib.checkboxtree.TreeCheckingMode;
import javax.swing.tree.TreePath;

public class PropagatePreservingUncheckTreeCheckingMode
extends TreeCheckingMode {
    PropagatePreservingUncheckTreeCheckingMode(DefaultTreeCheckingModel model) {
        super(model);
    }

    @Override
    public void checkPath(TreePath path) {
        this.model.checkSubTree(path);
        TreePath[] parents = new TreePath[path.getPathCount()];
        parents[0] = path;
        boolean greyAll = false;
        block5: for (int i = 1; i < parents.length; ++i) {
            parents[i] = parents[i - 1].getParentPath();
            this.model.addToCheckedPathsSet(parents[i]);
            if (greyAll) {
                this.model.addToGreyedPathsSet(parents[i]);
                continue;
            }
            switch (this.model.getChildrenChecking(parents[i])) {
                case HALF_CHECKED: {
                    this.model.addToGreyedPathsSet(parents[i]);
                    greyAll = true;
                    continue block5;
                }
                case ALL_UNCHECKED: {
                    System.err.println("This should not happen (PropagatePreservingUncheckTreeCheckingMode)");
                    continue block5;
                }
                case ALL_CHECKED: {
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
        block5: while ((parentPath = parentPath.getParentPath()) != null) {
            switch (this.model.getChildrenChecking(parentPath)) {
                case HALF_CHECKED: {
                    this.model.addToCheckedPathsSet(parentPath);
                    this.model.addToGreyedPathsSet(parentPath);
                    continue block5;
                }
                case ALL_UNCHECKED: {
                    this.model.removeFromCheckedPathsSet(parentPath);
                    this.model.removeFromGreyedPathsSet(parentPath);
                    continue block5;
                }
                case ALL_CHECKED: {
                    System.err.println("This should not happen (PropagatePreservingUncheckTreeCheckingMode)");
                    continue block5;
                }
            }
            System.err.println("This should not happen (PropagatePreservingCheckTreeCheckingMode)");
        }
    }

    @Override
    public void updateCheckAfterChildrenInserted(TreePath parent) {
        if (this.model.isPathChecked(parent)) {
            this.model.checkSubTree(parent);
        } else {
            this.model.uncheckSubTree(parent);
        }
    }

    @Override
    public void updateCheckAfterChildrenRemoved(TreePath parent) {
        if (this.model.isPathChecked(parent) && this.model.getChildrenPath(parent).length != 0 && !this.model.pathHasChildrenWithValue(parent, true)) {
            this.uncheckPath(parent);
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

