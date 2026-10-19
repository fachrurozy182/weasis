/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.dcm4che3.imageio.codec;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.imageio.codec.XPEGParserException;
import org.dcm4che3.imageio.codec.mp4.MP4FileType;
import org.weasis.core.util.annotations.Generated;

@Generated
public interface XPEGParser {
    public long getCodeStreamPosition();

    public long getPositionAfterAPPSegments();

    public MP4FileType getMP4FileType();

    public Attributes getAttributes(Attributes var1);

    public String getTransferSyntaxUID(boolean var1) throws XPEGParserException;

    default public String getTransferSyntaxUID() throws XPEGParserException {
        return this.getTransferSyntaxUID(false);
    }
}

