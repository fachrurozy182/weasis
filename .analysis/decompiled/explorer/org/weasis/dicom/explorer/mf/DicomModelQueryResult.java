/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSpecialElement
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.mf.AbstractQueryResult
 *  org.weasis.dicom.mf.Patient
 *  org.weasis.dicom.mf.Series
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.mf.Study
 *  org.weasis.dicom.mf.WadoParameters
 */
package org.weasis.dicom.explorer.mf;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.mf.AbstractQueryResult;
import org.weasis.dicom.mf.Patient;
import org.weasis.dicom.mf.Series;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.Study;
import org.weasis.dicom.mf.WadoParameters;

public class DicomModelQueryResult
extends AbstractQueryResult {
    private final WadoParameters wadoParameters;

    public DicomModelQueryResult(DicomModel model, WadoParameters wadoParameters) {
        this(model, wadoParameters, null);
    }

    public DicomModelQueryResult(DicomModel model, WadoParameters wadoParameters, MediaSeriesGroup ... patient) {
        this.wadoParameters = Objects.requireNonNull(wadoParameters);
        this.init(model, patient);
    }

    public WadoParameters getWadoParameters() {
        return this.wadoParameters;
    }

    private void init(DicomModel model, MediaSeriesGroup ... patientGroups) {
        int imgNotAdd = 0;
        Collection<MediaSeriesGroup> pts = patientGroups == null || patientGroups.length == 0 ? model.getChildren(MediaSeriesGroupNode.rootNode) : Arrays.asList(patientGroups);
        for (MediaSeriesGroup patient : pts) {
            List dcmSpecElements = (List)patient.getTagValue(TagW.DicomSpecialElementList);
            Patient p = DicomModelQueryResult.getPatient(patient, this);
            for (MediaSeriesGroup study : model.getChildren(patient)) {
                Study st = DicomModelQueryResult.getStudy(study, p);
                for (MediaSeriesGroup series : model.getChildren(study)) {
                    Series s = DicomModelQueryResult.getSeries(series, st);
                    if (!(series instanceof MediaSeries)) continue;
                    WadoParameters wadoParams = (WadoParameters)series.getTagValue(TagW.WadoParameters);
                    if (wadoParams == null || !StringUtil.hasText((CharSequence)wadoParams.getBaseURL()) || !wadoParams.getBaseURL().equals(this.wadoParameters.getBaseURL())) {
                        imgNotAdd += ((MediaSeries)series).size(null);
                        continue;
                    }
                    if (DicomModel.isHiddenModality((MediaSeries)series)) {
                        String seriesInstanceUID = (String)TagD.getTagValue((TagReadable)series, (int)0x20000E, String.class);
                        for (DicomSpecialElement spel : dcmSpecElements) {
                            String seriesUID = (String)TagD.getTagValue((TagReadable)spel, (int)0x20000E, String.class);
                            if (!seriesInstanceUID.equals(seriesUID) || spel.getMediaReader().isEditableDicom()) continue;
                            this.buildInstance((MediaElement)spel, s);
                        }
                        continue;
                    }
                    for (MediaElement media : ((MediaSeries)series).getSortedMedias(null)) {
                        this.buildInstance(media, s);
                    }
                }
            }
        }
        this.removeItemsWithoutElements();
    }

    public static Patient getPatient(MediaSeriesGroup patient, AbstractQueryResult query) {
        String ispid;
        String id = (String)TagD.getTagValue((TagReadable)((TagReadable)Objects.requireNonNull(patient)), (int)0x100020, String.class);
        Patient p = query.getPatient(id, ispid = (String)TagD.getTagValue((TagReadable)patient, (int)0x100021, String.class));
        if (p == null) {
            p = new Patient(id, ispid);
            p.setPatientName((String)TagD.getTagValue((TagReadable)patient, (int)0x100010, String.class));
            LocalDate date = (LocalDate)TagD.getTagValue((TagReadable)patient, (int)0x100030, LocalDate.class);
            p.setPatientBirthDate(date == null ? null : TagD.formatDicomDate((LocalDate)date));
            p.setPatientSex((String)TagD.getTagValue((TagReadable)patient, (int)0x100040, String.class));
            query.addPatient(p);
        }
        return p;
    }

    public static Study getStudy(MediaSeriesGroup study, Patient patient) {
        String uid = (String)TagD.getTagValue((TagReadable)((TagReadable)Objects.requireNonNull(study)), (int)0x20000D, String.class);
        Study s = Objects.requireNonNull(patient).getStudy(uid);
        if (s == null) {
            s = new Study(uid);
            s.setStudyDescription((String)TagD.getTagValue((TagReadable)study, (int)528432, String.class));
            LocalDate date = (LocalDate)TagD.getTagValue((TagReadable)study, (int)524320, LocalDate.class);
            s.setStudyDate(date == null ? null : TagD.formatDicomDate((LocalDate)date));
            LocalTime time = (LocalTime)TagD.getTagValue((TagReadable)study, (int)524336, LocalTime.class);
            s.setStudyTime(time == null ? null : TagD.formatDicomTime((LocalTime)time));
            s.setAccessionNumber((String)TagD.getTagValue((TagReadable)study, (int)524368, String.class));
            s.setStudyID((String)TagD.getTagValue((TagReadable)study, (int)0x200010, String.class));
            s.setReferringPhysicianName((String)TagD.getTagValue((TagReadable)study, (int)524432, String.class));
            patient.addStudy(s);
        }
        return s;
    }

    public static Series getSeries(MediaSeriesGroup series, Study study) {
        String uid = (String)TagD.getTagValue((TagReadable)((TagReadable)Objects.requireNonNull(series)), (int)0x20000E, String.class);
        Series s = Objects.requireNonNull(study).getSeries(uid);
        if (s == null) {
            s = new Series(uid);
            s.setSeriesDescription((String)TagD.getTagValue((TagReadable)series, (int)528446, String.class));
            s.setSeriesNumber(StringUtil.getNullIfNull((Object)TagD.getTagValue((TagReadable)series, (int)0x200011, Integer.class)));
            s.setModality((String)TagD.getTagValue((TagReadable)series, (int)524384, String.class));
            s.setThumbnail((String)TagW.getTagValue((TagReadable)series, (TagW)TagW.DirectDownloadThumbnail, String.class));
            s.setWadoTransferSyntaxUID((String)TagW.getTagValue((TagReadable)series, (TagW)TagW.WadoTransferSyntaxUID, String.class));
            Integer rate = (Integer)TagW.getTagValue((TagReadable)series, (TagW)TagW.WadoCompressionRate, Integer.class);
            if (rate != null) {
                s.setWadoCompression(rate.intValue());
            }
            study.addSeries(s);
        }
        return s;
    }

    public void buildInstance(MediaElement media, Series s) {
        Integer frame;
        String sopUID;
        SopInstance sop;
        if (media != null && (sop = s.getSopInstance(sopUID = (String)TagD.getTagValue((TagReadable)media, (int)524312, String.class), frame = (Integer)TagD.getTagValue((TagReadable)media, (int)2097171, Integer.class))) == null) {
            sop = new SopInstance(sopUID, frame);
            sop.setDirectDownloadFile((String)TagW.getTagValue((TagReadable)media, (TagW)TagW.DirectDownloadFile, String.class));
            sop.setImageComments((String)TagD.getTagValue((TagReadable)media, (int)0x204000, String.class));
            s.addSopInstance(sop);
        }
    }
}

