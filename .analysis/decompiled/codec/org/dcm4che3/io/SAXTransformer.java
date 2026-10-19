/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.SAXTransformerFactoryHolder;
import org.dcm4che3.io.SAXWriter;
import org.xml.sax.SAXException;

public abstract class SAXTransformer {
    public static SAXWriter getSAXWriter(Templates templates, Attributes result) throws TransformerConfigurationException {
        return SAXTransformer.getSAXWriter(templates, result, null);
    }

    public static SAXWriter getSAXWriter(Templates templates, Attributes result, SetupTransformer setup) throws TransformerConfigurationException {
        return SAXTransformer.getSAXWriter(templates, (Result)new SAXResult(new ContentHandlerAdapter(result)), setup);
    }

    public static SAXWriter getSAXWriter(Templates templates, Result result) throws TransformerConfigurationException {
        return SAXTransformer.getSAXWriter(templates, result, null);
    }

    public static SAXWriter getSAXWriter(Templates templates, Result result, SetupTransformer setup) throws TransformerConfigurationException {
        return SAXTransformer.getSAXWriter(SAXTransformerFactoryHolder.factory.newTransformerHandler(templates), result, setup);
    }

    public static SAXWriter getSAXWriter(Result result) throws TransformerConfigurationException {
        return SAXTransformer.getSAXWriter(result, null);
    }

    public static SAXWriter getSAXWriter(Result result, SetupTransformer setup) throws TransformerConfigurationException {
        return SAXTransformer.getSAXWriter(SAXTransformerFactoryHolder.factory.newTransformerHandler(), result, setup);
    }

    private static SAXWriter getSAXWriter(TransformerHandler th, Result result, SetupTransformer setup) {
        th.setResult(result);
        if (setup != null) {
            setup.setup(th.getTransformer());
        }
        return new SAXWriter(th);
    }

    public static Attributes transform(Attributes ds, Templates templates, boolean includeNameSpaceDeclaration, boolean includeKeword) throws SAXException, TransformerConfigurationException {
        return SAXTransformer.transform(ds, templates, includeNameSpaceDeclaration, includeKeword, null);
    }

    public static Attributes transform(Attributes ds, Templates templates, boolean includeNameSpaceDeclaration, boolean includeKeword, SetupTransformer setup) throws SAXException, TransformerConfigurationException {
        Attributes modify = new Attributes();
        SAXWriter w = SAXTransformer.getSAXWriter(templates, modify, setup);
        w.setIncludeNamespaceDeclaration(includeNameSpaceDeclaration);
        w.setIncludeKeyword(includeKeword);
        w.write(ds);
        return modify;
    }

    public static Templates newTemplates(Source source) throws TransformerConfigurationException {
        return SAXTransformerFactoryHolder.factory.newTemplates(source);
    }

    public static interface SetupTransformer {
        public void setup(Transformer var1);
    }
}

