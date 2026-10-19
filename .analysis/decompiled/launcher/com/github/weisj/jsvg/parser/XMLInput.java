/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import org.jetbrains.annotations.NotNull;

public interface XMLInput {
    @NotNull
    public XMLEventReader createReader() throws XMLStreamException;
}

