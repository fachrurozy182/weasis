/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.codec.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.weasis.dicom.mf.SopInstance;

public class SeriesInstanceList {
    private final Map<String, SopInstance> dicomInstanceMap = new HashMap<String, SopInstance>();
    private final Map<String, SopInstance> dicomUniqueSopInstanceMap = new HashMap<String, SopInstance>();
    private boolean containsMultiframes = false;

    public void addSopInstance(SopInstance s) {
        if (s != null) {
            SopInstance.addSopInstance(this.dicomInstanceMap, s);
            SopInstance sop = this.dicomUniqueSopInstanceMap.get(s.getSopInstanceUID());
            if (sop == null) {
                this.dicomUniqueSopInstanceMap.put(s.getSopInstanceUID(), s);
            } else {
                this.containsMultiframes = true;
            }
        }
    }

    public SopInstance getSopInstance(String sopUID, Integer instanceNumber) {
        return SopInstance.getSopInstance(this.dicomInstanceMap, sopUID, instanceNumber);
    }

    public SopInstance getSopInstance(String sopUID) {
        return this.dicomUniqueSopInstanceMap.get(sopUID);
    }

    public boolean isContainsMultiframes() {
        return this.containsMultiframes;
    }

    public boolean isEmpty() {
        return this.dicomInstanceMap.isEmpty();
    }

    public int size() {
        return this.dicomInstanceMap.size();
    }

    public int uniqueSopInstanceCount() {
        return this.dicomUniqueSopInstanceMap.size();
    }

    public List<SopInstance> getSortedList() {
        ArrayList<SopInstance> sopList = new ArrayList<SopInstance>(this.dicomInstanceMap.values());
        Collections.sort(sopList);
        return sopList;
    }
}

