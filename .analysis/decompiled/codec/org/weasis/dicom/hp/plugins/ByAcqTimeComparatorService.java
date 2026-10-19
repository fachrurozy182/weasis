/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.plugins;

import org.dcm4che3.data.Attributes;
import org.weasis.dicom.hp.HPComparator;
import org.weasis.dicom.hp.plugins.ByAcqTimeComparator;
import org.weasis.dicom.hp.spi.HPComparatorCategoryService;

public class ByAcqTimeComparatorService
extends HPComparatorCategoryService {
    public ByAcqTimeComparatorService() {
        super("BY_ACQ_TIME");
    }

    @Override
    public HPComparator createHPComparator(Attributes sortOp) {
        return new ByAcqTimeComparator(sortOp);
    }
}

