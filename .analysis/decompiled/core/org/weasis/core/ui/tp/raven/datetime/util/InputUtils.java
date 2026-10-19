/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.FlatClientProperties
 */
package org.weasis.core.ui.tp.raven.datetime.util;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import org.weasis.core.ui.tp.raven.datetime.util.InputValidationListener;

public class InputUtils
extends MaskFormatter {
    private static Map<Component, OldEditorProperty> inputMap;

    public static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalTime stringToTime(boolean use24h, String value) {
        try {
            if (use24h) {
                DateTimeFormatter format24h = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
                return LocalTime.from(format24h.parse(value));
            }
            DateTimeFormatter format12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
            return LocalTime.from(format12h.parse(value.toUpperCase()));
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static LocalDate stringToDate(DateTimeFormatter format, String value) {
        try {
            return LocalDate.from(format.parse(value));
        }
        catch (Exception e) {
            return null;
        }
    }

    public static LocalDate[] stringToDate(DateTimeFormatter format, String separator, String value) {
        try {
            String[] dates = value.split(separator);
            LocalDate from = LocalDate.from(format.parse(dates[0]));
            LocalDate to = LocalDate.from(format.parse(dates[1]));
            return new LocalDate[]{from, to};
        }
        catch (Exception e) {
            return null;
        }
    }

    public static void useTimeInput(JFormattedTextField txt, boolean use24h, ValueCallback callback, InputValidationListener<LocalTime> inputValidationListener) {
        try {
            TimeInputFormat mask = new TimeInputFormat(use24h ? "##:##" : "##:## ??", use24h, inputValidationListener);
            OldEditorProperty oldEditorProperty = InputUtils.initEditor(txt, mask, callback);
            PropertyChangeListener propertyChangeListener = evt -> callback.valueChanged(txt.getValue());
            txt.addPropertyChangeListener("value", propertyChangeListener);
            oldEditorProperty.propertyChangeListener = propertyChangeListener;
            InputUtils.putPropertyChange(txt, oldEditorProperty);
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void useDateInput(JFormattedTextField txt, String pattern, boolean between, String separator, ValueCallback callback, InputValidationListener<LocalDate> inputValidationListener) {
        try {
            String format = InputUtils.datePatternToInputFormat(pattern, "#");
            DateInputFormat mask = new DateInputFormat((String)(between ? format + separator + format : format), between, separator, pattern, inputValidationListener);
            OldEditorProperty oldEditorProperty = InputUtils.initEditor(txt, mask, callback);
            if (callback != null) {
                PropertyChangeListener propertyChangeListener = evt -> callback.valueChanged(txt.getValue());
                txt.addPropertyChangeListener("value", propertyChangeListener);
                oldEditorProperty.propertyChangeListener = propertyChangeListener;
            }
            InputUtils.putPropertyChange(txt, oldEditorProperty);
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void changeTimeFormatted(JFormattedTextField txt, boolean use24h, InputValidationListener<LocalTime> inputValidationListener) {
        try {
            TimeInputFormat mask = new TimeInputFormat(use24h ? "##:##" : "##:## ??", use24h, inputValidationListener);
            mask.setCommitsOnValidEdit(true);
            mask.setPlaceholderCharacter('-');
            DefaultFormatterFactory df = new DefaultFormatterFactory(mask);
            txt.setFormatterFactory(df);
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void changeDateFormatted(JFormattedTextField txt, String pattern, boolean between, String separator, InputValidationListener<LocalDate> inputValidationListener) {
        try {
            String format = InputUtils.datePatternToInputFormat(pattern, "#");
            DateInputFormat mask = new DateInputFormat((String)(between ? format + separator + format : format), between, separator, pattern, inputValidationListener);
            mask.setCommitsOnValidEdit(true);
            mask.setPlaceholderCharacter('-');
            DefaultFormatterFactory df = new DefaultFormatterFactory(mask);
            txt.setFormatterFactory(df);
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    public static String datePatternToInputFormat(String pattern, String rpm) {
        String regex = "[dmy]";
        return pattern.toLowerCase().replaceAll(regex, rpm);
    }

    private static OldEditorProperty initEditor(JFormattedTextField txt, MaskFormatter format, ValueCallback callback) {
        InputUtils.removePropertyChange(txt);
        OldEditorProperty oldEditorProperty = OldEditorProperty.getFromOldEditor(txt);
        format.setCommitsOnValidEdit(true);
        format.setPlaceholderCharacter('-');
        DefaultFormatterFactory df = new DefaultFormatterFactory(format);
        txt.setFormatterFactory(df);
        txt.putClientProperty("JTextField.showClearButton", true);
        txt.putClientProperty("JTextField.clearCallback", o -> {
            txt.setValue(null);
            if (callback != null) {
                callback.valueChanged(null);
            }
        });
        return oldEditorProperty;
    }

    private static void putPropertyChange(JFormattedTextField txt, OldEditorProperty oldEditorProperty) {
        if (inputMap == null) {
            inputMap = new HashMap<Component, OldEditorProperty>();
        }
        inputMap.put(txt, oldEditorProperty);
    }

    public static void removePropertyChange(JFormattedTextField txt) {
        if (inputMap == null) {
            return;
        }
        OldEditorProperty oldEditorProperty = inputMap.get(txt);
        if (oldEditorProperty != null) {
            oldEditorProperty.removeFromEditor(txt);
            inputMap.remove(txt);
        }
    }

    private static class TimeInputFormat
    extends MaskFormatter {
        private final InputValidationListener<LocalTime> inputValidationListener;
        private final DateTimeFormatter timeFormat;

        public TimeInputFormat(String mark, boolean use24h, InputValidationListener<LocalTime> inputValidationListener) throws ParseException {
            super(mark);
            this.inputValidationListener = inputValidationListener;
            this.timeFormat = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(use24h ? "HH:mm" : "hh:mm a").toFormatter(Locale.ENGLISH);
        }

        @Override
        public Object stringToValue(String value) throws ParseException {
            this.checkTime(value);
            return super.stringToValue(value);
        }

        public void checkTime(String value) throws ParseException {
            try {
                LocalTime time = LocalTime.parse(value, this.timeFormat);
                if (this.inputValidationListener == null) {
                    return;
                }
                if (this.inputValidationListener.isValidation() && !this.inputValidationListener.checkSelectionAble(time)) {
                    throw new DateTimeException("error selection able");
                }
                this.inputValidationListener.inputChanged(true);
            }
            catch (DateTimeException e) {
                if (this.inputValidationListener != null) {
                    this.inputValidationListener.inputChanged(false);
                }
                throw new ParseException(e.getMessage(), 0);
            }
        }
    }

    public static interface ValueCallback {
        public void valueChanged(Object var1);
    }

    private static class OldEditorProperty {
        protected PropertyChangeListener propertyChangeListener;
        protected JFormattedTextField.AbstractFormatterFactory formatter;
        protected Component oldTrailingComponent;
        private boolean isShowClearButton;
        private Consumer<?> clearButtonCallback;
        private String outline;
        protected Object value;
        protected String text;

        private OldEditorProperty() {
        }

        protected static OldEditorProperty getFromOldEditor(JFormattedTextField editor) {
            OldEditorProperty oldEditorProperty = new OldEditorProperty();
            oldEditorProperty.formatter = editor.getFormatterFactory();
            oldEditorProperty.oldTrailingComponent = (Component)FlatClientProperties.clientProperty((JComponent)editor, (String)"JTextField.trailingComponent", null, Component.class);
            oldEditorProperty.isShowClearButton = FlatClientProperties.clientPropertyBoolean((JComponent)editor, (String)"JTextField.showClearButton", (boolean)false);
            oldEditorProperty.clearButtonCallback = (Consumer)FlatClientProperties.clientProperty((JComponent)editor, (String)"JTextField.clearCallback", null, Consumer.class);
            oldEditorProperty.outline = (String)FlatClientProperties.clientProperty((JComponent)editor, (String)"JComponent.outline", null, String.class);
            oldEditorProperty.value = editor.getValue();
            oldEditorProperty.text = editor.getText();
            return oldEditorProperty;
        }

        protected void removeFromEditor(JFormattedTextField editor) {
            editor.removePropertyChangeListener("value", this.propertyChangeListener);
            editor.setFormatterFactory(this.formatter);
            editor.putClientProperty("JTextField.trailingComponent", this.oldTrailingComponent);
            editor.putClientProperty("JTextField.showClearButton", this.isShowClearButton);
            editor.putClientProperty("JTextField.showClearButton", this.clearButtonCallback);
            editor.putClientProperty("JComponent.outline", this.outline);
            editor.setValue(this.value);
            editor.setText(this.text);
        }
    }

    private static class DateInputFormat
    extends MaskFormatter {
        private final boolean between;
        private final String separator;
        private final DateFormat dateFormat;
        private final InputValidationListener<LocalDate> inputValidationListener;

        public DateInputFormat(String mark, boolean between, String separator, String pattern, InputValidationListener<LocalDate> inputValidationListener) throws ParseException {
            super(mark);
            this.between = between;
            this.separator = separator;
            this.dateFormat = new SimpleDateFormat(pattern);
            this.inputValidationListener = inputValidationListener;
            this.dateFormat.setLenient(false);
        }

        @Override
        public Object stringToValue(String value) throws ParseException {
            this.checkTime(value);
            return super.stringToValue(value);
        }

        public void checkTime(String value) throws ParseException {
            try {
                if (this.between) {
                    String[] values = value.split(this.separator);
                    Date fromDate = this.dateFormat.parse(values[0]);
                    Date toDate = this.dateFormat.parse(values[1]);
                    if (this.inputValidationListener == null) {
                        return;
                    }
                    if (this.inputValidationListener.isValidation()) {
                        LocalDate date1 = InputUtils.dateToLocalDate(fromDate);
                        LocalDate date2 = InputUtils.dateToLocalDate(toDate);
                        if (!this.inputValidationListener.checkSelectionAble(date1) || !this.inputValidationListener.checkSelectionAble(date2)) {
                            throw new ParseException("error selection able", 0);
                        }
                    }
                } else {
                    LocalDate d;
                    Date date = this.dateFormat.parse(value);
                    if (this.inputValidationListener == null) {
                        return;
                    }
                    if (this.inputValidationListener.isValidation() && !this.inputValidationListener.checkSelectionAble(d = InputUtils.dateToLocalDate(date))) {
                        throw new ParseException("error selection able", 0);
                    }
                }
                this.inputValidationListener.inputChanged(true);
            }
            catch (ParseException e) {
                if (this.inputValidationListener != null) {
                    this.inputValidationListener.inputChanged(false);
                }
                throw e;
            }
        }
    }
}

