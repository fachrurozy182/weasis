/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.IOException;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;

public interface Value {
    public static final Value NULL = new Value(){

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
            return vr == VR.SQ && encOpts.undefEmptySequenceLength ? -1 : 0;
        }

        @Override
        public void writeTo(DicomOutputStream dos, VR vr) throws IOException {
        }

        @Override
        public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
            return vr == VR.SQ && encOpts.undefEmptySequenceLength ? 8 : 0;
        }

        public String toString() {
            return "";
        }

        @Override
        public byte[] toBytes(VR vr, boolean bigEndian) {
            return ByteUtils.EMPTY_BYTES;
        }
    };

    public boolean isEmpty();

    public byte[] toBytes(VR var1, boolean var2) throws IOException;

    public void writeTo(DicomOutputStream var1, VR var2) throws IOException;

    public int calcLength(DicomEncodingOptions var1, boolean var2, VR var3);

    public int getEncodedLength(DicomEncodingOptions var1, boolean var2, VR var3);
}

