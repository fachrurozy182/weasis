/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.pref.node;

import java.awt.Component;
import java.awt.Window;
import javax.swing.JOptionPane;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.pref.node.AbstractListEditor;
import org.weasis.dicom.explorer.pref.node.DicomWebNode;

public class HttpHeadersEditor
extends AbstractListEditor<String> {
    private final DicomWebNode node;

    public HttpHeadersEditor(Window parent, DicomWebNode node) {
        super(parent, Messages.getString("DicomWebNodeDialog.httpHeaders"));
        this.node = node;
        this.initializeList();
        this.pack();
    }

    @Override
    protected void initializeList() {
        if (this.node.getHeaders().isEmpty()) {
            this.itemList.setListData(new String[0]);
        } else {
            this.itemList.setListData((String[])this.node.getHeaders().entrySet().stream().map(entry -> (String)entry.getKey() + ": " + (String)entry.getValue()).toArray(String[]::new));
        }
    }

    @Override
    protected void deleteItem(String item) {
        String[] kv = item.split(":", 2);
        if (kv.length == 2) {
            this.node.removeHeader(kv[0]);
        }
    }

    @Override
    protected void modifyItem(String input) {
        String property = (String)JOptionPane.showInputDialog(WinUtil.getValidComponent((Component)this), Messages.getString("HttpHeadersEditor.msg_keyValue"), this.getTitle(), -1, null, null, input);
        if (StringUtil.hasLength((CharSequence)property)) {
            String[] kv = property.split(":", 2);
            if (kv.length == 2) {
                this.node.addHeader(kv[0].trim(), kv[1].trim());
            }
            this.initializeList();
        }
    }
}

