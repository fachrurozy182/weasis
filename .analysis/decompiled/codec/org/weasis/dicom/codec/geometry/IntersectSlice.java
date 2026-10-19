/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 */
package org.weasis.dicom.codec.geometry;

import java.awt.geom.Point2D;
import java.util.List;
import org.joml.Vector3d;
import org.weasis.dicom.codec.geometry.GeometryOfSlice;
import org.weasis.dicom.codec.geometry.LocalizerPoster;

public class IntersectSlice
extends LocalizerPoster {
    public IntersectSlice(Vector3d row, Vector3d column, Vector3d tlhc, Vector3d voxelSpacing, Vector3d dimensions) {
        super(row, column, tlhc, voxelSpacing, dimensions);
    }

    public IntersectSlice(GeometryOfSlice geometry) {
        super(geometry);
    }

    private static boolean allTrue(boolean[] array) {
        boolean all = true;
        for (boolean b : array) {
            if (b) continue;
            all = false;
            break;
        }
        return all;
    }

    private static boolean oppositeEdges(boolean[] array) {
        return array[0] && array[2] || array[1] && array[3];
    }

    private static boolean adjacentEdges(boolean[] array) {
        return array[0] && array[1] || array[1] && array[2] || array[2] && array[3] || array[3] && array[0];
    }

    private static boolean[] classifyCornersOfRectangleIntoEdgesCrossingZPlane(Vector3d[] corners) {
        int size = corners.length;
        boolean[] classification = new boolean[size];
        for (int i = 0; i < size; ++i) {
            int next = i == size - 1 ? 0 : i + 1;
            classification[i] = IntersectSlice.classifyCornersIntoEdgeCrossingZPlane(corners[i], corners[next]);
        }
        return classification;
    }

    @Override
    public List<Point2D> getOutlineOnLocalizerForThisGeometry(Vector3d row, Vector3d column, Vector3d tlhc, Vector3d voxelSpacing, double sliceThickness, Vector3d dimensions) {
        Vector3d[] corners = IntersectSlice.getCornersOfSourceRectangleInSourceSpace(row, column, tlhc, voxelSpacing, dimensions);
        for (int i = 0; i < 4; ++i) {
            corners[i] = this.transformPointFromSourceSpaceIntoLocalizerSpace(corners[i]);
        }
        boolean[] edges = IntersectSlice.classifyCornersOfRectangleIntoEdgesCrossingZPlane(corners);
        if (IntersectSlice.allTrue(edges)) {
            return this.drawOutlineOnLocalizer(corners);
        }
        if (IntersectSlice.oppositeEdges(edges)) {
            return this.drawLinesBetweenAnyPointsWhichIntersectPlaneWhereZIsZero(corners);
        }
        if (IntersectSlice.adjacentEdges(edges)) {
            return this.drawLinesBetweenAnyPointsWhichIntersectPlaneWhereZIsZero(corners);
        }
        return null;
    }
}

