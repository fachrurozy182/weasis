/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.util;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public final class ShapeUtil {
    private static final int NON_RECTILINEAR_TRANSFORM_MASK = 48;

    private ShapeUtil() {
    }

    public static boolean isInvalidArea(@NotNull Rectangle2D area) {
        return area.isEmpty() || Double.isNaN(area.getWidth()) || Double.isNaN(area.getHeight());
    }

    @NotNull
    public static Shape intersect(@NotNull Shape s1, @NotNull Shape s2, boolean keep1, boolean keep2) {
        if (s1 instanceof Rectangle && s2 instanceof Rectangle) {
            return ((Rectangle)s1).intersection((Rectangle)s2);
        }
        if (s1 instanceof Rectangle2D) {
            return ShapeUtil.intersectRectShape((Rectangle2D)s1, s2, keep1, keep2);
        }
        if (s2 instanceof Rectangle2D) {
            return ShapeUtil.intersectRectShape((Rectangle2D)s2, s1, keep2, keep1);
        }
        return ShapeUtil.intersectByArea(s1, s2, keep1, keep2);
    }

    @NotNull
    private static Shape intersectRectShape(@NotNull Rectangle2D r, @NotNull Shape s, boolean keep1, boolean keep2) {
        if (s instanceof Rectangle2D) {
            Rectangle2D r2 = (Rectangle2D)s;
            Rectangle2D outputRect = !keep1 ? r : (!keep2 ? r2 : new Rectangle2D.Float());
            double x1 = Math.max(r.getX(), r2.getX());
            double x2 = Math.min(r.getX() + r.getWidth(), r2.getX() + r2.getWidth());
            double y1 = Math.max(r.getY(), r2.getY());
            double y2 = Math.min(r.getY() + r.getHeight(), r2.getY() + r2.getHeight());
            if (x2 - x1 < 0.0 || y2 - y1 < 0.0) {
                outputRect.setFrameFromDiagonal(0.0, 0.0, 0.0, 0.0);
            } else {
                outputRect.setFrameFromDiagonal(x1, y1, x2, y2);
            }
            return outputRect;
        }
        if (r.contains(s.getBounds2D())) {
            if (keep2) {
                s = ShapeUtil.cloneShape(s);
            }
            return s;
        }
        return ShapeUtil.intersectByArea(r, s, keep1, keep2);
    }

    @NotNull
    private static Shape intersectByArea(@NotNull Shape s1, @NotNull Shape s2, boolean keep1, boolean keep2) {
        Area a1;
        if (!keep1 && s1 instanceof Area) {
            a1 = (Area)s1;
        } else if (!keep2 && s2 instanceof Area) {
            a1 = (Area)s2;
            s2 = s1;
        } else {
            a1 = new Area(s1);
        }
        Area a2 = s2 instanceof Area ? (Area)s2 : new Area(s2);
        a1.intersect(a2);
        if (a1.isRectangular()) {
            return a1.getBounds2D();
        }
        return a1;
    }

    @NotNull
    public static Shape transformShape(@NotNull Shape s, @NotNull AffineTransform transform) {
        if (transform.getType() > 1) {
            return ShapeUtil.transformShape(transform, s);
        }
        return ShapeUtil.transformShape(transform.getTranslateX(), transform.getTranslateY(), s);
    }

    private static Shape transformShape(@NotNull AffineTransform tx, @NotNull Shape shape) {
        if (shape instanceof Rectangle2D && (tx.getType() & 0x30) == 0) {
            double[] matrix;
            Rectangle2D rect = (Rectangle2D)shape;
            matrix = new double[]{rect.getX(), rect.getY(), matrix[0] + rect.getWidth(), matrix[1] + rect.getHeight()};
            tx.transform(matrix, 0, matrix, 0, 2);
            ShapeUtil.fixRectangleOrientation(matrix, rect);
            return new Rectangle2D.Double(matrix[0], matrix[1], matrix[2] - matrix[0], matrix[3] - matrix[1]);
        }
        if (tx.isIdentity()) {
            return ShapeUtil.cloneShape(shape);
        }
        return tx.createTransformedShape(shape);
    }

    private static void fixRectangleOrientation(double[] m, @NotNull Rectangle2D r) {
        double t;
        if (r.getWidth() > 0.0 != m[2] - m[0] > 0.0) {
            t = m[0];
            m[0] = m[2];
            m[2] = t;
        }
        if (r.getHeight() > 0.0 != m[3] - m[1] > 0.0) {
            t = m[1];
            m[1] = m[3];
            m[3] = t;
        }
    }

    @NotNull
    private static Shape transformShape(double tx, double ty, @NotNull Shape s) {
        if (s instanceof Rectangle2D) {
            Rectangle2D rect = (Rectangle2D)s;
            return new Rectangle2D.Double(rect.getX() + tx, rect.getY() + ty, rect.getWidth(), rect.getHeight());
        }
        if (tx == 0.0 && ty == 0.0) {
            return ShapeUtil.cloneShape(s);
        }
        AffineTransform mat = AffineTransform.getTranslateInstance(tx, ty);
        return mat.createTransformedShape(s);
    }

    private static Shape cloneShape(Shape s) {
        return new GeneralPath(s);
    }
}

