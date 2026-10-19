/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 */
package org.weasis.dicom.codec.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3d;
import org.weasis.dicom.codec.geometry.GeometryOfSlice;
import org.weasis.dicom.codec.geometry.LocalizerPoster;

public class IntersectVolume
extends LocalizerPoster {
    public IntersectVolume(Vector3d row, Vector3d column, Vector3d tlhc, Vector3d voxelSpacing, Vector3d dimensions) {
        super(row, column, tlhc, voxelSpacing, dimensions);
    }

    public IntersectVolume(GeometryOfSlice geometry) {
        super(geometry);
    }

    @Override
    public List<Point2D> getOutlineOnLocalizerForThisGeometry(Vector3d row, Vector3d column, Vector3d tlhc, Vector3d voxelSpacing, double sliceThickness, Vector3d dimensions) {
        Vector3d[] corners = IntersectVolume.getCornersOfSourceCubeInSourceSpace(row, column, tlhc, voxelSpacing, sliceThickness, dimensions);
        for (int i = 0; i < 8; ++i) {
            corners[i] = this.transformPointFromSourceSpaceIntoLocalizerSpace(corners[i]);
        }
        List<Vector3d> intersections = this.getIntersectionsOfCubeWithZPlane(corners);
        if (intersections != null && !intersections.isEmpty()) {
            ArrayList<Point2D> pts = new ArrayList<Point2D>(intersections.size());
            for (Vector3d point3d : intersections) {
                pts.add(this.transformPointInLocalizerPlaneIntoImageSpace(point3d));
            }
            return pts;
        }
        return null;
    }
}

