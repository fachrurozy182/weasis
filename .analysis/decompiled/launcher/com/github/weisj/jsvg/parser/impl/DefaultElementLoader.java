/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.parser.DomDocument;
import com.github.weisj.jsvg.parser.ElementLoader;
import com.github.weisj.jsvg.parser.impl.ExternalDocumentLoader;
import com.github.weisj.jsvg.parser.impl.Url;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class DefaultElementLoader
implements ElementLoader {
    private static final DocumentLoader DEFAULT_DOCUMENT_LOADER = new DefaultDocumentLoader();
    private final DocumentLoader documentLoader;

    DefaultElementLoader(AllowExternalResources allowExternalResources) {
        this.documentLoader = DefaultElementLoader.createDocumentLoader(allowExternalResources);
    }

    @NotNull
    private static DocumentLoader createDocumentLoader(AllowExternalResources allowExternalResources) {
        if (allowExternalResources == AllowExternalResources.DENY) {
            return DEFAULT_DOCUMENT_LOADER;
        }
        return new ExternalDocumentLoader();
    }

    @Override
    @Nullable
    public <T> T loadElement(@NotNull Class<T> type, @Nullable String value, @NotNull DomDocument document) {
        Url url = Url.parse(value, Url.RequireFragment.YES);
        if (url == null) {
            return null;
        }
        DomDocument resolutionDocument = document;
        if (url.url() != null && (resolutionDocument = this.documentLoader.resolveDocument(document, url.url())) == null) {
            return null;
        }
        return resolutionDocument.getElementById(type, url.fragment());
    }

    static enum AllowExternalResources {
        DENY,
        ALLOW;

    }

    static interface DocumentLoader {
        @Nullable
        public DomDocument resolveDocument(@NotNull DomDocument var1, @NotNull String var2);
    }

    private static class DefaultDocumentLoader
    implements DocumentLoader {
        private DefaultDocumentLoader() {
        }

        @Override
        @Nullable
        public DomDocument resolveDocument(@NotNull DomDocument document, @NotNull String name) {
            if (name.isEmpty()) {
                return document;
            }
            return null;
        }
    }
}

