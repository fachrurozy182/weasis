/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp;

import org.dcm4che3.data.Attributes;

public interface HPSelector {
    public boolean matches(Attributes var1, int var2);

    public Attributes getAttributes();

    public String getImageSetSelectorUsageFlag();

    public String getFilterByCategory();

    public String getFilterByAttributePresence();

    public Integer getSelectorAttribute();

    public String getSelectorAttributeVR();

    public Integer getSelectorSequencePointer();

    public Integer getFunctionalGroupPointer();

    public String getSelectorSequencePointerPrivateCreator();

    public String getFunctionalGroupPrivateCreator();

    public String getSelectorAttributePrivateCreator();

    public Object getSelectorValue();

    public Integer getSelectorValueNumber();

    public String getFilterByOperator();
}

