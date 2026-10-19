/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.TreeCheckingListener;
import javax.swing.tree.TreePath;

public interface TreeCheckingModel {
    public void addCheckingPath(TreePath var1);

    public void addCheckingPaths(TreePath[] var1);

    public void addTreeCheckingListener(TreeCheckingListener var1);

    public void clearChecking();

    public CheckingMode getCheckingMode();

    public TreePath[] getCheckingPaths();

    public TreePath[] getCheckingRoots();

    public TreePath[] getGreyingPaths();

    public boolean isPathChecked(TreePath var1);

    public boolean isPathEnabled(TreePath var1);

    public boolean isPathGreyed(TreePath var1);

    public void removeCheckingPath(TreePath var1);

    public void removeCheckingPaths(TreePath[] var1);

    public void removeTreeCheckingListener(TreeCheckingListener var1);

    public void setCheckingMode(CheckingMode var1);

    public void setCheckingPath(TreePath var1);

    public void setCheckingPaths(TreePath[] var1);

    public void setPathEnabled(TreePath var1, boolean var2);

    public void setPathsEnabled(TreePath[] var1, boolean var2);

    public void toggleCheckingPath(TreePath var1);

    public static enum CheckingMode {
        PROPAGATE,
        PROPAGATE_PRESERVING_CHECK,
        PROPAGATE_PRESERVING_UNCHECK,
        PROPAGATE_UP_UNCHECK,
        SIMPLE,
        SINGLE;

    }
}

