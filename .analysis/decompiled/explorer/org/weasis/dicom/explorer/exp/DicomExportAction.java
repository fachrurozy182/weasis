/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.service.UICore
 *  org.weasis.core.ui.editor.image.ImageViewerEventManager
 *  org.weasis.core.ui.editor.image.ViewCanvas
 *  org.weasis.core.ui.launcher.Launcher
 *  org.weasis.core.ui.launcher.Launcher$Configuration
 *  org.weasis.core.ui.launcher.Placeholder
 *  org.weasis.core.util.FileUtil
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.DicomMediaIO
 *  org.weasis.dicom.codec.DicomSeries
 */
package org.weasis.dicom.explorer.exp;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.service.UICore;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.launcher.Launcher;
import org.weasis.core.ui.launcher.Placeholder;
import org.weasis.core.util.FileUtil;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.LocalPersistence;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.exp.CheckTreeModel;
import org.weasis.dicom.explorer.exp.ExportDicomView;

public class DicomExportAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomExportAction.class);
    protected final Launcher launcher;
    protected final DicomModel dicomModel;
    protected static final BiFunction<String, ImageViewerEventManager<?>, String> folderReplacement = (val, imageViewerEventManager) -> {
        if ("wado.folder".equals(val)) {
            return DicomMediaIO.DICOM_EXPORT_DIR.toString();
        }
        if ("qr.folder".equals(val)) {
            return String.valueOf(AppProperties.APP_TEMP_DIR) + File.separator + "tmp" + File.separator + "qr";
        }
        if ("last.folder".equals(val)) {
            return LocalPersistence.getProperties().getProperty("lastOpenDir", "");
        }
        return null;
    };
    protected static final BiFunction<String, ImageViewerEventManager<?>, String> tagReplacement = (tag, manager) -> {
        MediaSeries patt0$temp;
        if (manager == null) {
            return null;
        }
        ViewCanvas view = manager.getSelectedViewPane();
        if (view != null && (patt0$temp = view.getSeries()) instanceof DicomSeries) {
            DicomSeries series = (DicomSeries)patt0$temp;
            ImageElement image = view.getImage();
            TagW tagW = TagW.get((String)tag);
            if (tagW == null) {
                return null;
            }
            if (image != null && image.containTagKey(tagW)) {
                return tagW.getFormattedTagValue(image.getTagValue(tagW), null);
            }
            if (series.containTagKey(tagW)) {
                return tagW.getFormattedTagValue(series.getTagValue(tagW), null);
            }
            MediaSeriesGroup study = null;
            MediaSeriesGroup patient = null;
            Object tagValue = series.getTagValue(TagW.ExplorerModel);
            if (tagValue instanceof DicomModel) {
                DicomModel model = (DicomModel)tagValue;
                study = model.getParent((MediaSeriesGroup)series, DicomModel.study);
                patient = model.getParent((MediaSeriesGroup)series, DicomModel.patient);
            }
            if (study != null && study.containTagKey(tagW)) {
                return tagW.getFormattedTagValue(study.getTagValue(tagW), null);
            }
            if (patient != null && patient.containTagKey(tagW)) {
                return tagW.getFormattedTagValue(patient.getTagValue(tagW), null);
            }
        }
        return null;
    };
    protected static final Placeholder DICOM_FOLDER_PLACEHOLDER = new Placeholder("\\{dicom:(\\S+)}", folderReplacement);
    protected static final Placeholder DICOM_TAG_PLACEHOLDER = new Placeholder("\\{tag:(\\S+)}", tagReplacement);

    public DicomExportAction(Launcher launcher, DicomModel dicomModel) {
        this.launcher = Objects.requireNonNull(launcher);
        this.dicomModel = Objects.requireNonNull(dicomModel);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void execute() throws IOException {
        Launcher.Configuration config = this.launcher.getConfiguration();
        CheckTreeModel treeModel = new CheckTreeModel(this.dicomModel);
        ExportDicomView view = new ExportDicomView(Messages.getString("dicom.selection"), 0, this.dicomModel, treeModel);
        JScrollPane scrollPane = new JScrollPane((Component)((Object)view.initGUI()));
        scrollPane.setPreferredSize(GuiUtils.getDimension((int)650, (int)500));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        int option = JOptionPane.showConfirmDialog(UICore.getInstance().getApplicationWindow(), scrollPane, view.getTitle(), 2, -1);
        HashSet<Path> sources = new HashSet<Path>();
        if (option == 0) {
            TreePath[] paths;
            for (TreePath treePath : paths = treeModel.getCheckingPaths()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
                Object object = node.getUserObject();
                if (!(object instanceof DicomImageElement)) continue;
                DicomImageElement img = (DicomImageElement)object;
                sources.add(img.getFilePath());
            }
        }
        if (sources.isEmpty()) {
            return;
        }
        this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_START, (Object)this.dicomModel, null, (Object)this));
        try {
            Path destination = DicomExportAction.getSelectionTempFolder().toPath();
            config.setSource(destination);
            DicomExportAction.copyFilesOrDirectories(sources, destination);
        }
        catch (IOException e) {
            LOGGER.error("Error copying files", (Throwable)e);
        }
        finally {
            this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_STOP, (Object)this.dicomModel, null, (Object)this));
        }
    }

    protected static File getSelectionTempFolder() {
        return FileUtil.createTempDir((Path)AppProperties.buildAccessibleTempDirectory((String[])new String[]{"tmp", "sel"})).toFile();
    }

    protected static void copyFilesOrDirectories(Set<Path> sources, Path destination) throws IOException {
        for (Path source : sources) {
            if (Files.isDirectory(source, new LinkOption[0])) {
                FileUtil.copyFolder((Path)source, (Path)destination, (CopyOption[])new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
                continue;
            }
            Files.copy(source, destination.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String resolvePlaceholders(String template, ImageViewerEventManager<?> eventManager) {
        String result = DICOM_FOLDER_PLACEHOLDER.resolvePlaceholders(template, eventManager);
        return DICOM_TAG_PLACEHOLDER.resolvePlaceholders(result, eventManager);
    }
}

