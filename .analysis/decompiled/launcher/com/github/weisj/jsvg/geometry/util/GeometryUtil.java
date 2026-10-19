/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.geometry.util;

import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.geometry.size.FloatInsets;
import com.github.weisj.jsvg.geometry.util.PathLengthCalculator;
import com.github.weisj.jsvg.geometry.util.TransformUtil;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GeometryUtil {
    private static final float EPS = 1.0E-4f;

    private GeometryUtil() {
    }

    public static boolean approximatelyEqual(double a, double b) {
        return Math.abs(a - b) < (double)1.0E-4f;
    }

    public static boolean approximatelyZero(double a) {
        return GeometryUtil.approximatelyEqual(a, 0.0);
    }

    public static boolean notablyGreater(double a, double b) {
        return a - b > (double)1.0E-4f;
    }

    public static boolean approximatelyNegative(double a) {
        return a < (double)1.0E-4f;
    }

    public static boolean approximatelyPositive(double a) {
        return a > (double)-1.0E-4f;
    }

    public static double scaleXOfTransform(@Nullable AffineTransform at) {
        if (at == null) {
            return 1.0;
        }
        double sx = at.getScaleX();
        double shy = at.getShearY();
        return Math.sqrt(sx * sx + shy * shy);
    }

    public static double scaleYOfTransform(@Nullable AffineTransform at) {
        if (at == null) {
            return 1.0;
        }
        double sy = at.getScaleY();
        double shx = at.getShearX();
        return Math.sqrt(sy * sy + shx * shx);
    }

    @NotNull
    public static Point2D.Float midPoint(@NotNull Point2D.Float x, @NotNull Point2D.Float y) {
        return new Point2D.Float((x.x + y.x) / 2.0f, (x.y + y.y) / 2.0f);
    }

    @NotNull
    public static Point2D.Float lerp(float t, @NotNull Point2D.Float a, @NotNull Point2D.Float b) {
        return new Point2D.Float(GeometryUtil.lerp(t, b.x, a.x), GeometryUtil.lerp(t, b.y, a.y));
    }

    public static float lerp(float t, float a, float b) {
        return (1.0f - t) * a + t * b;
    }

    public static double lerp(float t, double a, double b) {
        return (double)(1.0f - t) * a + (double)t * b;
    }

    public static double distanceSquared(@NotNull Point2D.Float p1, @NotNull Point2D.Float p2, float scaleX, float scaleY) {
        return GeometryUtil.distanceSquared(scaleX * p1.x, scaleY * p1.y, (double)(scaleX * p2.x), (double)(scaleY * p2.y));
    }

    public static double distanceSquared(@NotNull Point2D.Float p1, @NotNull Point2D.Float p2) {
        return GeometryUtil.distanceSquared(p1.x, p1.y, (double)p2.x, (double)p2.y);
    }

    public static double distanceSquared(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public static double pathLength(@NotNull Shape shape) {
        return GeometryUtil.pathLength(shape.getPathIterator(null));
    }

    private static double pathLength(@NotNull PathIterator pathIterator) {
        PathLengthCalculator pathLengthCalculator = new PathLengthCalculator();
        double length = 0.0;
        double[] args = new double[6];
        while (!pathIterator.isDone()) {
            length += pathLengthCalculator.segmentLength(pathIterator.currentSegment(args), args);
            pathIterator.next();
        }
        return length;
    }

    public static boolean isSingleClosedPath(@NotNull Shape shape) {
        PathIterator pathIterator = shape.getPathIterator(null);
        int numPaths = 0;
        int numClosedPaths = 0;
        float[] segment = new float[6];
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(segment)) {
                case 0: {
                    ++numPaths;
                    break;
                }
                case 4: {
                    ++numClosedPaths;
                    break;
                }
            }
            if (numPaths > 1 || numClosedPaths > 1) {
                return false;
            }
            pathIterator.next();
        }
        return numPaths == 1 && numClosedPaths == 1;
    }

    public static double lineLength(double x1, double y1, double x2, double y2) {
        return Math.sqrt(GeometryUtil.distanceSquared(x1, y1, x2, y2));
    }

    @NotNull
    public static Rectangle2D containingBoundsAfterTransform(@NotNull AffineTransform transform, @NotNull Rectangle2D rect) {
        if (transform.isIdentity()) {
            return rect.getBounds2D();
        }
        Point2D.Double p1 = new Point2D.Double(rect.getX(), rect.getY());
        Point2D.Double p2 = new Point2D.Double(rect.getX() + rect.getWidth(), rect.getY());
        Point2D.Double p3 = new Point2D.Double(rect.getX(), rect.getY() + rect.getHeight());
        Point2D.Double p4 = new Point2D.Double(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
        Rectangle2D r1 = rect.getBounds2D();
        r1.setFrameFromDiagonal(transform.transform(p1, p1), transform.transform(p2, p2));
        Rectangle2D r2 = rect.getBounds2D();
        r2.setFrameFromDiagonal(transform.transform(p3, p3), transform.transform(p4, p4));
        Rectangle2D.union(r1, r2, r1);
        return r1;
    }

    public static float left(@NotNull Rectangle2D rect) {
        return (float)rect.getX();
    }

    public static float top(@NotNull Rectangle2D rect) {
        return (float)rect.getY();
    }

    public static float right(@NotNull Rectangle2D rect) {
        return (float)(rect.getX() + rect.getWidth());
    }

    public static float bottom(@NotNull Rectangle2D rect) {
        return (float)(rect.getY() + rect.getHeight());
    }

    @NotNull
    public static Rectangle2D grow(@NotNull Rectangle2D bounds, FloatInsets grow) {
        return new Rectangle2D.Double(bounds.getX() - (double)grow.left(), bounds.getY() - (double)grow.top(), bounds.getWidth() + (double)grow.left() + (double)grow.right(), bounds.getHeight() + (double)grow.top() + (double)grow.bottom());
    }

    @NotNull
    public static Rectangle2D grow(@NotNull Rectangle2D bounds, double increase) {
        return new Rectangle2D.Double(bounds.getX() - increase, bounds.getY() - increase, bounds.getWidth() + 2.0 * increase, bounds.getHeight() + 2.0 * increase);
    }

    @NotNull
    public static FloatInsets max(@NotNull FloatInsets in1, @NotNull FloatInsets in2) {
        return new FloatInsets(Math.max(in1.top(), in2.top()), Math.max(in1.left(), in2.left()), Math.max(in1.bottom(), in2.bottom()), Math.max(in1.right(), in2.right()));
    }

    @NotNull
    public static FloatInsets min(@NotNull FloatInsets in1, @NotNull FloatInsets in2) {
        return new FloatInsets(Math.min(in1.top(), in2.top()), Math.min(in1.left(), in2.left()), Math.min(in1.bottom(), in2.bottom()), Math.min(in1.right(), in2.right()));
    }

    @NotNull
    public static FloatInsets overhangInsets(@NotNull Rectangle2D reference, @NotNull Rectangle2D bounds) {
        return new FloatInsets(Math.max(0.0f, GeometryUtil.top(reference) - GeometryUtil.top(bounds)), Math.max(0.0f, GeometryUtil.left(reference) - GeometryUtil.left(bounds)), Math.max(0.0f, GeometryUtil.bottom(bounds) - GeometryUtil.bottom(reference)), Math.max(0.0f, GeometryUtil.right(bounds) - GeometryUtil.right(reference)));
    }

    @NotNull
    public static String compactRepresentation(@NotNull Rectangle2D rect) {
        return "[" + rect.getX() + ", " + rect.getY() + ", " + rect.getWidth() + "x" + rect.getHeight() + "]";
    }

    @NotNull
    public static Rectangle2D toIntegerBounds(@NotNull Rectangle2D in, @NotNull Rectangle2D out) {
        double minY = Math.floor(in.getMinY());
        double minX = Math.floor(in.getMinX());
        double maxX = Math.ceil(in.getMaxX());
        double maxY = Math.ceil(in.getMaxY());
        out.setFrame(minX, minY, maxX - minX, maxY - minY);
        return out;
    }

    @NotNull
    public static Rectangle2D adjustForAliasing(@NotNull Rectangle2D r) {
        return GeometryUtil.toIntegerBounds(r, r);
    }

    @NotNull
    public static AffineTransform createInverse(@NotNull AffineTransform at) {
        try {
            return at.createInverse();
        }
        catch (NoninvertibleTransformException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nullable
    public static AffineTransform toAwtTransform(@NotNull RenderContext context, @Nullable TransformValue transform) {
        if (transform == null) {
            return null;
        }
        return transform.get(context.measureContext());
    }

    @NotNull
    public static Point2D lastPointOnPath(@NotNull PathIterator pathIterator) {
        Point2D.Double lastPoint = new Point2D.Double();
        Point2D.Double lastMoveToPoint = new Point2D.Double();
        double[] args = new double[6];
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(args)) {
                case 0: {
                    ((Point2D)lastPoint).setLocation(args[0], args[1]);
                    ((Point2D)lastMoveToPoint).setLocation(args[0], args[1]);
                    break;
                }
                case 1: {
                    ((Point2D)lastPoint).setLocation(args[0], args[1]);
                    break;
                }
                case 2: {
                    ((Point2D)lastPoint).setLocation(args[2], args[3]);
                    break;
                }
                case 3: {
                    ((Point2D)lastPoint).setLocation(args[4], args[5]);
                    break;
                }
                case 4: {
                    ((Point2D)lastPoint).setLocation(((Point2D)lastMoveToPoint).getX(), ((Point2D)lastMoveToPoint).getY());
                    break;
                }
            }
            pathIterator.next();
        }
        return lastPoint;
    }

    @NotNull
    public static Rectangle2D convertBounds(@NotNull RenderContext context, @NotNull Rectangle2D r, @NotNull Space from, @NotNull Space to) {
        if (from == to) {
            return r;
        }
        Rectangle2D out = r;
        if (from == Space.USER) {
            if (to == Space.ROOT) {
                out = GeometryUtil.containingBoundsAfterTransform(context.userSpaceTransform(), r);
            } else if (to == Space.DEVICE) {
                out = GeometryUtil.containingBoundsAfterTransform(context.userSpaceTransform(), r);
                out = GeometryUtil.containingBoundsAfterTransform(context.rootTransform(), out);
            }
        }
        if (from == Space.ROOT) {
            if (to == Space.USER) {
                out = GeometryUtil.containingBoundsAfterTransform(GeometryUtil.createInverse(context.rootTransform()), r);
            } else if (to == Space.DEVICE) {
                out = GeometryUtil.containingBoundsAfterTransform(context.rootTransform(), r);
            }
        }
        if (from == Space.DEVICE) {
            if (to == Space.USER) {
                out = GeometryUtil.containingBoundsAfterTransform(GeometryUtil.createInverse(context.rootTransform()), r);
                out = GeometryUtil.containingBoundsAfterTransform(GeometryUtil.createInverse(context.userSpaceTransform()), out);
            } else if (to == Space.ROOT) {
                out = GeometryUtil.containingBoundsAfterTransform(GeometryUtil.createInverse(context.rootTransform()), r);
            }
        }
        return out;
    }

    @NotNull
    public static Rectangle2D userBoundsToDeviceBounds(@NotNull RenderContext context, @NotNull Rectangle2D r) {
        return GeometryUtil.convertBounds(context, r, Space.USER, Space.DEVICE);
    }

    @NotNull
    public static Point2D getLocation(@NotNull Rectangle2D r) {
        return new Point2D.Double(r.getX(), r.getY());
    }

    public static boolean isValidRect(@NotNull Rectangle2D r) {
        return !Double.isNaN(r.getX()) && !Double.isNaN(r.getY()) && !Double.isNaN(r.getWidth()) && !Double.isNaN(r.getHeight());
    }

    @NotNull
    public static AffineTransform interpolate(@NotNull AffineTransform a, @NotNull AffineTransform b, float t) {
        return TransformUtil.interpolate(a, b, t);
    }

    public static enum Space {
        USER,
        ROOT,
        DEVICE;

    }
}

