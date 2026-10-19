/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.plugins;

import org.dcm4che3.data.Attributes;
import org.weasis.dicom.hp.HPComparator;
import org.weasis.dicom.hp.plugins.AlongAxisComparator;
import org.weasis.dicom.hp.spi.HPComparatorCategoryService;

public class AlongAxisComparatorService
extends HPComparatorCategoryService {
    public AlongAxisComparatorService() {
        super("ALONG_AXIS");
    }

    @Override
    public HPComparator createHPComparator(Attributes sortOp) {
        return new AlongAxisComparator(sortOp);
    }
}

