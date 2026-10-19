/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.spi;

import org.dcm4che3.data.Attributes;
import org.weasis.dicom.hp.HPSelector;
import org.weasis.dicom.hp.spi.HPCategoryService;

public abstract class HPSelectorCategoryService
extends HPCategoryService {
    protected HPSelectorCategoryService(String category) {
        super(category);
    }

    public abstract HPSelector createHPSelector(Attributes var1);
}

