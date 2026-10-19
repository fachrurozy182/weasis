/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 */
package org.weasis.core.api.media.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.util.FileUtil;

public class FileCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCache.class);
    private final MediaReader reader;
    private Path originalTempFile;
    private Path transformedFile;
    private boolean requireTransformation;

    public FileCache(MediaReader reader) {
        this.reader = Objects.requireNonNull(reader);
        this.requireTransformation = false;
    }

    public boolean isLocalFile() {
        return this.reader.getUri().getScheme().startsWith("file");
    }

    public boolean isElementInMemory() {
        return this.reader.getUri().getScheme().startsWith("data");
    }

    public Optional<Path> getOriginalFile() {
        Path originalFile = null;
        if (this.originalTempFile != null) {
            originalFile = this.originalTempFile;
        } else if (this.isLocalFile()) {
            try {
                originalFile = Paths.get(this.reader.getUri());
            }
            catch (Exception e) {
                LOGGER.error("Cannot convert uri to file: {}", (Object)this.reader.getUri(), (Object)e);
            }
        }
        return Optional.ofNullable(originalFile);
    }

    public Path getFinalFile() {
        if (this.transformedFile != null) {
            return this.transformedFile;
        }
        return this.getOriginalFile().orElse(null);
    }

    public synchronized Path getOriginalTempFile() {
        return this.originalTempFile;
    }

    public synchronized void setOriginalTempFile(Path downloadedFile) {
        this.originalTempFile = downloadedFile;
    }

    public synchronized Path getTransformedFile() {
        return this.transformedFile;
    }

    public synchronized void setTransformedFile(Path transformedFile) {
        this.transformedFile = transformedFile;
    }

    public synchronized boolean isRequireTransformation() {
        return this.requireTransformation;
    }

    public synchronized void setRequireTransformation(boolean requireTransformation) {
        this.requireTransformation = requireTransformation;
    }

    public long getLength() {
        Optional<Path> f = this.getOriginalFile();
        return f.map(path -> {
            try {
                return Files.size(path);
            }
            catch (IOException e) {
                LOGGER.error("Cannot get file size: {}", path, (Object)e);
                return 0L;
            }
        }).orElse(0L);
    }

    public FileTime getLastModified() {
        Optional<Path> f = this.getOriginalFile();
        return f.map(path -> {
            try {
                return Files.getLastModifiedTime(path, new LinkOption[0]);
            }
            catch (IOException e) {
                LOGGER.error("Cannot get last modified time: {}", path, (Object)e);
                return null;
            }
        }).orElse(FileTime.fromMillis(0L));
    }

    public void dispose() {
        FileUtil.delete((Path)this.originalTempFile);
        FileUtil.delete((Path)this.transformedFile);
    }
}

