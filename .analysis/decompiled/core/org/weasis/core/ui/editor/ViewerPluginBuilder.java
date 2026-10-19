/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.editor.FileModel;
import org.weasis.core.ui.editor.MediaFactory;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.ViewerOpenOptions;

public class ViewerPluginBuilder {
    public static final FileModel DefaultDataModel = new FileModel();
    private final SeriesViewerFactory factory;
    private final List<? extends MediaSeries<? extends MediaElement>> series;
    private final DataExplorerModel model;
    private final ViewerOpenOptions options;

    public ViewerPluginBuilder(SeriesViewerFactory factory, List<? extends MediaSeries<? extends MediaElement>> series, DataExplorerModel model, ViewerOpenOptions options) {
        if (factory == null || series == null || model == null) {
            throw new IllegalArgumentException();
        }
        this.factory = factory;
        this.series = series;
        this.model = model;
        this.options = options == null ? ViewerOpenOptions.defaults() : options;
    }

    public SeriesViewerFactory getFactory() {
        return this.factory;
    }

    public List<MediaSeries<MediaElement>> getSeries() {
        return this.series;
    }

    public DataExplorerModel getModel() {
        return this.model;
    }

    public ViewerOpenOptions getViewerOpenOptions() {
        return this.options;
    }

    public void open() {
        this.model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REGISTER, this.model, null, (Object)this));
    }

    public static void openInDefaultViewer(MediaSeries<? extends MediaElement> series, DataExplorerModel model, ViewerOpenOptions options) {
        if (series == null) {
            return;
        }
        SeriesViewerFactory factory = GuiUtils.getUICore().getViewerFactory(series.getMimeType());
        if (factory != null) {
            new ViewerPluginBuilder(factory, List.of(series), model == null ? DefaultDataModel : model, options).open();
        }
    }

    public static void openInDefaultViewer(List<? extends MediaSeries<? extends MediaElement>> series, DataExplorerModel model, ViewerOpenOptions options) {
        if (series == null || series.isEmpty()) {
            return;
        }
        ArrayList<String> mimes = new ArrayList<String>();
        for (MediaSeries<? extends MediaElement> mediaSeries : series) {
            String mime = mediaSeries.getMimeType();
            if (mime == null || mimes.contains(mime)) continue;
            mimes.add(mime);
        }
        for (String string : mimes) {
            SeriesViewerFactory plugin = GuiUtils.getUICore().getViewerFactory(string);
            if (plugin == null) continue;
            ArrayList<MediaSeries<? extends MediaElement>> seriesList = new ArrayList<MediaSeries<? extends MediaElement>>();
            for (MediaSeries<? extends MediaElement> mediaSeries : series) {
                if (!string.equals(mediaSeries.getMimeType())) continue;
                seriesList.add(mediaSeries);
            }
            new ViewerPluginBuilder(plugin, seriesList, model, options).open();
        }
    }

    public static void openInDefaultViewer(MediaElement media, DataExplorerModel model, ViewerOpenOptions options) {
        if (media != null) {
            ViewerPluginBuilder.openInDefaultViewer(media.getMediaReader().getMediaSeries(), model, options);
        }
    }

    public static void openInDefaultViewer(Path file, ViewerOpenOptions options) {
        MediaReader<MediaElement> reader = MediaFactory.getMedia(file);
        if (reader != null) {
            MediaSeries<MediaElement> s = MediaFactory.buildMediaSeriesWithDefaultModel(reader);
            ViewerPluginBuilder.openInDefaultViewer(s, (DataExplorerModel)DefaultDataModel, options);
        }
    }
}

