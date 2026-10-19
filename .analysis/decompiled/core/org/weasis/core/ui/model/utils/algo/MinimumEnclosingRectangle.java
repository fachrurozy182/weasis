/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.utils.algo;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.weasis.core.ui.model.utils.algo.ConvexHull;
import org.weasis.core.util.MathUtil;

public class MinimumEnclosingRectangle {
    private final List<Point2D> points;
    private final boolean isConvex;
    private List<Point2D> cvxPts = null;
    private Line2D minDistSeg = new Line2D.Double();
    private Point2D minDistPt = null;
    private double minDist = 0.0;

    public MinimumEnclosingRectangle(List<Point2D> points) {
        this(points, false);
    }

    public MinimumEnclosingRectangle(List<Point2D> points, boolean isConvex) {
        this.points = points;
        this.isConvex = isConvex;
    }

    private void computeWidthConvex(List<Point2D> pts) {
        this.cvxPts = pts;
        int size = this.cvxPts.size();
        if (size == 0) {
            this.minDist = 0.0;
            this.minDistPt = null;
            this.minDistSeg = null;
        } else if (size == 1) {
            this.minDist = 0.0;
            this.minDistPt = this.cvxPts.get(0);
            this.minDistSeg.setLine(this.minDistPt.getX(), this.minDistPt.getY(), this.minDistPt.getX(), this.minDistPt.getY());
        } else if (size == 2 || size == 3) {
            this.minDist = 0.0;
            this.minDistPt = this.cvxPts.get(0);
            Point2D p2 = this.cvxPts.get(1);
            this.minDistSeg.setLine(this.minDistPt.getX(), this.minDistPt.getY(), p2.getX(), p2.getY());
        } else {
            this.searchRingMinDiameter(this.cvxPts);
        }
    }

    private void searchRingMinDiameter(List<Point2D> pts) {
        this.minDist = Double.MAX_VALUE;
        int curIndex = 1;
        for (int i = 0; i < pts.size() - 1; ++i) {
            curIndex = this.findMaxPerpendicularDistanceFromAB(pts, pts.get(i), pts.get(i + 1), curIndex);
        }
    }

    private int findMaxPerpendicularDistanceFromAB(List<Point2D> pts, Point2D ptA, Point2D ptB, int startIndex) {
        int maxIndex;
        double maxDist;
        double nextDist = maxDist = MinimumEnclosingRectangle.distanceFromAB(pts.get(startIndex), ptA, ptB);
        int nextIndex = maxIndex = startIndex;
        while (nextDist >= maxDist) {
            maxDist = nextDist;
            maxIndex = nextIndex;
            nextIndex = MinimumEnclosingRectangle.nextIndex(pts, maxIndex);
            nextDist = MinimumEnclosingRectangle.distanceFromAB(pts.get(nextIndex), ptA, ptB);
        }
        if (maxDist < this.minDist) {
            this.minDist = maxDist;
            this.minDistPt = pts.get(maxIndex);
            this.minDistSeg = new Line2D.Double(ptA, ptB);
        }
        return maxIndex;
    }

    private static int nextIndex(List<Point2D> pts, int index) {
        int i = index + 1;
        return i >= pts.size() ? 0 : i;
    }

    public static double distanceFromAB(Point2D p, Point2D a, Point2D b) {
        double len2 = (b.getX() - a.getX()) * (b.getX() - a.getX()) + (b.getY() - a.getY()) * (b.getY() - a.getY());
        double s = ((a.getY() - p.getY()) * (b.getX() - a.getX()) - (a.getX() - p.getX()) * (b.getY() - a.getY())) / len2;
        return Math.abs(s) * Math.sqrt(len2);
    }

    public List<Point2D> getMinimumRectangle() {
        if (this.minDistPt == null) {
            if (this.isConvex) {
                this.computeWidthConvex(this.points);
            } else {
                List<Point2D> convexPts = new ConvexHull(this.points).getConvexHull();
                this.computeWidthConvex(convexPts);
            }
        }
        if (MathUtil.isEqualToZero((double)this.minDist)) {
            return Collections.emptyList();
        }
        double dx = this.minDistSeg.getX2() - this.minDistSeg.getX1();
        double dy = this.minDistSeg.getY2() - this.minDistSeg.getY1();
        double minPara = Double.MAX_VALUE;
        double maxPara = -1.7976931348623157E308;
        double minPerp = Double.MAX_VALUE;
        double maxPerp = -1.7976931348623157E308;
        for (Point2D cvxPt : this.cvxPts) {
            double perpC;
            double paraC = MinimumEnclosingRectangle.getC(dx, dy, cvxPt);
            if (paraC > maxPara) {
                maxPara = paraC;
            }
            if (paraC < minPara) {
                minPara = paraC;
            }
            if ((perpC = MinimumEnclosingRectangle.getC(-dy, dx, cvxPt)) > maxPerp) {
                maxPerp = perpC;
            }
            if (!(perpC < minPerp)) continue;
            minPerp = perpC;
        }
        Line2D.Double maxPerpLine = MinimumEnclosingRectangle.getLine(-dx, -dy, maxPerp);
        Line2D.Double minPerpLine = MinimumEnclosingRectangle.getLine(-dx, -dy, minPerp);
        Line2D.Double maxParaLine = MinimumEnclosingRectangle.getLine(-dy, dx, maxPara);
        Line2D.Double minParaLine = MinimumEnclosingRectangle.getLine(-dy, dx, minPara);
        ArrayList<Point2D> rect = new ArrayList<Point2D>();
        rect.add(MinimumEnclosingRectangle.intersection(maxParaLine.getP1(), maxParaLine.getP2(), maxPerpLine.getP1(), maxPerpLine.getP2()));
        rect.add(MinimumEnclosingRectangle.intersection(minParaLine.getP1(), minParaLine.getP2(), maxPerpLine.getP1(), maxPerpLine.getP2()));
        rect.add(MinimumEnclosingRectangle.intersection(minParaLine.getP1(), minParaLine.getP2(), minPerpLine.getP1(), minPerpLine.getP2()));
        rect.add(MinimumEnclosingRectangle.intersection(maxParaLine.getP1(), maxParaLine.getP2(), minPerpLine.getP1(), minPerpLine.getP2()));
        return rect;
    }

    public static Point2D intersection(Point2D u1, Point2D u2, Point2D w1, Point2D w2) {
        double ux = u1.getY() - u2.getY();
        double uy = u2.getX() - u1.getX();
        double ul = u1.getX() * u2.getY() - u2.getX() * u1.getY();
        double wx = w1.getY() - w2.getY();
        double wy = w2.getX() - w1.getX();
        double wl = w1.getX() * w2.getY() - w2.getX() * w1.getY();
        double x = uy * wl - wy * ul;
        double y = wx * ul - ux * wl;
        double l = ux * wy - wx * uy;
        double xNorm = x / l;
        double yNorm = y / l;
        if (Double.isNaN(xNorm) || Double.isInfinite(xNorm) || Double.isNaN(yNorm) || Double.isInfinite(yNorm)) {
            return null;
        }
        return new Point2D.Double(xNorm, yNorm);
    }

    private static double getC(double a, double b, Point2D p) {
        return a * p.getY() - b * p.getX();
    }

    private static Line2D.Double getLine(double a, double b, double c) {
        if (Math.abs(b) > Math.abs(a)) {
            return new Line2D.Double(0.0, c / b, 1.0, c / b - a / b);
        }
        return new Line2D.Double(c / a, 0.0, c / a - b / a, 1.0);
    }
}

