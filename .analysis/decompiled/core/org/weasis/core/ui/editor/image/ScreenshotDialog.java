/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemFileChooser
 *  com.formdev.flatlaf.util.SystemFileChooser$FileFilter
 *  com.formdev.flatlaf.util.SystemFileChooser$FileNameExtensionFilter
 *  net.miginfocom.swing.MigLayout
 *  org.opencv.core.Mat
 *  org.opencv.core.MatOfInt
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.ImageIOHandler
 */
package org.weasis.core.ui.editor.image;

import com.formdev.flatlaf.util.SystemFileChooser;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.RenderedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageTransferHandler;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewTransferHandler;
import org.weasis.core.ui.util.ColorLayerUI;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.ImageIOHandler;

public class ScreenshotDialog<I extends ImageElement>
extends JDialog {
    public static final String P_LAST_DIR = "screenshot.last.dir";
    static final String DICOM = "DICOM";
    private final ViewCanvas<I> viewCanvas;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final JRadioButton viewRadio = new JRadioButton(Messages.getString("current.view"));
    private final JRadioButton imageRadio = new JRadioButton(Messages.getString("original.image"));
    private final JCheckBox paddingCheckBox = new JCheckBox("DICOM " + ActionW.IMAGE_PIX_PADDING.getTitle(), true);
    private final JCheckBox overlayCheckBox = new JCheckBox("DICOM " + ActionW.IMAGE_OVERLAY.getTitle(), true);
    private final JCheckBox shutterCheckBox = new JCheckBox("DICOM " + ActionW.IMAGE_SHUTTER.getTitle(), true);
    private final JCheckBox preservePixelCheckBox;
    private final JCheckBox anonymize;
    private final JLabel labelSize;
    private final JSpinner spinner;
    private final JLabel labelSizePix;

    public ScreenshotDialog(Window parent, String title, ViewCanvas<I> viewCanvas) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.preservePixelCheckBox = new JCheckBox(Messages.getString("preserve.16.bit") + " (" + String.format(Messages.getString("not.supported.with.s"), Format.JPEG.title) + ")", false);
        this.anonymize = new JCheckBox(Messages.getString("anonymize"), true);
        this.labelSize = new JLabel(Messages.getString("size") + " (%):");
        this.spinner = new JSpinner();
        this.labelSizePix = new JLabel();
        this.setDefaultCloseOperation(2);
        this.spinner.setModel(new SpinnerNumberModel(100, 5, 200, 5));
        this.spinner.getModel().addChangeListener(e -> this.setImageSize((Integer)((SpinnerNumberModel)e.getSource()).getValue()));
        this.viewCanvas = Objects.requireNonNull(viewCanvas);
        this.initComponents();
        this.pack();
    }

    private void setImageSize(int val) {
        PlanarImage image;
        Object imageElement = this.viewCanvas.getImage();
        PlanarImage planarImage = image = imageElement == null ? null : ((ImageElement)imageElement).getImage(null);
        if (image != null) {
            float ratio = (float)val / 100.0f;
            this.labelSizePix.setText(String.format("(%dx%d px)", Math.round((float)image.width() * ratio), Math.round((float)image.height() * ratio)));
        }
    }

    private void initComponents() {
        JPanel panel = GuiUtils.getVerticalBoxLayoutPanel(new JComponent[0]);
        panel.setBorder(GuiUtils.getEmptyBorder(15));
        this.buttonGroup.add(this.viewRadio);
        this.buttonGroup.add(this.imageRadio);
        this.viewRadio.setSelected(true);
        this.setImageRadioAction(false);
        this.viewRadio.addActionListener(e -> this.setImageRadioAction(false));
        this.imageRadio.addActionListener(e -> this.setImageRadioAction(true));
        this.setImageSize(100);
        boolean dicom = this.viewCanvas.getPanner() == null;
        panel.add(GuiUtils.getFlowLayoutPanel(3, 0, 2, this.viewRadio));
        panel.add(GuiUtils.getHorizontalBoxLayoutPanel(this.buildViewPanel(dicom)));
        panel.add(GuiUtils.getFlowLayoutPanel(3, 0, 2, this.imageRadio));
        panel.add(GuiUtils.getHorizontalBoxLayoutPanel(this.buildImagePanel(dicom)));
        JButton clipButton = new JButton(Messages.getString("clipboard"));
        clipButton.addActionListener(e -> {
            if (this.viewRadio.isSelected()) {
                ViewTransferHandler imageTransferHandler = new ViewTransferHandler(this.anonymize.isSelected());
                imageTransferHandler.exportToClipboard(this.viewCanvas.getJComponent(), Toolkit.getDefaultToolkit().getSystemClipboard(), 1);
            } else {
                Object img = this.viewCanvas.getImage();
                if (img != null) {
                    double ratio = (double)((Integer)this.spinner.getValue()).intValue() / 100.0;
                    SimpleOpManager manager = ((ImageElement)img).buildSimpleOpManager(this.preservePixelCheckBox.isSelected(), this.paddingCheckBox.isSelected(), this.shutterCheckBox.isSelected(), this.overlayCheckBox.isSelected(), ratio);
                    ImageTransferHandler imageTransferHandler = new ImageTransferHandler(manager);
                    imageTransferHandler.exportToClipboard(this.viewCanvas.getJComponent(), Toolkit.getDefaultToolkit().getSystemClipboard(), 1);
                }
            }
        });
        JButton saveButton = new JButton(Messages.getString("save"));
        saveButton.addActionListener(e -> {
            boolean mustBeReleased = false;
            ImageCV result = null;
            if (this.viewRadio.isSelected()) {
                ViewCanvas<I> patt0$temp = this.viewCanvas;
                if (patt0$temp instanceof DefaultView2d) {
                    DefaultView2d view2DPane = (DefaultView2d)patt0$temp;
                    RenderedImage imgP = ViewTransferHandler.createComponentImage(view2DPane, this.anonymize.isSelected());
                    result = ImageConversion.toMat((RenderedImage)imgP);
                }
            } else {
                Object img = this.viewCanvas.getImage();
                if (img != null) {
                    double ratio = (double)((Integer)this.spinner.getValue()).intValue() / 100.0;
                    SimpleOpManager manager = ((ImageElement)img).buildSimpleOpManager(this.preservePixelCheckBox.isSelected(), this.paddingCheckBox.isSelected(), this.shutterCheckBox.isSelected(), this.overlayCheckBox.isSelected(), ratio);
                    Optional<PlanarImage> inputImage = manager.getFirstNodeInputImage();
                    if (inputImage.isPresent()) {
                        Optional<PlanarImage> rimage = manager.process();
                        if (rimage.isEmpty()) {
                            rimage = inputImage;
                        }
                        mustBeReleased = !Objects.equals(rimage, inputImage);
                        result = rimage.orElse(null);
                    }
                }
            }
            this.saveImageFile((PlanarImage)result, mustBeReleased);
        });
        this.getRootPane().setDefaultButton(saveButton);
        saveButton.addActionListener(evt -> this.doClose());
        panel.add(GuiUtils.getFlowLayoutPanel(4, 15, 10, saveButton, clipButton));
        panel.add(GuiUtils.boxYLastElement(5));
        this.setContentPane(panel);
    }

    private void saveImageFile(PlanarImage image, boolean mustBeReleased) {
        if (image != null) {
            WProperties localPersistence = GuiUtils.getUICore().getLocalPersistence();
            String targetDirectoryPath = localPersistence.getProperty(P_LAST_DIR, "");
            SystemFileChooser fileChooser = new SystemFileChooser(targetDirectoryPath);
            fileChooser.setFileSelectionMode(0);
            fileChooser.setAcceptAllFileFilterUsed(false);
            SystemFileChooser.FileNameExtensionFilter filter = new SystemFileChooser.FileNameExtensionFilter(Format.PNG.title, new String[]{Format.PNG.extension});
            fileChooser.addChoosableFileFilter((SystemFileChooser.FileFilter)new SystemFileChooser.FileNameExtensionFilter(Format.JP2.title, new String[]{Format.JP2.extension}));
            fileChooser.addChoosableFileFilter((SystemFileChooser.FileFilter)new SystemFileChooser.FileNameExtensionFilter(Format.JPEG.title, new String[]{Format.JPEG.extension}));
            fileChooser.addChoosableFileFilter((SystemFileChooser.FileFilter)new SystemFileChooser.FileNameExtensionFilter(Format.JPEG_XL.title, new String[]{Format.JPEG_XL.extension}));
            fileChooser.addChoosableFileFilter((SystemFileChooser.FileFilter)filter);
            fileChooser.addChoosableFileFilter((SystemFileChooser.FileFilter)new SystemFileChooser.FileNameExtensionFilter(Format.TIFF.title, new String[]{Format.TIFF.extension}));
            fileChooser.setFileFilter((SystemFileChooser.FileFilter)filter);
            if (fileChooser.showSaveDialog(null) == 0 && fileChooser.getSelectedFile() != null) {
                File file = fileChooser.getSelectedFile();
                filter = (SystemFileChooser.FileNameExtensionFilter)fileChooser.getFileFilter();
                String extension = filter == null ? Format.PNG.extension : filter.getExtensions()[0];
                String extFile = "." + extension;
                String filename = file.getName().endsWith(extFile) ? file.getPath() : file.getPath() + extFile;
                File destinationFile = new File(filename);
                if (Format.PNG.extension.equals(extension)) {
                    ImageIOHandler.writePNG((Mat)image.toMat(), (Path)destinationFile.toPath());
                } else {
                    MatOfInt map = new MatOfInt();
                    if (Format.JPEG.extension.equals(extension)) {
                        map.fromArray(new int[]{1, 90});
                    } else if (Format.JPEG_XL.extension.equals(extension)) {
                        int quality = this.preservePixelCheckBox.isSelected() ? 100 : 90;
                        map.fromArray(new int[]{640, quality});
                    }
                    ImageIOHandler.writeImage((Mat)image.toMat(), (Path)destinationFile.toPath(), (MatOfInt)map);
                }
                if (mustBeReleased) {
                    ImageConversion.releasePlanarImage((PlanarImage)image);
                }
                if (destinationFile.canRead()) {
                    localPersistence.setProperty(P_LAST_DIR, destinationFile.getParentFile().getPath());
                }
            }
        }
    }

    private void setImageRadioAction(boolean enable) {
        this.labelSize.setEnabled(enable);
        this.spinner.setEnabled(enable);
        this.labelSizePix.setEnabled(enable);
        this.preservePixelCheckBox.setEnabled(enable);
        this.paddingCheckBox.setEnabled(enable);
        this.shutterCheckBox.setEnabled(enable);
        this.overlayCheckBox.setEnabled(enable);
        this.anonymize.setEnabled(!enable);
    }

    private JPanel buildViewPanel(boolean dicom) {
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout((LayoutManager)new MigLayout("insets 0 25lp 30lp 10lp, fillx", "[grow 0]"));
        if (dicom) {
            dataPanel.add((Component)this.anonymize, "newline");
        }
        return dataPanel;
    }

    private JPanel buildImagePanel(boolean dicom) {
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout((LayoutManager)new MigLayout("insets 0 25lp 30lp 10lp, fillx", "[grow 0]"));
        dataPanel.add((Component)GuiUtils.getFlowLayoutPanel(this.labelSize, this.spinner, this.labelSizePix), "newline");
        dataPanel.add((Component)this.preservePixelCheckBox, "newline");
        if (dicom) {
            dataPanel.add((Component)this.paddingCheckBox, "newline");
            dataPanel.add((Component)this.shutterCheckBox, "newline");
            dataPanel.add((Component)this.overlayCheckBox, "newline");
        }
        return dataPanel;
    }

    private void doClose() {
        this.dispose();
    }

    public static <E extends ImageElement> void showDialog(ViewCanvas<E> selView) {
        if (selView != null) {
            ColorLayerUI layer = ColorLayerUI.createTransparentLayerUI(selView.getJComponent());
            ScreenshotDialog<E> dialog = new ScreenshotDialog<E>(SwingUtilities.getWindowAncestor(selView.getJComponent()), ActionW.EXPORT_VIEW.getTitle(), selView);
            ColorLayerUI.showCenterScreen(dialog, layer);
        }
    }

    public static enum Format {
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

