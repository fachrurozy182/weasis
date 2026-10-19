/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.util.TagUtils;

public class AttributeSelector
implements Serializable {
    private static final long serialVersionUID = 9122372717582373730L;
    private static final int MIN_ITEM_POINTER_STR_LEN = 30;
    private final int tag;
    private final String privateCreator;
    private final List<ItemPointer> itemPointers;
    private String str;

    public AttributeSelector(int tag) {
        this(tag, null, Collections.EMPTY_LIST);
    }

    public AttributeSelector(int tag, String privateCreator) {
        this(tag, privateCreator, Collections.EMPTY_LIST);
    }

    public AttributeSelector(int tag, String privateCreator, ItemPointer ... itemPointers) {
        this(tag, privateCreator, Arrays.asList(itemPointers));
    }

    public AttributeSelector(int tag, String privateCreator, List<ItemPointer> itemPointers) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.itemPointers = itemPointers;
    }

    public int tag() {
        return this.tag;
    }

    public String privateCreator() {
        return this.privateCreator;
    }

    public int level() {
        return this.itemPointers.size();
    }

    public ItemPointer itemPointer(int index) {
        return this.itemPointers.get(index);
    }

    public String selectStringValue(Attributes attrs, int valueIndex, String defVal) {
        Attributes item = attrs.getNestedDataset(this.itemPointers);
        return item != null ? item.getString(this.privateCreator, this.tag, valueIndex, defVal) : defVal;
    }

    public String toString() {
        if (this.str == null) {
            this.str = this.toStringBuilder().toString();
        }
        return this.str;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        AttributeSelector that = (AttributeSelector)o;
        return this.tag == that.tag && Objects.equals(this.privateCreator, that.privateCreator) && this.itemPointers.equals(that.itemPointers);
    }

    public int hashCode() {
        return Objects.hash(this.tag, this.privateCreator, this.itemPointers);
    }

    StringBuilder toStringBuilder() {
        StringBuilder sb = new StringBuilder(32);
        for (ItemPointer ip : this.itemPointers) {
            this.appendTo(ip.sequenceTag, ip.privateCreator, "\"]/Item", sb);
            if (ip.itemIndex >= 0) {
                sb.append("[@number=\"").append(ip.itemIndex + 1).append("\"]");
            }
            sb.append('/');
        }
        return this.appendTo(this.tag, this.privateCreator, "\"]", sb);
    }

    private StringBuilder appendTo(int tag, String privateCreator, String suffix, StringBuilder sb) {
        sb.append("DicomAttribute[@tag=\"").append(TagUtils.toHexString(tag));
        if (privateCreator != null) {
            sb.append("\" and @privateCreator=\"").append(privateCreator);
        }
        return sb.append(suffix);
    }

    public static AttributeSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new AttributeSelector(AttributeSelector.selectTag(s, fromIndex), AttributeSelector.selectPrivateCreator(s, fromIndex), AttributeSelector.itemPointersOf(s, fromIndex));
        }
        catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    static int selectTag(String s, int fromIndex) {
        String tagStr = AttributeSelector.select("@tag=", s, fromIndex);
        return Integer.parseInt(tagStr, 16);
    }

    static String selectPrivateCreator(String s, int fromIndex) {
        return AttributeSelector.select("@privateCreator=", s, fromIndex);
    }

    static int selectNumber(String s, int fromIndex) {
        String no = AttributeSelector.select("@number=", s, fromIndex);
        return no != null ? Integer.parseInt(no) : 0;
    }

    private static List<ItemPointer> itemPointersOf(String s, int endIndex) {
        if (endIndex == 0) {
            return Collections.emptyList();
        }
        ArrayList<ItemPointer> list = new ArrayList<ItemPointer>();
        int fromIndex = 0;
        while (fromIndex < endIndex) {
            list.add(new ItemPointer(AttributeSelector.selectPrivateCreator(s, fromIndex), AttributeSelector.selectTag(s, fromIndex), AttributeSelector.selectNumber(s, fromIndex) - 1));
            fromIndex = s.indexOf("DicomAttribute", fromIndex + 30);
        }
        list.trimToSize();
        return list;
    }

    private static String select(String key, String s, int fromIndex) {
        int pos = s.indexOf(key, fromIndex);
        if (pos < 0) {
            return null;
        }
        int quotePos = pos + key.length();
        int beginIndex = quotePos + 1;
        return s.substring(beginIndex, s.indexOf(s.charAt(quotePos), beginIndex));
    }

    public boolean matches(List<ItemPointer> itemPointers, String privateCreator, int tag) {
        int level;
        block4: {
            block3: {
                if (tag != this.tag || !Objects.equals(privateCreator, this.privateCreator)) break block3;
                level = this.level();
                if (itemPointers.size() == level) break block4;
            }
            return false;
        }
        for (int i = 0; i < level; ++i) {
            ItemPointer itemPointer = itemPointers.get(i);
            ItemPointer other = this.itemPointer(i);
            if (itemPointer.itemIndex >= 0 && other.itemIndex >= 0 ? itemPointer.equals(other) : itemPointer.equalsIgnoreItemIndex(other)) continue;
            return false;
        }
        return true;
    }
}

