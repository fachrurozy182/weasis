/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.plugins;

import org.dcm4che3.data.Attributes;
import org.weasis.dicom.hp.HPSelector;
import org.weasis.dicom.hp.plugins.ImagePlaneSelector;
import org.weasis.dicom.hp.spi.HPSelectorCategoryService;

public class ImagePlaneSelectorService
extends HPSelectorCategoryService {
    private static final float MIN_MIN_COSINE = 0.8f;
    private float minCosine = 0.9f;

    public ImagePlaneSelectorService() {
        super("IMAGE_PLANE");
    }

    @Override
    public void setProperty(String name, Object value) {
        if (!"MinCosine".equals(name)) {
            throw new IllegalArgumentException("Unsupported property: " + name);
        }
        float tmp = ((Float)value).floatValue();
        if (tmp < 0.8f || tmp > 1.0f) {
            throw new IllegalArgumentException("minCosine: " + String.valueOf(value));
        }
        this.minCosine = tmp;
    }

    @Override
    public Object getProperty(String name) {
        if (!"MinCosine".equals(name)) {
            throw new IllegalArgumentException("Unsupported property: " + name);
        }
        return Float.valueOf(this.minCosine);
    }

    @Override
    public HPSelector createHPSelector(Attributes filterOp) {
        ImagePlaneSelector sel = new ImagePlaneSelector(filterOp);
        sel.setMinCosine(this.minCosine);
        return sel;
    }
}

