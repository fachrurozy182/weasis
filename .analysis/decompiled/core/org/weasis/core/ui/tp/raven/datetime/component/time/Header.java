/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 */
package org.weasis.core.ui.tp.raven.datetime.component.time;

import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Locale;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.ui.tp.raven.datetime.TimePicker;
import org.weasis.core.ui.tp.raven.datetime.component.time.TimeSelectionModel;
import org.weasis.core.ui.tp.raven.datetime.component.time.event.TimeActionListener;

public class Header
extends JPanel {
    private final TimePicker timePicker;
    private final TimeActionListener timeActionListener;
    private final DecimalFormat format = new DecimalFormat("00");
    private MigLayout layout;
    private Color color;
    private JToggleButton buttonHour;
    private JToggleButton buttonMinute;
    private JToolBar amPmToolBar;
    private JButton buttonAm;
    private JButton buttonPm;

    public Header(TimePicker timePicker, TimeActionListener timeActionListener) {
        this.timePicker = timePicker;
        this.timeActionListener = timeActionListener;
        this.init();
    }

    private void init() {
        this.setOpaque(true);
        this.layout = new MigLayout("fill,insets 10", "center");
        this.setLayout((LayoutManager)this.layout);
        this.add((Component)this.createToolBar(), "id b1");
        this.add((Component)this.createAmPm(), "pos b1.x2+rel 0.5al n n");
    }

    public void updateHeader() {
        int hour = this.timePicker.getTimeSelectionModel().getHour();
        int minute = this.timePicker.getTimeSelectionModel().getMinute();
        if (hour == -1 && minute == -1) {
            this.buttonAm.setSelected(false);
            this.buttonPm.setSelected(false);
        } else if (hour >= 12) {
            this.setSelectedAm(false);
        } else {
            this.setSelectedAm(true);
        }
        if (!this.timePicker.is24HourView()) {
            if (hour >= 12) {
                hour -= 12;
            }
            if (hour == 0) {
                hour = 12;
            }
        }
        String hourText = hour == -1 ? "--" : this.format.format(hour);
        String minuteText = minute == -1 ? "--" : this.format.format(minute);
        this.buttonHour.setText(hourText);
        this.buttonMinute.setText(minuteText);
    }

    public void setOrientation(int orientation) {
        String c = orientation == 1 ? "pos b1.x2+rel 0.5al n n" : "pos 0.5al b1.y2+rel n n";
        this.amPmToolBar.setOrientation(orientation);
        this.layout.setComponentConstraints((Component)this.amPmToolBar, (Object)c);
    }

    public boolean isAm() {
        return !this.buttonPm.isSelected();
    }

    public void setHourSelectionView(boolean hourSelectionView) {
        if (hourSelectionView) {
            this.buttonHour.setSelected(true);
        } else {
            this.buttonMinute.setSelected(true);
        }
    }

    public void setUse24hour(boolean use24hour) {
        this.amPmToolBar.setVisible(!use24hour);
    }

    protected JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.putClientProperty("FlatLaf.style", "background:null;hoverButtonGroupBackground:null;");
        this.buttonHour = this.createButton();
        this.buttonMinute = this.createButton();
        ButtonGroup group = new ButtonGroup();
        group.add(this.buttonHour);
        group.add(this.buttonMinute);
        this.buttonHour.setSelected(true);
        this.buttonHour.addActionListener(e -> this.timeActionListener.selectionViewChanged(true));
        this.buttonMinute.addActionListener(e -> this.timeActionListener.selectionViewChanged(false));
        toolBar.add(this.buttonHour);
        toolBar.add(this.createSplit());
        toolBar.add(this.buttonMinute);
        return toolBar;
    }

    protected JToggleButton createButton() {
        JToggleButton button = new JToggleButton("--");
        button.putClientProperty("FlatLaf.style", "font:+15;toolbar.margin:3,5,3,5;foreground:contrast($Component.accentColor,$ToggleButton.background,#fff);background:null;toolbar.hoverBackground:null");
        return button;
    }

    protected JButton createAmPmButton(boolean isAm) {
        String[] amPM = DateFormatSymbols.getInstance(Locale.ENGLISH).getAmPmStrings();
        String amOrPm = isAm ? amPM[0] : amPM[1];
        JButton button = new JButton(amOrPm);
        button.addActionListener(e -> this.actionAmPmChanged(isAm));
        button.putClientProperty("FlatLaf.style", "font:+1;foreground:contrast($Component.accentColor,$ToggleButton.background,#fff);background:null;toolbar.hoverBackground:null");
        return button;
    }

    private void actionAmPmChanged(boolean isAm) {
        TimeSelectionModel timeSelectionModel = this.timePicker.getTimeSelectionModel();
        int hour = timeSelectionModel.getHour();
        int minute = timeSelectionModel.getMinute();
        if (hour == -1 && minute == -1) {
            this.setSelectedAm(isAm);
            this.timePicker.repaint();
        } else {
            if (isAm) {
                if (hour >= 12) {
                    hour -= 12;
                }
            } else if (hour < 12) {
                hour += 12;
            }
            timeSelectionModel.set(hour, minute);
        }
    }

    private void setSelectedAm(boolean isAm) {
        this.buttonAm.setSelected(isAm);
        this.buttonPm.setSelected(!isAm);
    }

    protected JLabel createSplit() {
        JLabel label = new JLabel(":");
        label.putClientProperty("FlatLaf.style", "font:+10;foreground:contrast($Component.accentColor,$Label.background,#fff)");
        return label;
    }

    protected JToolBar createAmPm() {
        this.amPmToolBar = new JToolBar();
        this.amPmToolBar.setOrientation(1);
        this.amPmToolBar.putClientProperty("FlatLaf.style", "background:null;hoverButtonGroupBackground:null");
        this.buttonAm = this.createAmPmButton(true);
        this.buttonPm = this.createAmPmButton(false);
        this.amPmToolBar.add(this.buttonAm);
        this.amPmToolBar.add(this.buttonPm);
        return this.amPmToolBar;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Color getBackground() {
        if (this.color != null) {
            return this.color;
        }
        return UIManager.getColor("Component.accentColor");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.buttonAm.setEnabled(enabled);
        this.buttonPm.setEnabled(enabled);
    }
}

