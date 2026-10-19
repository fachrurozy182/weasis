/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.hp.HPDisplaySet;
import org.weasis.dicom.macro.Module;

public class HPScrollingGroup
extends Module {
    private final List<HPDisplaySet> displaySets;

    public HPScrollingGroup() {
        super(new Attributes());
        this.displaySets = new ArrayList<HPDisplaySet>();
    }

    public HPScrollingGroup(Attributes attributes, List<HPDisplaySet> totDisplaySets) {
        super(attributes);
        int[] group = attributes.getInts(7471634);
        if (group == null) {
            throw new IllegalArgumentException("Missing (0072,0212) Display Set Scrolling Group");
        }
        if (group.length < 2) {
            throw new IllegalArgumentException("Display Set Scrolling Group cannot have less than 2 Display Sets");
        }
        this.displaySets = new ArrayList<HPDisplaySet>(group.length);
        for (int i : group) {
            try {
                this.displaySets.add(totDisplaySets.get(i - 1));
            }
            catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Referenced Display Set does not exists");
            }
        }
    }

    @Generated
    public List<HPDisplaySet> getDisplaySets() {
        return Collections.unmodifiableList(this.displaySets);
    }

    public void addDisplaySet(HPDisplaySet displaySet) {
        if (displaySet.getDisplaySetNumber() == 0) {
            throw new IllegalArgumentException("Missing Display Set Number");
        }
        this.displaySets.add(displaySet);
        this.updateAttributes();
    }

    public boolean removeDisplaySet(HPDisplaySet displaySet) {
        if (displaySet == null) {
            throw new NullPointerException();
        }
        if (!this.displaySets.remove(displaySet)) {
            return false;
        }
        this.updateAttributes();
        return true;
    }

    public void updateAttributes() {
        int[] val = new int[this.displaySets.size()];
        for (int i = 0; i < val.length; ++i) {
            val[i] = this.displaySets.get(i).getDisplaySetNumber();
        }
        this.dcmItems.setInt(7471634, VR.US, val);
    }

    public boolean isValid() {
        return this.displaySets.size() >= 2;
    }
}

