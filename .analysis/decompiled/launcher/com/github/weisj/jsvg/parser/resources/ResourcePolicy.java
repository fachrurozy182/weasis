/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.resources;

import com.github.weisj.jsvg.parser.resources.impl.DefaultResourcePolicy;
import java.net.URI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ResourcePolicy {
    public static final ResourcePolicy DENY_ALL = new DefaultResourcePolicy(0);
    public static final ResourcePolicy DENY_EXTERNAL = new DefaultResourcePolicy(8);
    public static final ResourcePolicy ALLOW_RELATIVE = new DefaultResourcePolicy(9);
    public static final ResourcePolicy ALLOW_ALL = new DefaultResourcePolicy(15);

    @Nullable
    public URI resolveResourceURI(@Nullable URI var1, @NotNull String var2);

    @Nullable
    public URI resolveResourceURI(@Nullable URI var1, @NotNull URI var2);
}

