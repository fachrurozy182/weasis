/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.Serializable;
import java.util.Objects;

public class ItemPointer
implements Serializable {
    private static final long serialVersionUID = 5183950023496022964L;
    public final int sequenceTag;
    public final String privateCreator;
    public final int itemIndex;

    public ItemPointer(int sequenceTag) {
        this(null, sequenceTag, -1);
    }

    public ItemPointer(int sequenceTag, int itemIndex) {
        this(null, sequenceTag, itemIndex);
    }

    public ItemPointer(String privateCreator, int sequenceTag) {
        this(privateCreator, sequenceTag, -1);
    }

    public ItemPointer(String privateCreator, int sequenceTag, int itemIndex) {
        this.sequenceTag = sequenceTag;
        this.privateCreator = privateCreator;
        this.itemIndex = itemIndex;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ItemPointer that = (ItemPointer)o;
        return this.sequenceTag == that.sequenceTag && this.itemIndex == that.itemIndex && Objects.equals(this.privateCreator, that.privateCreator);
    }

    public int hashCode() {
        return Objects.hash(this.sequenceTag, this.privateCreator, this.itemIndex);
    }

    public String toString() {
        return "ItemPointer{sequenceTag=" + this.sequenceTag + ", privateCreator='" + this.privateCreator + '\'' + ", itemIndex=" + this.itemIndex + '}';
    }

    public boolean equalsIgnoreItemIndex(ItemPointer that) {
        return this.sequenceTag == that.sequenceTag && Objects.equals(this.privateCreator, that.privateCreator);
    }
}

