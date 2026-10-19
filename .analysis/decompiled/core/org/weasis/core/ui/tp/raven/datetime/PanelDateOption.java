/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 */
package org.weasis.core.ui.tp.raven.datetime;

import java.awt.Component;
import java.awt.LayoutManager;
import java.time.LocalDate;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.PanelDateOptionLabel;

public class PanelDateOption
extends JPanel {
    private final DatePicker datePicker;
    private boolean disableChange;
    private ButtonGroup buttonGroup;

    public PanelDateOption(DatePicker datePicker) {
        this.datePicker = datePicker;
        this.init();
    }

    private void init() {
        this.putClientProperty("FlatLaf.style", "background:null");
        this.buttonGroup = new ButtonGroup();
    }

    public void installDateOptionLabel() {
        this.removeAll();
        PanelDateOptionLabel panelDateOptionLabel = this.datePicker.getPanelDateOptionLabel();
        if (panelDateOptionLabel == null) {
            panelDateOptionLabel = this.createDefaultPanelDateOptionLabel();
        }
        Object layoutRowConstraints = "";
        List<PanelDateOptionLabel.Item> items = panelDateOptionLabel.getListItems();
        for (int i = 0; i < items.size(); ++i) {
            PanelDateOptionLabel.Item item = items.get(i);
            this.add(this.createButton(item.getLabel(), item.getCallback()));
            layoutRowConstraints = item.getCallback() == null ? (String)layoutRowConstraints + "push" : (String)layoutRowConstraints + "[]";
        }
        layoutRowConstraints = (String)layoutRowConstraints + "[]";
        this.setLayout((LayoutManager)new MigLayout("wrap,insets 5,fillx", "[fill]", (String)layoutRowConstraints));
        this.add(new JSeparator(1), "dock west,height 100%", 0);
        this.repaint();
        this.revalidate();
    }

    private JToggleButton createButton(String name, PanelDateOptionLabel.LabelCallback callback) {
        JToggleButton button = new JToggleButton(name);
        button.setHorizontalAlignment(10);
        if (callback == null) {
            button.setName("custom");
        }
        button.addActionListener(e -> {
            this.disableChange = true;
            boolean isEnable = this.datePicker.isEnabled();
            if (callback == null) {
                if (isEnable) {
                    this.datePicker.clearSelectedDate();
                }
            } else {
                LocalDate[] dates = callback.getDate();
                if (dates.length == 0) {
                    throw new IllegalArgumentException("Date option is empty so can't be select");
                }
                boolean singleDate = DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED.equals((Object)this.datePicker.getDateSelectionMode());
                if (isEnable) {
                    if (singleDate) {
                        this.datePicker.setSelectedDate(dates[0]);
                    } else {
                        if (dates.length < 2) {
                            dates = new LocalDate[]{dates[0], dates[0]};
                        }
                        this.datePicker.setSelectedDateRange(dates[0], dates[1]);
                    }
                } else {
                    this.datePicker.slideTo(dates[0]);
                }
            }
        });
        button.putClientProperty("FlatLaf.style", "arc:10;borderWidth:0;focusWidth:0;innerFocusWidth:0;margin:4,10,4,10;background:null");
        this.buttonGroup.add(button);
        return button;
    }

    public void setSelectedCustom() {
        if (!this.disableChange) {
            AbstractButton customButton = null;
            for (Component com : this.getComponents()) {
                String name = com.getName();
                if (name == null || !name.equals("custom")) continue;
                customButton = (JToggleButton)com;
                break;
            }
            if (customButton != null) {
                customButton.setSelected(true);
            }
        }
        this.disableChange = false;
    }

    private PanelDateOptionLabel createDefaultPanelDateOptionLabel() {
        PanelDateOptionLabel defaultPanelDateOptionLabel = new PanelDateOptionLabel();
        defaultPanelDateOptionLabel.add("Today", PanelDateOptionLabel.LabelCallback.TODAY);
        defaultPanelDateOptionLabel.add("Yesterday", PanelDateOptionLabel.LabelCallback.YESTERDAY);
        defaultPanelDateOptionLabel.add("Last 7 Days", PanelDateOptionLabel.LabelCallback.LAST_7_DAYS);
        defaultPanelDateOptionLabel.add("Last 30 Days", PanelDateOptionLabel.LabelCallback.LAST_30_DAYS);
        defaultPanelDateOptionLabel.add("This Month", PanelDateOptionLabel.LabelCallback.THIS_MONTH);
        defaultPanelDateOptionLabel.add("Last Month", PanelDateOptionLabel.LabelCallback.LAST_MONTH);
        defaultPanelDateOptionLabel.add("Last Year", PanelDateOptionLabel.LabelCallback.LAST_YEAR);
        defaultPanelDateOptionLabel.add("Custom", PanelDateOptionLabel.LabelCallback.CUSTOM);
        return defaultPanelDateOptionLabel;
    }
}

