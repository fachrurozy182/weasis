/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.plugins;

import java.util.Date;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.weasis.dicom.hp.AbstractHPComparator;
import org.weasis.dicom.hp.enums.SortingDirection;
import org.weasis.dicom.hp.enums.SortingOperation;

public class ByAcqTimeComparator
extends AbstractHPComparator {
    private final int sign;
    private final Attributes sortOp;

    public ByAcqTimeComparator(Attributes sortOp) {
        this.sortOp = sortOp;
        String cs = sortOp.getString(7472644);
        if (cs == null) {
            throw new IllegalArgumentException("Missing (0072,0604) Sorting Direction");
        }
        this.sign = SortingDirection.fromString(cs).getSign();
    }

    public ByAcqTimeComparator(SortingDirection sortingDirection) {
        this.sign = sortingDirection.getSign();
        this.sortOp = new Attributes();
        this.sortOp.setString(7472642, VR.CS, SortingOperation.BY_ACQ_TIME.getCodeString());
        this.sortOp.setString(7472644, VR.CS, sortingDirection.getCodeString());
    }

    @Override
    public final Attributes getAttributes() {
        return this.sortOp;
    }

    @Override
    public int compare(Attributes o1, int frame1, Attributes o2, int frame2) {
        Date t1 = this.toAcqTime(o1);
        Date t2 = this.toAcqTime(o2);
        if (t1 == null || t2 == null) {
            return 0;
        }
        return t1.compareTo(t2) * this.sign;
    }

    private Date toAcqTime(Attributes o) {
        Date t = o.getDate(524322, 524338);
        if (t == null && (t = o.getDate(524330)) == null) {
            t = o.getDate(524323, 524339);
        }
        return t;
    }
}

