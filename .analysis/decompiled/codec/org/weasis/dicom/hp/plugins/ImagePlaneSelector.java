/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.plugins;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.weasis.dicom.geom.ImageOrientation;
import org.weasis.dicom.geom.PatientOrientation;
import org.weasis.dicom.geom.Vector3;
import org.weasis.dicom.hp.AbstractHPSelector;

public class ImagePlaneSelector
extends AbstractHPSelector {
    public static final float DEF_MIN_COSINE = 0.9f;
    private final Attributes filterOp;
    private float minCosine = 0.9f;
    private final ImageOrientation.Plan[] imagePlanes;

    public ImagePlaneSelector(Attributes filterOp) {
        String vrStr = filterOp.getString(7471184);
        if (vrStr == null) {
            throw new IllegalArgumentException("Missing (0072,0050) Selector Attribute VR");
        }
        if (!"CS".equals(vrStr)) {
            throw new IllegalArgumentException("(0072,0050) Selector Attribute VR: " + vrStr);
        }
        String[] values = filterOp.getStrings(7471202);
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Missing (0072,0062) AbstractHPSelector CS Value");
        }
        this.imagePlanes = new ImageOrientation.Plan[values.length];
        for (int i = 0; i < values.length; ++i) {
            this.imagePlanes[i] = ImageOrientation.Plan.valueOf(values[i]);
        }
        this.filterOp = filterOp;
    }

    public ImagePlaneSelector(ImageOrientation.Plan[] imagePlanes) {
        this.imagePlanes = (ImageOrientation.Plan[])imagePlanes.clone();
        this.filterOp = new Attributes();
        this.filterOp.setString(7472130, VR.CS, "IMAGE_PLANE");
        this.filterOp.setString(7471184, VR.CS, "CS");
        String[] values = new String[imagePlanes.length];
        for (int i = 0; i < values.length; ++i) {
            values[i] = imagePlanes[i].name();
        }
        this.filterOp.setString(7471202, VR.CS, values);
    }

    @Override
    public final Attributes getAttributes() {
        return this.filterOp;
    }

    public final float getMinCosine() {
        return this.minCosine;
    }

    public final void setMinCosine(float minCosine) {
        this.minCosine = minCosine;
    }

    @Override
    public boolean matches(Attributes attributes, int frame) {
        ImageOrientation.Plan imagePlane;
        double[] floats = attributes.getDoubles(2097207);
        if (floats != null && floats.length == 6) {
            Vector3 vr = Vector3.of(floats);
            Vector3 vc = new Vector3(floats[3], floats[4], floats[5]);
            imagePlane = ImageOrientation.getPlan(vr, vc, this.minCosine);
        } else {
            String[] ss = attributes.getStrings(0x200020);
            if (ss != null && ss.length == 2) {
                PatientOrientation.Biped rowAxis = PatientOrientation.Biped.fromString(ss[0]);
                PatientOrientation.Biped colAxis = PatientOrientation.Biped.fromString(ss[1]);
                imagePlane = ImageOrientation.getPlan(rowAxis, colAxis);
            } else {
                return true;
            }
        }
        for (ImageOrientation.Plan plane : this.imagePlanes) {
            if (imagePlane != plane) continue;
            return true;
        }
        return false;
    }
}

