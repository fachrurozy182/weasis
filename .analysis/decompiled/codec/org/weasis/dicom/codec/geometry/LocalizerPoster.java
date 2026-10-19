/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3d
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.dicom.codec.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.weasis.core.util.MathUtil;
import org.weasis.dicom.codec.geometry.GeometryOfSlice;

public abstract class LocalizerPoster {
    protected Vector3d localizerRow;
    protected Vector3d localizerColumn;
    protected Vector3d localizerNormal;
    protected Vector3d localizerTLHC;
    protected Vector3d localizerVoxelSpacing;
    protected Vector3d localizerDimensions;
    protected Matrix3d rotateIntoLocalizerSpace;

    public LocalizerPoster(Vector3d row, Vector3d column, Vector3d tlhc, Vector3d voxelSpacing, Vector3d dimensions) {
        this.localizerRow = row;
        this.localizerColumn = column;
        this.localizerTLHC = tlhc;
        this.localizerVoxelSpacing = voxelSpacing;
        this.localizerDimensions = dimensions;
        this.doCommonConstructorStuff();
    }

    public LocalizerPoster(GeometryOfSlice geometry) {
        this.localizerRow = geometry.getRow();
        this.localizerColumn = geometry.getColumn();
        this.localizerTLHC = geometry.getTLHC();
        this.localizerVoxelSpacing = geometry.getVoxelSpacing();
        this.localizerDimensions = geometry.getDimensions();
        this.doCommonConstructorStuff();
    }

    public static void validateDirectionCosines(Vector3d row, Vector3d column) {
        if (Math.abs(row.lengthSquared() - 1.0) > 0.001) {
            throw new IllegalArgumentException("Row not a unit vector");
        }
        if (Math.abs(column.lengthSquared() - 1.0) > 0.001) {
            throw new IllegalArgumentException("Column not a unit vector");
        }
        if (row.dot((Vector3dc)column) > 0.005) {
            throw new IllegalArgumentException("Row and column vectors are not orthogonal");
        }
    }

    public static Vector3d[] getCornersOfSourceRectangleInSourceSpace(Vector3d row, Vector3d column, Vector3d originalTLHC, Vector3d voxelSpacing, Vector3d dimensions) {
        LocalizerPoster.validateDirectionCosines(row, column);
        Vector3d distanceAlongRow = new Vector3d((Vector3dc)row).mul(dimensions.y * voxelSpacing.y);
        Vector3d distanceAlongColumn = new Vector3d((Vector3dc)column).mul(dimensions.x * voxelSpacing.x);
        Vector3d tlhc = new Vector3d((Vector3dc)originalTLHC);
        Vector3d trhc = new Vector3d((Vector3dc)tlhc).add((Vector3dc)distanceAlongRow);
        Vector3d blhc = new Vector3d((Vector3dc)tlhc).add((Vector3dc)distanceAlongColumn);
        Vector3d brhc = new Vector3d((Vector3dc)tlhc).add((Vector3dc)distanceAlongRow).add((Vector3dc)distanceAlongColumn);
        return new Vector3d[]{tlhc, trhc, brhc, blhc};
    }

    public static Vector3d[] getCornersOfSourceCubeInSourceSpace(Vector3d row, Vector3d column, Vector3d originalTLHC, Vector3d voxelSpacing, double sliceThickness, Vector3d dimensions) {
        LocalizerPoster.validateDirectionCosines(row, column);
        Vector3d normal = new Vector3d((Vector3dc)row).cross((Vector3dc)column);
        Vector3d distanceAlongRow = new Vector3d((Vector3dc)row).mul(dimensions.y * voxelSpacing.y);
        Vector3d distanceAlongColumn = new Vector3d((Vector3dc)column).mul(dimensions.x * voxelSpacing.x);
        Vector3d distanceAlongNormal = new Vector3d((Vector3dc)normal).mul(dimensions.z / 2.0 * sliceThickness);
        Vector3d tlhcT = new Vector3d((Vector3dc)originalTLHC).add((Vector3dc)distanceAlongNormal);
        Vector3d trhcT = new Vector3d((Vector3dc)tlhcT).add((Vector3dc)distanceAlongRow);
        Vector3d blhcT = new Vector3d((Vector3dc)tlhcT).add((Vector3dc)distanceAlongColumn);
        Vector3d brhcT = new Vector3d((Vector3dc)tlhcT).add((Vector3dc)distanceAlongRow).add((Vector3dc)distanceAlongColumn);
        Vector3d tlhcB = new Vector3d((Vector3dc)originalTLHC).sub((Vector3dc)distanceAlongNormal);
        Vector3d trhcB = new Vector3d((Vector3dc)tlhcB).add((Vector3dc)distanceAlongRow);
        Vector3d blhcB = new Vector3d((Vector3dc)tlhcB).add((Vector3dc)distanceAlongColumn);
        Vector3d brhcB = new Vector3d((Vector3dc)tlhcB).add((Vector3dc)distanceAlongRow).add((Vector3dc)distanceAlongColumn);
        return new Vector3d[]{tlhcT, trhcT, brhcT, blhcT, tlhcB, trhcB, brhcB, blhcB};
    }

    protected Vector3d transformPointFromSourceSpaceIntoLocalizerSpace(Vector3d point) {
        Vector3d newPoint = new Vector3d((Vector3dc)point);
        newPoint.sub((Vector3dc)this.localizerTLHC);
        this.rotateIntoLocalizerSpace.transform(newPoint);
        return newPoint;
    }

    protected Point2D.Double transformPointInLocalizerPlaneIntoImageSpace(Vector3d point) {
        double scaleSubPixelHeightOfColumn = (this.localizerDimensions.x - 1.0) / this.localizerDimensions.x;
        double scaleSubPixelWidthOfRow = (this.localizerDimensions.y - 1.0) / this.localizerDimensions.y;
        return new Point2D.Double(point.x / this.localizerVoxelSpacing.y * scaleSubPixelHeightOfColumn + 0.5, point.y / this.localizerVoxelSpacing.x * scaleSubPixelWidthOfRow + 0.5);
    }

    protected List<Point2D> drawOutlineOnLocalizer(List<Vector3d> corners) {
        if (corners != null && !corners.isEmpty()) {
            Vector3d[] cornersArray = new Vector3d[corners.size()];
            return this.drawOutlineOnLocalizer(corners.toArray(cornersArray));
        }
        return null;
    }

    protected List<Point2D> drawOutlineOnLocalizer(Vector3d[] corners) {
        ArrayList<Point2D> shapes = new ArrayList<Point2D>();
        for (Vector3d corner : corners) {
            shapes.add(this.transformPointInLocalizerPlaneIntoImageSpace(corner));
        }
        return shapes;
    }

    protected Vector3d intersectLineBetweenTwoPointsWithPlaneWhereZIsZero(Vector3d aP, Vector3d bP) {
        double[] u = new double[3];
        u[1] = MathUtil.isEqual((double)bP.z, (double)aP.z) ? aP.y : (bP.y - aP.y) / (bP.z - aP.z) * (0.0 - aP.z) + aP.y;
        u[0] = MathUtil.isEqual((double)bP.y, (double)aP.y) ? aP.x : (bP.x - aP.x) / (bP.y - aP.y) * (u[1] - aP.y) + aP.x;
        u[2] = 0.0;
        return new Vector3d(u);
    }

    protected List<Point2D> drawLinesBetweenAnyPointsWhichIntersectPlaneWhereZIsZero(Vector3d[] corners) {
        int size = corners.length;
        ArrayList<Vector3d> intersections = new ArrayList<Vector3d>();
        for (int i = 0; i < size; ++i) {
            int next = i == size - 1 ? 0 : i + 1;
            double thisZ = corners[i].z;
            double nextZ = corners[next].z;
            if (!(thisZ <= 0.0 && nextZ >= 0.0) && (!(thisZ >= 0.0) || !(nextZ <= 0.0))) continue;
            Vector3d intersection = this.intersectLineBetweenTwoPointsWithPlaneWhereZIsZero(corners[i], corners[next]);
            intersections.add(intersection);
        }
        return !intersections.isEmpty() ? this.drawOutlineOnLocalizer(intersections) : null;
    }

    protected static boolean classifyCornersIntoEdgeCrossingZPlane(Vector3d startCorner, Vector3d endCorner) {
        double startZ = startCorner.z;
        double endZ = endCorner.z;
        return startZ <= 0.0 && endZ >= 0.0 || startZ >= 0.0 && endZ <= 0.0;
    }

    protected List<Vector3d> getIntersectionsOfCubeWithZPlane(Vector3d[] corners) {
        int[][] edges = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        ArrayList<Vector3d> rawIntersections = new ArrayList<Vector3d>(6);
        for (int[] edge : edges) {
            if (!LocalizerPoster.classifyCornersIntoEdgeCrossingZPlane(corners[edge[0]], corners[edge[1]])) continue;
            rawIntersections.add(this.intersectLineBetweenTwoPointsWithPlaneWhereZIsZero(corners[edge[0]], corners[edge[1]]));
        }
        if (rawIntersections.size() < 2) {
            return rawIntersections;
        }
        double cx = 0.0;
        double cy = 0.0;
        for (Vector3d p : rawIntersections) {
            cx += p.x;
            cy += p.y;
        }
        double centX = cx /= (double)rawIntersections.size();
        double centY = cy /= (double)rawIntersections.size();
        rawIntersections.sort((a, b) -> {
            double angleA = Math.atan2(a.y - centY, a.x - centX);
            double angleB = Math.atan2(b.y - centY, b.x - centX);
            return Double.compare(angleA, angleB);
        });
        return rawIntersections;
    }

    protected void doCommonConstructorStuff() {
        LocalizerPoster.validateDirectionCosines(this.localizerRow, this.localizerColumn);
        this.localizerNormal = new Vector3d((Vector3dc)this.localizerRow).cross((Vector3dc)this.localizerColumn);
        this.rotateIntoLocalizerSpace = new Matrix3d();
        this.rotateIntoLocalizerSpace.setRow(0, (Vector3dc)this.localizerRow);
        this.rotateIntoLocalizerSpace.setRow(1, (Vector3dc)this.localizerColumn);
        this.rotateIntoLocalizerSpace.setRow(2, (Vector3dc)this.localizerNormal);
    }

    public abstract List<Point2D> getOutlineOnLocalizerForThisGeometry(Vector3d var1, Vector3d var2, Vector3d var3, Vector3d var4, double var5, Vector3d var7);

    public final List<Point2D> getOutlineOnLocalizerForThisGeometry(GeometryOfSlice geometry) {
        return this.getOutlineOnLocalizerForThisGeometry(geometry.getRow(), geometry.getColumn(), geometry.getTLHC(), geometry.getVoxelSpacing(), geometry.getSliceThickness(), geometry.getDimensions());
    }
}

