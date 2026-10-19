/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.dcm4che3.img.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.stream.ImageReaderDescriptor;
import org.weasis.core.util.annotations.Generated;

public interface BytesWithImageDescriptor
extends ImageReaderDescriptor {
    public ByteBuffer getBytes(int var1) throws IOException;

    public String getTransferSyntax();

    @Generated
    default public boolean isBigEndian() {
        return false;
    }

    default public boolean isFloatPixelData() {
        return false;
    }

    public VR getPixelDataVR();
}

