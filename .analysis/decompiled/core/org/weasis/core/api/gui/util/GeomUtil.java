/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.api.gui.util;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import org.weasis.core.util.MathUtil;

public final class GeomUtil {
    private GeomUtil() {
    }

    public static boolean isLineValid(Point2D ptA, Point2D ptB) {
        return ptA != null && ptB != null && !ptA.equals(ptB);
    }

    public static double getAngleRad(Point2D ptA, Point2D ptB, Point2D ptC) {
        if (ptA != null && ptB != null && ptC != null) {
            return GeomUtil.getAngleRad(ptB, ptC) - GeomUtil.getAngleRad(ptB, ptA);
        }
        return 0.0;
    }

    public static double getAngleDeg(Point2D ptA, Point2D ptB, Point2D ptC) {
        if (ptA != null && ptB != null && ptC != null) {
            return Math.toDegrees(GeomUtil.getAngleRad(ptA, ptB, ptC));
        }
        return 0.0;
    }

    public static double getAngleRad(Point2D ptA, Point2D ptB) {
        return Math.atan2(ptA.getY() - ptB.getY(), ptB.getX() - ptA.getX());
    }

    public static double getAngleDeg(Point2D ptA, Point2D ptB) {
        return Math.toDegrees(GeomUtil.getAngleRad(ptA, ptB));
    }

    public static double normalizeAngle(double angle) {
        if ((angle = (angle + Math.PI) % (Math.PI * 2) - Math.PI) > Math.PI) {
            angle -= Math.PI * 2;
        } else if (angle < -Math.PI) {
            angle += Math.PI * 2;
        }
        return angle;
    }

    public static double getSmallestRotationAngleRad(double angle) {
        double a = angle % (Math.PI * 2);
        if (Math.abs(a) > Math.PI) {
            a -= Math.signum(a) * (Math.PI * 2);
        }
        return a;
    }

    public static double getSmallestRotationAngleDeg(double angle) {
        double a = angle % 360.0;
        if (Math.abs(a) > 180.0) {
            a -= Math.signum(a) * 360.0;
        }
        return a;
    }

    public static double getSmallestAngleRad(double angle) {
        double a = angle % Math.PI;
        if (Math.abs(a) > 1.5707963267948966) {
            a -= Math.signum(a) * Math.PI;
        }
        return a;
    }

    public static double getSmallestAngleDeg(double angle) {
        double a = angle % 180.0;
        if (Math.abs(a) > 90.0) {
            a -= Math.signum(a) * 180.0;
        }
        return a;
    }

    public static Point2D getMidPoint(Point2D ptA, Point2D ptB) {
        if (ptA != null && ptB != null) {
            return new Point2D.Double((ptA.getX() + ptB.getX()) / 2.0, (ptA.getY() + ptB.getY()) / 2.0);
        }
        return null;
    }

    public static Point2D getColinearPointWithLength(Point2D ptA, Point2D ptB, double newLength) {
        if (ptA != null && ptB != null) {
            return GeomUtil.getCollinearPointWithRatio(ptA, ptB, newLength / ptA.distance(ptB));
        }
        return null;
    }

    public static Point2D.Double getCollinearPointWithRatio(Point2D ptA, Point2D ptB, double k) {
        if (ptA != null && ptB != null) {
            return new Point2D.Double(ptB.getX() * k + ptA.getX() * (1.0 - k), ptB.getY() * k + ptA.getY() * (1.0 - k));
        }
        return null;
    }

    public static Line2D getMedianLine(Line2D line1, Line2D line2) {
        Point2D.Double ptN;
        Point2D.Double ptM;
        Point2D ptD;
        Line2D.Double line4;
        if (line1 == null || line2 == null) {
            return null;
        }
        Point2D ptA = line1.getP1();
        Point2D ptB = line1.getP2();
        Point2D ptC = line2.getP1();
        Line2D.Double line3 = new Line2D.Double(ptA, ptC);
        if (line3.intersectsLine(line4 = new Line2D.Double(ptB, ptD = line2.getP2()))) {
            ptM = new Point2D.Double((ptA.getX() + ptD.getX()) / 2.0, (ptA.getY() + ptD.getY()) / 2.0);
            ptN = new Point2D.Double((ptB.getX() + ptC.getX()) / 2.0, (ptB.getY() + ptC.getY()) / 2.0);
        } else {
            ptM = new Point2D.Double((ptA.getX() + ptC.getX()) / 2.0, (ptA.getY() + ptC.getY()) / 2.0);
            ptN = new Point2D.Double((ptB.getX() + ptD.getX()) / 2.0, (ptB.getY() + ptD.getY()) / 2.0);
        }
        return new Line2D.Double(ptM, ptN);
    }

    public static Line2D getMedianLine(Point2D ptA, Point2D ptB, Point2D ptC, Point2D ptD) {
        if (ptA == null || ptB == null || ptC == null || ptD == null) {
            return null;
        }
        return GeomUtil.getMedianLine(new Line2D.Double(ptA, ptB), new Line2D.Double(ptC, ptD));
    }

    public static Point2D getIntersectPoint(Point2D ptA, Point2D ptB, Point2D ptC, Point2D ptD) {
        if (ptA == null || ptB == null || ptC == null || ptD == null) {
            return null;
        }
        Point2D.Double ptP = null;
        double denominator = (ptB.getX() - ptA.getX()) * (ptD.getY() - ptC.getY()) - (ptB.getY() - ptA.getY()) * (ptD.getX() - ptC.getX());
        if (MathUtil.isDifferentFromZero((double)denominator)) {
            double numerator = (ptA.getY() - ptC.getY()) * (ptD.getX() - ptC.getX()) - (ptA.getX() - ptC.getX()) * (ptD.getY() - ptC.getY());
            double r = numerator / denominator;
            ptP = new Point2D.Double(ptA.getX() + r * (ptB.getX() - ptA.getX()), ptA.getY() + r * (ptB.getY() - ptA.getY()));
        }
        return ptP;
    }

    public static Point2D getIntersectPoint(Line2D line1, Line2D line2) {
        if (line1 == null || line2 == null) {
            return null;
        }
        return GeomUtil.getIntersectPoint(line1.getP1(), line1.getP2(), line2.getP1(), line2.getP2());
    }

    public static Point2D getIntersectPoint(Line2D line, Rectangle2D rect) {
        if (line == null || rect == null) {
            return null;
        }
        Point2D p = GeomUtil.lineIntersection(line, new Line2D.Double(rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMinY()));
        if (p == null && (p = GeomUtil.lineIntersection(line, new Line2D.Double(rect.getMinX(), rect.getMaxY(), rect.getMaxX(), rect.getMaxY()))) == null && (p = GeomUtil.lineIntersection(line, new Line2D.Double(rect.getMinX(), rect.getMinY(), rect.getMinX(), rect.getMaxY()))) == null) {
            p = GeomUtil.lineIntersection(line, new Line2D.Double(rect.getMaxX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY()));
        }
        return p;
    }

    public static Line2D cropLine(Line2D line, Rectangle2D rect) {
        if (line == null || rect == null || rect.isEmpty()) {
            return line;
        }
        Point2D p1 = GeomUtil.lineIntersection(line, new Line2D.Double(rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMinY()));
        Point2D p2 = GeomUtil.lineIntersection(line, new Line2D.Double(rect.getMinX(), rect.getMaxY(), rect.getMaxX(), rect.getMaxY()));
        Point2D p3 = GeomUtil.lineIntersection(line, new Line2D.Double(rect.getMinX(), rect.getMinY(), rect.getMinX(), rect.getMaxY()));
        Point2D p4 = GeomUtil.lineIntersection(line, new Line2D.Double(rect.getMaxX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY()));
        Point2D pl1 = null;
        Point2D pl2 = null;
        for (Point2D p : Arrays.asList(p1, p2, p3, p4)) {
            if (p == null) continue;
            if (pl1 == null) {
                pl1 = p;
                continue;
            }
            pl2 = p;
            break;
        }
        return GeomUtil.getLine2D(line, pl1, pl2, rect);
    }

    public static Line2D cropLine(Line2D line, Path2D path) {
        if (line == null || path == null) {
            return line;
        }
        Point2D pl1 = null;
        Point2D pl2 = null;
        FlatteningPathIterator iterator = new FlatteningPathIterator(path.getPathIterator(null), 2.0);
        double[] coords = new double[6];
        Point2D.Double lastPoint = null;
        while (!iterator.isDone()) {
            int segmentType = iterator.currentSegment(coords);
            if (segmentType == 0) {
                lastPoint = new Point2D.Double(coords[0], coords[1]);
            } else if (segmentType == 1) {
                Point2D.Double currentPoint = new Point2D.Double(coords[0], coords[1]);
                Point2D intersection = GeomUtil.lineIntersection(line, new Line2D.Double(lastPoint, currentPoint));
                if (intersection != null) {
                    if (pl1 == null) {
                        pl1 = intersection;
                    } else {
                        pl2 = intersection;
                        break;
                    }
                }
                lastPoint = currentPoint;
            }
            iterator.next();
        }
        return GeomUtil.getLine2D(line, pl1, pl2, path);
    }

    private static Line2D getLine2D(Line2D line, Point2D pl1, Point2D pl2, Shape path) {
        if (pl1 != null && pl2 != null) {
            return GeomUtil.keepLineOrientation(line, pl1, pl2);
        }
        if (pl1 != null) {
            if (path.contains(line.getP1())) {
                return GeomUtil.keepLineOrientation(line, pl1, line.getP1());
            }
            if (path.contains(line.getP2())) {
                return GeomUtil.keepLineOrientation(line, pl1, line.getP2());
            }
        }
        return line;
    }

    private static Line2D keepLineOrientation(Line2D line, Point2D pl1, Point2D pl2) {
        if (line.getP1().distance(pl1) < line.getP1().distance(pl2)) {
            return new Line2D.Double(pl1, pl2);
        }
        return new Line2D.Double(pl2, pl1);
    }

    private static Point2D lineIntersection(Line2D line1, Line2D line2) {
        if (line1.intersectsLine(line2)) {
            return GeomUtil.getIntersectPoint(line1, line2);
        }
        return null;
    }

    public static boolean lineParallel(Point2D ptA, Point2D ptB, Point2D ptC, Point2D ptD) {
        if (ptA == null || ptB == null || ptC == null || ptD == null) {
            throw new IllegalArgumentException("All the points must not be null");
        }
        return MathUtil.isEqualToZero((double)((ptB.getX() - ptA.getX()) * (ptD.getY() - ptC.getY()) - (ptB.getY() - ptA.getY()) * (ptD.getX() - ptC.getX())));
    }

    public static boolean lineColinear(Point2D ptA, Point2D ptB, Point2D ptC, Point2D ptD) {
        if (GeomUtil.lineParallel(ptA, ptB, ptC, ptD)) {
            return MathUtil.isEqualToZero((double)((ptA.getY() - ptC.getY()) * (ptD.getX() - ptC.getX()) - (ptA.getX() - ptC.getX()) * (ptD.getY() - ptC.getY())));
        }
        return false;
    }

    public static Point2D getPerpendicularPointToLine(Point2D ptA, Point2D ptB, Point2D ptC) {
        if (ptA == null || ptB == null || ptA.equals(ptB) || ptC == null) {
            return null;
        }
        double ax = ptA.getX();
        double ay = ptA.getY();
        double bx = ptB.getX();
        double by = ptB.getY();
        double cx = ptC.getX();
        double cy = ptC.getY();
        double r = ((ay - cy) * (ay - by) + (ax - cx) * (ax - bx)) / Point2D.distanceSq(ax, ay, bx, by);
        return new Point2D.Double(ax + r * (bx - ax), ay + r * (by - ay));
    }

    public static Point2D getPerpendicularPointToLine(Line2D line, Point2D ptC) {
        if (line == null || ptC == null) {
            return null;
        }
        return GeomUtil.getPerpendicularPointToLine(line.getP1(), line.getP2(), ptC);
    }

    public static Point2D getPerpendicularPointFromLine(Point2D ptA, Point2D ptB, Point2D ptP, double distPC) {
        if (ptA == null || ptB == null || ptA.equals(ptB) || ptP == null) {
            return null;
        }
        double distAB = ptA.distance(ptB);
        double ux = -(ptB.getY() - ptA.getY()) / distAB;
        double uy = (ptB.getX() - ptA.getX()) / distAB;
        return new Point2D.Double(ptP.getX() + distPC * ux, ptP.getY() + distPC * uy);
    }

    public static Point2D getPerpendicularPointFromLine(Point2D ptA, Point2D ptB, double distAP, double distPC) {
        return GeomUtil.getPerpendicularPointFromLine(ptA, ptB, GeomUtil.getColinearPointWithLength(ptA, ptB, distAP), distPC);
    }

    public static Line2D getParallelLine(Point2D ptA, Point2D ptB, double dist) {
        if (ptA == null || ptB == null || ptA.equals(ptB)) {
            return null;
        }
        double abX = ptB.getX() - ptA.getX();
        double abY = ptB.getY() - ptA.getY();
        double perpSize = Math.sqrt(abX * abX + abY * abY);
        double sideVectorX = dist * -abY / perpSize;
        double sideVectorY = dist * abX / perpSize;
        Point2D.Double ptC = new Point2D.Double(ptA.getX() + sideVectorX, ptA.getY() + sideVectorY);
        Point2D.Double ptD = new Point2D.Double(ptB.getX() + sideVectorX, ptB.getY() + sideVectorY);
        return new Line2D.Double(ptC, ptD);
    }

    public static Point2D getCircleCenter(List<Point2D> ptList) {
        if (ptList == null) {
            return null;
        }
        return switch (ptList.size()) {
            case 3 -> GeomUtil.getCircleCenter(ptList.get(0), ptList.get(1), ptList.get(2));
            case 2 -> new Point2D.Double((ptList.get(0).getX() + ptList.get(1).getX()) / 2.0, (ptList.get(0).getY() + ptList.get(1).getY()) / 2.0);
            default -> null;
        };
    }

    public static Point2D getCircleCenter(Point2D ptA, Point2D ptB, Point2D ptC) {
        if (ptA == null || ptB == null || ptC == null) {
            return null;
        }
        double ax = ptA.getX();
        double ay = ptA.getY();
        double bx = ptB.getX();
        double by = ptB.getY();
        double cx = ptC.getX();
        double cy = ptC.getY();
        double c1 = bx - ax;
        double c2 = by - ay;
        double c3 = cx - ax;
        double c4 = cy - ay;
        double c5 = c1 * (ax + bx) + c2 * (ay + by);
        double c6 = c3 * (ax + cx) + c4 * (ay + cy);
        double denom = 2.0 * (c1 * (cy - by) - c2 * (cx - bx));
        if (MathUtil.isEqualToZero((double)denom)) {
            return null;
        }
        double px = (c4 * c5 - c2 * c6) / denom;
        double py = (c1 * c6 - c3 * c5) / denom;
        return new Point2D.Double(px, py);
    }

    public static double extractScalingFactor(AffineTransform transform) {
        double scalingFactor = 1.0;
        if (transform != null) {
            double sx = transform.getScaleX();
            double shx = transform.getShearX();
            if (MathUtil.isDifferentFromZero((double)sx) || MathUtil.isDifferentFromZero((double)shx)) {
                scalingFactor = Math.sqrt(sx * sx + shx * shx);
            }
        }
        return scalingFactor;
    }

    public static double extractAngleRad(AffineTransform transform) {
        double angleRad = 0.0;
        if (transform != null) {
            double sinTheta = transform.getShearY();
            double cosTheta = transform.getScaleX();
            angleRad = Math.atan2(sinTheta, cosTheta);
            if ((transform.getType() & 0x40) != 0) {
                angleRad *= -1.0;
            }
        }
        return angleRad;
    }

    public static Shape getScaledShape(Shape shape, double scalingFactor, Point2D anchorPoint) {
        if (shape == null || MathUtil.isEqualToZero((double)scalingFactor)) {
            return null;
        }
        AffineTransform scaleTransform = new AffineTransform();
        if (MathUtil.isDifferent((double)scalingFactor, (double)1.0)) {
            if (anchorPoint != null) {
                scaleTransform.translate(anchorPoint.getX(), anchorPoint.getY());
            }
            scaleTransform.scale(scalingFactor, scalingFactor);
            if (anchorPoint != null) {
                scaleTransform.translate(-anchorPoint.getX(), -anchorPoint.getY());
            }
        }
        return scaleTransform.createTransformedShape(shape);
    }

    public static Rectangle2D getScaledRectangle(Rectangle2D rect, double scalingFactor) {
        Rectangle2D newRect = null;
        if (rect != null && MathUtil.isDifferent((double)scalingFactor, (double)1.0)) {
            double resizedWidth = rect.getWidth() * scalingFactor;
            double resizedHeight = rect.getHeight() * scalingFactor;
            newRect = (Rectangle2D)rect.clone();
            newRect.setRect(rect.getX(), rect.getY(), resizedWidth, resizedHeight);
        }
        return newRect;
    }

    public static Shape getCornerShape(Point2D ptA, Point2D ptO, Point2D ptB, double cornerSize) {
        if (ptA == null || ptO == null || ptB == null || ptA.equals(ptO) || ptB.equals(ptO)) {
            return null;
        }
        Point2D ptI1 = GeomUtil.getColinearPointWithLength(ptO, ptA, cornerSize);
        Point2D ptI2 = GeomUtil.getColinearPointWithLength(ptO, ptB, cornerSize);
        double rotSignum = Math.signum(GeomUtil.getSmallestRotationAngleDeg(GeomUtil.getAngleDeg(ptB, ptO, ptA)));
        Point2D ptI3 = GeomUtil.getPerpendicularPointFromLine(ptO, ptA, ptI1, rotSignum * cornerSize);
        if (ptI3 == null) {
            return null;
        }
        Path2D.Double path = new Path2D.Double(1, 3);
        ((Path2D)path).moveTo(ptI1.getX(), ptI1.getY());
        ((Path2D)path).lineTo(ptI3.getX(), ptI3.getY());
        ((Path2D)path).lineTo(ptI2.getX(), ptI2.getY());
        return path;
    }

    public static Shape getArrowShape(Point2D ptO, Point2D ptB, double length, double width) {
        if (ptO == null || ptB == null || ptB.equals(ptO)) {
            return null;
        }
        Point2D ptI2 = GeomUtil.getColinearPointWithLength(ptO, ptB, length);
        Point2D ptI3 = GeomUtil.getPerpendicularPointFromLine(ptO, ptB, ptI2, width / 2.0);
        if (ptI3 == null) {
            return null;
        }
        Path2D.Double path = new Path2D.Double(1, 5);
        ((Path2D)path).moveTo(ptI2.getX(), ptI2.getY());
        ((Path2D)path).lineTo(ptI3.getX(), ptI3.getY());
        ((Path2D)path).lineTo(ptO.getX(), ptO.getY());
        ((Path2D)path).lineTo(ptI2.getX() - (ptI3.getX() - ptI2.getX()), ptI2.getY() - (ptI3.getY() - ptI2.getY()));
        ((Path2D)path).lineTo(ptI2.getX(), ptI2.getY());
        return path;
    }

    public static Rectangle2D getGrowingRectangle(Rectangle2D rect, double growingSize) {
        if (rect == null) {
            return null;
        }
        Rectangle2D growingRect = (Rectangle2D)rect.clone();
        GeomUtil.growRectangle(growingRect, growingSize);
        return growingRect;
    }

    public static void growRectangle(Rectangle2D rect, double growingSize) {
        if (rect == null) {
            return;
        }
        if (MathUtil.isDifferentFromZero((double)growingSize)) {
            double newX = rect.getX() - growingSize;
            double newY = rect.getY() - growingSize;
            double newWidth = rect.getWidth() + 2.0 * growingSize;
            double newHeight = rect.getHeight() + 2.0 * growingSize;
            rect.setRect(newX, newY, newWidth, newHeight);
        }
    }
}

