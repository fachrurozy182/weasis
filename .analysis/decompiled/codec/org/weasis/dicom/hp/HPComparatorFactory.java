/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import java.util.Date;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.hp.AbstractHPComparator;
import org.weasis.dicom.hp.HPComparator;
import org.weasis.dicom.hp.HangingProtocol;
import org.weasis.dicom.hp.enums.SortingDirection;
import org.weasis.dicom.hp.plugins.AlongAxisComparator;
import org.weasis.dicom.hp.plugins.ByAcqTimeComparator;
import org.weasis.dicom.hp.spi.HPComparatorCategoryService;

public class HPComparatorFactory {
    public static final int FRAME_INDEX = 65535;

    private HPComparatorFactory() {
    }

    public static HPComparator createHPComparator(Attributes sortingOp) {
        if (sortingOp.containsValue(7472642)) {
            return HPComparatorFactory.createSortByCategory(sortingOp);
        }
        HPComparator cmp = new SortByAttribute(sortingOp);
        cmp = HPComparatorFactory.addSequencePointer(cmp);
        cmp = HPComparatorFactory.addFunctionalGroupPointer(cmp);
        return cmp;
    }

    private static HPComparator createSortByCategory(Attributes sortingOp) {
        HPComparatorCategoryService spi = HangingProtocol.getHPComparatorSpi(sortingOp.getString(7472642));
        if (spi == null) {
            throw new IllegalArgumentException("Unsupported Sort-by Category: " + sortingOp.getString(7472642));
        }
        return spi.createHPComparator(sortingOp);
    }

    public static HPComparator createSortByAttribute(String privateCreator, int tag, int valueNumber, SortingDirection sortingDirection) {
        return new SortByAttribute(privateCreator, tag, valueNumber, sortingDirection);
    }

    public static HPComparator createSortAlongAxis(SortingDirection sortingDirection) {
        return new AlongAxisComparator(sortingDirection);
    }

    public static HPComparator createSortByAcqTime(SortingDirection sortingDirection) {
        return new ByAcqTimeComparator(sortingDirection);
    }

    public static HPComparator addSequencePointer(String privateCreator, int tag, HPComparator comparator) {
        if (tag == 0) {
            return comparator;
        }
        if (comparator.getSelectorSequencePointer() != 0) {
            throw new IllegalArgumentException("Sequence Pointer already added");
        }
        if (comparator.getFunctionalGroupPointer() != 0) {
            throw new IllegalArgumentException("Functional Group Pointer already added");
        }
        comparator.getAttributes().setInt(7471186, VR.AT, tag);
        if (privateCreator != null) {
            comparator.getAttributes().setString(7471188, VR.LO, privateCreator);
        }
        return new Seq(privateCreator, tag, comparator);
    }

    public static HPComparator addFunctionalGroupPointer(String privCreator, int tag, HPComparator comparator) {
        if (tag == 0) {
            return comparator;
        }
        if (comparator.getFunctionalGroupPointer() != 0) {
            throw new IllegalArgumentException("Functional Group Pointer already added");
        }
        comparator.getAttributes().setInt(2134375, VR.AT, tag);
        if (privCreator != null) {
            comparator.getAttributes().setString(2134584, VR.LO, privCreator);
        }
        return new FctGrp(tag, privCreator, comparator);
    }

    @Generated
    private static HPComparator addSequencePointer(HPComparator cmp) {
        int tag = cmp.getSelectorSequencePointer();
        if (tag != 0) {
            String privCreator = cmp.getSelectorSequencePointerPrivateCreator();
            cmp = new Seq(privCreator, tag, cmp);
        }
        return cmp;
    }

    @Generated
    private static HPComparator addFunctionalGroupPointer(HPComparator cmp) {
        int tag = cmp.getFunctionalGroupPointer();
        if (tag != 0) {
            String privCreator = cmp.getFunctionalGroupPrivateCreator();
            cmp = new FctGrp(tag, privCreator, cmp);
        }
        return cmp;
    }

    @Generated
    private static int compareAttributes(Attributes c1, Attributes c2) {
        if (c1 == null || c2 == null) {
            return 0;
        }
        String v1 = c1.getString(524544);
        String v2 = c2.getString(524544);
        if (v1 == null || v2 == null) {
            return 0;
        }
        return v1.compareTo(v2);
    }

    @Generated
    private static int compareInts(int[] v1, int i1, int[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2) {
            return 0;
        }
        return v1[i1 - 1] - v2[i2 - 1];
    }

    @Generated
    private static int compareDoubles(double[] v1, int i1, double[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2) {
            return 0;
        }
        double d = v1[i1 - 1] - v2[i2 - 1];
        return d < 0.0 ? -1 : (d > 0.0 ? 1 : 0);
    }

    @Generated
    private static int compareFloats(float[] v1, int i1, float[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2) {
            return 0;
        }
        float d = v1[i1 - 1] - v2[i2 - 1];
        return d < 0.0f ? -1 : (d > 0.0f ? 1 : 0);
    }

    @Generated
    private static int compareDates(Date[] v1, int i1, Date[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2) {
            return 0;
        }
        return v1[i1 - 1].compareTo(v2[i2 - 1]);
    }

    @Generated
    private static int compareIntegers(int[] v1, int i1, int[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2) {
            return 0;
        }
        long d = ((long)v1[i1 - 1] & 0xFFFFFFFFL) - ((long)v2[i2 - 1] & 0xFFFFFFFFL);
        return d < 0L ? -1 : (d > 0L ? 1 : 0);
    }

    @Generated
    private static int compareStrings(String[] v1, int i1, String[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2) {
            return 0;
        }
        return v1[i1 - 1].compareTo(v2[i2 - 1]);
    }

    private static class SortByAttribute
    extends AttributeComparator {
        private final Attributes sortingOp;
        private final int valueNumber;
        private final int sign;

        SortByAttribute(String privateCreator, int tag, int valueNumber, SortingDirection sortingDirection) {
            super(tag, privateCreator);
            if (valueNumber == 0) {
                throw new IllegalArgumentException("valueNumber = 0");
            }
            this.valueNumber = valueNumber;
            this.sign = sortingDirection.getSign();
            this.sortingOp = new Attributes();
            this.sortingOp.setInt(7471142, VR.AT, tag);
            if (privateCreator != null) {
                this.sortingOp.setString(7471190, VR.LO, privateCreator);
            }
            this.sortingOp.setInt(7471144, VR.US, valueNumber);
            this.sortingOp.setString(7472644, VR.CS, sortingDirection.getCodeString());
        }

        SortByAttribute(Attributes sortingOp) {
            super(SortByAttribute.getSelectorAttribute(sortingOp), sortingOp.getString(7471190));
            this.valueNumber = sortingOp.getInt(7471144, 0);
            if (this.valueNumber == 0) {
                throw new IllegalArgumentException("Missing or invalid (0072,0028) Selector Value Number: " + sortingOp.getString(7471144));
            }
            String cs = sortingOp.getString(7472644);
            if (cs == null) {
                throw new IllegalArgumentException("Missing (0072,0604) Sorting Direction");
            }
            this.sign = SortingDirection.fromString(cs).getSign();
            this.sortingOp = sortingOp;
        }

        private static int getSelectorAttribute(Attributes sortingOp) {
            int tag = sortingOp.getInt(7471142, 0);
            if (tag == 0) {
                throw new IllegalArgumentException("Missing (0072,0026) Selector Attribute");
            }
            return tag;
        }

        @Override
        public Attributes getAttributes() {
            return this.sortingOp;
        }

        @Override
        @Generated
        public int compare(Attributes o1, int frame1, Attributes o2, int frame2) {
            VR e1 = o1.getVR(this.privateCreator, this.tag);
            if (e1 == null) {
                return 0;
            }
            VR e2 = o2.getVR(this.privateCreator, this.tag);
            if (e2 == null) {
                return 0;
            }
            if (!e1.equals((Object)e2)) {
                return 0;
            }
            int i1 = frame1;
            int i2 = frame2;
            if (this.valueNumber != 65535) {
                i1 = i2 = this.valueNumber;
            }
            return switch (e1) {
                case VR.AE, VR.AS, VR.CS, VR.LO, VR.LT, VR.PN, VR.SH, VR.ST, VR.UI, VR.UT -> this.sign * HPComparatorFactory.compareStrings(o1.getStrings(this.privateCreator, this.tag), i1, o2.getStrings(this.privateCreator, this.tag), i2);
                case VR.AT, VR.UL, VR.US -> this.sign * HPComparatorFactory.compareIntegers(o1.getInts(this.privateCreator, this.tag), i1, o2.getInts(this.privateCreator, this.tag), i2);
                case VR.DA, VR.DT, VR.TM -> this.sign * HPComparatorFactory.compareDates(o1.getDates(this.privateCreator, this.tag), i1, o2.getDates(this.privateCreator, this.tag), i2);
                case VR.DS, VR.FL -> this.sign * HPComparatorFactory.compareFloats(o1.getFloats(this.privateCreator, this.tag), i1, o2.getFloats(this.privateCreator, this.tag), i2);
                case VR.FD -> this.sign * HPComparatorFactory.compareDoubles(o1.getDoubles(this.privateCreator, this.tag), i1, o2.getDoubles(this.privateCreator, this.tag), i2);
                case VR.IS, VR.SL, VR.SS -> this.sign * HPComparatorFactory.compareInts(o1.getInts(this.privateCreator, this.tag), i1, o2.getInts(this.privateCreator, this.tag), i2);
                case VR.SQ -> this.sign * HPComparatorFactory.compareAttributes(o1.getNestedDataset(this.privateCreator, this.tag), o2.getNestedDataset(this.privateCreator, this.tag));
                default -> 0;
            };
        }
    }

    private static class Seq
    extends AttributeComparatorDecorator {
        Seq(String privateCreator, int tag, HPComparator cmp) {
            super(tag, privateCreator, cmp);
        }

        @Override
        public int compare(Attributes o1, int frame1, Attributes o2, int frame2) {
            Attributes v1 = o1.getNestedDataset(this.privateCreator, this.tag);
            if (v1 == null) {
                return 0;
            }
            Attributes v2 = o2.getNestedDataset(this.privateCreator, this.tag);
            if (v2 == null) {
                return 0;
            }
            return this.cmp.compare(v1, frame1, v2, frame2);
        }
    }

    private static class FctGrp
    extends AttributeComparatorDecorator {
        FctGrp(int tag, String privateCreator, HPComparator cmp) {
            super(tag, privateCreator, cmp);
        }

        @Override
        public int compare(Attributes o1, int frame1, Attributes o2, int frame2) {
            Attributes fg1 = this.fctGrp(o1, frame1);
            if (fg1 == null) {
                return 0;
            }
            Attributes fg2 = this.fctGrp(o1, frame1);
            if (fg2 == null) {
                return 0;
            }
            return this.cmp.compare(fg1, frame1, fg2, frame2);
        }

        private Attributes fctGrp(Attributes o, int frame) {
            Attributes fctGrp;
            Attributes sharedFctGrp = o.getNestedDataset(1375769129);
            if (sharedFctGrp != null && (fctGrp = sharedFctGrp.getNestedDataset(this.privateCreator, this.tag)) != null) {
                return fctGrp;
            }
            Sequence frameFctGrpSeq = o.getSequence(1375769136);
            if (frameFctGrpSeq == null || frameFctGrpSeq.size() < frame) {
                return null;
            }
            Attributes frameFctGrp = (Attributes)frameFctGrpSeq.get(frame - 1);
            return frameFctGrp.getNestedDataset(this.privateCreator, this.tag);
        }
    }

    private static abstract class AttributeComparatorDecorator
    extends AttributeComparator {
        protected final HPComparator cmp;

        AttributeComparatorDecorator(int tag, String privateCreator, HPComparator cmp) {
            super(tag, privateCreator);
            this.cmp = cmp;
        }

        @Override
        public Attributes getAttributes() {
            return this.cmp.getAttributes();
        }
    }

    private static abstract class AttributeComparator
    extends AbstractHPComparator {
        protected final int tag;
        protected final String privateCreator;

        AttributeComparator(int tag, String privateCreator) {
            if (tag == 0) {
                throw new IllegalArgumentException("tag: 0");
            }
            this.tag = tag;
            this.privateCreator = privateCreator;
        }
    }
}

