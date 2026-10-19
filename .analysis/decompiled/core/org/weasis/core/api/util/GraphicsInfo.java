/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.util;

import java.util.Optional;

public final class GraphicsInfo {
    private static volatile Gpu gpu;

    private GraphicsInfo() {
    }

    public static void set(String vendor, String renderer, String glVersion, boolean softwareRendered) {
        gpu = new Gpu(vendor == null ? "" : vendor, renderer == null ? "" : renderer, glVersion == null ? "" : glVersion, softwareRendered);
    }

    public static Optional<Gpu> get() {
        return Optional.ofNullable(gpu);
    }

    public record Gpu(String vendor, String renderer, String glVersion, boolean softwareRendered) {
    }
}

