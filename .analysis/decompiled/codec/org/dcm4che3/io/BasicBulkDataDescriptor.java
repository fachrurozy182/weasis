/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dcm4che3.data.AttributeSelector;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;

public class BasicBulkDataDescriptor
implements BulkDataDescriptor {
    private String bulkDataDescriptorID;
    private boolean excludeDefaults;
    private final List<AttributeSelector> selectors = new ArrayList<AttributeSelector>();
    private final EnumMap<VR, Integer> lengthsThresholdByVR = new EnumMap(VR.class);

    public BasicBulkDataDescriptor() {
    }

    public BasicBulkDataDescriptor(String bulkDataDescriptorID) {
        this.bulkDataDescriptorID = bulkDataDescriptorID;
    }

    public String getBulkDataDescriptorID() {
        return this.bulkDataDescriptorID;
    }

    public void setBulkDataDescriptorID(String bulkDataDescriptorID) {
        this.bulkDataDescriptorID = bulkDataDescriptorID;
    }

    public boolean isExcludeDefaults() {
        return this.excludeDefaults;
    }

    public BasicBulkDataDescriptor excludeDefaults() {
        return this.excludeDefaults(true);
    }

    public BasicBulkDataDescriptor excludeDefaults(boolean excludeDefaults) {
        this.excludeDefaults = excludeDefaults;
        return this;
    }

    public BasicBulkDataDescriptor addAttributeSelector(AttributeSelector ... selectors) {
        for (AttributeSelector selector : selectors) {
            this.selectors.add(Objects.requireNonNull(selector));
        }
        return this;
    }

    public AttributeSelector[] getAttributeSelectors() {
        return this.selectors.toArray(new AttributeSelector[0]);
    }

    public void setAttributeSelectorsFromStrings(String[] ss) {
        ArrayList<AttributeSelector> tmp = new ArrayList<AttributeSelector>(ss.length);
        for (String s : ss) {
            tmp.add(AttributeSelector.valueOf(s));
        }
        this.selectors.clear();
        this.selectors.addAll(tmp);
    }

    public BasicBulkDataDescriptor addTag(int ... tags) {
        for (int tag : tags) {
            this.selectors.add(new AttributeSelector(tag));
        }
        return this;
    }

    public BasicBulkDataDescriptor addTagPath(int ... tagPaths) {
        if (tagPaths.length == 0) {
            throw new IllegalArgumentException("tagPaths.length == 0");
        }
        this.selectors.add(new AttributeSelector(tagPaths[tagPaths.length - 1], null, BasicBulkDataDescriptor.toItemPointers(tagPaths)));
        return this;
    }

    private static List<ItemPointer> toItemPointers(int[] tagPaths) {
        int level = tagPaths.length - 1;
        if (level == 0) {
            return Collections.emptyList();
        }
        ArrayList<ItemPointer> itemPointers = new ArrayList<ItemPointer>(level);
        for (int i = 0; i < level; ++i) {
            itemPointers.add(new ItemPointer(tagPaths[i]));
        }
        return itemPointers;
    }

    public BasicBulkDataDescriptor addLengthsThreshold(int threshold, VR ... vrs) {
        if (vrs.length == 0) {
            throw new IllegalArgumentException("Missing VR");
        }
        for (VR vr : vrs) {
            this.lengthsThresholdByVR.put(vr, threshold);
        }
        return this;
    }

    public String[] getLengthsThresholdsAsStrings() {
        if (this.lengthsThresholdByVR.isEmpty()) {
            return StringUtils.EMPTY_STRING;
        }
        HashMap<Integer, EnumSet<VR>> vrsByLength = new HashMap<Integer, EnumSet<VR>>();
        for (Map.Entry<VR, Integer> entry : this.lengthsThresholdByVR.entrySet()) {
            EnumSet<VR> vrs = (EnumSet<VR>)vrsByLength.get(entry.getValue());
            if (vrs == null) {
                vrs = EnumSet.noneOf(VR.class);
                vrsByLength.put(entry.getValue(), vrs);
            }
            vrs.add(entry.getKey());
        }
        String[] ss = new String[vrsByLength.size()];
        int i = 0;
        for (Map.Entry entry : vrsByLength.entrySet()) {
            StringBuilder sb = new StringBuilder();
            Iterator vr = ((EnumSet)entry.getValue()).iterator();
            sb.append(vr.next());
            while (vr.hasNext()) {
                sb.append(',').append(vr.next());
            }
            ss[i++] = sb.append('=').append(entry.getKey()).toString();
        }
        return ss;
    }

    public void setLengthsThresholdsFromStrings(String ... ss) {
        EnumMap<VR, Integer> tmp = new EnumMap<VR, Integer>(VR.class);
        for (String s : ss) {
            String[] entry = StringUtils.split(s, '=');
            if (entry.length != 2) {
                throw new IllegalArgumentException(s);
            }
            try {
                Integer length = Integer.valueOf(entry[1]);
                for (String vr : StringUtils.split(entry[0], ',')) {
                    tmp.put(VR.valueOf(vr), length);
                }
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(s);
            }
        }
        this.lengthsThresholdByVR.clear();
        this.lengthsThresholdByVR.putAll(tmp);
    }

    @Override
    public boolean isBulkData(List<ItemPointer> itemPointers, String privateCreator, int tag, VR vr, int length) {
        return !this.excludeDefaults && BasicBulkDataDescriptor.isStandardBulkData(itemPointers, tag) || this.selected(itemPointers, privateCreator, tag) || BasicBulkDataDescriptor.exeeds(length, this.lengthsThresholdByVR.get((Object)vr));
    }

    private boolean selected(List<ItemPointer> itemPointers, String privateCreator, int tag) {
        for (AttributeSelector selector : this.selectors) {
            if (!selector.matches(itemPointers, privateCreator, tag)) continue;
            return true;
        }
        return false;
    }

    static boolean isStandardBulkData(List<ItemPointer> itemPointer, int tag) {
        switch (TagUtils.normalizeRepeatingGroup(tag)) {
            case 2626049: 
            case 2626050: 
            case 2626051: 
            case 2626052: 
            case 0x281211: 
            case 0x281212: 
            case 2626067: 
            case 0x281221: 
            case 0x281222: 
            case 2626083: 
            case 2626084: 
            case 2654176: 
            case 4325393: 
            case 1342185484: 
            case 0x50003000: 
            case 1442840608: 
            case 0x60003000: 
            case 2145386504: 
            case 2145386505: 
            case 2145386512: {
                return itemPointer.isEmpty();
            }
            case 1409290256: {
                return itemPointer.size() == 1 && itemPointer.get((int)0).sequenceTag == 1409286400;
            }
        }
        return false;
    }

    private static boolean exeeds(int length, Integer lengthThreshold) {
        return lengthThreshold != null && length > lengthThreshold;
    }
}

