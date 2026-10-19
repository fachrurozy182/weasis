/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.datetime.util;

public interface InputValidationListener<T> {
    public boolean isValidation();

    public void inputChanged(boolean var1);

    public boolean checkSelectionAble(T var1);
}

