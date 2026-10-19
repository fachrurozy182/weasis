/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.api.gui.util;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.BasicActionState;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.JSliderW;
import org.weasis.core.api.gui.util.MouseActionAdapter;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.util.MathUtil;

public abstract class SliderChangeListener
extends MouseActionAdapter
implements ChangeListener,
ActionState {
    private final DefaultBoundedRangeModel model;
    protected final BasicActionState basicState;
    protected volatile boolean triggerAction = true;
    protected volatile boolean triggerWhenAdjusting;
    protected Double realMin;
    protected Double realMax;

    protected SliderChangeListener(Feature<? extends ActionState> action, int min, int max, int value) {
        this(action, min, max, value, true);
    }

    protected SliderChangeListener(Feature<? extends ActionState> action, int min, int max, int value, boolean triggerWhenAdjusting, double mouseSensitivity) {
        this(action, min, max, value, triggerWhenAdjusting);
        this.setMouseSensitivity(mouseSensitivity);
    }

    protected SliderChangeListener(Feature<? extends ActionState> action, int min, int max, int value, boolean triggerWhenAdjusting) {
        this.basicState = new BasicActionState(action);
        this.triggerWhenAdjusting = triggerWhenAdjusting;
        this.model = new DefaultBoundedRangeModel(value, 0, min, max);
        this.model.addChangeListener(this);
    }

    protected SliderChangeListener(Feature<? extends ActionState> action, double min, double max, double value, boolean triggerWhenAdjusting, double mouseSensitivity, int sliderRange) {
        this.basicState = new BasicActionState(action);
        this.triggerWhenAdjusting = triggerWhenAdjusting;
        this.setMouseSensitivity(mouseSensitivity);
        this.model = new DefaultBoundedRangeModel(0, 0, 0, sliderRange);
        this.setRealMinMaxValue(min, max, value, false);
        this.model.addChangeListener(this);
    }

    @Override
    public void enableAction(boolean enabled) {
        this.basicState.enableAction(enabled);
    }

    @Override
    public boolean isActionEnabled() {
        return this.basicState.isActionEnabled();
    }

    public void setSliderMinMax(int min, int max) {
        this.setSliderMinMaxValue(min, max, this.model.getValue());
    }

    public void setSliderMinMaxValue(int min, int max, int value) {
        this.setSliderMinMaxValue(min, max, value, true);
    }

    public void setSliderMinMaxValue(int min, int max, int value, boolean triggerChangedEvent) {
        this.realMin = null;
        this.realMax = null;
        this.minMaxValueAction(min, max, value, triggerChangedEvent);
    }

    public void setRealMinMaxValue(double min, double max, double value) {
        this.setRealMinMaxValue(min, max, value, true);
    }

    public void setRealMinMaxValue(double min, double max, double value, boolean triggerChangedEvent) {
        if (max - min == 0.0) {
            max += 1.0;
        }
        this.realMin = min;
        this.realMax = max;
        this.minMaxValueAction(this.toSliderValue(min), this.toSliderValue(max), this.toSliderValue(value), triggerChangedEvent);
    }

    private synchronized void minMaxValueAction(int min, int max, int value, boolean trigger) {
        if (min > max) {
            throw new IllegalStateException("min > max");
        }
        int v = value > max ? max : Math.max(value, min);
        boolean oldTrigger = this.triggerAction;
        this.triggerAction = trigger;
        this.model.setRangeProperties(v, this.model.getExtent(), min, max, this.model.getValueIsAdjusting());
        this.triggerAction = oldTrigger;
        boolean paintThick = max < 65536;
        for (Object c : this.basicState.getComponents()) {
            if (!(c instanceof JSliderW)) continue;
            JSliderW s = (JSliderW)c;
            if (s.isShowLabels()) {
                s.setPaintTicks(paintThick);
                s.setPaintLabels(paintThick);
            }
            this.updateSliderProperties(s);
            SliderChangeListener.setSliderLabelValues(s, min, max, this.realMin, this.realMax);
        }
    }

    public boolean isTriggerAction() {
        return this.triggerAction;
    }

    @Override
    public Feature<? extends ActionState> getActionW() {
        return this.basicState.getActionW();
    }

    public void setSliderValue(int value) {
        this.model.setValue(value);
    }

    public void setSliderValue(int value, boolean triggerChangedEvent) {
        if (triggerChangedEvent) {
            this.setSliderValue(value);
        } else {
            boolean adjusting;
            boolean bl = adjusting = this.triggerWhenAdjusting || !this.model.getValueIsAdjusting();
            if (adjusting) {
                boolean oldTrigger = this.triggerAction;
                this.triggerAction = false;
                this.setSliderValue(value);
                this.triggerAction = oldTrigger;
            }
        }
    }

    public void setRealValue(double value) {
        this.model.setValue(this.toSliderValue(value));
    }

    public void setRealValue(double value, boolean triggerChangedEvent) {
        if (triggerChangedEvent) {
            this.setRealValue(value);
        } else {
            boolean adjusting;
            boolean bl = adjusting = this.triggerWhenAdjusting || !this.model.getValueIsAdjusting();
            if (adjusting) {
                boolean oldTrigger = this.triggerAction;
                this.triggerAction = false;
                this.setRealValue(value);
                this.triggerAction = oldTrigger;
            }
        }
    }

    public boolean isTriggerWhenAdjusting() {
        return this.triggerWhenAdjusting;
    }

    public int getSliderMin() {
        return this.model.getMinimum();
    }

    public int getSliderMax() {
        return this.model.getMaximum();
    }

    public int getSliderValue() {
        return this.model.getValue();
    }

    public double getRealValue() {
        return this.toModelValue(this.model.getValue());
    }

    public DefaultBoundedRangeModel getSliderModel() {
        return this.model;
    }

    public String getValueToDisplay() {
        return SliderChangeListener.getDisplayedModelValue(this.getSliderValue(), this.getSliderMax(), this.realMin, this.realMax);
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        boolean adjusting;
        boolean bl = adjusting = this.triggerWhenAdjusting || !this.model.getValueIsAdjusting();
        if (this.triggerAction && adjusting) {
            this.stateChanged(this.model);
            AuditLog.LOGGER.info("action:{} val:{} min:{} max:{}", new Object[]{this.basicState.getActionW().cmd(), this.model.getValue(), this.model.getMinimum(), this.model.getMaximum()});
        }
        for (Object c : this.basicState.getComponents()) {
            if (!(c instanceof JSliderW)) continue;
            JSliderW slider = (JSliderW)c;
            this.updateSliderProperties(slider);
        }
    }

    public abstract void stateChanged(BoundedRangeModel var1);

    public String toString() {
        return this.basicState.getActionW().getTitle();
    }

    @Override
    public boolean registerActionState(Object c) {
        if (this.basicState.registerActionState(c)) {
            if (c instanceof JSliderW) {
                JSliderW slider = (JSliderW)c;
                slider.setModel(this.model);
                this.updateSliderProperties(slider);
            }
            return true;
        }
        return false;
    }

    @Override
    public void unregisterActionState(Object c) {
        this.basicState.unregisterActionState(c);
        if (c instanceof JSliderW) {
            JSliderW slider = (JSliderW)c;
            slider.setModel(new DefaultBoundedRangeModel(0, 0, 0, 100));
        }
    }

    public static void setSliderLabelValues(JSliderW slider, int min, int max) {
        SliderChangeListener.setSliderLabelValues(slider, min, max, null, null);
    }

    public static void setSliderLabelValues(JSliderW slider, int min, int max, Double realMin, Double realMax) {
        int div = slider.getLabelDivision();
        if (div < 1) {
            return;
        }
        int space = (max - min) / (div - 1);
        int spacing = Math.max(space, 1);
        if (!slider.getPaintLabels()) {
            return;
        }
        Hashtable table = new Hashtable();
        GuiExecutor.invokeAndWait(() -> {
            for (int i = 0; i < div; ++i) {
                int index = i * spacing + min;
                table.put(index, new JLabel(SliderChangeListener.getDisplayedModelValue(i * spacing + min, max, realMin, realMax)));
            }
        });
        slider.setLabelTable(table);
        SliderChangeListener.setFont(slider, FontItem.MINI.getFont());
        slider.setMajorTickSpacing(spacing);
    }

    private static String getDisplayedModelValue(int sliderValue, int sliderMax, Double modelMin, Double modelMax) {
        if (modelMin == null || modelMax == null) {
            return Integer.toString(sliderValue);
        }
        double realVal = SliderChangeListener.toModelValue(sliderValue, sliderMax, modelMin, modelMax);
        return DecFormatter.twoDecimal(realVal);
    }

    public void updateSliderProperties(JSliderW slider) {
        String result = this.basicState.getActionW().getTitle() + ": " + this.getValueToDisplay();
        SliderChangeListener.updateSliderProperties(slider, result);
    }

    public static void updateSliderProperties(JSliderW slider, String title) {
        Border border;
        if (slider.isDisplayValueInTitle() && (border = slider.getBorder()) instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder)border;
            titledBorder.setTitle(title);
            slider.repaint();
        } else {
            slider.setToolTipText(title);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (this.basicState.isActionEnabled() && !e.isConsumed()) {
            int buttonMask = this.getButtonMaskEx();
            if ((e.getModifiersEx() & buttonMask) != 0) {
                this.model.setValueIsAdjusting(true);
                this.lastPosition = this.isMoveOnX() ? e.getX() : e.getY();
                this.dragAccumulator = this.getSliderValue();
            }
        } else {
            this.dragAccumulator = Double.MAX_VALUE;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.basicState.isActionEnabled() && !e.isConsumed()) {
            int buttonMask = this.getButtonMaskEx();
            int modifier = e.getModifiersEx();
            if ((modifier & buttonMask) != 0 && MathUtil.isDifferent((double)this.dragAccumulator, (double)Double.MAX_VALUE)) {
                this.model.setValueIsAdjusting(true);
                int position = this.isMoveOnX() ? e.getX() : e.getY();
                int mask = 192;
                double acceleratorKey = (modifier & mask) == 0 ? 1.0 : ((modifier & mask) == mask ? 5.0 : 2.5);
                double val = (double)(position - this.lastPosition) * this.getMouseSensitivity() * acceleratorKey;
                if (MathUtil.isEqualToZero((double)val)) {
                    return;
                }
                this.lastPosition = position;
                this.dragAccumulator = this.isInverse() ? (this.dragAccumulator -= val) : (this.dragAccumulator += val);
                if (this.dragAccumulator < (double)this.getSliderMin()) {
                    this.dragAccumulator = this.getSliderMin();
                }
                if (this.dragAccumulator > (double)this.getSliderMax()) {
                    this.dragAccumulator = this.getSliderMax();
                }
                if (val < 0.0) {
                    this.setSliderValue((int)Math.ceil(this.dragAccumulator));
                } else {
                    this.setSliderValue((int)Math.floor(this.dragAccumulator));
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (this.basicState.isActionEnabled() && !e.isConsumed() && (e.getModifiers() & this.getButtonMask()) != 0) {
            this.model.setValueIsAdjusting(false);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (this.basicState.isActionEnabled() && !e.isConsumed()) {
            this.setSliderValue(this.getSliderValue() + e.getWheelRotation() * e.getScrollAmount());
        }
    }

    public JSliderW createSlider(int labelDivision, boolean displayValueInTitle) {
        TitledBorder titledBorder = new TitledBorder(BorderFactory.createEmptyBorder(), this.basicState.getActionW().getTitle(), 4, 0, FontItem.MEDIUM.getFont(), null);
        JSliderW slider = new JSliderW(this.model.getMinimum(), this.model.getMaximum(), this.model.getValue());
        slider.setLabelDivision(labelDivision);
        slider.setDisplayValueInTitle(displayValueInTitle);
        slider.setPaintTicks(true);
        slider.setShowLabels(labelDivision > 0);
        slider.setBorder(titledBorder);
        this.registerActionState(slider);
        if (slider.isShowLabels()) {
            slider.setPaintLabels(true);
            SliderChangeListener.setSliderLabelValues(slider, this.model.getMinimum(), this.model.getMaximum(), this.realMin, this.realMax);
        }
        return slider;
    }

    public int toSliderValue(double modelValue) {
        Double modelMin = this.realMin;
        Double modelMax = this.realMax;
        if (modelMin == null || modelMax == null) {
            return (int)modelValue;
        }
        return (int)Math.round((modelValue - modelMin) * ((double)this.model.getMaximum() / (modelMax - modelMin)));
    }

    public double toModelValue(int sliderValue) {
        return SliderChangeListener.toModelValue(sliderValue, this.model.getMaximum(), this.realMin, this.realMax);
    }

    protected static double toModelValue(int sliderValue, int sliderMax, Double modelMin, Double modelMax) {
        if (modelMin == null || modelMax == null) {
            return sliderValue;
        }
        return (double)sliderValue * (modelMax - modelMin) / (double)sliderMax + modelMin;
    }

    public static void setFont(JSlider jslider, Font font) {
        Dictionary labelTable = jslider.getLabelTable();
        if (labelTable == null) {
            return;
        }
        Enumeration labels = labelTable.keys();
        while (labels.hasMoreElements()) {
            Object v = labelTable.get(labels.nextElement());
            if (!(v instanceof JLabel)) continue;
            JLabel label = (JLabel)v;
            label.setFont(font);
        }
    }
}

