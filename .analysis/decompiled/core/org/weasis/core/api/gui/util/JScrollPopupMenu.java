/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;

public class JScrollPopupMenu
extends JPopupMenu {
    protected int maximumVisibleRows = 10;
    private JScrollBar popupScrollBar;

    public JScrollPopupMenu() {
        this(null);
    }

    public JScrollPopupMenu(String label) {
        super(label);
        this.setLayout(new ScrollPopupMenuLayout());
        super.add(this.getScrollBar());
        this.addMouseWheelListener(event -> {
            JScrollBar scrollBar = this.getScrollBar();
            int amount = event.getScrollType() == 0 ? event.getUnitsToScroll() * scrollBar.getUnitIncrement() : (event.getWheelRotation() < 0 ? -1 : 1) * scrollBar.getBlockIncrement();
            scrollBar.setValue(scrollBar.getValue() + amount);
            event.consume();
        });
    }

    protected JScrollBar getScrollBar() {
        if (this.popupScrollBar == null) {
            this.popupScrollBar = new JScrollBar(1);
            this.popupScrollBar.addAdjustmentListener(e -> {
                this.doLayout();
                this.repaint();
            });
            this.popupScrollBar.setVisible(false);
        }
        return this.popupScrollBar;
    }

    public int getMaximumVisibleRows() {
        return this.maximumVisibleRows;
    }

    public void setMaximumVisibleRows(int maximumVisibleRows) {
        this.maximumVisibleRows = maximumVisibleRows;
    }

    @Override
    public void paintChildren(Graphics g) {
        Insets insets = this.getInsets();
        g.clipRect(insets.left, insets.top, this.getWidth(), this.getHeight() - insets.top - insets.bottom);
        super.paintChildren(g);
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
        if (this.maximumVisibleRows < this.getComponentCount() - 1) {
            this.getScrollBar().setVisible(true);
        }
    }

    @Override
    public void remove(int index) {
        super.remove(++index);
        if (this.maximumVisibleRows >= this.getComponentCount() - 1) {
            this.getScrollBar().setVisible(false);
        }
    }

    @Override
    public void show(Component invoker, int x, int y) {
        JScrollBar scrollBar = this.getScrollBar();
        if (scrollBar.isVisible()) {
            int extent = 0;
            int max = 0;
            int i = 0;
            int unit = -1;
            int width = 0;
            for (Component comp : this.getComponents()) {
                if (comp instanceof JScrollBar) continue;
                Dimension preferredSize = comp.getPreferredSize();
                width = Math.max(width, preferredSize.width);
                if (unit < 0) {
                    unit = preferredSize.height;
                }
                if (i++ < this.maximumVisibleRows) {
                    extent += preferredSize.height;
                }
                max += preferredSize.height;
            }
            Insets insets = this.getInsets();
            int widthMargin = insets.left + insets.right;
            int heightMargin = insets.top + insets.bottom;
            scrollBar.setUnitIncrement(unit);
            scrollBar.setBlockIncrement(extent);
            scrollBar.setValues(0, heightMargin + extent, 0, heightMargin + max);
            int height = heightMargin + extent;
            this.setPopupSize(new Dimension(width += scrollBar.getPreferredSize().width + widthMargin, height));
        }
        super.show(invoker, x, y);
    }

    protected static class ScrollPopupMenuLayout
    implements LayoutManager {
        protected ScrollPopupMenuLayout() {
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            int visibleAmount = Integer.MAX_VALUE;
            Dimension dim = new Dimension();
            for (Component comp : parent.getComponents()) {
                if (!comp.isVisible()) continue;
                if (comp instanceof JScrollBar) {
                    JScrollBar scrollBar = (JScrollBar)comp;
                    visibleAmount = scrollBar.getVisibleAmount();
                    continue;
                }
                Dimension pref = comp.getPreferredSize();
                dim.width = Math.max(dim.width, pref.width);
                dim.height += pref.height;
            }
            Insets insets = parent.getInsets();
            dim.height = Math.min(dim.height + insets.top + insets.bottom, visibleAmount);
            return dim;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            int visibleAmount = Integer.MAX_VALUE;
            Dimension dim = new Dimension();
            for (Component comp : parent.getComponents()) {
                if (!comp.isVisible()) continue;
                if (comp instanceof JScrollBar) {
                    JScrollBar scrollBar = (JScrollBar)comp;
                    visibleAmount = scrollBar.getVisibleAmount();
                    continue;
                }
                Dimension min = comp.getMinimumSize();
                dim.width = Math.max(dim.width, min.width);
                dim.height += min.height;
            }
            Insets insets = parent.getInsets();
            dim.height = Math.min(dim.height + insets.top + insets.bottom, visibleAmount);
            return dim;
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets insets = parent.getInsets();
            int width = parent.getWidth() - insets.left - insets.right;
            int height = parent.getHeight() - insets.top - insets.bottom;
            int x = insets.left;
            int y = insets.top;
            int position = 0;
            for (Component comp : parent.getComponents()) {
                if (!(comp instanceof JScrollBar)) continue;
                JScrollBar scrollBar = (JScrollBar)comp;
                if (!comp.isVisible()) continue;
                Dimension dim = scrollBar.getPreferredSize();
                scrollBar.setBounds(x + width - dim.width, y, dim.width, height);
                width -= dim.width;
                position = scrollBar.getValue();
            }
            y -= position;
            for (Component comp : parent.getComponents()) {
                if (comp instanceof JScrollBar || !comp.isVisible()) continue;
                Dimension pref = comp.getPreferredSize();
                comp.setBounds(x, y, width, pref.height);
                y += pref.height;
            }
        }
    }
}

