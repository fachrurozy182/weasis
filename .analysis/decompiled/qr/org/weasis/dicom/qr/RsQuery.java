/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.util.TagUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 *  org.weasis.dicom.codec.utils.DicomMediaUtils
 *  org.weasis.dicom.codec.utils.PatientComparator
 *  org.weasis.dicom.explorer.DicomModel
 *  org.weasis.dicom.explorer.rs.RsQueryResult
 *  org.weasis.dicom.param.DicomParam
 */
package org.weasis.dicom.qr;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.DicomMediaUtils;
import org.weasis.dicom.codec.utils.PatientComparator;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.rs.RsQueryResult;
import org.weasis.dicom.param.DicomParam;

public class RsQuery
implements Callable<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RsQuery.class);
    private final DicomModel dicomModel;
    private final Properties properties;
    private final Map<String, String> queryHeaders;
    protected final List<DicomParam> queries;
    private final AuthMethod authMethod;

    public RsQuery(DicomModel dicomModel, Properties properties, List<DicomParam> queries, AuthMethod authMethod, Map<String, String> queryHeaders) {
        this.dicomModel = Objects.requireNonNull(dicomModel);
        this.properties = Objects.requireNonNull(properties);
        this.queries = Objects.requireNonNull(queries);
        String url = properties.getProperty("dicomweb.url");
        if (StringUtil.hasText((CharSequence)url)) {
            if (url.endsWith("/")) {
                properties.setProperty("dicomweb.url", url.substring(0, url.length() - 1));
            }
        } else {
            throw new IllegalArgumentException("DICOMweb URL cannot be null");
        }
        this.authMethod = authMethod;
        this.queryHeaders = queryHeaders == null ? Collections.emptyMap() : queryHeaders;
    }

    public DicomModel getDicomModel() {
        return this.dicomModel;
    }

    public String getBaseUrl() {
        return this.properties.getProperty("dicomweb.url");
    }

    @Override
    public Boolean call() throws Exception {
        StringBuilder buf = new StringBuilder(this.getBaseUrl());
        buf.append("/studies?");
        for (int i = 0; i < this.queries.size(); ++i) {
            String value;
            DicomParam query = this.queries.get(i);
            StringJoiner joiner = new StringJoiner(",");
            if (query.getValues() != null) {
                for (String v : query.getValues()) {
                    if (!StringUtil.hasText((CharSequence)v)) continue;
                    String encode = URLEncoder.encode(v, StandardCharsets.UTF_8);
                    joiner.add(encode);
                }
            }
            if (!StringUtil.hasText((CharSequence)(value = joiner.toString()))) continue;
            buf.append(TagUtils.toHexString((int)query.getTag()));
            buf.append("=");
            if (0x100020 == query.getTag()) {
                int beginIndex = value.indexOf("^^^");
                String patientVal = beginIndex <= 0 ? value : value.substring(0, beginIndex);
                buf.append(patientVal);
                if (beginIndex > 0) {
                    buf.append("&00100021=");
                    buf.append(value.substring(beginIndex + 3));
                }
            } else {
                buf.append(value);
            }
            if (i + 1 >= this.queries.size()) continue;
            buf.append("&");
        }
        this.buildQueries(buf);
        return true;
    }

    public void buildQueries(StringBuilder buf) {
        try {
            buf.append(RsQueryResult.STUDY_QUERY);
            buf.append(this.properties.getProperty("query.ext", ""));
            buf.append(this.properties.getProperty("page.size", ""));
            LOGGER.debug("QIDO-RS request: {}", (Object)buf);
            List studies = RsQueryResult.parseJSON((String)buf.toString(), (AuthMethod)this.authMethod, (URLParameters)new URLParameters(this.queryHeaders));
            for (Attributes studyDataSet : studies) {
                this.fillSeries(studyDataSet);
            }
        }
        catch (Exception e) {
            LOGGER.error("QIDO-RS", (Throwable)e);
        }
    }

    private void fillSeries(Attributes studyDataSet) {
        String studyUID = studyDataSet.getString(0x20000D);
        if (StringUtil.hasText((CharSequence)studyUID)) {
            RsQuery.populateDicomModel(this.dicomModel, studyDataSet);
        }
    }

    static void populateDicomModel(DicomModel dicomModel, Attributes item) {
        String studyUID;
        Object study;
        PatientComparator patientComparator = new PatientComparator(item);
        String patientPseudoUID = patientComparator.buildPatientPseudoUID();
        Object patient = dicomModel.getHierarchyNode(MediaSeriesGroupNode.rootNode, (Object)patientPseudoUID);
        if (patient == null) {
            patient = new MediaSeriesGroupNode(TagW.PatientPseudoUID, patientPseudoUID, DicomModel.patient.tagView()){

                public String toString() {
                    return RsQuery.getDisplayValue(this, 0x100010) + " [" + RsQuery.getDisplayValue(this, 0x100020) + "] " + RsQuery.getDisplayValue(this, 0x100030) + " " + RsQuery.getDisplayValue(this, 0x100040);
                }
            };
            DicomMediaUtils.writeMetaData((MediaSeriesGroup)patient, (Attributes)item);
            dicomModel.addHierarchyNode(MediaSeriesGroupNode.rootNode, patient);
        }
        if ((study = dicomModel.getHierarchyNode(patient, (Object)(studyUID = item.getString(0x20000D)))) == null) {
            study = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.STUDY), studyUID, DicomModel.study.tagView()){

                public String toString() {
                    StringBuilder buf = new StringBuilder(RsQuery.getDisplayValue(this, 528432));
                    buf.append(" [");
                    buf.append(RsQuery.getDisplayValue(this, 524385));
                    buf.append("] ");
                    LocalDateTime studyDate = TagD.dateTime((int)524320, (int)524336, (TagReadable)this);
                    if (studyDate != null) {
                        buf.append(TagUtil.formatDateTime((TemporalAccessor)studyDate));
                        buf.append(" ");
                    }
                    buf.append(RsQuery.getDisplayValue(this, 524368));
                    return buf.toString();
                }
            };
            DicomMediaUtils.writeMetaData((MediaSeriesGroup)study, (Attributes)item);
            dicomModel.addHierarchyNode(patient, study);
        }
    }

    private static String getDisplayValue(MediaSeriesGroupNode node, int tagID) {
        Object value;
        TagW tag = TagD.get((int)tagID);
        if (tag != null && (value = node.getTagValue(tag)) != null) {
            String format = tag.addGMTOffset(null, (TagReadable)node);
            return tag.getFormattedTagValue(value, format);
        }
        return "";
    }
}
