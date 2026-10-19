/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.DefaultTreeCheckingModel;
import eu.essilab.lablib.checkboxtree.TreeCheckingMode;
import javax.swing.tree.TreePath;

public class PropagateTreeCheckingMode
extends TreeCheckingMode {
    PropagateTreeCheckingMode(DefaultTreeCheckingModel model) {
        super(model);
    }

    @Override
    public void checkPath(TreePath path) {
        this.model.checkSubTree(path);
        this.model.updatePathGreyness(path);
        this.model.updateAncestorsGreyness(path);
    }

    @Override
    public void uncheckPath(TreePath path) {
        this.model.uncheckSubTree(path);
        this.model.updatePathGreyness(path);
        this.model.updateAncestorsGreyness(path);
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
        this.model.updatePathGreyness(parent);
        this.model.updateAncestorsGreyness(parent);
    }

    @Override
    public void updateCheckAfterStructureChanged(TreePath parent) {
        if (this.model.isPathChecked(parent)) {
            this.model.checkSubTree(parent);
        } else {
            this.model.uncheckSubTree(parent);
        }
    }
}

