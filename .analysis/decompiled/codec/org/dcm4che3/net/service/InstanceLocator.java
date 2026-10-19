/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

public class InstanceLocator
implements Serializable {
    private static final long serialVersionUID = -5744253220472580137L;
    public final String cuid;
    public final String iuid;
    public final String tsuid;
    public final String uri;
    private Object obj;

    public InstanceLocator(String cuid, String iuid, String tsuid, String uri) {
        this.cuid = cuid;
        this.iuid = iuid;
        this.tsuid = tsuid;
        this.uri = uri;
    }

    public final Object getObject() {
        return this.obj;
    }

    public final InstanceLocator setObject(Object obj) {
        this.obj = obj;
        return this;
    }

    public File getFile() {
        try {
            return new File(new URI(this.uri));
        }
        catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}

