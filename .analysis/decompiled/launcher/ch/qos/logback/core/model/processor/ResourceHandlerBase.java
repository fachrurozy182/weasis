/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.ResourceModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public abstract class ResourceHandlerBase
extends ModelHandlerBase {
    protected String attributeInUse;
    protected boolean optional;

    protected ResourceHandlerBase(Context context) {
        super(context);
    }

    protected InputStream openURL(URL url) {
        try {
            return url.openStream();
        }
        catch (IOException e) {
            this.warnIfRequired("Failed to open [" + url.toString() + "]");
            return null;
        }
    }

    protected boolean checkAttributes(ResourceModel resourceModel) {
        String fileAttribute = resourceModel.getFile();
        String urlAttribute = resourceModel.getUrl();
        String resourceAttribute = resourceModel.getResource();
        int count = 0;
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(fileAttribute)) {
            ++count;
        }
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(urlAttribute)) {
            ++count;
        }
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(resourceAttribute)) {
            ++count;
        }
        if (count == 0) {
            this.addError("One of \"path\", \"resource\" or \"url\" attributes must be set.");
            return false;
        }
        if (count > 1) {
            this.addError("Only one of \"file\", \"url\" or \"resource\" attributes should be set.");
            return false;
        }
        if (count == 1) {
            return true;
        }
        throw new IllegalStateException("Count value [" + count + "] is not expected");
    }

    protected String getAttribureInUse() {
        return this.attributeInUse;
    }

    protected URL getInputURL(ContextAwarePropertyContainer contextAwarePropertyContainer, ResourceModel resourceModel) {
        String fileAttribute = resourceModel.getFile();
        String urlAttribute = resourceModel.getUrl();
        String resourceAttribute = resourceModel.getResource();
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(fileAttribute)) {
            this.attributeInUse = contextAwarePropertyContainer.subst(fileAttribute);
            return this.filePathAsURL(this.attributeInUse);
        }
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(urlAttribute)) {
            this.attributeInUse = contextAwarePropertyContainer.subst(urlAttribute);
            return this.attributeToURL(this.attributeInUse);
        }
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(resourceAttribute)) {
            this.attributeInUse = contextAwarePropertyContainer.subst(resourceAttribute);
            return this.resourceAsURL(this.attributeInUse);
        }
        throw new IllegalStateException("A URL stream should have been returned at this stage");
    }

    protected URL filePathAsURL(String path) {
        URI uri = new File(path).toURI();
        try {
            return uri.toURL();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected URL attributeToURL(String urlAttribute) {
        try {
            return new URL(urlAttribute);
        }
        catch (MalformedURLException mue) {
            String errMsg = "URL [" + urlAttribute + "] is not well formed.";
            this.addError(errMsg, mue);
            return null;
        }
    }

    protected URL resourceAsURL(String resourceAttribute) {
        URL url = Loader.getResourceBySelfClassLoader(resourceAttribute);
        if (url == null) {
            this.warnIfRequired("Could not find resource corresponding to [" + resourceAttribute + "]");
            return null;
        }
        return url;
    }

    protected void warnIfRequired(String msg) {
        if (!this.optional) {
            this.addWarn(msg);
        }
    }
}

