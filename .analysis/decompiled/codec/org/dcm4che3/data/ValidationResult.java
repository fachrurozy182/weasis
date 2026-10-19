/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.util.ArrayList;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;

public class ValidationResult {
    private ArrayList<IOD.DataElement> missingAttributes;
    private ArrayList<IOD.DataElement> missingAttributeValues;
    private ArrayList<IOD.DataElement> notAllowedAttributes;
    private ArrayList<InvalidAttributeValue> invalidAttributeValues;

    public boolean hasMissingAttributes() {
        return this.missingAttributes != null;
    }

    public boolean hasMissingAttributeValues() {
        return this.missingAttributeValues != null;
    }

    public boolean hasInvalidAttributeValues() {
        return this.invalidAttributeValues != null;
    }

    public boolean hasNotAllowedAttributes() {
        return this.notAllowedAttributes != null;
    }

    public boolean isValid() {
        return !this.hasMissingAttributes() && !this.hasMissingAttributeValues() && !this.hasInvalidAttributeValues() && !this.hasNotAllowedAttributes();
    }

    public void addMissingAttribute(IOD.DataElement dataElement) {
        if (this.missingAttributes == null) {
            this.missingAttributes = new ArrayList();
        }
        this.missingAttributes.add(dataElement);
    }

    public void addMissingAttributeValue(IOD.DataElement dataElement) {
        if (this.missingAttributeValues == null) {
            this.missingAttributeValues = new ArrayList();
        }
        this.missingAttributeValues.add(dataElement);
    }

    public void addInvalidAttributeValue(IOD.DataElement dataElement, Invalid reason) {
        this.addInvalidAttributeValue(dataElement, reason, null, null);
    }

    public void addInvalidAttributeValue(IOD.DataElement dataElement, Invalid reason, ValidationResult[] itemValidationResult, IOD[] missingItems) {
        if (this.invalidAttributeValues == null) {
            this.invalidAttributeValues = new ArrayList();
        }
        this.invalidAttributeValues.add(new InvalidAttributeValue(dataElement, reason, itemValidationResult, missingItems));
    }

    public void addNotAllowedAttribute(IOD.DataElement el) {
        if (this.notAllowedAttributes == null) {
            this.notAllowedAttributes = new ArrayList();
        }
        this.notAllowedAttributes.add(el);
    }

    public int[] tagsOfNotAllowedAttributes() {
        return this.tagsOf(this.notAllowedAttributes);
    }

    public int[] tagsOfMissingAttributeValues() {
        return this.tagsOf(this.missingAttributeValues);
    }

    public int[] tagsOfMissingAttributes() {
        return this.tagsOf(this.missingAttributes);
    }

    public int[] tagsOfInvalidAttributeValues() {
        ArrayList<InvalidAttributeValue> list = this.invalidAttributeValues;
        if (list == null) {
            return ByteUtils.EMPTY_INTS;
        }
        int[] tags = new int[list.size()];
        for (int i = 0; i < tags.length; ++i) {
            tags[i] = list.get((int)i).dataElement.tag;
        }
        return tags;
    }

    public int[] getOffendingElements() {
        return this.cat(this.tagsOfMissingAttributes(), this.tagsOfMissingAttributeValues(), this.tagsOfInvalidAttributeValues(), this.tagsOfNotAllowedAttributes());
    }

    private int[] cat(int[] ... iss) {
        int length = 0;
        for (int[] is : iss) {
            length += is.length;
        }
        int[] tags = new int[length];
        int off = 0;
        for (int[] is : iss) {
            System.arraycopy(is, 0, tags, off, is.length);
            off += is.length;
        }
        return tags;
    }

    private int[] tagsOf(List<IOD.DataElement> list) {
        if (list == null) {
            return ByteUtils.EMPTY_INTS;
        }
        int[] tags = new int[list.size()];
        for (int i = 0; i < tags.length; ++i) {
            tags[i] = list.get((int)i).tag;
        }
        return tags;
    }

    public String getErrorComment() {
        StringBuilder sb = new StringBuilder();
        if (this.notAllowedAttributes != null) {
            return ValidationResult.errorComment(sb, "Not allowed Attribute", this.tagsOfNotAllowedAttributes()).toString();
        }
        if (this.missingAttributes != null) {
            return ValidationResult.errorComment(sb, "Missing Attribute", this.tagsOfMissingAttributes()).toString();
        }
        if (this.missingAttributeValues != null) {
            return ValidationResult.errorComment(sb, "Missing Value of Attribute", this.tagsOfMissingAttributeValues()).toString();
        }
        if (this.invalidAttributeValues != null) {
            return ValidationResult.errorComment(sb, "Invalid Attribute", this.tagsOfInvalidAttributeValues()).toString();
        }
        return null;
    }

    private static StringBuilder errorComment(StringBuilder sb, String prompt, int[] tags) {
        sb.append(prompt);
        String prefix = tags.length > 1 ? "s: " : ": ";
        for (int tag : tags) {
            sb.append(prefix).append(TagUtils.toString(tag));
            prefix = ", ";
        }
        return sb;
    }

    public String toString() {
        if (this.isValid()) {
            return "VALID";
        }
        StringBuilder sb = new StringBuilder();
        if (this.notAllowedAttributes != null) {
            ValidationResult.errorComment(sb, "Not allowed Attribute", this.tagsOfNotAllowedAttributes()).append(StringUtils.LINE_SEPARATOR);
        }
        if (this.missingAttributes != null) {
            ValidationResult.errorComment(sb, "Missing Attribute", this.tagsOfMissingAttributes()).append(StringUtils.LINE_SEPARATOR);
        }
        if (this.missingAttributeValues != null) {
            ValidationResult.errorComment(sb, "Missing Value of Attribute", this.tagsOfMissingAttributeValues()).append(StringUtils.LINE_SEPARATOR);
        }
        if (this.invalidAttributeValues != null) {
            ValidationResult.errorComment(sb, "Invalid Attribute", this.tagsOfInvalidAttributeValues()).append(StringUtils.LINE_SEPARATOR);
        }
        return sb.substring(0, sb.length() - 1);
    }

    public String asText(Attributes attrs) {
        if (this.isValid()) {
            return "VALID";
        }
        StringBuilder sb = new StringBuilder();
        this.appendTextTo(0, attrs, sb);
        return sb.substring(0, sb.length() - 1);
    }

    private void appendTextTo(int level, Attributes attrs, StringBuilder sb) {
        if (this.notAllowedAttributes != null) {
            this.appendTextTo(level, attrs, "Not allowed Attributes:", this.notAllowedAttributes, sb);
        }
        if (this.missingAttributes != null) {
            this.appendTextTo(level, attrs, "Missing Attributes:", this.missingAttributes, sb);
        }
        if (this.missingAttributeValues != null) {
            this.appendTextTo(level, attrs, "Missing Attribute Values:", this.missingAttributeValues, sb);
        }
        if (this.invalidAttributeValues != null) {
            this.appendInvalidAttributeValues(level, attrs, "Invalid Attribute Values:", sb);
        }
    }

    private void appendTextTo(int level, Attributes attrs, String title, List<IOD.DataElement> list, StringBuilder sb) {
        this.appendPrefixTo(level, sb);
        sb.append(title).append(StringUtils.LINE_SEPARATOR);
        for (IOD.DataElement el : list) {
            this.appendAttribute(level, el.tag, sb);
            this.appendIODRef(el.getLineNumber(), sb);
            sb.append(StringUtils.LINE_SEPARATOR);
        }
    }

    private void appendIODRef(int lineNumber, StringBuilder sb) {
        if (lineNumber > 0) {
            sb.append(" // IOD line #").append(lineNumber);
        }
    }

    private void appendInvalidAttributeValues(int level, Attributes attrs, String title, StringBuilder sb) {
        this.appendPrefixTo(level, sb);
        sb.append(title);
        sb.append(StringUtils.LINE_SEPARATOR);
        for (InvalidAttributeValue iav : this.invalidAttributeValues) {
            int tag = iav.dataElement.tag;
            this.appendAttribute(level, tag, sb);
            VR.Holder vr = new VR.Holder();
            Object value = attrs.getValue(tag, vr);
            sb.append(' ').append((Object)vr.vr);
            sb.append(" [");
            vr.vr.prompt(value, attrs.bigEndian(), attrs.getSpecificCharacterSet(vr.vr), 200, sb);
            sb.append(']');
            if (iav.reason != Invalid.Item) {
                sb.append(" Invalid ").append((Object)iav.reason);
                this.appendIODRef(iav.dataElement.getLineNumber(), sb);
            }
            sb.append(StringUtils.LINE_SEPARATOR);
            if (iav.missingItems != null) {
                for (IOD iod : iav.missingItems) {
                    this.appendPrefixTo(level + 1, sb);
                    sb.append("Missing Item");
                    this.appendIODRef(iod.getLineNumber(), sb);
                    sb.append(StringUtils.LINE_SEPARATOR);
                }
            }
            if (iav.itemValidationResults == null) continue;
            Sequence seq = (Sequence)value;
            for (int i = 0; i < iav.itemValidationResults.length; ++i) {
                ValidationResult itemResult = iav.itemValidationResults[i];
                if (itemResult.isValid()) continue;
                this.appendPrefixTo(level + 1, sb);
                sb.append("Invalid Item ").append(i + 1).append(':').append(StringUtils.LINE_SEPARATOR);
                itemResult.appendTextTo(level + 1, (Attributes)seq.get(i), sb);
            }
        }
    }

    private void appendAttribute(int level, int tag, StringBuilder sb) {
        this.appendPrefixTo(level, sb);
        sb.append(TagUtils.toString(tag)).append(' ').append(ElementDictionary.keywordOf(tag, null));
    }

    private void appendPrefixTo(int level, StringBuilder sb) {
        while (level-- > 0) {
            sb.append('>');
        }
    }

    public static enum Invalid {
        VR,
        VM,
        Value,
        Item,
        MultipleItems,
        Code;

    }

    public class InvalidAttributeValue {
        public final IOD.DataElement dataElement;
        public final Invalid reason;
        public final ValidationResult[] itemValidationResults;
        public final IOD[] missingItems;

        public InvalidAttributeValue(IOD.DataElement dataElement, Invalid reason, ValidationResult[] itemValidationResults, IOD[] missingItems) {
            this.dataElement = dataElement;
            this.reason = reason;
            this.itemValidationResults = itemValidationResults;
            this.missingItems = missingItems;
        }
    }
}

