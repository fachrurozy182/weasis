/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.pref;

import java.util.Objects;
import org.weasis.launcher.Utils;

public class Preference {
    private final String code;
    private final String type;
    private final String javaType;
    private final String category;
    private String value;
    private String defaultValue;
    private String description;

    public Preference(String code, String type, String javaType, String category) {
        if (!Utils.hasText(code)) {
            throw new IllegalArgumentException("Invalid code");
        }
        this.code = code;
        this.type = type;
        this.javaType = javaType;
        this.category = category;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = "null".equals(value) ? null : value;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = "null".equals(defaultValue) ? null : defaultValue;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return this.type;
    }

    public String getCategory() {
        return this.category;
    }

    public String getJavaType() {
        return this.javaType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Preference that = (Preference)o;
        return Objects.equals(this.code, that.code);
    }

    public int hashCode() {
        return Objects.hash(this.code);
    }
}

