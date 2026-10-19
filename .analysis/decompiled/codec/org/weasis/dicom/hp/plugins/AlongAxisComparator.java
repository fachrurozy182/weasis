/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.plugins;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.weasis.dicom.hp.AbstractHPComparator;
import org.weasis.dicom.hp.enums.SortingDirection;
import org.weasis.dicom.hp.enums.SortingOperation;

public class AlongAxisComparator
extends AbstractHPComparator {
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    private static final int RX = 0;
    private static final int RY = 1;
    private static final int RZ = 2;
    private static final int CX = 3;
    private static final int CY = 4;
    private static final int CZ = 5;
    private final Attributes sortOp;
    private final int sign;

    public AlongAxisComparator(Attributes sortOp) {
        this.sortOp = sortOp;
        String cs = sortOp.getString(7472644);
        if (cs == null) {
            throw new IllegalArgumentException("Missing (0072,0604) Sorting Direction");
        }
        this.sign = SortingDirection.fromString(cs).getSign();
    }

    public AlongAxisComparator(SortingDirection sortingDirection) {
        this.sign = sortingDirection.getSign();
        this.sortOp = new Attributes();
        this.sortOp.setString(7472642, VR.CS, SortingOperation.ALONG_AXIS.getCodeString());
        this.sortOp.setString(7472644, VR.CS, sortingDirection.getCodeString());
    }

    @Override
    public final Attributes getAttributes() {
        return this.sortOp;
    }

    @Override
    public int compare(Attributes o1, int frame1, Attributes o2, int frame2) {
        try {
            double v1 = this.dot(o1, frame1);
            double v2 = this.dot(o2, frame2);
            if (v1 < v2) {
                return -this.sign;
            }
            if (v1 > v2) {
                return this.sign;
            }
        }
        catch (NullPointerException nullPointerException) {
        }
        catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
            // empty catch block
        }
        return 0;
    }

    private double dot(Attributes o, int frame) {
        double[] ipp = this.getImagePositionPatient(o, frame);
        double[] iop = this.getImageOrientationPatient(o, frame);
        double nx = iop[1] * iop[5] - iop[2] * iop[4];
        double ny = iop[2] * iop[3] - iop[0] * iop[5];
        double nz = iop[0] * iop[4] - iop[1] * iop[3];
        return ipp[0] * nx + ipp[1] * ny + ipp[2] * nz;
    }

    private double[] getImageOrientationPatient(Attributes o, int frame) {
        double[] iop = o.getDoubles(2097207);
        if (iop != null) {
            return iop;
        }
        Attributes item = o.getNestedDataset(1375769136, frame);
        iop = (item = item.getNestedDataset(2134291, 0)).getDoubles(2097207);
        if (iop != null) {
            return iop;
        }
        item = o.getNestedDataset(1375769129, 0);
        return item.getDoubles(2097207);
    }

    private double[] getImagePositionPatient(Attributes o, int frame) {
        double[] ipp = o.getDoubles(0x200032);
        if (ipp != null) {
            return ipp;
        }
        Attributes item = o.getNestedDataset(1375769136, frame);
        ipp = (item = item.getNestedDataset(2134291, 0)).getDoubles(0x200032);
        if (ipp != null) {
            return ipp;
        }
        item = o.getNestedDataset(1375769129, 0);
        return item.getDoubles(0x200032);
    }
}

