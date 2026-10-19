/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

public enum ColorSubsampling {
    YBR_XXX_422{

        @Override
        public int frameLength(int w, int h) {
            return w * h * 2;
        }

        @Override
        public int indexOfY(int x, int y, int w) {
            return (w * y + x) * 2 - x % 2;
        }

        @Override
        public int indexOfBR(int x, int y, int w) {
            return w * y * 2 + (x >> 1 << 2) + 2;
        }
    }
    ,
    YBR_XXX_420{

        @Override
        public int frameLength(int w, int h) {
            return w * h / 2 * 3;
        }

        @Override
        public int indexOfY(int x, int y, int w) {
            int withoutBR = y / 2;
            int withBR = y - withoutBR;
            return w * (withBR * 2 + withoutBR) + (y % 2 == 0 ? x * 2 - x % 2 : x);
        }

        @Override
        public int indexOfBR(int x, int y, int w) {
            return w * (y / 2) * 3 + (x >> 1 << 2) + 2;
        }
    };


    public abstract int frameLength(int var1, int var2);

    public abstract int indexOfY(int var1, int var2, int var3);

    public abstract int indexOfBR(int var1, int var2, int var3);
}

