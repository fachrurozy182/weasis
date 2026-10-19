/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.dock.common.CLocation
 *  bibliothek.gui.dock.common.mode.ExtendedMode
 */
package org.weasis.core.ui.editor.image.dockable;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.JSliderW;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.ui.docking.PluginTool;

public abstract class MiniTool
extends PluginTool
implements ActionListener {
    public static final String BUTTON_NAME = Messages.getString("MiniToolDockable.title");
    private SliderChangeListener currentAction;
    private final JSliderW slider;
    private boolean vertical = true;

    protected MiniTool(String pluginName) {
        super(pluginName, PluginTool.POSITION.EAST, ExtendedMode.NORMALIZED, Insertable.Type.TOOL, 5);
        this.dockable.setTitleShown(false);
        this.setDockableWidth(32);
        this.currentAction = this.getActions()[0];
        this.slider = MiniTool.createSlider(this.currentAction, this.vertical);
        this.jbInit();
    }

    private void jbInit() {
        this.setLayout(new BoxLayout(this, this.vertical ? 1 : 0));
        Dimension dim = GuiUtils.getDimension(5, 5);
        DropDownButton button = new DropDownButton(this, "Mini", this.currentAction.getActionW().getDropButtonIcon()){
            final /* synthetic */ MiniTool this$0;
            {
                MiniTool miniTool = this$0;
                Objects.requireNonNull(miniTool);
                this.this$0 = miniTool;
                super(type, icon);
            }

            @Override
            protected JPopupMenu getPopupMenu() {
                return this.this$0.getPopupMenuScroll(this);
            }
        };
        button.setToolTipText(Messages.getString("MiniToolDockable.change"));
        button.setAlignmentY(0.5f);
        button.setAlignmentX(0.5f);
        this.add(button);
        this.add(Box.createRigidArea(dim));
        this.slider.setAlignmentY(0.5f);
        this.slider.setAlignmentX(0.5f);
        this.add(this.slider);
        this.add(Box.createRigidArea(dim));
    }

    public abstract SliderChangeListener[] getActions();

    public static JSliderW createSlider(SliderChangeListener action, boolean vertical) {
        JSliderW slider = new JSliderW(action.getSliderMin(), action.getSliderMax(), action.getSliderValue());
        slider.setDisplayValueInTitle(false);
        slider.setInverted(vertical);
        slider.setOrientation(vertical ? 1 : 0);
        slider.setPaintTicks(true);
        slider.setPreferredSize(GuiUtils.getDimension(28, 250));
        slider.setShowLabels(false);
        action.registerActionState(slider);
        return slider;
    }

    @Override
    protected void changeToolWindowAnchor(CLocation clocation) {
        int w = this.getWidth();
        int h = this.getHeight();
        if (w != 0 && h != 0) {
            boolean bl = this.vertical = h >= w;
            if (this.vertical != this.slider.getInverted()) {
                this.setLayout(new BoxLayout(this, this.vertical ? 1 : 0));
                this.slider.setInverted(this.vertical);
                this.slider.setOrientation(this.vertical ? 1 : 0);
                this.slider.revalidate();
                this.slider.repaint();
            }
        }
    }

    private JPopupMenu getPopupMenuScroll(DropDownButton dropButton) {
        String type = dropButton.getType();
        JPopupMenu popupMouseScroll = new JPopupMenu(type);
        popupMouseScroll.setInvoker(dropButton);
        ButtonGroup groupButtons = new ButtonGroup();
        SliderChangeListener[] actions = this.getActions();
        for (int i = 0; i < actions.length; ++i) {
            JRadioButtonMenuItem radio = new JRadioButtonMenuItem(actions[i].toString(), (Icon)actions[i].getActionW().getIcon(), actions[i].equals(this.currentAction));
            GuiUtils.applySelectedIconEffect(radio);
            radio.setActionCommand(Integer.toString(i));
            radio.addActionListener(this);
            popupMouseScroll.add(radio);
            groupButtons.add(radio);
        }
        return popupMouseScroll;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JRadioButtonMenuItem item;
        Container container;
        Object object = e.getSource();
        if (object instanceof JRadioButtonMenuItem && (container = (item = (JRadioButtonMenuItem)object).getParent()) instanceof JPopupMenu) {
            JPopupMenu popupMenu = (JPopupMenu)container;
            SliderChangeListener newAction = this.getAction(e.getActionCommand());
            if (newAction == null || this.currentAction == newAction) {
                return;
            }
            if (this.currentAction != null) {
                this.currentAction.unregisterActionState(this.slider);
            }
            newAction.registerActionState(this.slider);
            this.currentAction = newAction;
            Component component = popupMenu.getInvoker();
            if (component instanceof DropDownButton) {
                DropDownButton dropDownButton = (DropDownButton)component;
                dropDownButton.setIcon(this.currentAction.getActionW().getDropButtonIcon());
            }
        }
    }

    private SliderChangeListener getAction(String actionCommand) {
        try {
            int index = Integer.parseInt(actionCommand);
            SliderChangeListener[] actions = this.getActions();
            if (index >= 0 && index < actions.length) {
                return actions[index];
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return null;
    }
}

