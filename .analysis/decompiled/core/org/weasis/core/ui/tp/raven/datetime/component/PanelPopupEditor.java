/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.datetime.component;

import java.awt.Component;
import java.awt.Point;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.TimePicker;
import org.weasis.core.ui.tp.raven.datetime.util.Utils;

public abstract class PanelPopupEditor
extends JPanel {
    protected JFormattedTextField editor;
    protected JPopupMenu popupMenu;
    protected boolean editorValidation = true;
    protected boolean isValid;
    protected boolean validationOnNull;
    protected String defaultPlaceholder;
    protected Point popupSpace = new Point(1, 1);
    protected LookAndFeel oldThemes = UIManager.getLookAndFeel();

    public void showPopup() {
        this.showPopup(this.editor);
    }

    public void showPopup(Component component) {
        if (this.popupMenu == null) {
            this.popupMenu = new JPopupMenu();
            this.popupMenu.putClientProperty("FlatLaf.style", "borderInsets:1,1,1,1");
            this.popupMenu.add(this);
        }
        if (UIManager.getLookAndFeel() != this.oldThemes) {
            SwingUtilities.updateComponentTreeUI(this.popupMenu);
            this.oldThemes = UIManager.getLookAndFeel();
        }
        Point point = Utils.adjustPopupLocation(this.popupMenu, component, this.popupSpace);
        this.popupOpen();
        this.popupMenu.show(component, point.x, point.y);
    }

    protected JPanel createButtonPanel() {
        JButton nowButton = new JButton(Messages.getString("now"));
        nowButton.setFocusable(false);
        nowButton.addActionListener(actionEvent -> {
            PanelPopupEditor patt0$temp = this;
            if (patt0$temp instanceof DatePicker) {
                DatePicker picker = (DatePicker)patt0$temp;
                picker.now();
            } else {
                PanelPopupEditor patt1$temp = this;
                if (patt1$temp instanceof TimePicker) {
                    TimePicker picker = (TimePicker)patt1$temp;
                    picker.now();
                }
            }
        });
        JButton closeButton = new JButton(Messages.getString("close"));
        closeButton.setFocusable(false);
        closeButton.addActionListener(actionEvent -> SwingUtilities.getWindowAncestor(this).dispose());
        return GuiUtils.getFlowLayoutPanel(4, 10, 10, nowButton, closeButton);
    }

    public void closePopup() {
        if (this.popupMenu != null) {
            this.popupMenu.setVisible(false);
            this.repaint();
        }
    }

    public boolean isEditorValidation() {
        return this.editorValidation;
    }

    public void setEditorValidation(boolean editorValidation) {
        if (this.editorValidation != editorValidation) {
            this.editorValidation = editorValidation;
            if (this.editor != null) {
                if (editorValidation) {
                    this.validChanged(this.editor, this.isValid);
                } else {
                    this.validChanged(this.editor, true);
                }
            }
        }
    }

    public boolean isValidationOnNull() {
        return this.validationOnNull;
    }

    public void setValidationOnNull(boolean validationOnNull) {
        if (this.validationOnNull != validationOnNull) {
            this.validationOnNull = validationOnNull;
            this.commitEdit();
        }
    }

    protected void checkValidation(boolean status) {
        boolean valid;
        boolean bl = valid = status || this.isEditorValidationOnNull();
        if (this.isValid != valid) {
            this.isValid = valid;
            if (this.editor != null && this.editorValidation) {
                this.validChanged(this.editor, valid);
            }
        }
    }

    protected void validChanged(JFormattedTextField editor, boolean isValid) {
        String style = isValid ? null : "error";
        editor.putClientProperty("JComponent.outline", style);
    }

    protected boolean isEditorValidationOnNull() {
        if (this.validationOnNull) {
            return false;
        }
        return this.editor != null && this.editor.getText().equals(this.getDefaultPlaceholder());
    }

    protected void commitEdit() {
        if (this.editor != null && this.editorValidation) {
            try {
                this.editor.commitEdit();
            }
            catch (ParseException parseException) {
                // empty catch block
            }
        }
    }

    public JFormattedTextField getEditor() {
        return this.editor;
    }

    public Point getPopupSpace() {
        return this.popupSpace;
    }

    public void setPopupSpace(Point popupSpace) {
        this.popupSpace = popupSpace;
    }

    protected abstract String getDefaultPlaceholder();

    protected abstract void popupOpen();
}

