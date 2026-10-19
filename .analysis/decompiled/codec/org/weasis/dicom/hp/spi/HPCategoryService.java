/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.spi;

public abstract class HPCategoryService {
    protected String categoryName;

    protected HPCategoryService(String category) {
        this.categoryName = category;
    }

    public final String getCategoryName() {
        return this.categoryName;
    }

    public void setProperty(String name, Object value) {
        throw new IllegalArgumentException("Unsupported property: " + name);
    }

    public Object getProperty(String name) {
        throw new IllegalArgumentException("Unsupported property: " + name);
    }
}

