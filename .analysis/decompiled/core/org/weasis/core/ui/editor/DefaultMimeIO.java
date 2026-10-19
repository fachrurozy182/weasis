/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.ui.editor;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.FileCache;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesEvent;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;
import org.weasis.opencv.data.PlanarImage;

public class DefaultMimeIO
implements MediaReader<MediaElement> {
    private static final TagView defaultTagView = new TagView(TagW.FileName);
    protected URI uri;
    protected final String mimeType;
    private MediaElement mediaElement = null;
    private final FileCache fileCache;

    public DefaultMimeIO(URI media, String mimeType) {
        this.uri = Objects.requireNonNull(media);
        this.fileCache = new FileCache(this);
        this.mimeType = mimeType == null ? "application/x-unknown-mime-type" : mimeType;
    }

    @Override
    public PlanarImage getImageFragment(MediaElement media) throws Exception {
        return null;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    @Override
    public MediaElement getPreview() {
        return this.getSingleImage();
    }

    @Override
    public boolean delegate(DataExplorerModel explorerModel) {
        return false;
    }

    public MediaElement[] getMediaElement() {
        return new MediaElement[]{this.getSingleImage()};
    }

    @Override
    public MediaSeries<MediaElement> getMediaSeries() {
        Series<MediaElement> mediaSeries = new Series<MediaElement>(this, TagW.FilePath, (Object)this.toString(), defaultTagView, 1){
            {
                Objects.requireNonNull(this$0);
                super(tagID, identifier, displayTag, initialCapacity);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public String getMimeType() {
                if (!this.medias.isEmpty()) {
                    1 var1_1 = this;
                    synchronized (var1_1) {
                        MediaElement img = (MediaElement)this.medias.getFirst();
                        if (img != null) {
                            return img.getMimeType();
                        }
                    }
                }
                return null;
            }

            @Override
            public void addMedia(MediaElement media) {
                if (media != null) {
                    this.add(media);
                    DataExplorerModel model = (DataExplorerModel)this.getTagValue(TagW.ExplorerModel);
                    if (model != null) {
                        model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.ADD, model, null, (Object)new SeriesEvent(SeriesEvent.Action.ADD_IMAGE, this, media)));
                    }
                }
            }

            @Override
            public MediaElement getFirstSpecialElement() {
                return null;
            }
        };
        mediaSeries.add(this.getSingleImage());
        mediaSeries.setTag(TagW.FileName, this.mediaElement.getName());
        return mediaSeries;
    }

    @Override
    public int getMediaElementNumber() {
        return 1;
    }

    private MediaElement getSingleImage() {
        if (this.mediaElement == null) {
            this.mediaElement = new MediaElement(this, null);
        }
        return this.mediaElement;
    }

    @Override
    public String getMediaFragmentMimeType() {
        return this.mimeType;
    }

    @Override
    public Map<TagW, Object> getMediaFragmentTags(Object key) {
        return new HashMap<TagW, Object>();
    }

    @Override
    public void close() {
    }

    @Override
    public Codec getCodec() {
        return null;
    }

    @Override
    public String[] getReaderDescription() {
        return new String[]{"Default mime type reader "};
    }

    @Override
    public Object getTagValue(TagW tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceURI(URI uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTag(TagW tag, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containTagKey(TagW tag) {
        return false;
    }

    @Override
    public void setTagNoNull(TagW tag, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCache getFileCache() {
        return this.fileCache;
    }

    @Override
    public boolean buildFile(File output) {
        return false;
    }
}

