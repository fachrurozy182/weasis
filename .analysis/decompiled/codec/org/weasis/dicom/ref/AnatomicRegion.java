/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.ref;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.macro.Code;
import org.weasis.dicom.macro.ItemCode;
import org.weasis.dicom.ref.AnatomicBuilder;
import org.weasis.dicom.ref.AnatomicItem;
import org.weasis.dicom.ref.AnatomicModifier;
import org.weasis.dicom.ref.BodyPart;
import org.weasis.dicom.ref.CodingScheme;
import org.weasis.dicom.ref.OtherPart;

public class AnatomicRegion {
    private final AnatomicBuilder.CategoryBuilder category;
    private final AnatomicItem region;
    private final Set<AnatomicModifier> modifiers;

    public AnatomicRegion(AnatomicItem region) {
        this(null, region, null);
    }

    public AnatomicRegion(AnatomicBuilder.CategoryBuilder category, AnatomicItem region, Set<AnatomicModifier> modifiers) {
        this.category = category;
        this.region = Objects.requireNonNull(region);
        this.modifiers = modifiers == null ? new HashSet() : modifiers;
    }

    public static AnatomicRegion read(Attributes dcm) {
        if (dcm == null) {
            return null;
        }
        return AnatomicRegion.readFromRegionSequence(dcm).or(() -> AnatomicRegion.readFromLegacyBodyPart(dcm)).orElse(null);
    }

    public static void write(Attributes dcm, AnatomicRegion region) {
        if (dcm == null || region == null) {
            return;
        }
        Attributes regAttributes = new Attributes();
        AnatomicItem anatomicItem = region.getRegion();
        Code code = new Code(regAttributes);
        AnatomicRegion.writeCode(code, anatomicItem);
        AnatomicRegion.writeRegionContext(code, region.getCategory(), anatomicItem);
        AnatomicRegion.writeLegacyBodyPart(dcm, anatomicItem);
        AnatomicRegion.writeModifiers(regAttributes, region.getModifiers());
        dcm.newSequence(533016, 1).add(regAttributes);
    }

    private static Optional<AnatomicRegion> readFromRegionSequence(Attributes dcm) {
        Attributes regionAttributes = dcm.getNestedDataset(533016);
        if (regionAttributes == null) {
            return Optional.empty();
        }
        Code code = new Code(regionAttributes);
        String codeValue = code.getExistingCodeValue();
        if (!StringUtil.hasText((CharSequence)codeValue)) {
            return Optional.empty();
        }
        AnatomicBuilder.CategoryBuilder category = AnatomicRegion.findOrCreateCategory(code);
        AnatomicItem item = AnatomicRegion.findAnatomicItem(code, dcm, regionAttributes, category);
        AnatomicRegion region = new AnatomicRegion(category, item, null);
        AnatomicRegion.addModifiers(regionAttributes, region);
        return Optional.of(region);
    }

    private static Optional<AnatomicRegion> readFromLegacyBodyPart(Attributes dcm) {
        String bodyPart = dcm.getString(1572885);
        if (!StringUtil.hasText((CharSequence)bodyPart)) {
            return Optional.empty();
        }
        BodyPart item = AnatomicBuilder.getBodyPartFromLegacyCode(bodyPart);
        return Optional.ofNullable(item).map(AnatomicRegion::new);
    }

    private static AnatomicItem findAnatomicItem(Code code, Attributes dcm, Attributes regionAttributes, AnatomicBuilder.CategoryBuilder category) {
        String codeValue = code.getExistingCodeValue();
        AnatomicItem item = AnatomicRegion.findExtensionItem(category, code);
        if (item != null) {
            return item;
        }
        item = AnatomicBuilder.getBodyPartFromCode(codeValue);
        if (item != null) {
            return item;
        }
        item = AnatomicBuilder.getSurfacePartFromCode(codeValue);
        if (item != null) {
            return item;
        }
        boolean paired = AnatomicRegion.isPaired(dcm, regionAttributes);
        return new OtherPart(codeValue, code.getCodeMeaning(), code.getCodingScheme(), paired);
    }

    private static AnatomicItem findExtensionItem(AnatomicBuilder.CategoryBuilder category, Code code) {
        if (!(category instanceof AnatomicBuilder.ExtendedCategory)) {
            return null;
        }
        String codeValue = code.getExistingCodeValue();
        CodingScheme scheme = code.getCodingScheme();
        return AnatomicBuilder.getCategoryItems(category).stream().filter(i -> i.getCodingScheme() == scheme && i.getCodeValue().equals(codeValue)).findFirst().orElse(null);
    }

    private static AnatomicBuilder.CategoryBuilder findOrCreateCategory(Code code) {
        AnatomicBuilder.ExtendedCategory extended = AnatomicBuilder.getExtendedCategory(code.getContextGroupExtensionCreatorUID()).orElse(null);
        if (extended != null) {
            return extended;
        }
        String contextUID = code.getContextUID();
        if (!StringUtil.hasText((CharSequence)contextUID)) {
            return null;
        }
        return AnatomicBuilder.getCategoryFromContextUID(contextUID).orElseGet(() -> new AnatomicBuilder.OtherCategory(contextUID, code.getContextIdentifier(), code.getContextIdentifier()));
    }

    private static void writeLegacyBodyPart(Attributes dcm, AnatomicItem anatomicItem) {
        String legacyCode = anatomicItem.getLegacyCode();
        if (StringUtil.hasText((CharSequence)legacyCode)) {
            dcm.setString(1572885, VR.CS, legacyCode);
        }
    }

    private static void writeModifiers(Attributes regAttributes, Set<AnatomicModifier> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            return;
        }
        Sequence seq = regAttributes.newSequence(533024, modifiers.size());
        for (AnatomicModifier modifier : modifiers) {
            Attributes mod = new Attributes();
            Code mcode = new Code(mod);
            AnatomicRegion.writeCode(mcode, modifier);
            seq.add(mod);
        }
    }

    private static void writeRegionContext(Code code, AnatomicBuilder.CategoryBuilder category, AnatomicItem anatomicItem) {
        if (category == null) {
            return;
        }
        code.setContextUID(category.getContextUID());
        code.setContextIdentifier(category.getIdentifier());
        if (category instanceof AnatomicBuilder.ExtendedCategory) {
            AnatomicBuilder.ExtendedCategory extended = (AnatomicBuilder.ExtendedCategory)category;
            if (anatomicItem.isContextGroupExtension()) {
                code.setContextGroupExtensionFlag("Y");
                code.setContextGroupExtensionCreatorUID(extended.getExtensionCreatorUID());
            }
        }
    }

    private static void writeCode(Code code, ItemCode anatomicCode) {
        code.setCodingScheme(anatomicCode.getCodingScheme());
        code.setCodeValue(anatomicCode.getCodeValue());
        code.setCodeMeaning(anatomicCode.getCodeMeaning());
    }

    private static void addModifiers(Attributes regionAttributes, AnatomicRegion region) {
        Sequence seq = regionAttributes.getSequence(533024);
        if (seq == null) {
            return;
        }
        for (Attributes attribute : seq) {
            AnatomicModifier modifier = AnatomicModifier.fromCode(new Code(attribute).getExistingCodeValue());
            if (modifier == null) continue;
            region.addModifier(modifier);
        }
    }

    private static boolean isPaired(Attributes dcm, Attributes regionAttributes) {
        String laterality = dcm.getString(2134130, dcm.getString(0x200062));
        if (StringUtil.hasText((CharSequence)laterality) && !"U".equals(laterality)) {
            return true;
        }
        Sequence seq = regionAttributes.getSequence(533024);
        if (seq != null) {
            for (Attributes attribute : seq) {
                AnatomicModifier modifier = AnatomicModifier.fromCode(new Code(attribute).getExistingCodeValue());
                if (modifier != AnatomicModifier.LEFT && modifier != AnatomicModifier.RIGHT && modifier != AnatomicModifier.BILATERAL) continue;
                return true;
            }
        }
        return false;
    }

    public AnatomicItem getRegion() {
        return this.region;
    }

    public AnatomicBuilder.CategoryBuilder getCategory() {
        return this.category;
    }

    public Set<AnatomicModifier> getModifiers() {
        return this.modifiers;
    }

    public void addModifier(AnatomicModifier modifier) {
        this.modifiers.add(modifier);
    }

    public void removeModifier(AnatomicModifier modifier) {
        this.modifiers.remove(modifier);
    }

    public String toString() {
        String modifiersList = this.modifiers.stream().map(AnatomicModifier::getCodeMeaning).collect(Collectors.joining(", "));
        return StringUtil.hasText((CharSequence)modifiersList) ? this.region.getCodeMeaning() + " (" + modifiersList + ")" : this.region.getCodeMeaning();
    }
}

