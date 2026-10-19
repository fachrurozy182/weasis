/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.media.data;

import java.io.File;
import java.net.URI;
import java.util.Map;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.FileCache;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.opencv.data.PlanarImage;

public interface MediaReader<E extends MediaElement>
extends Taggable {
    public URI getUri();

    public FileCache getFileCache();

    public E[] getMediaElement();

    public MediaSeries<E> getMediaSeries();

    public boolean delegate(DataExplorerModel var1);

    default public boolean isOnlyDelegate() {
        return false;
    }

    public MediaElement getPreview();

    public PlanarImage getImageFragment(MediaElement var1) throws Exception;

    public int getMediaElementNumber();

    public String getMediaFragmentMimeType();

    public Map<TagW, Object> getMediaFragmentTags(Object var1);

    public void close();

    public Codec getCodec();

    public String[] getReaderDescription();

    @Override
    public Object getTagValue(TagW var1);

    public void replaceURI(URI var1);

    public boolean buildFile(File var1);
}

