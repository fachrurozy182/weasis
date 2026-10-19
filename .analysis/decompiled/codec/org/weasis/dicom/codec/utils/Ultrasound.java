/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.codec.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.img.util.DicomUtils;

public class Ultrasound {
    private Ultrasound() {
    }

    public static List<Attributes> getRegions(Attributes attributes) {
        Sequence seq = attributes.getSequence(1597457);
        if (seq == null || seq.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Attributes> list = new ArrayList<Attributes>(seq.size());
        for (Attributes attr : seq) {
            list.add(new Attributes(attr));
        }
        return list;
    }

    public static Attributes getUniqueSpatialRegion(Attributes attributes) {
        List<Attributes> regions = Ultrasound.getRegions(attributes);
        Attributes spatialCalib = null;
        for (Attributes r : regions) {
            Integer unit = Ultrasound.getUnitsForXY(r);
            if (unit == null || !unit.equals(3)) continue;
            if (spatialCalib == null) {
                spatialCalib = r;
                continue;
            }
            Double calib1X = DicomUtils.getDoubleFromDicomElement(spatialCalib, 1597484, null);
            Double calib1Y = DicomUtils.getDoubleFromDicomElement(spatialCalib, 1597486, null);
            Double calib2X = DicomUtils.getDoubleFromDicomElement(r, 1597484, null);
            Double calib2Y = DicomUtils.getDoubleFromDicomElement(r, 1597486, null);
            if (Objects.equals(calib1X, calib2X) && Objects.equals(calib1Y, calib2Y)) continue;
            return null;
        }
        return spatialCalib;
    }

    public static Integer getUnitsForXY(Attributes region) {
        Integer unitX = DicomUtils.getIntegerFromDicomElement(region, 1597476, null);
        Integer unitY = DicomUtils.getIntegerFromDicomElement(region, 1597478, null);
        if (unitX != null && unitX.equals(unitY)) {
            return unitX;
        }
        return null;
    }

    public static String valueOfUnits(int id) {
        return switch (id) {
            case 1 -> "Percent";
            case 2 -> "dB";
            case 3 -> "cm";
            case 4 -> "seconds";
            case 5 -> "hertz(seconds-1)";
            case 6 -> "dB/seconds";
            case 7 -> "cm/sec";
            case 8 -> "cm2";
            case 9 -> "cm2/sec";
            case 10 -> "cm3";
            case 11 -> "cm3/sec";
            case 12 -> "degrees";
            default -> "None";
        };
    }
}

