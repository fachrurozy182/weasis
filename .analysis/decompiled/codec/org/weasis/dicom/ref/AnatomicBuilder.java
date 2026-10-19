/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.ref;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.ref.AnatomicItem;
import org.weasis.dicom.ref.AnatomicModifier;
import org.weasis.dicom.ref.BodyPart;
import org.weasis.dicom.ref.MesCategory;
import org.weasis.dicom.ref.SurfacePart;

public final class AnatomicBuilder {
    private static final Map<CategoryBuilder, List<AnatomicItem>> STANDARD_CATEGORIES = AnatomicBuilder.createCategoryMap();
    private static final AtomicReference<Map<CategoryBuilder, List<AnatomicItem>>> EXTENSION_CATEGORIES = new AtomicReference(Map.of());
    private static final Map<String, BodyPart> CODE_TO_BODY_PART = AnatomicBuilder.createBodyPartCodeMap();
    private static final Map<String, BodyPart> LEGACY_CODE_TO_BODY_PART = AnatomicBuilder.createBodyPartLegacyCodeMap();
    private static final Map<String, AnatomicModifier> CODE_TO_ANATOMIC_MODIFIER = AnatomicBuilder.createModifierCodeMap();
    private static final Map<String, SurfacePart> CODE_TO_SURFACE_PART = AnatomicBuilder.createSurfacePartCodeMap();

    private AnatomicBuilder() {
    }

    private static Map<CategoryBuilder, List<AnatomicItem>> createCategoryMap() {
        LinkedHashMap<Category, List<Object>> map = new LinkedHashMap<Category, List<Object>>();
        map.put(Category.SURFACE, List.of(SurfacePart.values()));
        map.put(Category.ALL_REGIONS, List.of(BodyPart.values()));
        map.put(Category.COMMON, AnatomicBuilder.filterBodyParts(BodyPart::isCommon));
        map.put(Category.ENDOSCOPY, AnatomicBuilder.filterBodyParts(BodyPart::isEndoscopic));
        return Collections.unmodifiableMap(map);
    }

    public static Map<CategoryBuilder, List<AnatomicItem>> getCategoryMap() {
        Map<CategoryBuilder, List<AnatomicItem>> extensions = EXTENSION_CATEGORIES.get();
        if (extensions.isEmpty()) {
            return STANDARD_CATEGORIES;
        }
        LinkedHashMap<CategoryBuilder, List<AnatomicItem>> merged = new LinkedHashMap<CategoryBuilder, List<AnatomicItem>>(STANDARD_CATEGORIES);
        merged.putAll(extensions);
        return Collections.unmodifiableMap(merged);
    }

    public static List<AnatomicItem> getCategoryItems(CategoryBuilder category) {
        if (category == null) {
            return List.of();
        }
        List<AnatomicItem> items = STANDARD_CATEGORIES.get(category);
        if (items == null) {
            items = EXTENSION_CATEGORIES.get().get(category);
        }
        return items == null ? List.of() : items;
    }

    public static Optional<CategoryBuilder> getCategoryFromContextUID(String contextUID) {
        CategoryBuilder standard = Category.fromContextUID(contextUID).orElse(null);
        if (standard != null) {
            return Optional.of(standard);
        }
        return EXTENSION_CATEGORIES.get().keySet().stream().filter(c -> c.getContextUID().equals(contextUID)).findFirst();
    }

    public static Optional<ExtendedCategory> getExtendedCategory(String extensionCreatorUID) {
        if (!StringUtil.hasText((CharSequence)extensionCreatorUID)) {
            return Optional.empty();
        }
        return EXTENSION_CATEGORIES.get().keySet().stream().filter(ExtendedCategory.class::isInstance).map(ExtendedCategory.class::cast).filter(c -> extensionCreatorUID.equals(c.getExtensionCreatorUID())).findFirst();
    }

    public static void registerCategory(CategoryBuilder category, List<AnatomicItem> items) {
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(items, "items must not be null");
        List<AnatomicItem> copy = List.copyOf(items);
        if (!(category instanceof ExtendedCategory) && Category.fromContextUID(category.getContextUID()).isPresent()) {
            throw new IllegalArgumentException("Standard categories cannot be overridden: " + category.getContextUID());
        }
        EXTENSION_CATEGORIES.updateAndGet(current -> {
            LinkedHashMap<CategoryBuilder, List> updated = new LinkedHashMap<CategoryBuilder, List>((Map<CategoryBuilder, List>)current);
            updated.remove(category);
            updated.put(category, copy);
            return Collections.unmodifiableMap(updated);
        });
    }

    public static boolean unregisterCategory(CategoryBuilder category) {
        if (category == null) {
            return false;
        }
        Map<CategoryBuilder, List<AnatomicItem>> previous = EXTENSION_CATEGORIES.getAndUpdate(current -> {
            if (!current.containsKey(category)) {
                return current;
            }
            LinkedHashMap updated = new LinkedHashMap(current);
            updated.remove(category);
            return Collections.unmodifiableMap(updated);
        });
        return previous.containsKey(category);
    }

    private static List<AnatomicItem> filterBodyParts(Predicate<BodyPart> filter) {
        return Stream.of(BodyPart.values()).filter(filter).map(AnatomicItem.class::cast).toList();
    }

    private static Map<String, BodyPart> createBodyPartCodeMap() {
        return Stream.of(BodyPart.values()).collect(Collectors.toUnmodifiableMap(BodyPart::getCodeValue, Function.identity()));
    }

    private static Map<String, BodyPart> createBodyPartLegacyCodeMap() {
        return Stream.of(BodyPart.values()).filter(bp -> StringUtil.hasText((CharSequence)bp.getLegacyCode())).collect(Collectors.toUnmodifiableMap(BodyPart::getLegacyCode, Function.identity()));
    }

    private static Map<String, AnatomicModifier> createModifierCodeMap() {
        return Stream.of(AnatomicModifier.values()).collect(Collectors.toUnmodifiableMap(AnatomicModifier::getCodeValue, Function.identity()));
    }

    private static Map<String, SurfacePart> createSurfacePartCodeMap() {
        return Stream.of(SurfacePart.values()).collect(Collectors.toUnmodifiableMap(SurfacePart::getCodeValue, Function.identity()));
    }

    public static List<BodyPart> getAllBodyParts() {
        return List.of(BodyPart.values());
    }

    public static List<BodyPart> getCommonBodyParts() {
        return AnatomicBuilder.getBodyParts(BodyPart::isCommon);
    }

    public static List<BodyPart> getEndoscopicBodyParts() {
        return AnatomicBuilder.getBodyParts(BodyPart::isEndoscopic);
    }

    public static List<BodyPart> getBodyParts(Predicate<BodyPart> filter) {
        return Stream.of(BodyPart.values()).filter(filter).toList();
    }

    public static BodyPart getBodyPartFromCode(String code) {
        return CODE_TO_BODY_PART.get(code);
    }

    public static BodyPart getBodyPartFromLegacyCode(String legacyCode) {
        return StringUtil.hasText((CharSequence)legacyCode) ? LEGACY_CODE_TO_BODY_PART.get(legacyCode.trim()) : null;
    }

    public static BodyPart getBodyPartFromPredicate(Predicate<BodyPart> filter) {
        return Stream.of(BodyPart.values()).filter(filter).findFirst().orElse(null);
    }

    public static BodyPart getBodyPartFromCodeMeaning(String codeMeaning) {
        if (!StringUtil.hasText((CharSequence)codeMeaning)) {
            return null;
        }
        String trimmedMeaning = codeMeaning.trim();
        return AnatomicBuilder.getBodyPartFromPredicate(bp -> Objects.equals(trimmedMeaning, bp.getCodeMeaning()));
    }

    public static AnatomicModifier getAnatomicModifierFromCode(String code) {
        return CODE_TO_ANATOMIC_MODIFIER.get(code);
    }

    public static SurfacePart getSurfacePartFromCode(String code) {
        return CODE_TO_SURFACE_PART.get(code);
    }

    public static List<SurfacePart> getAllSurfaceParts() {
        return List.of(SurfacePart.values());
    }

    public static List<AnatomicModifier> getAllAnatomicModifiers() {
        return List.of(AnatomicModifier.values());
    }

    public static enum Category implements CategoryBuilder
    {
        SURFACE("1.2.840.10008.6.1.1268", "CID 4029"),
        ALL_REGIONS("1.2.840.10008.6.1.2", "CID 4"),
        COMMON("1.2.840.10008.6.1.308", "CID 4031"),
        ENDOSCOPY("1.2.840.10008.6.1.311", "CID 4040");

        private static final Map<String, Category> UID_LOOKUP;
        private final String contextUID;
        private final String identifier;

        private Category(String contextUID, String identifier) {
            this.contextUID = Objects.requireNonNull(contextUID);
            this.identifier = Objects.requireNonNull(identifier);
            OtherCategory.validateIdentifier(identifier);
        }

        @Override
        public String getContextUID() {
            return this.contextUID;
        }

        @Override
        public String getIdentifier() {
            return this.identifier;
        }

        @Override
        public String getTitle() {
            return MesCategory.getString(this.contextUID);
        }

        public String getTitle(Locale locale) {
            return MesCategory.getString(this.contextUID, locale);
        }

        public String toString() {
            return this.getTitle();
        }

        public static Optional<Category> fromContextUID(String uid) {
            return Optional.ofNullable(UID_LOOKUP.get(uid));
        }

        static {
            UID_LOOKUP = Stream.of(Category.values()).collect(Collectors.toUnmodifiableMap(Category::getContextUID, Function.identity()));
        }
    }

    public static interface CategoryBuilder {
        public String getContextUID();

        public String getIdentifier();

        public String getTitle();
    }

    public static final class ExtendedCategory
    implements CategoryBuilder {
        private final Category baseCategory;
        private final String extensionCreatorUID;
        private final String title;

        public ExtendedCategory(Category baseCategory, String extensionCreatorUID, String title) {
            this.baseCategory = Objects.requireNonNull(baseCategory, "baseCategory must not be null");
            this.extensionCreatorUID = Objects.requireNonNull(extensionCreatorUID, "extensionCreatorUID must not be null");
            this.title = Objects.requireNonNull(title, "title must not be null");
        }

        @Override
        public String getContextUID() {
            return this.baseCategory.getContextUID();
        }

        @Override
        public String getIdentifier() {
            return this.baseCategory.getIdentifier();
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        public Category getBaseCategory() {
            return this.baseCategory;
        }

        public String getExtensionCreatorUID() {
            return this.extensionCreatorUID;
        }

        public String toString() {
            return this.title;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object obj) {
            if (!(obj instanceof ExtendedCategory)) return false;
            ExtendedCategory other = (ExtendedCategory)obj;
            if (this.baseCategory != other.baseCategory) return false;
            if (!this.extensionCreatorUID.equals(other.extensionCreatorUID)) return false;
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.baseCategory, this.extensionCreatorUID);
        }
    }

    public static final class OtherCategory
    implements CategoryBuilder {
        private static final String VR_CS_PATTERN = "[A-Z0-9 _]*";
        private static final int MAX_IDENTIFIER_LENGTH = 16;
        private final String contextUID;
        private final String identifier;
        private final String title;

        public OtherCategory(String contextUID, String identifier, String title) {
            this.contextUID = Objects.requireNonNull(contextUID, "contextUID must not be null");
            this.identifier = Objects.requireNonNull(identifier, "identifier must not be null");
            this.title = Objects.requireNonNull(title, "title must not be null");
            OtherCategory.validateIdentifier(identifier);
        }

        private static void validateIdentifier(String identifier) {
            if (identifier.length() > 16 || !identifier.matches(VR_CS_PATTERN)) {
                throw new IllegalArgumentException("Identifier must be a valid VR.CS (max 16 characters: uppercase letters, digits, spaces, and underscores)");
            }
        }

        @Override
        public String getContextUID() {
            return this.contextUID;
        }

        @Override
        public String getIdentifier() {
            return this.identifier;
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        public String toString() {
            return this.getTitle();
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object obj) {
            if (!(obj instanceof OtherCategory)) return false;
            OtherCategory other = (OtherCategory)obj;
            if (!Objects.equals(this.contextUID, other.contextUID)) return false;
            return true;
        }

        public int hashCode() {
            return Objects.hashCode(this.contextUID);
        }
    }
}

