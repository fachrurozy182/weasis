/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.image;

import org.weasis.core.api.image.ImageOpEvent;

@FunctionalInterface
public interface OpEventListener {
    public void handleImageOpEvent(ImageOpEvent var1);
}

