/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.util.DicomUtils;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.geom.PatientOrientation;
import org.weasis.dicom.hp.HPComparator;
import org.weasis.dicom.hp.HPComparatorFactory;
import org.weasis.dicom.hp.HPImageBox;
import org.weasis.dicom.hp.HPImageSet;
import org.weasis.dicom.hp.HPSelector;
import org.weasis.dicom.hp.HPSelectorFactory;
import org.weasis.dicom.macro.Module;

public class HPDisplaySet
extends Module {
    private HPImageSet imageSet;
    private final List<HPImageBox> imageBoxes;
    private final List<HPSelector> filters;
    private final List<HPComparator> cmps;

    protected HPDisplaySet(Attributes attributes, HPImageSet imageSet) {
        super(attributes);
        this.imageSet = imageSet;
        Sequence imageBoxesSeq = attributes.getSequence(7471872);
        if (imageBoxesSeq == null || imageBoxesSeq.isEmpty()) {
            throw new IllegalArgumentException("Missing (0072,0300) Image Boxes Sequence");
        }
        int numImageBoxes = imageBoxesSeq.size();
        this.imageBoxes = new ArrayList<HPImageBox>(numImageBoxes);
        for (int i = 0; i < numImageBoxes; ++i) {
            this.imageBoxes.add(this.createHPImageBox((Attributes)imageBoxesSeq.get(i), numImageBoxes));
        }
        Sequence filterOpSeq = attributes.getSequence(7472128);
        if (filterOpSeq == null || filterOpSeq.isEmpty()) {
            this.filters = new ArrayList<HPSelector>(0);
        } else {
            int n = filterOpSeq.size();
            this.filters = new ArrayList<HPSelector>(n);
            for (int i = 0; i < n; ++i) {
                this.filters.add(HPSelectorFactory.createDisplaySetFilter((Attributes)filterOpSeq.get(i)));
            }
        }
        Sequence sortingOpSeq = attributes.getSequence(7472640);
        if (sortingOpSeq == null || sortingOpSeq.isEmpty()) {
            this.cmps = new ArrayList<HPComparator>(0);
        } else {
            int n = sortingOpSeq.size();
            this.cmps = new ArrayList<HPComparator>(n);
            for (int i = 0; i < n; ++i) {
                this.cmps.add(HPComparatorFactory.createHPComparator((Attributes)sortingOpSeq.get(i)));
            }
        }
    }

    public HPDisplaySet() {
        super(new Attributes());
        this.imageBoxes = new ArrayList<HPImageBox>();
        this.filters = new ArrayList<HPSelector>();
        this.cmps = new ArrayList<HPComparator>();
    }

    protected HPImageBox createHPImageBox(Attributes item, int numImageBoxes) {
        return new HPImageBox(item, numImageBoxes);
    }

    @Generated
    public final HPImageSet getImageSet() {
        return this.imageSet;
    }

    @Generated
    public void setImageSet(HPImageSet imageSet) {
        this.dcmItems.setInt(7471154, VR.US, imageSet.getImageSetNumber());
        this.imageSet = imageSet;
    }

    @Generated
    public List<HPImageBox> getImageBoxes() {
        return Collections.unmodifiableList(this.imageBoxes);
    }

    public void addImageBox(HPImageBox imageBox) {
        imageBox.setImageBoxNumber(this.imageBoxes.size() + 1);
        this.dcmItems.ensureSequence(7471872, 1).add(imageBox.getAttributes());
        this.imageBoxes.add(imageBox);
    }

    public boolean removeImageBox(HPImageBox imageBox) {
        int index = this.imageBoxes.indexOf(imageBox);
        if (index == -1) {
            return false;
        }
        this.removeSequenceItem(7471872, index);
        this.imageBoxes.remove(index);
        while (index < this.imageBoxes.size()) {
            this.imageBoxes.get(index).setImageBoxNumber(index + 1);
            ++index;
        }
        return true;
    }

    public void removeAllImageBoxes() {
        this.removeAllSequenceItems(7471872);
        this.imageBoxes.clear();
    }

    @Generated
    public List<HPSelector> getFilterOperations() {
        return Collections.unmodifiableList(this.filters);
    }

    public void addFilterOperation(HPSelector selector) {
        this.dcmItems.ensureSequence(7472128, 1).add(selector.getAttributes());
        this.filters.add(selector);
    }

    public boolean removeFilterOperation(HPSelector cmp) {
        int index = this.filters.indexOf(cmp);
        if (index == -1) {
            return false;
        }
        this.removeSequenceItem(7472128, index);
        this.filters.remove(index);
        return true;
    }

    public void removeAllFilterOperations() {
        this.removeAllSequenceItems(7472128);
        this.filters.clear();
    }

    @Generated
    public List<HPComparator> getSortingOperations() {
        return Collections.unmodifiableList(this.cmps);
    }

    public void addSortingOperation(HPComparator cmp) {
        this.dcmItems.ensureSequence(7472640, 1).add(cmp.getAttributes());
        this.cmps.add(cmp);
    }

    public boolean removeSortingOperation(HPComparator cmp) {
        int index = this.cmps.indexOf(cmp);
        if (index == -1) {
            return false;
        }
        this.removeSequenceItem(7472640, index);
        this.cmps.remove(index);
        return true;
    }

    public void removeAllSortingOperations() {
        this.removeAllSequenceItems(7472640);
        this.cmps.clear();
    }

    public boolean contains(Attributes o, int frame) {
        for (HPSelector selector : this.filters) {
            if (selector.matches(o, frame)) continue;
            return false;
        }
        return true;
    }

    public int compare(Attributes o1, int frame1, Attributes o2, int frame2) {
        int result = 0;
        int n = this.cmps.size();
        for (int i = 0; result == 0 && i < n; ++i) {
            HPComparator cmp = this.cmps.get(i);
            result = cmp.compare(o1, frame1, o2, frame2);
        }
        return result;
    }

    @Generated
    public Integer getDisplaySetNumber() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 0x720202, null);
    }

    @Generated
    public void setDisplaySetNumber(int displaySetNumber) {
        this.dcmItems.setInt(0x720202, VR.US, displaySetNumber);
    }

    @Generated
    public void setDisplaySetLabel(String label) {
        this.dcmItems.setString(7471619, VR.LO, label);
    }

    @Generated
    public String getDisplaySetLabel() {
        return this.dcmItems.getString(7471619);
    }

    @Generated
    public Integer getDisplaySetPresentationGroup() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471620, null);
    }

    @Generated
    public void setDisplaySetPresentationGroup(int group) {
        this.dcmItems.setInt(7471620, VR.US, group);
    }

    @Generated
    public String getBlendingOperationType() {
        return this.dcmItems.getString(7472384);
    }

    @Generated
    public void setBlendingOperationType(String type) {
        this.dcmItems.setString(7472384, VR.CS, type);
    }

    @Generated
    public String getReformattingOperationType() {
        return this.dcmItems.getString(7472400);
    }

    @Generated
    public void setReformattingOperationType(String type) {
        this.dcmItems.setString(7472400, VR.CS, type);
    }

    @Generated
    public Double getReformattingThickness() {
        return DicomUtils.getDoubleFromDicomElement(this.dcmItems, 7472402, null);
    }

    @Generated
    public void setReformattingThickness(double thickness) {
        this.dcmItems.setDouble(7472402, VR.FD, thickness);
    }

    @Generated
    public Double getReformattingInterval() {
        return DicomUtils.getDoubleFromDicomElement(this.dcmItems, 7472404, null);
    }

    @Generated
    public void setReformattingInterval(double interval) {
        this.dcmItems.setDouble(7472404, VR.FD, interval);
    }

    @Generated
    public String getReformattingOperationInitialViewDirection() {
        return this.dcmItems.getString(7472406);
    }

    @Generated
    public void setReformattingOperationInitialViewDirection(String direction) {
        this.dcmItems.setString(7472406, VR.CS, direction);
    }

    @Generated
    public String[] get3DRenderingType() {
        return this.dcmItems.getStrings(7472416);
    }

    @Generated
    public void set3DRenderingType(String[] type) {
        this.dcmItems.setString(7472416, VR.CS, type);
    }

    @Generated
    public String[] getRowDisplaySetPatientOrientation() {
        return this.dcmItems.getStrings(0x720700);
    }

    @Generated
    public void setDisplaySetPatientOrientation(PatientOrientation.Biped row, PatientOrientation.Biped col) {
        this.dcmItems.setString(0x720700, VR.CS, row.name(), col.name());
    }

    @Generated
    public void setDisplaySetPatientOrientation(PatientOrientation.Quadruped row, PatientOrientation.Quadruped col) {
        this.dcmItems.setString(0x720700, VR.CS, row.name(), col.name());
    }

    @Generated
    public String getVOIType() {
        return this.dcmItems.getString(0x720702);
    }

    @Generated
    public void setVOIType(String type) {
        this.dcmItems.setString(0x720702, VR.CS, type);
    }

    @Generated
    public String getPseudoColorType() {
        return this.dcmItems.getString(7472900);
    }

    @Generated
    public void setPseudoColorType(String type) {
        this.dcmItems.setString(7472900, VR.CS, type);
    }

    @Generated
    public String getShowGrayscaleInverted() {
        return this.dcmItems.getString(7472902);
    }

    @Generated
    public void setShowGrayscaleInverted(String flag) {
        this.dcmItems.setString(7472902, VR.CS, flag);
    }

    @Generated
    public String getShowImageTrueSizeFlag() {
        return this.dcmItems.getString(7472912);
    }

    @Generated
    public void setShowImageTrueSizeFlag(String flag) {
        this.dcmItems.setString(7472912, VR.CS, flag);
    }

    @Generated
    public String getShowGraphicAnnotationFlag() {
        return this.dcmItems.getString(7472914);
    }

    @Generated
    public void setShowGraphicAnnotationFlag(String flag) {
        this.dcmItems.setString(7472914, VR.CS, flag);
    }

    @Generated
    public String getShowAcquisitionTechniquesFlag() {
        return this.dcmItems.getString(7472918);
    }

    @Generated
    public void setShowAcquisitionTechniquesFlag(String flag) {
        this.dcmItems.setString(7472918, VR.CS, flag);
    }

    @Generated
    public String getDisplaySetPresentationGroupDescription() {
        return this.dcmItems.getString(7471622);
    }

    @Generated
    public void setDisplaySetPresentationGroupDescription(String description) {
        this.dcmItems.setString(7471622, VR.CS, description);
    }
}

