/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import eu.essilab.lablib.checkboxtree.QuadristateButtonModel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ActionMapUIResource;

public class QuadristateCheckbox
extends JCheckBox {
    public QuadristateCheckbox() {
        this((String)null);
    }

    public QuadristateCheckbox(String text) {
        this(text, QuadristateButtonModel.State.UNCHECKED);
    }

    public QuadristateCheckbox(String text, Icon icon, QuadristateButtonModel.State state) {
        super(text, icon);
        super.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                QuadristateCheckbox.this.grabFocus();
                QuadristateCheckbox.this.getModel().nextState();
            }
        });
        ActionMapUIResource map = new ActionMapUIResource();
        map.put("pressed", new AbstractAction(){

            @Override
            public void actionPerformed(ActionEvent e) {
                QuadristateCheckbox.this.grabFocus();
                QuadristateCheckbox.this.getModel().nextState();
            }
        });
        map.put("released", null);
        SwingUtilities.replaceUIActionMap(this, map);
        this.setState(state);
    }

    public QuadristateCheckbox(String text, QuadristateButtonModel.State initial) {
        this(text, null, initial);
    }

    @Override
    public void addMouseListener(MouseListener l) {
    }

    @Override
    public QuadristateButtonModel getModel() {
        return (QuadristateButtonModel)super.getModel();
    }

    public QuadristateButtonModel.State getState() {
        return this.getModel().getState();
    }

    @Override
    protected void init(String text, Icon icon) {
        this.model = new QuadristateButtonModel();
        super.setModel(this.model);
        super.init(text, icon);
    }

    public void setModel(QuadristateButtonModel model) {
        super.setModel(model);
    }

    public void setState(QuadristateButtonModel.State state) {
        this.getModel().setState(state);
    }
}

