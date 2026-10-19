/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.pref;

import java.awt.GridLayout;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.PageItem;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.pref.GraphicPrefView;
import org.weasis.core.ui.pref.LabelsPrefView;
import org.weasis.core.ui.pref.PreferenceDialog;

public class DrawPrefView
extends AbstractItemDialogPage {
    private final JPanel menuPanel = new JPanel();
    private final JCheckBox checkboxConfirmDeleteMeasurement = new JCheckBox(Messages.getString("DrawPrefView.confirm_delete"));

    public DrawPrefView(PreferenceDialog dialog) {
        super(MeasureTool.BUTTON_NAME, 700);
        this.menuPanel.setLayout(new GridLayout(0, 2));
        this.add(this.menuPanel);
        this.add(GuiUtils.boxVerticalStrut(15));
        this.add(GuiUtils.getFlowLayoutPanel(0, 10, this.checkboxConfirmDeleteMeasurement));
        this.add(GuiUtils.boxVerticalStrut(15));
        this.add(GuiUtils.boxYLastElement(5));
        this.getProperties().setProperty("help.item", "draw-measure");
        this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
        this.initialize();
        List<LabelsPrefView> childPages = List.of(new GraphicPrefView(), new LabelsPrefView());
        childPages.forEach(p -> this.addSubPage((PageItem)p, actionEvent -> dialog.showPage(p.getTitle()), this.menuPanel));
    }

    protected void initialize() {
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        this.checkboxConfirmDeleteMeasurement.setSelected(preferences.getBooleanProperty("weasis.confirm.delete.measurement", true));
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
        GuiUtils.getUICore().getSystemPreferences().putBooleanProperty("weasis.confirm.delete.measurement", this.checkboxConfirmDeleteMeasurement.isSelected());
    }

    @Override
    public void resetToDefaultValues() {
        GuiUtils.getUICore().getSystemPreferences().resetProperty("weasis.confirm.delete.measurement", Boolean.TRUE.toString());
        this.initialize();
    }
}

