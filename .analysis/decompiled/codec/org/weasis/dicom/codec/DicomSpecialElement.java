/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.core.api.media.data.SeriesComparator
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.Taggable
 *  org.weasis.core.util.StringUtil
 *  org.weasis.core.util.StringUtil$Suffix
 */
package org.weasis.dicom.codec;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.SeriesComparator;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.macro.SOPInstanceReferenceAndMAC;
import org.weasis.dicom.param.AttributeEditor;

public class DicomSpecialElement
extends MediaElement
implements DicomElement {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomSpecialElement.class);
    public static final SeriesComparator<DicomSpecialElement> ORDER_BY_DESCRIPTION = new SeriesComparator<DicomSpecialElement>(){

        public int compare(DicomSpecialElement arg0, DicomSpecialElement arg1) {
            return String.CASE_INSENSITIVE_ORDER.compare(arg0.getLabel(), arg1.getLabel());
        }
    };
    public static final SeriesComparator<DicomSpecialElement> ORDER_BY_DATE = new SeriesComparator<DicomSpecialElement>(){

        public int compare(DicomSpecialElement m1, DicomSpecialElement m2) {
            int comp;
            int comp2;
            LocalDateTime date1 = TagD.dateTime(524323, 524339, (TagReadable)m1);
            LocalDateTime date2 = TagD.dateTime(524323, 524339, (TagReadable)m2);
            if (date1 == null || date2 == null) {
                date1 = TagD.dateTime(524321, 524337, (TagReadable)m1);
                date2 = TagD.dateTime(524321, 524337, (TagReadable)m2);
            }
            if (date1 != null && date2 != null && (comp2 = date2.compareTo(date1)) != 0) {
                return comp2;
            }
            Integer val1 = TagD.getTagValue((TagReadable)m1, 0x200011, Integer.class);
            Integer val2 = TagD.getTagValue((TagReadable)m2, 0x200011, Integer.class);
            if (val1 != null && val2 != null && (comp = val1.compareTo(val2)) != 0) {
                return comp;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(m1.getLabel(), m2.getLabel());
        }
    };
    protected String label;

    public DicomSpecialElement(DicomMediaIO mediaIO) {
        super((MediaReader)mediaIO, null);
        this.initLabel();
        DcmMediaReader.addTimeZone((Taggable)this, mediaIO.getDicomObject());
    }

    protected String getLabelPrefix() {
        Integer val;
        StringBuilder buf = new StringBuilder();
        String modality = TagD.getTagValue((TagReadable)this, 524384, String.class);
        if (modality != null) {
            buf.append(modality);
            buf.append(" ");
        }
        if ((val = TagD.getTagValue((TagReadable)this, 2097171, Integer.class)) != null) {
            buf.append("[");
            buf.append(val);
            buf.append("] ");
        }
        return buf.toString();
    }

    protected void initLabel() {
        StringBuilder buf = new StringBuilder(this.getLabelPrefix());
        String desc = TagD.getTagValue((TagReadable)this, 528446, String.class);
        if (desc != null) {
            buf.append(desc);
        }
        this.label = buf.toString();
    }

    @Override
    public DicomMediaIO getMediaReader() {
        return (DicomMediaIO)super.getMediaReader();
    }

    public String getShortLabel() {
        return StringUtil.getTruncatedString((String)this.label, (int)50, (StringUtil.Suffix)StringUtil.Suffix.THREE_PTS);
    }

    public String getLabel() {
        return this.label;
    }

    public String toString() {
        String modality = TagD.getTagValue((TagReadable)this, 524384, String.class);
        int prefix = modality == null ? 0 : modality.length() + 1;
        String l = this.getShortLabel();
        return l.length() > prefix ? this.label.substring(prefix) : l;
    }

    @Override
    public Attributes saveToFile(File output, DicomElement.DicomExportParameters params) {
        return DicomSpecialElement.saveToFile(this, output, params.dicomEditors());
    }

    public static Attributes saveToFile(DicomElement dicom, File output, List<AttributeEditor> dicomEditors) {
        boolean hasTransformation;
        DcmMediaReader reader = dicom.getMediaReader();
        boolean bl = hasTransformation = dicomEditors != null && !dicomEditors.isEmpty();
        if (reader.isEditableDicom() || hasTransformation) {
            Attributes dcm = reader.getDicomObject();
            if (dcm != null) {
                Attributes attributes;
                DicomOutputStream out = new DicomOutputStream(output);
                try {
                    Attributes dataSet;
                    if (hasTransformation) {
                        dataSet = new Attributes(dcm);
                        dicomEditors.forEach(e -> e.apply(dataSet, null));
                    } else {
                        dataSet = dcm;
                    }
                    String dstTsuid = reader.getDicomMetaData().getTransferSyntaxUID();
                    if ("1.2.840.10008.1.2".equals(dstTsuid) || "1.2.840.10008.1.2.2".equals(dstTsuid)) {
                        dstTsuid = "1.2.840.10008.1.2";
                    }
                    out.writeDataset(dataSet.createFileMetaInformation(dstTsuid), dataSet);
                    attributes = dataSet;
                }
                catch (Throwable throwable) {
                    try {
                        try {
                            out.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                        throw throwable;
                    }
                    catch (IOException e2) {
                        LOGGER.error("Cannot write dicom ({}) into {}", new Object[]{dcm.getString(524312), output, e2});
                    }
                }
                out.close();
                return attributes;
            }
        } else {
            MediaElement.saveToFile((MediaReader)reader, (File)output);
            return new Attributes();
        }
        return null;
    }

    public static boolean isSopuidInReferencedSeriesSequence(Map<String, SOPInstanceReferenceAndMAC> seq, String sopUID, Integer dicomFrameNumber) {
        if (seq != null && StringUtil.hasText((CharSequence)sopUID) && seq.containsKey(sopUID)) {
            if (dicomFrameNumber != null) {
                int[] seqFrame;
                SOPInstanceReferenceAndMAC val = seq.get(sopUID);
                int[] nArray = seqFrame = val == null ? null : val.getReferencedFrameNumber();
                if (seqFrame == null || seqFrame.length == 0) {
                    return true;
                }
                for (int k : seqFrame) {
                    if (k != dicomFrameNumber) continue;
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
}

