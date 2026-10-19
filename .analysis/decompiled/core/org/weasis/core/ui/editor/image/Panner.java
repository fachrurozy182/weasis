/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import javax.swing.event.MouseInputAdapter;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.model.layer.imp.RenderedImageLayer;

public final class Panner<E extends ImageElement>
extends Thumbnail {
    private final MouseClickHandler mouseClickHandler = new MouseClickHandler(this);
    private final DefaultView2d<E> view;
    private final Rectangle2D slider;
    private final Rectangle2D panArea;
    private boolean updatingImageDisplay;

    public Panner(DefaultView2d<E> view) {
        super(156);
        this.view = view;
        this.setForeground(Color.RED);
        this.slider = new Rectangle2D.Double();
        this.panArea = new Rectangle2D.Double();
    }

    @Override
    public void registerListeners() {
        super.registerListeners();
        this.addMouseListener(this.mouseClickHandler);
        this.addMouseMotionListener(this.mouseClickHandler);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        this.updateImageSize();
    }

    public boolean isUpdatingImageDisplay() {
        return this.updatingImageDisplay;
    }

    public void updateImage() {
        E img;
        if (this.view != null && (img = this.view.getImage()) != null) {
            this.thumbnailPath = null;
            this.readable = true;
            this.removeImageFromCache();
            this.buildThumbnail((MediaElement)img, true, ((RenderedImageLayer)this.view.getImageLayer()).getPreprocessing());
            this.updateImageSize();
        }
    }

    public void updateImageSize() {
        if (this.view != null) {
            Rectangle2D ma;
            double modelRatio;
            int imageHeight;
            Insets insets = this.getInsets();
            int imageWidth = this.getWidth() - (insets.left + insets.right);
            double imageRatio = (double)imageWidth / (double)(imageHeight = this.getHeight() - (insets.top + insets.bottom));
            if (imageRatio < (modelRatio = (ma = this.view.getViewModel().getModelArea()).getWidth() / ma.getHeight())) {
                imageHeight = (int)Math.round((double)imageWidth / modelRatio);
            } else {
                imageWidth = (int)Math.round((double)imageHeight * modelRatio);
            }
            if (imageWidth > 0 && imageHeight > 0) {
                double x = 0.0;
                double y = 0.0;
                if (imageWidth < this.getWidth()) {
                    x = (double)(this.getWidth() - imageWidth) * 0.5;
                }
                if (imageHeight < this.getHeight()) {
                    y = (double)(this.getHeight() - imageHeight) * 0.5;
                }
                this.panArea.setRect(x, y, imageWidth, imageHeight);
                this.updateSlider();
            }
        }
    }

    public void updateSlider() {
        if (this.updatingImageDisplay || this.view == null) {
            return;
        }
        Rectangle2D ma = this.view.getViewModel().getModelArea();
        double vs = this.view.getViewModel().getViewScale();
        double x = this.panArea.getX() + this.panArea.getWidth() * (this.view.getViewModel().getModelOffsetX() + ma.getWidth() * 0.5) / ma.getWidth() - this.slider.getWidth() * 0.5;
        double y = this.panArea.getY() + this.panArea.getHeight() * (this.view.getViewModel().getModelOffsetY() + ma.getHeight() * 0.5) / ma.getHeight() - this.slider.getHeight() * 0.5;
        double w = this.panArea.getWidth() * (double)this.view.getWidth() / (ma.getWidth() * vs);
        double h = this.panArea.getHeight() * (double)this.view.getHeight() / (ma.getHeight() * vs);
        this.slider.setFrame(x, y, w, h);
        this.repaint();
    }

    public void moveToOrigin() {
        if (this.view != null) {
            Rectangle2D area = this.view.getViewModel().getModelArea();
            this.view.setCenter((this.view.viewToModelLength(Double.valueOf(this.view.getWidth())) - area.getWidth()) * 0.5, (this.view.viewToModelLength(Double.valueOf(this.view.getHeight())) - area.getHeight()) * 0.5);
        }
    }

    public void moveToCenter() {
        if (this.view != null) {
            this.view.center();
        }
    }

    @Override
    protected void drawOverIcon(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(this.getForeground().getRed(), this.getForeground().getGreen(), this.getForeground().getBlue(), 40));
        g2d.fill(this.slider);
        g2d.setColor(this.getForeground());
        g2d.draw(this.slider);
    }

    class MouseClickHandler
    extends MouseInputAdapter {
        private Point pickPoint;
        private Point2D sliderPoint;
        final /* synthetic */ Panner this$0;

        MouseClickHandler(Panner this$0) {
            Panner panner = this$0;
            Objects.requireNonNull(panner);
            this.this$0 = panner;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.pickPoint = e.getPoint();
            if (!this.this$0.slider.contains(this.pickPoint)) {
                this.this$0.slider.setFrame((double)this.pickPoint.x - this.this$0.slider.getWidth() * 0.5, (double)this.pickPoint.y - this.this$0.slider.getHeight() * 0.5, this.this$0.slider.getWidth(), this.this$0.slider.getHeight());
                this.this$0.repaint();
            }
            this.sliderPoint = new Point2D.Double(this.this$0.slider.getX(), this.this$0.slider.getY());
            this.updateImageDisplay();
        }

        @Override
        public void mouseReleased(MouseEvent mouseevent) {
            this.updateImageDisplay();
            this.this$0.repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (this.pickPoint != null) {
                this.this$0.slider.setFrame(this.sliderPoint.getX() + (double)(e.getX() - this.pickPoint.x), this.sliderPoint.getY() + (double)(e.getY() - this.pickPoint.y), this.this$0.slider.getWidth(), this.this$0.slider.getHeight());
                this.updateImageDisplay();
                this.this$0.repaint();
            }
        }

        private void updateImageDisplay() {
            if (this.this$0.view != null) {
                Rectangle2D ma = this.this$0.view.getViewModel().getModelArea();
                double mpX = (this.this$0.slider.getCenterX() - this.this$0.panArea.getCenterX()) * ma.getWidth() / this.this$0.panArea.getWidth();
                double mpY = (this.this$0.slider.getCenterY() - this.this$0.panArea.getCenterY()) * ma.getHeight() / this.this$0.panArea.getHeight();
                this.this$0.updatingImageDisplay = true;
                this.this$0.view.setCenter(mpX, mpY);
                this.this$0.updatingImageDisplay = false;
            }
        }
    }
}

