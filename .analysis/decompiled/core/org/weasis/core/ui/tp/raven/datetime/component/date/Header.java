/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 */
package org.weasis.core.ui.tp.raven.datetime.component.date;

import java.awt.LayoutManager;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateControlEvent;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateControlListener;

public class Header
extends JPanel {
    protected JButton buttonMonth;
    protected JButton buttonYear;
    protected Icon backIcon;
    protected Icon forwardIcon;

    public Header() {
        this(10, 2023);
    }

    public Header(int month, int year) {
        this.init(month, year);
    }

    private void init(int month, int year) {
        this.putClientProperty("FlatLaf.style", "background:null");
        this.setLayout((LayoutManager)new MigLayout("fill,insets 3", "[]push[][]push[]", "fill"));
        JButton cmdBack = this.createButton();
        JButton cmdNext = this.createButton();
        this.backIcon = ResourceUtil.getIcon(ResourceUtil.ActionIcon.PREVIOUS);
        this.forwardIcon = ResourceUtil.getIcon(ResourceUtil.ActionIcon.NEXT);
        cmdBack.setIcon(this.backIcon);
        cmdNext.setIcon(this.forwardIcon);
        this.buttonMonth = this.createButton();
        this.buttonYear = this.createButton();
        cmdBack.addActionListener(e -> this.fireDateControlChanged(new DateControlEvent(this, 1, 10)));
        cmdNext.addActionListener(e -> this.fireDateControlChanged(new DateControlEvent(this, 1, 11)));
        this.buttonMonth.addActionListener(e -> this.fireDateControlChanged(new DateControlEvent(this, 1, 12)));
        this.buttonYear.addActionListener(e -> this.fireDateControlChanged(new DateControlEvent(this, 1, 13)));
        this.add(cmdBack);
        this.add(this.buttonMonth);
        this.add(this.buttonYear);
        this.add(cmdNext);
        this.setDate(month, year);
    }

    protected JButton createButton() {
        JButton button = new JButton();
        button.putClientProperty("FlatLaf.style", "background:null;arc:10;borderWidth:0;focusWidth:0;innerFocusWidth:0;margin:0,5,0,5");
        return button;
    }

    public void addDateControlListener(DateControlListener listener) {
        this.listenerList.add(DateControlListener.class, listener);
    }

    public void removeDateControlListener(DateControlListener listener) {
        this.listenerList.remove(DateControlListener.class, listener);
    }

    public void fireDateControlChanged(DateControlEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != DateControlListener.class) continue;
            ((DateControlListener)listeners[i + 1]).dateControlChanged(event);
        }
    }

    public void setDate(int month, int year) {
        this.buttonMonth.setText(DatePicker.getDefaultMonths()[month]);
        this.buttonYear.setText("" + year);
    }

    public Icon getBackIcon() {
        return this.backIcon;
    }

    public void setBackIcon(Icon backIcon) {
        this.backIcon = backIcon;
    }

    public Icon getForwardIcon() {
        return this.forwardIcon;
    }

    public void setForwardIcon(Icon forwardIcon) {
        this.forwardIcon = forwardIcon;
    }
}

