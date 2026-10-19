/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.GuiUtils
 */
package org.weasis.dicom.explorer.pref.node;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.dicom.explorer.Messages;

public abstract class AbstractListEditor<T>
extends JDialog {
    protected final DefaultListSelectionModel selectedModel = new DefaultListSelectionModel();
    protected final JList<T> itemList = new JList();

    public AbstractListEditor(Window parent, String title) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.itemList.setSelectionMode(0);
        this.jbInit();
        this.itemList.addMouseListener(new MouseAdapter(this){
            final /* synthetic */ AbstractListEditor this$0;
            {
                AbstractListEditor abstractListEditor = this$0;
                Objects.requireNonNull(abstractListEditor);
                this.this$0 = abstractListEditor;
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    this.this$0.editItem();
                }
            }
        });
    }

    private void jbInit() {
        this.setDefaultCloseOperation(2);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(GuiUtils.getEmptyBorder((int)10, (int)15, (int)5, (int)15));
        this.selectedModel.setSelectionMode(2);
        JButton jButtonClose = new JButton(Messages.getString("HttpHeadersEditor.close"));
        jButtonClose.addActionListener(actionEvent -> this.cancel());
        this.itemList.setSelectionModel(this.selectedModel);
        JButton jButtonDelete = new JButton(Messages.getString("HttpHeadersEditor.delete"));
        jButtonDelete.addActionListener(actionEvent -> this.deleteSelectedItems());
        JButton jButtonEdit = new JButton(Messages.getString("HttpHeadersEditor.edit"));
        jButtonEdit.addActionListener(actionEvent -> this.editItem());
        JButton jButtonAdd = new JButton(Messages.getString("HttpHeadersEditor.add"));
        jButtonAdd.addActionListener(actionEvent -> this.addItem());
        JScrollPane scrollPane = new JScrollPane(this.itemList);
        scrollPane.setPreferredSize(GuiUtils.getDimension((int)300, (int)150));
        panel.add((Component)GuiUtils.getFlowLayoutPanel((int)4, (int)0, (int)10, (JComponent[])new JComponent[]{jButtonClose}), "South");
        panel.add((Component)GuiUtils.getHorizontalBoxLayoutPanel((int)10, (JComponent[])new JComponent[]{GuiUtils.getVerticalBoxLayoutPanel((int)10, (JComponent[])new JComponent[]{jButtonEdit, jButtonAdd, jButtonDelete})}), "East");
        panel.add((Component)scrollPane, "Center");
        this.setContentPane(panel);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == 201) {
            this.cancel();
        }
        super.processWindowEvent(e);
    }

    public void cancel() {
        this.dispose();
    }

    public void deleteSelectedItems() {
        if (this.isNoItemSelected()) {
            return;
        }
        List<T> selectedItems = this.itemList.getSelectedValuesList();
        for (T item : selectedItems) {
            this.deleteItem(item);
        }
        this.selectedModel.clearSelection();
        this.initializeList();
    }

    public void editItem() {
        if (this.isNoItemSelected()) {
            return;
        }
        List<T> selectedItems = this.itemList.getSelectedValuesList();
        if (selectedItems.size() == 1) {
            this.modifyItem(selectedItems.getFirst());
        } else {
            JOptionPane.showMessageDialog(this, Messages.getString("HttpHeadersEditor.msg_onlyone"), this.getTitle(), 0);
        }
    }

    private boolean isNoItemSelected() {
        if (this.selectedModel.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, Messages.getString("HttpHeadersEditor.msg_noheader"), this.getTitle(), 0);
            return true;
        }
        return false;
    }

    public void addItem() {
        this.modifyItem(null);
    }

    protected abstract void initializeList();

    protected abstract void deleteItem(T var1);

    protected abstract void modifyItem(T var1);
}

