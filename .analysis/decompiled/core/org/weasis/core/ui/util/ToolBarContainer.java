/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.InsertableUtil;
import org.weasis.core.ui.util.DynamicToolbar;
import org.weasis.core.ui.util.Toolbar;
import org.weasis.core.ui.util.WtoolBar;

public class ToolBarContainer
extends JPanel {
    public static final Toolbar EMPTY = ToolBarContainer.buildEmptyToolBar("empty");
    private final List<Toolbar> bars = new ArrayList<Toolbar>();

    public ToolBarContainer() {
        this.setLayout(new FlowLayout(3, 0, 0));
        this.addMouseListener(new PopClickListener(this));
    }

    public void registerToolBar(List<Toolbar> toolBars) {
        this.unregisterAll();
        if (toolBars == null || toolBars.isEmpty()) {
            this.add(EMPTY.getComponent());
            this.bars.add(EMPTY);
        } else {
            ArrayList<Toolbar> sortedBars = new ArrayList<Toolbar>(toolBars);
            InsertableUtil.sortInsertable(sortedBars);
            for (Toolbar toolbar : sortedBars) {
                WtoolBar component = toolbar.getComponent();
                if (component instanceof DynamicToolbar) {
                    DynamicToolbar dyn = (DynamicToolbar)((Object)component);
                    dyn.updateToolbar();
                }
                if (toolbar.isComponentEnabled()) {
                    this.add(component);
                }
                this.bars.add(toolbar);
            }
        }
        this.revalidate();
        this.repaint();
    }

    public void displayToolbar(WtoolBar barComponent, boolean show) {
        if (show == barComponent.isComponentEnabled()) {
            return;
        }
        if (show) {
            int index = (int)this.bars.stream().takeWhile(b -> b.getComponent() != barComponent).filter(Insertable::isComponentEnabled).count();
            this.add((Component)barComponent, index);
        } else {
            this.remove(barComponent);
        }
        barComponent.setComponentEnabled(show);
        this.revalidate();
        this.repaint();
    }

    private void unregisterAll() {
        this.bars.clear();
        this.removeAll();
    }

    public List<Toolbar> getRegisteredToolBars() {
        return new ArrayList<Toolbar>(this.bars);
    }

    public static WtoolBar buildEmptyToolBar(String name) {
        WtoolBar toolBar = new WtoolBar(name, 0){

            @Override
            public Insertable.Type getType() {
                return Insertable.Type.EMPTY;
            }
        };
        toolBar.add(ToolBarContainer.buildToolBarSizerComponent());
        return toolBar;
    }

    private static JComponent buildToolBarSizerComponent() {
        return new JButton(new Icon(){

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
            }

            @Override
            public int getIconWidth() {
                return 2;
            }

            @Override
            public int getIconHeight() {
                return 30;
            }
        });
    }

    private class PopClickListener
    extends MouseAdapter {
        final /* synthetic */ ToolBarContainer this$0;

        private PopClickListener(ToolBarContainer toolBarContainer) {
            ToolBarContainer toolBarContainer2 = toolBarContainer;
            Objects.requireNonNull(toolBarContainer2);
            this.this$0 = toolBarContainer2;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                this.doPop(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                this.doPop(e);
            }
        }

        private void doPop(MouseEvent e) {
            PopUpToolbars menu = new PopUpToolbars(this.this$0);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private class PopUpToolbars
    extends JPopupMenu {
        final /* synthetic */ ToolBarContainer this$0;

        public PopUpToolbars(ToolBarContainer toolBarContainer) {
            ToolBarContainer toolBarContainer2 = toolBarContainer;
            Objects.requireNonNull(toolBarContainer2);
            this.this$0 = toolBarContainer2;
            for (Toolbar bar : toolBarContainer.bars) {
                if (Insertable.Type.EMPTY.equals((Object)bar.getType())) continue;
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(bar.getComponentName(), bar.isComponentEnabled());
                item.addActionListener(e -> {
                    Object patt0$temp = e.getSource();
                    if (patt0$temp instanceof JCheckBoxMenuItem) {
                        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)patt0$temp;
                        toolBarContainer.displayToolbar(bar.getComponent(), menuItem.isSelected());
                    }
                });
                this.add(item);
            }
        }
    }
}

