/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import eu.essilab.lablib.checkboxtree.CheckboxTree;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.JSliderW;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.ui.dialog.PropertiesDialog;
import org.weasis.core.ui.editor.image.HistogramView;
import org.weasis.core.ui.editor.image.ImageRegionStatistics;
import org.weasis.core.ui.model.graphic.imp.seg.GroupTreeNode;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.util.SegRegionCellRenderer;
import org.weasis.core.ui.util.SegRegionTool;
import org.weasis.core.ui.util.StructToolTipTreeNode;
import org.weasis.core.ui.util.TreeBuilder;

public class SegRegionTree
extends CheckboxTree {
    protected final JPopupMenu popupMenu = new JPopupMenu();
    protected final SegRegionTool segRegionTool;
    private int hoveredSwatchRow = -1;

    public SegRegionTree(SegRegionTool segRegionTool) {
        this.segRegionTool = segRegionTool;
    }

    public JPopupMenu getPopupMenu() {
        return this.popupMenu;
    }

    public SegRegionTool getSegRegionTool() {
        return this.segRegionTool;
    }

    protected void mousePressed(MouseEvent e) {
        this.popupMenu.removeAll();
        if (SwingUtilities.isRightMouseButton(e)) {
            this.showPopupMenu(e);
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            this.pickColorOnSwatch(e.getPoint());
        }
    }

    protected void showPopupMenu(MouseEvent e) {
        DefaultMutableTreeNode node = this.getTreeNode(e.getPoint());
        if (node != null) {
            boolean leaf = node.isLeaf();
            if (!leaf) {
                this.addPopupMenuItem(this.getCheckAllMenuItem(node, true));
                this.addPopupMenuItem(this.getCheckAllMenuItem(node, false));
            }
            this.addPopupMenuItem(this.getColorMenuItem(node));
            this.addPopupMenuItem(this.getOpacityMenuItem(node, e.getPoint()));
            if (leaf) {
                this.addPopupMenuItem(this.getSelectionMenuItem(node));
                this.addPopupMenuItem(this.getStatisticMenuItem(node));
            }
            this.popupMenu.show(this, e.getX(), e.getY());
        }
    }

    protected void addPopupMenuItem(JMenuItem menuItem) {
        if (menuItem != null) {
            this.popupMenu.add(menuItem);
        }
    }

    public void initListeners() {
        MouseAdapter adapter = new MouseAdapter(this){
            final /* synthetic */ SegRegionTree this$0;
            {
                SegRegionTree segRegionTree = this$0;
                Objects.requireNonNull(segRegionTree);
                this.this$0 = segRegionTree;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                this.this$0.mousePressed(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                this.this$0.setHoveredSwatchRow(this.this$0.swatchRowAt(e.getPoint()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                this.this$0.setHoveredSwatchRow(-1);
            }
        };
        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
    }

    public boolean isSwatchHovered(int row) {
        return row >= 0 && row == this.hoveredSwatchRow;
    }

    private int swatchRowAt(Point pt) {
        DefaultMutableTreeNode node;
        Object object;
        TreePath path = this.getPathForLocation(pt.x, pt.y);
        if (path != null && (object = path.getLastPathComponent()) instanceof DefaultMutableTreeNode && this.isOnColorSwatch(pt, path, node = (DefaultMutableTreeNode)object)) {
            return this.getRowForPath(path);
        }
        return -1;
    }

    private void setHoveredSwatchRow(int row) {
        if (row == this.hoveredSwatchRow) {
            return;
        }
        int previous = this.hoveredSwatchRow;
        this.hoveredSwatchRow = row;
        this.setCursor(row < 0 ? null : Cursor.getPredefinedCursor(12));
        this.repaintRow(previous);
        this.repaintRow(row);
    }

    private void repaintRow(int row) {
        Rectangle bounds;
        Rectangle rectangle = bounds = row < 0 ? null : this.getRowBounds(row);
        if (bounds != null) {
            this.repaint(bounds);
        }
    }

    @Override
    public String getToolTipText(MouseEvent evt) {
        TreePath curPath = this.getPathForLocation(evt.getX(), evt.getY());
        return StructToolTipTreeNode.getSegItemToolTipText(curPath);
    }

    protected DefaultMutableTreeNode getTreeNode(Point mousePosition) {
        Object userObject;
        TreePath treePath = this.getPathForLocation(mousePosition.x, mousePosition.y);
        if (treePath != null && (userObject = treePath.getLastPathComponent()) instanceof DefaultMutableTreeNode) {
            return (DefaultMutableTreeNode)userObject;
        }
        return null;
    }

    protected JMenuItem getCheckAllMenuItem(DefaultMutableTreeNode node, boolean selected) {
        JMenuItem selectAllMenuItem = new JMenuItem(selected ? Messages.getString("select.all.the.child.nodes") : Messages.getString("unselect.all.the.child.nodes"));
        selectAllMenuItem.addActionListener(e -> {
            if (node != null) {
                Enumeration<TreeNode> children = node.children();
                while (children.hasMoreElements()) {
                    TreeNode child = children.nextElement();
                    if (!(child instanceof DefaultMutableTreeNode)) continue;
                    DefaultMutableTreeNode dtm = (DefaultMutableTreeNode)child;
                    TreePath tp = new TreePath(dtm.getPath());
                    TreeBuilder.setPathSelection(this, tp, selected);
                }
            }
        });
        return selectAllMenuItem;
    }

    protected JMenuItem getColorMenuItem(DefaultMutableTreeNode node) {
        JMenuItem jMenuItem = new JMenuItem(Messages.getString("MeasureTool.pick_color"));
        jMenuItem.addActionListener(actionEvent -> this.pickColor(SegRegionTree.collectRegions(node)));
        return jMenuItem;
    }

    private void pickColorOnSwatch(Point pt) {
        DefaultMutableTreeNode node;
        Object object;
        TreePath path = this.getPathForLocation(pt.x, pt.y);
        if (path != null && (object = path.getLastPathComponent()) instanceof DefaultMutableTreeNode && this.isOnColorSwatch(pt, path, node = (DefaultMutableTreeNode)object)) {
            List<SegRegion<?>> segRegions = SegRegionTree.collectRegions(node);
            SwingUtilities.invokeLater(() -> this.pickColor(segRegions));
        }
    }

    private boolean isOnColorSwatch(Point pt, TreePath path, DefaultMutableTreeNode node) {
        Component comp = this.getCellRenderer().getTreeCellRendererComponent(this, node, false, this.isExpanded(path), node.isLeaf(), this.getRowForPath(path), false);
        if (comp instanceof SegRegionCellRenderer) {
            SegRegionCellRenderer renderer = (SegRegionCellRenderer)comp;
            Rectangle swatch = renderer.getSwatchBounds(this.getPathBounds(path));
            return swatch != null && swatch.contains(pt);
        }
        return false;
    }

    protected void pickColor(List<SegRegion<?>> segRegions) {
        if (segRegions.isEmpty()) {
            return;
        }
        Color color = JColorChooser.showDialog(this, Messages.getString("MeasureTool.pick_color"), ((SegRegion)segRegions.getFirst()).getColor());
        if (color == null) {
            return;
        }
        for (SegRegion<?> region : segRegions) {
            Color previous = region.getColor();
            int alpha = previous == null ? 255 : previous.getAlpha();
            region.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        }
        this.repaint();
        this.segRegionTool.updateVisibleNode();
    }

    protected static List<SegRegion<?>> collectRegions(DefaultMutableTreeNode node) {
        if (node == null) {
            return List.of();
        }
        if (node.isLeaf()) {
            List<SegRegion<?>> list;
            Object object = node.getUserObject();
            if (object instanceof SegRegion) {
                SegRegion region = (SegRegion)object;
                list = List.of(region);
            } else {
                list = List.of();
            }
            return list;
        }
        ArrayList segRegions = new ArrayList(node.getChildCount());
        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode dtm;
            Object object = children.nextElement();
            if (!(object instanceof DefaultMutableTreeNode) || !((object = (dtm = (DefaultMutableTreeNode)object).getUserObject()) instanceof SegRegion)) continue;
            SegRegion region = (SegRegion)object;
            segRegions.add(region);
        }
        return segRegions;
    }

    protected JMenuItem getOpacityMenuItem(DefaultMutableTreeNode node, Point pt) {
        JMenuItem jMenuItem = new JMenuItem(PropertiesDialog.FILL_OPACITY);
        jMenuItem.addActionListener(actionEvent -> this.showSliderInPopup(node, pt));
        return jMenuItem;
    }

    private void showSliderInPopup(DefaultMutableTreeNode node, Point pt) {
        if (node != null) {
            List<SegRegion<?>> segRegions = SegRegionTree.collectRegions(node);
            if (segRegions.isEmpty()) {
                return;
            }
            JPopupMenu menu = new JPopupMenu();
            JSliderW jSlider = PropertiesDialog.createOpacitySlider(PropertiesDialog.FILL_OPACITY);
            GuiUtils.setPreferredWidth(jSlider, 250);
            jSlider.setValue((int)(((SegRegion)segRegions.getFirst()).getInteriorOpacity() * 100.0f));
            PropertiesDialog.updateSlider(jSlider, PropertiesDialog.FILL_OPACITY);
            jSlider.addChangeListener(l -> {
                float value = PropertiesDialog.updateSlider(jSlider, PropertiesDialog.FILL_OPACITY);
                for (SegRegion c : segRegions) {
                    c.setInteriorOpacity(value);
                }
                this.segRegionTool.updateVisibleNode();
            });
            menu.add(jSlider);
            menu.show(this, pt.x, pt.y);
        }
    }

    public void updateVisibleNode(DefaultMutableTreeNode start, GroupTreeNode parent) {
        Enumeration<TreeNode> children = start.children();
        while (children.hasMoreElements()) {
            TreePath tp;
            DefaultMutableTreeNode dtm = (DefaultMutableTreeNode)children.nextElement();
            if (dtm.isLeaf()) {
                tp = new TreePath(dtm.getPath());
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
                Object object = node.getUserObject();
                if (!(object instanceof SegRegion)) continue;
                SegRegion region = (SegRegion)object;
                boolean selected = this.getCheckingModel().isPathChecked(tp);
                region.setSelected(selected);
                region.setVisible(selected && parent.isParentVisible());
                continue;
            }
            if (!(dtm instanceof GroupTreeNode)) continue;
            GroupTreeNode groupTreeNode = (GroupTreeNode)dtm;
            tp = new TreePath(dtm.getPath());
            boolean selected = this.getCheckingModel().isPathChecked(tp);
            groupTreeNode.setSelected(selected);
            this.updateVisibleNode(dtm, groupTreeNode);
        }
    }

    protected JMenuItem getSelectionMenuItem(DefaultMutableTreeNode node) {
        JMenuItem selectAllMenuItem = new JMenuItem(Messages.getString("show.in.the.image.view"));
        selectAllMenuItem.addActionListener(actionEvent -> {
            Object patt0$temp;
            if (node != null && node.isLeaf() && (patt0$temp = node.getUserObject()) instanceof SegRegion) {
                SegRegion region = (SegRegion)patt0$temp;
                this.segRegionTool.show(region);
            }
        });
        return selectAllMenuItem;
    }

    protected JMenuItem getStatisticMenuItem(DefaultMutableTreeNode node) {
        JMenuItem selectAllMenuItem = new JMenuItem(Messages.getString("pixel.statistics"));
        selectAllMenuItem.addActionListener(actionEvent -> {
            Object patt0$temp;
            if (node != null && node.isLeaf() && (patt0$temp = node.getUserObject()) instanceof SegRegion) {
                SegRegion region = (SegRegion)patt0$temp;
                this.segRegionTool.computeStatistics(region);
            }
        });
        return selectAllMenuItem;
    }

    public void showStatistics(SegContour contour, MeasurableLayer layer) {
        if (contour == null) {
            return;
        }
        List<MeasureItem> measList = ImageRegionStatistics.getImageStatistics(contour.getSegGraphic(), layer, true);
        JPanel tableContainer = HistogramView.buildStatisticsTable(measList);
        JOptionPane.showMessageDialog(this.getParent(), tableContainer, Messages.getString("HistogramView.stats"), -1, null);
    }

    public void setPathSelection(TreePath path, boolean selected) {
        TreeBuilder.setPathSelection(this, path, selected);
    }

    public boolean hasAllParentsChecked(TreePath path) {
        boolean allParentsChecked = true;
        Object[] pathArray = path.getPath();
        for (int i = 1; i < pathArray.length; ++i) {
            TreePath parentPath = new TreePath(Arrays.copyOfRange(pathArray, 0, i + 1));
            if (this.getCheckingModel().isPathChecked(parentPath)) continue;
            allParentsChecked = false;
            break;
        }
        return allParentsChecked;
    }
}

