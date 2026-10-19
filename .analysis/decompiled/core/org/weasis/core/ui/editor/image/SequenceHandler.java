/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.editor.image;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.util.UriListFlavor;

public class SequenceHandler
extends TransferHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceHandler.class);
    private final boolean sequenceFlavor;
    private final boolean fileFlavor;

    public SequenceHandler() {
        this(true, true);
    }

    public SequenceHandler(boolean sequence, boolean files) {
        super("series");
        this.sequenceFlavor = sequence;
        this.fileFlavor = files;
    }

    @Override
    public Transferable createTransferable(JComponent comp) {
        Transferable transferable;
        if (comp instanceof SeriesThumbnail) {
            SeriesThumbnail thumbnail = (SeriesThumbnail)comp;
            transferable = this.getSeriesFromThumbnail(thumbnail);
        } else {
            transferable = null;
        }
        return transferable;
    }

    private Transferable getSeriesFromThumbnail(SeriesThumbnail thumbnail) {
        MediaSeries<MediaElement> series = thumbnail.getSeries();
        return series instanceof Series ? series : null;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }
        return this.sequenceFlavor && support.isDataFlavorSupported(Series.sequenceDataFlavor) || this.fileFlavor && this.isFileFlavorSupported(support);
    }

    private boolean isFileFlavorSupported(TransferHandler.TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || support.isDataFlavorSupported(UriListFlavor.flavor);
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        List<Path> files;
        if (!this.canImport(support)) {
            return false;
        }
        if (this.fileFlavor && (files = this.extractFiles(support)) != null) {
            return this.dropFiles(files);
        }
        return this.sequenceFlavor && this.importDataExt(support);
    }

    private List<Path> extractFiles(TransferHandler.TransferSupport support) {
        Transferable transferable = support.getTransferable();
        if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return this.extractJavaFileList(transferable);
        }
        if (support.isDataFlavorSupported(UriListFlavor.flavor)) {
            return this.extractUriList(transferable);
        }
        return null;
    }

    private List<Path> extractJavaFileList(Transferable transferable) {
        try {
            List files = (List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
            return files.stream().map(File::toPath).toList();
        }
        catch (Exception e) {
            LOGGER.error("Failed to extract draggable files", (Throwable)e);
            return null;
        }
    }

    private List<Path> extractUriList(Transferable transferable) {
        try {
            String uriList = (String)transferable.getTransferData(UriListFlavor.flavor);
            return UriListFlavor.textURIListToPathList(uriList).stream().toList();
        }
        catch (Exception e) {
            LOGGER.error("Failed to extract draggable URIs", (Throwable)e);
            return null;
        }
    }

    protected boolean importDataExt(TransferHandler.TransferSupport support) {
        return false;
    }

    protected boolean dropFiles(List<Path> files) {
        files.forEach(file -> ViewerPluginBuilder.openInDefaultViewer(file, ViewerOpenOptions.defaults()));
        return true;
    }
}

