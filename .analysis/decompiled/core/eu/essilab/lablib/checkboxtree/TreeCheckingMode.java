/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.DefaultTreeCheckingModel;
import javax.swing.tree.TreePath;

public abstract class TreeCheckingMode {
    protected DefaultTreeCheckingModel model;

    TreeCheckingMode(DefaultTreeCheckingModel model) {
        this.model = model;
    }

    public abstract void checkPath(TreePath var1);

    public abstract void uncheckPath(TreePath var1);

    public abstract void updateCheckAfterChildrenInserted(TreePath var1);

    public abstract void updateCheckAfterChildrenRemoved(TreePath var1);

    public abstract void updateCheckAfterStructureChanged(TreePath var1);
}

