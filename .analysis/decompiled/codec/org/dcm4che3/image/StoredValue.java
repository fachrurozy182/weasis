/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;

public abstract class StoredValue {
    public abstract int valueOf(int var1);

    public abstract int minValue();

    public abstract int maxValue();

    public static StoredValue valueOf(Attributes attrs) {
        int bitsStored = attrs.getInt(2621697, 0);
        if (bitsStored == 0) {
            bitsStored = attrs.getInt(2621696, 8);
        }
        return attrs.getInt(2621699, 0) != 0 ? new Signed(bitsStored) : new Unsigned(bitsStored);
    }

    public static class Signed
    extends StoredValue {
        private final int bitsStored;
        private final int shift;

        public Signed(int bitsStored) {
            this.bitsStored = bitsStored;
            this.shift = 32 - bitsStored;
        }

        @Override
        public int valueOf(int pixel) {
            return pixel << this.shift >> this.shift;
        }

        @Override
        public int minValue() {
            return -(1 << this.bitsStored - 1);
        }

        @Override
        public int maxValue() {
            return (1 << this.bitsStored - 1) - 1;
        }
    }

    public static class Unsigned
    extends StoredValue {
        private final int mask;

        public Unsigned(int bitsStored) {
            this.mask = (1 << bitsStored) - 1;
        }

        @Override
        public int valueOf(int pixel) {
            return pixel & this.mask;
        }

        @Override
        public int minValue() {
            return 0;
        }

        @Override
        public int maxValue() {
            return this.mask;
        }
    }
}

