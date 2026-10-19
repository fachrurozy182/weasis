/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.macro;

import java.util.Collection;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;

public class Module {
    protected final Attributes dcmItems;

    public Module(Attributes dcmItems) {
        this.dcmItems = Objects.requireNonNull(dcmItems);
    }

    public final Attributes getAttributes() {
        return this.dcmItems;
    }

    public final void removeAllSequenceItems(int seqTag) {
        Sequence seq = this.dcmItems.getSequence(seqTag);
        if (seq != null) {
            seq.clear();
        }
    }

    public final void removeSequenceItem(int seqTag, int index) {
        Sequence seq = this.dcmItems.getSequence(seqTag);
        if (seq != null && index >= 0 && index < seq.size()) {
            seq.remove(index);
        }
    }

    public final void removeSequenceItem(int seqTag, Attributes item) {
        Sequence seq = this.dcmItems.getSequence(seqTag);
        if (seq != null) {
            seq.remove(item);
        }
    }

    protected void updateSequence(int tag, Module module) {
        this.clearExistingSequence(tag);
        if (module != null) {
            Attributes attributes = this.prepareAttributesForSequence(module.getAttributes());
            this.dcmItems.newSequence(tag, 1).add(attributes);
        }
    }

    protected void updateSequence(int tag, Collection<? extends Module> modules) {
        this.clearExistingSequence(tag);
        if (modules != null && !modules.isEmpty()) {
            Sequence newSequence = this.dcmItems.newSequence(tag, modules.size());
            for (Module module : modules) {
                Attributes attributes = this.prepareAttributesForSequence(module.getAttributes());
                newSequence.add(attributes);
            }
        }
    }

    private void clearExistingSequence(int tag) {
        Sequence oldSequence = this.dcmItems.getSequence(tag);
        if (oldSequence != null) {
            oldSequence.clear();
        }
    }

    private Attributes prepareAttributesForSequence(Attributes attributes) {
        Attributes parent = attributes.getParent();
        if (parent != null) {
            return new Attributes(attributes);
        }
        return attributes;
    }
}

