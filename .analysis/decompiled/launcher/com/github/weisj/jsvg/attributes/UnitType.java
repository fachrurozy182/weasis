/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public enum UnitType {
    UserSpaceOnUse,
    ObjectBoundingBox;


    @NotNull
    public MeasureContext deriveMeasure(@NotNull MeasureContext measure) {
        return this.deriveMeasure(measure, 1.0, 1.0);
    }

    @NotNull
    public MeasureContext deriveMeasure(@NotNull MeasureContext measure, double objectWidth, double objectHeight) {
        if (this == ObjectBoundingBox) {
            return measure.derive((float)objectWidth, (float)objectHeight);
        }
        return measure;
    }

    @NotNull
    public AffineTransform viewTransform(@NotNull Rectangle2D bounds) {
        if (this == ObjectBoundingBox) {
            AffineTransform at = AffineTransform.getTranslateInstance(bounds.getX(), bounds.getY());
            at.scale(bounds.getWidth(), bounds.getHeight());
            return at;
        }
        return new AffineTransform();
    }

    @NotNull
    public Rectangle2D.Double computeViewBounds(@NotNull MeasureContext measure, @NotNull Rectangle2D elementBounds, @NotNull Length x, @NotNull Length y, @NotNull Length width, @NotNull Length height) {
        MeasureContext patternMeasure = this.deriveMeasure(measure, elementBounds.getWidth(), elementBounds.getHeight());
        Rectangle2D.Double viewBounds = new Rectangle2D.Double(x.resolve(patternMeasure), y.resolve(patternMeasure), width.resolve(patternMeasure), height.resolve(patternMeasure));
        if (this == ObjectBoundingBox) {
            viewBounds.x += elementBounds.getX();
            viewBounds.y += elementBounds.getY();
        }
        return viewBounds;
    }
}

