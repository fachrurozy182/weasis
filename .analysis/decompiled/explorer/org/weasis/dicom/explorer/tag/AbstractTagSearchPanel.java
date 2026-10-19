/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.GuiUtils
 */
package org.weasis.dicom.explorer.tag;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.dicom.explorer.Messages;

public abstract class AbstractTagSearchPanel
extends JPanel
implements KeyListener {
    protected final JTextField textFieldSearch = new JTextField();
    protected final JToolBar navigateToolbar = new JToolBar();

    protected AbstractTagSearchPanel() {
        this.setLayout(new BoxLayout(this, 0));
        this.add(new JLabel(Messages.getString("DicomFieldsView.search") + ": "));
        GuiUtils.setPreferredWidth((Component)this.textFieldSearch, (int)300, (int)100);
        this.textFieldSearch.getDocument().addDocumentListener(new DocumentListener(this){
            final /* synthetic */ AbstractTagSearchPanel this$0;
            {
                AbstractTagSearchPanel abstractTagSearchPanel = this$0;
                Objects.requireNonNull(abstractTagSearchPanel);
                this.this$0 = abstractTagSearchPanel;
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                this.this$0.filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.this$0.filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                this.this$0.filter();
            }
        });
        this.add(this.textFieldSearch);
        this.textFieldSearch.putClientProperty("JTextField.showClearButton", true);
        JButton up = new JButton(GuiUtils.getUpArrowIcon());
        up.setToolTipText(Messages.getString("DicomFieldsView.previous"));
        up.addActionListener(evt -> this.previous());
        this.navigateToolbar.add(up);
        JButton down = new JButton(GuiUtils.getDownArrowIcon());
        down.setToolTipText(Messages.getString("DicomFieldsView.next"));
        down.addActionListener(evt -> this.next());
        this.navigateToolbar.add(down);
        this.textFieldSearch.putClientProperty("JTextField.trailingComponent", this.navigateToolbar);
    }

    protected abstract void filter();

    protected abstract void previous();

    protected abstract void next();

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.isShiftDown() && e.getKeyCode() == 114) {
            this.previous();
        } else if (e.getKeyCode() == 114) {
            this.next();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }
}

