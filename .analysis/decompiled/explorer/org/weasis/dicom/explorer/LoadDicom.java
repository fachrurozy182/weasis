/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Thumbnail
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.DicomMediaIO
 *  org.weasis.dicom.codec.DicomMediaIO$ResultContainer
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.DicomSpecialElement
 *  org.weasis.dicom.codec.DicomSpecialElementFactory
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 */
package org.weasis.dicom.explorer;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.DicomSpecialElementFactory;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.exp.ExplorerTask;

public abstract class LoadDicom
extends ExplorerTask<Boolean, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadDicom.class);
    public static final String SHOW_UNSUPPORTED_SOP_CLASS_INFO = "weasis.show.unsupported.sop.class.info";
    protected final AtomicInteger errors = new AtomicInteger(0);
    protected final AtomicInteger unsupported = new AtomicInteger(0);
    protected final DicomModel dicomModel;
    protected final PluginOpeningStrategy openingStrategy;

    protected LoadDicom(DataExplorerModel explorerModel, boolean interruptible, HangingProtocols.OpeningViewer openingMode) {
        this(explorerModel, interruptible, new PluginOpeningStrategy(openingMode));
    }

    protected LoadDicom(DataExplorerModel explorerModel, boolean interruptible, PluginOpeningStrategy openingStrategy) {
        super(Messages.getString("DicomExplorer.loading"), interruptible);
        if (!(explorerModel instanceof DicomModel)) {
            throw new IllegalArgumentException("invalid DICOM model");
        }
        this.dicomModel = (DicomModel)explorerModel;
        this.openingStrategy = Optional.ofNullable(openingStrategy).orElseGet(() -> new PluginOpeningStrategy(HangingProtocols.OpeningViewer.ALL_PATIENTS));
    }

    protected void startLoadingEvent() {
        if (this.openingStrategy.isFullImportSession()) {
            this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_START, (Object)this.dicomModel, null, (Object)this));
        }
    }

    @Override
    protected void done() {
        this.openingStrategy.reset();
        if (this.openingStrategy.isFullImportSession()) {
            int nbUnsupported;
            this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_STOP, (Object)this.dicomModel, null, (Object)this));
            LOGGER.info("End of loading DICOM locally");
            int nbErrors = this.errors.get();
            if (nbErrors > 0) {
                JOptionPane.showMessageDialog(GuiUtils.getUICore().getApplicationWindow(), this.getErrorPanel(nbErrors), Messages.getString("DicomImport.imp_dicom"), 0);
            }
            if ((nbUnsupported = this.unsupported.get()) > 0 && GuiUtils.getUICore().getSystemPreferences().getBooleanProperty(SHOW_UNSUPPORTED_SOP_CLASS_INFO, true)) {
                JCheckBox dontShowAgain = new JCheckBox(Messages.getString("dicom.unsupported.dont.show.again"));
                JPanel panel = GuiUtils.getVerticalBoxLayoutPanel((JComponent[])new JComponent[]{this.getUnsupportedPanel(nbUnsupported), dontShowAgain});
                JOptionPane.showMessageDialog(GuiUtils.getUICore().getApplicationWindow(), panel, Messages.getString("DicomImport.imp_dicom"), 1);
                if (dontShowAgain.isSelected()) {
                    GuiUtils.getUICore().getSystemPreferences().putBooleanProperty(SHOW_UNSUPPORTED_SOP_CLASS_INFO, false);
                }
            }
        }
    }

    private JTextPane getErrorPanel(int nbErrors) {
        String message = Messages.getString("dicom.file.cannot.be.read").formatted(new Object[]{nbErrors});
        String logOutput = Messages.getString("check.log.output");
        String html = "  <P>\n  %s<br>\n  <a href=\"%s\">%s</a>\n  </P>\n".formatted(new Object[]{message, GuiUtils.getUICore().getSystemPreferences().getProperty("weasis.help.online") + "logging", logOutput});
        JTextPane jTextPane1 = GuiUtils.getPanelWithHyperlink((String)html);
        jTextPane1.setBorder(new EmptyBorder(5, 5, 15, 5));
        return jTextPane1;
    }

    private JTextPane getUnsupportedPanel(int nbUnsupported) {
        String message = Messages.getString("dicom.file.unsupported.sop.class").formatted(new Object[]{nbUnsupported});
        String html = "  <P>\n  %s\n  </P>\n".formatted(new Object[]{message});
        JTextPane jTextPane1 = GuiUtils.getPanelWithHyperlink((String)html);
        jTextPane1.setBorder(new EmptyBorder(5, 5, 15, 5));
        return jTextPane1;
    }

    protected DicomSeries buildDicomStructure(DicomMediaIO dicomReader) {
        MediaSeriesGroup study;
        String studyUID = (String)dicomReader.getTagValue(TagD.getUID((TagD.Level)TagD.Level.STUDY));
        String patientPseudoUID = (String)dicomReader.getTagValue(TagD.getUID((TagD.Level)TagD.Level.PATIENT));
        MediaSeriesGroup patient = this.dicomModel.getHierarchyNode(MediaSeriesGroupNode.rootNode, patientPseudoUID);
        if (patient == null) {
            study = this.dicomModel.getStudyNode(studyUID);
            if (study == null) {
                patient = new MediaSeriesGroupNode(TagW.PatientPseudoUID, (Object)patientPseudoUID, DicomModel.patient.tagView());
                dicomReader.writeMetaData(patient);
                this.dicomModel.addHierarchyNode(MediaSeriesGroupNode.rootNode, patient);
                LOGGER.info("Adding patient: {}", (Object)patient);
            } else {
                patient = this.dicomModel.getParent(study, DicomModel.patient);
                LOGGER.warn("DICOM patient attributes are inconsistent! Name or ID is different within an exam.");
            }
        }
        if ((study = this.dicomModel.getHierarchyNode(patient, studyUID)) == null) {
            study = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.STUDY), (Object)studyUID, DicomModel.study.tagView());
            dicomReader.writeMetaData(study);
            this.dicomModel.addHierarchyNode(patient, study);
        }
        boolean editableDicom = dicomReader.isEditableDicom();
        String seriesUID = (String)dicomReader.getTagValue(TagD.get((int)0x20000E));
        DicomSeries dicomSeries = (DicomSeries)this.dicomModel.getHierarchyNode(study, seriesUID);
        try {
            if (dicomSeries == null) {
                dicomSeries = dicomReader.buildSeries(seriesUID);
                dicomSeries.setTag(TagW.ExplorerModel, (Object)this.dicomModel);
                dicomSeries.setTag(TagW.stepNDimensions, null);
                if (editableDicom) {
                    dicomSeries.setTag(TagW.ObjectToSave, (Object)Boolean.TRUE);
                }
                dicomReader.writeMetaData((MediaSeriesGroup)dicomSeries);
                this.dicomModel.addHierarchyNode(study, (MediaSeriesGroup)dicomSeries);
                this.getDicomImageElements(dicomReader, dicomSeries, editableDicom);
                Integer splitNb = (Integer)dicomSeries.getTagValue(TagW.SplitSeriesNumber);
                if (splitNb != null) {
                    this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)this.dicomModel, null, (Object)dicomSeries));
                }
                this.openingStrategy.openViewerPlugin(patient, this.dicomModel, (Series<?>)dicomSeries);
            } else {
                Integer splitNb;
                Thumbnail t;
                DicomImageElement[] medias;
                if (this.isSOPInstanceUIDExist(study, (Series<?>)dicomSeries, TagD.getTagValue((TagReadable)dicomReader, (int)524312, String.class))) {
                    this.openingStrategy.openViewerPlugin(patient, this.dicomModel, (Series<?>)dicomSeries);
                    return null;
                }
                if (dicomSeries.size(null) == 0) {
                    dicomReader.writeMetaData((MediaSeriesGroup)dicomSeries);
                }
                if ((medias = this.getDicomImageElements(dicomReader, dicomSeries, editableDicom)) != null && medias.length > 0 && (t = (Thumbnail)dicomSeries.getTagValue(TagW.Thumbnail)) != null) {
                    GuiExecutor.execute(() -> t.repaint());
                }
                if ((splitNb = (Integer)dicomSeries.getTagValue(TagW.SplitSeriesNumber)) != null) {
                    this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)this.dicomModel, null, (Object)dicomSeries));
                }
                this.openingStrategy.openViewerPlugin(patient, this.dicomModel, (Series<?>)dicomSeries);
            }
        }
        catch (Exception e) {
            LOGGER.error("Build DICOM hierarchy", (Throwable)e);
        }
        return dicomSeries;
    }

    private DicomImageElement[] getDicomImageElements(DicomMediaIO dicomReader, DicomSeries series, boolean editableDicom) {
        Function<DicomSpecialElementFactory, DicomSpecialElement> buildSpecialElement = factory -> factory.buildDicomSpecialElement(dicomReader);
        DicomMediaIO.ResultContainer result = dicomReader.getMediaElement(buildSpecialElement);
        DicomImageElement[] medias = result.getImage();
        if (medias != null) {
            for (DicomImageElement media : medias) {
                this.dicomModel.applySplittingRules(series, media);
                if (!editableDicom) continue;
                media.setTag(TagW.ObjectToSave, (Object)Boolean.TRUE);
            }
            if (medias.length > 0) {
                series.setFileSize(series.getFileSize() + medias[0].getLength());
            }
        }
        if (result.getSpecialElement() != null) {
            DicomSpecialElement media = result.getSpecialElement();
            this.dicomModel.applySplittingRules(series, media);
            series.setFileSize(series.getFileSize() + media.getLength());
            this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)this.dicomModel, null, (Object)media));
        }
        return medias;
    }

    protected boolean isSOPInstanceUIDExist(MediaSeriesGroup study, Series<?> dicomSeries, Object sopUID) {
        String uid;
        TagW sopTag = TagD.getUID((TagD.Level)TagD.Level.INSTANCE);
        if (dicomSeries.hasMediaContains(sopTag, sopUID)) {
            return true;
        }
        Object splitNb = dicomSeries.getTagValue(TagW.SplitSeriesNumber);
        if (splitNb != null && study != null && (uid = (String)TagD.getTagValue(dicomSeries, (int)0x20000E, String.class)) != null) {
            for (MediaSeriesGroup group : this.dicomModel.getChildren(study)) {
                if (dicomSeries == group || !(group instanceof Series)) continue;
                Series s = (Series)group;
                if (!uid.equals(TagD.getTagValue((TagReadable)group, (int)0x20000E)) || !s.hasMediaContains(sopTag, sopUID)) continue;
                return true;
            }
        }
        return false;
    }
}

