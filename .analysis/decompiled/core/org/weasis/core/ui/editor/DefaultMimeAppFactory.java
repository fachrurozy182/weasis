/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemInfo
 */
package org.weasis.core.ui.editor;

import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Desktop;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.MimeSystemAppViewer;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.SeriesViewerUI;
import org.weasis.core.ui.editor.ViewerOpenOptions;

public class DefaultMimeAppFactory
implements SeriesViewerFactory {
    public static final String NAME = Messages.getString("DefaultMimeAppFactory.sys_app");
    public static final MimeSystemAppViewer MimeSystemViewer = new MimeSystemAppViewer(){

        @Override
        public String getPluginName() {
            return NAME;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void addSeries(MediaSeries<MediaElement> series) {
            if (series != null) {
                Iterable<MediaElement> list = series.getMedias(null, null);
                MediaSeries<MediaElement> mediaSeries = series;
                synchronized (mediaSeries) {
                    for (MediaElement m : list) {
                        Desktop desktop;
                        if (SystemInfo.isLinux) {
                            1.startAssociatedProgramFromLinux(m.getFilePath());
                            continue;
                        }
                        if (!Desktop.isDesktopSupported() || !(desktop = Desktop.getDesktop()).isSupported(Desktop.Action.OPEN)) continue;
                        1.startAssociatedProgramFromDesktop(desktop, m.getFilePath());
                    }
                }
            }
        }

        @Override
        public SeriesViewerUI getSeriesViewerUI() {
            return null;
        }

        @Override
        public String getDockableUID() {
            return null;
        }
    };
    private static final DefaultMimeAppFactory instance = new DefaultMimeAppFactory();

    private DefaultMimeAppFactory() {
    }

    public static DefaultMimeAppFactory getInstance() {
        return instance;
    }

    @Override
    public Icon getIcon() {
        return ResourceUtil.getIcon(ResourceUtil.ActionIcon.OPEN_EXTERNAL);
    }

    @Override
    public String getUIName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean canReadMimeType(String mimeType) {
        return true;
    }

    @Override
    public boolean isViewerCreatedByThisFactory(SeriesViewer<? extends MediaElement> viewer) {
        return false;
    }

    public SeriesViewer<MediaElement> createSeriesViewer(ViewerOpenOptions options, DataExplorerModel model) {
        return MimeSystemViewer;
    }

    @Override
    public int getLevel() {
        return 100;
    }

    @Override
    public boolean canAddSeries() {
        return false;
    }

    @Override
    public boolean canExternalizeSeries() {
        return false;
    }

    @Override
    public boolean canReadSeries(MediaSeries<?> series) {
        return series != null && series.size(null) > 0;
    }
}

