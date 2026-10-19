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
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.tp.raven.datetime.DateSelectionAble;
import org.weasis.core.ui.tp.raven.datetime.PanelDateOption;
import org.weasis.core.ui.tp.raven.datetime.PanelDateOptionLabel;
import org.weasis.core.ui.tp.raven.datetime.component.PanelPopupEditor;
import org.weasis.core.ui.tp.raven.datetime.component.date.DateSelectionModel;
import org.weasis.core.ui.tp.raven.datetime.component.date.DefaultDateCellRenderer;
import org.weasis.core.ui.tp.raven.datetime.component.date.Header;
import org.weasis.core.ui.tp.raven.datetime.component.date.PanelDate;
import org.weasis.core.ui.tp.raven.datetime.component.date.PanelMonth;
import org.weasis.core.ui.tp.raven.datetime.component.date.PanelYear;
import org.weasis.core.ui.tp.raven.datetime.component.date.SingleDate;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateControlEvent;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateControlListener;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateSelectionModelEvent;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateSelectionModelListener;
import org.weasis.core.ui.tp.raven.datetime.event.DateSelectionEvent;
import org.weasis.core.ui.tp.raven.datetime.event.DateSelectionListener;
import org.weasis.core.ui.tp.raven.datetime.util.InputUtils;
import org.weasis.core.ui.tp.raven.datetime.util.InputValidationListener;
import org.weasis.core.ui.tp.raven.slider.PanelSlider;
import org.weasis.core.ui.tp.raven.slider.SimpleTransition;
import org.weasis.core.ui.tp.raven.slider.SliderTransition;

public class DatePicker
extends PanelPopupEditor
implements DateSelectionModelListener,
DateControlListener,
ChangeListener {
    private static String[] defaultWeekdays = null;
    private static String[] defaultMonths = null;
    private DateTimeFormatter format;
    private String dateFormatPattern = "dd/MM/yyyy";
    private DateSelectionListener dateSelectionListener;
    private InputValidationListener<LocalDate> inputValidationListener;
    private DateSelectionModel dateSelectionModel;
    private PanelDateOption panelDateOption;
    private PanelDateOptionLabel panelDateOptionLabel;
    private InputUtils.ValueCallback valueCallback;
    private Icon editorIcon;
    private String separator = " to ";
    private boolean usePanelOption;
    private boolean closeAfterSelected;
    private boolean animationEnabled = true;
    private boolean startWeekOnMonday;
    private float selectionArc = 999.0f;
    private int month = 10;
    private int year = 2023;
    private Color color;
    private JButton editorButton;
    private SelectionState selectionState = SelectionState.DATE;
    private PanelDate panelDate;
    private PanelMonth panelMonth;
    private PanelYear panelYear;
    private DefaultDateCellRenderer defaultDateCellRenderer = new DefaultDateCellRenderer();
    private final Header header = new Header();
    private final PanelSlider panelSlider = new PanelSlider();

    public DatePicker() {
        this((DateSelectionModel)null);
    }

    public DatePicker(DateSelectionModel dateSelectionModel) {
        this.init(dateSelectionModel);
    }

    private void init(DateSelectionModel dateSelectionModel) {
        this.putClientProperty("FlatLaf.style", "[light]background:darken($Panel.background,2%);[dark]background:lighten($Panel.background,2%);");
        this.setLayout((LayoutManager)new MigLayout("wrap,insets 10,fill", "[fill]", "[top,grow 0][center,fill]"));
        this.format = DateTimeFormatter.ofPattern(this.dateFormatPattern);
        this.header.addDateControlListener(this);
        if (dateSelectionModel == null) {
            dateSelectionModel = this.createDefaultDateSelection();
        }
        this.setDateSelectionModel(dateSelectionModel);
        this.add(this.header);
        this.add((Component)this.panelSlider, "width 260!,height 250!");
        this.initDate();
        JPanel buttonPanel = this.createButtonPanel();
        this.add((Component)buttonPanel, "dock south");
    }

    private void initDate() {
        LocalDate date = LocalDate.now(ZoneId.systemDefault());
        int month = date.getMonthValue() - 1;
        int year = date.getYear();
        this.month = month;
        this.year = year;
        this.header.setDate(month, year);
        this.panelSlider.addSlide(this.createPanelDate(month, year), null);
    }

    public void setToBack() {
        if (this.selectionState == SelectionState.DATE) {
            if (this.month == 0) {
                this.month = 11;
                --this.year;
            } else {
                --this.month;
            }
            this.header.setDate(this.month, this.year);
            this.panelSlider.addSlide(this.createPanelDate(this.month, this.year), this.getSliderTransition(SimpleTransition.SliderType.BACK));
        } else if (this.selectionState == SelectionState.MONTH) {
            --this.year;
            this.header.setDate(this.month, this.year);
            this.panelSlider.addSlide(this.createPanelMonth(this.year), this.getSliderTransition(SimpleTransition.SliderType.BACK));
        } else {
            int oldYear = this.panelYear.getYear();
            this.panelSlider.addSlide(this.createPanelYear(oldYear - 28), this.getSliderTransition(SimpleTransition.SliderType.BACK));
        }
    }

    public void setToForward() {
        if (this.selectionState == SelectionState.DATE) {
            if (this.month == 11) {
                this.month = 0;
                ++this.year;
            } else {
                ++this.month;
            }
            this.header.setDate(this.month, this.year);
            this.panelSlider.addSlide(this.createPanelDate(this.month, this.year), this.getSliderTransition(SimpleTransition.SliderType.FORWARD));
        } else if (this.selectionState == SelectionState.MONTH) {
            ++this.year;
            this.header.setDate(this.month, this.year);
            this.panelSlider.addSlide(this.createPanelMonth(this.year), this.getSliderTransition(SimpleTransition.SliderType.FORWARD));
        } else {
            int oldYear = this.panelYear.getYear();
            this.panelSlider.addSlide(this.createPanelYear(oldYear + 28), this.getSliderTransition(SimpleTransition.SliderType.FORWARD));
        }
    }

    public void selectMonth() {
        if (this.selectionState != SelectionState.MONTH) {
            this.panelSlider.addSlide(this.createPanelMonth(this.year), this.getSliderTransition(this.selectionState == SelectionState.DATE ? SimpleTransition.SliderType.TOP_DOWN : SimpleTransition.SliderType.DOWN_TOP));
            this.selectionState = SelectionState.MONTH;
        } else {
            this.panelSlider.addSlide(this.createPanelDate(this.month, this.year), this.getSliderTransition(SimpleTransition.SliderType.DOWN_TOP));
            this.selectionState = SelectionState.DATE;
        }
    }

    public void selectYear() {
        if (this.selectionState != SelectionState.YEAR) {
            this.panelSlider.addSlide(this.createPanelYear(this.year), this.getSliderTransition(SimpleTransition.SliderType.TOP_DOWN));
            this.selectionState = SelectionState.YEAR;
        } else {
            this.panelSlider.addSlide(this.createPanelDate(this.month, this.year), this.getSliderTransition(SimpleTransition.SliderType.DOWN_TOP));
            this.selectionState = SelectionState.DATE;
        }
    }

    public DateSelectionMode getDateSelectionMode() {
        return this.dateSelectionModel.getDateSelectionMode();
    }

    public void setDateSelectionMode(DateSelectionMode dateSelectionMode) {
        if (this.getDateSelectionMode() != dateSelectionMode) {
            this.dateSelectionModel.setDateSelectionMode(dateSelectionMode);
            if (this.editor != null) {
                InputUtils.changeDateFormatted(this.editor, this.dateFormatPattern, this.getDateSelectionMode() == DateSelectionMode.BETWEEN_DATE_SELECTED, this.separator, this.getInputValidationListener());
                this.defaultPlaceholder = null;
                this.clearSelectedDate();
                this.commitEdit();
            }
            this.repaint();
        }
    }

    public void now() {
        LocalDate date = LocalDate.now(ZoneId.systemDefault());
        if (this.getDateSelectionMode() == DateSelectionMode.BETWEEN_DATE_SELECTED) {
            this.setSelectedDateRange(date, date);
        } else {
            this.setSelectedDate(date);
        }
    }

    public void toDateSelectionView() {
        LocalDate date = this.getSelectedDate();
        if (date == null) {
            date = LocalDate.now(ZoneId.systemDefault());
        }
        int m = date.getMonthValue() - 1;
        int y = date.getYear();
        if (this.selectionState != SelectionState.DATE || y != this.year || m != this.month) {
            this.panelSlider.addSlide(this.createPanelDate(m, y), this.getSliderTransition(SimpleTransition.SliderType.DEFAULT));
            this.month = m;
            this.year = y;
            this.selectionState = SelectionState.DATE;
            this.header.setDate(this.month, this.year);
            this.updateSelected();
        }
    }

    public void selectCurrentMonth() {
        LocalDate date = LocalDate.now(ZoneId.systemDefault());
        if (this.getDateSelectionMode() == DateSelectionMode.BETWEEN_DATE_SELECTED) {
            this.setSelectedDateRange(date.withDayOfMonth(1), date);
        } else {
            this.setSelectedDate(date);
        }
    }

    public void setSelectedDate(LocalDate date) {
        if (date == null) {
            this.clearSelectedDate();
        } else {
            this.dateSelectionModel.setDate(new SingleDate(date));
            if (this.getDateSelectionMode() == DateSelectionMode.BETWEEN_DATE_SELECTED) {
                this.dateSelectionModel.setToDate(new SingleDate(date));
            }
            this.slideTo(date);
        }
    }

    public void setSelectedDateRange(LocalDate from, LocalDate to) {
        if (this.getDateSelectionMode() == DateSelectionMode.SINGLE_DATE_SELECTED) {
            throw new IllegalArgumentException("Single date mode can't accept the range date");
        }
        this.dateSelectionModel.setSelectDate(new SingleDate(from), new SingleDate(to));
        this.slideTo(from);
    }

    public void setEditor(JFormattedTextField editor) {
        if (editor != this.editor) {
            JFormattedTextField old = this.editor;
            if (old != null) {
                this.uninstallEditor(old);
            }
            this.editor = editor;
            if (this.editor != null) {
                this.installEditor(editor);
                if (this.editorValidation) {
                    this.validChanged(editor, this.isValid);
                } else {
                    this.validChanged(editor, true);
                }
            }
        }
    }

    public Icon getEditorIcon() {
        return this.editorIcon;
    }

    public void setEditorIcon(Icon editorIcon) {
        this.editorIcon = editorIcon;
        this.editorButton.setIcon(editorIcon);
    }

    public DateSelectionAble getDateSelectionAble() {
        return this.dateSelectionModel.getDateSelectionAble();
    }

    public void setDateSelectionAble(DateSelectionAble dateSelectionAble) {
        this.dateSelectionModel.setDateSelectionAble(dateSelectionAble);
        if (this.selectionState == SelectionState.DATE) {
            this.panelDate.load();
        }
        this.commitEdit();
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.repaint();
    }

    public String getSeparator() {
        return this.separator;
    }

    public void setSeparator(String separator) {
        if (separator == null) {
            throw new IllegalArgumentException("separator can't be null");
        }
        if (!this.separator.equals(separator)) {
            this.separator = separator;
            if (this.editor != null) {
                InputUtils.changeDateFormatted(this.editor, this.dateFormatPattern, this.getDateSelectionMode() == DateSelectionMode.BETWEEN_DATE_SELECTED, separator, this.getInputValidationListener());
                this.defaultPlaceholder = null;
                this.setEditorValue();
            }
        }
    }

    public String getDateFormat() {
        return this.dateFormatPattern;
    }

    public void setDateFormat(String format) {
        if (format == null) {
            throw new IllegalArgumentException("format can't be null");
        }
        if (!this.dateFormatPattern.equals(format)) {
            this.format = DateTimeFormatter.ofPattern(format);
            if (this.editor != null) {
                InputUtils.changeDateFormatted(this.editor, format, this.getDateSelectionMode() == DateSelectionMode.BETWEEN_DATE_SELECTED, this.separator, this.getInputValidationListener());
                this.defaultPlaceholder = null;
            }
            this.dateFormatPattern = format;
        }
    }

    public boolean isUsePanelOption() {
        return this.usePanelOption;
    }

    public void setUsePanelOption(boolean usePanelOption) {
        if (this.usePanelOption != usePanelOption) {
            this.usePanelOption = usePanelOption;
            if (usePanelOption) {
                if (this.panelDateOption == null) {
                    this.panelDateOption = new PanelDateOption(this);
                    this.panelDateOption.installDateOptionLabel();
                }
                this.add((Component)this.panelDateOption, "dock east,gap 0 10 10 10");
                this.repaint();
                this.revalidate();
            } else if (this.panelDateOption != null) {
                this.remove(this.panelDateOption);
                this.panelDateOption = null;
                this.repaint();
                this.revalidate();
            }
        }
    }

    public PanelDateOptionLabel getPanelDateOptionLabel() {
        return this.panelDateOptionLabel;
    }

    public void setPanelDateOptionLabel(PanelDateOptionLabel panelDateOptionLabel) {
        if (panelDateOptionLabel == null) {
            throw new IllegalArgumentException("panelDateOptionLabel can't be null");
        }
        this.panelDateOptionLabel = panelDateOptionLabel;
        if (this.panelDateOption != null) {
            this.panelDateOption.installDateOptionLabel();
        }
    }

    public boolean isCloseAfterSelected() {
        return this.closeAfterSelected;
    }

    public void setCloseAfterSelected(boolean closeAfterSelected) {
        this.closeAfterSelected = closeAfterSelected;
    }

    public boolean isAnimationEnabled() {
        return this.animationEnabled;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public boolean isStartWeekOnMonday() {
        return this.startWeekOnMonday;
    }

    public void setStartWeekOnMonday(boolean startWeekOnMonday) {
        if (this.startWeekOnMonday != startWeekOnMonday) {
            this.startWeekOnMonday = startWeekOnMonday;
            if (this.selectionState == SelectionState.DATE && this.panelDate != null) {
                this.panelDate.load();
                this.panelDate.repaint();
                this.panelDate.revalidate();
            }
        }
    }

    public float getSelectionArc() {
        return this.selectionArc;
    }

    public void setSelectionArc(float selectionArc) {
        if (this.selectionArc != selectionArc) {
            this.selectionArc = selectionArc;
            this.updateSelected();
        }
    }

    public void clearSelectedDate() {
        this.dateSelectionModel.setSelectDate(null, null);
        this.updateSelected();
    }

    public boolean isDateSelected() {
        if (this.getDateSelectionMode() == DateSelectionMode.SINGLE_DATE_SELECTED) {
            return this.dateSelectionModel.getDate() != null;
        }
        return this.dateSelectionModel.getDate() != null && this.dateSelectionModel.getToDate() != null;
    }

    public LocalDate getSelectedDate() {
        SingleDate date = this.dateSelectionModel.getDate();
        if (date != null) {
            return date.toLocalDate();
        }
        return null;
    }

    public LocalDate[] getSelectedDateRange() {
        SingleDate from = this.dateSelectionModel.getDate();
        if (from != null) {
            LocalDate[] dates = new LocalDate[2];
            dates[0] = from.toLocalDate();
            SingleDate to = this.dateSelectionModel.getToDate();
            if (to != null) {
                dates[1] = to.toLocalDate();
                return dates;
            }
        }
        return null;
    }

    public String getSelectedDateAsString() {
        if (this.isDateSelected()) {
            if (this.getDateSelectionMode() == DateSelectionMode.SINGLE_DATE_SELECTED) {
                return this.format.format(this.getSelectedDate());
            }
            LocalDate[] dates = this.getSelectedDateRange();
            return this.format.format(dates[0]) + this.separator + this.format.format(dates[1]);
        }
        return null;
    }

    public void slideTo(LocalDate date) {
        int m = date.getMonthValue() - 1;
        int y = date.getYear();
        if (this.year != y || this.month != m) {
            if (this.year < y || this.year <= y && this.month < m) {
                this.panelSlider.addSlide(this.createPanelDate(m, y), this.getSliderTransition(SimpleTransition.SliderType.FORWARD));
            } else {
                this.panelSlider.addSlide(this.createPanelDate(m, y), this.getSliderTransition(SimpleTransition.SliderType.BACK));
            }
            this.month = m;
            this.year = y;
            this.selectionState = SelectionState.DATE;
            this.header.setDate(this.month, this.year);
        } else if (this.selectionState != SelectionState.DATE) {
            this.panelSlider.addSlide(this.createPanelDate(m, y), this.getSliderTransition(SimpleTransition.SliderType.DOWN_TOP));
            this.selectionState = SelectionState.DATE;
        }
        this.updateSelected();
    }

    public void addDateSelectionListener(DateSelectionListener listener) {
        this.listenerList.add(DateSelectionListener.class, listener);
    }

    public void removeDateSelectionListener(DateSelectionListener listener) {
        this.listenerList.remove(DateSelectionListener.class, listener);
    }

    public DefaultDateCellRenderer getDefaultDateCellRenderer() {
        return this.defaultDateCellRenderer;
    }

    public void setDefaultDateCellRenderer(DefaultDateCellRenderer defaultDateCellRenderer) {
        this.defaultDateCellRenderer = defaultDateCellRenderer;
        this.repaint();
    }

    public Header getHeader() {
        return this.header;
    }

    public DateSelectionModel getDateSelectionModel() {
        return this.dateSelectionModel;
    }

    public void setDateSelectionModel(DateSelectionModel dateSelectionModel) {
        if (dateSelectionModel == null) {
            throw new IllegalArgumentException("dateSelectionModel can't be null");
        }
        if (this.dateSelectionModel != dateSelectionModel) {
            DateSelectionModel old = this.dateSelectionModel;
            if (old != null) {
                old.removeDatePickerSelectionListener(this);
            }
            this.dateSelectionModel = dateSelectionModel;
            this.dateSelectionModel.addDatePickerSelectionListener(this);
        }
    }

    @Override
    public void dateSelectionModelChanged(DateSelectionModelEvent e) {
        if (e.getAction() == 2) {
            this.verifyDateSelection();
        }
        this.repaint();
    }

    @Override
    public void dateControlChanged(DateControlEvent e) {
        if (e.getType() == 10) {
            this.setToBack();
        } else if (e.getType() == 11) {
            this.setToForward();
        } else if (e.getType() == 12) {
            this.selectMonth();
        } else if (e.getType() == 13) {
            this.selectYear();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == this.panelMonth) {
            this.month = this.panelMonth.getSelectedMonth();
            this.header.setDate(this.month, this.year);
            this.panelSlider.addSlide(this.createPanelDate(this.month, this.year), this.getSliderTransition(SimpleTransition.SliderType.DOWN_TOP));
            this.selectionState = SelectionState.DATE;
        } else if (e.getSource() == this.panelYear) {
            this.year = this.panelYear.getSelectedYear();
            this.header.setDate(this.month, this.year);
            this.panelSlider.addSlide(this.createPanelMonth(this.year), this.getSliderTransition(SimpleTransition.SliderType.DOWN_TOP));
            this.selectionState = SelectionState.MONTH;
        }
    }

    protected DateSelectionModel createDefaultDateSelection() {
        return new DateSelectionModel();
    }

    protected SliderTransition getSliderTransition(SimpleTransition.SliderType type) {
        if (!this.animationEnabled) {
            return null;
        }
        return SimpleTransition.get(type);
    }

    private void updateSelected() {
        if (this.selectionState == SelectionState.DATE) {
            this.panelDate.checkSelection();
        } else if (this.selectionState == SelectionState.MONTH) {
            this.panelMonth.checkSelection();
        } else if (this.selectionState == SelectionState.YEAR) {
            this.panelYear.checkSelection();
        }
    }

    private void installEditor(JFormattedTextField editor) {
        if (editor != null) {
            JToolBar toolBar = new JToolBar();
            this.editorButton = new JButton(this.editorIcon != null ? this.editorIcon : ResourceUtil.getIcon(ResourceUtil.OtherIcon.CALENDAR));
            toolBar.add(this.editorButton);
            this.editorButton.addActionListener(e -> {
                if (editor.isEnabled()) {
                    editor.grabFocus();
                    this.showPopup();
                }
            });
            InputUtils.useDateInput(editor, this.dateFormatPattern, this.getDateSelectionMode() == DateSelectionMode.BETWEEN_DATE_SELECTED, this.separator, this.getValueCallback(), this.getInputValidationListener());
            this.setEditorValue();
            editor.putClientProperty("JTextField.trailingComponent", toolBar);
            this.addDateSelectionListener(this.getDateSelectionListener());
        }
    }

    private void uninstallEditor(JFormattedTextField editor) {
        if (editor != null) {
            this.editorButton = null;
            InputUtils.removePropertyChange(editor);
            if (this.dateSelectionListener != null) {
                this.removeDateSelectionListener(this.dateSelectionListener);
            }
        }
    }

    private InputUtils.ValueCallback getValueCallback() {
        if (this.valueCallback == null) {
            this.valueCallback = value -> {
                if (value == null && this.isDateSelected()) {
                    this.clearSelectedDate();
                } else if (value != null && !value.equals(this.getSelectedDateAsString())) {
                    if (this.getDateSelectionMode() == DateSelectionMode.SINGLE_DATE_SELECTED) {
                        LocalDate date = InputUtils.stringToDate(this.format, value.toString());
                        if (date != null) {
                            this.setSelectedDate(date);
                        }
                    } else {
                        LocalDate[] dates = InputUtils.stringToDate(this.format, this.separator, value.toString());
                        if (dates != null) {
                            this.setSelectedDateRange(dates[0], dates[1]);
                        }
                    }
                }
            };
        }
        return this.valueCallback;
    }

    private DateSelectionListener getDateSelectionListener() {
        if (this.dateSelectionListener == null) {
            this.dateSelectionListener = dateSelectionEvent -> this.setEditorValue();
        }
        return this.dateSelectionListener;
    }

    private void setEditorValue() {
        String value = this.getSelectedDateAsString();
        if (value != null) {
            if (!this.editor.getText().equalsIgnoreCase(value)) {
                this.editor.setValue(value);
            }
        } else {
            this.editor.setValue(null);
        }
    }

    private InputValidationListener<LocalDate> getInputValidationListener() {
        if (this.inputValidationListener == null) {
            this.inputValidationListener = new InputValidationListener<LocalDate>(this){
                final /* synthetic */ DatePicker this$0;
                {
                    DatePicker datePicker = this$0;
                    Objects.requireNonNull(datePicker);
                    this.this$0 = datePicker;
                }

                @Override
                public boolean isValidation() {
                    return this.this$0.dateSelectionModel.getDateSelectionAble() != null;
                }

                @Override
                public void inputChanged(boolean status) {
                    this.this$0.checkValidation(status);
                }

                @Override
                public boolean checkSelectionAble(LocalDate date) {
                    if (this.this$0.dateSelectionModel.getDateSelectionAble() == null) {
                        return true;
                    }
                    return this.this$0.dateSelectionModel.getDateSelectionAble().isDateSelectedAble(date);
                }
            };
        }
        return this.inputValidationListener;
    }

    @Override
    protected String getDefaultPlaceholder() {
        if (this.defaultPlaceholder == null) {
            if (this.getDateSelectionMode() == DateSelectionMode.BETWEEN_DATE_SELECTED) {
                String d = InputUtils.datePatternToInputFormat(this.dateFormatPattern, "-");
                this.defaultPlaceholder = d + this.separator + d;
            } else {
                this.defaultPlaceholder = InputUtils.datePatternToInputFormat(this.dateFormatPattern, "-");
            }
        }
        return this.defaultPlaceholder;
    }

    @Override
    protected void popupOpen() {
        this.toDateSelectionView();
    }

    private void verifyDateSelection() {
        if (this.getDateSelectionMode() == DateSelectionMode.BETWEEN_DATE_SELECTED) {
            SingleDate fromDate = this.dateSelectionModel.getDate();
            SingleDate toDate = this.dateSelectionModel.getToDate();
            if (fromDate == null && toDate != null || fromDate != null && toDate == null) {
                return;
            }
        }
        if (this.isCloseAfterSelected()) {
            this.closePopup();
        }
        this.fireDateSelectionChanged(new DateSelectionEvent(this));
        if (this.panelDateOption != null) {
            this.panelDateOption.setSelectedCustom();
        }
    }

    public void fireDateSelectionChanged(DateSelectionEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != DateSelectionListener.class) continue;
            ((DateSelectionListener)listeners[i + 1]).dateSelected(event);
        }
    }

    private void setPanelDate(PanelDate panelDate) {
        this.panelDate = panelDate;
    }

    private void setPanelMonth(PanelMonth panelMonth) {
        PanelMonth old = this.panelMonth;
        if (old != null) {
            old.removeChangeListener(this);
        }
        this.panelMonth = panelMonth;
        this.panelMonth.addChangeListener(this);
    }

    private void setPanelYear(PanelYear panelYear) {
        PanelYear old = this.panelYear;
        if (old != null) {
            old.removeChangeListener(this);
        }
        this.panelYear = panelYear;
        this.panelYear.addChangeListener(this);
    }

    private PanelDate createPanelDate(int month, int year) {
        PanelDate panelDate = new PanelDate(this, month, year);
        this.setPanelDate(panelDate);
        return panelDate;
    }

    private PanelMonth createPanelMonth(int year) {
        PanelMonth panelMonth = new PanelMonth(this, year);
        this.setPanelMonth(panelMonth);
        return panelMonth;
    }

    private PanelYear createPanelYear(int year) {
        PanelYear panelYear = new PanelYear(this, year);
        this.setPanelYear(panelYear);
        return panelYear;
    }

    public static void setDefaultWeekdays(String[] defaultWeekdays) {
        DatePicker.defaultWeekdays = defaultWeekdays == null ? null : Arrays.copyOf(defaultWeekdays, defaultWeekdays.length);
    }

    public static String[] getDefaultWeekdays() {
        if (defaultWeekdays == null) {
            return DateFormatSymbols.getInstance().getShortWeekdays();
        }
        return Arrays.copyOf(defaultWeekdays, defaultWeekdays.length);
    }

    public static void setDefaultMonths(String[] defaultMonths) {
        DatePicker.defaultMonths = defaultMonths == null ? null : Arrays.copyOf(defaultMonths, defaultMonths.length);
    }

    public static String[] getDefaultMonths() {
        if (defaultMonths == null) {
            return DateFormatSymbols.getInstance().getMonths();
        }
        return Arrays.copyOf(defaultMonths, defaultMonths.length);
    }

    private static enum SelectionState {
        DATE,
        MONTH,
        YEAR;

    }

    public static enum DateSelectionMode {
        SINGLE_DATE_SELECTED,
        BETWEEN_DATE_SELECTED;

    }
}

