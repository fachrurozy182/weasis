/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.editor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.AbstractFileModel;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.media.MimeInspector;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.FileCache;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.ui.editor.DefaultMimeIO;
import org.weasis.core.ui.editor.FileModel;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.serialize.XmlSerializer;

public final class MediaFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaFactory.class);

    private MediaFactory() {
    }

    public static MediaReader<MediaElement> getMedia(Path file) {
        return MediaFactory.getMedia(file, true);
    }

    public static MediaReader<MediaElement> getMedia(Path file, boolean systemReader) {
        if (file != null && Files.isReadable(file)) {
            Codec<MediaElement> codec;
            boolean cache = file.startsWith(AppProperties.FILE_CACHE_DIR.toString());
            String mimeType = MimeInspector.getMimeType(file);
            if (mimeType != null && (codec = BundleTools.getCodec(mimeType, "dcm4che")) != null) {
                MediaReader<MediaElement> mreader = codec.getMediaIO(file.toUri(), mimeType, null);
                if (cache) {
                    mreader.getFileCache().setOriginalTempFile(file);
                }
                return mreader;
            }
            if (systemReader) {
                DefaultMimeIO mreader = new DefaultMimeIO(file.toUri(), mimeType);
                if (cache) {
                    mreader.getFileCache().setOriginalTempFile(file);
                }
                return mreader;
            }
        }
        return null;
    }

    public static MediaSeries<MediaElement> buildMediaSeriesWithDefaultModel(MediaReader reader) {
        return MediaFactory.buildMediaSeriesWithDefaultModel(reader, null, null, null);
    }

    public static MediaSeries<MediaElement> buildMediaSeriesWithDefaultModel(MediaReader reader, String groupUID, TagW groupName, String groupValue) {
        return MediaFactory.buildMediaSeriesWithDefaultModel(reader, groupUID, groupName, groupValue, null);
    }

    public static MediaSeries<MediaElement> buildMediaSeriesWithDefaultModel(MediaReader<MediaElement> reader, String groupUID, TagW groupName, String groupValue, String seriesUID) {
        MediaSeriesGroup group2;
        if (reader instanceof DefaultMimeIO) {
            return reader.getMediaSeries();
        }
        MediaSeries<Object> series = null;
        MediaElement[] medias = reader.getMediaElement();
        if (medias == null) {
            return null;
        }
        FileModel defaultModel = ViewerPluginBuilder.DefaultDataModel;
        String sUID = seriesUID == null ? UUID.randomUUID().toString() : seriesUID;
        String gUID = groupUID == null ? UUID.randomUUID().toString() : groupUID;
        MediaSeriesGroup group1 = defaultModel.getHierarchyNode(MediaSeriesGroupNode.rootNode, gUID);
        if (group1 == null) {
            group1 = new MediaSeriesGroupNode(TagW.Group, gUID, AbstractFileModel.group.tagView());
            group1.setTagNoNull(groupName, groupValue);
            defaultModel.addHierarchyNode(MediaSeriesGroupNode.rootNode, group1);
        }
        if ((group2 = defaultModel.getHierarchyNode(group1, sUID)) instanceof Series) {
            series = (Series)group2;
        }
        try {
            if (series == null) {
                series = reader.getMediaSeries();
                series.setTag(TagW.ExplorerModel, defaultModel);
                defaultModel.addHierarchyNode(group1, series);
            } else {
                MediaElement[] sopTag = TagW.get("SOPInstanceUID");
                if (((Series)series).hasMediaContains((TagW)sopTag, reader.getTagValue((TagW)sopTag))) {
                    return series;
                }
                MediaElement[] mediaElementArray = medias;
                int n = mediaElementArray.length;
                for (int i = 0; i < n; ++i) {
                    MediaElement media = mediaElementArray[i];
                    series.addMedia(media);
                }
            }
            for (MediaElement media : medias) {
                MediaFactory.openAssociatedGraphics(media);
            }
        }
        catch (Exception e) {
            LOGGER.error("Build series error", (Throwable)e);
        }
        return series;
    }

    public static void openAssociatedGraphics(MediaElement media) {
        if (media instanceof ImageElement) {
            Path gpxFile;
            GraphicModel graphicModel;
            FileCache fc = media.getFileCache();
            Optional<Path> fo = fc.getOriginalFile();
            if (fc.isLocalFile() && fo.isPresent() && (graphicModel = XmlSerializer.readPresentationModel((gpxFile = Path.of(String.valueOf(fo.get()) + ".xml", new String[0])).toFile())) != null) {
                media.setTag(TagW.PresentationModel, graphicModel);
            }
        }
    }
}

