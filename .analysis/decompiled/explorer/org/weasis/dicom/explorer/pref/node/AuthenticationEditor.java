/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.api.net.auth.OAuth2ServiceFactory
 */
package org.weasis.dicom.explorer.pref.node;

import java.awt.Window;
import java.util.ArrayList;
import javax.swing.JComboBox;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.OAuth2ServiceFactory;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.pref.node.AbstractListEditor;
import org.weasis.dicom.explorer.pref.node.AuthMethodDialog;
import org.weasis.dicom.explorer.pref.node.AuthenticationPersistence;

public class AuthenticationEditor
extends AbstractListEditor<AuthMethod> {
    private final JComboBox<AuthMethod> comboBox;

    public AuthenticationEditor(Window parent, JComboBox<AuthMethod> comboBox) {
        super(parent, Messages.getString("authentication.manager"));
        this.comboBox = comboBox;
        this.initializeList();
        this.pack();
    }

    @Override
    protected void initializeList() {
        ArrayList<AuthMethod> list = new ArrayList<AuthMethod>();
        for (int i = 0; i < this.comboBox.getItemCount(); ++i) {
            AuthMethod auth = this.comboBox.getItemAt(i);
            if (OAuth2ServiceFactory.NO_AUTH.equals(auth)) continue;
            list.add(auth);
        }
        this.itemList.setListData(list.toArray(new AuthMethod[0]));
    }

    @Override
    protected void deleteItem(AuthMethod item) {
        this.comboBox.removeItem(item);
        AuthenticationPersistence.removeMethod(item);
    }

    @Override
    protected void modifyItem(AuthMethod input) {
        AuthMethodDialog dialog = new AuthMethodDialog((Window)this, Messages.getString("auth.method"), input, this.comboBox);
        GuiUtils.showCenterScreen((Window)dialog);
        this.initializeList();
    }
}

