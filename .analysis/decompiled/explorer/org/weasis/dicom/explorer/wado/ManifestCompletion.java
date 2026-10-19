/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.mf.HttpTag
 *  org.weasis.dicom.mf.WadoParameters
 */
package org.weasis.dicom.explorer.wado;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dcm4che3.data.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.rs.RsQueryResult;
import org.weasis.dicom.explorer.wado.ManifestModelBuilder;
import org.weasis.dicom.explorer.wado.ReaderParams;
import org.weasis.dicom.mf.HttpTag;
import org.weasis.dicom.mf.WadoParameters;

final class ManifestCompletion {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestCompletion.class);
    private static final String STUDY_FIELDS = "00080020,00080030,00080050,00080061,00080090,00081030,00100010,00100020,00100021,00100030,00100040,0020000D,00200010";
    private static final String SERIES_FIELDS = "0008103E,00080060,0020000E,00200011,00081190";

    private ManifestCompletion() {
    }

    static boolean isPartialLevel(WadoParameters wado, boolean hasChildren) {
        return !hasChildren && wado != null && wado.isWadoRS();
    }

    static void completePatient(ReaderParams params, WadoParameters wado, MediaSeriesGroup patient) {
        String patientID = (String)TagD.getTagValue((TagReadable)patient, (int)0x100020, String.class);
        if (!StringUtil.hasText((CharSequence)patientID) || "UNKNOWN".equals(patientID)) {
            LOGGER.warn("Cannot complete a patient-level DICOMweb manifest without a PatientID");
            return;
        }
        String issuer = (String)TagD.getTagValue((TagReadable)patient, (int)0x100021, String.class);
        StringBuilder buf = new StringBuilder(wado.getBaseURL());
        buf.append("/studies?00100020=");
        buf.append(URLEncoder.encode(patientID, StandardCharsets.UTF_8));
        if (StringUtil.hasText((CharSequence)issuer)) {
            buf.append("&00100021=").append(URLEncoder.encode(issuer, StandardCharsets.UTF_8));
        }
        ManifestCompletion.appendIncludeField(buf, STUDY_FIELDS);
        for (Attributes studyDataset : ManifestCompletion.query(buf.toString(), params, wado, "patient studies")) {
            MediaSeriesGroup study = ManifestModelBuilder.buildStudy(studyDataset, params, patient);
            ManifestCompletion.completeStudy(params, wado, patient, study);
        }
    }

    static void completeStudy(ReaderParams params, WadoParameters wado, MediaSeriesGroup patient, MediaSeriesGroup study) {
        String studyUID = (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
        if (!StringUtil.hasText((CharSequence)studyUID)) {
            return;
        }
        StringBuilder buf = new StringBuilder(wado.getBaseURL());
        buf.append("/studies/").append(studyUID).append("/series");
        ManifestCompletion.appendIncludeField(buf, SERIES_FIELDS);
        for (Attributes seriesDataset : ManifestCompletion.query(buf.toString(), params, wado, "study series")) {
            DicomSeries dicomSeries = ManifestModelBuilder.buildSeries(seriesDataset, params, study, wado);
            ManifestModelBuilder.completeSeries(params, patient, study, dicomSeries, ManifestModelBuilder.getSeriesInstanceList(dicomSeries));
        }
    }

    private static List<Attributes> query(String url, ReaderParams params, WadoParameters wado, String context) {
        try {
            AuthMethod authMethod = params.wadoUri == null ? null : params.wadoUri.getAuthMethod();
            LOGGER.debug("QIDO-RS completion ({}): {}", (Object)context, (Object)url);
            return RsQueryResult.parseJSON(url, authMethod, new URLParameters(ManifestCompletion.queryHeaders(wado)));
        }
        catch (Exception e) {
            LOGGER.error("QIDO-RS completion failed for {}", (Object)context, (Object)e);
            return List.of();
        }
    }

    private static Map<String, String> queryHeaders(WadoParameters wado) {
        HashMap<String, String> headers = new HashMap<String, String>();
        if (wado.getHttpTaglist() != null) {
            for (HttpTag tag : wado.getHttpTaglist()) {
                headers.put(tag.getKey(), tag.getValue());
            }
        }
        if (StringUtil.hasText((CharSequence)wado.getWebLogin())) {
            headers.put("Authorization", "Basic " + wado.getWebLogin());
        }
        headers.putIfAbsent("Accept", "application/dicom+json");
        return headers;
    }

    private static void appendIncludeField(StringBuilder buf, String fields) {
        buf.append(buf.indexOf("?") < 0 ? (char)'?' : '&');
        buf.append("includefield=").append(fields);
    }
}

