/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.spi;

import org.dcm4che3.data.Attributes;
import org.weasis.dicom.hp.HPComparator;
import org.weasis.dicom.hp.spi.HPCategoryService;

public abstract class HPComparatorCategoryService
extends HPCategoryService {
    protected HPComparatorCategoryService(String category) {
        super(category);
    }

    public abstract HPComparator createHPComparator(Attributes var1);
}

