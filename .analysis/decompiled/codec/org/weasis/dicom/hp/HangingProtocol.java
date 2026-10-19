/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.hp.HPDefinition;
import org.weasis.dicom.hp.HPDisplaySet;
import org.weasis.dicom.hp.HPImageSet;
import org.weasis.dicom.hp.HPNavigationGroup;
import org.weasis.dicom.hp.HPScreenDefinition;
import org.weasis.dicom.hp.HPScrollingGroup;
import org.weasis.dicom.hp.HPSelector;
import org.weasis.dicom.hp.HPSelectorFactory;
import org.weasis.dicom.hp.spi.HPCategoryService;
import org.weasis.dicom.hp.spi.HPComparatorCategoryService;
import org.weasis.dicom.hp.spi.HPRegistry;
import org.weasis.dicom.hp.spi.HPSelectorCategoryService;
import org.weasis.dicom.macro.Code;
import org.weasis.dicom.macro.Module;
import org.weasis.dicom.macro.SOPInstanceReference;

public class HangingProtocol
extends Module {
    private final List<HPDefinition> definitions = new ArrayList<HPDefinition>();
    private final List<HPScreenDefinition> screenDefs = new ArrayList<HPScreenDefinition>();
    private final List<HPImageSet> imageSets = new ArrayList<HPImageSet>();
    private final List<HPDisplaySet> displaySets = new ArrayList<HPDisplaySet>();
    private final List<HPScrollingGroup> scrollingGroups = new ArrayList<HPScrollingGroup>();
    private final List<HPNavigationGroup> navigationGroups = new ArrayList<HPNavigationGroup>();
    private int maxPresGroup = 0;

    public HangingProtocol(Attributes attributes) {
        super(attributes);
        this.init();
    }

    public HangingProtocol() {
        super(new Attributes());
        this.initDocument(null, null);
    }

    public HangingProtocol(HangingProtocol source) {
        super(new Attributes(source.getAttributes()));
        this.maxPresGroup = source.maxPresGroup;
        this.initDocument(this.getSOPClassUID(), this.getSOPInstanceUID());
        this.init();
    }

    protected void initDocument(String cuid, String refUid) {
        SOPInstanceReference refSOP = new SOPInstanceReference();
        if (refUid != null) {
            refSOP.setReferencedSOPInstanceUID(refUid);
        }
        refSOP.setReferencedSOPClassUID(cuid != null ? cuid : "1.2.840.10008.5.1.4.38.1");
        this.setSourceHangingProtocol(refSOP);
        this.setSOPInstanceUID(UIDUtils.createUID());
    }

    private void init() {
        this.initHangingProtocolDefinition();
        this.initNominalScreenDefinition();
        this.initImageSets();
        this.initDisplaySets();
        this.initScrollingGroups();
        this.initNavigationGroups();
    }

    @Generated
    public void writeToDicomFile(Path outputPath) throws IOException {
        HangingProtocol.validateOutputPath(outputPath);
        FileUtil.prepareToWriteFile((Path)outputPath);
        try (DicomOutputStream dos = new DicomOutputStream(outputPath.toFile());){
            Attributes fmi = this.dcmItems.createFileMetaInformation("1.2.840.10008.1.2.1");
            dos.writeDataset(fmi, this.dcmItems);
        }
    }

    @Generated
    private static void validateOutputPath(Path outputPath) {
        if (StringUtil.hasText((CharSequence)outputPath.toString())) {
            throw new IllegalArgumentException("Output path cannot be null or empty");
        }
    }

    @Generated
    protected HPImageSet createImageSet(List<HPSelector> selectors, Attributes dcmobj) {
        return new HPImageSet(selectors, dcmobj);
    }

    @Generated
    protected HPDisplaySet createDisplaySet(Attributes ds, HPImageSet is) {
        return new HPDisplaySet(ds, is);
    }

    @Generated
    protected HPDefinition createHangingProtocolDefinition(Attributes dcmobj) {
        return new HPDefinition(dcmobj);
    }

    @Generated
    protected HPScreenDefinition createNominalScreenDefinition(Attributes item) {
        return new HPScreenDefinition(item);
    }

    @Generated
    protected HPNavigationGroup createNavigationGroup(Attributes dcmobj) {
        return new HPNavigationGroup(dcmobj, this.displaySets);
    }

    @Generated
    protected HPScrollingGroup createScrollingGroup(Attributes dssg) {
        return new HPScrollingGroup(dssg, this.displaySets);
    }

    @Generated
    public String getHangingProtocolName() {
        return this.dcmItems.getString(0x720002);
    }

    @Generated
    public void setHangingProtocolName(String name) {
        this.dcmItems.setString(0x720002, VR.SH, name);
    }

    @Generated
    public String getHangingProtocolDescription() {
        return this.dcmItems.getString(7471108);
    }

    @Generated
    public void setHangingProtocolDescription(String description) {
        this.dcmItems.setString(7471108, VR.LO, description);
    }

    @Generated
    public String getHangingProtocolLevel() {
        return this.dcmItems.getString(7471110);
    }

    @Generated
    public void setHangingProtocolLevel(String level) {
        this.dcmItems.setString(7471110, VR.CS, level);
    }

    @Generated
    public String getHangingProtocolCreator() {
        return this.dcmItems.getString(7471112);
    }

    @Generated
    public void setHangingProtocolCreator(String creator) {
        this.dcmItems.setString(7471112, VR.LO, creator);
    }

    @Generated
    public Date getHangingProtocolCreationDateTime() {
        return this.dcmItems.getDate(7471114);
    }

    @Generated
    public void setHangingProtocolCreationDateTime(Date datetime) {
        this.dcmItems.setDate(7471114, VR.DT, datetime);
    }

    @Generated
    public Integer getNumberOfPriorsReferenced() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471124, null);
    }

    @Generated
    public void setNumberOfPriorsReferenced(int priors) {
        this.dcmItems.setInt(7471124, VR.US, priors);
    }

    @Generated
    public Integer getNumberOfScreens() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471360, null);
    }

    @Generated
    public void setNumberOfScreens(int screens) {
        this.dcmItems.setInt(7471360, VR.US, screens);
    }

    @Generated
    public Code getHangingProtocolUserIdentificationCode() {
        return Code.getNestedCode(this.dcmItems, 7471118);
    }

    @Generated
    public void setHangingProtocolUserIdentificationCodeSequence(Code user) {
        this.dcmItems.ensureSequence(7471166, 1).add(user.getAttributes());
    }

    @Generated
    public SOPInstanceReference getSourceHangingProtocol() {
        Attributes item = this.dcmItems.getNestedDataset(7471122);
        return item != null ? new SOPInstanceReference(item) : null;
    }

    @Generated
    public void setSourceHangingProtocol(SOPInstanceReference sop) {
        this.dcmItems.ensureSequence(7471122, 1).add(sop.getAttributes());
    }

    @Generated
    public String getHangingProtocolUserGroupName() {
        return this.dcmItems.getString(7471120);
    }

    @Generated
    public void setHangingProtocolUserGroupName(String name) {
        this.dcmItems.setString(7471120, VR.LO, name);
    }

    @Generated
    public String getPartialDataDisplayHandling() {
        return this.dcmItems.getString(7471624);
    }

    @Generated
    public void setPartialDataDisplayHandling(String type) {
        this.dcmItems.setString(7471624, VR.CS, type);
    }

    @Generated
    public List<HPDefinition> getHangingProtocolDefinitions() {
        return Collections.unmodifiableList(this.definitions);
    }

    public void addHangingProtocolDefinition(HPDefinition def) {
        if (def == null) {
            throw new NullPointerException();
        }
        this.dcmItems.ensureSequence(7471116, 1).add(def.getAttributes());
        this.definitions.add(def);
    }

    public boolean removeHangingProtocolDefinition(HPDefinition def) {
        if (def == null) {
            throw new NullPointerException();
        }
        int index = this.definitions.indexOf(def);
        if (index == -1) {
            return false;
        }
        this.dcmItems.ensureSequence(7471116, 1).add(def.getAttributes());
        this.definitions.remove(index);
        return true;
    }

    public void removeAllHangingProtocolDefinition() {
        this.removeAllSequenceItems(7471116);
        this.definitions.clear();
    }

    @Generated
    public List<HPImageSet> getImageSets() {
        return Collections.unmodifiableList(this.imageSets);
    }

    public HPImageSet addNewImageSet(HPImageSet shareSelectors) {
        int number = this.imageSets.size() + 1;
        HPImageSet is = shareSelectors != null ? new HPImageSet(shareSelectors, number) : new HPImageSet(number);
        is.setImageSetNumber(this.imageSets.size() + 1);
        this.imageSets.add(is);
        return is;
    }

    public boolean removeImageSet(HPImageSet imageSet) {
        if (imageSet == null) {
            throw new NullPointerException();
        }
        int index = this.imageSets.indexOf(imageSet);
        if (index == -1) {
            return false;
        }
        for (HPDisplaySet hpDisplaySet : this.getDisplaySetsOfImageSet(imageSet)) {
            this.removeDisplaySet(hpDisplaySet);
        }
        Attributes tbis = imageSet.getAttributes();
        Attributes is = tbis.getParent();
        Sequence tbissq = is.getSequence(7471152);
        if (tbissq != null) {
            tbissq.remove(tbis);
            if (tbissq.isEmpty()) {
                this.removeSequenceItem(0x720020, is);
            }
        }
        this.imageSets.remove(index);
        while (index < this.imageSets.size()) {
            HPImageSet otherImageSet = this.imageSets.get(index);
            otherImageSet.setImageSetNumber(index + 1);
            for (HPDisplaySet hpDisplaySet : this.getDisplaySetsOfImageSet(otherImageSet)) {
                hpDisplaySet.setImageSet(otherImageSet);
            }
            ++index;
        }
        return true;
    }

    public void removeAllImageSets() {
        this.removeAllSequenceItems(0x720020);
        this.imageSets.clear();
        this.removeAllDisplaySets();
    }

    @Generated
    public List<HPScreenDefinition> getNominalScreenDefinitions() {
        return Collections.unmodifiableList(this.screenDefs);
    }

    public void addNominalScreenDefinition(HPScreenDefinition def) {
        if (def == null) {
            throw new NullPointerException();
        }
        this.dcmItems.ensureSequence(7471362, 1).add(def.getAttributes());
        this.screenDefs.add(def);
    }

    public boolean removeNominalScreenDefinition(HPScreenDefinition def) {
        if (def == null) {
            throw new NullPointerException();
        }
        int index = this.screenDefs.indexOf(def);
        if (index == -1) {
            return false;
        }
        this.removeSequenceItem(7471362, index);
        this.screenDefs.remove(index);
        return true;
    }

    public void removeAllNominalScreenDefinitions() {
        this.removeAllSequenceItems(7471362);
        this.screenDefs.clear();
    }

    @Generated
    public int getNumberOfPresentationGroups() {
        return this.maxPresGroup;
    }

    public List<HPDisplaySet> getDisplaySetsOfPresentationGroup(int pgNo) {
        ArrayList<HPDisplaySet> result = new ArrayList<HPDisplaySet>(this.displaySets.size());
        for (HPDisplaySet ds : this.displaySets) {
            if (ds.getDisplaySetPresentationGroup() != pgNo) continue;
            result.add(ds);
        }
        return result;
    }

    public List<HPDisplaySet> getDisplaySetsOfImageSet(HPImageSet is) {
        ArrayList<HPDisplaySet> result = new ArrayList<HPDisplaySet>(this.displaySets.size());
        for (HPDisplaySet ds : this.displaySets) {
            if (ds.getImageSet() != is) continue;
            result.add(ds);
        }
        return result;
    }

    public String getDisplaySetPresentationGroupDescription(int pgNo) {
        for (HPDisplaySet ds : this.displaySets) {
            String desc;
            if (ds.getDisplaySetPresentationGroup() != pgNo || (desc = ds.getDisplaySetPresentationGroupDescription()) == null) continue;
            return desc;
        }
        return null;
    }

    @Generated
    public List<HPDisplaySet> getDisplaySets() {
        return Collections.unmodifiableList(this.displaySets);
    }

    public HPDisplaySet addNewDisplaySet(HPImageSet imageSet, HPDisplaySet prototype) {
        if (imageSet == null) {
            throw new NullPointerException("imageSet");
        }
        if (!this.imageSets.contains(imageSet)) {
            throw new IllegalArgumentException("imageSet does not belongs to this HP object");
        }
        Attributes dcmobj = new Attributes();
        if (prototype != null) {
            dcmobj.addAll(prototype.getAttributes());
        }
        dcmobj.setInt(7471154, VR.US, imageSet.getImageSetNumber());
        HPDisplaySet displaySet = this.createDisplaySet(dcmobj, imageSet);
        this.doAddDisplaySet(displaySet);
        return displaySet;
    }

    public void addDisplaySet(HPDisplaySet displaySet) {
        if (displaySet == null) {
            throw new NullPointerException("displaySet");
        }
        this.doAddDisplaySet(displaySet);
    }

    protected void doAddDisplaySet(HPDisplaySet displaySet) {
        int group;
        displaySet.setDisplaySetNumber(this.displaySets.size() + 1);
        Integer presentationGroup = displaySet.getDisplaySetPresentationGroup();
        int n = group = presentationGroup == null ? 0 : presentationGroup;
        if (group == 0) {
            group = Math.max(this.maxPresGroup, 1);
            displaySet.setDisplaySetPresentationGroup(group);
        }
        this.maxPresGroup = Math.max(this.maxPresGroup, group);
        this.dcmItems.ensureSequence(0x720200, 1).add(displaySet.getAttributes());
        this.displaySets.add(displaySet);
    }

    public boolean removeDisplaySet(HPDisplaySet displaySet) {
        if (displaySet == null) {
            throw new NullPointerException();
        }
        int index = this.displaySets.indexOf(displaySet);
        if (index == -1) {
            return false;
        }
        this.removeSequenceItem(0x720200, index);
        this.displaySets.remove(index);
        for (int i = index; i < this.displaySets.size(); ++i) {
            this.displaySets.get(i).setDisplaySetNumber(i + 1);
        }
        this.updateScrollingGroups(displaySet);
        this.updateNavigationGroups(displaySet);
        return true;
    }

    private void updateScrollingGroups(HPDisplaySet displaySet) {
        int index = 0;
        Iterator<HPScrollingGroup> iterator = this.scrollingGroups.iterator();
        while (iterator.hasNext()) {
            HPScrollingGroup group = iterator.next();
            if (group.removeDisplaySet(displaySet) && !group.isValid()) {
                this.removeSequenceItem(7471632, index--);
                iterator.remove();
            } else {
                group.updateAttributes();
            }
            ++index;
        }
    }

    private void updateNavigationGroups(HPDisplaySet displaySet) {
        int index = 0;
        Iterator<HPNavigationGroup> iterator = this.navigationGroups.iterator();
        while (iterator.hasNext()) {
            boolean shouldRemove;
            HPNavigationGroup group = iterator.next();
            boolean bl = shouldRemove = group.removeReferenceDisplaySet(displaySet) && !group.isValid() || group.getNavigationDisplaySet() == displaySet;
            if (shouldRemove) {
                if (group.getNavigationDisplaySet() == displaySet) {
                    group.setNavigationDisplaySet(null);
                }
                this.removeSequenceItem(7471636, index--);
                iterator.remove();
            } else {
                group.updateAttributes();
            }
            ++index;
        }
    }

    public void removeAllDisplaySets() {
        this.removeAllSequenceItems(0x720200);
        this.displaySets.clear();
        this.removeAllScrollingGroups();
        this.removeAllNavigationGroups();
        this.maxPresGroup = 0;
    }

    @Generated
    public List<HPScrollingGroup> getScrollingGroups() {
        return this.maskNull(this.scrollingGroups);
    }

    public void addScrollingGroup(HPScrollingGroup scrollingGroup) {
        this.dcmItems.ensureSequence(7471632, 1).add(scrollingGroup.getAttributes());
        this.scrollingGroups.add(scrollingGroup);
    }

    public boolean removeScrollingGroup(HPScrollingGroup scrollingGroup) {
        if (scrollingGroup == null) {
            throw new NullPointerException();
        }
        int index = this.scrollingGroups.indexOf(scrollingGroup);
        if (index == -1) {
            return false;
        }
        this.removeSequenceItem(7471632, index);
        this.scrollingGroups.remove(index);
        return true;
    }

    public void removeAllScrollingGroups() {
        this.dcmItems.remove(7471632);
        this.scrollingGroups.clear();
    }

    @Generated
    public List<HPNavigationGroup> getNavigationGroups() {
        return this.maskNull(this.navigationGroups);
    }

    @Generated
    private <T> List<T> maskNull(List<T> list) {
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    public void addNavigationGroup(HPNavigationGroup navigationGroup) {
        this.dcmItems.ensureSequence(7471636, 1).add(navigationGroup.getAttributes());
        this.navigationGroups.add(navigationGroup);
    }

    public boolean removeNavigationGroup(HPNavigationGroup navigationGroup) {
        if (navigationGroup == null) {
            throw new NullPointerException();
        }
        int index = this.navigationGroups.indexOf(navigationGroup);
        if (index == -1) {
            return false;
        }
        this.removeSequenceItem(7471636, index);
        this.navigationGroups.remove(index);
        return true;
    }

    public void removeAllNavigationGroups() {
        this.dcmItems.remove(7471636);
        this.navigationGroups.clear();
    }

    private void initNavigationGroups() {
        Sequence nis = this.dcmItems.getSequence(7471636);
        if (nis == null || nis.isEmpty()) {
            return;
        }
        for (Attributes ni : nis) {
            this.navigationGroups.add(this.createNavigationGroup(ni));
        }
    }

    private void initScrollingGroups() {
        Sequence ssq = this.dcmItems.getSequence(7471632);
        if (ssq == null || ssq.isEmpty()) {
            return;
        }
        for (Attributes value : ssq) {
            this.scrollingGroups.add(this.createScrollingGroup(value));
        }
    }

    private void initDisplaySets() {
        Sequence dssq = this.dcmItems.getSequence(0x720200);
        if (dssq == null || dssq.isEmpty()) {
            return;
        }
        for (Attributes ds : dssq) {
            HPImageSet is;
            if (ds.getInt(0x720202, 0) != this.displaySets.size() + 1) {
                throw new IllegalArgumentException("Missing or invalid (0072,0202) Display Set Number: " + ds.getString(0x720202));
            }
            int dspg = ds.getInt(7471620, 0);
            if (dspg == 0) {
                throw new IllegalArgumentException("Missing or invalid (0072,0204) Display Set Presentation Group: " + ds.getString(7471620));
            }
            this.maxPresGroup = Math.max(this.maxPresGroup, dspg);
            try {
                is = this.imageSets.get(ds.getInt(7471154, 0) - 1);
            }
            catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Missing or invalid (0072,0032) Image Set Number: " + ds.getString(7471154));
            }
            this.displaySets.add(this.createDisplaySet(ds, is));
        }
    }

    private void initImageSets() {
        Sequence issq = this.dcmItems.getSequence(0x720020);
        if (issq == null || issq.isEmpty()) {
            return;
        }
        for (Attributes is : issq) {
            Sequence isssq = is.getSequence(0x720022);
            if (isssq == null) {
                throw new IllegalArgumentException("Missing (0072,0022) Image Set Selector Sequence");
            }
            int isssqCount = isssq.size();
            ArrayList<HPSelector> selectors = new ArrayList<HPSelector>(isssqCount);
            for (Attributes value : isssq) {
                selectors.add(HPSelectorFactory.createImageSetSelector(value));
            }
            Sequence tbissq = is.getSequence(7471152);
            if (tbissq == null) {
                throw new IllegalArgumentException("Missing (0072,0030) Time Based Image Sets Sequence");
            }
            for (Attributes timeBasedSelector : tbissq) {
                if (timeBasedSelector.getInt(7471154, 0) != this.imageSets.size() + 1) {
                    throw new IllegalArgumentException("Missing or invalid (0072,0032) Image Set Number: " + timeBasedSelector.getString(7471154));
                }
                this.imageSets.add(this.createImageSet(selectors, timeBasedSelector));
            }
        }
    }

    private void initNominalScreenDefinition() {
        Sequence nsdsq = this.dcmItems.getSequence(7471362);
        if (nsdsq == null || nsdsq.isEmpty()) {
            return;
        }
        for (Attributes value : nsdsq) {
            this.screenDefs.add(this.createNominalScreenDefinition(value));
        }
    }

    private void initHangingProtocolDefinition() {
        Sequence defsq = this.dcmItems.getSequence(7471116);
        if (defsq == null || defsq.isEmpty()) {
            return;
        }
        for (Attributes value : defsq) {
            this.definitions.add(this.createHangingProtocolDefinition(value));
        }
    }

    @Generated
    public static HPSelectorCategoryService getHPSelectorSpi(String category) {
        return (HPSelectorCategoryService)HangingProtocol.getHPCategorySpi(HPSelectorCategoryService.class, category);
    }

    @Generated
    public static HPComparatorCategoryService getHPComparatorSpi(String category) {
        return (HPComparatorCategoryService)HangingProtocol.getHPCategorySpi(HPComparatorCategoryService.class, category);
    }

    @Generated
    private static <T extends HPCategoryService> HPCategoryService getHPCategorySpi(Class<T> serviceClass, String category) {
        Iterator<HPCategoryService> iter = HPRegistry.getHPRegistry().getServiceProviders(serviceClass);
        while (iter.hasNext()) {
            HPCategoryService spi = iter.next();
            if (!spi.getCategoryName().equals(category)) continue;
            return spi;
        }
        return null;
    }

    @Generated
    public static String[] getSupportedHPSelectorCategories() {
        return HangingProtocol.getSupportedHPCategories(HPSelectorCategoryService.class);
    }

    @Generated
    public static String[] getSupportedHPComparatorCategories() {
        return HangingProtocol.getSupportedHPCategories(HPComparatorCategoryService.class);
    }

    @Generated
    private static <T extends HPCategoryService> String[] getSupportedHPCategories(Class<T> serviceClass) {
        Iterator<HPCategoryService> iter = HPRegistry.getHPRegistry().getServiceProviders(serviceClass);
        HashSet<String> set = new HashSet<String>();
        while (iter.hasNext()) {
            HPCategoryService spi = iter.next();
            String ss = spi.getCategoryName();
            set.add(ss);
        }
        return set.toArray(new String[0]);
    }

    @Generated
    public String getSOPClassUID() {
        return this.dcmItems.getString(524310);
    }

    @Generated
    public void setSOPClassUID(String uid) {
        this.dcmItems.setString(524310, VR.UI, uid);
    }

    @Generated
    public String getSOPInstanceUID() {
        return this.dcmItems.getString(524312);
    }

    @Generated
    public void setSOPInstanceUID(String uid) {
        this.dcmItems.setString(524312, VR.UI, uid);
    }

    @Generated
    public String[] getSpecificCharacterSet() {
        return this.dcmItems.getStrings(524293);
    }

    @Generated
    public void setSpecificCharacterSet(String[] ss) {
        this.dcmItems.setString(524293, VR.CS, ss);
    }

    @Generated
    public Date getInstanceCreationDateTime() {
        return this.dcmItems.getDate(524306, 524307);
    }

    @Generated
    public void setInstanceCreationDateTime(Date d) {
        this.dcmItems.setDate(524306, VR.DA, d);
        this.dcmItems.setDate(524307, VR.TM, d);
    }

    @Generated
    public String getInstanceCreatorUID() {
        return this.dcmItems.getString(524308);
    }

    @Generated
    public void setInstanceCreatorUID(String s) {
        this.dcmItems.setString(524308, VR.UI, s);
    }

    @Generated
    public String getRelatedGeneralSOPClassUID() {
        return this.dcmItems.getString(524314);
    }

    @Generated
    public void setRelatedGeneralSOPClassUID(String s) {
        this.dcmItems.setString(524314, VR.UI, s);
    }

    @Generated
    public String getOriginalSpecializedSOPClassUID() {
        return this.dcmItems.getString(524315);
    }

    @Generated
    public void setOriginalSpecializedSOPClassUID(String s) {
        this.dcmItems.setString(524315, VR.UI, s);
    }

    @Generated
    public String getInstanceNumber() {
        return this.dcmItems.getString(2097171);
    }

    @Generated
    public void setInstanceNumber(String s) {
        this.dcmItems.setString(2097171, VR.IS, s);
    }
}

