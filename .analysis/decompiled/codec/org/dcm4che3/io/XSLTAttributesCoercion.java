/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import javax.xml.transform.Templates;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.AttributesCoercion;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXTransformer;

public class XSLTAttributesCoercion
implements AttributesCoercion {
    private final Templates templates;
    private boolean includeNameSpaceDeclaration;
    private boolean includeKeyword;
    private SAXTransformer.SetupTransformer setupTransformer;
    private AttributesCoercion next;

    public XSLTAttributesCoercion(Templates templates, AttributesCoercion next) {
        this.templates = templates;
        this.next = next;
    }

    @Override
    public String remapUID(String uid) {
        return this.next != null ? this.next.remapUID(uid) : uid;
    }

    public boolean isIncludeNameSpaceDeclaration() {
        return this.includeNameSpaceDeclaration;
    }

    public void setIncludeNameSpaceDeclaration(boolean includeNameSpaceDeclaration) {
        this.includeNameSpaceDeclaration = includeNameSpaceDeclaration;
    }

    public XSLTAttributesCoercion includeNameSpaceDeclaration(boolean includeNameSpaceDeclaration) {
        this.setIncludeNameSpaceDeclaration(includeNameSpaceDeclaration);
        return this;
    }

    public boolean isIncludeKeyword() {
        return this.includeKeyword;
    }

    public void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    public XSLTAttributesCoercion includeKeyword(boolean includeKeyword) {
        this.setIncludeKeyword(includeKeyword);
        return this;
    }

    public SAXTransformer.SetupTransformer getSetupTransformer() {
        return this.setupTransformer;
    }

    public void setSetupTransformer(SAXTransformer.SetupTransformer setupTransformer) {
        this.setupTransformer = setupTransformer;
    }

    public XSLTAttributesCoercion setupTransformer(SAXTransformer.SetupTransformer setupTransformer) {
        this.setSetupTransformer(setupTransformer);
        return this;
    }

    @Override
    public void coerce(Attributes attrs, Attributes modified) throws Exception {
        Attributes newAttrs;
        try {
            newAttrs = SAXTransformer.transform(attrs, this.templates, this.includeNameSpaceDeclaration, this.includeKeyword, this.setupTransformer);
            String[] cscodes = attrs.getStrings(524293);
            if (cscodes != null) {
                newAttrs.setString(524293, VR.CS, cscodes);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (modified != null) {
            attrs.update(Attributes.UpdatePolicy.OVERWRITE, newAttrs, modified);
        } else {
            attrs.addAll(newAttrs);
        }
        if (this.next != null) {
            this.next.coerce(attrs, modified);
        }
    }
}

