/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.FilterEncoder;
import org.tukaani.xz.FinishableOutputStream;

public abstract class FilterOptions
implements Cloneable {
    public static int getEncoderMemoryUsage(FilterOptions[] options) {
        int m = 0;
        for (int i = 0; i < options.length; ++i) {
            m += options[i].getEncoderMemoryUsage();
        }
        return m;
    }

    public static int getDecoderMemoryUsage(FilterOptions[] options) {
        int m = 0;
        for (int i = 0; i < options.length; ++i) {
            m += options[i].getDecoderMemoryUsage();
        }
        return m;
    }

    public abstract int getEncoderMemoryUsage();

    public FinishableOutputStream getOutputStream(FinishableOutputStream out) {
        return this.getOutputStream(out, ArrayCache.getDefaultCache());
    }

    public abstract FinishableOutputStream getOutputStream(FinishableOutputStream var1, ArrayCache var2);

    public abstract int getDecoderMemoryUsage();

    public InputStream getInputStream(InputStream in) throws IOException {
        return this.getInputStream(in, ArrayCache.getDefaultCache());
    }

    public abstract InputStream getInputStream(InputStream var1, ArrayCache var2) throws IOException;

    abstract FilterEncoder getFilterEncoder();

    FilterOptions() {
    }
}

