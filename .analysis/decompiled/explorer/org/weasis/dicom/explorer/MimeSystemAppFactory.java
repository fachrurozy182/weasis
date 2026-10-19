/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemInfo
 *  org.osgi.service.component.annotations.Component
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ActionIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.ui.editor.MimeSystemAppViewer
 *  org.weasis.core.ui.editor.SeriesViewer
 *  org.weasis.core.ui.editor.SeriesViewerFactory
 *  org.weasis.core.ui.editor.SeriesViewerUI
 *  org.weasis.core.ui.editor.ViewerOpenOptions
 *  org.weasis.dicom.codec.FilesExtractor
 */
package org.weasis.dicom.explorer;

import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import javax.swing.Icon;
import org.osgi.service.component.annotations.Component;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.MimeSystemAppViewer;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.SeriesViewerUI;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.dicom.codec.FilesExtractor;
import org.weasis.dicom.explorer.Messages;

@Component(service={SeriesViewerFactory.class})
public class MimeSystemAppFactory
implements SeriesViewerFactory {
    public static final String NAME = Messages.getString("MimeSystemAppViewer.app");
    public static final MimeSystemAppViewer mimeSystemViewer = new MimeSystemAppViewer(){

        public String getPluginName() {
            return NAME;
        }

        public void addSeries(MediaSeries<MediaElement> series) {
            if (series instanceof FilesExtractor) {
                FilesExtractor extractor = (FilesExtractor)series;
                for (File file : extractor.getExtractFiles()) {
                    Desktop desktop;
                    if (SystemInfo.isLinux) {
                        1.startAssociatedProgramFromLinux((Path)file.toPath());
                        continue;
                    }
                    if (!Desktop.isDesktopSupported() || !(desktop = Desktop.getDesktop()).isSupported(Desktop.Action.OPEN)) continue;
                    1.startAssociatedProgramFromDesktop((Desktop)desktop, (Path)file.toPath());
                }
            }
        }

        public SeriesViewerUI getSeriesViewerUI() {
            return null;
        }

        public String getDockableUID() {
            return null;
        }
    };

    public Icon getIcon() {
        return ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.OPEN_EXTERNAL);
    }

    public String getUIName() {
        return NAME;
    }

    public String getDescription() {
        return NAME;
    }

    public boolean canReadMimeType(String mimeType) {
        return "video/dicom".equals(mimeType) || "encap/dicom".equals(mimeType);
    }

    public boolean isViewerCreatedByThisFactory(SeriesViewer<? extends MediaElement> viewer) {
        return false;
    }

    public SeriesViewer<? extends MediaElement> createSeriesViewer(ViewerOpenOptions options, DataExplorerModel model) {
        return mimeSystemViewer;
    }

    public int getLevel() {
        return 100;
    }

    public boolean canAddSeries() {
        return false;
    }

    public boolean canExternalizeSeries() {
        return false;
    }

    public boolean canReadSeries(MediaSeries<?> series) {
        return series instanceof FilesExtractor;
    }
}

