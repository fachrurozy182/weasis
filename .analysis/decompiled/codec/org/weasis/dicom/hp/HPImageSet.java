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
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.hp.HPSelector;
import org.weasis.dicom.hp.HPTimeBasedImageSet;
import org.weasis.dicom.macro.Module;

public class HPImageSet
extends Module {
    private final List<HPSelector> selectors;
    private int imageSetNumber;

    protected HPImageSet(List<HPSelector> selectors, Attributes attributes) {
        super(attributes);
        this.selectors = selectors;
        this.addSTimeBasedImageSetsSequence(attributes.getSequence(7471152));
    }

    protected HPImageSet(int imageSetNumber) {
        this(null, imageSetNumber);
    }

    protected HPImageSet(HPImageSet shareSelectors, int imageSetNumber) {
        super(new Attributes());
        this.imageSetNumber = imageSetNumber;
        if (shareSelectors == null) {
            this.selectors = new ArrayList<HPSelector>(4);
        } else {
            this.selectors = new ArrayList<HPSelector>(shareSelectors.selectors);
            Sequence tbissq = shareSelectors.getTimeBasedImageSetsSequence();
            this.addSTimeBasedImageSetsSequence(tbissq);
        }
    }

    protected void addSTimeBasedImageSetsSequence(Sequence sequence) {
        if (sequence != null && !sequence.isEmpty()) {
            Sequence seq = this.dcmItems.ensureSequence(7471152, sequence.size());
            for (Attributes a : sequence) {
                seq.add(new Attributes(a));
            }
            this.setImageSetNumber(this.imageSetNumber);
        }
    }

    public boolean contains(Attributes dcm, int frame) {
        for (HPSelector selector : this.selectors) {
            if (selector.matches(dcm, frame)) continue;
            return false;
        }
        return true;
    }

    @Generated
    public Sequence getImageSetSelectorSequence() {
        return this.dcmItems.getSequence(0x720022);
    }

    @Generated
    public Sequence getTimeBasedImageSetsSequence() {
        return this.dcmItems.getSequence(7471152);
    }

    @Generated
    public void addTimeBasedImageSet(HPTimeBasedImageSet item) {
        this.dcmItems.ensureSequence(7471152, 1).add(item.getAttributes());
    }

    @Generated
    public List<HPSelector> getImageSetSelectors() {
        return Collections.unmodifiableList(this.selectors);
    }

    @Generated
    public void addImageSetSelector(HPSelector selector) {
        this.dcmItems.ensureSequence(0x720022, 1).add(selector.getAttributes());
        this.selectors.add(selector);
    }

    @Generated
    public int getImageSetNumber() {
        return this.imageSetNumber;
    }

    public void setImageSetNumber(int i) {
        this.imageSetNumber = i;
        Sequence seq = this.getTimeBasedImageSetsSequence();
        if (seq != null && !seq.isEmpty()) {
            seq.forEach(attributes1 -> attributes1.setInt(7471154, VR.US, i));
        }
    }
}

