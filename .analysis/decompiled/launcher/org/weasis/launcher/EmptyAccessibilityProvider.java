/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import javax.accessibility.AccessibilityProvider;

public class EmptyAccessibilityProvider
extends AccessibilityProvider {
    @Override
    public String getName() {
        return "EmptyAccessibilityProvider";
    }

    @Override
    public void activate() {
    }
}

