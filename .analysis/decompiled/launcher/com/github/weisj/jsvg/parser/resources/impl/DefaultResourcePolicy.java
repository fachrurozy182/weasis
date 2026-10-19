/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.resources.impl;

import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import com.github.weisj.jsvg.parser.resources.ResourcePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultResourcePolicy
implements ResourcePolicy {
    private static final Logger LOGGER = LogFactory.createLogger(DefaultResourcePolicy.class);
    public static final int FLAG_ALLOW_RELATIVE = 1;
    public static final int FLAG_ALLOW_ABSOLUTE = 2;
    public static final int FLAG_ALLOW_NON_LOCAL = 4;
    public static final int FLAG_ALLOW_EMBEDDED_DATA = 8;
    private final int flags;

    public DefaultResourcePolicy(int flags) {
        this.flags = flags;
    }

    public boolean allowsExternalResources() {
        return (this.flags & 1) != 0 || (this.flags & 2) != 0;
    }

    @Override
    @Nullable
    public URI resolveResourceURI(@Nullable URI baseURI, @NotNull String path) {
        try {
            return this.resolveResourceURI(baseURI, new URI(DefaultResourcePolicy.cleanup(path)));
        }
        catch (URISyntaxException e) {
            LOGGER.log(Logger.Level.INFO, "Failed to resolve URI: " + path);
            return null;
        }
    }

    @NotNull
    private static String cleanup(@NotNull String path) {
        if (path.startsWith("data")) {
            return path.replace(" ", "");
        }
        return path;
    }

    @Override
    @Nullable
    public URI resolveResourceURI(@Nullable URI baseDocumentUri, @NotNull URI resourceUri) {
        if ("data".equals(resourceUri.getScheme())) {
            if ((this.flags & 8) == 0) {
                LOGGER.log(Logger.Level.INFO, () -> String.format("Rejected URI %s because embedded data is not allowed", resourceUri));
                return null;
            }
            return resourceUri;
        }
        if (resourceUri.isAbsolute()) {
            if ((this.flags & 2) == 0) {
                LOGGER.log(Logger.Level.INFO, () -> String.format("Rejected URI %s because absolute paths are not allowed", resourceUri));
                return null;
            }
            if (!"file".equals(resourceUri.getScheme()) && (this.flags & 4) == 0) {
                LOGGER.log(Logger.Level.INFO, () -> String.format("Rejected URI %s because non-local paths are not allowed", resourceUri));
                return null;
            }
            return resourceUri;
        }
        if (baseDocumentUri == null) {
            return null;
        }
        return baseDocumentUri.resolve(resourceUri);
    }
}

