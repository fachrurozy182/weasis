/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemFileChooser
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.net.URIUtils
 *  org.weasis.core.api.service.WProperties
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ActionIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.imp;

import com.formdev.flatlaf.util.SystemFileChooser;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.net.URIUtils;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.LocalPersistence;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.imp.ImportDicom;

public class LocalImport
extends AbstractItemDialogPage
implements ImportDicom {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalImport.class);
    public static final String LAST_RECURSIVE_MODE = "last.recursive.mode";
    public static final String LAST_OPEN_FILES = "last.open.files";
    public static final String LAST_OPEN_DIR = "lastOpenDir";
    private static final String MULTI_DIR_MESSAGE = "LocalImport.multi_dir";
    private static final String DIR_SUFFIX = ".dir";
    private final JCheckBox checkboxSearch = new JCheckBox(Messages.getString("LocalImport.recursive"), true);
    private final JTextField textFieldFiles = new JTextField();
    private final JTextField textFieldFolders = new JTextField();
    private File[] selectedFiles;
    private File[] selectedFolders;

    public LocalImport() {
        super(Messages.getString("LocalImport.local_dev"));
        this.setComponentPosition(0);
        this.initGUI();
    }

    public void initGUI() {
        this.add(LocalImport.createGroupPanel(Messages.getString("LocalImport.files"), GuiUtils.getHorizontalBoxLayoutPanel((int)5, (JComponent[])new JComponent[]{LocalImport.createSearchField(this.textFieldFiles, LAST_OPEN_FILES, this::browseFiles)})));
        this.add(GuiUtils.boxVerticalStrut((int)15));
        WProperties props = LocalPersistence.getProperties();
        boolean recursive = LangUtil.emptyToTrue((String)props.getProperty(LAST_RECURSIVE_MODE));
        this.checkboxSearch.setSelected(recursive);
        this.add(LocalImport.createGroupPanel(Messages.getString("LocalImport.folders"), GuiUtils.getHorizontalBoxLayoutPanel((int)5, (JComponent[])new JComponent[]{LocalImport.createSearchField(this.textFieldFolders, LAST_OPEN_DIR, this::browseFolders)}), GuiUtils.getFlowLayoutPanel((int)2, (int)0, (JComponent[])new JComponent[]{this.checkboxSearch})));
        this.add(GuiUtils.boxYLastElement((int)5));
    }

    private static JPanel createGroupPanel(String title, JComponent ... items) {
        JPanel panel = GuiUtils.getVerticalBoxLayoutPanel((int)5, (JComponent[])items);
        panel.setBorder(GuiUtils.getTitledBorder((String)title));
        return panel;
    }

    static JPanel createSearchPanel(JTextField textField, String title, String key, Runnable browseAction) {
        JLabel label = new JLabel(title + ":");
        return GuiUtils.getHorizontalBoxLayoutPanel((JComponent[])new JComponent[]{label, GuiUtils.boxHorizontalStrut((int)2), LocalImport.createSearchField(textField, key, browseAction)});
    }

    static JTextField createSearchField(JTextField textField, String key, Runnable browseAction) {
        JButton btnSearch = new JButton((Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.MORE_H));
        btnSearch.addActionListener(actionEvent -> browseAction.run());
        LocalImport.configureTextField(textField, key);
        textField.putClientProperty("JTextField.trailingComponent", btnSearch);
        return textField;
    }

    private static void configureTextField(JTextField textField, String key) {
        WProperties props = LocalPersistence.getProperties();
        textField.setText(props.getProperty(key, ""));
        textField.putClientProperty("JTextField.showClearButton", true);
        Dimension preferredSize = textField.getPreferredSize();
        preferredSize.width = 200;
        textField.setPreferredSize(preferredSize);
        textField.setMaximumSize(new Dimension(Short.MAX_VALUE, preferredSize.height));
    }

    private void browseFiles() {
        this.browseFileSystem(true);
    }

    private void browseFolders() {
        this.browseFileSystem(false);
    }

    private void browseFileSystem(boolean filesOnly) {
        File[] selected;
        String lastItemKey = filesOnly ? LAST_OPEN_FILES : LAST_OPEN_DIR;
        String initialDirectory = this.getInitialDirectory(lastItemKey);
        SystemFileChooser fileChooser = new SystemFileChooser(initialDirectory);
        fileChooser.setFileSelectionMode(filesOnly ? 0 : 1);
        fileChooser.setMultiSelectionEnabled(true);
        if (fileChooser.showOpenDialog((Component)WinUtil.getParentWindow((Component)((Object)this))) == 0 && (selected = fileChooser.getSelectedFiles()) != null && selected.length > 0) {
            this.handleSelection(selected, filesOnly, lastItemKey);
        }
    }

    private String getInitialDirectory(String lastItemKey) {
        String path = (LAST_OPEN_FILES.equals(lastItemKey) ? this.textFieldFiles : this.textFieldFolders).getText().trim();
        if (StringUtil.hasText((CharSequence)path) && !path.equals(Messages.getString(MULTI_DIR_MESSAGE))) {
            return LocalImport.directoryPath(path);
        }
        WProperties props = LocalPersistence.getProperties();
        String dir = props.getProperty(lastItemKey + DIR_SUFFIX, "");
        if (StringUtil.hasText((CharSequence)dir)) {
            return dir;
        }
        return LocalImport.directoryPath(props.getProperty(lastItemKey, ""));
    }

    private static String directoryPath(String path) {
        if (!StringUtil.hasText((CharSequence)path)) {
            return path;
        }
        File f = new File(path);
        if (f.isFile()) {
            File parent = f.getParentFile();
            return parent != null ? parent.getPath() : path;
        }
        return path;
    }

    private void handleSelection(File[] selected, boolean filesOnly, String lastItemKey) {
        boolean isSingleSelection;
        JTextField textField = filesOnly ? this.textFieldFiles : this.textFieldFolders;
        boolean bl = isSingleSelection = selected.length == 1;
        if (isSingleSelection) {
            textField.setText(selected[0].getPath());
        } else {
            textField.setText(Messages.getString(MULTI_DIR_MESSAGE));
        }
        if (filesOnly) {
            this.selectedFiles = selected;
        } else {
            this.selectedFolders = selected;
        }
        this.saveSelection(lastItemKey, selected, isSingleSelection);
    }

    private void saveSelection(String key, File[] selected, boolean isSingleSelection) {
        WProperties props = LocalPersistence.getProperties();
        if (isSingleSelection) {
            String path = selected[0].getPath();
            props.setProperty(key, path);
            props.setProperty(key + DIR_SUFFIX, LocalImport.directoryPath(path));
        } else {
            props.setProperty(key, "");
            File commonParent = LocalImport.commonParent(selected);
            props.setProperty(key + DIR_SUFFIX, commonParent != null ? commonParent.getPath() : "");
        }
    }

    private static File commonParent(File[] files) {
        File parent = files[0].getParentFile();
        if (parent == null) {
            return null;
        }
        for (int i = 1; i < files.length; ++i) {
            File p = files[i].getParentFile();
            while (p != null && !LocalImport.isAncestorOrSelf(p, parent) && !LocalImport.isAncestorOrSelf(parent, p)) {
                if ((parent = parent.getParentFile()) != null) continue;
                return null;
            }
            if (p == null || !LocalImport.isAncestorOrSelf(parent, p)) continue;
            parent = p;
        }
        return parent;
    }

    private static boolean isAncestorOrSelf(File ancestor, File file) {
        for (File current = file; current != null; current = current.getParentFile()) {
            if (!current.equals(ancestor)) continue;
            return true;
        }
        return false;
    }

    public void closeAdditionalWindow() {
        String foldersText;
        WProperties props = LocalPersistence.getProperties();
        props.setProperty(LAST_RECURSIVE_MODE, String.valueOf(this.checkboxSearch.isSelected()));
        String filesText = this.textFieldFiles.getText().trim();
        if (!filesText.equals(Messages.getString(MULTI_DIR_MESSAGE))) {
            props.setProperty(LAST_OPEN_FILES, filesText);
            props.setProperty("last.open.files.dir", LocalImport.directoryPath(filesText));
        }
        if (!(foldersText = this.textFieldFolders.getText().trim()).equals(Messages.getString(MULTI_DIR_MESSAGE))) {
            props.setProperty(LAST_OPEN_DIR, foldersText);
            props.setProperty("lastOpenDir.dir", LocalImport.directoryPath(foldersText));
        }
    }

    private HangingProtocols.OpeningViewer getOpeningViewer() {
        return HangingProtocols.OpeningViewer.ALL_PATIENTS;
    }

    public void resetToDefaultValues() {
    }

    public void setImportPath(String path) {
        this.textFieldFolders.setText(path);
    }

    @Override
    public void importDICOM(DicomModel dicomModel, JProgressBar info) {
        List<File> filesToImport = this.collectFilesToImport();
        if (!filesToImport.isEmpty()) {
            File[] filesArray = (File[])filesToImport.toArray(File[]::new);
            DicomModel.LOADING_EXECUTOR.execute(new LoadLocalDicom(filesArray, this.checkboxSearch.isSelected(), (DataExplorerModel)dicomModel, this.getOpeningViewer()));
            this.resetSelection();
        }
    }

    private List<File> collectFilesToImport() {
        ArrayList<File> files = new ArrayList<File>();
        if (this.selectedFiles != null) {
            files.addAll(List.of(this.selectedFiles));
        } else {
            File file = this.getFileFromTextField(this.textFieldFiles);
            if (file != null && file.isFile()) {
                files.add(file);
            }
        }
        if (this.selectedFolders != null) {
            files.addAll(List.of(this.selectedFolders));
        } else {
            File folder = this.getFileFromTextField(this.textFieldFolders);
            if (folder != null) {
                files.add(folder);
            }
        }
        return files;
    }

    private File getFileFromTextField(JTextField textField) {
        String path = textField.getText().trim();
        if (!StringUtil.hasText((CharSequence)path) || path.equals(Messages.getString(MULTI_DIR_MESSAGE))) {
            return null;
        }
        File file = this.tryCreateFile(path);
        if (file != null && file.canRead()) {
            return file;
        }
        try {
            return URIUtils.toFile((URI)new URI(path));
        }
        catch (Exception e) {
            LOGGER.error("Cannot import DICOM from {}", (Object)path);
            return null;
        }
    }

    private File tryCreateFile(String path) {
        try {
            return new File(path);
        }
        catch (Exception e) {
            LOGGER.debug("Cannot create file from path: {}", (Object)path);
            return null;
        }
    }

    private void resetSelection() {
        this.selectedFiles = null;
        this.selectedFolders = null;
    }
}

