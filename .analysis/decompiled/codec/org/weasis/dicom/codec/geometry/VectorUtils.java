/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package org.weasis.dicom.codec.geometry;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class VectorUtils {
    public static Vector3d computeNormalOfSurface(Vector3d v1, Vector3d v2) {
        Vector3d normal = v1.cross((Vector3dc)v2, new Vector3d());
        return normal.lengthSquared() > 0.0 ? normal.normalize() : normal;
    }

    public static Vector3d computeNormalOfSurface(Vector3d origin, Vector3d v1, Vector3d v2) {
        Vector3d w;
        Vector3d u = v1.sub((Vector3dc)origin, new Vector3d());
        Vector3d normal = u.cross((Vector3dc)(w = v2.sub((Vector3dc)origin, new Vector3d())));
        return normal.lengthSquared() > 0.0 ? normal.normalize() : normal;
    }

    public static Vector3d orientNormalToDominantPositiveAxis(Vector3d normal) {
        double dominantComponent;
        if (normal == null) {
            return null;
        }
        double ax = Math.abs(normal.x);
        double ay = Math.abs(normal.y);
        double az = Math.abs(normal.z);
        double d = ax >= ay && ax >= az ? normal.x : (dominantComponent = ay >= az ? normal.y : normal.z);
        if (dominantComponent < 0.0) {
            normal.negate();
        }
        return normal;
    }
}

