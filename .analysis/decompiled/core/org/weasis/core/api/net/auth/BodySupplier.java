/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  org.jetbrains.annotations.NotNull
 */
package org.weasis.core.api.net.auth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public interface BodySupplier<T> {
    public T get() throws IOException;

    public long length();

    public static BodySupplier<InputStream> ofBytes(byte[] bytes) {
        return new ByteArraySupplier(bytes, 0, bytes.length);
    }

    public static BodySupplier<InputStream> ofBytes(byte[] bytes, int offset, int length) {
        return new ByteArraySupplier(bytes, offset, length);
    }

    public static BodySupplier<InputStream> ofString(String content) {
        return BodySupplier.ofString(content, StandardCharsets.UTF_8);
    }

    public static BodySupplier<InputStream> ofString(String content, Charset charset) {
        return BodySupplier.ofBytes(content.getBytes(charset));
    }

    public static BodySupplier<InputStream> ofPath(Path path) throws IOException {
        return new PathSupplier(path, Files.size(path));
    }

    public static BodySupplier<InputStream> empty() {
        return BodySupplier.ofBytes(new byte[0]);
    }

    public record ByteArraySupplier(byte[] bytes, int offset, int len) implements BodySupplier<InputStream>
    {
        @Override
        public InputStream get() {
            return new ByteArrayInputStream(this.bytes, this.offset, this.len);
        }

        @Override
        public long length() {
            return this.len;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            byte[] otherBytes;
            if (this == o) {
                return true;
            }
            if (!(o instanceof ByteArraySupplier)) return false;
            ByteArraySupplier byteArraySupplier = (ByteArraySupplier)o;
            try {
                byte[] byArray;
                otherBytes = byArray = byteArraySupplier.bytes();
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
            int n = byteArraySupplier.offset();
            if (!true) return false;
            int otherOffset = n;
            n = byteArraySupplier.len();
            if (!true) return false;
            int otherLen = n;
            if (this.offset != otherOffset) return false;
            if (this.len != otherLen) return false;
            if (!Arrays.equals(this.bytes, otherBytes)) return false;
            return true;
        }

        public int hashCode() {
            int result = Arrays.hashCode(this.bytes);
            result = 31 * result + this.offset;
            result = 31 * result + this.len;
            return result;
        }

        @NotNull
        public String toString() {
            return "ByteArraySupplier[bytes=" + Arrays.toString(this.bytes) + ", offset=" + this.offset + ", len=" + this.len + "]";
        }
    }

    public record PathSupplier(Path path, long size) implements BodySupplier<InputStream>
    {
        @Override
        public InputStream get() throws IOException {
            return Files.newInputStream(this.path, new OpenOption[0]);
        }

        @Override
        public long length() {
            return this.size;
        }
    }
}

