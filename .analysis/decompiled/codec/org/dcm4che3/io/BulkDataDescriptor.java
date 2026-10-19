/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.util.List;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.BasicBulkDataDescriptor;

public interface BulkDataDescriptor {
    public static final BulkDataDescriptor DEFAULT = new BulkDataDescriptor(){

        @Override
        public boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length) {
            return BasicBulkDataDescriptor.isStandardBulkData(itemPointer, tag);
        }
    };
    public static final BulkDataDescriptor PIXELDATA = new BulkDataDescriptor(){

        @Override
        public boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length) {
            return tag == 2145386512;
        }
    };

    public boolean isBulkData(List<ItemPointer> var1, String var2, int var3, VR var4, int var5);
}

