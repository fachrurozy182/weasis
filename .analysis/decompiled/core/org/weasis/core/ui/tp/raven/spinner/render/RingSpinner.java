/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.UIScale
 */
package org.weasis.core.ui.tp.raven.spinner.render;

import com.formdev.flatlaf.util.UIScale;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import org.weasis.core.ui.tp.raven.spinner.render.SpinnerRender;

public class RingSpinner
implements SpinnerRender {
    private final int size;

    public RingSpinner(int size) {
        this.size = size;
    }

    @Override
    public boolean isDisplayStringAble() {
        return true;
    }

    @Override
    public boolean isPaintComplete() {
        return true;
    }

    @Override
    public void paintCompleteIndeterminate(Graphics2D g2, Component component, Rectangle rec, float last, float f, float p) {
        g2.setColor(component.getBackground());
        g2.fill(this.createShape(rec, 0.0, 360.0));
        Point2D lastPoint = this.getPoint(last);
        double target = p * 360.0f;
        double targetStart = 360.0 - lastPoint.getX();
        double targetEnd = 360.0 - lastPoint.getY() + target;
        Shape shape = this.createShape(rec, lastPoint.getX() + targetStart * (double)f, lastPoint.getY() + targetEnd * (double)f);
        g2.setColor(component.getForeground());
        g2.fill(shape);
    }

    @Override
    public void paintIndeterminate(Graphics2D g2, Component component, Rectangle rec, float f) {
        Point2D p = this.getPoint(f);
        g2.setColor(component.getBackground());
        g2.fill(this.createShape(rec, 0.0, 360.0));
        Shape shape = this.createShape(rec, p.getX(), p.getY());
        g2.setColor(component.getForeground());
        g2.fill(shape);
    }

    @Override
    public void paintDeterminate(Graphics2D g2, Component component, Rectangle rec, float p) {
        g2.setColor(component.getBackground());
        g2.fill(this.createShape(rec, 0.0, 360.0));
        g2.setColor(component.getForeground());
        g2.fill(this.createShape(rec, 0.0, p * 360.0f));
    }

    @Override
    public int getInsets() {
        return UIScale.scale((int)(this.size + 5));
    }

    private Shape createShape(Rectangle rec, double start, double end) {
        start *= -1.0;
        end *= -1.0;
        double add = (end += 90.0) - (start += 90.0);
        Area area = new Area(new Arc2D.Double(rec.x, rec.y, rec.width, rec.height, start, add, 2));
        float lineWidth = UIScale.scale((int)this.size);
        float x = (float)rec.x + lineWidth;
        float y = (float)rec.y + lineWidth;
        float width = (float)rec.width - lineWidth * 2.0f;
        float height = (float)rec.height - lineWidth * 2.0f;
        area.subtract(new Area(new Arc2D.Double(x, y, width, height, 0.0, 360.0, 2)));
        return area;
    }

    private Point2D getPoint(float f) {
        double start;
        double end;
        double a = 50.0;
        double b = 360.0 - a;
        if (f > 1.0f) {
            float ease = RingSpinner.easeInOutQuad(f -= 1.0f);
            end = b + (double)f * a;
            start = a + (double)ease * b;
        } else {
            float ease = RingSpinner.easeInOutQuad(f);
            end = (double)ease * b;
            start = (double)f * a;
        }
        return new Point2D.Double(start, end);
    }

    public static float easeInOutQuad(float x) {
        double v = (double)x < 0.5 ? (double)(2.0f * x * x) : 1.0 - Math.pow(-2.0f * x + 2.0f, 2.0) / 2.0;
        return (float)v;
    }
}

