/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.httpclient.multipart.BodyPartPayload
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.httpclient.multipart.BodyPartPayload;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.weasis.core.api.net.auth.BodySupplier;

public class FileBodyPartPayload
extends BodyPartPayload {
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String FORM_DATA = "form-data";
    private final BodySupplier<InputStream> bodySupplier;

    public FileBodyPartPayload(BodySupplier<InputStream> bodySupplier) {
        this(bodySupplier, null);
    }

    public FileBodyPartPayload(BodySupplier<InputStream> bodySupplier, String filename) {
        this(null, bodySupplier, filename);
    }

    public FileBodyPartPayload(String contentType, BodySupplier<InputStream> bodySupplier, String filename) {
        super(FileBodyPartPayload.buildHeaders(contentType, filename));
        this.bodySupplier = bodySupplier;
    }

    public BodySupplier<InputStream> getPayload() {
        return this.bodySupplier;
    }

    private static Map<String, String> buildHeaders(String contentType, String filename) {
        LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>(2);
        if (contentType != null) {
            headers.put("Content-Type", contentType);
        }
        headers.put(CONTENT_DISPOSITION, FileBodyPartPayload.disposition(filename));
        return Map.copyOf(headers);
    }

    private static String disposition(String filename) {
        return filename == null ? FORM_DATA : "form-data; filename=\"" + filename + "\"";
    }
}

