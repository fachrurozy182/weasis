/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor;

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.ui.editor.SeriesViewerUI;
import org.weasis.core.ui.util.WtoolBar;

public interface SeriesViewer<E extends MediaElement> {
    public String getPluginName();

    public void close();

    public List<MediaSeries<E>> getOpenSeries();

    public void addSeries(MediaSeries<E> var1);

    public void removeSeries(MediaSeries<E> var1);

    public JMenu fillSelectedPluginMenu(JMenu var1);

    public SeriesViewerUI getSeriesViewerUI();

    default public WtoolBar getStatusBar() {
        return null;
    }

    public void setSelected(boolean var1);

    public MediaSeriesGroup getGroupID();

    public String getDockableUID();

    default public List<Action> getExportActions() {
        return Collections.emptyList();
    }

    default public List<Action> getPrintActions() {
        return Collections.emptyList();
    }
}

