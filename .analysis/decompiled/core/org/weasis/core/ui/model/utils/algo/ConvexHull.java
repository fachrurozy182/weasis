/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.utils.algo;

import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.TreeSet;
import org.weasis.core.util.MathUtil;

public class ConvexHull {
    public static final int COUNTERCLOCKWISE = 1;
    public static final int CLOCKWISE = -1;
    private final List<Point2D> pts;

    public ConvexHull(List<Point2D> pts) {
        this.pts = ConvexHull.removeDuplicates(pts);
    }

    public static List<Point2D> removeDuplicates(List<Point2D> points) {
        TreeSet<Point2D> treeSet = new TreeSet<Point2D>(Comparator.comparingDouble(Point2D::getY).thenComparingDouble(Point2D::getX));
        treeSet.addAll(points);
        return new ArrayList<Point2D>(treeSet);
    }

    public List<Point2D> getConvexHull() {
        if (this.pts.size() < 3) {
            return this.pts;
        }
        return new ArrayList<Point2D>(ConvexHull.grahamScan(ConvexHull.preSort(this.pts)));
    }

    private static List<Point2D> preSort(List<Point2D> pts) {
        Point2D p = (Point2D)pts.getFirst();
        for (int i = 1; i < pts.size(); ++i) {
            Point2D pc = pts.get(i);
            if (!(pc.getY() < p.getY()) && (!MathUtil.isEqual((double)pc.getY(), (double)p.getY()) || !(pc.getX() < p.getX()))) continue;
            p = pc;
            Collections.swap(pts, 0, i);
        }
        pts.sort(new RadialSorter(p));
        return pts;
    }

    private static Deque<Point2D> grahamScan(List<Point2D> pts) {
        ArrayDeque<Point2D> ps = new ArrayDeque<Point2D>();
        ps.addFirst(pts.get(2));
        ps.addFirst(pts.get(1));
        ps.addFirst(pts.get(0));
        for (int i = 3; i < pts.size(); ++i) {
            Point2D p = (Point2D)ps.removeLast();
            Point2D pc = pts.get(i);
            while (!ps.isEmpty() && ConvexHull.getOrientation(ps.peekLast(), p, pc) > 0) {
                p = ps.removeLast();
            }
            ps.addLast(p);
            ps.addLast(pc);
        }
        ps.addLast((Point2D)pts.getFirst());
        return ps;
    }

    private static int signum(double x) {
        if (x > 0.0) {
            return 1;
        }
        if (x < 0.0) {
            return -1;
        }
        return 0;
    }

    public static int getOrientation(Point2D a, Point2D b, Point2D c) {
        return ConvexHull.signum((b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX()));
    }

    private record RadialSorter(Point2D origin) implements Comparator<Point2D>
    {
        @Override
        public int compare(Point2D p1, Point2D p2) {
            return RadialSorter.polarCompare(this.origin, p1, p2);
        }

        private static int polarCompare(Point2D o, Point2D p, Point2D q) {
            double dxp = p.getX() - o.getX();
            double dyp = p.getY() - o.getY();
            double dxq = q.getX() - o.getX();
            double dyq = q.getY() - o.getY();
            int orient = ConvexHull.getOrientation(o, p, q);
            if (orient == 1) {
                return 1;
            }
            if (orient == -1) {
                return -1;
            }
            double op = dxp * dxp + dyp * dyp;
            double oq = dxq * dxq + dyq * dyq;
            return Double.compare(op, oq);
        }
    }
}

