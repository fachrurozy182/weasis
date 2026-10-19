/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.ref;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.weasis.dicom.macro.ItemCode;
import org.weasis.dicom.ref.CodingScheme;
import org.weasis.dicom.ref.MesModifier;

public enum AnatomicModifier implements ItemCode
{
    RIGHT(CodingScheme.SCT, 24028007),
    LEFT(CodingScheme.SCT, 7771000),
    BILATERAL(CodingScheme.SCT, 51440002),
    UNILATERAL(CodingScheme.SCT, 66459002),
    LATERAL(CodingScheme.SCT, 49370004),
    MEDIAN(CodingScheme.DCM, 130290),
    ANTERIOR(CodingScheme.SCT, 255549009),
    POSTERIOR(CodingScheme.SCT, 255551008),
    CEPHALIC(CodingScheme.SCT, 66787007),
    CAUDAL(CodingScheme.SCT, 3583002),
    MEDIAL(CodingScheme.SCT, 255561001),
    CENTRAL(CodingScheme.SCT, 26216008),
    PERIPHERAL(CodingScheme.SCT, 14414005),
    EXTERNAL(CodingScheme.SCT, 261074009),
    INTERNAL(CodingScheme.SCT, 260521003),
    INTERMEDIATE(CodingScheme.SCT, 11896004),
    INFERIOR(CodingScheme.SCT, 261089000),
    SUPERIOR(CodingScheme.SCT, 264217000),
    TRANSVERSE(CodingScheme.SCT, 62824007),
    PROXIMAL(CodingScheme.SCT, 40415009),
    DISTAL(CodingScheme.SCT, 46053002),
    POSTAXIAL(CodingScheme.SCT, 60583000),
    PREAXIAL(CodingScheme.SCT, 32400000),
    APICAL(CodingScheme.SCT, 43674008),
    BASAL(CodingScheme.SCT, 57195005),
    AFFERENT(CodingScheme.SCT, 49530007),
    EFFERENT(CodingScheme.SCT, 33843005),
    CORONAL(CodingScheme.SCT, 81654009),
    SUPERFICIAL(CodingScheme.SCT, 26283006),
    DEEP(CodingScheme.SCT, 795002),
    HORIZONTAL(CodingScheme.SCT, 24020000),
    LONGITUDINAL(CodingScheme.SCT, 38717003),
    VERTICAL(CodingScheme.SCT, 33096000),
    SAGITTAL(CodingScheme.SCT, 30730003),
    AXIAL(CodingScheme.SCT, 24422004),
    EXTRA_ARTICULAR(CodingScheme.SCT, 87687004),
    SURFACE(CodingScheme.SCT, 410679008),
    GUTTER(CodingScheme.SCT, 68493006),
    HILAR(CodingScheme.SCT, 32381004),
    CAPSULAR(CodingScheme.SCT, 11070000),
    SUBCAPSULAR(CodingScheme.SCT, 61397002),
    EDGE(CodingScheme.SCT, 57183005),
    ANTEROLATERAL(CodingScheme.SCT, 37197008),
    POSTEROLATERAL(CodingScheme.SCT, 90069004),
    INTRA_ARTICULAR(CodingScheme.SCT, 131183008),
    MARGINAL(CodingScheme.SCT, 112233002);

    private static final Map<String, AnatomicModifier> CODE_LOOKUP;
    private final CodingScheme scheme;
    private final String codeValue;

    private AnatomicModifier(CodingScheme scheme, int codeValue) {
        this.scheme = scheme;
        this.codeValue = String.valueOf(codeValue);
    }

    @Override
    public String getCodeValue() {
        return this.codeValue;
    }

    @Override
    public String getCodeMeaning() {
        return MesModifier.getString(this.codeValue);
    }

    public String getCodeMeaning(Locale locale) {
        return MesModifier.getString(this.codeValue, locale);
    }

    @Override
    public CodingScheme getCodingScheme() {
        return this.scheme;
    }

    public String toString() {
        return this.getCodeMeaning();
    }

    public static AnatomicModifier fromCode(String code) {
        return CODE_LOOKUP.get(code);
    }

    static {
        CODE_LOOKUP = Stream.of(AnatomicModifier.values()).collect(Collectors.toUnmodifiableMap(AnatomicModifier::getCodeValue, Function.identity()));
    }
}

