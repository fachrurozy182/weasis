/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.AttributeSource
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Taggable
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.ui.editor.image.ViewerPlugin
 *  org.weasis.core.ui.util.ColorLayerUI
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 *  org.weasis.dicom.codec.utils.PatientComparator
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.mf.WadoParameters
 */
package org.weasis.dicom.explorer.wado;

import java.awt.Component;
import java.net.URL;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import org.dcm4che3.data.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.AttributeSource;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.util.ColorLayerUI;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.PatientComparator;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.DicomWebNode;
import org.weasis.dicom.explorer.wado.DownloadPriority;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.explorer.wado.ReaderParams;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.WadoParameters;

final class ManifestModelBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestModelBuilder.class);
    private static final TagW PATIENT_UID = TagD.getUID((TagD.Level)TagD.Level.PATIENT);
    private static final TagW STUDY_UID = TagD.getUID((TagD.Level)TagD.Level.STUDY);
    private static final TagW PATIENT_ID = TagD.get((int)0x100020);
    private static final TagW ISSUER_OF_PATIENT_ID = TagD.get((int)0x100021);
    private static final TagW PATIENT_NAME = TagD.get((int)0x100010);
    private static final TagW SERIES_INSTANCE_UID = TagD.get((int)0x20000E);
    private static final TagW[] PATIENT_TAGS = TagD.getTagFromIDs((int[])new int[]{0x100040, 0x100030, 1048626});
    private static final TagW[] STUDY_TAGS = TagD.getTagFromIDs((int[])new int[]{524320, 524336, 528432, 524368, 0x200010});
    private static final TagW[] SERIES_TAGS = TagD.getTagFromIDs((int[])new int[]{524384, 0x200011, 524321, 524337, 528446, 524432});
    private static final String SOP_INSTANCE_UID_KEY = TagD.getKeywordFromTag((int)524312, null);
    private static final String SOP_CLASS_UID_KEY = TagD.getKeywordFromTag((int)524310, null);
    private static final String INSTANCE_NUMBER_KEY = TagD.getKeywordFromTag((int)2097171, null);

    private ManifestModelBuilder() {
    }

    static WadoParameters buildArcQueryParameters(AttributeSource source, ReaderParams params) {
        String arcID = TagUtil.getTagAttribute((AttributeSource)source, (String)"arcId", (String)"");
        String wadoURL = TagUtil.getTagAttribute((AttributeSource)source, (String)"baseUrl", null);
        boolean onlySopUID = Boolean.parseBoolean(TagUtil.getTagAttribute((AttributeSource)source, (String)"requireOnlySOPInstanceUID", (String)Boolean.FALSE.toString()));
        String additionalParameters = TagUtil.getTagAttribute((AttributeSource)source, (String)"additionalParameters", (String)"");
        String overrideList = TagUtil.getTagAttribute((AttributeSource)source, (String)"overrideDicomTagsList", null);
        String webLogin = TagUtil.getTagAttribute((AttributeSource)source, (String)"webLogin", null);
        String queryMode = TagUtil.getTagAttribute((AttributeSource)source, (String)"queryMode", null);
        boolean wadoRs = "DICOM_WEB".equals(queryMode);
        WadoParameters wadoParameters = new WadoParameters(arcID, wadoURL, onlySopUID, additionalParameters, overrideList, webLogin, wadoRs);
        params.wadoUri = ManifestModelBuilder.getWadoUrl(wadoURL);
        params.bulkSeriesRetrieve = ManifestModelBuilder.isBulkSeriesRetrieve(source, wadoRs);
        return wadoParameters;
    }

    static boolean isBulkSeriesRetrieve(AttributeSource source, boolean wadoRs) {
        if (!wadoRs) {
            return false;
        }
        String seriesRetrieve = TagUtil.getTagAttribute((AttributeSource)source, (String)"seriesRetrieve", null);
        if (seriesRetrieve != null) {
            return Boolean.parseBoolean(seriesRetrieve);
        }
        return Boolean.getBoolean("weasis.dicom.web.series.bulk");
    }

    static void addHttpTag(AttributeSource source, WadoParameters wadoParameters) {
        String httpKey = TagUtil.getTagAttribute((AttributeSource)source, (String)"key", null);
        String httpValue = TagUtil.getTagAttribute((AttributeSource)source, (String)"value", null);
        wadoParameters.addHttpTag(httpKey, httpValue);
    }

    static void showViewerMessage(AttributeSource source) {
        String title = TagUtil.getTagAttribute((AttributeSource)source, (String)"title", null);
        String message = TagUtil.getTagAttribute((AttributeSource)source, (String)"description", null);
        if (StringUtil.hasText((CharSequence)title) && StringUtil.hasText((CharSequence)message)) {
            String severity = TagUtil.getTagAttribute((AttributeSource)source, (String)"severity", (String)"WARN");
            int messageType = "ERROR".equals(severity) ? 0 : ("INFO".equals(severity) ? 1 : 2);
            GuiExecutor.execute(() -> {
                ColorLayerUI layer = ColorLayerUI.createTransparentLayerUI((Component)GuiUtils.getUICore().getBaseArea());
                JOptionPane.showMessageDialog(ColorLayerUI.getContentPane((ColorLayerUI)layer), message, title, messageType);
                if (layer != null) {
                    layer.hideUI();
                }
            });
        }
    }

    static MediaSeriesGroup buildPatient(AttributeSource source, ReaderParams params, WadoParameters wadoParameters) {
        PatientComparator patientComparator = new PatientComparator(source);
        String patientPseudoUID = patientComparator.buildPatientPseudoUID();
        DicomModel model = params.getModel();
        MediaSeriesGroup patient = model.getHierarchyNode(MediaSeriesGroupNode.rootNode, patientPseudoUID);
        if (patient == null) {
            patient = new MediaSeriesGroupNode(PATIENT_UID, (Object)patientPseudoUID, DicomModel.patient.tagView());
            patient.setTag(PATIENT_ID, (Object)TagUtil.getTagAttribute((AttributeSource)source, (String)PATIENT_ID.getKeyword(), (String)"UNKNOWN"));
            patient.setTag(PATIENT_NAME, (Object)TagUtil.getTagAttribute((AttributeSource)source, (String)PATIENT_NAME.getKeyword(), (String)"UNKNOWN"));
            patient.setTagNoNull(ISSUER_OF_PATIENT_ID, (Object)TagUtil.getTagAttribute((AttributeSource)source, (String)ISSUER_OF_PATIENT_ID.getKeyword(), null));
            for (TagW tag : PATIENT_TAGS) {
                tag.readValue((Object)source, (Taggable)patient);
            }
            model.addHierarchyNode(MediaSeriesGroupNode.rootNode, patient);
            LOGGER.info("Adding new patient: {}", (Object)patient);
        }
        return patient;
    }

    static MediaSeriesGroup buildStudy(AttributeSource source, ReaderParams params, MediaSeriesGroup patient) {
        return ManifestModelBuilder.buildStudy((Object)source, params, patient);
    }

    static MediaSeriesGroup buildStudy(Attributes dataset, ReaderParams params, MediaSeriesGroup patient) {
        return ManifestModelBuilder.buildStudy((Object)dataset, params, patient);
    }

    private static MediaSeriesGroup buildStudy(Object source, ReaderParams params, MediaSeriesGroup patient) {
        String studyUID;
        DicomModel model = params.getModel();
        MediaSeriesGroup study = model.getHierarchyNode(patient, studyUID = (String)STUDY_UID.getValue(source));
        if (study == null) {
            study = new MediaSeriesGroupNode(STUDY_UID, (Object)studyUID, DicomModel.study.tagView());
            for (TagW tag : STUDY_TAGS) {
                tag.readValue(source, (Taggable)study);
            }
            model.addHierarchyNode(patient, study);
        }
        return study;
    }

    static DicomSeries buildSeries(AttributeSource source, ReaderParams params, MediaSeriesGroup study, WadoParameters wadoParameters) {
        String seriesUID;
        DicomModel model = params.getModel();
        DicomSeries dicomSeries = (DicomSeries)model.getHierarchyNode(study, seriesUID = (String)SERIES_INSTANCE_UID.getValue((Object)source));
        if (dicomSeries == null) {
            dicomSeries = new DicomSeries(seriesUID);
            dicomSeries.setTag(SERIES_INSTANCE_UID, (Object)seriesUID);
            dicomSeries.setTag(TagW.ExplorerModel, (Object)model);
            dicomSeries.setTag(TagW.WadoParameters, (Object)wadoParameters);
            for (TagW tag : SERIES_TAGS) {
                tag.readValue((Object)source, (Taggable)dicomSeries);
            }
            dicomSeries.setTagNoNull(TagW.WadoTransferSyntaxUID, (Object)TagUtil.getTagAttribute((AttributeSource)source, (String)TagW.WadoTransferSyntaxUID.getKeyword(), null));
            dicomSeries.setTagNoNull(TagW.WadoCompressionRate, (Object)TagUtil.getIntegerTagAttribute((AttributeSource)source, (String)TagW.WadoCompressionRate.getKeyword(), null));
            dicomSeries.setTagNoNull(TagW.DirectDownloadThumbnail, (Object)TagUtil.getTagAttribute((AttributeSource)source, (String)TagW.DirectDownloadThumbnail.getKeyword(), null));
            model.addHierarchyNode(study, (MediaSeriesGroup)dicomSeries);
        } else {
            WadoParameters wado = (WadoParameters)dicomSeries.getTagValue(TagW.WadoParameters);
            if (wado == null) {
                dicomSeries.setTag(TagW.WadoParameters, (Object)wadoParameters);
            } else if (!wado.getBaseURL().equals(wadoParameters.getBaseURL())) {
                LOGGER.error("Wado parameters must be unique within a DICOM Series: {}", (Object)dicomSeries);
                return null;
            }
        }
        return dicomSeries;
    }

    static DicomSeries buildSeries(Attributes dataset, ReaderParams params, MediaSeriesGroup study, WadoParameters wadoParameters) {
        String seriesUID;
        DicomModel model = params.getModel();
        DicomSeries dicomSeries = (DicomSeries)model.getHierarchyNode(study, seriesUID = (String)SERIES_INSTANCE_UID.getValue((Object)dataset));
        if (dicomSeries == null) {
            dicomSeries = new DicomSeries(seriesUID);
            dicomSeries.setTag(SERIES_INSTANCE_UID, (Object)seriesUID);
            dicomSeries.setTag(TagW.ExplorerModel, (Object)model);
            dicomSeries.setTag(TagW.WadoParameters, (Object)wadoParameters);
            for (TagW tag : SERIES_TAGS) {
                tag.readValue((Object)dataset, (Taggable)dicomSeries);
            }
            model.addHierarchyNode(study, (MediaSeriesGroup)dicomSeries);
        }
        return dicomSeries;
    }

    static SeriesInstanceList getSeriesInstanceList(DicomSeries dicomSeries) {
        SeriesInstanceList list = (SeriesInstanceList)dicomSeries.getTagValue(TagW.WadoInstanceReferenceList);
        return list != null ? list : new SeriesInstanceList();
    }

    static void addSopInstance(AttributeSource source, SeriesInstanceList seriesInstanceList) {
        Integer frame;
        SopInstance sop;
        String sopInstanceUID = TagUtil.getTagAttribute((AttributeSource)source, (String)SOP_INSTANCE_UID_KEY, null);
        if (sopInstanceUID != null && (sop = seriesInstanceList.getSopInstance(sopInstanceUID, frame = TagUtil.getIntegerTagAttribute((AttributeSource)source, (String)INSTANCE_NUMBER_KEY, null))) == null) {
            String sopClassUID = TagUtil.getTagAttribute((AttributeSource)source, (String)SOP_CLASS_UID_KEY, null);
            sop = new SopInstance(sopInstanceUID, sopClassUID, frame);
            sop.setDirectDownloadFile(TagUtil.getTagAttribute((AttributeSource)source, (String)TagW.DirectDownloadFile.getKeyword(), null));
            seriesInstanceList.addSopInstance(sop);
        }
    }

    static void completeSeries(ReaderParams params, MediaSeriesGroup patient, MediaSeriesGroup study, DicomSeries dicomSeries, SeriesInstanceList seriesInstanceList) {
        if (seriesInstanceList.isEmpty() && params.bulkSeriesRetrieve) {
            dicomSeries.setTag(LoadSeries.SERIES_BULK_RETRIEVE, (Object)Boolean.TRUE);
        }
        dicomSeries.setTag(TagW.WadoInstanceReferenceList, (Object)seriesInstanceList);
        boolean bulkRetrieve = Boolean.TRUE.equals(dicomSeries.getTagValue(LoadSeries.SERIES_BULK_RETRIEVE));
        if (!seriesInstanceList.isEmpty() || bulkRetrieve) {
            DicomModel model = params.getModel();
            String seriesUID = (String)TagD.getTagValue((TagReadable)dicomSeries, (int)0x20000E, String.class);
            AuthMethod authMethod = params.wadoUri == null ? null : params.wadoUri.getAuthMethod();
            LoadSeries loadSeries = new LoadSeries(dicomSeries, model, authMethod, GuiUtils.getUICore().getSystemPreferences().getIntProperty("download.concurrent.series.images", 4), true, true);
            loadSeries.setPriority(new DownloadPriority(patient, study, (MediaSeriesGroup)dicomSeries, true));
            params.getSeriesMap().put(seriesUID, loadSeries);
        }
    }

    static void focusUniquePatient(Set<MediaSeriesGroup> patients) {
        if (patients.size() == 1) {
            MediaSeriesGroup uniquePatient = patients.iterator().next();
            GuiExecutor.execute(() -> {
                List viewerPlugins;
                List list = viewerPlugins = GuiUtils.getUICore().getViewerPlugins();
                synchronized (list) {
                    for (ViewerPlugin p : viewerPlugins) {
                        if (!uniquePatient.equals((Object)p.getGroupID())) continue;
                        p.setSelectedAndGetFocus();
                        break;
                    }
                }
            });
        }
    }

    static void startDownloads(ReaderParams params, WadoParameters wadoParameters) {
        for (LoadSeries loadSeries : params.getSeriesMap().values()) {
            if (DicomModel.isHiddenModality(loadSeries.getDicomSeries())) continue;
            loadSeries.startDownloadImageReference(wadoParameters);
        }
    }

    static DicomWebNode getWadoUrl(String url) {
        List<AbstractDicomNode> webNodes = AbstractDicomNode.loadDicomNodes(AbstractDicomNode.Type.WEB, AbstractDicomNode.UsageType.RETRIEVE, DicomWebNode.WebType.WADO);
        for (AbstractDicomNode n : webNodes) {
            DicomWebNode wn;
            URL wadoURL;
            if (!(n instanceof DicomWebNode) || (wadoURL = (wn = (DicomWebNode)n).getUrl()) == null || !wadoURL.toString().equals(url)) continue;
            return wn;
        }
        return null;
    }
}

