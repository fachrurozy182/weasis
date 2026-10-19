/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.util.UIDUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.ThreadUtil
 *  org.weasis.core.ui.model.GraphicModel
 *  org.weasis.core.ui.tp.raven.spinner.SpinnerProgress
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.LangUtil
 *  org.weasis.dicom.codec.DicomElement
 *  org.weasis.dicom.codec.DicomElement$DicomExportParameters
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.param.CancelListener
 *  org.weasis.dicom.param.DicomProgress
 */
package org.weasis.dicom.explorer.exp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.ThreadUtil;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.tp.raven.spinner.SpinnerProgress;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.LangUtil;
import org.weasis.dicom.codec.DicomElement;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.exp.CheckTreeModel;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.exp.ExportDicom;
import org.weasis.dicom.explorer.exp.ExportTree;
import org.weasis.dicom.explorer.exp.LocalExport;
import org.weasis.dicom.param.CancelListener;
import org.weasis.dicom.param.DicomProgress;

public class ExportDicomView
extends AbstractItemDialogPage
implements ExportDicom {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportDicomView.class);
    protected final File exportDir;
    protected final DicomModel dicomModel;
    protected final ExportTree exportTree;
    protected final ExecutorService executor = ThreadUtil.newFixedThreadPool((int)3, (String)"DicomSend");

    public ExportDicomView(String title, int position, DicomModel dicomModel, CheckTreeModel treeModel) {
        this(title, position, dicomModel, treeModel, null);
    }

    public ExportDicomView(String title, int position, DicomModel dicomModel, CheckTreeModel treeModel, File exportDir) {
        super(title, position);
        this.dicomModel = dicomModel;
        this.exportTree = new ExportTree(treeModel);
        this.exportDir = exportDir == null ? FileUtil.createTempDir((Path)AppProperties.buildAccessibleTempDirectory((String[])new String[]{"tmp", "send"})).toFile() : exportDir;
    }

    public ExportDicomView initGUI() {
        this.exportTree.setBorder(UIManager.getBorder("ScrollPane.border"));
        this.add(this.exportTree);
        return this;
    }

    public void closeAdditionalWindow() {
        this.executor.shutdown();
    }

    public void resetToDefaultValues() {
    }

    @Override
    public void exportDICOM(final CheckTreeModel model, JProgressBar info) throws IOException {
        ExplorerTask<Boolean, String> task = new ExplorerTask<Boolean, String>(this, this.getTitle(), false){
            final /* synthetic */ ExportDicomView this$0;
            {
                ExportDicomView exportDicomView = this$0;
                Objects.requireNonNull(exportDicomView);
                this.this$0 = exportDicomView;
                super(message, interruptible);
            }

            @Override
            protected Boolean doInBackground() throws Exception {
                return this.this$0.sendDicomFiles(model, this);
            }

            @Override
            protected void done() {
                this.this$0.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_STOP, (Object)this.this$0.dicomModel, null, (Object)this));
            }
        };
        this.executor.execute(task);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean sendDicomFiles(CheckTreeModel model, ExplorerTask<Boolean, String> t) {
        this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_START, (Object)this.dicomModel, null, t));
        File exportDir = FileUtil.createTempDir((Path)AppProperties.buildAccessibleTempDirectory((String[])new String[]{"tmp", "send"})).toFile();
        File prTempStorage = FileUtil.createTempDir((Path)AppProperties.buildAccessibleTempDirectory((String[])new String[]{"tmp", "pr"})).toFile();
        try {
            this.writeDicom(t, exportDir, prTempStorage, model);
            if (t.isCancelled()) {
                boolean bl = false;
                return bl;
            }
            ArrayList<String> files = new ArrayList<String>();
            files.add(exportDir.getAbsolutePath());
            DicomProgress dicomProgress = ExportDicomView.getDicomProgress(t);
            t.addCancelListener((CancelListener)dicomProgress);
            boolean ok = this.exportAction(files, dicomProgress);
            if (ok) {
                DicomModel.LOADING_EXECUTOR.execute(new LoadLocalDicom(prTempStorage.listFiles(), false, (DataExplorerModel)this.dicomModel, HangingProtocols.OpeningViewer.ALL_PATIENTS));
            }
            boolean bl = ok;
            return bl;
        }
        finally {
            FileUtil.recursiveDelete((Path)exportDir.toPath());
            prTempStorage.deleteOnExit();
        }
    }

    private static DicomProgress getDicomProgress(ExplorerTask<Boolean, String> t) {
        SpinnerProgress progressBar = t.getBar();
        DicomProgress dicomProgress = new DicomProgress();
        dicomProgress.addProgressListener(p -> GuiExecutor.execute(() -> {
            int c = p.getNumberOfCompletedSuboperations() + p.getNumberOfFailedSuboperations();
            int r = p.getNumberOfRemainingSuboperations();
            progressBar.setValue(c * 100 / (c + r));
        }));
        return dicomProgress;
    }

    protected boolean exportAction(List<String> files, DicomProgress dicomProgress) {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeDicom(ExplorerTask<Boolean, String> task, File writeDir, File prTempDir, CheckTreeModel model) {
        ExportDicomView exportDicomView = this;
        synchronized (exportDicomView) {
            TreePath[] paths;
            ArrayList<Object> uids = new ArrayList<Object>();
            for (TreePath treePath : paths = model.getCheckingPaths()) {
                MediaSeries s;
                String prFile;
                Object iuid;
                if (task.isCancelled()) {
                    return;
                }
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
                Object object = node.getUserObject();
                if (object instanceof DicomImageElement) {
                    DicomImageElement img = (DicomImageElement)object;
                    iuid = (String)TagD.getTagValue((TagReadable)img, (int)524312, String.class);
                    int index = uids.indexOf(iuid);
                    if (index != -1) continue;
                    uids.add(iuid);
                    String path = LocalExport.buildPath((MediaElement)img, false, false, node, null);
                    this.saveFile(writeDir, (DicomElement)img, (String)iuid, path);
                    continue;
                }
                iuid = node.getUserObject();
                if (iuid instanceof DicomElement) {
                    DicomElement dcm = (DicomElement)iuid;
                    iuid = (String)TagD.getTagValue((TagReadable)((TagReadable)dcm), (int)524312, String.class);
                    String path = LocalExport.buildPath((MediaElement)dcm, false, false, node, null);
                    this.saveFile(writeDir, dcm, (String)iuid, path);
                    continue;
                }
                iuid = node.getUserObject();
                if (!(iuid instanceof MediaSeries) || (prFile = ExportDicomView.saveOtherMediaSeries(writeDir, s = (MediaSeries)iuid, node)) == null) continue;
                File pr = new File(prFile);
                try {
                    Files.copy(pr.toPath(), new File(prTempDir, pr.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e) {
                    LOGGER.error("Failed to copy PR file to temporary directory ", (Throwable)e);
                }
            }
        }
    }

    private static String saveOtherMediaSeries(File writeDir, MediaSeries<?> s, DefaultMutableTreeNode node) {
        Series series;
        if (LangUtil.nullToFalse((Boolean)((Boolean)s.getTagValue(TagW.ObjectToSave))) && (series = (Series)s.getTagValue(CheckTreeModel.SourceSeriesForPR)) != null) {
            String seriesInstanceUID = UIDUtils.createUID();
            for (MediaElement dcm : series.getMedias(null, null)) {
                GraphicModel grModel = (GraphicModel)dcm.getTagValue(TagW.PresentationModel);
                if (grModel == null || !grModel.hasSerializableGraphics()) continue;
                String path = LocalExport.buildPath(dcm, false, false, node, null);
                return LocalExport.buildAndWritePR(dcm, false, new File(writeDir, path), null, node, seriesInstanceUID);
            }
        }
        return null;
    }

    private void saveFile(File writeDir, DicomElement dcm, String iuid, String path) {
        File destinationDir = new File(writeDir, path);
        destinationDir.mkdirs();
        DicomElement.DicomExportParameters dicomExportParameters = new DicomElement.DicomExportParameters(null, true, null, 0, 0);
        dcm.saveToFile(new File(destinationDir, iuid), dicomExportParameters);
    }
}

