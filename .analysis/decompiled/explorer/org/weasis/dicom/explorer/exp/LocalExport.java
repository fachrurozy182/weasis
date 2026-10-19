/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemFileChooser
 *  com.formdev.flatlaf.util.SystemFileChooser$FileFilter
 *  com.formdev.flatlaf.util.SystemFileChooser$FileNameExtensionFilter
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.data.VR
 *  org.dcm4che3.imageio.codec.TransferSyntaxType
 *  org.dcm4che3.img.util.DicomUtils
 *  org.dcm4che3.media.DicomDirWriter
 *  org.dcm4che3.media.RecordType
 *  org.dcm4che3.util.UIDUtils
 *  org.opencv.core.Mat
 *  org.opencv.core.MatOfInt
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.ActionW
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.image.ImageOpNode
 *  org.weasis.core.api.image.SimpleOpManager
 *  org.weasis.core.api.image.WindowOp
 *  org.weasis.core.api.image.ZoomOp
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.FontItem
 *  org.weasis.core.ui.model.GraphicModel
 *  org.weasis.core.ui.serialize.XmlSerializer
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.core.util.StringUtil$Suffix
 *  org.weasis.core.util.ZipUtil
 *  org.weasis.dicom.codec.DcmMediaReader
 *  org.weasis.dicom.codec.DicomElement
 *  org.weasis.dicom.codec.DicomElement$DicomExportParameters
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.FileExtractor
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TransferSyntax
 *  org.weasis.dicom.codec.display.WindowAndPresetsOp
 *  org.weasis.dicom.param.AttributeEditor
 *  org.weasis.dicom.param.DefaultAttributeEditor
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.ImageIOHandler
 */
package org.weasis.dicom.explorer.exp;

import com.formdev.flatlaf.util.SystemFileChooser;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.util.UIDUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.image.WindowOp;
import org.weasis.core.api.image.ZoomOp;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.serialize.XmlSerializer;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.core.util.ZipUtil;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomElement;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.FileExtractor;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.TransferSyntax;
import org.weasis.dicom.codec.display.WindowAndPresetsOp;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.LocalPersistence;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.exp.CheckTreeModel;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.exp.ExportDicom;
import org.weasis.dicom.explorer.exp.ExportTree;
import org.weasis.dicom.explorer.imp.DicomDirLoader;
import org.weasis.dicom.explorer.pr.DicomPrSerializer;
import org.weasis.dicom.param.AttributeEditor;
import org.weasis.dicom.param.DefaultAttributeEditor;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.ImageIOHandler;

public class LocalExport
extends AbstractItemDialogPage
implements ExportDicom {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalExport.class);
    public static final String LAST_DIR = "lastExportDir";
    public static final String INC_DICOMDIR = "exp.include.dicomdir";
    public static final String KEEP_INFO_DIR = "exp.keep.dir.name";
    public static final String IMG_QUALITY = "exp.img.quality";
    public static final String IMG_JXL_QUALITY = "exp.img.jxl.quality";
    public static final String IMG_16_BIT = "exp.16-bit";
    public static final String IMG_PIXEL_PADDING = "exp.padding";
    public static final String IMG_SHUTTER = "exp.shutter";
    public static final String IMG_OVERLAY = "exp.overlay";
    public static final String DICOM_TSUID = "exp.dicom.tsuid";
    public static final String DICOM_ONLY_RAW = "exp.dicom.only.raw";
    public static final String DICOM_NEW_UID = "exp.dicom.new.uid";
    public static final String CD_COMPATIBLE = "exp.cd";
    protected final DicomModel dicomModel;
    protected File outputFolder;
    protected final ExportTree exportTree;
    protected final List<TransferSyntax> transferSyntaxList = List.of(TransferSyntax.NONE, TransferSyntax.EXPLICIT_VR_LE, TransferSyntax.JPEG_LOSSY_8, TransferSyntax.JPEG_LOSSY_12, TransferSyntax.JPEG_LOSSLESS_70, TransferSyntax.JPEGLS_LOSSLESS, TransferSyntax.JPEGLS_NEAR_LOSSLESS, TransferSyntax.JPEG2000_LOSSLESS, TransferSyntax.JPEG2000, TransferSyntax.JPEGXL_LOSSLESS, TransferSyntax.JPEGXL_RECOMPRESSION, TransferSyntax.JPEGXL);
    protected final JComboBox<Format> comboBoxImgFormat;

    public LocalExport(DicomModel dicomModel, CheckTreeModel treeModel) {
        this(Messages.getString("LocalExport.local_dev"), 0, dicomModel, treeModel);
    }

    public LocalExport(String title, int pagePosition, DicomModel dicomModel, CheckTreeModel treeModel) {
        super(title, pagePosition);
        this.dicomModel = dicomModel;
        this.exportTree = new ExportTree(treeModel);
        this.comboBoxImgFormat = new JComboBox<Format>(new DefaultComboBoxModel<Format>(Format.values()));
        this.initGUI();
    }

    public void initGUI() {
        JLabel lblImportAFolder = new JLabel(Messages.getString("LocalExport.exp") + ":");
        JButton btnNewButton = new JButton(Messages.getString("LocalExport.options"));
        btnNewButton.addActionListener(e -> this.showExportingOptions());
        ArrayList<JComponent> list = new ArrayList<JComponent>();
        list.add(lblImportAFolder);
        list.add(this.comboBoxImgFormat);
        list.add(GuiUtils.boxHorizontalStrut((int)5));
        list.add(btnNewButton);
        list.addAll(this.getAdditionalOption());
        this.add(GuiUtils.getFlowLayoutPanel((int)2, (int)0, (JComponent[])((JComponent[])list.toArray(JComponent[]::new))));
        this.add(GuiUtils.boxVerticalStrut((int)5));
        this.exportTree.setBorder(UIManager.getBorder("ScrollPane.border"));
        this.add(this.exportTree);
    }

    protected List<JComponent> getAdditionalOption() {
        return Collections.emptyList();
    }

    protected Properties getPreferences() {
        return LocalPersistence.getProperties();
    }

    protected void showExportingOptions() {
        boolean dicomZip;
        Properties pref = this.getPreferences();
        boolean forceDICOMDIR = Boolean.parseBoolean(pref.getProperty("force.dicomdir", Boolean.FALSE.toString()));
        JCheckBox boxKeepNames = new JCheckBox(Messages.getString("LocalExport.keep_dir"), Boolean.parseBoolean(pref.getProperty(KEEP_INFO_DIR, Boolean.TRUE.toString())));
        Format selected = (Format)((Object)this.comboBoxImgFormat.getSelectedItem());
        boolean bl = dicomZip = Format.DICOM_ZIP == selected;
        if (Format.DICOM == selected || dicomZip) {
            TransferSyntax tsuid;
            if (dicomZip) {
                forceDICOMDIR = true;
            }
            boolean jxl = (tsuid = TransferSyntax.getTransferSyntax((String)pref.getProperty(DICOM_TSUID, TransferSyntax.NONE.name()))) == TransferSyntax.JPEGXL_LOSSLESS || tsuid == TransferSyntax.JPEGXL_RECOMPRESSION || tsuid == TransferSyntax.JPEGXL;
            String qualityKey = jxl ? IMG_JXL_QUALITY : IMG_QUALITY;
            ArrayList<JComponent> options = new ArrayList<JComponent>();
            JSlider slider = LocalExport.buildQualitySlider(pref, qualityKey);
            JCheckBox onlyUncompressed = new JCheckBox(Messages.getString("transcode.only.uncompressed"), Boolean.parseBoolean(pref.getProperty(DICOM_ONLY_RAW, Boolean.TRUE.toString())));
            JComboBox<TransferSyntax> syntaxComboBox = new JComboBox<TransferSyntax>(new Vector<TransferSyntax>(this.transferSyntaxList));
            syntaxComboBox.setSelectedItem(null);
            syntaxComboBox.addItemListener(e -> {
                if (e.getStateChange() == 1) {
                    TransferSyntax t = (TransferSyntax)e.getItem();
                    boolean realTsuid = t != null && t != TransferSyntax.NONE;
                    slider.setEnabled(realTsuid && TransferSyntaxType.isLossyCompression((String)t.getTransferSyntaxUID()) && t != TransferSyntax.JPEGXL_RECOMPRESSION);
                    onlyUncompressed.setEnabled(realTsuid && !DicomUtils.isNative((String)t.getTransferSyntaxUID()));
                }
            });
            syntaxComboBox.setSelectedItem(tsuid);
            JPanel transcodingPanel = GuiUtils.getVerticalBoxLayoutPanel((JComponent[])new JComponent[]{GuiUtils.getFlowLayoutPanel((JComponent[])new JComponent[]{syntaxComboBox}), GuiUtils.getFlowLayoutPanel((JComponent[])new JComponent[]{onlyUncompressed}), slider});
            Border spaceY = GuiUtils.getEmptyBorder((int)10, (int)5, (int)10, (int)5);
            transcodingPanel.setBorder(BorderFactory.createCompoundBorder(spaceY, GuiUtils.getTitledBorder((String)Messages.getString("transcoding"))));
            options.add(transcodingPanel);
            JCheckBox newUidCheckBox = new JCheckBox(Messages.getString("generate.new.unique.identifiers"), Boolean.parseBoolean(pref.getProperty(DICOM_NEW_UID, Boolean.FALSE.toString())));
            options.add(newUidCheckBox);
            JCheckBox box1 = new JCheckBox(Messages.getString("LocalExport.inc_dicomdir"), Boolean.parseBoolean(pref.getProperty(INC_DICOMDIR, Boolean.TRUE.toString())));
            JCheckBox box2 = new JCheckBox(Messages.getString("LocalExport.cd_folders"), Boolean.parseBoolean(pref.getProperty(CD_COMPATIBLE, Boolean.FALSE.toString())));
            box2.setEnabled(box1.isSelected());
            boxKeepNames.setEnabled(!box1.isSelected());
            if (!forceDICOMDIR) {
                options.add(box1);
                options.add(box2);
                options.add(boxKeepNames);
            }
            box1.addActionListener(e -> {
                boxKeepNames.setEnabled(!box1.isSelected());
                box2.setEnabled(box1.isSelected());
            });
            int response = JOptionPane.showOptionDialog(WinUtil.getValidComponent((Component)WinUtil.getParentWindow((Component)((Object)this))), options.toArray(), Messages.getString("LocalExport.export_message"), 2, -1, null, null, null);
            if (response == 0) {
                pref.setProperty(qualityKey, String.valueOf(slider.getValue()));
                pref.setProperty(DICOM_TSUID, StringUtil.getEmptyStringIfNullEnum((Enum)((TransferSyntax)syntaxComboBox.getSelectedItem())));
                pref.setProperty(DICOM_ONLY_RAW, String.valueOf(onlyUncompressed.isSelected()));
                pref.setProperty(DICOM_NEW_UID, String.valueOf(newUidCheckBox.isSelected()));
                if (!forceDICOMDIR) {
                    pref.setProperty(INC_DICOMDIR, String.valueOf(box1.isSelected()));
                    pref.setProperty(KEEP_INFO_DIR, String.valueOf(boxKeepNames.isSelected()));
                    pref.setProperty(CD_COMPATIBLE, String.valueOf(box2.isSelected()));
                }
            }
        } else if (Format.JPEG == selected || Format.PNG == selected || Format.TIFF == selected || Format.JP2 == selected || Format.JPEG_XL == selected) {
            boolean jxl = Format.JPEG_XL == selected;
            boolean lossy = Format.JPEG == selected || jxl;
            String qualityKey = jxl ? IMG_JXL_QUALITY : IMG_QUALITY;
            JSlider slider = null;
            String dicom = "DICOM ";
            JCheckBox paddingCheckBox = new JCheckBox(dicom + ActionW.IMAGE_PIX_PADDING.getTitle(), Boolean.parseBoolean(pref.getProperty(IMG_PIXEL_PADDING, Boolean.TRUE.toString())));
            JCheckBox overlayCheckBox = new JCheckBox(dicom + ActionW.IMAGE_OVERLAY.getTitle(), Boolean.parseBoolean(pref.getProperty(IMG_OVERLAY, Boolean.TRUE.toString())));
            JCheckBox shutterCheckBox = new JCheckBox(dicom + ActionW.IMAGE_SHUTTER.getTitle(), Boolean.parseBoolean(pref.getProperty(IMG_SHUTTER, Boolean.TRUE.toString())));
            ArrayList<JComponent> options = new ArrayList<JComponent>();
            if (lossy) {
                slider = LocalExport.buildQualitySlider(pref, qualityKey);
                options.add(slider);
            }
            JCheckBox preservePixelCheckBox = new JCheckBox(Messages.getString("LocalExport.ch_16"), Boolean.parseBoolean(pref.getProperty(IMG_16_BIT, Boolean.FALSE.toString())));
            preservePixelCheckBox.setEnabled(Format.JPEG != selected);
            options.add(preservePixelCheckBox);
            options.add(paddingCheckBox);
            options.add(shutterCheckBox);
            options.add(overlayCheckBox);
            options.add(boxKeepNames);
            int response = JOptionPane.showOptionDialog(WinUtil.getValidComponent((Component)WinUtil.getParentWindow((Component)((Object)this))), options.toArray(), Messages.getString("LocalExport.export_message"), 2, -1, null, null, null);
            if (response == 0) {
                if (slider != null) {
                    pref.setProperty(qualityKey, String.valueOf(slider.getValue()));
                }
                pref.setProperty(IMG_16_BIT, String.valueOf(preservePixelCheckBox.isSelected()));
                pref.setProperty(IMG_PIXEL_PADDING, String.valueOf(paddingCheckBox.isSelected()));
                pref.setProperty(IMG_SHUTTER, String.valueOf(shutterCheckBox.isSelected()));
                pref.setProperty(IMG_OVERLAY, String.valueOf(overlayCheckBox.isSelected()));
                pref.setProperty(KEEP_INFO_DIR, String.valueOf(boxKeepNames.isSelected()));
            }
        }
    }

    private static JSlider buildQualitySlider(Properties pref, String qualityKey) {
        int defaultQuality = StringUtil.getInt((String)pref.getProperty(qualityKey, null), (int)85);
        JSlider slider = new JSlider(1, 100, defaultQuality);
        TitledBorder titledBorder = new TitledBorder(BorderFactory.createEmptyBorder(), Messages.getString("LocalExport.jpeg_quality") + ": " + slider.getValue(), 4, 0, FontItem.MEDIUM.getFont(), null);
        slider.setBorder(titledBorder);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(false);
        slider.setMajorTickSpacing(10);
        slider.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            ((TitledBorder)slider.getBorder()).setTitle(Messages.getString("LocalExport.jpeg_quality") + ": " + source.getValue());
            slider.repaint();
        });
        return slider;
    }

    public void browseImgFile(Format format) {
        File selectedFile;
        File targetFile;
        Properties pref = this.getPreferences();
        String targetDirectoryPath = pref.getProperty(LAST_DIR, "");
        boolean isSaveFileMode = format == Format.DICOM_ZIP;
        SystemFileChooser fileChooser = new SystemFileChooser(targetDirectoryPath);
        if (isSaveFileMode) {
            fileChooser.setFileSelectionMode(0);
            fileChooser.setAcceptAllFileFilterUsed(false);
            SystemFileChooser.FileNameExtensionFilter filter = new SystemFileChooser.FileNameExtensionFilter("Zip file", new String[]{"zip"});
            fileChooser.addChoosableFileFilter((SystemFileChooser.FileFilter)filter);
            fileChooser.setFileFilter((SystemFileChooser.FileFilter)filter);
        } else {
            fileChooser.setFileSelectionMode(1);
        }
        fileChooser.setMultiSelectionEnabled(false);
        if (StringUtil.hasText((CharSequence)targetDirectoryPath) && (targetFile = new File(targetDirectoryPath)).exists()) {
            if (targetFile.isFile()) {
                fileChooser.setSelectedFile(targetFile);
            } else if (targetFile.isDirectory()) {
                String newExportSelectionName = Messages.getString("LocalExport.newExportSelectionName");
                fileChooser.setSelectedFile(new File(newExportSelectionName));
            }
        }
        if (fileChooser.showSaveDialog((Component)WinUtil.getParentWindow((Component)((Object)this))) != 0 || (selectedFile = fileChooser.getSelectedFile()) == null) {
            this.outputFolder = null;
        } else {
            this.outputFolder = isSaveFileMode ? (".zip".equals(FileUtil.getExtension((String)selectedFile.getName())) ? selectedFile : new File(String.valueOf(selectedFile) + ".zip")) : (selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile());
            pref.setProperty(LAST_DIR, this.outputFolder.isDirectory() ? this.outputFolder.getPath() : this.outputFolder.getParent());
        }
    }

    public void closeAdditionalWindow() {
    }

    public void resetToDefaultValues() {
    }

    @Override
    public void exportDICOM(final CheckTreeModel model, JProgressBar info) throws IOException {
        final Format format = (Format)((Object)this.comboBoxImgFormat.getSelectedItem());
        this.browseImgFile(format);
        if (this.outputFolder != null) {
            final File exportDir = this.outputFolder.getCanonicalFile();
            ExplorerTask<Boolean, String> task = new ExplorerTask<Boolean, String>(this, Messages.getString("LocalExport.exporting"), false){
                final /* synthetic */ LocalExport this$0;
                {
                    LocalExport localExport = this$0;
                    Objects.requireNonNull(localExport);
                    this.this$0 = localExport;
                    super(message, interruptible);
                }

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected Boolean doInBackground() throws Exception {
                    this.this$0.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_START, (Object)this.this$0.dicomModel, null, (Object)this));
                    Properties pref = this.this$0.getPreferences();
                    if (format == Format.DICOM) {
                        this.this$0.writeDicom(this, exportDir, model, pref);
                    } else if (format == Format.DICOM_ZIP) {
                        pref.setProperty(LocalExport.INC_DICOMDIR, Boolean.TRUE.toString());
                        pref.setProperty(LocalExport.CD_COMPATIBLE, Boolean.TRUE.toString());
                        File writeDir = FileUtil.createTempDir((Path)AppProperties.buildAccessibleTempDirectory((String[])new String[]{"tmp", Format.DICOM_ZIP.extension})).toFile();
                        this.this$0.writeDicom(this, writeDir, model, pref);
                        try {
                            ZipUtil.zip((Path)writeDir.toPath(), (Path)exportDir.toPath());
                        }
                        catch (Exception e) {
                            LOGGER.error("Cannot export DICOM ZIP file: {}", (Object)exportDir, (Object)e);
                        }
                        finally {
                            FileUtil.recursiveDelete((Path)writeDir.toPath());
                        }
                    } else {
                        this.this$0.writeOther(this, exportDir, model, format, pref);
                    }
                    return true;
                }

                @Override
                protected void done() {
                    this.this$0.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_STOP, (Object)this.this$0.dicomModel, null, (Object)this));
                }
            };
            task.execute();
        }
    }

    private static String instanceFileName(MediaElement img) {
        String iUid = LocalExport.makeFileIDs((String)TagD.getTagValue((TagReadable)img, (int)524312, String.class));
        Integer instance = (Integer)TagD.getTagValue((TagReadable)img, (int)2097171, Integer.class);
        if (instance != null) {
            String val = instance.toString();
            if (val.length() < 5) {
                char[] chars = new char[5 - val.length()];
                Arrays.fill(chars, '0');
                return new String(chars) + val + "-" + iUid;
            }
            return val + "-" + iUid;
        }
        return iUid;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void writeOther(ExplorerTask task, File exportDir, CheckTreeModel model, Format format, Properties pref) {
        boolean jxl = Format.JPEG_XL == format;
        String qualityKey = jxl ? IMG_JXL_QUALITY : IMG_QUALITY;
        boolean keepNames = Boolean.parseBoolean(pref.getProperty(KEEP_INFO_DIR, Boolean.TRUE.toString()));
        int jpegQuality = StringUtil.getInt((String)pref.getProperty(qualityKey), (int)85);
        boolean padding = Boolean.parseBoolean(pref.getProperty(IMG_PIXEL_PADDING, Boolean.TRUE.toString()));
        boolean overlay = Boolean.parseBoolean(pref.getProperty(IMG_OVERLAY, Boolean.TRUE.toString()));
        boolean shutter = Boolean.parseBoolean(pref.getProperty(IMG_SHUTTER, Boolean.TRUE.toString()));
        boolean img16 = Boolean.parseBoolean(pref.getProperty(IMG_16_BIT, Boolean.FALSE.toString()));
        if (format == Format.JPEG) {
            img16 = false;
        }
        try {
            ExportTree exportTree = this.exportTree;
            synchronized (exportTree) {
                DefaultMutableTreeNode node;
                TreePath[] paths;
                ArrayList<String> seriesGph = new ArrayList<String>();
                for (TreePath treePath : paths = model.getCheckingPaths()) {
                    Series series;
                    MediaSeries s;
                    node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
                    if (!(node.getUserObject() instanceof Series) || !LangUtil.nullToFalse((Boolean)((Boolean)(s = (MediaSeries)node.getUserObject()).getTagValue(TagW.ObjectToSave))) || (series = (Series)s.getTagValue(CheckTreeModel.SourceSeriesForPR)) == null) continue;
                    seriesGph.add((String)series.getTagValue(TagD.get((int)0x20000E)));
                }
                for (TreePath treePath : paths) {
                    if (task.isCancelled()) {
                        return;
                    }
                    node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
                    Object object = node.getUserObject();
                    if (object instanceof MediaElement) {
                        MediaElement dcm = (MediaElement)object;
                        if (object instanceof FileExtractor) {
                            File fileSrc = ((FileExtractor)dcm).getExtractFile();
                            if (fileSrc == null) continue;
                            String instance = LocalExport.instanceFileName(dcm);
                            String path = LocalExport.buildPath(dcm, keepNames, node);
                            File destinationDir = new File(exportDir, path);
                            destinationDir.mkdirs();
                            File destinationFile = new File(destinationDir, instance + FileUtil.getExtension((String)fileSrc.getName()));
                            StreamUtil.copyFile((Path)fileSrc.toPath(), (Path)destinationFile.toPath());
                            continue;
                        }
                    }
                    if (!(object instanceof DicomImageElement)) continue;
                    DicomImageElement img = (DicomImageElement)object;
                    String instance = LocalExport.instanceFileName((MediaElement)img);
                    String path = LocalExport.buildPath((MediaElement)img, keepNames, node);
                    File destinationDir = new File(exportDir, path);
                    destinationDir.mkdirs();
                    SimpleOpManager manager = img.buildSimpleOpManager(img16, padding, shutter, overlay, 1.0);
                    Optional<PlanarImage> image = manager.getFirstNodeInputImage();
                    if (image.isPresent()) {
                        PlanarImage rimage = manager.process().orElse(null);
                        if (rimage == null) {
                            rimage = (PlanarImage)image.get();
                        }
                        boolean mustBeReleased = !Objects.equals(rimage, image.get());
                        image = Optional.of(rimage);
                        File destinationFile = new File(destinationDir, instance + "." + format.extension);
                        if (format == Format.PNG) {
                            ImageIOHandler.writePNG((Mat)image.get().toMat(), (Path)destinationFile.toPath());
                        } else {
                            MatOfInt map = new MatOfInt();
                            if (format == Format.JPEG) {
                                map.fromArray(new int[]{1, jpegQuality});
                            } else if (format == Format.JPEG_XL) {
                                map.fromArray(new int[]{640, jpegQuality});
                            }
                            ImageIOHandler.writeImage((Mat)image.get().toMat(), (Path)destinationFile.toPath(), (MatOfInt)map);
                        }
                        if (mustBeReleased) {
                            ImageConversion.releasePlanarImage((PlanarImage)image.get());
                        }
                        if (!seriesGph.contains(img.getTagValue(TagD.get((int)0x20000E)))) continue;
                        XmlSerializer.writePresentation((ImageElement)img, (File)destinationFile);
                        continue;
                    }
                    LOGGER.error("Cannot export DICOM file to {}: {}", (Object)format, img.getFileCache().getOriginalFile().orElse(null));
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Cannot extract media from DICOM", (Throwable)e);
        }
    }

    private static Attributes getUIDs(DefaultAttributeEditor editor, MediaElement dcm) {
        Attributes uidTags = new Attributes();
        uidTags.setString(0x100020, VR.LO, (String)dcm.getTagValue(TagW.PatientPseudoUID));
        uidTags.setString(0x20000D, VR.UI, (String)TagD.getTagValue((TagReadable)dcm, (int)0x20000D, String.class));
        uidTags.setString(0x20000E, VR.UI, (String)TagD.getTagValue((TagReadable)dcm, (int)0x20000E, String.class));
        uidTags.setString(524312, VR.UI, (String)TagD.getTagValue((TagReadable)dcm, (int)524312, String.class));
        editor.apply(uidTags, null);
        return uidTags;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void writeDicom(ExplorerTask task, File exportDir, CheckTreeModel model, Properties pref) throws IOException {
        boolean realTsuid;
        TransferSyntax tsuid = TransferSyntax.getTransferSyntax((String)pref.getProperty(DICOM_TSUID, TransferSyntax.NONE.name()));
        boolean jxl = tsuid == TransferSyntax.JPEGXL_LOSSLESS || tsuid == TransferSyntax.JPEGXL_RECOMPRESSION || tsuid == TransferSyntax.JPEGXL;
        String qualityKey = jxl ? IMG_JXL_QUALITY : IMG_QUALITY;
        int jpegQuality = StringUtil.getInt((String)pref.getProperty(qualityKey), (int)85);
        int compressionRatio = 100 - jpegQuality;
        boolean newUID = Boolean.parseBoolean(pref.getProperty(DICOM_NEW_UID, Boolean.FALSE.toString()));
        boolean onlyRaw = Boolean.parseBoolean(pref.getProperty(DICOM_ONLY_RAW, Boolean.TRUE.toString()));
        boolean bl = realTsuid = tsuid != TransferSyntax.NONE;
        if (realTsuid && DicomUtils.isNative((String)tsuid.getTransferSyntaxUID())) {
            onlyRaw = false;
        }
        DefaultAttributeEditor editor = new DefaultAttributeEditor(newUID, null);
        boolean writeDicomdir = Boolean.parseBoolean(pref.getProperty(INC_DICOMDIR, Boolean.TRUE.toString()));
        boolean keepNames = !writeDicomdir && Boolean.parseBoolean(pref.getProperty(KEEP_INFO_DIR, Boolean.TRUE.toString()));
        boolean cdCompatible = Boolean.parseBoolean(pref.getProperty(CD_COMPATIBLE, Boolean.FALSE.toString()));
        try (DicomDirWriter writer = null;){
            Serializable dcmdirFile;
            if (writeDicomdir) {
                dcmdirFile = new File(exportDir, "DICOMDIR");
                writer = DicomDirLoader.open((File)dcmdirFile);
            }
            dcmdirFile = this.exportTree;
            synchronized (dcmdirFile) {
                TreePath[] paths;
                ArrayList<String> uids = new ArrayList<String>();
                for (TreePath treePath : paths = model.getCheckingPaths()) {
                    Series series;
                    MediaSeries s;
                    String iuid;
                    Object uidTags;
                    if (task.isCancelled()) {
                        return;
                    }
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
                    Object object = node.getUserObject();
                    if (object instanceof DicomImageElement) {
                        DicomImageElement img = (DicomImageElement)object;
                        uidTags = LocalExport.getUIDs(editor, (MediaElement)img);
                        iuid = uidTags.getString(524312);
                        int index = uids.indexOf(iuid);
                        if (index != -1) continue;
                        uids.add(iuid);
                        if (!keepNames) {
                            iuid = LocalExport.makeFileIDs(iuid);
                        }
                        String path = LocalExport.buildPath((MediaElement)img, keepNames, cdCompatible, node, (Attributes)uidTags);
                        File destinationDir = new File(exportDir, path);
                        destinationDir.mkdirs();
                        List<AttributeEditor> dicomEditors = LocalExport.getAttributeEditors(editor);
                        File destinationFile = new File(destinationDir, iuid);
                        DicomElement.DicomExportParameters dicomExportParameters = new DicomElement.DicomExportParameters(tsuid, onlyRaw, dicomEditors, jpegQuality, compressionRatio);
                        Attributes attributes = img.saveToFile(destinationFile, dicomExportParameters);
                        if (attributes == null) continue;
                        if (attributes.isEmpty()) {
                            attributes = img.getMediaReader().getDicomObject();
                        }
                        LocalExport.writeInDicomDir(writer, attributes, node, iuid, destinationFile);
                        continue;
                    }
                    uidTags = node.getUserObject();
                    if (uidTags instanceof DicomElement) {
                        DicomElement dcm = (DicomElement)uidTags;
                        uidTags = LocalExport.getUIDs(editor, (MediaElement)dcm);
                        iuid = uidTags.getString(524312);
                        if (!keepNames) {
                            iuid = LocalExport.makeFileIDs(iuid);
                        }
                        String path = LocalExport.buildPath((MediaElement)dcm, keepNames, cdCompatible, node, (Attributes)uidTags);
                        File destinationDir = new File(exportDir, path);
                        destinationDir.mkdirs();
                        File destinationFile = new File(destinationDir, iuid);
                        DicomElement.DicomExportParameters dicomExportParameters = new DicomElement.DicomExportParameters(null, onlyRaw, LocalExport.getAttributeEditors(editor), jpegQuality, compressionRatio);
                        Attributes attributes = dcm.saveToFile(destinationFile, dicomExportParameters);
                        if (attributes == null) continue;
                        if (attributes.isEmpty()) {
                            attributes = dcm.getMediaReader().getDicomObject();
                        }
                        LocalExport.writeInDicomDir(writer, attributes, node, iuid, destinationFile);
                        continue;
                    }
                    if (!(node.getUserObject() instanceof Series) || !LangUtil.nullToFalse((Boolean)((Boolean)(s = (MediaSeries)node.getUserObject()).getTagValue(TagW.ObjectToSave))) || (series = (Series)s.getTagValue(CheckTreeModel.SourceSeriesForPR)) == null) continue;
                    String seriesInstanceUID = UIDUtils.createUID();
                    for (MediaElement dcm : series.getMedias(null, null)) {
                        String path;
                        String outputFile;
                        GraphicModel grModel = (GraphicModel)dcm.getTagValue(TagW.PresentationModel);
                        if (grModel == null || !grModel.hasSerializableGraphics() || (outputFile = LocalExport.buildAndWritePR(dcm, keepNames, new File(exportDir, path = LocalExport.buildPath(dcm, keepNames, cdCompatible, node, LocalExport.getUIDs(editor, dcm))), writer, node, seriesInstanceUID)) == null) continue;
                        File f = new File(outputFile);
                        DicomModel.LOADING_EXECUTOR.execute(new LoadLocalDicom(new File[]{f}, false, (DataExplorerModel)this.dicomModel, HangingProtocols.OpeningViewer.ALL_PATIENTS));
                    }
                }
            }
        }
        {
            return;
        }
    }

    private static List<AttributeEditor> getAttributeEditors(DefaultAttributeEditor editor) {
        if (editor.isGenerateUIDs() || editor.getTagToOverride() != null) {
            return List.of(editor);
        }
        return Collections.emptyList();
    }

    public static String buildAndWritePR(MediaElement img, boolean keepNames, File destinationDir, DicomDirWriter writer, DefaultMutableTreeNode node, String seriesInstanceUID) {
        GraphicModel grModel;
        Attributes imgAttributes;
        MediaReader mediaReader = img.getMediaReader();
        if (mediaReader instanceof DcmMediaReader) {
            DcmMediaReader reader = (DcmMediaReader)mediaReader;
            v0 = reader.getDicomObject();
        } else {
            v0 = imgAttributes = null;
        }
        if (imgAttributes != null && (grModel = (GraphicModel)img.getTagValue(TagW.PresentationModel)) != null && grModel.hasSerializableGraphics()) {
            String prUid = UIDUtils.createUID();
            File outputFile = new File(destinationDir, keepNames ? prUid : LocalExport.makeFileIDs(prUid));
            destinationDir.mkdirs();
            Attributes prAttributes = DicomPrSerializer.writePresentation(grModel, img instanceof DicomImageElement ? (DicomImageElement)img : null, outputFile, seriesInstanceUID, prUid);
            if (prAttributes != null) {
                try {
                    LocalExport.writeInDicomDir(writer, prAttributes, node, outputFile.getName(), outputFile);
                    return outputFile.getPath();
                }
                catch (IOException e) {
                    LOGGER.error("Writing DICOMDIR", (Throwable)e);
                }
            }
        }
        return null;
    }

    public static String buildPath(MediaElement img, boolean preservePath, boolean cdCompatible, DefaultMutableTreeNode node, Attributes uidTags) {
        StringBuilder buffer = new StringBuilder();
        if (preservePath) {
            TreeNode[] objects = node.getPath();
            if (objects.length > 2) {
                for (int i = 1; i < objects.length - 1; ++i) {
                    buffer.append(LocalExport.buildFolderName(objects[i].toString(), 30));
                    buffer.append(File.separator);
                }
            }
        } else {
            if (cdCompatible) {
                buffer.append("DICOM");
                buffer.append(File.separator);
            }
            String patientUID = null;
            String studyUID = null;
            String seriesUID = null;
            if (uidTags != null) {
                patientUID = uidTags.getString(0x100020);
                studyUID = uidTags.getString(0x20000D);
                seriesUID = uidTags.getString(0x20000E);
            }
            if (!StringUtil.hasText(patientUID)) {
                patientUID = (String)img.getTagValue(TagW.PatientPseudoUID);
            }
            if (!StringUtil.hasText(studyUID)) {
                studyUID = (String)TagD.getTagValue((TagReadable)img, (int)0x20000D, String.class);
            }
            if (!StringUtil.hasText(seriesUID)) {
                seriesUID = (String)TagD.getTagValue((TagReadable)img, (int)0x20000E, String.class);
            }
            buffer.append(LocalExport.makeFileIDs(patientUID));
            buffer.append(File.separator);
            buffer.append(LocalExport.makeFileIDs(studyUID));
            buffer.append(File.separator);
            buffer.append(LocalExport.makeFileIDs(seriesUID));
        }
        return buffer.toString();
    }

    public static String buildPath(MediaElement img, boolean keepNames, DefaultMutableTreeNode node) {
        StringBuilder buffer = new StringBuilder();
        if (keepNames) {
            TreeNode[] objects = node.getPath();
            if (objects.length > 3) {
                buffer.append(LocalExport.buildFolderName(objects[1].toString(), 30));
                buffer.append(File.separator);
                buffer.append(LocalExport.buildFolderName(objects[2].toString(), 30));
                buffer.append(File.separator);
                buffer.append(LocalExport.buildFolderName(objects[3].toString(), 25));
                buffer.append('-');
                buffer.append(LocalExport.makeFileIDs((String)TagD.getTagValue((TagReadable)img, (int)0x20000E, String.class)));
            }
        } else {
            buffer.append(LocalExport.makeFileIDs((String)img.getTagValue(TagW.PatientPseudoUID)));
            buffer.append(File.separator);
            buffer.append(LocalExport.makeFileIDs((String)TagD.getTagValue((TagReadable)img, (int)0x20000D, String.class)));
            buffer.append(File.separator);
            buffer.append(LocalExport.makeFileIDs((String)TagD.getTagValue((TagReadable)img, (int)0x20000E, String.class)));
        }
        return buffer.toString();
    }

    private static String buildFolderName(String str, int length) {
        String value = FileUtil.getValidFileNameWithoutHTML((String)str);
        return (value = StringUtil.getTruncatedString((String)value, (int)length, (StringUtil.Suffix)StringUtil.Suffix.UNDERSCORE).trim()).endsWith(".") ? value.substring(0, value.length() - 1) : value;
    }

    private static boolean writeInDicomDir(DicomDirWriter writer, Attributes dataset, DefaultMutableTreeNode node, String iuid, File destinationFile) throws IOException {
        if (writer != null && dataset != null) {
            Attributes fmi = dataset.createFileMetaInformation("1.2.840.10008.1.2");
            String miuid = fmi.getString(131075, null);
            String pid = dataset.getString(0x100020, null);
            String styuid = dataset.getString(0x20000D, null);
            String seruid = dataset.getString(0x20000E, null);
            if (styuid != null && seruid != null) {
                Attributes seriesRec;
                Attributes studyRec;
                Attributes patRec;
                if (pid == null) {
                    pid = styuid;
                    dataset.setString(0x100020, VR.LO, pid);
                }
                if ((patRec = writer.findPatientRecord(new String[]{pid})) == null) {
                    patRec = DicomDirLoader.RecordFactory.createRecord(RecordType.PATIENT, null, dataset, null, null);
                    writer.addRootDirectoryRecord(patRec);
                }
                if ((studyRec = writer.findStudyRecord(patRec, new String[]{styuid})) == null) {
                    studyRec = DicomDirLoader.RecordFactory.createRecord(RecordType.STUDY, null, dataset, null, null);
                    writer.addLowerDirectoryRecord(patRec, studyRec);
                }
                if ((seriesRec = writer.findSeriesRecord(studyRec, new String[]{seruid})) == null) {
                    DicomSeries dicomSeries;
                    DicomImageElement midImage;
                    Attributes iconItem;
                    DefaultMutableTreeNode treeNode;
                    Object object;
                    TreeNode treeNode2;
                    seriesRec = DicomDirLoader.RecordFactory.createRecord(RecordType.SERIES, null, dataset, null, null);
                    if (seriesRec != null && (treeNode2 = node.getParent()) instanceof DefaultMutableTreeNode && (object = (treeNode = (DefaultMutableTreeNode)treeNode2).getUserObject()) instanceof DicomSeries && (iconItem = LocalExport.mkIconItem(midImage = (DicomImageElement)(dicomSeries = (DicomSeries)object).getMedia(MediaSeries.MEDIA_POSITION.MIDDLE, null, null))) != null) {
                        seriesRec.newSequence(0x880200, 1).add(iconItem);
                    }
                    writer.addLowerDirectoryRecord(studyRec, seriesRec);
                }
                if (writer.findLowerInstanceRecord(seriesRec, false, new String[]{iuid}) == null) {
                    Attributes instRec = DicomDirLoader.RecordFactory.createRecord(dataset, fmi, writer.toFileIDs(destinationFile));
                    writer.addLowerDirectoryRecord(seriesRec, instRec);
                }
            } else if (writer.findRootInstanceRecord(false, new String[]{miuid}) == null) {
                Attributes instRec = DicomDirLoader.RecordFactory.createRecord(dataset, fmi, writer.toFileIDs(destinationFile));
                writer.addRootDirectoryRecord(instRec);
            }
        }
        return true;
    }

    public static String makeFileIDs(String uid) {
        if (uid != null) {
            return Integer.toHexString(uid.hashCode());
        }
        return null;
    }

    public static Attributes mkIconItem(DicomImageElement image) {
        if (image == null) {
            return null;
        }
        ImageCV thumbnail = null;
        PlanarImage imgPl = image.getImage(null);
        if (imgPl != null) {
            SimpleOpManager manager = new SimpleOpManager();
            manager.addImageOperationAction((ImageOpNode)new WindowAndPresetsOp());
            manager.setParamValue(WindowOp.OP_NAME, "img.element", (Object)image);
            manager.setParamValue(WindowOp.OP_NAME, ActionW.IMAGE_PIX_PADDING.cmd(), (Object)true);
            manager.setParamValue(WindowOp.OP_NAME, ActionW.DEFAULT_PRESET.cmd(), (Object)true);
            ZoomOp rectifyZoomOp = image.getRectifyAspectRatioZoomOp();
            if (rectifyZoomOp != null) {
                manager.addImageOperationAction((ImageOpNode)rectifyZoomOp);
            }
            manager.setFirstNode(imgPl);
            thumbnail = ImageIOHandler.buildThumbnail((PlanarImage)manager.process().orElse(null), (Dimension)new Dimension(128, 128), (boolean)true);
        }
        if (thumbnail == null) {
            return null;
        }
        int w = thumbnail.width();
        int h = thumbnail.height();
        String pmi = (String)TagD.getTagValue((TagReadable)image, (int)2621444, String.class);
        if (thumbnail.channels() >= 3) {
            pmi = "PALETTE COLOR";
        }
        byte[] iconPixelData = new byte[w * h];
        Attributes iconItem = new Attributes();
        if ("PALETTE COLOR".equals(pmi)) {
            BufferedImage bi = ImageConversion.convertTo((RenderedImage)ImageConversion.toBufferedImage(thumbnail), (int)13);
            IndexColorModel cm = (IndexColorModel)bi.getColorModel();
            int[] lutDesc = new int[]{cm.getMapSize(), 0, 8};
            byte[] r = new byte[lutDesc[0]];
            byte[] g = new byte[lutDesc[0]];
            byte[] b = new byte[lutDesc[0]];
            cm.getReds(r);
            cm.getGreens(g);
            cm.getBlues(b);
            iconItem.setInt(2625793, VR.US, lutDesc);
            iconItem.setInt(2625794, VR.US, lutDesc);
            iconItem.setInt(2625795, VR.US, lutDesc);
            iconItem.setBytes(2626049, VR.OW, r);
            iconItem.setBytes(2626050, VR.OW, g);
            iconItem.setBytes(2626051, VR.OW, b);
            WritableRaster raster = bi.getRaster();
            int i = 0;
            for (int y = 0; y < h; ++y) {
                int x = 0;
                while (x < w) {
                    iconPixelData[i] = (byte)raster.getSample(x, y, 0);
                    ++x;
                    ++i;
                }
            }
        } else {
            pmi = "MONOCHROME2";
            thumbnail.get(0, 0, iconPixelData);
        }
        iconItem.setString(2621444, VR.CS, pmi);
        iconItem.setInt(2621456, VR.US, new int[]{h});
        iconItem.setInt(2621457, VR.US, new int[]{w});
        iconItem.setInt(0x280002, VR.US, new int[]{1});
        iconItem.setInt(2621696, VR.US, new int[]{8});
        iconItem.setInt(2621697, VR.US, new int[]{8});
        iconItem.setInt(2621698, VR.US, new int[]{7});
        iconItem.setBytes(2145386512, VR.OW, iconPixelData);
        return iconItem;
    }

    public static enum Format {
        DICOM("DICOM", "dcm"),
        DICOM_ZIP("DICOM ZIP", "zip"),
        JPEG("JPEG Lossy", "jpg"),
        JPEG_XL("JPEG XL", "jxl"),
        PNG("PNG", "png"),
        TIFF("TIFF", "tif"),
        JP2("JPEG 2000", "jp2");

        private final String title;
        private final String extension;

        private Format(String title, String extension) {
            this.title = title;
            this.extension = extension;
        }

        public String toString() {
            return this.title;
        }

        public String getTitle() {
            return this.title;
        }

        public String getExtension() {
            return this.extension;
        }
    }
}

