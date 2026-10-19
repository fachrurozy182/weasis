/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.PropagatePreservingCheckTreeCheckingMode;
import eu.essilab.lablib.checkboxtree.PropagatePreservingUncheckTreeCheckingMode;
import eu.essilab.lablib.checkboxtree.PropagateTreeCheckingMode;
import eu.essilab.lablib.checkboxtree.PropagateUpWhiteTreeCheckingMode;
import eu.essilab.lablib.checkboxtree.SimpleTreeCheckingMode;
import eu.essilab.lablib.checkboxtree.SingleTreeCheckingMode;
import eu.essilab.lablib.checkboxtree.TreeCheckingEvent;
import eu.essilab.lablib.checkboxtree.TreeCheckingListener;
import eu.essilab.lablib.checkboxtree.TreeCheckingMode;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class DefaultTreeCheckingModel
implements TreeCheckingModel {
    private static final TreeModel NULL_TREE_MODEL = new TreeModel(){

        @Override
        public void addTreeModelListener(TreeModelListener l) {
        }

        @Override
        public Object getChild(Object parent, int index) {
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            return 0;
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return 0;
        }

        @Override
        public Object getRoot() {
            return null;
        }

        @Override
        public boolean isLeaf(Object node) {
            return false;
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }
    };
    private HashSet<TreePath> checkedPathsSet;
    protected TreeCheckingMode checkingMode;
    private HashSet<TreePath> disabledPathsSet;
    private HashSet<TreePath> greyedPathsSet;
    protected EventListenerList listenerList = new EventListenerList();
    protected TreeModel model;
    private PropagateCheckingListener propagateCheckingListener;

    public DefaultTreeCheckingModel(TreeModel model) {
        this.model = model == null ? NULL_TREE_MODEL : model;
        this.checkedPathsSet = new HashSet();
        this.greyedPathsSet = new HashSet();
        this.disabledPathsSet = new HashSet();
        this.propagateCheckingListener = new PropagateCheckingListener();
        this.setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE);
    }

    @Override
    public void addCheckingPath(TreePath path) {
        this.checkingMode.checkPath(path);
        TreeCheckingEvent event = new TreeCheckingEvent(this, path, true);
        this.fireValueChanged(event);
    }

    @Override
    public void addCheckingPaths(TreePath[] paths) {
        for (TreePath path : paths) {
            this.addCheckingPath(path);
        }
    }

    void addToCheckedPathsSet(TreePath path) {
        this.checkedPathsSet.add(path);
    }

    void addToGreyedPathsSet(TreePath path) {
        this.greyedPathsSet.add(path);
    }

    @Override
    public void addTreeCheckingListener(TreeCheckingListener x) {
        this.listenerList.add(TreeCheckingListener.class, x);
    }

    public void checkSubTree(TreePath path) {
        this.addToCheckedPathsSet(path);
        this.removeFromGreyedPathsSet(path);
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        for (int childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            this.checkSubTree(childPath);
        }
    }

    @Override
    public void clearChecking() {
        this.checkedPathsSet.clear();
        this.greyedPathsSet.clear();
        if (this.model != null && this.model.getRoot() != null) {
            this.fireValueChanged(new TreeCheckingEvent(this, new TreePath(this.model.getRoot()), false));
        }
    }

    protected void fireValueChanged(TreeCheckingEvent e) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != TreeCheckingListener.class) continue;
            ((TreeCheckingListener)listeners[i + 1]).valueChanged(e);
        }
    }

    @Override
    public TreeCheckingModel.CheckingMode getCheckingMode() {
        if (this.checkingMode instanceof SimpleTreeCheckingMode) {
            return TreeCheckingModel.CheckingMode.SIMPLE;
        }
        if (this.checkingMode instanceof PropagateTreeCheckingMode) {
            return TreeCheckingModel.CheckingMode.PROPAGATE;
        }
        if (this.checkingMode instanceof PropagatePreservingCheckTreeCheckingMode) {
            return TreeCheckingModel.CheckingMode.PROPAGATE_PRESERVING_CHECK;
        }
        if (this.checkingMode instanceof PropagatePreservingUncheckTreeCheckingMode) {
            return TreeCheckingModel.CheckingMode.PROPAGATE_PRESERVING_UNCHECK;
        }
        if (this.checkingMode instanceof PropagateUpWhiteTreeCheckingMode) {
            return TreeCheckingModel.CheckingMode.PROPAGATE_UP_UNCHECK;
        }
        return null;
    }

    @Override
    public TreePath[] getCheckingPaths() {
        return this.checkedPathsSet.toArray(new TreePath[this.checkedPathsSet.size()]);
    }

    @Override
    public TreePath[] getCheckingRoots() {
        TreePath[] retVal = new TreePath[]{};
        if (this.model.getRoot() != null) {
            Vector<TreePath> roots = this.getCheckingRoots(new TreePath(this.model.getRoot()));
            retVal = roots.toArray(retVal);
        }
        return retVal;
    }

    private Vector<TreePath> getCheckingRoots(TreePath path) {
        Object node = path.getLastPathComponent();
        Vector<TreePath> roots = new Vector<TreePath>();
        if (!this.isPathGreyed(path)) {
            if (this.isPathChecked(path)) {
                roots.add(path);
            }
            return roots;
        }
        int childrenNumber = this.model.getChildCount(node);
        for (int childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            roots.addAll(this.getCheckingRoots(childPath));
        }
        return roots;
    }

    public ChildrenChecking getChildrenChecking(TreePath path) {
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        boolean someChecked = false;
        boolean someUnchecked = false;
        for (int childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            if (this.isPathGreyed(childPath)) {
                return ChildrenChecking.HALF_CHECKED;
            }
            if (this.isPathChecked(childPath)) {
                if (someUnchecked) {
                    return ChildrenChecking.HALF_CHECKED;
                }
                someChecked = true;
                continue;
            }
            if (someChecked) {
                return ChildrenChecking.HALF_CHECKED;
            }
            someUnchecked = true;
        }
        if (someChecked) {
            return ChildrenChecking.ALL_CHECKED;
        }
        if (someUnchecked) {
            return ChildrenChecking.ALL_UNCHECKED;
        }
        return ChildrenChecking.NO_CHILDREN;
    }

    protected TreePath[] getChildrenPath(TreePath path) {
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        TreePath[] childrenPath = new TreePath[childrenNumber];
        for (int childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            childrenPath[childIndex] = path.pathByAddingChild(this.model.getChild(node, childIndex));
        }
        return childrenPath;
    }

    @Override
    public TreePath[] getGreyingPaths() {
        return this.greyedPathsSet.toArray(new TreePath[this.greyedPathsSet.size()]);
    }

    public boolean hasDifferentChildren(TreePath path) {
        return this.pathHasChildrenWithValue(path, !this.isPathChecked(path));
    }

    @Override
    public boolean isPathChecked(TreePath path) {
        return this.checkedPathsSet.contains(path);
    }

    @Override
    public boolean isPathEnabled(TreePath path) {
        return !this.disabledPathsSet.contains(path);
    }

    @Override
    public boolean isPathGreyed(TreePath path) {
        return this.greyedPathsSet.contains(path);
    }

    public boolean pathHasCheckedChildren(TreePath path) {
        return this.pathHasChildrenWithValue(path, true);
    }

    protected boolean pathHasChildrenWithValue(TreePath path, boolean value) {
        TreePath childPath;
        int childIndex;
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        for (childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            if (this.isPathChecked(childPath) != value) continue;
            return true;
        }
        for (childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            if (!this.pathHasChildrenWithValue(childPath, value)) continue;
            return true;
        }
        return false;
    }

    public boolean pathHasUncheckedChildren(TreePath path) {
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        for (int childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            if (!(this.isPathGreyed(childPath) | !this.isPathChecked(childPath))) continue;
            return true;
        }
        return false;
    }

    @Override
    public void removeCheckingPath(TreePath path) {
        this.checkingMode.uncheckPath(path);
        TreeCheckingEvent event = new TreeCheckingEvent(this, path, false);
        this.fireValueChanged(event);
    }

    @Override
    public void removeCheckingPaths(TreePath[] paths) {
        for (TreePath path : paths) {
            this.removeCheckingPath(path);
        }
    }

    void removeFromCheckedPathsSet(TreePath path) {
        this.checkedPathsSet.remove(path);
    }

    void removeFromGreyedPathsSet(TreePath path) {
        this.greyedPathsSet.remove(path);
    }

    @Override
    public void removeTreeCheckingListener(TreeCheckingListener x) {
        this.listenerList.remove(TreeCheckingListener.class, x);
    }

    @Override
    public void setCheckingMode(TreeCheckingModel.CheckingMode mode) {
        switch (mode) {
            case SIMPLE: {
                this.checkingMode = new SimpleTreeCheckingMode(this);
                break;
            }
            case SINGLE: {
                this.checkingMode = new SingleTreeCheckingMode(this);
                break;
            }
            case PROPAGATE: {
                this.checkingMode = new PropagateTreeCheckingMode(this);
                break;
            }
            case PROPAGATE_PRESERVING_CHECK: {
                this.checkingMode = new PropagatePreservingCheckTreeCheckingMode(this);
                break;
            }
            case PROPAGATE_PRESERVING_UNCHECK: {
                this.checkingMode = new PropagatePreservingUncheckTreeCheckingMode(this);
                break;
            }
            case PROPAGATE_UP_UNCHECK: {
                this.checkingMode = new PropagateUpWhiteTreeCheckingMode(this);
                break;
            }
        }
    }

    public void setCheckingMode(TreeCheckingMode mode) {
        this.checkingMode = mode;
    }

    @Override
    public void setCheckingPath(TreePath path) {
        this.clearChecking();
        this.addCheckingPath(path);
    }

    @Override
    public void setCheckingPaths(TreePath[] paths) {
        this.clearChecking();
        for (TreePath path : paths) {
            this.addCheckingPath(path);
        }
    }

    @Override
    public void setPathEnabled(TreePath path, boolean enable) {
        if (enable) {
            this.disabledPathsSet.remove(path);
        } else {
            this.disabledPathsSet.add(path);
        }
    }

    @Override
    public void setPathsEnabled(TreePath[] paths, boolean enable) {
        for (TreePath path : paths) {
            this.setPathEnabled(path, enable);
        }
    }

    public void setTreeModel(TreeModel newModel) {
        TreeModel oldModel = this.model;
        if (oldModel != null) {
            oldModel.removeTreeModelListener(this.propagateCheckingListener);
        }
        this.model = newModel;
        if (newModel != null) {
            newModel.addTreeModelListener(this.propagateCheckingListener);
        }
        this.clearChecking();
    }

    @Override
    public void toggleCheckingPath(TreePath path) {
        if (!this.isPathEnabled(path)) {
            return;
        }
        if (this.isPathChecked(path)) {
            this.removeCheckingPath(path);
        } else {
            this.addCheckingPath(path);
        }
    }

    public String toString() {
        if (this.model.getRoot() != null) {
            return this.toString(new TreePath(this.model.getRoot()));
        }
        return null;
    }

    private String toString(TreePath path) {
        String checkString = "n";
        String greyString = "n";
        String enableString = "n";
        if (this.isPathChecked(path)) {
            checkString = "y";
        }
        if (this.isPathEnabled(path)) {
            enableString = "y";
        }
        if (this.isPathGreyed(path)) {
            greyString = "y";
        }
        String description = "Path checked: " + checkString + " greyed: " + greyString + " enabled: " + enableString + " Name: " + path.toString() + "\n";
        for (TreePath childPath : this.getChildrenPath(path)) {
            description = description + this.toString(childPath);
        }
        return description;
    }

    public void uncheckSubTree(TreePath path) {
        this.removeFromCheckedPathsSet(path);
        this.removeFromGreyedPathsSet(path);
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        for (int childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            this.uncheckSubTree(childPath);
        }
    }

    public void ungreySubTree(TreePath path) {
        this.removeFromGreyedPathsSet(path);
        for (TreePath childPath : this.getChildrenPath(path)) {
            this.ungreySubTree(childPath);
        }
    }

    public void updateAncestorsGreyness(TreePath path) {
        TreePath[] parents = new TreePath[path.getPathCount()];
        parents[0] = path;
        boolean greyAll = this.isPathGreyed(path);
        for (int i = 1; i < parents.length; ++i) {
            parents[i] = parents[i - 1].getParentPath();
            if (greyAll) {
                this.addToGreyedPathsSet(parents[i]);
                continue;
            }
            this.updatePathGreyness(parents[i]);
            greyAll = this.isPathGreyed(parents[i]);
        }
    }

    public void updateCheckingConsistency() {
        if (this.model.getRoot() != null) {
            this.updateSubTreeCheckingConsistency(new TreePath(this.model.getRoot()));
        }
    }

    protected void updatePathGreyness(TreePath ancestor) {
        boolean value = this.isPathChecked(ancestor);
        Object ancestorNode = ancestor.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(ancestorNode);
        for (int childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            Object childNode = this.model.getChild(ancestorNode, childIndex);
            TreePath childPath = ancestor.pathByAddingChild(childNode);
            if (this.isPathGreyed(childPath)) {
                this.addToGreyedPathsSet(ancestor);
                return;
            }
            if (this.isPathChecked(childPath) == value) continue;
            this.addToGreyedPathsSet(ancestor);
            return;
        }
        this.removeFromGreyedPathsSet(ancestor);
    }

    public void updateSubTreeCheckingConsistency(TreePath path) {
        if (this.isPathGreyed(path)) {
            for (TreePath childPath : this.getChildrenPath(path)) {
                this.updateSubTreeCheckingConsistency(childPath);
            }
        } else {
            if (this.isPathChecked(path)) {
                this.checkSubTree(path);
            } else {
                this.uncheckSubTree(path);
            }
            return;
        }
        this.updatePathGreyness(path);
    }

    public void updateSubTreeGreyness(TreePath path) {
        if (this.pathHasChildrenWithValue(path, !this.isPathChecked(path))) {
            this.addToGreyedPathsSet(path);
        } else {
            this.removeFromGreyedPathsSet(path);
        }
        if (this.isPathGreyed(path)) {
            for (TreePath childPath : this.getChildrenPath(path)) {
                this.updateSubTreeGreyness(childPath);
            }
            return;
        }
        this.ungreySubTree(path);
    }

    public void updateTreeGreyness() {
        if (this.model.getRoot() != null) {
            this.updateSubTreeGreyness(new TreePath(this.model.getRoot()));
        }
    }

    private class PropagateCheckingListener
    implements TreeModelListener {
        private PropagateCheckingListener() {
        }

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            DefaultTreeCheckingModel.this.updateSubTreeGreyness(path);
            DefaultTreeCheckingModel.this.updateAncestorsGreyness(path);
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            DefaultTreeCheckingModel.this.checkingMode.updateCheckAfterChildrenInserted(path);
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            DefaultTreeCheckingModel.this.checkingMode.updateCheckAfterChildrenRemoved(path);
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            DefaultTreeCheckingModel.this.checkingMode.updateCheckAfterStructureChanged(path);
        }
    }

    public static enum ChildrenChecking {
        ALL_CHECKED,
        ALL_UNCHECKED,
        HALF_CHECKED,
        NO_CHILDREN;

    }
}

