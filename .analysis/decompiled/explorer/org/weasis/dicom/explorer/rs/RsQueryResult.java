/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.json.Json
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.img.util.DateTimeUtils
 *  org.dcm4che3.img.util.DicomUtils
 *  org.dcm4che3.json.JSONReader
 *  org.dcm4che3.json.JSONReader$Callback
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Taggable
 *  org.weasis.core.api.net.HttpStream
 *  org.weasis.core.api.net.HttpUtils
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 *  org.weasis.dicom.codec.utils.PatientComparator
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.mf.AbstractQueryResult
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.mf.WadoParameters
 *  org.weasis.dicom.web.MultipartConstants$DicomContentType
 */
package org.weasis.dicom.explorer.rs;

import jakarta.json.Json;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.util.DateTimeUtils;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.json.JSONReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.core.api.net.HttpStream;
import org.weasis.core.api.net.HttpUtils;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.PatientComparator;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.rs.RsQueryParams;
import org.weasis.dicom.explorer.wado.DownloadPriority;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.mf.AbstractQueryResult;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.WadoParameters;
import org.weasis.dicom.web.MultipartConstants;

public class RsQueryResult
extends AbstractQueryResult {
    private static final Logger LOGGER = LoggerFactory.getLogger(RsQueryResult.class);
    private static final boolean MULTIPLE_PARAMS = LangUtil.emptyToFalse((String)System.getProperty("dicom.qido.query.multi.params"));
    public static final String STUDY_QUERY = RsQueryResult.multiParams("&includefield=00080020,00080030,00080050,00080061,00080090,00081030,00100010,00100020,00100021,00100030,00100040,0020000D,00200010");
    public static final String SERIES_QUERY = RsQueryResult.multiParams("0008103E,00080060,0020000E,00200011,00081190,00201209");
    public static final String INSTANCE_QUERY = RsQueryResult.multiParams("00080018,00200013,00081190");
    public static final String QIDO_REQUEST = "QIDO-RS request: {}";
    private final RsQueryParams rsQueryParams;
    private final WadoParameters wadoParameters;
    private final boolean defaultStartDownloading;
    private final AuthMethod authMethod;

    public RsQueryResult(RsQueryParams rsQueryParams, AuthMethod authMethod) {
        this.rsQueryParams = rsQueryParams;
        this.authMethod = authMethod;
        this.wadoParameters = new WadoParameters("", true, true);
        rsQueryParams.getRetrieveHeaders().forEach((arg_0, arg_1) -> ((WadoParameters)this.wadoParameters).addHttpTag(arg_0, arg_1));
        this.wadoParameters.addHttpTag("Accept", "multipart/related;type=\"" + String.valueOf(MultipartConstants.DicomContentType.DICOM) + "\";" + rsQueryParams.getProperties().getProperty("accept.ext"));
        this.defaultStartDownloading = GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.download.immediately", true);
    }

    private static String multiParams(String query) {
        return MULTIPLE_PARAMS ? query.replace(",", "&includefield=") : query;
    }

    public WadoParameters getWadoParameters() {
        return null;
    }

    public void buildFromPatientID(List<String> patientIDs) {
        for (String patientID : LangUtil.emptyIfNull(patientIDs)) {
            if (!StringUtil.hasText((CharSequence)patientID)) continue;
            int beginIndex = patientID.indexOf("^^^");
            StringBuilder buf = new StringBuilder(this.rsQueryParams.getBaseUrl());
            buf.append("/studies?00100020=");
            String patientVal = beginIndex <= 0 ? patientID : patientID.substring(0, beginIndex);
            try {
                buf.append(URLEncoder.encode(patientVal, StandardCharsets.UTF_8));
                if (beginIndex > 0) {
                    buf.append("&00100021=");
                    buf.append(patientID.substring(beginIndex + 3));
                }
                buf.append(STUDY_QUERY);
                buf.append(this.rsQueryParams.getProperties().getProperty("query.ext", ""));
                LOGGER.debug(QIDO_REQUEST, (Object)buf);
                List<Attributes> studies = RsQueryResult.parseJSON(buf.toString(), this.authMethod, new URLParameters(this.rsQueryParams.getQueryHeaders()));
                if (studies.isEmpty()) continue;
                studies.sort(RsQueryResult.getStudyComparator());
                this.applyAllFilters(studies);
            }
            catch (Exception e) {
                LOGGER.error("QIDO-RS with PatientID {}", (Object)patientID, (Object)e);
            }
        }
    }

    public static String seriesQueryUrl(String baseUrl, String studyUid, String queryExtension) {
        return baseUrl + "/studies/" + studyUid + "/series?includefield=" + SERIES_QUERY + StringUtil.getEmptyStringIfNull((Object)queryExtension);
    }

    public static URLParameters jsonQueryParameters(Map<String, String> headers) {
        HashMap<String, String> jsonHeaders = new HashMap<String, String>(headers);
        jsonHeaders.put("Accept", "application/dicom+json");
        return new URLParameters(jsonHeaders);
    }

    public static List<Attributes> parseJSON(String url, AuthMethod authMethod, URLParameters urlParameters) throws Exception {
        ArrayList<Attributes> items = new ArrayList<Attributes>();
        try (HttpStream response = HttpUtils.getHttpResponse((String)url, (URLParameters)urlParameters, (AuthMethod)authMethod);
             InputStreamReader instream = new InputStreamReader(response.getInputStream(), StandardCharsets.UTF_8);){
            int code = response.getResponseCode();
            if (code == 200 || code == 206) {
                JSONReader reader = new JSONReader(Json.createParser((Reader)instream));
                JSONReader.Callback callback = (fmi, dataset) -> items.add(dataset);
                reader.readDatasets(callback);
            }
            if (code == 413) {
                throw new IllegalStateException("The size of the results exceeds the maximum payload size supported by the origin server.");
            }
            if (authMethod != null && code == 401) {
                authMethod.resetToken();
                authMethod.getToken();
            }
        }
        return items;
    }

    public static int seriesInstanceCount(String dicomWebBaseUrl, String studyUID, String seriesUID, URLParameters urlParameters, AuthMethod authMethod) {
        String url = "%s/studies/%s/series?0020000E=%s&includefield=00201209".formatted(new Object[]{dicomWebBaseUrl, studyUID, seriesUID});
        try {
            for (Attributes series : RsQueryResult.parseJSON(url, authMethod, RsQueryResult.jsonQueryParameters(urlParameters.headers()))) {
                if (!seriesUID.equals(series.getString(0x20000E))) continue;
                return DicomUtils.getIntegerFromDicomElement((Attributes)series, (int)2101769, (Integer)0);
            }
        }
        catch (Exception e) {
            LOGGER.debug("Cannot fetch instance count for series {}", (Object)seriesUID, (Object)e);
        }
        return 0;
    }

    private void applyAllFilters(List<Attributes> studies) {
        int recent;
        int rep;
        Date date;
        Attributes s;
        if (StringUtil.hasText((CharSequence)this.rsQueryParams.getLowerDateTime())) {
            Date lowerDateTime = null;
            try {
                lowerDateTime = DateTimeUtils.parseXmlDateTime((CharSequence)this.rsQueryParams.getLowerDateTime()).getTime();
            }
            catch (Exception e) {
                LOGGER.error("Cannot parse date: {}", (Object)this.rsQueryParams.getLowerDateTime(), (Object)e);
            }
            if (lowerDateTime != null) {
                for (int i = studies.size() - 1; i >= 0; --i) {
                    s = studies.get(i);
                    date = s.getDate(2251937253163056L);
                    if (date == null || (rep = date.compareTo(lowerDateTime)) <= 0) continue;
                    studies.remove(i);
                }
            }
        }
        if (StringUtil.hasText((CharSequence)this.rsQueryParams.getUpperDateTime())) {
            Date upperDateTime = null;
            try {
                upperDateTime = DateTimeUtils.parseXmlDateTime((CharSequence)this.rsQueryParams.getUpperDateTime()).getTime();
            }
            catch (Exception e) {
                LOGGER.error("Cannot parse date: {}", (Object)this.rsQueryParams.getUpperDateTime(), (Object)e);
            }
            if (upperDateTime != null) {
                for (int i = studies.size() - 1; i >= 0; --i) {
                    s = studies.get(i);
                    date = s.getDate(2251937253163056L);
                    if (date == null || (rep = date.compareTo(upperDateTime)) >= 0) continue;
                    studies.remove(i);
                }
            }
        }
        if (StringUtil.hasText((CharSequence)this.rsQueryParams.getMostRecentResults()) && (recent = StringUtil.getInteger((String)this.rsQueryParams.getMostRecentResults()).intValue()) > 0 && studies.size() > recent) {
            studies.subList(recent, studies.size()).clear();
        }
        if (StringUtil.hasText((CharSequence)this.rsQueryParams.getModalitiesInStudy())) {
            for (int i = studies.size() - 1; i >= 0; --i) {
                Attributes s2 = studies.get(i);
                String m = s2.getString(524385);
                if (!StringUtil.hasText((CharSequence)m)) continue;
                boolean remove = true;
                for (String mod : this.rsQueryParams.getModalitiesInStudy().split(",")) {
                    if (!m.contains(mod)) continue;
                    remove = false;
                    break;
                }
                if (!remove) continue;
                studies.remove(i);
            }
        }
        if (StringUtil.hasText((CharSequence)this.rsQueryParams.getKeywords())) {
            int i;
            String[] keys = this.rsQueryParams.getKeywords().split(",");
            for (i = 0; i < keys.length; ++i) {
                keys[i] = StringUtil.deAccent((String)keys[i].trim().toUpperCase());
            }
            block9: for (i = studies.size() - 1; i >= 0; --i) {
                s = studies.get(i);
                String desc = StringUtil.deAccent((String)s.getString(528432, "").toUpperCase());
                for (String key : keys) {
                    if (desc.contains(key)) continue block9;
                }
                studies.remove(i);
            }
        }
        for (Attributes studyDataSet : studies) {
            this.fillSeries(studyDataSet, this.defaultStartDownloading);
        }
    }

    private static Comparator<Attributes> getStudyComparator() {
        return (o1, o2) -> {
            Date date1 = o1.getDate(524320);
            Date date2 = o2.getDate(524320);
            if (date1 != null && date2 != null) {
                int rep = date2.compareTo(date1);
                if (rep == 0) {
                    Date time1 = o1.getDate(524336);
                    Date time2 = o2.getDate(524336);
                    if (time1 != null && time2 != null) {
                        return time2.compareTo(time1);
                    }
                } else {
                    return rep;
                }
            }
            if (date1 == null && date2 == null) {
                return o1.getString(0x20000D, "").compareTo(o2.getString(0x20000D, ""));
            }
            if (date1 == null) {
                return 1;
            }
            if (date2 == null) {
                return -1;
            }
            return 0;
        };
    }

    public void buildFromStudyInstanceUID(List<String> studyInstanceUIDs) {
        this.buildFromStudyInstanceUID(studyInstanceUIDs, this.defaultStartDownloading);
    }

    public void buildFromStudyInstanceUID(List<String> studyInstanceUIDs, boolean startDownloading) {
        for (String studyInstanceUID : LangUtil.emptyIfNull(studyInstanceUIDs)) {
            if (!StringUtil.hasText((CharSequence)studyInstanceUID)) continue;
            StringBuilder buf = new StringBuilder(this.rsQueryParams.getBaseUrl());
            buf.append("/studies?0020000D=");
            buf.append(studyInstanceUID);
            buf.append(STUDY_QUERY);
            buf.append(this.rsQueryParams.getProperties().getProperty("query.ext", ""));
            try {
                LOGGER.debug(QIDO_REQUEST, (Object)buf);
                List<Attributes> studies = RsQueryResult.parseJSON(buf.toString(), this.authMethod, new URLParameters(this.rsQueryParams.getQueryHeaders()));
                for (Attributes studyDataSet : studies) {
                    this.fillSeries(studyDataSet, startDownloading);
                }
            }
            catch (Exception e) {
                LOGGER.error("QIDO-RS with studyUID {}", (Object)studyInstanceUID, (Object)e);
            }
        }
    }

    public void buildFromStudyAccessionNumber(List<String> accessionNumbers) {
        for (String accessionNumber : LangUtil.emptyIfNull(accessionNumbers)) {
            if (!StringUtil.hasText((CharSequence)accessionNumber)) continue;
            StringBuilder buf = new StringBuilder(this.rsQueryParams.getBaseUrl());
            buf.append("/studies?00080050=");
            buf.append(accessionNumber);
            buf.append(STUDY_QUERY);
            buf.append(this.rsQueryParams.getProperties().getProperty("query.ext", ""));
            try {
                LOGGER.debug(QIDO_REQUEST, (Object)buf);
                List<Attributes> studies = RsQueryResult.parseJSON(buf.toString(), this.authMethod, new URLParameters(this.rsQueryParams.getQueryHeaders()));
                for (Attributes studyDataSet : studies) {
                    this.fillSeries(studyDataSet, this.defaultStartDownloading);
                }
            }
            catch (Exception e) {
                LOGGER.error("QIDO-RS with AccessionNumber {}", (Object)accessionNumber, (Object)e);
            }
        }
    }

    public void buildFromSeriesInstanceUID(List<String> seriesInstanceUIDs) {
        boolean wholeStudy = LangUtil.emptyToFalse((String)this.rsQueryParams.getProperties().getProperty("show.whole.study"));
        LinkedHashSet<String> studyHashSet = new LinkedHashSet<String>();
        for (String seriesInstanceUID : LangUtil.emptyIfNull(seriesInstanceUIDs)) {
            if (!StringUtil.hasText((CharSequence)seriesInstanceUID)) continue;
            StringBuilder buf = new StringBuilder(this.rsQueryParams.getBaseUrl());
            buf.append("/series?0020000E=");
            buf.append(seriesInstanceUID);
            buf.append(STUDY_QUERY);
            buf.append(",0008103E,00080060,00081190,00200011");
            buf.append(this.rsQueryParams.getProperties().getProperty("query.ext", ""));
            try {
                LOGGER.debug(QIDO_REQUEST, (Object)buf);
                List<Attributes> series = RsQueryResult.parseJSON(buf.toString(), this.authMethod, new URLParameters(this.rsQueryParams.getQueryHeaders()));
                if (series.isEmpty()) continue;
                Attributes dataset = (Attributes)series.getFirst();
                MediaSeriesGroup patient = RsQueryResult.getPatient(dataset, this.rsQueryParams.getDicomModel());
                MediaSeriesGroup study = RsQueryResult.getStudy(patient, dataset, this.rsQueryParams.getDicomModel());
                for (Attributes seriesDataset : series) {
                    DicomSeries dicomSeries = this.getSeries(study, seriesDataset, this.defaultStartDownloading);
                    this.fillSeriesContent((Series<?>)dicomSeries);
                }
                studyHashSet.add(dataset.getString(0x20000D));
            }
            catch (Exception e) {
                LOGGER.error("QIDO-RS with seriesUID {}", (Object)seriesInstanceUID, (Object)e);
            }
        }
        if (wholeStudy) {
            this.buildFromStudyInstanceUID(new ArrayList<String>(studyHashSet), false);
        }
    }

    public void buildFromSopInstanceUID(List<String> sopInstanceUIDs) {
        for (String sopInstanceUID : LangUtil.emptyIfNull(sopInstanceUIDs)) {
            if (!StringUtil.hasText((CharSequence)sopInstanceUID)) continue;
            StringBuilder buf = new StringBuilder(this.rsQueryParams.getBaseUrl());
            buf.append("/instances?00080018=");
            buf.append(sopInstanceUID);
            buf.append(STUDY_QUERY);
            buf.append(",0008103E,00080060,0020000E,00200011");
            buf.append(",00200013,00081190");
            buf.append(this.rsQueryParams.getProperties().getProperty("query.ext", ""));
            try {
                LOGGER.debug(QIDO_REQUEST, (Object)buf);
                List<Attributes> instances = RsQueryResult.parseJSON(buf.toString(), this.authMethod, new URLParameters(this.rsQueryParams.getQueryHeaders()));
                if (instances.isEmpty()) continue;
                Attributes dataset = instances.get(0);
                MediaSeriesGroup patient = RsQueryResult.getPatient(dataset, this.rsQueryParams.getDicomModel());
                MediaSeriesGroup study = RsQueryResult.getStudy(patient, dataset, this.rsQueryParams.getDicomModel());
                DicomSeries dicomSeries = this.getSeries(study, dataset, this.defaultStartDownloading);
                String seriesRetrieveURL = (String)TagD.getTagValue((TagReadable)dicomSeries, (int)528784, String.class);
                SeriesInstanceList seriesInstanceList = (SeriesInstanceList)dicomSeries.getTagValue(TagW.WadoInstanceReferenceList);
                if (seriesInstanceList == null) continue;
                for (Attributes instanceDataSet : instances) {
                    RsQueryResult.addSopInstance(instanceDataSet, seriesInstanceList, seriesRetrieveURL);
                }
            }
            catch (Exception e) {
                LOGGER.error("QIDO-RS with sopInstanceUID {}", (Object)sopInstanceUID, (Object)e);
            }
        }
    }

    private void fillSeries(Attributes studyDataSet, boolean startDownloading) {
        String studyInstanceUID = studyDataSet.getString(0x20000D);
        if (StringUtil.hasText((CharSequence)studyInstanceUID)) {
            String url = RsQueryResult.seriesQueryUrl(this.rsQueryParams.getBaseUrl(), studyInstanceUID, this.rsQueryParams.getProperties().getProperty("query.ext"));
            try {
                LOGGER.debug(QIDO_REQUEST, (Object)url);
                List<Attributes> series = RsQueryResult.parseJSON(url, this.authMethod, new URLParameters(this.rsQueryParams.getQueryHeaders()));
                if (!series.isEmpty()) {
                    MediaSeriesGroup patient = RsQueryResult.getPatient(studyDataSet, this.rsQueryParams.getDicomModel());
                    MediaSeriesGroup study = RsQueryResult.getStudy(patient, studyDataSet, this.rsQueryParams.getDicomModel());
                    for (Attributes seriesDataset : series) {
                        DicomSeries dicomSeries = this.getSeries(study, seriesDataset, startDownloading);
                        this.fillSeriesContent((Series<?>)dicomSeries);
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error("QIDO-RS all series with studyUID {}", (Object)studyInstanceUID, (Object)e);
            }
        }
    }

    private void fillSeriesContent(Series<?> dicomSeries) {
        dicomSeries.setTag(LoadSeries.SERIES_BULK_RETRIEVE, (Object)Boolean.TRUE);
    }

    public static void addSopInstance(Attributes instanceDataSet, SeriesInstanceList seriesInstanceList, String seriesRetrieveURL) {
        Integer frame;
        String sopUID = instanceDataSet.getString(524312);
        SopInstance sop = seriesInstanceList.getSopInstance(sopUID, frame = DicomUtils.getIntegerFromDicomElement((Attributes)instanceDataSet, (int)2097171, null));
        if (sop == null) {
            sop = new SopInstance(sopUID, frame);
            Object rurl = instanceDataSet.getString(528784);
            if (!StringUtil.hasText((CharSequence)rurl)) {
                rurl = seriesRetrieveURL + "/instances/" + sopUID;
            }
            sop.setDirectDownloadFile((String)rurl);
            seriesInstanceList.addSopInstance(sop);
        }
    }

    public static MediaSeriesGroup getPatient(Attributes patientDataset, DicomModel model) {
        if (patientDataset == null) {
            throw new IllegalArgumentException("patientDataset cannot be null");
        }
        PatientComparator patientComparator = new PatientComparator(patientDataset);
        String patientPseudoUID = patientComparator.buildPatientPseudoUID();
        MediaSeriesGroup patient = model.getHierarchyNode(MediaSeriesGroupNode.rootNode, patientPseudoUID);
        if (patient == null) {
            TagW[] tags;
            patient = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.PATIENT), (Object)patientPseudoUID, DicomModel.patient.tagView());
            patient.setTag(TagD.get((int)0x100020), (Object)patientComparator.getPatientId());
            patient.setTag(TagD.get((int)0x100010), (Object)patientComparator.getName());
            patient.setTagNoNull(TagD.get((int)0x100021), (Object)patientComparator.getIssuerOfPatientID());
            for (TagW tag : tags = TagD.getTagFromIDs((int[])new int[]{0x100040, 0x100030, 1048626})) {
                tag.readValue((Object)patientDataset, (Taggable)patient);
            }
            model.addHierarchyNode(MediaSeriesGroupNode.rootNode, patient);
            LOGGER.info("Adding new patient: {}", (Object)patient);
        }
        return patient;
    }

    public static MediaSeriesGroup getStudy(MediaSeriesGroup patient, Attributes studyDataset, DicomModel model) {
        if (studyDataset == null) {
            throw new IllegalArgumentException("studyDataset cannot be null");
        }
        String studyUID = studyDataset.getString(0x20000D);
        MediaSeriesGroup study = model.getHierarchyNode(patient, studyUID);
        if (study == null) {
            TagW[] tags;
            study = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.STUDY), (Object)studyUID, DicomModel.study.tagView());
            for (TagW tag : tags = TagD.getTagFromIDs((int[])new int[]{524320, 524336, 528432, 524368, 0x200010, 524432})) {
                tag.readValue((Object)studyDataset, (Taggable)study);
            }
            model.addHierarchyNode(patient, study);
        }
        return study;
    }

    private DicomSeries getSeries(MediaSeriesGroup study, Attributes seriesDataset, boolean startDownloading) {
        if (seriesDataset == null) {
            throw new IllegalArgumentException("seriesDataset cannot be null");
        }
        String seriesUID = seriesDataset.getString(0x20000E);
        DicomModel model = this.rsQueryParams.getDicomModel();
        DicomSeries dicomSeries = (DicomSeries)model.getHierarchyNode(study, seriesUID);
        if (dicomSeries == null) {
            TagW[] tags;
            dicomSeries = new DicomSeries(seriesUID);
            dicomSeries.setTag(TagD.get((int)0x20000E), (Object)seriesUID);
            dicomSeries.setTag(TagW.ExplorerModel, (Object)model);
            dicomSeries.setTag(TagW.WadoParameters, (Object)this.wadoParameters);
            dicomSeries.setTag(TagW.WadoInstanceReferenceList, (Object)new SeriesInstanceList());
            for (TagW tag : tags = TagD.getTagFromIDs((int[])new int[]{524384, 0x200011, 528446, 528784, 2101769})) {
                tag.readValue((Object)seriesDataset, (Taggable)dicomSeries);
            }
            if (!StringUtil.hasText((CharSequence)((CharSequence)TagD.getTagValue((TagReadable)dicomSeries, (int)528784, String.class)))) {
                StringBuilder buf = new StringBuilder(this.rsQueryParams.getBaseUrl());
                buf.append("/studies/");
                buf.append(study.getTagValue(TagD.get((int)0x20000D)));
                buf.append("/series/");
                buf.append(seriesUID);
                dicomSeries.setTag(TagD.get((int)528784), (Object)buf.toString());
            }
            model.addHierarchyNode(study, (MediaSeriesGroup)dicomSeries);
            LoadSeries loadSeries = new LoadSeries(dicomSeries, this.rsQueryParams.getDicomModel(), this.authMethod, GuiUtils.getUICore().getSystemPreferences().getIntProperty("download.concurrent.series.images", 4), true, startDownloading);
            loadSeries.setPriority(new DownloadPriority(model.getParent(study, DicomModel.patient), study, (MediaSeriesGroup)dicomSeries, true));
            this.rsQueryParams.getSeriesMap().put((String)TagD.getTagValue((TagReadable)dicomSeries, (int)0x20000E, String.class), loadSeries);
        }
        return dicomSeries;
    }
}

