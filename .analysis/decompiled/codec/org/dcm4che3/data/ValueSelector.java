/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.Serializable;
import java.util.Objects;
import org.dcm4che3.data.AttributeSelector;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;

public class ValueSelector
implements Serializable {
    private static final long serialVersionUID = 7043276856762009199L;
    private final AttributeSelector attributeSelector;
    private final int valueIndex;
    private String str;

    public ValueSelector(int tag, String privateCreator, int index, ItemPointer ... itemPointers) {
        this(new AttributeSelector(tag, privateCreator, itemPointers), index);
    }

    public ValueSelector(AttributeSelector attributeSelector, int index) {
        this.attributeSelector = Objects.requireNonNull(attributeSelector);
        this.valueIndex = index;
    }

    public int tag() {
        return this.attributeSelector.tag();
    }

    public String privateCreator() {
        return this.attributeSelector.privateCreator();
    }

    public int level() {
        return this.attributeSelector.level();
    }

    public ItemPointer itemPointer(int index) {
        return this.attributeSelector.itemPointer(index);
    }

    public int valueIndex() {
        return this.valueIndex;
    }

    public String selectStringValue(Attributes attrs, String defVal) {
        return this.attributeSelector.selectStringValue(attrs, this.valueIndex, defVal);
    }

    public String toString() {
        if (this.str == null) {
            this.str = this.attributeSelector.toStringBuilder().append("/Value[@number=\"").append(this.valueIndex + 1).append("\"]").toString();
        }
        return this.str;
    }

    public static ValueSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new ValueSelector(AttributeSelector.valueOf(s), AttributeSelector.selectNumber(s, fromIndex) - 1);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ValueSelector)) {
            return false;
        }
        return this.toString().equals(obj.toString());
    }
}

