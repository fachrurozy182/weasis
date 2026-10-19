/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.AttributeSource
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec.utils;

import java.time.LocalDate;
import java.util.Optional;
import org.dcm4che3.data.Attributes;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.AttributeSource;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TagD;

public class PatientComparator {
    private String issuerOfPatientID;
    private String patientId;
    private String name;
    private String birthdate;
    private String sex;

    public PatientComparator(Attributes item) {
        this.setPatientId(item.getString(0x100020));
        this.setIssuerOfPatientID(item.getString(0x100021));
        this.setName(item.getString(0x100010));
        this.setSex(item.getString(0x100040));
        this.setBirthdate(item.getString(0x100030));
    }

    public PatientComparator(AttributeSource source) {
        this.setPatientId(TagUtil.getTagAttribute((AttributeSource)source, (String)TagD.get(0x100020).getKeyword(), null));
        this.setIssuerOfPatientID(TagUtil.getTagAttribute((AttributeSource)source, (String)TagD.get(0x100021).getKeyword(), null));
        this.setName(TagUtil.getTagAttribute((AttributeSource)source, (String)TagD.get(0x100010).getKeyword(), null));
        this.setSex(TagUtil.getTagAttribute((AttributeSource)source, (String)TagD.get(0x100040).getKeyword(), null));
        this.setBirthdate(TagUtil.getTagAttribute((AttributeSource)source, (String)TagD.get(0x100030).getKeyword(), null));
    }

    public PatientComparator(TagReadable taggable) {
        this.setPatientId(TagD.getTagValue(taggable, 0x100020, String.class));
        this.setIssuerOfPatientID(TagD.getTagValue(taggable, 0x100021, String.class));
        this.setName(TagD.getTagValue(taggable, 0x100010, String.class));
        this.setSex(TagD.getTagValue(taggable, 0x100040, String.class));
        LocalDate date = TagD.getTagValue(taggable, 0x100030, LocalDate.class);
        this.setBirthdate(TagD.formatDicomDate(date));
    }

    public String buildPatientPseudoUID() {
        String property = GuiUtils.getUICore().getSystemPreferences().getProperty("patientComparator.buildPatientPseudoUID", null);
        if (StringUtil.hasText((CharSequence)property)) {
            String[] split;
            StringBuilder buffer = new StringBuilder();
            String[] stringArray = split = property.split(",");
            int n = stringArray.length;
            block14: for (int i = 0; i < n; ++i) {
                String string;
                switch (string = stringArray[i]) {
                    case "issuerOfPatientID": {
                        buffer.append(this.issuerOfPatientID);
                        continue block14;
                    }
                    case "patientId": {
                        buffer.append(this.patientId);
                        continue block14;
                    }
                    case "patientName": {
                        buffer.append(this.name);
                        continue block14;
                    }
                    case "patientBirthdate": {
                        buffer.append(this.birthdate);
                        continue block14;
                    }
                    case "patientSex": {
                        buffer.append(this.sex);
                    }
                }
            }
            return buffer.toString();
        }
        return this.patientId + this.issuerOfPatientID + this.name;
    }

    public String getIssuerOfPatientID() {
        return this.issuerOfPatientID;
    }

    public void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = Optional.ofNullable(issuerOfPatientID).orElse("").trim();
    }

    public String getPatientId() {
        return this.patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = Optional.ofNullable(patientId).orElse("UNKNOWN").trim();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = Optional.ofNullable(name).orElse("UNKNOWN").toUpperCase().trim();
    }

    public String getBirthdate() {
        return this.birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = Optional.ofNullable(birthdate).orElse("").trim();
    }

    public String getSex() {
        return this.sex;
    }

    public void setSex(String sex) {
        this.sex = Optional.ofNullable(sex).orElse("").trim();
    }
}

