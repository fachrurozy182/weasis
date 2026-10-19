/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.dcm4che3.img.stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.stream.ImageReaderDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;

public class DicomFileInputStream
extends DicomInputStream
implements ImageReaderDescriptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomFileInputStream.class);
    private final Path path;
    private final AtomicReference<DicomMetaData> metadata = new AtomicReference();

    public DicomFileInputStream(Path path) throws IOException {
        this(path, null);
    }

    public DicomFileInputStream(Path path, DicomMetaData metadata) throws IOException {
        super(Files.newInputStream(Objects.requireNonNull(path, "Path cannot be null"), new OpenOption[0]));
        this.path = path;
        this.metadata.set(metadata);
    }

    public DicomFileInputStream(String pathString) throws IOException {
        this(DicomFileInputStream.createPath(pathString));
    }

    public Path getPath() {
        return this.path;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DicomMetaData getMetadata() throws IOException {
        DicomMetaData result = this.metadata.get();
        if (result == null) {
            DicomFileInputStream dicomFileInputStream = this;
            synchronized (dicomFileInputStream) {
                result = this.metadata.get();
                if (result == null) {
                    result = new DicomMetaData(this);
                    this.metadata.set(result);
                }
            }
        }
        return result;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        try {
            return this.getMetadata().getImageDescriptor();
        }
        catch (IOException e) {
            LOGGER.error("Error reading image descriptor from path: {}", (Object)this.path, (Object)e);
            return null;
        }
    }

    private static Path createPath(String pathString) {
        if (!StringUtil.hasText((CharSequence)pathString)) {
            throw new IllegalArgumentException("Path string cannot be null or empty");
        }
        return Path.of(pathString, new String[0]);
    }

    public String toString() {
        return "DicomFileInputStream{path=" + String.valueOf(this.path) + "}";
    }
}

