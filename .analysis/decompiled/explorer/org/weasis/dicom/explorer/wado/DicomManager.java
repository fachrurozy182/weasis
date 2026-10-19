/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.TagView
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 */
package org.weasis.dicom.explorer.wado;

import java.util.ArrayList;
import java.util.List;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.tag.DicomFieldsView;

public class DicomManager {
    private static DicomManager instance;
    private boolean portableDirCache = true;
    private final List<DicomFieldsView.DicomData> limitedDicomTags = new ArrayList<DicomFieldsView.DicomData>();

    private DicomManager() {
        this.initRequiredDicomTags();
    }

    public static synchronized DicomManager getInstance() {
        if (instance == null) {
            instance = new DicomManager();
        }
        return instance;
    }

    private void initRequiredDicomTags() {
        TagView[] patient = new TagView[]{new TagView(new TagW[]{TagD.get((int)0x100010)}), new TagView(new TagW[]{TagD.get((int)0x100020)}), new TagView(new TagW[]{TagD.get((int)0x100021)}), new TagView(new TagW[]{TagD.get((int)0x100040)}), new TagView(new TagW[]{TagD.get((int)0x100030)})};
        TagView[] station = new TagView[]{new TagView(new TagW[]{TagD.get((int)524400)}), new TagView(new TagW[]{TagD.get((int)528528)}), new TagView(new TagW[]{TagD.get((int)528400)})};
        TagView[] study = new TagView[]{new TagView(new TagW[]{TagD.get((int)0x20000D)}), new TagView(new TagW[]{TagD.get((int)524320)}), new TagView(new TagW[]{TagD.get((int)524336)}), new TagView(new TagW[]{TagD.get((int)0x200010)}), new TagView(new TagW[]{TagD.get((int)524368)}), new TagView(new TagW[]{TagD.get((int)528432)}), new TagView(new TagW[]{TagD.get((int)3293184)})};
        TagView[] series = new TagView[]{new TagView(new TagW[]{TagD.get((int)0x20000E)}), new TagView(new TagW[]{TagD.get((int)524321)}), new TagView(new TagW[]{TagD.get((int)524337)}), new TagView(new TagW[]{TagD.get((int)0x200011)}), new TagView(new TagW[]{TagD.get((int)524384)}), new TagView(new TagW[]{TagD.get((int)524432)}), new TagView(new TagW[]{TagD.get((int)524416)}), new TagView(new TagW[]{TagD.get((int)528448)}), new TagView(new TagW[]{TagD.get((int)528446)}), new TagView(new TagW[]{TagD.get((int)1572885)})};
        TagView[] image = new TagView[]{new TagView(new TagW[]{TagD.get((int)524312)}), new TagView(TagD.getTagFromIDs((int[])new int[]{561159, 524296})), new TagView(new TagW[]{TagD.get((int)524310)}), new TagView(new TagW[]{TagD.get((int)131088)}), new TagView(new TagW[]{TagD.get((int)2097171)}), new TagView(new TagW[]{TagD.get((int)0x204000)}), new TagView(TagD.getTagFromIDs((int[])new int[]{2134130, 0x200062, 0x200060})), new TagView(new TagW[]{TagW.AnatomicRegion}), new TagView(new TagW[]{TagD.get((int)2621444)}), new TagView(new TagW[]{TagD.get((int)0x280002)}), new TagView(new TagW[]{TagD.get((int)2621699)}), new TagView(new TagW[]{TagD.get((int)2621457)}), new TagView(new TagW[]{TagD.get((int)2621456)}), new TagView(new TagW[]{TagD.get((int)2621696)}), new TagView(new TagW[]{TagD.get((int)2621697)})};
        TagView[] imgPlane = new TagView[]{new TagView(new TagW[]{TagD.get((int)2621488)}), new TagView(new TagW[]{TagD.get((int)2101313)}), new TagView(new TagW[]{TagD.get((int)1572944)}), new TagView(new TagW[]{TagD.get((int)0x200032)}), new TagView(new TagW[]{TagD.get((int)2097207)}), new TagView(new TagW[]{TagD.get((int)3293184)})};
        TagView[] imgAcq = new TagView[]{new TagView(new TagW[]{TagD.get((int)1572960)}), new TagView(new TagW[]{TagD.get((int)0x180010)})};
        this.limitedDicomTags.add(new DicomFieldsView.DicomData(Messages.getString("DicomFieldsView.pat"), patient, TagD.Level.PATIENT));
        this.limitedDicomTags.add(new DicomFieldsView.DicomData(Messages.getString("DicomFieldsView.station"), station, TagD.Level.SERIES));
        this.limitedDicomTags.add(new DicomFieldsView.DicomData(Messages.getString("DicomFieldsView.study"), study, TagD.Level.STUDY));
        this.limitedDicomTags.add(new DicomFieldsView.DicomData(Messages.getString("DicomFieldsView.series"), series, TagD.Level.SERIES));
        this.limitedDicomTags.add(new DicomFieldsView.DicomData(Messages.getString("DicomFieldsView.object"), image, TagD.Level.INSTANCE));
        this.limitedDicomTags.add(new DicomFieldsView.DicomData(Messages.getString("DicomFieldsView.plane"), imgPlane, TagD.Level.INSTANCE));
        this.limitedDicomTags.add(new DicomFieldsView.DicomData(Messages.getString("DicomFieldsView.acqu"), imgAcq, TagD.Level.INSTANCE));
    }

    public List<DicomFieldsView.DicomData> getLimitedDicomTags() {
        return this.limitedDicomTags;
    }

    public boolean isPortableDirCache() {
        return this.portableDirCache;
    }

    public void setPortableDirCache(boolean portableDirCache) {
        this.portableDirCache = portableDirCache;
    }
}

