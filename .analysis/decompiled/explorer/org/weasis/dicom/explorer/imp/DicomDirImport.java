/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemFileChooser
 *  com.formdev.flatlaf.util.SystemInfo
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.net.URIUtils
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$OtherIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.imp;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.net.URIUtils;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadDicomDir;
import org.weasis.dicom.explorer.LocalPersistence;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.imp.DicomDirLoader;
import org.weasis.dicom.explorer.imp.DicomImport;
import org.weasis.dicom.explorer.imp.ImportDicom;
import org.weasis.dicom.explorer.imp.LocalImport;
import org.weasis.dicom.explorer.wado.LoadSeries;

public class DicomDirImport
extends AbstractItemDialogPage
implements ImportDicom {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomDirImport.class);
    private static final String LAST_DICOM_DIR = "lastDicomDir";
    private final JTextField textField = new JTextField();
    private JCheckBox checkboxWriteInCache;

    public DicomDirImport() {
        super(Messages.getString("DicomDirImport.dicomdir"), 5);
        this.initGUI();
    }

    public void initGUI() {
        this.add(LocalImport.createSearchPanel(this.textField, Messages.getString("DicomDirImport.path"), LAST_DICOM_DIR, this::browseDicomDirFile));
        JButton btnCdrom = new JButton(Messages.getString("DicomDirImport.detect"), (Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.OtherIcon.CDROM));
        btnCdrom.addActionListener(e -> {
            File dcmdir = DicomDirImport.getDcmDirFromMedia();
            if (dcmdir != null) {
                String path = dcmdir.getPath();
                this.textField.setText(path);
                LocalPersistence.getProperties().setProperty(LAST_DICOM_DIR, path);
            }
        });
        this.checkboxWriteInCache = new JCheckBox(Messages.getString("DicomDirImport.cache"));
        this.add(GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{btnCdrom}));
        this.add(GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{this.checkboxWriteInCache}));
        this.add(GuiUtils.boxYLastElement((int)5));
    }

    public void browseDicomDirFile() {
        File selectedFile;
        SystemFileChooser fileChooser;
        String directory = this.getImportPath();
        if (directory == null) {
            directory = LocalPersistence.getProperties().getProperty(LAST_DICOM_DIR, "");
        }
        if ((fileChooser = DicomDirImport.getSystemFileChooser(directory)).showOpenDialog((Component)((Object)this)) == 0 && (selectedFile = fileChooser.getSelectedFile()) != null) {
            String path = selectedFile.getPath();
            this.textField.setText(path);
            LocalPersistence.getProperties().setProperty(LAST_DICOM_DIR, path);
        }
    }

    private static SystemFileChooser getSystemFileChooser(String directory) {
        SystemFileChooser fileChooser = new SystemFileChooser(directory);
        fileChooser.setFileSelectionMode(0);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setApproveCallback((selectedFiles, context) -> {
            if (selectedFiles.length > 0) {
                File f = selectedFiles[0];
                String name = f.getName().toUpperCase();
                if (name.equals("DICOMDIR") || name.startsWith("DICOMDIR.")) {
                    return 0;
                }
                context.showMessageDialog(2, "Select a \"DICOMDIR\" filename.", null, 0, new String[]{Messages.getString("HttpHeadersEditor.close")});
            }
            return 1;
        });
        return fileChooser;
    }

    public void closeAdditionalWindow() {
    }

    private HangingProtocols.OpeningViewer getOpeningViewer() {
        return HangingProtocols.OpeningViewer.ALL_PATIENTS;
    }

    public void resetToDefaultValues() {
    }

    private String getImportPath() {
        String path = this.textField.getText().trim();
        if (StringUtil.hasText((CharSequence)path)) {
            return path;
        }
        return null;
    }

    @Override
    public void importDICOM(DicomModel dicomModel, JProgressBar info) {
        File file = null;
        String path = this.getImportPath();
        if (path != null) {
            File f = new File(path);
            if (f.canRead()) {
                file = f;
            } else {
                try {
                    f = URIUtils.toFile((URI)new URI(path));
                    if (f.canRead()) {
                        file = f;
                    }
                }
                catch (Exception e) {
                    LOGGER.error("Cannot read {}", (Object)path);
                }
            }
        }
        if (file != null) {
            LocalPersistence.getProperties().setProperty(LAST_DICOM_DIR, file.getPath());
            List<LoadSeries> loadSeries = DicomDirImport.loadDicomDir(file, dicomModel, this.checkboxWriteInCache.isSelected());
            if (loadSeries != null && !loadSeries.isEmpty()) {
                DicomModel.LOADING_EXECUTOR.execute(new LoadDicomDir(loadSeries, dicomModel, this.getOpeningViewer()));
            } else {
                Dialog dialog;
                LOGGER.error("Cannot import DICOM from {}", (Object)file);
                int response = JOptionPane.showConfirmDialog(WinUtil.getValidComponent((Component)((Object)this)), Messages.getString("DicomExplorer.mes_import_manual"), this.getTitle(), 0, 2);
                if (response == 0 && (dialog = WinUtil.getParentDialog((Component)((Object)this))) instanceof DicomImport) {
                    DicomImport dcmImport = (DicomImport)((Object)dialog);
                    dcmImport.setCancelVeto(true);
                    dcmImport.showPage(Messages.getString("DicomImport.imp_dicom"));
                    AbstractItemDialogPage page = dcmImport.getCurrentPage();
                    if (page instanceof LocalImport) {
                        LocalImport localImport = (LocalImport)page;
                        localImport.setImportPath(file.getParent());
                    }
                }
            }
        }
    }

    public static List<LoadSeries> loadDicomDir(File file, DicomModel dicomModel, boolean writeInCache) {
        List<LoadSeries> loadSeries = null;
        if (file != null && file.canRead()) {
            DicomDirLoader dirImport = new DicomDirLoader(file, dicomModel, writeInCache);
            loadSeries = dirImport.readDicomDir();
        }
        return loadSeries;
    }

    private static void addFiles(List<File> dvs, File folder) {
        File[] files;
        if (folder.canRead() && (files = folder.listFiles()) != null) {
            Collections.addAll(dvs, files);
        }
    }

    public static File getDcmDirFromMedia() {
        ArrayList<File> dvs = new ArrayList<File>();
        try {
            if (SystemInfo.isWindows) {
                dvs.addAll(Arrays.asList(File.listRoots()));
            } else if (SystemInfo.isMacOS) {
                DicomDirImport.addFiles(dvs, new File("/Volumes"));
            } else {
                DicomDirImport.addFiles(dvs, new File("/media"));
                DicomDirImport.addFiles(dvs, new File("/mnt"));
                String user = System.getProperty("user.name", "local");
                DicomDirImport.addFiles(dvs, new File("/media/" + user));
                DicomDirImport.addFiles(dvs, new File("/run/media/" + user));
            }
        }
        catch (Exception e) {
            LOGGER.error("Error when reading device directories: {}", (Object)e.getMessage());
        }
        Collections.reverse(dvs);
        String[] dicomdir = new String[]{"DICOMDIR", "dicomdir", "DICOMDIR.", "dicomdir."};
        for (File drive : dvs) {
            if (!drive.canRead() || drive.isHidden()) continue;
            for (String s : dicomdir) {
                File f = new File(drive, s);
                if (!f.canRead() || f.canWrite()) continue;
                return f;
            }
        }
        return null;
    }
}

