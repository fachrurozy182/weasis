/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.TreeCheckingListener;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import javax.swing.tree.TreePath;

public class NullTreeCheckingModel
implements TreeCheckingModel {
    private static final NullTreeCheckingModel singleton = new NullTreeCheckingModel();

    private NullTreeCheckingModel() {
    }

    public static NullTreeCheckingModel getInstance() {
        return singleton;
    }

    @Override
    public void addCheckingPath(TreePath path) {
    }

    @Override
    public void addCheckingPaths(TreePath[] paths) {
    }

    @Override
    public void addTreeCheckingListener(TreeCheckingListener tcl) {
    }

    @Override
    public void clearChecking() {
    }

    @Override
    public TreeCheckingModel.CheckingMode getCheckingMode() {
        return null;
    }

    @Override
    public TreePath[] getCheckingPaths() {
        return null;
    }

    @Override
    public TreePath[] getCheckingRoots() {
        return null;
    }

    @Override
    public TreePath[] getGreyingPaths() {
        return null;
    }

    @Override
    public boolean isPathChecked(TreePath path) {
        return false;
    }

    @Override
    public boolean isPathEnabled(TreePath path) {
        return true;
    }

    @Override
    public boolean isPathGreyed(TreePath path) {
        return false;
    }

    @Override
    public void removeCheckingPath(TreePath path) {
    }

    @Override
    public void removeCheckingPaths(TreePath[] paths) {
    }

    @Override
    public void removeTreeCheckingListener(TreeCheckingListener tcl) {
    }

    @Override
    public void setCheckingMode(TreeCheckingModel.CheckingMode mode) {
    }

    @Override
    public void setCheckingPath(TreePath path) {
    }

    @Override
    public void setCheckingPaths(TreePath[] paths) {
    }

    @Override
    public void setPathEnabled(TreePath path, boolean enable) {
    }

    @Override
    public void setPathsEnabled(TreePath[] paths, boolean enable) {
    }

    @Override
    public void toggleCheckingPath(TreePath pathForRow) {
    }
}

