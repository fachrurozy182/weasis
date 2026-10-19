/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.explorer.model.TreeModel
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.ui.editor.SeriesViewer
 *  org.weasis.core.ui.editor.SeriesViewerFactory
 *  org.weasis.core.ui.editor.ViewerOpenOptions
 *  org.weasis.core.ui.editor.ViewerPlacement
 *  org.weasis.core.ui.editor.ViewerPluginBuilder
 *  org.weasis.core.ui.editor.image.SequenceHandler
 *  org.weasis.core.ui.editor.image.SynchData$Mode
 *  org.weasis.core.ui.editor.image.ViewCanvas
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.DicomSeries
 */
package org.weasis.dicom.explorer;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.swing.TransferHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.explorer.model.TreeModel;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.core.ui.editor.ViewerPlacement;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.editor.image.SequenceHandler;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.DicomViewerPlugin;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.MimeSystemAppFactory;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.main.SeriesSelectionModel;

public class DicomSeriesHandler
extends SequenceHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomSeriesHandler.class);
    private final ViewCanvas<DicomImageElement> viewCanvas;

    public DicomSeriesHandler(ViewCanvas<DicomImageElement> viewCanvas) {
        super(true, true);
        this.viewCanvas = viewCanvas;
    }

    protected boolean importDataExt(TransferHandler.TransferSupport support) {
        try {
            DicomSeries series = this.extractSeries(support.getTransferable());
            if (series == null) {
                return false;
            }
            DicomViewerPlugin selectedPlugin = this.findSelectedPlugin();
            if (selectedPlugin == null) {
                return false;
            }
            SeriesViewerFactory plugin = GuiUtils.getUICore().getViewerFactory((SeriesViewer)selectedPlugin);
            if (plugin == null) {
                return false;
            }
            DataExplorerModel model = (DataExplorerModel)series.getTagValue(TagW.ExplorerModel);
            if (this.shouldBuildNewPlugin(plugin, series)) {
                ViewerPluginBuilder.openInDefaultViewer((MediaSeries)series, (DataExplorerModel)model, (ViewerOpenOptions)ViewerOpenOptions.defaults());
                return true;
            }
            return this.handleDicomSeries(series, model, plugin, selectedPlugin);
        }
        catch (Exception e) {
            LOGGER.error("Error importing draggable series", (Throwable)e);
            return false;
        }
    }

    protected boolean dropFiles(List<Path> files) {
        return DicomSeriesHandler.dropDicomFiles(files);
    }

    public static boolean dropDicomFiles(List<Path> paths) {
        if (paths == null || paths.isEmpty()) {
            return false;
        }
        return DicomSeriesHandler.getDicomExplorer().map(explorer -> {
            DicomModel model = explorer.getDataExplorerModel();
            File[] files = (File[])paths.stream().map(Path::toFile).toArray(File[]::new);
            DicomModel.LOADING_EXECUTOR.execute(new LoadLocalDicom(files, true, (DataExplorerModel)model, HangingProtocols.OpeningViewer.ALL_PATIENTS));
            return true;
        }).orElse(false);
    }

    private DicomSeries extractSeries(Transferable transferable) {
        try {
            Object object;
            if (transferable != null && (object = transferable.getTransferData(Series.sequenceDataFlavor)) instanceof DicomSeries) {
                DicomSeries series = (DicomSeries)object;
                return series;
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to extract series from transferable", (Throwable)e);
        }
        return null;
    }

    private DicomViewerPlugin findSelectedPlugin() {
        return GuiUtils.getUICore().getViewerPlugins().stream().filter(DicomViewerPlugin.class::isInstance).map(DicomViewerPlugin.class::cast).filter(plugin -> plugin.isContainingView(this.viewCanvas)).findFirst().orElse(null);
    }

    private boolean shouldBuildNewPlugin(SeriesViewerFactory plugin, DicomSeries series) {
        return plugin instanceof MimeSystemAppFactory || !plugin.canReadMimeType(series.getMimeType());
    }

    private boolean handleDicomSeries(DicomSeries series, DataExplorerModel model, SeriesViewerFactory plugin, DicomViewerPlugin selectedPlugin) {
        if (series == null || !(model instanceof TreeModel)) {
            return false;
        }
        TreeModel treeModel = (TreeModel)model;
        try {
            this.executeWithOpeningSeries(this.getSelectionModel(), () -> {
                if (this.canAddToCurrentPlugin(series, model, plugin, selectedPlugin, treeModel)) {
                    this.addSeriesToPlugin(series, selectedPlugin);
                } else {
                    this.openInAppropriatePlugin(series, model, plugin);
                }
            });
            return true;
        }
        catch (Exception e) {
            LOGGER.error("Error handling DICOM series", (Throwable)e);
            return false;
        }
    }

    private boolean canAddToCurrentPlugin(DicomSeries series, DataExplorerModel model, SeriesViewerFactory plugin, DicomViewerPlugin selectedPlugin, TreeModel treeModel) {
        MediaSeriesGroup parentGroup = treeModel.getParent((MediaSeriesGroup)series, model.getTreeModelNodeForNewPlugin());
        if (parentGroup == null) {
            return false;
        }
        return parentGroup.equals((Object)selectedPlugin.getGroupID()) && plugin.canReadSeries((MediaSeries)series) && plugin.canAddSeries();
    }

    private void addSeriesToPlugin(DicomSeries series, DicomViewerPlugin selectedPlugin) {
        if (this.isTileMode(selectedPlugin)) {
            selectedPlugin.addSeries((MediaSeries)series);
        } else {
            this.viewCanvas.setSeries((MediaSeries)series);
            if (Boolean.TRUE.equals(selectedPlugin.isContainingView(this.viewCanvas))) {
                selectedPlugin.setSelectedImagePaneFromFocus(this.viewCanvas);
            }
        }
    }

    private boolean isTileMode(DicomViewerPlugin selectedPlugin) {
        return SynchData.Mode.TILE.equals((Object)selectedPlugin.getSynchView().getSynchData().getMode());
    }

    private void openInAppropriatePlugin(DicomSeries series, DataExplorerModel model, SeriesViewerFactory plugin) {
        if (plugin.canReadSeries((MediaSeries)series) || plugin.canAddSeries()) {
            new ViewerPluginBuilder(plugin, List.of(series), model, ViewerOpenOptions.defaults()).open();
        } else {
            DicomSeriesHandler.openDicomSeriesInViewer(series, model);
        }
    }

    public static void openDicomSeriesInViewer(DicomSeries series, DataExplorerModel model) {
        SeriesViewerFactory plugin = GuiUtils.getUICore().getViewerFactory(series.getMimeType());
        if (plugin != null) {
            ViewerOpenOptions opts = ViewerOpenOptions.builder().placement((ViewerPlacement)ViewerPlacement.reuseViewer((boolean)false, (boolean)true)).build();
            new ViewerPluginBuilder(plugin, List.of(series), model, opts).open();
        }
    }

    private SeriesSelectionModel getSelectionModel() {
        return DicomSeriesHandler.getDicomExplorer().map(DicomExplorer::getSelectionList).orElse(null);
    }

    private static Optional<DicomExplorer> getDicomExplorer() {
        Optional<DicomExplorer> optional;
        DataExplorerView dicomView = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME);
        if (dicomView instanceof DicomExplorer) {
            DicomExplorer explorer = (DicomExplorer)dicomView;
            optional = Optional.of(explorer);
        } else {
            optional = Optional.empty();
        }
        return optional;
    }

    private void executeWithOpeningSeries(SeriesSelectionModel selectionModel, Runnable action) {
        if (selectionModel != null) {
            selectionModel.setOpeningSeries(true);
            try {
                action.run();
            }
            finally {
                selectionModel.setOpeningSeries(false);
            }
        } else {
            action.run();
        }
    }
}

