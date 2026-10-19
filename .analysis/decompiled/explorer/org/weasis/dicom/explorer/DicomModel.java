/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemInfo
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.img.data.PrDicomObject
 *  org.osgi.service.component.annotations.Component
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.command.Option
 *  org.weasis.core.api.command.Options
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.explorer.model.Tree
 *  org.weasis.core.api.explorer.model.TreeModel
 *  org.weasis.core.api.explorer.model.TreeModelNode
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.Codec
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.SeriesEvent
 *  org.weasis.core.api.media.data.SeriesEvent$Action
 *  org.weasis.core.api.media.data.SeriesThumbnail
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagView
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.GzipManager
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$OtherIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.api.util.ThreadUtil
 *  org.weasis.core.ui.editor.SeriesViewerFactory
 *  org.weasis.core.ui.editor.ViewerOpenOptions
 *  org.weasis.core.ui.editor.ViewerPlacement
 *  org.weasis.core.ui.editor.ViewerPluginBuilder
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.AbstractKOSpecialElement
 *  org.weasis.dicom.codec.DcmMediaReader
 *  org.weasis.dicom.codec.DicomEncapDocElement
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.DicomMediaIO
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.DicomSpecialElement
 *  org.weasis.dicom.codec.DicomVideoElement
 *  org.weasis.dicom.codec.FilesExtractor
 *  org.weasis.dicom.codec.HiddenSeriesManager
 *  org.weasis.dicom.codec.HiddenSpecialElement
 *  org.weasis.dicom.codec.KOSpecialElement
 *  org.weasis.dicom.codec.PRSpecialElement
 *  org.weasis.dicom.codec.RejectedKOSpecialElement
 *  org.weasis.dicom.codec.SortSeriesStack
 *  org.weasis.dicom.codec.SpecialElementReferences
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 *  org.weasis.dicom.codec.display.Modality
 *  org.weasis.dicom.codec.seg.SegBuildScheduler
 *  org.weasis.dicom.codec.seg.SegSpecialElement
 *  org.weasis.dicom.codec.seg.SegVisibilityPolicy
 *  org.weasis.dicom.codec.utils.SplittingModalityRules
 *  org.weasis.dicom.codec.utils.SplittingModalityRules$Rule
 *  org.weasis.dicom.codec.utils.SplittingRules
 */
package org.weasis.dicom.explorer;

import com.formdev.flatlaf.util.SystemInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.data.PrDicomObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.command.Option;
import org.weasis.core.api.command.Options;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.explorer.model.Tree;
import org.weasis.core.api.explorer.model.TreeModel;
import org.weasis.core.api.explorer.model.TreeModelNode;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesEvent;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.GzipManager;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.api.util.ThreadUtil;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.core.ui.editor.ViewerPlacement;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.AbstractKOSpecialElement;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomEncapDocElement;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.DicomVideoElement;
import org.weasis.dicom.codec.FilesExtractor;
import org.weasis.dicom.codec.HiddenSeriesManager;
import org.weasis.dicom.codec.HiddenSpecialElement;
import org.weasis.dicom.codec.KOSpecialElement;
import org.weasis.dicom.codec.PRSpecialElement;
import org.weasis.dicom.codec.RejectedKOSpecialElement;
import org.weasis.dicom.codec.SortSeriesStack;
import org.weasis.dicom.codec.SpecialElementReferences;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.display.Modality;
import org.weasis.dicom.codec.seg.SegBuildScheduler;
import org.weasis.dicom.codec.seg.SegSpecialElement;
import org.weasis.dicom.codec.seg.SegVisibilityPolicy;
import org.weasis.dicom.codec.utils.SplittingModalityRules;
import org.weasis.dicom.codec.utils.SplittingRules;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadDicomDir;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.MimeSystemAppFactory;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.imp.DicomDirLoader;
import org.weasis.dicom.explorer.imp.DicomZipMediaIO;
import org.weasis.dicom.explorer.main.SeriesPane;
import org.weasis.dicom.explorer.rs.RsQueryParams;
import org.weasis.dicom.explorer.wado.DicomManager;
import org.weasis.dicom.explorer.wado.DownloadManager;
import org.weasis.dicom.explorer.wado.LoadRemoteDicomManifest;
import org.weasis.dicom.explorer.wado.LoadRemoteDicomURL;
import org.weasis.dicom.explorer.wado.LoadSeries;

@Component(property={"osgi.command.scope=dicom", "osgi.command.function=get", "osgi.command.function=rs", "osgi.command.function=close"}, service={DicomModel.class})
public class DicomModel
implements TreeModel,
DataExplorerModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomModel.class);
    public static final String NAME = "DICOM";
    public static final String PREFERENCE_NODE = "dicom.model";
    public static final TreeModelNode patient = new TreeModelNode(1, 0, TagW.PatientPseudoUID, new TagView(TagD.getTagFromIDs((int[])new int[]{0x100010, 0x100020})));
    public static final TreeModelNode study = new TreeModelNode(2, 0, TagD.get((int)0x20000D), new TagView(TagD.getTagFromIDs((int[])new int[]{524320, 524368, 0x200010, 528432})));
    public static final TreeModelNode series = new TreeModelNode(3, 0, TagW.SubseriesInstanceUID, new TagView(TagD.getTagFromIDs((int[])new int[]{528446, 0x200011, 524337})));
    public static final ExecutorService LOADING_EXECUTOR = ThreadUtil.newSingleThreadExecutor((String)"DicomModelLoader");
    private static final List<TreeModelNode> modelStructure = Arrays.asList(TreeModelNode.ROOT, patient, study, series);
    private final Tree<MediaSeriesGroup> model = new Tree((Object)MediaSeriesGroupNode.rootNode);
    private PropertyChangeSupport propertyChange = null;
    private final SplittingRules splittingRules = new SplittingRules();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<Codec<MediaElement>> getCodecPlugins() {
        List codecs;
        ArrayList<Codec<MediaElement>> codecPlugins = new ArrayList<Codec<MediaElement>>(1);
        List list = codecs = GuiUtils.getUICore().getCodecPlugins();
        synchronized (list) {
            for (Codec codec : codecs) {
                if (codec == null || "JDK ImageIO".equals(codec.getCodecName()) || !codec.isMimeTypeSupported("application/dicom") && !codec.isMimeTypeSupported("application/x-zip") || codecPlugins.contains(codec)) continue;
                codecPlugins.add((Codec<MediaElement>)codec);
            }
        }
        return codecPlugins;
    }

    public Collection<MediaSeriesGroup> getChildren(MediaSeriesGroup node) {
        return this.model.getSuccessors((Object)node);
    }

    public MediaSeriesGroup getHierarchyNode(MediaSeriesGroup parent, Object valueID) {
        if (parent != null || valueID != null) {
            for (MediaSeriesGroup node : this.getChildren(parent)) {
                if (!node.matchIdValue(valueID)) continue;
                return node;
            }
        }
        return null;
    }

    public List<MediaSeriesGroup> getAllSeriesNodes(MediaSeriesGroup parent, Object valueID) {
        if (parent != null || valueID != null) {
            ArrayList<MediaSeriesGroup> list = new ArrayList<MediaSeriesGroup>();
            for (MediaSeriesGroup node : this.getChildren(parent)) {
                String seriesInstanceUID = (String)TagD.getTagValue((TagReadable)node, (int)0x20000E, String.class);
                if (!Objects.equals(seriesInstanceUID, valueID)) continue;
                list.add(node);
            }
            return list;
        }
        return Collections.emptyList();
    }

    public void mergePatientUID(String oldPatientUID, String newPatientUID, PluginOpeningStrategy openingStrategy) {
        MediaSeriesGroup pt = this.getHierarchyNode(MediaSeriesGroupNode.rootNode, oldPatientUID);
        MediaSeriesGroup pt2 = this.getHierarchyNode(MediaSeriesGroupNode.rootNode, newPatientUID);
        if (pt == null || Objects.equals(pt, pt2)) {
            return;
        }
        SegVisibilityPolicy.renamePatient((String)oldPatientUID, (String)newPatientUID);
        if (pt2 == null) {
            pt2 = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.PATIENT), (Object)newPatientUID, patient.tagView());
            Iterator iter = pt.getTagEntrySetIterator();
            while (iter.hasNext()) {
                Map.Entry e = (Map.Entry)iter.next();
                pt2.setTag((TagW)e.getKey(), e.getValue());
            }
            this.addHierarchyNode(MediaSeriesGroupNode.rootNode, pt2);
        }
        Collection<MediaSeriesGroup> studies = this.getChildren(pt);
        HashMap<MediaSeriesGroup, Collection<MediaSeriesGroup>> studyMap = new HashMap<MediaSeriesGroup, Collection<MediaSeriesGroup>>();
        for (MediaSeriesGroup mediaSeriesGroup : studies) {
            studyMap.put(mediaSeriesGroup, this.getChildren(mediaSeriesGroup));
        }
        this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REMOVE, (Object)this, null, (Object)pt));
        openingStrategy.removePatient(pt);
        for (Map.Entry entry : studyMap.entrySet()) {
            MediaSeriesGroup st = (MediaSeriesGroup)entry.getKey();
            this.removeHierarchyNode(pt, st);
            this.addHierarchyNode(pt2, st);
            for (MediaSeriesGroup s : (Collection)entry.getValue()) {
                this.addHierarchyNode(st, s);
                this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.ADD, (Object)this, null, (Object)s));
                if (!(s instanceof DicomSeries)) continue;
                DicomSeries dicomSeries = (DicomSeries)s;
                openingStrategy.openViewerPlugin(pt2, this, (Series<?>)dicomSeries);
            }
        }
        this.removeHierarchyNode(MediaSeriesGroupNode.rootNode, pt);
    }

    public void mergeStudyUID(String oldStudyUID, String studyUID) {
        MediaSeriesGroup studyGroup = this.getStudyNode(oldStudyUID);
        MediaSeriesGroup studyGroup2 = this.getStudyNode(studyUID);
        if (studyGroup == null || Objects.equals(studyGroup, studyGroup2)) {
            return;
        }
        MediaSeriesGroup patientGroup = this.getParent(studyGroup, patient);
        if (studyGroup2 == null) {
            studyGroup2 = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.STUDY), (Object)studyUID, study.tagView());
            Iterator iter = studyGroup.getTagEntrySetIterator();
            while (iter.hasNext()) {
                Map.Entry e = (Map.Entry)iter.next();
                studyGroup2.setTag((TagW)e.getKey(), e.getValue());
            }
            this.addHierarchyNode(patientGroup, studyGroup2);
        }
        Collection<MediaSeriesGroup> seriesGroups = this.getChildren(studyGroup);
        for (MediaSeriesGroup s : seriesGroups) {
            this.removeHierarchyNode(studyGroup, s);
            this.addHierarchyNode(studyGroup2, s);
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.ADD, (Object)this, null, (Object)s));
        }
        this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REMOVE, (Object)this, null, (Object)studyGroup));
        this.removeHierarchyNode(patientGroup, studyGroup);
    }

    public MediaSeriesGroup getStudyNode(String studyUID) {
        Objects.requireNonNull(studyUID);
        for (MediaSeriesGroup pt : this.getChildren(MediaSeriesGroupNode.rootNode)) {
            for (MediaSeriesGroup st : this.getChildren(pt)) {
                if (!st.matchIdValue((Object)studyUID)) continue;
                return st;
            }
        }
        return null;
    }

    public MediaSeriesGroup getSeriesNode(String seriesUID) {
        Objects.requireNonNull(seriesUID);
        for (MediaSeriesGroup pt : this.getChildren(MediaSeriesGroupNode.rootNode)) {
            for (MediaSeriesGroup st : this.getChildren(pt)) {
                for (MediaSeriesGroup item : this.getChildren(st)) {
                    if (!item.matchIdValue((Object)seriesUID)) continue;
                    return item;
                }
            }
        }
        return null;
    }

    public void addHierarchyNode(MediaSeriesGroup root, MediaSeriesGroup leaf) {
        this.model.addLeaf((Object)root, (Object)leaf);
    }

    public void removeHierarchyNode(MediaSeriesGroup root, MediaSeriesGroup leaf) {
        Tree tree = this.model.getTree((Object)root);
        if (tree != null) {
            tree.removeLeaf((Object)leaf);
        }
    }

    public MediaSeriesGroup getParent(MediaSeriesGroup node, TreeModelNode modelNode) {
        if (node != null && modelNode != null) {
            TagW matchTagID = modelNode.tagElement();
            if (node.getTagID().equals((Object)matchTagID)) {
                return node;
            }
            Tree tree = this.model.getTree((Object)node);
            if (tree != null) {
                Tree parent;
                while ((parent = tree.getParent()) != null) {
                    if (((MediaSeriesGroup)parent.getHead()).getTagID().equals((Object)matchTagID)) {
                        return (MediaSeriesGroup)parent.getHead();
                    }
                    tree = parent;
                }
            }
        }
        return null;
    }

    public void dispose() {
        this.removeAllPropertyChangeListener();
        for (MediaSeriesGroup pt : this.getChildren(MediaSeriesGroupNode.rootNode)) {
            for (MediaSeriesGroup st : this.getChildren(pt)) {
                for (MediaSeriesGroup item : this.getChildren(st)) {
                    item.dispose();
                }
            }
        }
        this.model.clear();
    }

    public String toString() {
        return NAME;
    }

    public List<TreeModelNode> getModelStructure() {
        return modelStructure;
    }

    public void addPropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (this.propertyChange == null) {
            this.propertyChange = new PropertyChangeSupport(this);
        }
        this.propertyChange.addPropertyChangeListener(propertychangelistener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (this.propertyChange != null) {
            this.propertyChange.removePropertyChangeListener(propertychangelistener);
        }
    }

    public void removeAllPropertyChangeListener() {
        if (this.propertyChange != null) {
            for (PropertyChangeListener listener : this.propertyChange.getPropertyChangeListeners()) {
                this.propertyChange.removePropertyChangeListener(listener);
            }
        }
    }

    public void firePropertyChange(ObservableEvent event) {
        if (this.propertyChange != null) {
            if (event == null) {
                throw new NullPointerException();
            }
            if (SwingUtilities.isEventDispatchThread()) {
                this.propertyChange.firePropertyChange((PropertyChangeEvent)event);
            } else {
                SwingUtilities.invokeLater(() -> this.propertyChange.firePropertyChange((PropertyChangeEvent)event));
            }
        }
    }

    public void mergeSeries(List<MediaSeries<? extends MediaElement>> seriesList) {
        if (seriesList != null && seriesList.size() > 1) {
            String uid = (String)TagD.getTagValue((TagReadable)((TagReadable)seriesList.getFirst()), (int)0x20000E, String.class);
            boolean sameOrigin = true;
            if (uid != null) {
                for (int i = 1; i < seriesList.size(); ++i) {
                    if (uid.equals(TagD.getTagValue((TagReadable)((TagReadable)seriesList.get(i)), (int)0x20000E))) continue;
                    sameOrigin = false;
                    break;
                }
            }
            if (sameOrigin) {
                int min = Integer.MAX_VALUE;
                MediaSeries<? extends MediaElement> base = (MediaSeries<? extends MediaElement>)seriesList.getFirst();
                for (MediaSeries<? extends MediaElement> s : seriesList) {
                    Integer splitNb = (Integer)s.getTagValue(TagW.SplitSeriesNumber);
                    if (splitNb == null || min <= splitNb) continue;
                    min = splitNb;
                    base = s;
                }
                for (MediaSeries<? extends MediaElement> s : seriesList) {
                    if (s == base) continue;
                    base.addAll((Collection)s.getMedias(null, null));
                    this.removeSeriesWithoutDisposingMedias((MediaSeriesGroup)s);
                }
                List sortedMedias = base.getSortedMedias(null);
                sortedMedias.sort(SortSeriesStack.instanceNumber);
                if (base instanceof DicomSeries) {
                    DicomSeries dicomSeries = (DicomSeries)base;
                    List imageList = dicomSeries.copyOfMedias(null, (Comparator)SortSeriesStack.slicePosition);
                    int samplingRate = LoadLocalDicom.calculateSamplingRateFor4d(imageList);
                    base.setTag(TagW.stepNDimensions, (Object)samplingRate);
                }
                this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REPLACE, (Object)this, base, base));
            }
        }
    }

    public void removeHiddenSpecialElement(HiddenSpecialElement element) {
        if (element == null) {
            return;
        }
        String patientPseudoUID = (String)element.getTagValue(TagW.PatientPseudoUID);
        MediaSeriesGroup patientGroup = this.getHierarchyNode(MediaSeriesGroupNode.rootNode, patientPseudoUID);
        if (patientGroup == null) {
            return;
        }
        String studyUID = (String)TagD.getTagValue((TagReadable)element, (int)0x20000D, String.class);
        MediaSeriesGroup studyGroup = this.getHierarchyNode(patientGroup, studyUID);
        if (studyGroup == null) {
            return;
        }
        String seriesUID = (String)TagD.getTagValue((TagReadable)element, (int)0x20000E, String.class);
        Series dicomSeries = (Series)this.getHierarchyNode(studyGroup, seriesUID);
        if (dicomSeries == null) {
            return;
        }
        if (DicomModel.isHiddenModality(dicomSeries)) {
            Set specialElementList = (Set)HiddenSeriesManager.getInstance().series2Elements.get(seriesUID);
            if (specialElementList == null) {
                return;
            }
            if (specialElementList.remove(element)) {
                this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)this, null, (Object)element));
                element.dispose();
            }
            if (specialElementList.isEmpty()) {
                this.removeSeries((MediaSeriesGroup)dicomSeries);
            }
        }
    }

    public void removeSeriesWithoutDisposingMedias(MediaSeriesGroup dicomSeries) {
        if (dicomSeries != null) {
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REMOVE, (Object)this, null, (Object)dicomSeries));
            MediaSeriesGroup studyGroup = this.getParent(dicomSeries, study);
            this.removeHierarchyNode(studyGroup, dicomSeries);
            LOGGER.info("Remove Series (no dispose): {}", (Object)dicomSeries);
        }
    }

    public void removeSeries(MediaSeriesGroup seriesGroup) {
        if (seriesGroup != null) {
            if (!DownloadManager.getTasks().isEmpty() && seriesGroup instanceof DicomSeries) {
                DicomSeries dicomSeries = (DicomSeries)seriesGroup;
                DownloadManager.stopDownloading(dicomSeries, this);
            }
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REMOVE, (Object)this, null, (Object)seriesGroup));
            this.forgetAnnouncedSegmentations(seriesGroup);
            MediaSeriesGroup studyGroup = this.getParent(seriesGroup, study);
            this.removeHierarchyNode(studyGroup, seriesGroup);
            seriesGroup.dispose();
            LOGGER.info("Remove Series: {}", (Object)seriesGroup);
        }
    }

    public void removeStudy(MediaSeriesGroup studyGroup) {
        if (studyGroup != null) {
            if (!DownloadManager.getTasks().isEmpty()) {
                for (MediaSeriesGroup group : this.getChildren(studyGroup)) {
                    if (!(group instanceof DicomSeries)) continue;
                    DicomSeries dicomSeries = (DicomSeries)group;
                    DownloadManager.stopDownloading(dicomSeries, this);
                }
            }
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REMOVE, (Object)this, null, (Object)studyGroup));
            for (MediaSeriesGroup group : this.getChildren(studyGroup)) {
                group.dispose();
            }
            MediaSeriesGroup patientGroup = this.getParent(studyGroup, patient);
            this.removeHierarchyNode(patientGroup, studyGroup);
            LOGGER.info("Remove Study: {}", (Object)studyGroup);
        }
    }

    public void removePatient(MediaSeriesGroup patientGroup) {
        if (patientGroup != null) {
            if (!DownloadManager.getTasks().isEmpty()) {
                for (MediaSeriesGroup studyGroup : this.getChildren(patientGroup)) {
                    for (MediaSeriesGroup group : this.getChildren(studyGroup)) {
                        if (!(group instanceof DicomSeries)) continue;
                        DicomSeries dicomSeries = (DicomSeries)group;
                        DownloadManager.stopDownloading(dicomSeries, this);
                    }
                }
            }
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REMOVE, (Object)this, null, (Object)patientGroup));
            for (MediaSeriesGroup studyGroup : this.getChildren(patientGroup)) {
                for (MediaSeriesGroup group : this.getChildren(studyGroup)) {
                    group.dispose();
                }
            }
            SegVisibilityPolicy.forgetPatient((String)((String)patientGroup.getTagValue(TagW.PatientPseudoUID)));
            this.removeHierarchyNode(MediaSeriesGroupNode.rootNode, patientGroup);
            LOGGER.info("Remove Patient: {}", (Object)patientGroup);
        }
    }

    private void forgetAnnouncedSegmentations(MediaSeriesGroup seriesGroup) {
        MediaSeriesGroup patient = this.getParent(seriesGroup, DicomModel.patient);
        if (patient != null) {
            SegVisibilityPolicy.forgetSeries((String)((String)patient.getTagValue(TagW.PatientPseudoUID)), (String)((String)TagD.getTagValue((TagReadable)seriesGroup, (int)0x20000E, String.class)));
        }
    }

    public static boolean isHiddenModality(MediaSeries<?> series) {
        String modality = series == null ? null : (String)TagD.getTagValue(series, (int)524384, String.class);
        return DicomMediaIO.isHiddenModality(modality);
    }

    public static Collection<KOSpecialElement> getEditableKoSpecialElements(MediaSeriesGroup patient) {
        List list = HiddenSeriesManager.getHiddenElementsFromPatient(KOSpecialElement.class, (MediaSeriesGroup)patient);
        if (!list.isEmpty()) {
            for (int i = list.size() - 1; i >= 0; --i) {
                KOSpecialElement koElement = (KOSpecialElement)list.get(i);
                if (koElement.getMediaReader().isEditableDicom()) continue;
                list.remove(i);
            }
            return list;
        }
        return Collections.emptyList();
    }

    public static Collection<KOSpecialElement> getKoSpecialElements(MediaSeries<DicomImageElement> dicomSeries) {
        String patientPseudoUID = DicomModel.getPatientPseudoUID(dicomSeries);
        List specialElementList = HiddenSeriesManager.getHiddenElementsFromPatient(KOSpecialElement.class, (String)patientPseudoUID);
        String referencedSeriesInstanceUID = (String)TagD.getTagValue(dicomSeries, (int)0x20000E, String.class);
        return AbstractKOSpecialElement.getKoSpecialElements((Collection)specialElementList, (String)referencedSeriesInstanceUID);
    }

    public static Collection<RejectedKOSpecialElement> getRejectionKoSpecialElements(MediaSeries<DicomImageElement> dicomSeries) {
        String patientPseudoUID = DicomModel.getPatientPseudoUID(dicomSeries);
        List specialElementList = HiddenSeriesManager.getHiddenElementsFromPatient(RejectedKOSpecialElement.class, (String)patientPseudoUID);
        String referencedSeriesInstanceUID = (String)TagD.getTagValue(dicomSeries, (int)0x20000E, String.class);
        return AbstractKOSpecialElement.getRejectionKoSpecialElements((Collection)specialElementList, (String)referencedSeriesInstanceUID);
    }

    public static String getPatientPseudoUID(MediaSeries<DicomImageElement> dicomSeries) {
        if (dicomSeries == null) {
            return null;
        }
        DicomImageElement img = (DicomImageElement)dicomSeries.getMedia(MediaSeries.MEDIA_POSITION.FIRST, null, null);
        if (img == null) {
            return null;
        }
        return (String)img.getTagValue(TagW.PatientPseudoUID);
    }

    public static RejectedKOSpecialElement getRejectionKoSpecialElement(MediaSeries<DicomImageElement> dicomSeries, String sopUID, Integer dicomFrameNumber) {
        String patientPseudoUID = DicomModel.getPatientPseudoUID(dicomSeries);
        List specialElementList = HiddenSeriesManager.getHiddenElementsFromPatient(RejectedKOSpecialElement.class, (String)patientPseudoUID);
        String referencedSeriesInstanceUID = (String)TagD.getTagValue(dicomSeries, (int)0x20000E, String.class);
        return AbstractKOSpecialElement.getRejectionKoSpecialElement((Collection)specialElementList, (String)referencedSeriesInstanceUID, (String)sopUID, (Integer)dicomFrameNumber);
    }

    public static List<PRSpecialElement> getPrSpecialElements(MediaSeries<DicomImageElement> dicomSeries, DicomImageElement img) {
        String patientPseudoUID = DicomModel.getPatientPseudoUID(dicomSeries);
        List specialElementList = HiddenSeriesManager.getHiddenElementsFromPatient(PRSpecialElement.class, (String)patientPseudoUID);
        if (!specialElementList.isEmpty()) {
            String referencedSeriesInstanceUID = (String)TagD.getTagValue(dicomSeries, (int)0x20000E, String.class);
            String seriesUID = (String)TagD.getTagValue((TagReadable)img, (int)0x20000E, String.class);
            if (Objects.equals(seriesUID, referencedSeriesInstanceUID)) {
                return PRSpecialElement.getPRSpecialElements((Collection)specialElementList, (DicomImageElement)img);
            }
        }
        return Collections.emptyList();
    }

    public static <E> List<E> getSpecialElements(MediaSeriesGroup series, Class<E> clazz) {
        List elements;
        if (series != null && clazz != null && clazz.isAssignableFrom(clazz) && (elements = (List)series.getTagValue(TagW.DicomSpecialElementList)) != null) {
            ArrayList<DicomSpecialElement> list = new ArrayList<DicomSpecialElement>();
            for (DicomSpecialElement el : elements) {
                if (!clazz.isInstance(el)) continue;
                list.add(el);
            }
            return list;
        }
        return Collections.emptyList();
    }

    public static <E> E getFirstSpecialElement(MediaSeriesGroup group, Class<E> clazz) {
        List sps;
        if (group != null && clazz != null && clazz.isAssignableFrom(clazz) && (sps = (List)group.getTagValue(TagW.DicomSpecialElementList)) != null) {
            for (DicomSpecialElement el : sps) {
                if (!clazz.isInstance(el)) continue;
                return (E)el;
            }
        }
        return null;
    }

    public void openRelatedSeries(KOSpecialElement koSpecialElement, MediaSeriesGroup patient) {
        SeriesViewerFactory plugin;
        if (koSpecialElement != null && patient != null && (plugin = GuiUtils.getUICore().getViewerFactory("series/dicom")) != null && !(plugin instanceof MimeSystemAppFactory)) {
            Set koSet = koSpecialElement.getReferencedSeriesInstanceUIDSet();
            ArrayList<MediaSeries> seriesList = new ArrayList<MediaSeries>();
            for (MediaSeriesGroup st : this.getChildren(patient)) {
                for (MediaSeriesGroup s : this.getChildren(st)) {
                    if (!koSet.contains(TagD.getTagValue((TagReadable)s, (int)0x20000E))) continue;
                    seriesList.add((MediaSeries)s);
                }
            }
            if (!seriesList.isEmpty()) {
                ViewerOpenOptions opts = this.createViewerKeyImageOpenOptions();
                ViewerPluginBuilder builder = new ViewerPluginBuilder(plugin, seriesList, (DataExplorerModel)this, opts);
                builder.open();
                this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.SELECT, (Object)opts.uid(), null, (Object)koSpecialElement));
            }
        }
    }

    public ViewerOpenOptions createViewerKeyImageOpenOptions() {
        String uid = UUID.randomUUID().toString();
        return ViewerOpenOptions.builder().placement((ViewerPlacement)ViewerPlacement.newTab()).icon((Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.OtherIcon.KEY_IMAGE)).uid(uid).build();
    }

    private void splitSeries(DicomMediaIO mediaIo, DicomSeries original, DicomImageElement media) {
        this.splitSeries(mediaIo, original).addMedia(media);
    }

    private void replaceSeries(DicomMediaIO mediaIO, DicomSeries original, DicomImageElement media) {
        this.replaceSeries(mediaIO, original).addMedia(media);
    }

    DicomSeries splitSeries(DicomMediaIO dicomReader, DicomSeries original) {
        return this.createSplitOrReplaceSeries(dicomReader, original, true);
    }

    DicomSeries replaceSeries(DicomMediaIO io, DicomSeries orig) {
        return this.createSplitOrReplaceSeries(io, orig, false);
    }

    private DicomSeries createSplitOrReplaceSeries(DicomMediaIO dicomReader, DicomSeries original, boolean isSplit) {
        MediaSeriesGroup study = this.getParent((MediaSeriesGroup)original, DicomModel.study);
        String uid = (String)TagD.getTagValue((TagReadable)original, (int)0x20000E, String.class);
        int idx = this.nextIndex(study, uid);
        String newUid = "#" + idx + "." + uid;
        DicomSeries s = dicomReader.buildSeries(newUid);
        dicomReader.writeMetaData((MediaSeriesGroup)s);
        if (original.getTagValue(TagW.SplitSeriesNumber) == null) {
            original.setTag(TagW.SplitSeriesNumber, (Object)(isSplit ? 1 : -1));
        }
        s.setTag(TagW.SplitSeriesNumber, (Object)(isSplit ? idx + 1 : idx));
        s.setTag(TagW.ExplorerModel, (Object)this);
        s.setTag(TagW.WadoParameters, original.getTagValue(TagW.WadoParameters));
        this.addHierarchyNode(study, (MediaSeriesGroup)s);
        LOGGER.info("{} of the series: {}", (Object)(isSplit ? "Splitting" : "Replacement"), (Object)s);
        return s;
    }

    private int nextIndex(MediaSeriesGroup study, String uid) {
        int i = 1;
        while (this.getHierarchyNode(study, "#" + i + "." + uid) != null) {
            ++i;
        }
        return i;
    }

    private void rebuildSeries(DicomMediaIO dicomReader, DicomImageElement media) {
        String seriesUID;
        DicomSeries dicomSeries;
        MediaSeriesGroup st;
        String studyUID = (String)TagD.getTagValue((TagReadable)dicomReader, (int)0x20000D, String.class);
        String patientPseudoUID = (String)dicomReader.getTagValue(TagW.PatientPseudoUID);
        MediaSeriesGroup pt = this.getHierarchyNode(MediaSeriesGroupNode.rootNode, patientPseudoUID);
        if (pt == null) {
            st = this.getStudyNode(studyUID);
            if (st == null) {
                pt = new MediaSeriesGroupNode(TagW.PatientPseudoUID, (Object)patientPseudoUID, patient.tagView());
                dicomReader.writeMetaData(pt);
                this.addHierarchyNode(MediaSeriesGroupNode.rootNode, pt);
                LOGGER.info("Adding patient: {}", (Object)pt);
            } else {
                pt = this.getParent(st, patient);
                LOGGER.warn("DICOM patient attributes are inconsistent! Name or ID is different within an exam.");
            }
        }
        if ((st = this.getHierarchyNode(pt, studyUID)) == null) {
            st = new MediaSeriesGroupNode(TagD.get((int)0x20000D), (Object)studyUID, study.tagView());
            dicomReader.writeMetaData(st);
            this.addHierarchyNode(pt, st);
        }
        if ((dicomSeries = (DicomSeries)this.getHierarchyNode(st, seriesUID = (String)TagD.getTagValue((TagReadable)dicomReader, (int)0x20000E, String.class))) == null) {
            dicomSeries = dicomReader.buildSeries(seriesUID);
            dicomReader.writeMetaData((MediaSeriesGroup)dicomSeries);
            dicomSeries.setTag(TagW.ExplorerModel, (Object)this);
            this.addHierarchyNode(st, (MediaSeriesGroup)dicomSeries);
            LOGGER.info("Series rebuilding: {}", (Object)dicomSeries);
        }
        dicomSeries.addMedia(media);
        this.buildThumbnail(dicomSeries);
    }

    public void allSeriesPostProcessing() {
        for (MediaSeriesGroup pt : this.getChildren(MediaSeriesGroupNode.rootNode)) {
            for (MediaSeriesGroup st : this.getChildren(pt)) {
                for (MediaSeriesGroup item : this.getChildren(st)) {
                    DicomSeries dicomSeries;
                    int imageCount;
                    Integer step = (Integer)item.getTagValue(TagW.stepNDimensions);
                    if (step != null && step >= 1 || !(item instanceof DicomSeries) || (imageCount = (dicomSeries = (DicomSeries)item).size(null)) == 0 || DicomModel.isHiddenModality(dicomSeries)) continue;
                    boolean split = LoadLocalDicom.seriesPostProcessing(dicomSeries, this);
                    if (!split) {
                        this.buildThumbnail(dicomSeries);
                    }
                    if (!dicomSeries.isSuitableFor3d()) continue;
                    this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)dicomSeries, null, (Object)new SeriesEvent(SeriesEvent.Action.UPDATE, (Object)dicomSeries, null)));
                }
            }
        }
    }

    public void buildThumbnail(DicomSeries dicomSeries) {
        SeriesThumbnail t = (SeriesThumbnail)dicomSeries.getTagValue(TagW.Thumbnail);
        if (t == null) {
            int thumbnailSize = SeriesThumbnail.getThumbnailSizeFromPreferences();
            t = SeriesPane.createThumbnail(dicomSeries, this, thumbnailSize);
            dicomSeries.setTag(TagW.Thumbnail, (Object)t);
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.ADD, (Object)this, null, (Object)dicomSeries));
        }
    }

    public void applySplittingRules(DicomSeries original, DicomSpecialElement media) {
        DicomMediaIO dicomMediaIO;
        if (media != null && (dicomMediaIO = media.getMediaReader()) instanceof DicomMediaIO) {
            DicomMediaIO dicomReader = dicomMediaIO;
            String seriesUID = (String)TagD.getTagValue((TagReadable)original, (int)0x20000E, String.class);
            this.splitSpecialElement(media, original, seriesUID, dicomReader);
        }
    }

    public void applySplittingRules(DicomSeries original, DicomImageElement media) {
        DcmMediaReader dcmMediaReader;
        if (media != null && (dcmMediaReader = media.getMediaReader()) instanceof DicomMediaIO) {
            DicomMediaIO dicomReader = (DicomMediaIO)dcmMediaReader;
            String seriesUID = (String)TagD.getTagValue((TagReadable)original, (int)0x20000E, String.class);
            if (seriesUID == null) {
                throw new IllegalArgumentException("Series UID cannot be null");
            }
            if (!seriesUID.equals(TagD.getTagValue((TagReadable)dicomReader, (int)0x20000E))) {
                this.rebuildSeries(dicomReader, media);
                return;
            }
            if (original instanceof FilesExtractor) {
                this.handleFilesExtractorSeries(original, media, dicomReader);
            } else {
                this.handleRegularSeries(original, media, dicomReader, seriesUID);
            }
        }
    }

    private void handleFilesExtractorSeries(DicomSeries original, DicomImageElement media, DicomMediaIO dicomReader) {
        if (original.size(null) > 0) {
            if (media instanceof DicomVideoElement || media instanceof DicomEncapDocElement) {
                this.splitSeries(dicomReader, original, media);
            } else {
                this.findMatchingSeriesOrSplit(original, media);
            }
        } else {
            original.addMedia(media);
        }
    }

    private void handleRegularSeries(DicomSeries original, DicomImageElement media, DicomMediaIO dicomReader, String seriesUID) {
        if (media instanceof DicomVideoElement || media instanceof DicomEncapDocElement) {
            if (original.size(null) > 0) {
                this.splitSeries(dicomReader, original, media);
            } else {
                this.replaceSeries(dicomReader, original, media);
            }
            return;
        }
        int frames = dicomReader.getMediaElementNumber();
        if (frames < 1) {
            original.addMedia(media);
        } else {
            List<SplittingModalityRules.Rule> rules = this.buildRules(original, frames);
            if (DicomModel.isSimilar(rules, original, (MediaElement)media) || "seg/dicom".equals(original.getMimeType())) {
                original.addMedia(media);
                return;
            }
            this.findSimilarOrSplit(original, media, dicomReader, seriesUID, rules);
        }
    }

    private void findSimilarOrSplit(DicomSeries original, DicomImageElement media, DicomMediaIO dicomReader, String seriesUID, List<SplittingModalityRules.Rule> rules) {
        String uid;
        MediaSeriesGroup group;
        MediaSeriesGroup study = this.getParent((MediaSeriesGroup)original, DicomModel.study);
        int k = 1;
        while ((group = this.getHierarchyNode(study, uid = "#" + k + "." + seriesUID)) instanceof DicomSeries) {
            DicomSeries dicomSeries = (DicomSeries)group;
            if (DicomModel.isSimilar(rules, dicomSeries, (MediaElement)media)) {
                dicomSeries.addMedia(media);
                return;
            }
            ++k;
        }
        this.splitSeries(dicomReader, original, media);
    }

    private List<SplittingModalityRules.Rule> buildRules(DicomSeries initialSeries, int frames) {
        Modality modality = Modality.getModality((String)((String)TagD.getTagValue((TagReadable)initialSeries, (int)524384, String.class)));
        SplittingModalityRules splitRules = this.splittingRules.getSplittingModalityRules(modality, Modality.DEFAULT);
        List rules = splitRules == null ? Collections.emptyList() : (frames > 1 ? splitRules.getMultiFrameRules() : splitRules.getSingleFrameRules());
        return rules;
    }

    private void splitSpecialElement(DicomSpecialElement specialElement, DicomSeries originalSeries, String seriesUID, DicomMediaIO dicomReader) {
        String rMime = dicomReader.getMimeType();
        if (specialElement instanceof HiddenSpecialElement) {
            HiddenSpecialElement hiddenElement = (HiddenSpecialElement)specialElement;
            this.handleHiddenSpecialElement(hiddenElement, seriesUID, originalSeries);
        } else {
            this.handleRegularSpecialElement(specialElement, originalSeries, rMime, dicomReader);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleHiddenSpecialElement(HiddenSpecialElement hiddenElement, String seriesUID, DicomSeries originalSeries) {
        if (hiddenElement instanceof SpecialElementReferences) {
            SpecialElementReferences references = (SpecialElementReferences)hiddenElement;
            references.initReferences(seriesUID);
        }
        if (hiddenElement instanceof SegSpecialElement) {
            SegSpecialElement seg = (SegSpecialElement)hiddenElement;
            List refSeriesList = seg.getRefMap().keySet().stream().map(this::getSeriesNode).filter(series -> series instanceof DicomSeries).map(series -> (DicomSeries)series).toList();
            SegBuildScheduler.submitContourInit(() -> {
                try {
                    seg.initContours(originalSeries, refSeriesList);
                }
                catch (RuntimeException e) {
                    LOGGER.error("SEG loading failed for series {}", (Object)seriesUID, (Object)e);
                    return;
                }
                if (!seg.isReady()) {
                    return;
                }
                if (refSeriesList.isEmpty() && !seg.getPositionMap().isEmpty()) {
                    this.populateReferencesByFrameOfRef(seriesUID, originalSeries);
                }
                this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)this, null, (Object)seg));
            });
        } else if (hiddenElement instanceof PRSpecialElement) {
            PRSpecialElement pr = (PRSpecialElement)hiddenElement;
            PrDicomObject prDicomObject = pr.getPrDicomObject();
            if (StringUtil.hasText((CharSequence)seriesUID) && prDicomObject != null) {
                Attributes prAttributes = prDicomObject.getDicomObject();
                HiddenSeriesManager.getInstance().extractReferencedSeries(prAttributes, seriesUID);
            }
        } else if (hiddenElement instanceof KOSpecialElement) {
            Set referencedSeriesUIDs;
            KOSpecialElement ko = (KOSpecialElement)hiddenElement;
            if (StringUtil.hasText((CharSequence)seriesUID) && (referencedSeriesUIDs = ko.getReferencedSeriesInstanceUIDSet()) != null) {
                for (String referenceKey : referencedSeriesUIDs) {
                    if (!StringUtil.hasText((CharSequence)referenceKey)) continue;
                    HiddenSeriesManager.getInstance().reference2Series.computeIfAbsent(referenceKey, string -> new CopyOnWriteArraySet()).add(seriesUID);
                }
            }
        }
        DicomModel dicomModel = this;
        synchronized (dicomModel) {
            Map mapSeries = HiddenSeriesManager.getInstance().series2Elements;
            mapSeries.computeIfAbsent(seriesUID, string -> new CopyOnWriteArraySet()).add(hiddenElement);
            SegVisibilityPolicy.invalidate();
            String patientPseudoUID = (String)hiddenElement.getTagValue(TagW.PatientPseudoUID);
            if (patientPseudoUID != null) {
                Set patients = HiddenSeriesManager.getInstance().patient2Series.computeIfAbsent(patientPseudoUID, string -> new CopyOnWriteArraySet());
                patients.add(seriesUID);
            }
        }
    }

    private void populateReferencesByFrameOfRef(String segSeriesUID, DicomSeries originalSeries) {
        String segFrameOfRefUID = (String)TagD.getTagValue((TagReadable)originalSeries, (int)0x200052, String.class);
        if (segFrameOfRefUID == null) {
            return;
        }
        MediaSeriesGroup studyGroup = this.getParent((MediaSeriesGroup)originalSeries, study);
        if (studyGroup == null) {
            return;
        }
        MediaSeriesGroup patientGroup = this.getParent(studyGroup, patient);
        if (patientGroup == null) {
            return;
        }
        for (MediaSeriesGroup st : this.getChildren(patientGroup)) {
            for (MediaSeriesGroup item : this.getChildren(st)) {
                DicomSeries ds;
                String itemSeriesUID = (String)TagD.getTagValue((TagReadable)item, (int)0x20000E, String.class);
                String itemFrameOfRef = (String)TagD.getTagValue((TagReadable)item, (int)0x200052, String.class);
                if (!segFrameOfRefUID.equals(itemFrameOfRef) || segSeriesUID.equals(itemSeriesUID) || !(item instanceof DicomSeries) || !"series/dicom".equals((ds = (DicomSeries)item).getMimeType())) continue;
                HiddenSeriesManager.getInstance().reference2Series.computeIfAbsent(itemSeriesUID, string -> new CopyOnWriteArraySet()).add(segSeriesUID);
            }
        }
    }

    private void handleRegularSpecialElement(DicomSpecialElement specialElement, DicomSeries originalSeries, String rMime, DicomMediaIO dicomReader) {
        CopyOnWriteArrayList<DicomSpecialElement> specialElementList = (CopyOnWriteArrayList<DicomSpecialElement>)originalSeries.getTagValue(TagW.DicomSpecialElementList);
        if (specialElementList == null) {
            specialElementList = new CopyOnWriteArrayList<DicomSpecialElement>();
            originalSeries.setTag(TagW.DicomSpecialElementList, specialElementList);
        } else if ("sr/dicom".equals(rMime) || "wf/dicom".equals(rMime)) {
            DicomSeries s = this.splitSeries(dicomReader, originalSeries);
            specialElementList = new CopyOnWriteArrayList();
            s.setTag(TagW.DicomSpecialElementList, specialElementList);
        }
        specialElementList.add(specialElement);
    }

    private void findMatchingSeriesOrSplit(DicomSeries original, DicomImageElement media) {
        DicomMediaIO dicomReader = (DicomMediaIO)media.getMediaReader();
        int frames = dicomReader.getMediaElementNumber();
        if (frames < 1) {
            original.addMedia(media);
        } else {
            String seriesUID = (String)TagD.getTagValue((TagReadable)original, (int)0x20000E, String.class);
            List<SplittingModalityRules.Rule> rules = this.buildRules(original, frames);
            if (DicomModel.isSimilar(rules, original, (MediaElement)media)) {
                original.addMedia(media);
                return;
            }
            this.findSimilarOrSplit(original, media, dicomReader, seriesUID, rules);
        }
    }

    private static boolean hasSameConcatenationUID(MediaElement firstMedia, MediaElement media) {
        String firstConcatenationUID;
        if (firstMedia instanceof DicomImageElement && media instanceof DicomImageElement && (firstConcatenationUID = (String)TagD.getTagValue((TagReadable)firstMedia, (int)2134369, String.class)) != null) {
            String concatenationUID = (String)TagD.getTagValue((TagReadable)media, (int)2134369, String.class);
            return Objects.equals(firstConcatenationUID, concatenationUID);
        }
        return false;
    }

    private static boolean isSimilar(List<SplittingModalityRules.Rule> list, Series<?> s, MediaElement media) {
        MediaElement firstMedia = s.getMedia(0, null, null);
        if (firstMedia == null || DicomModel.hasSameConcatenationUID(firstMedia, media)) {
            return true;
        }
        if (firstMedia.getClass() != media.getClass()) {
            return false;
        }
        for (SplittingModalityRules.Rule rule : list) {
            if (rule.isTagValueMatching(firstMedia, media)) continue;
            return false;
        }
        return true;
    }

    public void get(String[] argv) throws IOException {
        String[] usage = new String[]{"Load DICOM files remotely or locally", "Usage: dicom:get ([-l PATH]... [-w URI]... [-r URI]... [-p] [-i DATA]... [-z URI]...)", "PATH is either a directory(recursive) or a file", "  -l --local=PATH   open DICOMs from local disk", "  -r --remote=URI   open DICOMs from an URI", "  -w --wado=URI     open DICOMs from an XML manifest", "  -z --zip=URI      open DICOM ZIP from an URI", "  -p --portable     open DICOMs from configured directories at the same level of the executable", "  -i --iwado=DATA   open DICOMs from an XML manifest (GZIP-Base64)", "  -? --help         show help"};
        Option opt = Options.compile((String[])usage).parse((Object[])argv);
        List largs = opt.getList("local");
        List rargs = opt.getList("remote");
        List zargs = opt.getList("zip");
        List iargs = opt.getList("iwado");
        List wargs = opt.getList("wado");
        if (opt.isSet("help") || largs.isEmpty() && rargs.isEmpty() && iargs.isEmpty() && wargs.isEmpty() && zargs.isEmpty() && !opt.isSet("portable")) {
            opt.usage();
            return;
        }
        GuiExecutor.execute(() -> {
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.SELECT, (Object)this, null, (Object)this));
            this.getCommand(opt, largs, rargs, iargs, wargs, zargs);
        });
    }

    /*
     * WARNING - void declaration
     */
    private void getCommand(Option opt, List<String> largs, List<String> rargs, List<String> iargs, List<String> wargs, List<String> zargs) {
        if (opt.isSet("local")) {
            void var8_9;
            File[] files = new File[largs.size()];
            boolean bl = false;
            while (var8_9 < files.length) {
                files[var8_9] = new File(largs.get((int)var8_9));
                ++var8_9;
            }
            HangingProtocols.OpeningViewer openingViewer = HangingProtocols.OpeningViewer.ALL_PATIENTS;
            LOADING_EXECUTOR.execute(new LoadLocalDicom(files, true, (DataExplorerModel)this, openingViewer));
        }
        if (opt.isSet("remote")) {
            LOADING_EXECUTOR.execute(new LoadRemoteDicomURL(rargs.toArray(new String[0]), (DataExplorerModel)this));
        }
        if (opt.isSet("wado")) {
            LOADING_EXECUTOR.execute(new LoadRemoteDicomManifest(wargs, this));
        }
        if (opt.isSet("zip")) {
            for (String string : zargs) {
                DicomZipMediaIO.loadDicomZip(string, this);
            }
        }
        if (opt.isSet("iwado")) {
            ArrayList<String> xmlFiles = new ArrayList<String>(iargs.size());
            for (String iarg : iargs) {
                try {
                    Path tempFile = Files.createTempFile(AppProperties.APP_TEMP_DIR, "wado_", ".xml", new FileAttribute[0]);
                    if (!GzipManager.gzipUncompressToFile((byte[])Base64.getDecoder().decode(iarg), (File)tempFile.toFile())) continue;
                    xmlFiles.add(tempFile.toString());
                }
                catch (Exception e) {
                    LOGGER.info("ungzip manifest", (Throwable)e);
                }
            }
            LOADING_EXECUTOR.execute(new LoadRemoteDicomManifest(xmlFiles, this));
        }
        if (opt.isSet("portable")) {
            String prop = System.getProperty("weasis.portable.dicom.directory");
            String string = System.getProperty("weasis.portable.dir");
            if (prop != null && string != null) {
                String[] dirs = prop.split(",");
                for (int i = 0; i < dirs.length; ++i) {
                    dirs[i] = dirs[i].trim().replace("/", File.separator);
                }
                File[] files = new File[dirs.length];
                boolean notCaseSensitive = SystemInfo.isWindows;
                if (notCaseSensitive) {
                    Arrays.sort(dirs, String.CASE_INSENSITIVE_ORDER);
                }
                String last = null;
                for (int i = 0; i < files.length; ++i) {
                    if (notCaseSensitive && dirs[i].equalsIgnoreCase(last)) {
                        last = null;
                        continue;
                    }
                    last = dirs[i];
                    files[i] = new File(string, dirs[i]);
                }
                List<LoadSeries> loadSeries = null;
                File dcmDirFile = new File(string, "DICOMDIR");
                if (dcmDirFile.canRead()) {
                    DicomDirLoader dirImport = new DicomDirLoader(dcmDirFile, this, DicomManager.getInstance().isPortableDirCache());
                    loadSeries = dirImport.readDicomDir();
                }
                if (loadSeries != null && !loadSeries.isEmpty()) {
                    LOADING_EXECUTOR.execute(new LoadDicomDir(loadSeries, this, HangingProtocols.OpeningViewer.ALL_PATIENTS));
                } else {
                    LOADING_EXECUTOR.execute(new LoadLocalDicom(files, true, (DataExplorerModel)this, HangingProtocols.OpeningViewer.ALL_PATIENTS));
                }
            }
        }
    }

    public void rs(String[] argv) throws IOException {
        String oidcLogin;
        String oidcIssuer;
        String authUID;
        String acceptExt;
        String retrieveExt;
        String[] usage = new String[]{"Load DICOM files from DICOMweb API (QIDO/WADO-RS)", "Usage: dicom:rs -u URL -r QUERYPARAMS... [-H HEADER]... [--query-header HEADER]... [--retrieve-header HEADER]... [--query-ext EXT] [--retrieve-ext EXT] [--accept-ext EXT]", "  -u --url=URL               URL of the DICOMweb service", "  -r --request=QUERYPARAMS   Query params of the URL, see weasis-pacs-connector", "  -H --header=HEADER         Pass custom header(s) to all the requests", "  --query-header=HEADER      Pass custom header(s) to the query requests (QIDO)", "  --retrieve-header=HEADER   Pass custom header(s) to the retrieve requests (WADO)", "  --query-ext=EXT            Additional parameters for Query URL (QIDO)", "  --retrieve-ext=EXT         Additional parameters for Retrieve URL (WADO)", "  --accept-ext=EXT           Additional parameters for DICOM multipart/related Accept header of the retrieve URL (WADO). Default value is: transfer-syntax=*", "  --auth-uid=UID             UID of the Weasis authentication method", "  --oidc-iss=UID             Issuer Identifier for OpenID Connect Authentication Request", "  --oidc-login=UID           Identifier the End-User might use to log in (OpenID Connect)", "  --show-whole-study         when downloading a series, show all the other series (ready for download) from the same study", "  -? --help                  show help"};
        Option opt = Options.compile((String[])usage).parse((Object[])argv);
        String rsUrl = opt.get("url");
        List pargs = opt.getList("request");
        if (opt.isSet("help") || rsUrl.isEmpty() || pargs.isEmpty()) {
            opt.usage();
            return;
        }
        Properties props = new Properties();
        props.setProperty("dicomweb.url", rsUrl);
        String queryExt = opt.get("query-ext");
        if (StringUtil.hasText((CharSequence)queryExt)) {
            props.setProperty("query.ext", queryExt);
        }
        if (StringUtil.hasText((CharSequence)(retrieveExt = opt.get("retrieve-ext")))) {
            props.setProperty("retrieve.ext", retrieveExt);
        }
        if (!StringUtil.hasText((CharSequence)(acceptExt = opt.get("accept-ext")))) {
            acceptExt = "transfer-syntax=*";
        }
        props.setProperty("accept.ext", acceptExt);
        if (opt.isSet("show-whole-study")) {
            props.setProperty("show.whole.study", Boolean.TRUE.toString());
        }
        if (StringUtil.hasText((CharSequence)(authUID = opt.get("auth-uid")))) {
            props.setProperty("auth.uid", authUID);
        }
        if (StringUtil.hasText((CharSequence)(oidcIssuer = opt.get("oidc-iss")))) {
            props.setProperty("oidc.issuer", oidcIssuer);
        }
        if (StringUtil.hasText((CharSequence)(oidcLogin = opt.get("oidc-login")))) {
            props.setProperty("oidc.user", oidcLogin);
        }
        GuiExecutor.execute(() -> {
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.SELECT, (Object)this, null, (Object)this));
            for (String query : pargs) {
                List common = opt.getList("header");
                List q = opt.getList("query-header");
                q.addAll(common);
                List r = opt.getList("retrieve-header");
                r.addAll(common);
                RsQueryParams rsquery = new RsQueryParams(this, props, RsQueryParams.getQueryMap(query), RsQueryParams.getHeaders(q), RsQueryParams.getHeaders(r));
                LOADING_EXECUTOR.execute(rsquery);
            }
        });
    }

    public void close(String[] argv) throws IOException {
        String[] usage = new String[]{"Close DICOM files", "Usage: dicom:close  (-a | ([-y UID]... [-s UID]...))", "  -a --all           close all the patients", "  -p --patient=ID    close a patient from its patient ID", "  -y --study=UID     close a study, UID is Study Instance UID", "  -s --series=UID    close a series, UID is Series Instance UID", "  -? --help          show help"};
        Option opt = Options.compile((String[])usage).parse((Object[])argv);
        List pargs = opt.getList("patient");
        List yargs = opt.getList("study");
        List sargs = opt.getList("series");
        if (opt.isSet("help") || pargs.isEmpty() && yargs.isEmpty() && sargs.isEmpty() && !opt.isSet("all")) {
            opt.usage();
            return;
        }
        GuiExecutor.execute(() -> {
            this.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.SELECT, (Object)this, null, (Object)this));
            this.closeCommand(opt, pargs, yargs, sargs);
        });
    }

    private void closeCommand(Option opt, List<String> pargs, List<String> yargs, List<String> sargs) {
        block7: {
            block6: {
                if (!opt.isSet("all")) break block6;
                for (MediaSeriesGroup patientGroup : this.model.getSuccessors((Object)MediaSeriesGroupNode.rootNode)) {
                    this.removePatient(patientGroup);
                }
                break block7;
            }
            if (opt.isSet("patient")) {
                for (MediaSeriesGroup patientGroup : this.model.getSuccessors((Object)MediaSeriesGroupNode.rootNode)) {
                    String patientID = (String)TagD.getTagValue((TagReadable)patientGroup, (int)0x100020, String.class);
                    if (!pargs.contains(patientID)) continue;
                    this.removePatient(patientGroup);
                }
            }
            if (opt.isSet("study")) {
                for (String studyUID : yargs) {
                    this.removeStudy(this.getStudyNode(studyUID));
                }
            }
            if (!opt.isSet("series")) break block7;
            for (String seriesUID : sargs) {
                this.findSeriesToRemove(seriesUID);
            }
        }
    }

    private void findSeriesToRemove(String seriesUID) {
        for (MediaSeriesGroup ptGroup : this.model.getSuccessors((Object)MediaSeriesGroupNode.rootNode)) {
            for (MediaSeriesGroup stGroup : this.model.getSuccessors((Object)ptGroup)) {
                String uid;
                MediaSeriesGroup group;
                int k = 1;
                while ((group = this.getHierarchyNode(stGroup, uid = "#" + k + "." + seriesUID)) != null) {
                    this.removeSeries(group);
                    ++k;
                }
                MediaSeriesGroup seGroup = this.getHierarchyNode(stGroup, seriesUID);
                if (seGroup == null) continue;
                this.removeSeries(seGroup);
                return;
            }
        }
    }

    public TreeModelNode getTreeModelNodeForNewPlugin() {
        return patient;
    }
}

