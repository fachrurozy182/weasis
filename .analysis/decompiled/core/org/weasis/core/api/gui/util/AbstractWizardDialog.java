/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.InsertableUtil;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.PageItem;
import org.weasis.core.ui.util.TreeBuilder;

public abstract class AbstractWizardDialog
extends JDialog {
    public static final int HORIZONTAL_GAP = 15;
    public static final int VERTICAL_GAP = 10;
    protected String settingTitle;
    protected AbstractItemDialogPage currentPage = null;
    protected DefaultMutableTreeNode pagesRoot = new DefaultMutableTreeNode("root");
    private final JPanel jPanelRootPanel = new JPanel();
    protected final JButton jButtonClose = new JButton(Messages.getString("AbstractWizardDialog.close"));
    protected final JTree tree = new JTree();
    protected final JPanel jPanelBottom = new JPanel();
    private final JPanel jPanelMain = new JPanel();
    protected JScrollPane jScrollPanePage = new JScrollPane();
    private final JScrollPane jScrollPane1 = new JScrollPane();

    protected AbstractWizardDialog(Window window, String title, Dialog.ModalityType modal, Dimension pageSize) {
        super(window, title, modal);
        this.settingTitle = title;
        this.jScrollPanePage.setPreferredSize(GuiUtils.getDimension(pageSize.width, pageSize.height));
        this.jScrollPanePage.setBorder(BorderFactory.createEmptyBorder());
        this.jbInit();
    }

    private void jbInit() {
        this.setDefaultCloseOperation(0);
        this.jPanelMain.setLayout(new BorderLayout());
        this.jPanelMain.add((Component)this.jScrollPanePage, "Center");
        JSplitPane splitPane = new JSplitPane(1, this.jScrollPane1, this.jPanelMain);
        this.jPanelRootPanel.setLayout(new BorderLayout());
        this.jPanelRootPanel.add((Component)splitPane, "Center");
        this.jPanelBottom.setLayout(new BoxLayout(this.jPanelBottom, 1));
        this.jPanelBottom.add(GuiUtils.getFlowLayoutPanel(4, 15, 10, this.jButtonClose));
        this.jButtonClose.addActionListener(e -> this.cancel());
        this.jPanelRootPanel.add((Component)this.jPanelBottom, "South");
        this.jScrollPane1.setViewportView(this.tree);
        this.getContentPane().add((Component)this.jPanelRootPanel, null);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == 201) {
            this.cancel();
        }
        super.processWindowEvent(e);
    }

    protected abstract void initializePages();

    public void showFirstPage() {
        if (this.pagesRoot.getChildCount() > 0) {
            this.tree.setSelectionRow(0);
        }
    }

    public void showPage(String title) {
        if (!this.selectPage(title, this.pagesRoot)) {
            this.showFirstPage();
        }
    }

    private boolean selectPage(String title, DefaultMutableTreeNode root) {
        if (title != null) {
            Enumeration<TreeNode> children = root.children();
            while (children.hasMoreElements()) {
                PageItem page;
                TreeNode child = children.nextElement();
                if (!(child instanceof DefaultMutableTreeNode)) continue;
                DefaultMutableTreeNode dtm = (DefaultMutableTreeNode)child;
                Object object = dtm.getUserObject();
                if (object instanceof PageItem && (page = (PageItem)object).getTitle().equals(title)) {
                    TreePath tp = new TreePath(dtm.getPath());
                    if (!dtm.isLeaf()) {
                        this.tree.expandPath(tp);
                    }
                    this.tree.setSelectionPath(tp);
                    return true;
                }
                if (dtm.getChildCount() <= 0 || !this.selectPage(title, dtm)) continue;
                return true;
            }
        }
        return false;
    }

    public AbstractItemDialogPage getCurrentPage() {
        Component object = null;
        JViewport viewPort = this.jScrollPanePage.getViewport();
        if (viewPort != null && viewPort.getComponentCount() > 0) {
            object = viewPort.getComponent(0);
        }
        if (object instanceof AbstractItemDialogPage) {
            AbstractItemDialogPage page = (AbstractItemDialogPage)object;
            return page;
        }
        return null;
    }

    protected void selectPage(AbstractItemDialogPage page) {
        if (page != null) {
            if (this.currentPage != null) {
                this.currentPage.deselectPageAction();
            }
            this.currentPage = page;
            this.currentPage.selectPageAction();
            this.jScrollPanePage.setViewportView(this.currentPage);
        }
    }

    protected void iniTree() {
        Enumeration<TreeNode> children = this.pagesRoot.children();
        while (children.hasMoreElements()) {
            TreeNode child = children.nextElement();
            if (!(child instanceof DefaultMutableTreeNode)) continue;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)child;
            this.iniSubpages(node);
        }
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)this.tree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        DefaultTreeModel model = new DefaultTreeModel(this.pagesRoot, false);
        this.tree.setModel(model);
        this.tree.setShowsRootHandles(true);
        this.tree.setRootVisible(false);
        this.tree.setExpandsSelectedPaths(true);
        this.tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode object;
            Object patt0$temp;
            if (e.getNewLeadSelectionPath() != null && (patt0$temp = (object = (DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent()).getUserObject()) instanceof AbstractItemDialogPage) {
                AbstractItemDialogPage page = (AbstractItemDialogPage)patt0$temp;
                this.selectPage(page);
            }
        });
        TreeBuilder.expandTree(this.tree, this.pagesRoot, 2);
    }

    protected void iniSubpages(DefaultMutableTreeNode node) {
        List<PageItem> subpages = null;
        Object object = node.getUserObject();
        if (object instanceof Insertable) {
            subpages = ((AbstractItemDialogPage)object).getSubPages();
        }
        if (subpages != null) {
            ArrayList<Insertable> list = new ArrayList<Insertable>();
            for (PageItem pageItem : subpages) {
                if (!(pageItem instanceof Insertable)) continue;
                Insertable insertable = (Insertable)((Object)pageItem);
                list.add(insertable);
            }
            InsertableUtil.sortInsertable(list);
            for (PageItem subpage : subpages) {
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subpage);
                node.add(subNode);
                this.iniSubpages(subNode);
            }
        }
    }

    public PageItem getSelectedPage() {
        Object object;
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)this.tree.getLastSelectedPathComponent();
        if (selectedNode != null && (object = selectedNode.getUserObject()) instanceof PageItem) {
            PageItem page = (PageItem)object;
            return page;
        }
        return null;
    }

    public void closeAllPages() {
        Enumeration<TreeNode> children = this.pagesRoot.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode treeNode;
            Object object;
            TreeNode child = children.nextElement();
            if (!(child instanceof DefaultMutableTreeNode) || !((object = (treeNode = (DefaultMutableTreeNode)child).getUserObject()) instanceof AbstractItemDialogPage)) continue;
            AbstractItemDialogPage page = (AbstractItemDialogPage)object;
            page.closeAdditionalWindow();
        }
    }

    protected void resetAllToDefault() {
        Enumeration<TreeNode> children = this.pagesRoot.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode node;
            Object object;
            TreeNode child = children.nextElement();
            if (!(child instanceof DefaultMutableTreeNode) || !((object = (node = (DefaultMutableTreeNode)child).getUserObject()) instanceof AbstractItemDialogPage)) continue;
            AbstractItemDialogPage page = (AbstractItemDialogPage)object;
            for (PageItem subpage : page.getSubPages()) {
                subpage.resetToDefaultValues();
            }
            page.resetToDefaultValues();
        }
    }

    public abstract void cancel();
}

