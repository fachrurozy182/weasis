/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Objects;

public interface Payload {
    public long size();

    public InputStream newInputStream();

    public static Payload ofBytes(byte[] data) {
        Objects.requireNonNull(data, "Data cannot be null");
        return new ByteArrayPayload(data);
    }

    public static Payload ofPath(Path path) {
        Objects.requireNonNull(path, "Path cannot be null");
        return new FilePayload(path);
    }

    public static Payload empty() {
        return EmptyPayload.INSTANCE;
    }

    public record ByteArrayPayload(byte[] data) implements Payload
    {
        public ByteArrayPayload {
            Objects.requireNonNull(data, "Data cannot be null");
        }

        @Override
        public long size() {
            return this.data.length;
        }

        @Override
        public InputStream newInputStream() {
            return new ByteArrayInputStream(this.data);
        }
    }

    public record FilePayload(Path path) implements Payload
    {
        public FilePayload {
            Objects.requireNonNull(path, "Path cannot be null");
        }

        @Override
        public long size() {
            try {
                return Files.size(this.path);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public InputStream newInputStream() {
            try {
                return Files.newInputStream(this.path, new OpenOption[0]);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public static enum EmptyPayload implements Payload
    {
        INSTANCE;


        @Override
        public long size() {
            return 0L;
        }

        @Override
        public InputStream newInputStream() {
            return InputStream.nullInputStream();
        }
    }
}

