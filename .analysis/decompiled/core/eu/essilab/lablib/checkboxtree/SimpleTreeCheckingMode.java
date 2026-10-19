/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.DefaultTreeCheckingModel;
import eu.essilab.lablib.checkboxtree.TreeCheckingMode;
import javax.swing.tree.TreePath;

public class SimpleTreeCheckingMode
extends TreeCheckingMode {
    SimpleTreeCheckingMode(DefaultTreeCheckingModel model) {
        super(model);
    }

    @Override
    public void checkPath(TreePath path) {
        this.model.addToCheckedPathsSet(path);
        this.model.updatePathGreyness(path);
        this.model.updateAncestorsGreyness(path);
    }

    @Override
    public void uncheckPath(TreePath path) {
        this.model.removeFromCheckedPathsSet(path);
        this.model.updatePathGreyness(path);
        this.model.updateAncestorsGreyness(path);
    }

    @Override
    public void updateCheckAfterChildrenInserted(TreePath parent) {
        this.model.updatePathGreyness(parent);
        this.model.updateAncestorsGreyness(parent);
    }

    @Override
    public void updateCheckAfterChildrenRemoved(TreePath parent) {
        this.model.updatePathGreyness(parent);
        this.model.updateAncestorsGreyness(parent);
    }

    @Override
    public void updateCheckAfterStructureChanged(TreePath parent) {
        this.model.updatePathGreyness(parent);
        this.model.updateAncestorsGreyness(parent);
    }
}

