/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.ref;

import java.util.Objects;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.macro.ItemCode;
import org.weasis.dicom.ref.AnatomicItem;
import org.weasis.dicom.ref.CodingScheme;

public record OtherPart(String codeValue, String codeMeaning, CodingScheme codingScheme, boolean paired) implements AnatomicItem
{
    public OtherPart {
        if (!StringUtil.hasText((CharSequence)codeValue)) {
            throw new IllegalArgumentException("Code value cannot be null or empty");
        }
        if (!StringUtil.hasText((CharSequence)codeMeaning)) {
            codeMeaning = "UNKNOWN";
        }
        if (codingScheme == null) {
            codingScheme = CodingScheme.UNKNOWN;
        }
    }

    public OtherPart(String codeValue, String codeMeaning, CodingScheme codingScheme) {
        this(codeValue, codeMeaning, codingScheme, false);
    }

    @Override
    public String getCodeValue() {
        return this.codeValue;
    }

    @Override
    public String getCodeMeaning() {
        return this.codeMeaning;
    }

    @Override
    public CodingScheme getCodingScheme() {
        return this.codingScheme;
    }

    @Override
    public String getLegacyCode() {
        return null;
    }

    @Override
    public boolean isPaired() {
        return this.paired;
    }

    public boolean hasSameCodingScheme(ItemCode other) {
        return other != null && Objects.equals((Object)this.codingScheme, (Object)other.getCodingScheme());
    }
}

