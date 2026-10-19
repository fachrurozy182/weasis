/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.core.api.media.data;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.api.media.data.FileCache;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.core.util.StreamUtil;

public class MediaElement
implements Taggable {
    protected final Map<TagW, Object> tags;
    protected final MediaReader<? extends MediaElement> mediaIO;
    protected final Object key;
    private volatile boolean loading = false;

    public <E extends MediaElement> MediaElement(MediaReader<E> mediaIO, Object key) {
        this.mediaIO = Objects.requireNonNull(mediaIO);
        this.key = key;
        this.tags = Optional.ofNullable(mediaIO.getMediaFragmentTags(key)).orElseGet(HashMap::new);
    }

    public MediaReader<? extends MediaElement> getMediaReader() {
        return this.mediaIO;
    }

    @Override
    public void setTag(TagW tag, Object value) {
        if (tag != null) {
            this.tags.put(tag, value);
        }
    }

    @Override
    public boolean containTagKey(TagW tag) {
        return this.tags.containsKey(tag);
    }

    @Override
    public Object getTagValue(TagW tag) {
        return tag == null ? null : this.tags.get(tag);
    }

    public TagW getTagElement(int id) {
        for (TagW e : this.tags.keySet()) {
            if (e.getId() != id) continue;
            return e;
        }
        return null;
    }

    @Override
    public void setTagNoNull(TagW tag, Object value) {
        if (value != null) {
            this.setTag(tag, value);
        }
    }

    @Override
    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator() {
        return this.tags.entrySet().iterator();
    }

    public void clearAllTags() {
        this.tags.clear();
    }

    public void dispose() {
        this.mediaIO.close();
        this.mediaIO.getFileCache().dispose();
    }

    public URI getMediaURI() {
        return this.mediaIO.getUri();
    }

    public Path getFilePath() {
        return this.mediaIO.getFileCache().getFinalFile();
    }

    public FileCache getFileCache() {
        return this.mediaIO.getFileCache();
    }

    public String getName() {
        return Paths.get(this.mediaIO.getUri()).getFileName().toString();
    }

    public Object getKey() {
        return this.key;
    }

    public boolean saveToFile(File output) {
        return MediaElement.saveToFile(this.mediaIO, output);
    }

    public static boolean saveToFile(MediaReader<?> mediaIO, File output) {
        if (mediaIO.getFileCache().isElementInMemory()) {
            return mediaIO.buildFile(output);
        }
        Optional<Path> file = mediaIO.getFileCache().getOriginalFile();
        return file.filter(value -> StreamUtil.copyFile((Path)value, (Path)output.toPath())).isPresent();
    }

    public long getLength() {
        return this.mediaIO.getFileCache().getLength();
    }

    public FileTime getLastModified() {
        return this.mediaIO.getFileCache().getLastModified();
    }

    public String getMimeType() {
        return this.mediaIO.getMediaFragmentMimeType();
    }

    protected final synchronized boolean setAsLoading() {
        if (!this.loading) {
            this.loading = true;
            return this.loading;
        }
        return false;
    }

    protected final synchronized void setAsLoaded() {
        this.loading = false;
    }

    public final synchronized boolean isLoading() {
        return this.loading;
    }
}

