/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import java.util.Arrays;
import java.util.Collection;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.geom.ImageOrientation;
import org.weasis.dicom.hp.AbstractHPSelector;
import org.weasis.dicom.hp.HPSelector;
import org.weasis.dicom.hp.HangingProtocol;
import org.weasis.dicom.hp.enums.Presence;
import org.weasis.dicom.hp.enums.SelectorUsageFlag;
import org.weasis.dicom.hp.filter.FilterOp;
import org.weasis.dicom.hp.filter.HPFilter;
import org.weasis.dicom.hp.plugins.ImagePlaneSelector;
import org.weasis.dicom.hp.spi.HPSelectorCategoryService;
import org.weasis.dicom.macro.Code;

public class HPSelectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HPSelectorFactory.class);
    public static final int FRAME_INDEX = 65535;

    private HPSelectorFactory() {
    }

    public static HPSelector createImageSetSelector(Attributes item) {
        String usageFlag = item.getString(7471140);
        if (usageFlag == null) {
            throw new IllegalArgumentException("Missing (0072,0024) Image Set Selector Usage Flag");
        }
        HPSelector sel = HPSelectorFactory.createAttributeValueSelector(item, HPSelectorFactory.isMatch(usageFlag), FilterOp.MEMBER_OF);
        sel = HPSelectorFactory.addSequencePointer(sel);
        sel = HPSelectorFactory.addFunctionalGroupPointer(sel);
        return sel;
    }

    private static boolean isMatch(String usageFlag) {
        return SelectorUsageFlag.fromString(usageFlag).isMatch();
    }

    public static VR getVR(String vr) {
        try {
            return VR.valueOf(vr);
        }
        catch (Exception e) {
            LOGGER.error("Cannot find VR for {}", (Object)vr);
            return VR.OB;
        }
    }

    public static HPSelector createAttributeValueSelector(String usageFlag, String privateCreator, int tag, int valueNumber, VR vr, String[] values) {
        return HPSelectorFactory.createAttributeValueSelector(usageFlag, privateCreator, tag, valueNumber, vr, values, null);
    }

    public static HPSelector createAttributeValueSelector(String usageFlag, String privateCreator, int tag, int valueNumber, VR vr, int[] values) {
        return HPSelectorFactory.createAttributeValueSelector(usageFlag, privateCreator, tag, valueNumber, vr, values, null);
    }

    public static HPSelector createAttributeValueSelector(String usageFlag, String privateCreator, int tag, int valueNumber, VR vr, float[] values) {
        return HPSelectorFactory.createAttributeValueSelector(usageFlag, privateCreator, tag, valueNumber, vr, values, null);
    }

    public static HPSelector createAttributeValueSelector(String usageFlag, String privateCreator, int tag, int valueNumber, double[] values) {
        return HPSelectorFactory.createAttributeValueSelector(usageFlag, privateCreator, tag, valueNumber, values, null);
    }

    public static HPSelector createCodeValueSelector(String usageFlag, String privateCreator, int tag, int valueNumber, Code[] values) {
        return new CodeValueSelector(tag, privateCreator, valueNumber, usageFlag, null, values);
    }

    public static HPSelector createDisplaySetFilter(Attributes item) {
        if (item.containsValue(7472130)) {
            return HPSelectorFactory.createFilterByCategory(item);
        }
        HPSelector sel = HPSelectorFactory.createDisplaySetSelector(item);
        sel = HPSelectorFactory.addSequencePointer(sel);
        sel = HPSelectorFactory.addFunctionalGroupPointer(sel);
        return sel;
    }

    private static HPSelector createFilterByCategory(Attributes filterOp) {
        HPSelectorCategoryService spi = HangingProtocol.getHPSelectorSpi(filterOp.getString(7472130));
        if (spi == null) {
            throw new IllegalArgumentException("Unsupported Filter-by Category: " + filterOp.getString(7472130));
        }
        return spi.createHPSelector(filterOp);
    }

    public static HPSelector createImagePlaneSelector(ImageOrientation.Plan[] imagePlanes) {
        return new ImagePlaneSelector(imagePlanes);
    }

    public static HPSelector createAttributePresenceSelector(String privateCreator, int tag, String filter) {
        return new AttributePresenceSelector(filter, tag, privateCreator);
    }

    public static HPSelector createAttributeValueSelector(String privateCreator, int tag, int valueNumber, VR vr, String[] values, HPFilter filterOp) {
        return HPSelectorFactory.createAttributeValueSelector(null, privateCreator, tag, valueNumber, vr, values, filterOp);
    }

    public static HPSelector createAttributeValueSelector(String privateCreator, int tag, int valueNumber, VR vr, int[] values, HPFilter filterOp) {
        return HPSelectorFactory.createAttributeValueSelector(null, privateCreator, tag, valueNumber, vr, values, filterOp);
    }

    public static HPSelector createAttributeValueSelector(String privateCreator, int tag, int valueNumber, VR vr, float[] values, HPFilter filterOp) {
        return HPSelectorFactory.createAttributeValueSelector(null, privateCreator, tag, valueNumber, vr, values, filterOp);
    }

    public static HPSelector createAttributeValueSelector(String privateCreator, int tag, int valueNumber, double[] values, HPFilter filterOp) {
        return HPSelectorFactory.createAttributeValueSelector(null, privateCreator, tag, valueNumber, values, filterOp);
    }

    public static HPSelector createCodeValueSelector(String privateCreator, int tag, int valueNumber, Code[] values, HPFilter filterOp) {
        return new CodeValueSelector(tag, privateCreator, valueNumber, null, filterOp, values);
    }

    public static HPSelector addSequencePointer(String privCreator, int tag, HPSelector selector) {
        if (tag == 0) {
            return selector;
        }
        AttributeSelector attrSel = (AttributeSelector)selector;
        if (selector.getSelectorSequencePointer() != null) {
            throw new IllegalArgumentException("Sequence Pointer already added");
        }
        if (selector.getFunctionalGroupPointer() != null) {
            throw new IllegalArgumentException("Functional Group Pointer already added");
        }
        selector.getAttributes().setInt(7471186, VR.AT, tag);
        if (privCreator != null) {
            selector.getAttributes().setString(7471188, VR.LO, privCreator);
        }
        return new Seq(tag, privCreator, attrSel);
    }

    public static HPSelector addFunctionalGroupPointer(String privCreator, int tag, HPSelector selector) {
        if (tag == 0) {
            return selector;
        }
        AttributeSelector attrSel = (AttributeSelector)selector;
        if (selector.getFunctionalGroupPointer() != null) {
            throw new IllegalArgumentException("Functional Group Pointer already added");
        }
        selector.getAttributes().setInt(2134375, VR.AT, tag);
        if (privCreator != null) {
            selector.getAttributes().setString(2134584, VR.LO, privCreator);
        }
        return new FctGrp(tag, privCreator, attrSel);
    }

    private static HPSelector addSequencePointer(HPSelector sel) {
        Integer seqTag = sel.getSelectorSequencePointer();
        if (seqTag != null && seqTag != 0) {
            String privCreator = sel.getSelectorSequencePointerPrivateCreator();
            sel = new Seq((int)seqTag, privCreator, (AttributeSelector)sel);
        }
        return sel;
    }

    private static HPSelector addFunctionalGroupPointer(HPSelector sel) {
        Integer fgTag = sel.getFunctionalGroupPointer();
        if (fgTag != null && fgTag != 0) {
            String privCreator = sel.getFunctionalGroupPrivateCreator();
            sel = new FctGrp((int)fgTag, privCreator, (AttributeSelector)sel);
        }
        return sel;
    }

    @Generated
    private static HPSelector createAttributeValueSelector(Attributes item, boolean match, HPFilter filterOp) {
        String vrStr = item.getString(7471184);
        if (vrStr == null) {
            throw new IllegalArgumentException("Missing (0072,0050) Selector Attribute VR");
        }
        return switch (HPSelectorFactory.getVR(vrStr)) {
            case VR.AT -> new IntSelector(item, 7471200, match, filterOp, VR.AT);
            case VR.CS -> new Str(item, 7471202, match, filterOp, VR.CS);
            case VR.DS -> new Flt(item, 0x720072, match, filterOp, VR.DS);
            case VR.FD -> new Dbl(item, 7471220, match, filterOp, VR.FD);
            case VR.FL -> new Flt(item, 7471222, match, filterOp, VR.FL);
            case VR.IS -> new IntSelector(item, 7471204, match, filterOp, VR.IS);
            case VR.LO -> new Str(item, 7471206, match, filterOp, VR.LO);
            case VR.LT -> new Str(item, 7471208, match, filterOp, VR.LT);
            case VR.PN -> new Str(item, 7471210, match, filterOp, VR.PN);
            case VR.SH -> new Str(item, 7471212, match, filterOp, VR.SH);
            case VR.SL -> new IntSelector(item, 7471228, match, filterOp, VR.SL);
            case VR.SQ -> new CodeValueSelector(item, match, filterOp, VR.SQ);
            case VR.SS -> new IntSelector(item, 7471230, match, filterOp, VR.SS);
            case VR.ST -> new Str(item, 7471214, match, filterOp, VR.ST);
            case VR.UL -> new UIntSelector(item, 7471224, match, filterOp, VR.UL);
            case VR.US -> new IntSelector(item, 7471226, match, filterOp, VR.US);
            case VR.UT -> new Str(item, 0x720070, match, filterOp, VR.UT);
            default -> throw new IllegalArgumentException("vr: " + vrStr);
        };
    }

    private static HPSelector createDisplaySetSelector(Attributes item) {
        if (item.containsValue(7472132)) {
            return new AttributePresenceSelector(item);
        }
        String filterOp = item.getString(7472134);
        if (filterOp == null) {
            throw new IllegalArgumentException("Missing (0072,0406) Filter-by Operator");
        }
        try {
            HPFilter filter = FilterOp.valueOf(filterOp);
            String usageFlag = item.getString(7471140);
            return HPSelectorFactory.createAttributeValueSelector(item, usageFlag == null || HPSelectorFactory.isMatch(usageFlag), filter);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Illegal (0072,0406) Filter-by Operator: " + filterOp);
        }
    }

    private static int getSelectorAttribute(Attributes item) {
        int tag = item.getInt(7471142, 0);
        if (tag == 0) {
            throw new IllegalArgumentException("Missing (0072,0026) Selector Attribute");
        }
        return tag;
    }

    private static boolean isPresent(String val) {
        return Presence.fromString(val).isPresent();
    }

    @Generated
    private static HPSelector createAttributeValueSelector(String usageFlag, String privateCreator, int tag, int valueNumber, VR vr, String[] values, HPFilter filterOp) {
        VR vr1 = vr != null ? vr : ElementDictionary.vrOf(tag, privateCreator);
        int valueTag = switch (vr1) {
            case VR.CS -> 7471202;
            case VR.LO -> 7471206;
            case VR.LT -> 7471208;
            case VR.PN -> 7471210;
            case VR.SH -> 7471212;
            case VR.ST -> 7471214;
            case VR.UT -> 0x720070;
            default -> throw new IllegalArgumentException("vr: " + String.valueOf((Object)vr));
        };
        return new Str(tag, privateCreator, valueNumber, valueTag, usageFlag, filterOp, vr, values);
    }

    @Generated
    public static HPSelector createAttributeValueSelector(String usageFlag, String privateCreator, int tag, int valueNumber, VR vr, int[] values, HPFilter filterOp) {
        int valueTag;
        VR vr1 = vr != null ? vr : ElementDictionary.vrOf(tag, privateCreator);
        boolean uint = false;
        switch (vr1) {
            case AT: {
                valueTag = 7471200;
                break;
            }
            case IS: {
                valueTag = 7471204;
                break;
            }
            case SL: {
                valueTag = 7471228;
                break;
            }
            case SS: {
                valueTag = 7471230;
                break;
            }
            case UL: {
                valueTag = 7471224;
                uint = true;
                break;
            }
            case US: {
                valueTag = 7471226;
                break;
            }
            default: {
                throw new IllegalArgumentException("vr: " + String.valueOf((Object)vr));
            }
        }
        return uint ? new UIntSelector(tag, privateCreator, valueNumber, valueTag, usageFlag, filterOp, vr, values) : new IntSelector(tag, privateCreator, valueNumber, valueTag, usageFlag, filterOp, vr, values);
    }

    private static long[] toLong(int[] is) {
        long[] ls = new long[is.length];
        for (int i = 0; i < is.length; ++i) {
            ls[i] = (long)is[i] & 0xFFFFFFFFL;
        }
        return ls;
    }

    private static HPSelector createAttributeValueSelector(String usageFlag, String privateCreator, int tag, int valueNumber, VR vr, float[] values, HPFilter filterOp) {
        VR vr1 = vr != null ? vr : ElementDictionary.vrOf(tag, privateCreator);
        int valueTag = switch (vr1) {
            case VR.DS -> 0x720072;
            case VR.FL -> 7471222;
            default -> throw new IllegalArgumentException("vr: " + String.valueOf((Object)vr));
        };
        return new Flt(tag, privateCreator, valueNumber, valueTag, usageFlag, filterOp, vr, values);
    }

    private static HPSelector createAttributeValueSelector(String usageFlag, String privateCreator, int tag, int valueNumber, double[] values, HPFilter filterOp) {
        return new Dbl(tag, privateCreator, valueNumber, 7471220, usageFlag, filterOp, VR.FD, values);
    }

    private static class CodeValueSelector
    extends AttributeValueSelector {
        private final Collection<Code> params;

        CodeValueSelector(Attributes item, boolean match, HPFilter filterOp, VR vr) {
            super(item, match, filterOp, vr);
            if (filterOp.isNumeric()) {
                throw new IllegalArgumentException("Filter-by Operator: " + item.getString(7472134) + " conflicts with non-numeric VR: SQ");
            }
            this.params = Code.toCodeMacros(item.getSequence(7471232));
            if (this.params.isEmpty()) {
                throw new IllegalArgumentException("Missing (0072,0080) Selector Code Sequence Value");
            }
        }

        CodeValueSelector(int tag, String privateCreator, int valueNumber, String usageFlag, HPFilter filterOp, Code[] values) {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, VR.SQ);
            this.params = Arrays.asList(values);
        }

        @Override
        public boolean matches(Attributes dcmobj, int frame) {
            Sequence values = dcmobj.getSequence(this.privateCreator, this.tag);
            if (values == null || values.isEmpty()) {
                return this.match;
            }
            return this.filterOp.op(Code.toCodeMacros(values), this.params);
        }
    }

    private static class AttributePresenceSelector
    extends BaseAttributeSelector {
        AttributePresenceSelector(Attributes item) {
            super(item, !HPSelectorFactory.isPresent(item.getString(7472132)));
        }

        AttributePresenceSelector(String filter, int tag, String privateCreator) {
            super(tag, privateCreator, !HPSelectorFactory.isPresent(filter));
            this.item.setString(7472132, VR.CS, filter);
        }

        @Override
        public boolean matches(Attributes dcmobj, int frame) {
            return dcmobj.containsValue(this.privateCreator, this.tag) != this.match;
        }
    }

    private static abstract class AttributeSelector
    extends AbstractHPSelector {
        protected final int tag;
        protected final String privateCreator;
        protected final boolean match;

        AttributeSelector(int tag, String privateCreator, boolean match) {
            this.tag = tag;
            this.privateCreator = privateCreator;
            this.match = match;
        }

        public final boolean isMatchIfNotPresent() {
            return this.match;
        }
    }

    private static class Seq
    extends AttributeSelectorDecorator {
        Seq(int tag, String privateCreator, AttributeSelector selector) {
            super(tag, privateCreator, selector.isMatchIfNotPresent(), selector);
        }

        @Override
        public boolean matches(Attributes dcmobj, int frame) {
            Sequence values1 = dcmobj.getSequence(this.privateCreator, this.tag);
            if (values1 == null || values1.isEmpty()) {
                return this.match;
            }
            for (Attributes attributes : values1) {
                if (!this.selector.matches(attributes, frame)) continue;
                return true;
            }
            return false;
        }
    }

    private static class FctGrp
    extends AttributeSelectorDecorator {
        FctGrp(int tag, String privateCreator, AttributeSelector selector) {
            super(tag, privateCreator, selector.isMatchIfNotPresent(), selector);
        }

        @Override
        public boolean matches(Attributes dcmobj, int frame) {
            Sequence fctGrp;
            Attributes sharedFctGrp = dcmobj.getNestedDataset(1375769129);
            if (sharedFctGrp != null && (fctGrp = sharedFctGrp.getSequence(this.privateCreator, this.tag)) != null) {
                return this.matches(fctGrp, frame);
            }
            Sequence frameFctGrpSeq = dcmobj.getSequence(1375769136);
            if (frameFctGrpSeq == null) {
                return this.match;
            }
            if (frame != 0) {
                return this.op((Attributes)frameFctGrpSeq.get(frame - 1), frame);
            }
            for (Attributes attributes : frameFctGrpSeq) {
                if (!this.op(attributes, frame)) continue;
                return true;
            }
            return false;
        }

        private boolean op(Attributes frameFctGrp, int frame) {
            if (frameFctGrp == null) {
                return this.match;
            }
            Sequence fctGrp = frameFctGrp.getSequence(this.privateCreator, this.tag);
            if (fctGrp == null) {
                return this.match;
            }
            return this.matches(fctGrp, frame);
        }

        private boolean matches(Sequence fctGrp, int frame) {
            for (Attributes attributes : fctGrp) {
                if (!this.selector.matches(attributes, frame)) continue;
                return true;
            }
            return false;
        }
    }

    private static class IntSelector
    extends AttributeValueSelector {
        private final int[] params;

        IntSelector(Attributes item, int valueTag, boolean match, HPFilter filterOp, VR vr) {
            super(item, match, filterOp, vr);
            this.params = item.getInts(valueTag);
            if (this.params == null || this.params.length == 0) {
                throw new IllegalArgumentException("Missing " + TagUtils.toString(valueTag));
            }
            if (filterOp.isNumeric() && filterOp.getNumParams() != this.params.length) {
                throw new IllegalArgumentException("Illegal Number of values: " + item.getString(valueTag));
            }
        }

        IntSelector(int tag, String privateCreator, int valueNumber, int valueTag, String usageFlag, HPFilter filterOp, VR vr, int[] values) {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = (int[])values.clone();
            this.item.setInt(valueTag, vr, values);
        }

        @Override
        public boolean matches(Attributes dcmobj, int frame) {
            int[] values = dcmobj.getInts(this.privateCreator, this.tag, this.vr);
            if (values == null || values.length < (this.valueNumber == 0 ? 1 : (this.valueNumber == 65535 ? frame : this.valueNumber))) {
                return this.match;
            }
            return this.filterOp.op(values, this.valueNumber == 65535 ? frame : this.valueNumber, this.params);
        }
    }

    private static class Str
    extends AttributeValueSelector {
        protected final String[] params;

        Str(Attributes item, int valueTag, boolean match, HPFilter filterOp, VR vr) {
            super(item, match, filterOp, vr);
            if (filterOp.isNumeric()) {
                throw new IllegalArgumentException("Filter-by Operator: " + item.getString(7472134) + " conflicts with non-numeric VR: " + item.getString(7471184));
            }
            this.params = item.getStrings(valueTag);
            if (this.params == null || this.params.length == 0) {
                throw new IllegalArgumentException("Missing " + TagUtils.toString(valueTag));
            }
        }

        Str(int tag, String privateCreator, int valueNumber, int valueTag, String usageFlag, HPFilter filterOp, VR vr, String[] values) {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = (String[])values.clone();
            this.item.setString(valueTag, vr, values);
        }

        @Override
        public boolean matches(Attributes dcmobj, int frame) {
            String[] values = dcmobj.getStrings(this.privateCreator, this.tag, this.vr);
            if (values == null || values.length < Math.max(this.valueNumber, 1)) {
                return this.match;
            }
            return this.filterOp.op(values, this.valueNumber, this.params);
        }
    }

    private static class Flt
    extends AttributeValueSelector {
        private final float[] params;

        Flt(Attributes item, int valueTag, boolean match, HPFilter filterOp, VR vr) {
            super(item, match, filterOp, vr);
            this.params = item.getFloats(valueTag);
            if (this.params == null || this.params.length == 0) {
                throw new IllegalArgumentException("Missing " + TagUtils.toString(valueTag));
            }
            if (filterOp.isNumeric() && filterOp.getNumParams() != this.params.length) {
                throw new IllegalArgumentException("Illegal Number of values: " + item.getString(valueTag));
            }
        }

        Flt(int tag, String privateCreator, int valueNumber, int valueTag, String usageFlag, HPFilter filterOp, VR vr, float[] values) {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = (float[])values.clone();
            this.item.setFloat(valueTag, vr, values);
        }

        @Override
        public boolean matches(Attributes dcmobj, int frame) {
            float[] values = dcmobj.getFloats(this.privateCreator, this.tag, this.vr);
            if (values == null || values.length < (this.valueNumber == 0 ? 1 : (this.valueNumber == 65535 ? frame : this.valueNumber))) {
                return this.match;
            }
            return this.filterOp.op(values, this.valueNumber == 65535 ? frame : this.valueNumber, this.params);
        }
    }

    private static class Dbl
    extends AttributeValueSelector {
        private final double[] params;

        Dbl(Attributes item, int valueTag, boolean match, HPFilter filterOp, VR vr) {
            super(item, match, filterOp, vr);
            this.params = item.getDoubles(valueTag);
            if (this.params == null || this.params.length == 0) {
                throw new IllegalArgumentException("Missing " + TagUtils.toString(valueTag));
            }
            if (filterOp.isNumeric() && filterOp.getNumParams() != this.params.length) {
                throw new IllegalArgumentException("Illegal Number of values: " + item.getString(valueTag));
            }
        }

        Dbl(int tag, String privateCreator, int valueNumber, int valueTag, String usageFlag, HPFilter filterOp, VR vr, double[] values) {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = (double[])values.clone();
            this.item.setDouble(valueTag, vr, values);
        }

        @Override
        public boolean matches(Attributes dcmobj, int frame) {
            double[] values = dcmobj.getDoubles(this.privateCreator, this.tag, this.vr);
            if (values == null || values.length < (this.valueNumber == 0 ? 1 : (this.valueNumber == 65535 ? frame : this.valueNumber))) {
                return this.match;
            }
            return this.filterOp.op(values, this.valueNumber == 65535 ? frame : this.valueNumber, this.params);
        }
    }

    private static class UIntSelector
    extends AttributeValueSelector {
        private final long[] params;

        UIntSelector(Attributes item, int valueTag, boolean match, HPFilter filterOp, VR vr) {
            super(item, match, filterOp, vr);
            int[] tmp = item.getInts(valueTag);
            if (tmp == null || tmp.length == 0) {
                throw new IllegalArgumentException("Missing " + TagUtils.toString(valueTag));
            }
            if (filterOp.isNumeric() && filterOp.getNumParams() != tmp.length) {
                throw new IllegalArgumentException("Illegal Number of values: " + item.getString(valueTag));
            }
            this.params = HPSelectorFactory.toLong(tmp);
        }

        UIntSelector(int tag, String privateCreator, int valueNumber, int valueTag, String usageFlag, HPFilter filterOp, VR vr, int[] values) {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = HPSelectorFactory.toLong(values);
            this.item.setInt(valueTag, vr, values);
        }

        @Override
        public boolean matches(Attributes dcmobj, int frame) {
            int[] values = dcmobj.getInts(this.privateCreator, this.tag, this.vr);
            if (values == null || values.length < (this.valueNumber == 0 ? 1 : (this.valueNumber == 65535 ? frame : this.valueNumber))) {
                return this.match;
            }
            return this.filterOp.op(values, this.valueNumber == 65535 ? frame : this.valueNumber, this.params);
        }
    }

    private static abstract class AttributeSelectorDecorator
    extends AttributeSelector {
        protected final HPSelector selector;

        AttributeSelectorDecorator(int tag, String privateCreator, boolean match, HPSelector selector) {
            super(tag, privateCreator, match);
            this.selector = selector;
        }

        @Override
        public Attributes getAttributes() {
            return this.selector.getAttributes();
        }
    }

    private static abstract class AttributeValueSelector
    extends BaseAttributeSelector {
        protected final int valueNumber;
        protected final HPFilter filterOp;
        protected final VR vr;

        AttributeValueSelector(Attributes item, boolean match, HPFilter filterOp, VR vr) {
            super(item, match);
            this.valueNumber = item.getInt(7471144, 0);
            this.filterOp = filterOp;
            this.vr = vr;
        }

        AttributeValueSelector(int tag, String privateCreator, int valueNumber, String usageFlag, HPFilter filterOp, VR vr) {
            super(tag, privateCreator, usageFlag == null || HPSelectorFactory.isMatch(usageFlag));
            this.valueNumber = valueNumber;
            this.filterOp = filterOp != null ? filterOp : FilterOp.MEMBER_OF;
            this.vr = vr;
            this.item.setInt(7471144, VR.US, valueNumber);
            if (filterOp != null) {
                this.item.setString(7472134, VR.CS, filterOp.getCodeString());
            }
            this.item.setString(7471184, VR.CS, vr.toString());
            if (usageFlag != null) {
                this.item.setString(7471140, VR.CS, usageFlag);
            }
        }
    }

    private static abstract class BaseAttributeSelector
    extends AttributeSelector {
        protected final Attributes item;

        BaseAttributeSelector(Attributes item, boolean match) {
            super(HPSelectorFactory.getSelectorAttribute(item), item.getString(7471190), match);
            this.item = item;
        }

        BaseAttributeSelector(int tag, String privateCreator, boolean match) {
            super(tag, privateCreator, match);
            this.item = new Attributes();
            this.item.setInt(7471142, VR.AT, tag);
            if (privateCreator != null) {
                this.item.setString(7471190, VR.LO, privateCreator);
            }
        }

        @Override
        public final Attributes getAttributes() {
            return this.item;
        }
    }
}

