/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.pref;

import java.awt.GridLayout;
import javax.swing.JPanel;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.PageItem;

public class DicomPrefView
extends AbstractItemDialogPage {
    private final JPanel menuPanel = new JPanel();

    public DicomPrefView() {
        super("DICOM", 600);
        this.menuPanel.setLayout(new GridLayout(0, 2));
        this.add(this.menuPanel);
        this.add(GuiUtils.boxVerticalStrut(15));
        this.add(GuiUtils.boxYLastElement(5));
    }

    @Override
    public JPanel getMenuPanel() {
        return this.menuPanel;
    }

    @Override
    public void closeAdditionalWindow() {
        for (PageItem subpage : this.getSubPages()) {
            subpage.closeAdditionalWindow();
        }
    }

    @Override
    public void resetToDefaultValues() {
    }
}

