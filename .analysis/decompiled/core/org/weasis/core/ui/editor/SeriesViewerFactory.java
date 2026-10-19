/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor;

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GUIEntry;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.ViewerOpenOptions;

public interface SeriesViewerFactory
extends GUIEntry {
    public SeriesViewer<?> createSeriesViewer(ViewerOpenOptions var1, DataExplorerModel var2);

    public boolean canReadMimeType(String var1);

    public boolean isViewerCreatedByThisFactory(SeriesViewer<? extends MediaElement> var1);

    public int getLevel();

    public boolean canAddSeries();

    public boolean canExternalizeSeries();

    public boolean canReadSeries(MediaSeries<?> var1);

    default public List<Action> getOpenActions() {
        return Collections.emptyList();
    }
}

