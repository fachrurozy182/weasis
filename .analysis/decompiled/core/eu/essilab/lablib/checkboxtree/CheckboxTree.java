/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.CheckboxTreeCellRenderer;
import eu.essilab.lablib.checkboxtree.DefaultCheckboxTreeCellRenderer;
import eu.essilab.lablib.checkboxtree.DefaultTreeCheckingModel;
import eu.essilab.lablib.checkboxtree.NullTreeCheckingModel;
import eu.essilab.lablib.checkboxtree.TreeCheckingEvent;
import eu.essilab.lablib.checkboxtree.TreeCheckingListener;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class CheckboxTree
extends JTree {
    private TreeCheckingModel checkingModel;
    private boolean selectsByChecking;

    public CheckboxTree() {
        super(CheckboxTree.getDefaultTreeModel());
        this.initialize();
    }

    public CheckboxTree(TreeModel treemodel) {
        super(treemodel);
        this.initialize();
    }

    public CheckboxTree(TreeNode root) {
        super(root);
        this.initialize();
    }

    public void addCheckingPath(TreePath path) {
        this.getCheckingModel().addCheckingPath(path);
    }

    public void addCheckingPaths(TreePath[] paths) {
        this.getCheckingModel().addCheckingPaths(paths);
    }

    public void addTreeCheckingListener(TreeCheckingListener tsl) {
        this.checkingModel.addTreeCheckingListener(tsl);
    }

    public void clearChecking() {
        this.getCheckingModel().clearChecking();
    }

    public void expandAll() {
        this.expandSubTree(new TreePath(this.getModel().getRoot()));
    }

    private void expandSubTree(TreePath path) {
        this.expandPath(path);
        Object node = path.getLastPathComponent();
        int childrenNumber = this.getModel().getChildCount(node);
        TreePath[] childrenPath = new TreePath[childrenNumber];
        for (int childIndex = 0; childIndex < childrenNumber; ++childIndex) {
            childrenPath[childIndex] = path.pathByAddingChild(this.getModel().getChild(node, childIndex));
            this.expandSubTree(childrenPath[childIndex]);
        }
    }

    public TreeCheckingModel getCheckingModel() {
        if (this.checkingModel == null) {
            this.checkingModel = NullTreeCheckingModel.getInstance();
        }
        return this.checkingModel;
    }

    public TreePath[] getCheckingPaths() {
        return this.getCheckingModel().getCheckingPaths();
    }

    public TreePath[] getCheckingRoots() {
        return this.getCheckingModel().getCheckingRoots();
    }

    public TreePath[] getGreyingPaths() {
        return this.getCheckingModel().getGreyingPaths();
    }

    private void initialize() {
        this.setCheckingModel(new DefaultTreeCheckingModel(this.treeModel));
        this.setCellRenderer(new DefaultCheckboxTreeCellRenderer());
        this.setSelectsByChecking(true);
        this.addKeyListener(new SpaceListener());
        this.selectionModel.setSelectionMode(4);
        this.setShowsRootHandles(true);
        this.putClientProperty("JTree.lineStyle", "Angled");
    }

    public boolean isPathChecked(TreePath path) {
        return this.getCheckingModel().isPathChecked(path);
    }

    public boolean isSelectsByChecking() {
        return this.selectsByChecking;
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        Rectangle rect;
        int y;
        int x;
        int row;
        if (e.getID() == 501 && !e.isConsumed() && this.isEnabled() && (row = this.getRowForLocation(x = e.getX(), y = e.getY())) != -1 && (rect = this.getRowBounds(row)) != null && this.getCellRenderer().isOnHotspot(x - rect.x, y - rect.y)) {
            this.getCheckingModel().toggleCheckingPath(this.getPathForRow(row));
            if (!this.isSelectsByChecking()) {
                return;
            }
        }
        super.processMouseEvent(e);
    }

    public void removeCheckingPath(TreePath path) {
        this.getCheckingModel().removeCheckingPath(path);
    }

    public void removeCheckingPaths(TreePath[] paths) {
        this.getCheckingModel().removeCheckingPaths(paths);
    }

    public void removeTreeCheckingListener(TreeCheckingListener tcl) {
        this.checkingModel.removeTreeCheckingListener(tcl);
    }

    @Override
    public void setCellRenderer(TreeCellRenderer tcl) {
        if (!(tcl instanceof CheckboxTreeCellRenderer)) {
            throw new IllegalArgumentException("The argument does not implement the CheckboxTreeCellRenderer interface: " + tcl);
        }
        super.setCellRenderer(tcl);
    }

    @Override
    public CheckboxTreeCellRenderer getCellRenderer() {
        if (this.cellRenderer == null) {
            this.cellRenderer = new DefaultCheckboxTreeCellRenderer();
        }
        return (CheckboxTreeCellRenderer)this.cellRenderer;
    }

    public void setCheckingModel(TreeCheckingModel newCheckingModel) {
        TreeCheckingModel oldCheckingModel = this.checkingModel;
        if (oldCheckingModel instanceof DefaultTreeCheckingModel) {
            ((DefaultTreeCheckingModel)oldCheckingModel).setTreeModel(null);
        }
        if (newCheckingModel != null) {
            this.checkingModel = newCheckingModel;
            if (newCheckingModel instanceof DefaultTreeCheckingModel) {
                ((DefaultTreeCheckingModel)newCheckingModel).setTreeModel(this.getModel());
            }
            newCheckingModel.addTreeCheckingListener(new TreeCheckingListener(){

                @Override
                public void valueChanged(TreeCheckingEvent e) {
                    CheckboxTree.this.repaint();
                }
            });
        } else {
            this.checkingModel = NullTreeCheckingModel.getInstance();
        }
    }

    public void setCheckingPath(TreePath path) {
        this.getCheckingModel().setCheckingPath(path);
    }

    public void setCheckingPaths(TreePath[] paths) {
        this.getCheckingModel().setCheckingPaths(paths);
    }

    @Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        if (this.checkingModel instanceof DefaultTreeCheckingModel) {
            ((DefaultTreeCheckingModel)this.checkingModel).setTreeModel(newModel);
        }
    }

    public void setSelectsByChecking(boolean selectsByChecking) {
        this.selectsByChecking = selectsByChecking;
    }

    @Override
    public String toString() {
        String retVal = super.toString();
        TreeCheckingModel tcm = this.getCheckingModel();
        if (tcm != null) {
            return retVal + "\n" + tcm.toString();
        }
        return retVal;
    }

    private class SpaceListener
    extends KeyAdapter {
        private SpaceListener() {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (!CheckboxTree.this.isEnabled()) {
                return;
            }
            TreePath path = CheckboxTree.this.getSelectionPath();
            if (e.getKeyCode() == 32 && path != null) {
                TreeCheckingModel cm = CheckboxTree.this.getCheckingModel();
                cm.toggleCheckingPath(path);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }
}

