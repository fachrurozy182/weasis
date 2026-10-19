/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.Animator
 *  com.formdev.flatlaf.util.Animator$Interpolator
 *  com.formdev.flatlaf.util.Animator$TimingTarget
 *  com.formdev.flatlaf.util.CubicBezierEasing
 */
package org.weasis.core.ui.tp.raven.slider;

import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.CubicBezierEasing;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.VolatileImage;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import org.weasis.core.ui.tp.raven.slider.SliderTransition;

public class PanelSlider
extends JLayeredPane {
    private PanelSnapshot panelSnapshot;
    private Component slideComponent;

    public Component getSlideComponent() {
        return this.slideComponent;
    }

    public PanelSlider() {
        this.init();
    }

    private void init() {
        this.panelSnapshot = new PanelSnapshot();
        this.setLayout(new CardLayout());
        this.setLayer(this.panelSnapshot, JLayeredPane.DRAG_LAYER);
        this.add(this.panelSnapshot);
        this.panelSnapshot.setVisible(false);
    }

    public void addSlide(Component component, SliderTransition transition) {
        this.slideComponent = component;
        if (this.getComponentCount() == 1) {
            this.add(component);
            this.repaint();
            this.revalidate();
            component.setVisible(true);
        } else {
            Component oldComponent = this.getComponent(1);
            this.add(component);
            if (transition != null) {
                this.doLayout();
                component.doLayout();
                Image oldImage = this.createImage(oldComponent);
                Image newImage = this.createImage(component);
                this.remove(oldComponent);
                this.panelSnapshot.animate(component, transition, oldImage, newImage);
            } else {
                component.setVisible(true);
                this.remove(oldComponent);
                this.revalidate();
                this.repaint();
            }
        }
    }

    private Image createImage(Component component) {
        VolatileImage snapshot = component.createVolatileImage(this.getWidth(), this.getHeight());
        if (snapshot != null) {
            component.paint(snapshot.getGraphics());
        }
        return snapshot;
    }

    private static class PanelSnapshot
    extends JComponent {
        private final Animator animator = new Animator(400, new Animator.TimingTarget(this){
            final /* synthetic */ PanelSnapshot this$0;
            {
                PanelSnapshot panelSnapshot = this$0;
                Objects.requireNonNull(panelSnapshot);
                this.this$0 = panelSnapshot;
            }

            public void timingEvent(float v) {
                this.this$0.animate = v;
                this.this$0.repaint();
            }

            public void end() {
                this.this$0.setVisible(false);
                this.this$0.component.setVisible(true);
                if (this.this$0.newImage != null) {
                    this.this$0.newImage.flush();
                }
                if (this.this$0.oldImage != null) {
                    this.this$0.oldImage.flush();
                }
            }
        });
        private Component component;
        private float animate;
        private SliderTransition sliderTransition;
        private Image oldImage;
        private Image newImage;

        public PanelSnapshot() {
            this.animator.setInterpolator((Animator.Interpolator)CubicBezierEasing.EASE);
        }

        protected void animate(Component component, SliderTransition sliderTransition, Image oldImage, Image newImage) {
            if (this.animator.isRunning()) {
                this.animator.stop();
            }
            this.component = component;
            this.oldImage = oldImage;
            this.newImage = newImage;
            this.sliderTransition = sliderTransition;
            this.animate = 0.0f;
            this.repaint();
            this.setVisible(true);
            this.animator.start();
        }

        @Override
        public void paint(Graphics g) {
            if (this.sliderTransition != null) {
                int width = this.getWidth();
                int height = this.getHeight();
                this.sliderTransition.render(g, this.oldImage, this.newImage, width, height, this.animate);
            }
        }
    }
}

