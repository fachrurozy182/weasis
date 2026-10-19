/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import com.github.weisj.jsvg.parser.DomDocument;
import com.github.weisj.jsvg.parser.impl.DefaultElementLoader;
import com.github.weisj.jsvg.parser.impl.ParsedDocument;
import com.github.weisj.jsvg.parser.impl.SVGDocumentBuilder;
import com.github.weisj.jsvg.parser.impl.StaxSVGLoader;
import com.github.weisj.jsvg.parser.impl.StreamUtil;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ExternalDocumentLoader
implements DefaultElementLoader.DocumentLoader {
    private static final Logger LOGGER = LogFactory.createLogger(ExternalDocumentLoader.class);
    @NotNull
    private final Map<URI, CachedDocument> cache = new HashMap<URI, CachedDocument>();

    ExternalDocumentLoader() {
    }

    @Override
    @Nullable
    public DomDocument resolveDocument(@NotNull DomDocument document, @NotNull String name) {
        if (name.isEmpty()) {
            return document;
        }
        return this.locateDocument(document, name);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    private DomDocument locateDocument(@NotNull DomDocument document, @NotNull String name) {
        URI documentUri = document.loaderContext().externalResourcePolicy().resolveResourceURI(document.rootURI(), name);
        if (documentUri == null) {
            return null;
        }
        try {
            Object cachedDocument2;
            URL documentUrl = documentUri.toURL();
            Map<URI, CachedDocument> map = this.cache;
            synchronized (map) {
                cachedDocument2 = this.cache.get(documentUri);
                if (cachedDocument2 != null) {
                    ParsedDocument cached = ((CachedDocument)cachedDocument2).document;
                    if (cached != null) return cached;
                    throw new IllegalStateException("Reference cycle containing external document: " + documentUri);
                }
            }
            CachedDocument cachedDocument = new CachedDocument();
            cachedDocument2 = this.cache;
            synchronized (cachedDocument2) {
                this.cache.put(documentUri, cachedDocument);
            }
            try (InputStream is = StreamUtil.createDocumentInputStream(documentUrl.openStream());){
                SVGDocumentBuilder builder = new StaxSVGLoader().parse(is, documentUri, document.loaderContext());
                if (builder == null) {
                    DomDocument domDocument = null;
                    return domDocument;
                }
                builder.preProcess();
                ParsedDocument parsedDocument = builder.parsedDocument();
                Object object = this.cache;
                synchronized (object) {
                    cachedDocument.document = parsedDocument;
                }
                object = parsedDocument;
                return object;
            }
        }
        catch (Exception e) {
            LOGGER.log(Logger.Level.WARNING, String.format("Failed to load external document: %s from %s - %s", name, documentUri, e.getMessage()));
            return null;
        }
    }

    private static final class CachedDocument {
        @Nullable
        private ParsedDocument document;

        private CachedDocument() {
        }
    }
}

