/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 */
package org.weasis.core.ui.tp.raven.datetime;

import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.tp.raven.datetime.TimeSelectionAble;
import org.weasis.core.ui.tp.raven.datetime.component.PanelPopupEditor;
import org.weasis.core.ui.tp.raven.datetime.component.time.Header;
import org.weasis.core.ui.tp.raven.datetime.component.time.PanelClock;
import org.weasis.core.ui.tp.raven.datetime.component.time.TimeSelectionModel;
import org.weasis.core.ui.tp.raven.datetime.component.time.event.TimeActionListener;
import org.weasis.core.ui.tp.raven.datetime.component.time.event.TimeSelectionModelEvent;
import org.weasis.core.ui.tp.raven.datetime.component.time.event.TimeSelectionModelListener;
import org.weasis.core.ui.tp.raven.datetime.event.TimeSelectionEvent;
import org.weasis.core.ui.tp.raven.datetime.event.TimeSelectionListener;
import org.weasis.core.ui.tp.raven.datetime.util.InputUtils;
import org.weasis.core.ui.tp.raven.datetime.util.InputValidationListener;

public class TimePicker
extends PanelPopupEditor
implements TimeSelectionModelListener {
    private final DateTimeFormatter format12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private final DateTimeFormatter format24h = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private TimeSelectionModel timeSelectionModel;
    private TimeSelectionListener timeSelectionListener;
    private InputValidationListener<LocalTime> inputValidationListener;
    private InputUtils.ValueCallback valueCallback;
    private Icon editorIcon;
    private MigLayout layout;
    private int orientation = 1;
    private Color color;
    private JButton editorButton;
    private LocalTime oldSelectedTime;
    private Header header;
    private PanelClock panelClock;

    public TimePicker() {
        this((TimeSelectionModel)null);
    }

    public TimePicker(TimeSelectionModel model) {
        this.init(model);
    }

    private void init(TimeSelectionModel model) {
        this.putClientProperty("FlatLaf.style", "[light]background:darken($Panel.background,2%);[dark]background:lighten($Panel.background,2%);");
        this.layout = new MigLayout("wrap,fill,insets 3", "fill", "fill");
        this.setLayout((LayoutManager)this.layout);
        if (model == null) {
            model = this.createDefaultTimeSelection();
        }
        this.setTimeSelectionModel(model);
        this.header = new Header(this, new HeaderActionListener(this));
        this.panelClock = new PanelClock(this, new ClockActionListener(this));
        this.add((Component)this.header, "width 120:120");
        this.add((Component)this.panelClock, "width 230:230,height 230:230");
        JPanel buttonPanel = this.createButtonPanel();
        this.add((Component)buttonPanel, "dock south");
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setOrientation(int orientation) {
        if (this.orientation != orientation) {
            String c = orientation == 1 ? "wrap," : "";
            this.layout.setLayoutConstraints((Object)(c + "fill,insets 3"));
            this.orientation = orientation;
            this.header.setOrientation(orientation);
            this.revalidate();
        }
    }

    public void setEditor(JFormattedTextField editor) {
        if (editor != this.editor) {
            if (this.editor != null) {
                this.uninstallEditor(this.editor);
            }
            this.editor = editor;
            if (editor != null) {
                this.installEditor(editor);
                if (this.editorValidation) {
                    this.validChanged(editor, this.isValid);
                } else {
                    this.validChanged(editor, true);
                }
            }
        }
    }

    public boolean is24HourView() {
        return this.panelClock.isUse24hour();
    }

    public void set24HourView(boolean hour24) {
        if (this.panelClock.isUse24hour() != hour24) {
            this.panelClock.setUse24hour(hour24);
            this.header.setUse24hour(hour24);
            this.panelClock.updateClock();
            this.header.updateHeader();
            this.repaint();
            if (this.editor != null) {
                InputUtils.changeTimeFormatted(this.editor, hour24, this.getInputValidationListener());
                this.defaultPlaceholder = null;
                this.setEditorValue();
            }
        }
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.header.setColor(color);
        this.panelClock.setColor(color);
    }

    public Icon getEditorIcon() {
        return this.editorIcon;
    }

    public void setEditorIcon(Icon editorIcon) {
        this.editorIcon = editorIcon;
        if (this.editorButton != null) {
            this.editorButton.setIcon(editorIcon);
        }
    }

    public void now() {
        this.setSelectedTime(LocalTime.now(ZoneId.systemDefault()));
    }

    public void setSelectedTime(LocalTime time) {
        if (time == null) {
            this.clearSelectedTime();
        } else {
            int hour = time.getHour();
            int minute = time.getMinute();
            this.timeSelectionModel.set(hour, minute);
        }
    }

    public void clearSelectedTime() {
        this.timeSelectionModel.set(-1, -1);
    }

    public boolean isTimeSelected() {
        return this.timeSelectionModel.isSelected();
    }

    public LocalTime getSelectedTime() {
        return this.timeSelectionModel.getTime();
    }

    public String getSelectedTimeAsString() {
        if (this.isTimeSelected()) {
            if (this.panelClock.isUse24hour()) {
                return this.format24h.format(this.getSelectedTime());
            }
            return this.format12h.format(this.getSelectedTime());
        }
        return null;
    }

    protected TimeSelectionModel createDefaultTimeSelection() {
        return new TimeSelectionModel();
    }

    public TimeSelectionAble getTimeSelectionAble() {
        return this.timeSelectionModel.getTimeSelectionAble();
    }

    public void setTimeSelectionAble(TimeSelectionAble timeSelectionAble) {
        this.timeSelectionModel.setTimeSelectionAble(timeSelectionAble);
        this.commitEdit();
    }

    public TimeSelectionModel getTimeSelectionModel() {
        return this.timeSelectionModel;
    }

    public void setTimeSelectionModel(TimeSelectionModel timeSelectionModel) {
        if (timeSelectionModel == null) {
            throw new IllegalArgumentException("timeSelectionModel can't be null");
        }
        if (this.timeSelectionModel != timeSelectionModel) {
            TimeSelectionModel old = this.timeSelectionModel;
            if (old != null) {
                old.removeTimePickerSelectionListener(this);
            }
            this.timeSelectionModel = timeSelectionModel;
            this.timeSelectionModel.addTimePickerSelectionListener(this);
        }
    }

    public boolean isHourSelectionView() {
        return this.panelClock.isHourSelectionView();
    }

    public void setHourSelectionView(boolean hourSelectionView) {
        this.header.setHourSelectionView(hourSelectionView);
        this.panelClock.setHourSelectionViewImmediately(hourSelectionView);
    }

    public void addTimeSelectionListener(TimeSelectionListener listener) {
        this.listenerList.add(TimeSelectionListener.class, listener);
    }

    public void removeTimeSelectionListener(TimeSelectionListener listener) {
        this.listenerList.remove(TimeSelectionListener.class, listener);
    }

    private void installEditor(JFormattedTextField editor) {
        JToolBar toolBar = new JToolBar();
        this.editorButton = new JButton(this.editorIcon != null ? this.editorIcon : ResourceUtil.getIcon(ResourceUtil.OtherIcon.CLOCK));
        toolBar.add(this.editorButton);
        this.editorButton.addActionListener(e -> {
            if (editor.isEnabled()) {
                editor.grabFocus();
                this.showPopup();
            }
        });
        InputUtils.useTimeInput(editor, this.panelClock.isUse24hour(), this.getValueCallback(), this.getInputValidationListener());
        this.setEditorValue();
        editor.putClientProperty("JTextField.trailingComponent", toolBar);
        this.addTimeSelectionListener(this.getTimeSelectionListener());
    }

    private void uninstallEditor(JFormattedTextField editor) {
        if (editor != null) {
            this.editorButton = null;
            InputUtils.removePropertyChange(editor);
            if (this.timeSelectionListener != null) {
                this.removeTimeSelectionListener(this.timeSelectionListener);
            }
        }
    }

    private InputUtils.ValueCallback getValueCallback() {
        if (this.valueCallback == null) {
            this.valueCallback = value -> {
                LocalTime time;
                if (value == null && this.isTimeSelected()) {
                    this.clearSelectedTime();
                } else if (value != null && !value.equals(this.getSelectedTimeAsString()) && (time = InputUtils.stringToTime(this.panelClock.isUse24hour(), value.toString())) != null) {
                    this.setSelectedTime(time);
                }
            };
        }
        return this.valueCallback;
    }

    private TimeSelectionListener getTimeSelectionListener() {
        if (this.timeSelectionListener == null) {
            this.timeSelectionListener = timeSelectionEvent -> this.setEditorValue();
        }
        return this.timeSelectionListener;
    }

    private void setEditorValue() {
        String value = this.getSelectedTimeAsString();
        if (value != null) {
            if (!this.editor.getText().equalsIgnoreCase(value)) {
                this.editor.setValue(value);
            }
        } else {
            this.editor.setValue(null);
        }
    }

    private InputValidationListener<LocalTime> getInputValidationListener() {
        if (this.inputValidationListener == null) {
            this.inputValidationListener = new InputValidationListener<LocalTime>(this){
                final /* synthetic */ TimePicker this$0;
                {
                    TimePicker timePicker = this$0;
                    Objects.requireNonNull(timePicker);
                    this.this$0 = timePicker;
                }

                @Override
                public boolean isValidation() {
                    return this.this$0.timeSelectionModel.getTimeSelectionAble() != null;
                }

                @Override
                public void inputChanged(boolean status) {
                    this.this$0.checkValidation(status);
                }

                @Override
                public boolean checkSelectionAble(LocalTime time) {
                    int hour = time.getHour();
                    int minute = time.getMinute();
                    return this.this$0.timeSelectionModel.checkSelection(hour, minute);
                }
            };
        }
        return this.inputValidationListener;
    }

    @Override
    protected String getDefaultPlaceholder() {
        if (this.defaultPlaceholder == null) {
            String pattern = this.is24HourView() ? "--:--" : "--:-- --";
            this.defaultPlaceholder = pattern;
        }
        return this.defaultPlaceholder;
    }

    @Override
    protected void popupOpen() {
        this.setHourSelectionView(true);
    }

    private void verifyTimeSelection() {
        LocalTime time = this.getSelectedTime();
        if (time == null && this.oldSelectedTime == null) {
            return;
        }
        if (time != null && this.oldSelectedTime != null && time.equals(this.oldSelectedTime)) {
            return;
        }
        this.oldSelectedTime = time;
        this.fireTimeSelectionChanged(new TimeSelectionEvent(this));
    }

    public void fireTimeSelectionChanged(TimeSelectionEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != TimeSelectionListener.class) continue;
            ((TimeSelectionListener)listeners[i + 1]).timeSelected(event);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.header.setEnabled(enabled);
        this.panelClock.setEnabled(enabled);
    }

    public Header getHeader() {
        return this.header;
    }

    @Override
    public void timeSelectionModelChanged(TimeSelectionModelEvent e) {
        if (!this.timeSelectionModel.isSelected() && e.getAction() == 3) {
            this.panelClock.setHourSelectionView(true);
            this.header.setHourSelectionView(true);
        }
        this.panelClock.updateClock();
        this.header.updateHeader();
        this.repaint();
        this.verifyTimeSelection();
    }

    private class HeaderActionListener
    implements TimeActionListener {
        final /* synthetic */ TimePicker this$0;

        private HeaderActionListener(TimePicker timePicker) {
            TimePicker timePicker2 = timePicker;
            Objects.requireNonNull(timePicker2);
            this.this$0 = timePicker2;
        }

        @Override
        public void selectionViewChanged(boolean isHourSelectionView) {
            this.this$0.panelClock.setHourSelectionView(isHourSelectionView);
        }
    }

    private class ClockActionListener
    implements TimeActionListener {
        final /* synthetic */ TimePicker this$0;

        private ClockActionListener(TimePicker timePicker) {
            TimePicker timePicker2 = timePicker;
            Objects.requireNonNull(timePicker2);
            this.this$0 = timePicker2;
        }

        @Override
        public void selectionViewChanged(boolean isHourSelectionView) {
            if (isHourSelectionView) {
                this.this$0.panelClock.setHourSelectionView(false);
                this.this$0.header.setHourSelectionView(false);
            }
        }
    }
}

