/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package org.weasis.dicom.codec.geometry;

import java.awt.geom.Point2D;
import java.util.Objects;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.weasis.dicom.codec.geometry.VectorUtils;

public class GeometryOfSlice {
    public static final double MIN_SPACING = 1.0E-5;
    protected Vector3d row;
    protected Vector3d column;
    protected Vector3d tlhc;
    protected Vector3d voxelSpacing;
    protected double sliceThickness;
    protected Vector3d dimensions;

    public GeometryOfSlice(Vector3d row, Vector3d column, Vector3d tlhc, Vector3d voxelSpacing, double sliceThickness, Vector3d dimensions) {
        this.row = row;
        this.column = column;
        this.tlhc = tlhc;
        this.voxelSpacing = voxelSpacing;
        this.sliceThickness = sliceThickness;
        this.dimensions = dimensions;
    }

    public GeometryOfSlice(GeometryOfSlice sliceGeometry) {
        this.row = new Vector3d((Vector3dc)sliceGeometry.row);
        this.column = new Vector3d((Vector3dc)sliceGeometry.column);
        this.tlhc = new Vector3d((Vector3dc)sliceGeometry.tlhc);
        this.voxelSpacing = new Vector3d((Vector3dc)sliceGeometry.voxelSpacing);
        this.sliceThickness = sliceGeometry.sliceThickness;
        this.dimensions = new Vector3d((Vector3dc)sliceGeometry.dimensions);
    }

    public final Vector3d getRow() {
        return this.row;
    }

    public final Vector3d getColumn() {
        return this.column;
    }

    public final Vector3d getNormal() {
        return VectorUtils.computeNormalOfSurface(this.row, this.column);
    }

    public final Vector3d getTLHC() {
        return this.tlhc;
    }

    public boolean isRowColumnOrthogonal() {
        return Math.abs(this.row.dot((Vector3dc)this.column)) <= 0.005;
    }

    public final Vector3d getPosition(Point2D p) {
        return new Vector3d(this.row.x * this.voxelSpacing.x * p.getX() + this.column.x * this.voxelSpacing.y * p.getY() + this.tlhc.x, this.row.y * this.voxelSpacing.x * p.getX() + this.column.y * this.voxelSpacing.y * p.getY() + this.tlhc.y, this.row.z * this.voxelSpacing.x * p.getX() + this.column.z * this.voxelSpacing.y * p.getY() + this.tlhc.z);
    }

    public final Point2D getImagePosition(Vector3d p3) {
        if (this.voxelSpacing.x < 1.0E-5 || this.voxelSpacing.y < 1.0E-5) {
            return null;
        }
        double ix = ((p3.x - this.tlhc.x) * this.row.x + (p3.y - this.tlhc.y) * this.row.y + (p3.z - this.tlhc.z) * this.row.z) / this.voxelSpacing.x;
        double iy = ((p3.x - this.tlhc.x) * this.column.x + (p3.y - this.tlhc.y) * this.column.y + (p3.z - this.tlhc.z) * this.column.z) / this.voxelSpacing.y;
        return new Point2D.Double(ix, iy);
    }

    public final Vector3d getVoxelSpacing() {
        return this.voxelSpacing;
    }

    public final double getSliceThickness() {
        return this.sliceThickness;
    }

    public final Vector3d getDimensions() {
        return this.dimensions;
    }

    public static String getOrientation(Vector3d orientation, boolean quadruped) {
        StringBuilder strbuf = new StringBuilder();
        if (orientation != null) {
            String orientationY;
            String orientationX;
            String string = orientation.x < 0.0 ? (quadruped ? "Rt" : "R") : (orientationX = quadruped ? "Le" : "L");
            String string2 = orientation.y < 0.0 ? (quadruped ? "V" : "A") : (orientationY = quadruped ? "D" : "P");
            String orientationZ = orientation.z < 0.0 ? (quadruped ? "Cd" : "F") : (quadruped ? "Cr" : "H");
            double absX = Math.abs(orientation.x);
            double absY = Math.abs(orientation.y);
            double absZ = Math.abs(orientation.z);
            for (int i = 0; i < 3; ++i) {
                if (absX > 1.0E-4 && absX > absY && absX > absZ) {
                    strbuf.append(orientationX);
                    absX = 0.0;
                    continue;
                }
                if (absY > 1.0E-4 && absY > absX && absY > absZ) {
                    strbuf.append(orientationY);
                    absY = 0.0;
                    continue;
                }
                if (!(absZ > 1.0E-4) || !(absZ > absX) || !(absZ > absY)) continue;
                strbuf.append(orientationZ);
                absZ = 0.0;
            }
        }
        return strbuf.toString();
    }

    public final String getRowOrientation(boolean quadruped) {
        return GeometryOfSlice.getOrientation(this.row, quadruped);
    }

    public final String getColumnOrientation(boolean quadruped) {
        return GeometryOfSlice.getOrientation(this.column, quadruped);
    }

    public final String getRowOrientation() {
        return this.getRowOrientation(false);
    }

    public final String getColumnOrientation() {
        return this.getColumnOrientation(false);
    }

    public boolean equals(Object obj) {
        if (obj instanceof GeometryOfSlice) {
            GeometryOfSlice geom = (GeometryOfSlice)obj;
            return this.row.equals((Object)geom.row) && this.column.equals((Object)geom.column) && this.tlhc.equals((Object)geom.tlhc) && this.voxelSpacing.equals((Object)geom.voxelSpacing) && this.sliceThickness == geom.sliceThickness && this.dimensions.equals((Object)geom.dimensions);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.getRow(), this.getColumn(), this.tlhc, this.getVoxelSpacing(), this.getSliceThickness(), this.getDimensions());
    }
}

