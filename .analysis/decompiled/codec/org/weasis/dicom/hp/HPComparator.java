/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp;

import org.dcm4che3.data.Attributes;

public interface HPComparator {
    public int compare(Attributes var1, int var2, Attributes var3, int var4);

    public Attributes getAttributes();

    public String getImageSetSelectorUsageFlag();

    public Integer getSelectorAttribute();

    public Integer getSelectorSequencePointer();

    public Integer getFunctionalGroupPointer();

    public String getSelectorSequencePointerPrivateCreator();

    public String getFunctionalGroupPrivateCreator();

    public String getSelectorAttributePrivateCreator();

    public Integer getSelectorValueNumber();

    public String getSortByCategory();

    public String getSortingDirection();
}

