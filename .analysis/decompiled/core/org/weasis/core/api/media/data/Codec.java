/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import java.net.URI;
import java.util.Hashtable;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;

public interface Codec<E extends MediaElement> {
    public String getCodecName();

    public String[] getReaderMIMETypes();

    public String[] getReaderExtensions();

    public String[] getWriterMIMETypes();

    public String[] getWriterExtensions();

    public MediaReader<E> getMediaIO(URI var1, String var2, Hashtable<String, Object> var3);

    public boolean isMimeTypeSupported(String var1);
}

