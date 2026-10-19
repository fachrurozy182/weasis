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

public class HPNavigationGroup
extends Module {
    private HPDisplaySet navDisplaySet;
    private final List<HPDisplaySet> refDisplaySets;

    public HPNavigationGroup() {
        super(new Attributes());
        this.refDisplaySets = new ArrayList<HPDisplaySet>();
    }

    public HPNavigationGroup(Attributes attributes, List<HPDisplaySet> displaySets) {
        super(attributes);
        int[] group = attributes.getInts(7471640);
        if (group == null || group.length == 0) {
            throw new IllegalArgumentException("Missing (0072,0218) Reference Display Sets");
        }
        int nds = attributes.getInt(7471638, 0);
        if (nds != 0) {
            try {
                this.navDisplaySet = displaySets.get(nds - 1);
            }
            catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Navigation Display Set does not exists: " + attributes.getString(7471638));
            }
        } else if (group.length == 1) {
            throw new IllegalArgumentException("Singular Reference Display Set without Navigation Display Set: " + attributes.getString(7471640));
        }
        this.refDisplaySets = new ArrayList<HPDisplaySet>(group.length);
        for (int i : group) {
            try {
                this.refDisplaySets.add(displaySets.get(i - 1));
            }
            catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Reference Display Set does not exists: " + attributes.getString(7471640));
            }
        }
    }

    @Generated
    public final HPDisplaySet getNavigationDisplaySet() {
        return this.navDisplaySet;
    }

    @Generated
    public final void setNavigationDisplaySet(HPDisplaySet displaySet) {
        this.navDisplaySet = displaySet;
        this.updateDisplaySetNumber();
    }

    @Generated
    public List<HPDisplaySet> getReferenceDisplaySets() {
        return Collections.unmodifiableList(this.refDisplaySets);
    }

    public void addReferenceDisplaySet(HPDisplaySet displaySet) {
        if (displaySet.getDisplaySetNumber() == 0) {
            throw new IllegalArgumentException("Missing Display Set Number");
        }
        this.refDisplaySets.add(displaySet);
        this.updateReferenceDisplaySets();
    }

    public boolean removeReferenceDisplaySet(HPDisplaySet displaySet) {
        if (displaySet == null) {
            throw new NullPointerException();
        }
        if (!this.refDisplaySets.remove(displaySet)) {
            return false;
        }
        this.updateReferenceDisplaySets();
        return true;
    }

    public void updateDisplaySetNumber() {
        if (this.navDisplaySet == null) {
            this.dcmItems.remove(7471638);
        } else {
            Integer val = this.navDisplaySet.getDisplaySetNumber();
            if (val != null) {
                this.dcmItems.setInt(7471638, VR.US, val);
            }
        }
        this.updateReferenceDisplaySets();
    }

    public void updateAttributes() {
        this.updateDisplaySetNumber();
        this.updateReferenceDisplaySets();
    }

    private void updateReferenceDisplaySets() {
        int[] val = new int[this.refDisplaySets.size()];
        for (int i = 0; i < val.length; ++i) {
            val[i] = this.refDisplaySets.get(i).getDisplaySetNumber();
        }
        this.dcmItems.setInt(7471640, VR.US, val);
    }

    public boolean isValid() {
        return this.refDisplaySets.size() >= (this.navDisplaySet != null ? 1 : 2);
    }
}

