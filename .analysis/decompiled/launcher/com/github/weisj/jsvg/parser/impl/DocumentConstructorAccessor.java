/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.nodes.SVG;
import org.jetbrains.annotations.NotNull;

public class DocumentConstructorAccessor {
    private static DocumentConstructor documentConstructor;

    public static void setDocumentConstructor(@NotNull DocumentConstructor constructor) {
        if (documentConstructor != null) {
            throw new IllegalStateException("Document constructor already set");
        }
        documentConstructor = constructor;
    }

    static DocumentConstructor constructor() {
        if (documentConstructor == null) {
            throw new IllegalStateException("Document constructor not set");
        }
        return documentConstructor;
    }

    static {
        try {
            Class.forName(SVGDocument.class.getName());
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static interface DocumentConstructor {
        @NotNull
        public SVGDocument create(@NotNull SVG var1);
    }
}

