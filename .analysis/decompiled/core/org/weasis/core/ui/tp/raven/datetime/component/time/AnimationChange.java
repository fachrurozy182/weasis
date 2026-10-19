/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.Animator
 *  com.formdev.flatlaf.util.Animator$Interpolator
 *  com.formdev.flatlaf.util.CubicBezierEasing
 */
package org.weasis.core.ui.tp.raven.datetime.component.time;

import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.CubicBezierEasing;
import java.awt.Component;

public class AnimationChange {
    public final Animator animator;
    private final AnimationValue angleValue = new AnimationValue();
    private final AnimationValue marginValue = new AnimationValue();
    private float angle;
    private float margin;

    public AnimationChange(Component component) {
        this.animator = new Animator(350, v -> {
            this.angle = this.angleValue.interpolate(v);
            this.margin = this.marginValue.interpolate(v);
            component.repaint();
        });
        this.animator.setInterpolator((Animator.Interpolator)CubicBezierEasing.EASE);
    }

    public void start(float angleTarget, float marginTarget, boolean animated) {
        if (this.angle != angleTarget || this.margin != marginTarget) {
            this.angleValue.set(this.angle, angleTarget);
            this.marginValue.set(this.margin, marginTarget);
            if (this.animator.isRunning()) {
                this.animator.stop();
            }
            if (animated) {
                this.animator.start();
            } else {
                this.angle = this.angleValue.interpolate(1.0f);
                this.margin = this.marginValue.interpolate(1.0f);
            }
        }
    }

    public float getAngle() {
        return this.angle;
    }

    public float getMargin() {
        return this.margin;
    }

    public void set(float angle, float margin) {
        if (this.animator.isRunning()) {
            this.animator.stop();
        }
        this.angle = angle;
        this.margin = margin;
    }

    public boolean isRunning() {
        return this.animator.isRunning();
    }

    private static class AnimationValue {
        private float from;
        private float target;

        private AnimationValue() {
        }

        public void set(float from, float target) {
            this.from = from;
            this.target = target;
        }

        private float interpolate(float fraction) {
            return this.from + (this.target - this.from) * fraction;
        }
    }
}

