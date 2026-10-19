/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.api.gui.util;

import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.JSliderW;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.util.MathUtil;

public abstract class SliderCineListener
extends SliderChangeListener {
    private static final double DEFAULT_MIN = 0.01;
    private static final double DEFAULT_MAX = 90.0;
    protected volatile boolean sweeping = false;
    private volatile boolean backward = false;
    private volatile long startTime;
    private volatile long spentTime;
    private final AtomicInteger iteration = new AtomicInteger(0);
    private final TIME time;
    private final SpinnerNumberModel speedModel;
    private final Timer timer = new Timer(50, actionEvent -> {
        int step = (int)(this.getSpeed() / 5.0);
        if (step <= 0) {
            step = 1;
        }
        if (this.iteration.getAndIncrement() % step == 0) {
            long prevStartTime = this.startTime;
            this.startTime = System.currentTimeMillis();
            this.spentTime = this.iteration.get() > step ? (System.currentTimeMillis() - prevStartTime) / 1000L : 0L;
        }
        int offset = this.backward ? -1 : 1;
        int frameIndex = this.getSliderValue() + offset;
        if (frameIndex > this.getSliderMax()) {
            if (this.sweeping) {
                this.backward = true;
                frameIndex -= 2;
            } else {
                frameIndex = this.getSliderMin();
            }
        } else if (frameIndex < this.getSliderMin()) {
            this.backward = false;
            frameIndex = this.getSliderMin();
        }
        this.setSliderValue(frameIndex);
    });

    protected SliderCineListener(Feature<? extends ActionState> action, int min, int max, int value, double speed, TIME time, double mouseSensitivity) {
        this(action, min, max, value, speed, time);
        this.setMouseSensitivity(mouseSensitivity);
    }

    protected SliderCineListener(Feature<? extends ActionState> action, int min, int max, int value, double speed, TIME time) {
        super(action, min, max, value);
        this.time = time;
        this.speedModel = new SpinnerNumberModel(speed, 0.01, 90.0, 1.0);
        this.speedModel.addChangeListener(changeEvent -> this.updateSpeed());
    }

    public void start() {
        if (!this.timer.isRunning() && this.getSliderMax() - this.getSliderMin() > 0) {
            this.timer.setDelay((int)(1000.0 / this.getSpeed()));
            this.iteration.set(0);
            this.timer.start();
        }
    }

    public void stop() {
        if (this.timer.isRunning()) {
            this.timer.stop();
        }
    }

    public boolean isCining() {
        return this.timer.isRunning();
    }

    public double getSpeed() {
        return (Double)this.speedModel.getValue();
    }

    @Override
    public void updateSliderProperties(JSliderW slider) {
        Border border;
        double rate = this.getCurrentCineRate();
        StringBuilder buffer = new StringBuilder(Messages.getString("SliderCineListener.img"));
        buffer.append(": ");
        buffer.append(this.getValueToDisplay());
        if (slider.isDisplayValueInTitle() && (border = slider.getBorder()) instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder)border;
            if (MathUtil.isDifferentFromZero((double)rate)) {
                buffer.append(" (");
                buffer.append(DecFormatter.twoDecimal(rate));
                if (TIME.SECOND.equals((Object)this.time)) {
                    buffer.append(Messages.getString("SliderCineListener.fps"));
                } else if (TIME.MINUTE.equals((Object)this.time)) {
                    buffer.append(Messages.getString("SliderCineListener.fpm"));
                } else if (TIME.HOUR.equals((Object)this.time)) {
                    buffer.append(Messages.getString("SliderCineListener.fph"));
                }
                buffer.append(")");
            }
            titledBorder.setTitleColor(rate > 0.0 && Math.abs(rate - this.getSpeed()) > this.getSpeed() / 20.0 ? GuiUtils.IconColor.ACTIONS_RED.getColor() : UIManager.getColor("TitledBorder.titleColor"));
            titledBorder.setTitle(buffer.toString());
            slider.repaint();
        } else {
            slider.setToolTipText(buffer.toString());
        }
    }

    public double getCurrentCineRate() {
        if (this.isCining()) {
            double time = this.spentTime;
            if (time == 0.0) {
                return 1000.0 / (double)this.timer.getDelay();
            }
            return this.spentTime;
        }
        return 0.0;
    }

    public void setSpeed(double speed) {
        this.speedModel.setValue(Math.max(0.01, Math.min(speed, 90.0)));
    }

    protected void updateSpeed() {
        if (this.timer.isRunning()) {
            this.iteration.set(0);
            this.timer.setDelay((int)(1000.0 / this.getSpeed()));
        }
    }

    public SpinnerNumberModel getSpeedModel() {
        return this.speedModel;
    }

    public void setSweeping(boolean sweep) {
        this.sweeping = sweep;
    }

    public static enum TIME {
        SECOND,
        MINUTE,
        HOUR;

    }
}

