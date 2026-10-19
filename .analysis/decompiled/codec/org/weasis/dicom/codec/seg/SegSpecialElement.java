/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.ResourceUtil$OtherIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.ui.model.graphic.imp.seg.SegMeasurableLayer
 *  org.weasis.core.ui.model.graphic.imp.seg.SegRegion
 *  org.weasis.core.ui.util.StructToolTipTreeNode
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.seg.RegionAttributes
 */
package org.weasis.dicom.codec.seg;

import java.awt.Color;
import java.awt.EventQueue;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javax.swing.tree.DefaultMutableTreeNode;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.img.data.CIELab;
import org.dcm4che3.img.util.DicomUtils;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.model.graphic.imp.seg.SegMeasurableLayer;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.core.ui.util.StructToolTipTreeNode;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.HiddenSeriesManager;
import org.weasis.dicom.codec.HiddenSpecialElement;
import org.weasis.dicom.codec.SpecialElementReferences;
import org.weasis.dicom.codec.SpecialElementRegion;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.geometry.VectorUtils;
import org.weasis.dicom.codec.seg.BasicContourLoader;
import org.weasis.dicom.codec.seg.CachedContourLoader;
import org.weasis.dicom.codec.seg.FractionalContourLoader;
import org.weasis.dicom.codec.seg.LabelMapContourLoader;
import org.weasis.dicom.codec.seg.LazyContourLoader;
import org.weasis.dicom.codec.seg.SegBuildScheduler;
import org.weasis.dicom.codec.seg.SegVisibilityPolicy;
import org.weasis.dicom.codec.seg.SegVolumeCache;
import org.weasis.dicom.codec.seg.SegmentationVolume;
import org.weasis.dicom.codec.seg.SegmentationVolumeBuildExecutor;
import org.weasis.dicom.codec.seg.SegmentationVolumeBuilder;
import org.weasis.dicom.codec.utils.DicomMediaUtils;
import org.weasis.dicom.macro.Code;
import org.weasis.opencv.seg.RegionAttributes;

public class SegSpecialElement
extends HiddenSpecialElement
implements SpecialElementReferences,
SpecialElementRegion {
    private static final Logger LOGGER = LoggerFactory.getLogger(SegSpecialElement.class);
    private static volatile SegmentationVolumeBuildExecutor volumeBuildExecutor = SegmentationVolumeBuildExecutor.DEFAULT;
    private static final Map<String, Integer> PALETTE_BASES = new ConcurrentHashMap<String, Integer>();
    private static final AtomicInteger NEXT_PALETTE_INDEX = new AtomicInteger(1);
    private final Map<String, Map<String, Set<LazyContourLoader>>> refMap = new HashMap<String, Map<String, Set<LazyContourLoader>>>();
    private final Map<Integer, LazyContourLoader> roiMap = new HashMap<Integer, LazyContourLoader>();
    private final TreeMap<Double, Set<LazyContourLoader>> positionMap = new TreeMap();
    private final Map<Integer, SegRegion<DicomImageElement>> segAttributes = new HashMap<Integer, SegRegion<DicomImageElement>>();
    private volatile Vector3d referenceNormal;
    private volatile SoftReference<SegmentationVolume> segVolumeRef;
    private final Object segVolumeLock = new Object();
    private final Map<Object, SoftReference<SegmentationVolume>> alignedVolumes = new WeakHashMap<Object, SoftReference<SegmentationVolume>>();
    private volatile CompletableFuture<SegmentationVolume> segVolumeBuildFuture;
    private volatile boolean segVolumeBuildAborted;
    private volatile boolean segVolumeUnavailable;
    private volatile boolean segVolumeBudgetEvicted;
    private volatile double segSliceSpacing;
    private volatile float opacity = 1.0f;
    private volatile Boolean visible;
    private volatile boolean defaultHidden;
    private volatile long defaultGeneration = -1L;
    private volatile LoadState loadState = LoadState.PENDING;

    public static void setVolumeBuildExecutor(SegmentationVolumeBuildExecutor exec) {
        volumeBuildExecutor = exec == null ? SegmentationVolumeBuildExecutor.DEFAULT : exec;
    }

    public SegSpecialElement(DicomMediaIO mediaIO) {
        super(mediaIO);
    }

    @Override
    public boolean isReady() {
        return this.loadState == LoadState.READY;
    }

    public LoadState getLoadState() {
        return this.loadState;
    }

    public void disposeContours() {
        this.roiMap.clear();
        this.positionMap.clear();
        this.segAttributes.clear();
        this.referenceNormal = null;
        this.segSliceSpacing = 0.0;
        this.disposeSegmentationVolume();
    }

    private Attributes dicomObject() {
        return this.mediaIO == null ? null : ((DicomMediaIO)this.mediaIO).getDicomObject();
    }

    private DicomImageElement[] segFrames() {
        DicomImageElement[] dicomImageElementArray;
        MediaReader mediaReader = this.mediaIO;
        if (mediaReader instanceof DicomMediaIO) {
            DicomMediaIO reader = (DicomMediaIO)mediaReader;
            dicomImageElementArray = reader.getMediaElement();
        } else {
            dicomImageElementArray = null;
        }
        DicomImageElement[] frames = dicomImageElementArray;
        return frames == null ? new DicomImageElement[]{} : frames;
    }

    private static DicomImageElement frameAt(DicomImageElement[] frames, int frameIndex) {
        return frameIndex < 0 || frameIndex >= frames.length ? null : frames[frameIndex];
    }

    public static DefaultMutableTreeNode buildStructRegionNode(SegRegion<?> contour) {
        return new StructToolTipTreeNode(contour, false){

            public String getToolTipText() {
                return ((SegRegion)this.getUserObject()).getToolTipHtml();
            }
        };
    }

    @Override
    protected void initLabel() {
        Attributes dicom;
        StringBuilder buf = new StringBuilder();
        Integer val = TagD.getTagValue((TagReadable)this, 2097171, Integer.class);
        if (val != null) {
            buf.append("[").append(val).append("] ");
        }
        if ((dicom = this.dicomObject()) != null) {
            String item = dicom.getString(528446);
            if (item != null) {
                buf.append(item);
            }
            if ((item = dicom.getString(0x700080)) != null) {
                buf.append(" - ").append(item);
            }
        }
        this.label = buf.toString();
    }

    @Override
    public Map<String, Map<String, Set<LazyContourLoader>>> getRefMap() {
        return this.refMap;
    }

    @Override
    public NavigableMap<Double, Set<LazyContourLoader>> getPositionMap() {
        return this.positionMap;
    }

    @Override
    public Vector3d getReferenceNormal() {
        Vector3d n = this.referenceNormal;
        return n == null ? null : new Vector3d((Vector3dc)n);
    }

    @Override
    public double getSliceSpacing() {
        return this.segSliceSpacing;
    }

    @Override
    public Vector3d getMaskRowDirection() {
        double[] iop = this.readSegIop();
        return iop == null ? null : new Vector3d(iop[0], iop[1], iop[2]);
    }

    @Override
    public Vector3d getMaskColumnDirection() {
        double[] iop = this.readSegIop();
        return iop == null ? null : new Vector3d(iop[3], iop[4], iop[5]);
    }

    @Override
    public double[] getMaskPixelSpacing() {
        Attributes dicom = this.dicomObject();
        if (dicom == null) {
            return null;
        }
        double[] ps = SegSpecialElement.readNestedDoubleArray(dicom, 1375769129, 2658576, 2621488);
        if (ps != null && ps.length >= 2) {
            return ps;
        }
        return DicomUtils.getDoubleArrayFromDicomElement(dicom, 2621488, null);
    }

    @Override
    public int getMaskWidth() {
        Attributes dicom = this.dicomObject();
        return dicom == null ? 0 : dicom.getInt(2621457, 0);
    }

    @Override
    public int getMaskHeight() {
        Attributes dicom = this.dicomObject();
        return dicom == null ? 0 : dicom.getInt(2621456, 0);
    }

    private double[] readSegIop() {
        Attributes dicom = this.dicomObject();
        if (dicom == null) {
            return null;
        }
        double[] iop = SegSpecialElement.readNestedDoubleArray(dicom, 1375769129, 2134294, 2097207);
        if (iop != null && iop.length == 6) {
            return iop;
        }
        return DicomUtils.getDoubleArrayFromDicomElement(dicom, 2097207, null);
    }

    private static double[] readNestedDoubleArray(Attributes container, int outerSeqTag, int innerSeqTag, int valueTag) {
        Attributes outer = container.getNestedDataset(outerSeqTag);
        if (outer == null) {
            return null;
        }
        Attributes inner = outer.getNestedDataset(innerSeqTag);
        if (inner == null) {
            return null;
        }
        return DicomUtils.getDoubleArrayFromDicomElement(inner, valueTag, null);
    }

    @Override
    public SegmentationVolume peekCanonicalSegmentationVolume() {
        SoftReference<SegmentationVolume> ref = this.segVolumeRef;
        return ref == null ? null : ref.get();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SegmentationVolume getOrBuildSegmentationVolume() {
        SegmentationVolume v;
        SoftReference<SegmentationVolume> ref = this.segVolumeRef;
        SegmentationVolume segmentationVolume = v = ref == null ? null : ref.get();
        if (v != null) {
            SegVolumeCache.touch(this);
            return v;
        }
        if (EventQueue.isDispatchThread()) {
            if (this.segVolumeBuildAborted || this.segVolumeUnavailable || this.segVolumeBudgetEvicted) {
                return null;
            }
            this.ensureAsyncBuild();
            return null;
        }
        Object object = this.segVolumeLock;
        synchronized (object) {
            ref = this.segVolumeRef;
            SegmentationVolume segmentationVolume2 = v = ref == null ? null : ref.get();
            if (v != null) {
                SegVolumeCache.touch(this);
                return v;
            }
            if (this.segVolumeUnavailable) {
                return null;
            }
            this.segVolumeBudgetEvicted = false;
            DicomSeries series = this.getMediaReader() == null ? null : this.getMediaReader().getMediaSeries();
            v = SegmentationVolumeBuilder.buildCanonical(this, series);
            if (v == null) {
                this.segVolumeUnavailable = true;
            } else {
                this.segVolumeRef = new SoftReference<SegmentationVolume>(v);
            }
        }
        if (v != null) {
            SegVolumeCache.register(this, v.heapBytes());
        }
        return v;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void releaseCanonicalVolume() {
        SegmentationVolume v;
        Object object = this.segVolumeLock;
        synchronized (object) {
            SoftReference<SegmentationVolume> ref = this.segVolumeRef;
            v = ref == null ? null : ref.get();
            this.segVolumeRef = null;
            this.segVolumeBudgetEvicted = true;
        }
        SegVolumeCache.remove(this);
        if (v != null) {
            v.removeData();
        }
    }

    private boolean isCanonicalBuildBlocked() {
        return this.segVolumeBuildFuture != null || this.segVolumeBuildAborted || this.segVolumeUnavailable || this.segVolumeBudgetEvicted;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void ensureAsyncBuild() {
        SoftReference<SegmentationVolume> ref = this.segVolumeRef;
        if (ref != null && ref.get() != null) {
            return;
        }
        if (this.isCanonicalBuildBlocked()) {
            return;
        }
        Object object = this.segVolumeLock;
        synchronized (object) {
            DicomSeries series;
            ref = this.segVolumeRef;
            if (ref != null && ref.get() != null) {
                return;
            }
            if (this.isCanonicalBuildBlocked()) {
                return;
            }
            DicomSeries dicomSeries = series = this.getMediaReader() == null ? null : this.getMediaReader().getMediaSeries();
            if (series == null) {
                return;
            }
            CompletableFuture result = new CompletableFuture();
            this.segVolumeBuildFuture = result;
            result.whenComplete((vol, err) -> this.finishAsyncBuild(series, result, (SegmentationVolume)vol, (Throwable)err));
            SegBuildScheduler.submitAsync(() -> this.startCanonicalBuild(series, result));
        }
    }

    private void startCanonicalBuild(DicomSeries series, CompletableFuture<SegmentationVolume> result) {
        CompletableFuture<SegmentationVolume> build;
        if (result.isDone()) {
            SegBuildScheduler.release();
            return;
        }
        try {
            build = volumeBuildExecutor.schedule(this, () -> SegmentationVolumeBuilder.buildCanonical(this, series));
        }
        catch (RuntimeException e) {
            SegBuildScheduler.release();
            result.completeExceptionally(e);
            return;
        }
        build.whenComplete((vol, err) -> {
            SegBuildScheduler.release();
            if (err == null) {
                result.complete((SegmentationVolume)vol);
            } else {
                result.completeExceptionally((Throwable)err);
            }
        });
        result.whenComplete((segmentationVolume, err) -> {
            if (err instanceof CancellationException) {
                build.cancel(true);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void finishAsyncBuild(DicomSeries series, CompletableFuture<SegmentationVolume> result, SegmentationVolume vol, Throwable err) {
        Object object = this.segVolumeLock;
        synchronized (object) {
            if (vol != null) {
                this.segVolumeRef = new SoftReference<SegmentationVolume>(vol);
            } else if (!(err instanceof CancellationException)) {
                this.segVolumeUnavailable = true;
            }
            if (this.segVolumeBuildFuture == result) {
                this.segVolumeBuildFuture = null;
            }
            if (err instanceof CancellationException) {
                this.segVolumeBuildAborted = true;
            }
        }
        if (vol != null) {
            SegVolumeCache.register(this, vol.heapBytes());
        }
        if (err instanceof CancellationException) {
            GuiExecutor.execute(() -> this.unloadAfterAbort(series));
        } else {
            if (err != null) {
                LOGGER.error("Async build of canonical segmentation volume failed", err);
            }
            this.fireSegUpdateEvent();
        }
    }

    private void fireSegUpdateEvent() {
        List explorerPlugins = GuiUtils.getUICore().getExplorerPlugins();
        explorerPlugins.stream().map(DataExplorerView::getDataExplorerModel).filter(m -> "DICOM".equals(m.toString())).findFirst().ifPresent(model -> model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, model, null, (Object)this)));
    }

    @Override
    public boolean isSegmentationVolumeBuilding() {
        return this.segVolumeBuildFuture != null;
    }

    public boolean isSegmentationVolumeBuildAborted() {
        return this.segVolumeBuildAborted;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void retrySegmentationVolumeBuild() {
        Object object = this.segVolumeLock;
        synchronized (object) {
            this.segVolumeBuildAborted = false;
            this.segVolumeUnavailable = false;
            this.segVolumeBudgetEvicted = false;
        }
    }

    private void unloadAfterAbort(DicomSeries series) {
        this.roiMap.clear();
        this.refMap.clear();
        this.positionMap.clear();
        this.segAttributes.clear();
        this.referenceNormal = null;
        this.segSliceSpacing = 0.0;
        this.unregisterFromHiddenSeriesManager(series);
        LOGGER.info("Segmentation volume build cancelled \u2014 unloading SEG data");
        this.fireSegUpdateEvent();
    }

    private void unregisterFromHiddenSeriesManager(DicomSeries series) {
        Set<String> refs;
        String sopUID;
        String seriesUID = TagD.getTagValue((TagReadable)series, 0x20000E, String.class);
        if (!StringUtil.hasText((CharSequence)seriesUID)) {
            return;
        }
        HiddenSeriesManager manager = HiddenSeriesManager.getInstance();
        Set<HiddenSpecialElement> set = manager.series2Elements.get(seriesUID);
        boolean seriesEmptyAfter = false;
        if (set != null) {
            set.remove(this);
            SegVisibilityPolicy.invalidate();
            if (set.isEmpty()) {
                manager.series2Elements.remove(seriesUID);
                seriesEmptyAfter = true;
            }
        }
        if ((sopUID = TagD.getTagValue((TagReadable)this, 524312, String.class)) != null && (refs = manager.sopRef2Series.get(sopUID)) != null) {
            refs.remove(seriesUID);
            if (refs.isEmpty()) {
                manager.sopRef2Series.remove(sopUID);
            }
        }
        if (seriesEmptyAfter) {
            Set<String> list;
            String patientPseudoUID = (String)this.getTagValue(TagW.PatientPseudoUID);
            if (patientPseudoUID != null && (list = manager.patient2Series.get(patientPseudoUID)) != null) {
                list.remove(seriesUID);
                if (list.isEmpty()) {
                    manager.patient2Series.remove(patientPseudoUID);
                }
            }
            manager.reference2Series.entrySet().removeIf(entry -> {
                Set referencingSeries = (Set)entry.getValue();
                referencingSeries.remove(seriesUID);
                return referencingSeries.isEmpty();
            });
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void disposeSegmentationVolume() {
        Object object = this.segVolumeLock;
        synchronized (object) {
            CompletableFuture<SegmentationVolume> f = this.segVolumeBuildFuture;
            if (f != null) {
                f.cancel(true);
                this.segVolumeBuildFuture = null;
            }
            this.segVolumeBuildAborted = false;
            this.segVolumeUnavailable = false;
            this.segVolumeBudgetEvicted = false;
            SoftReference<SegmentationVolume> ref = this.segVolumeRef;
            if (ref != null) {
                SegmentationVolume v = ref.get();
                if (v != null) {
                    v.removeData();
                }
                this.segVolumeRef = null;
            }
            SegVolumeCache.remove(this);
            for (SoftReference<SegmentationVolume> r : this.alignedVolumes.values()) {
                SegmentationVolume v = r == null ? null : r.get();
                if (v == null) continue;
                int remaining = v.getUseCount();
                if (remaining > 0) {
                    LOGGER.warn("disposeSegmentationVolume: freeing SegmentationVolume with {} active consumer(s) \u2014 views displaying this SEG may show stale or empty overlays", (Object)remaining);
                }
                v.removeData();
            }
            this.alignedVolumes.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SegmentationVolume getOrBuildAlignedVolume(Object imageVolumeKey, Function<SegSpecialElement, SegmentationVolume> builder) {
        if (imageVolumeKey == null || builder == null) {
            return null;
        }
        Object object = this.segVolumeLock;
        synchronized (object) {
            SegmentationVolume v;
            SoftReference<SegmentationVolume> ref = this.alignedVolumes.get(imageVolumeKey);
            SegmentationVolume segmentationVolume = v = ref == null ? null : ref.get();
            if (v != null && !v.isDisposed()) {
                return v;
            }
            if (ref != null) {
                this.alignedVolumes.remove(imageVolumeKey);
            }
            if ((v = builder.apply(this)) != null) {
                this.alignedVolumes.put(imageVolumeKey, new SoftReference<SegmentationVolume>(v));
            }
            return v;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void disposeAlignedVolume(Object imageVolumeKey) {
        if (imageVolumeKey == null) {
            return;
        }
        Object object = this.segVolumeLock;
        synchronized (object) {
            SegmentationVolume v;
            SoftReference<SegmentationVolume> ref = this.alignedVolumes.remove(imageVolumeKey);
            SegmentationVolume segmentationVolume = v = ref == null ? null : ref.get();
            if (v != null && v.getUseCount() <= 0) {
                v.removeData();
            }
        }
    }

    public Map<Integer, SegRegion<DicomImageElement>> getSegAttributes() {
        return this.segAttributes;
    }

    @Override
    public boolean isFractionalSeg() {
        for (SegRegion<DicomImageElement> r : this.segAttributes.values()) {
            if (!r.isFractional()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isVisible() {
        Boolean chosen = this.visible;
        if (chosen != null) {
            return chosen;
        }
        long generation = SegVisibilityPolicy.generation();
        if (generation != this.defaultGeneration) {
            this.defaultHidden = SegVisibilityPolicy.isHiddenByDefault(this);
            this.defaultGeneration = generation;
        }
        return !this.defaultHidden;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public float getOpacity() {
        return this.opacity;
    }

    @Override
    public void setOpacity(float opacity) {
        this.opacity = Math.clamp((float)opacity, (float)0.0f, (float)1.0f);
        this.updateOpacityInSegAttributes(this.opacity);
    }

    @Override
    public void initReferences(String originSeriesUID) {
        this.refMap.clear();
        Attributes dicom = this.dicomObject();
        if (dicom != null) {
            Function<String, Map<String, Set<LazyContourLoader>>> addSeries = seriesUID -> this.refMap.computeIfAbsent((String)seriesUID, string -> new HashMap());
            HiddenSeriesManager.getInstance().extractReferencedSeries(dicom, originSeriesUID, addSeries);
        }
    }

    @Override
    public ResourceUtil.ResourceIconPath getIconPath() {
        return ResourceUtil.OtherIcon.SEGMENTATION;
    }

    public void initContours(DicomSeries series, List<DicomSeries> refSeriesList) {
        if (series == null) {
            this.loadState = LoadState.READY;
            return;
        }
        this.loadState = LoadState.LOADING;
        this.roiMap.clear();
        Attributes dicom = this.dicomObject();
        if (dicom == null) {
            this.loadState = LoadState.READY;
            return;
        }
        boolean failed = false;
        try {
            SegmentationKind kind = SegmentationKind.fromDicom(dicom.getString(6422529));
            int maxFractionalValue = dicom.getInt(6422542, 255);
            String fractionalType = kind == SegmentationKind.FRACTIONAL ? dicom.getString(6422544) : null;
            Map<SegRegion<DicomImageElement>, FrameRange> regionPosition = this.parseSegmentSequence(dicom.getSequence(0x620002), kind, fractionalType, dicom.getString(524312));
            Sequence perFrameSeq = dicom.getSequence(1375769136);
            if (perFrameSeq != null) {
                this.processPerFrameSequence(series, refSeriesList, regionPosition, perFrameSeq, kind, maxFractionalValue);
                if (kind == SegmentationKind.LABELMAP && !regionPosition.isEmpty() && !perFrameSeq.isEmpty()) {
                    int last = perFrameSeq.size() - 1;
                    regionPosition.replaceAll((segRegion, frameRange) -> new FrameRange(0, last));
                }
            }
            this.refineSegSliceSpacingFromPositionMap();
            regionPosition.forEach((region, p) -> region.setMeasurableLayer(this.getMeasurableLayer((FrameRange)((Object)p))));
            this.loadState = failed ? LoadState.FAILED : LoadState.READY;
        }
        catch (RuntimeException e) {
            try {
                failed = true;
                LOGGER.error("Error initializing SEG contours", (Throwable)e);
                throw e;
            }
            catch (Throwable throwable) {
                this.loadState = failed ? LoadState.FAILED : LoadState.READY;
                SegVisibilityPolicy.invalidate();
                throw throwable;
            }
        }
        SegVisibilityPolicy.invalidate();
    }

    private Map<SegRegion<DicomImageElement>, FrameRange> parseSegmentSequence(Sequence segSeq, SegmentationKind kind, String fractionalType, String sopInstanceUID) {
        HashMap<SegRegion<DicomImageElement>, FrameRange> regionPosition = new HashMap<SegRegion<DicomImageElement>, FrameRange>();
        if (segSeq == null) {
            return regionPosition;
        }
        int colorIndex = SegSpecialElement.paletteBase(sopInstanceUID, segSeq.size());
        for (Attributes seg : segSeq) {
            Sequence categorySeq;
            Sequence regionSeq;
            int nb = seg.getInt(6422532, -1);
            String segmentLabel = seg.getString(6422533, "" + nb);
            int[] colorRgb = CIELab.dicomLab2rgb(DicomUtils.getIntArrayFromDicomElement(seg, 6422541, null));
            Color rgbColor = RegionAttributes.getColor((int[])colorRgb, (int)colorIndex++, (float)this.opacity);
            SegRegion attributes = new SegRegion(nb, segmentLabel, rgbColor);
            attributes.setInteriorOpacity(0.2f);
            attributes.setDescription(seg.getString(0x620006));
            attributes.setType(seg.getString(6422536));
            attributes.setAlgorithmName(seg.getString(6422537));
            if (kind == SegmentationKind.FRACTIONAL) {
                attributes.setFractionalType(fractionalType);
            }
            if ((regionSeq = seg.getSequence(533016)) != null) {
                attributes.setAnatomicRegionCodes(Code.toCodeMacros(regionSeq).stream().map(Code::getCodeMeaning).toList());
            }
            if ((categorySeq = seg.getSequence(6422531)) != null) {
                attributes.setCategories(Code.toCodeMacros(categorySeq).stream().map(Code::getCodeMeaning).toList());
            }
            this.segAttributes.put(nb, (SegRegion<DicomImageElement>)attributes);
            regionPosition.put((SegRegion<DicomImageElement>)attributes, FrameRange.EMPTY);
        }
        return regionPosition;
    }

    static int paletteBase(String sopInstanceUID, int segmentCount) {
        if (!StringUtil.hasText((CharSequence)sopInstanceUID)) {
            return NEXT_PALETTE_INDEX.getAndAdd(segmentCount);
        }
        return PALETTE_BASES.computeIfAbsent(sopInstanceUID, string -> NEXT_PALETTE_INDEX.getAndAdd(segmentCount));
    }

    private void processPerFrameSequence(DicomSeries series, List<DicomSeries> refSeriesList, Map<SegRegion<DicomImageElement>, FrameRange> regionPosition, Sequence perFrameSeq, SegmentationKind kind, int maxFractionalValue) {
        Attributes dicom = this.dicomObject();
        if (dicom == null) {
            return;
        }
        Attributes sharedFG = dicom.getNestedDataset(1375769129);
        String segFrameOfRefUID = dicom.getString(0x200052);
        String seriesFrameOfRefUID = TagD.getTagValue((TagReadable)series, 0x200052, String.class);
        boolean sameFrameOfRef = segFrameOfRefUID != null && segFrameOfRefUID.equals(seriesFrameOfRefUID);
        DicomImageElement[] frames = this.segFrames();
        int index = 0;
        for (Attributes frame : perFrameSeq) {
            boolean addedByPosition;
            LazyContourLoader loader;
            DicomImageElement binaryMask;
            if ((binaryMask = SegSpecialElement.frameAt(frames, ++index - 1)) != null) {
                this.registerFrameLoader(frame, binaryMask, index, kind, maxFractionalValue, regionPosition);
            }
            if ((loader = this.roiMap.get(index)) == null || (addedByPosition = sameFrameOfRef && this.addPositionMap(frame, sharedFG, loader))) continue;
            this.associateContours(refSeriesList, frame, sharedFG, loader, index, sameFrameOfRef);
        }
    }

    private void registerFrameLoader(Attributes frame, DicomImageElement binaryMask, int index, SegmentationKind kind, int maxFractionalValue, Map<SegRegion<DicomImageElement>, FrameRange> regionPosition) {
        if (kind == SegmentationKind.LABELMAP) {
            this.roiMap.put(index, new LabelMapContourLoader(binaryMask, index, this.segAttributes));
            return;
        }
        Attributes refSeqNb = frame.getNestedDataset(6422538);
        if (refSeqNb == null) {
            return;
        }
        Integer segmentNumber = DicomUtils.getIntegerFromDicomElement(refSeqNb, 6422539, null);
        if (segmentNumber == null) {
            return;
        }
        SegRegion<DicomImageElement> region = this.segAttributes.get(segmentNumber);
        if (region == null) {
            return;
        }
        CachedContourLoader loader = kind == SegmentationKind.FRACTIONAL ? new FractionalContourLoader(binaryMask, index, region, maxFractionalValue) : new BasicContourLoader(binaryMask, index, region);
        this.roiMap.put(index, loader);
        FrameRange p = regionPosition.get(region);
        if (p != null) {
            regionPosition.put(region, p.withFrame(index - 1));
        }
    }

    private static Boolean isSpatialLocationsPreserved(Sequence derivationSeq) {
        if (derivationSeq == null) {
            return null;
        }
        for (Attributes derivation : derivationSeq) {
            Sequence srcSeq = derivation.getSequence(532754);
            if (srcSeq == null) continue;
            for (Attributes src : srcSeq) {
                String slp = src.getString(2626394);
                if ("YES".equals(slp)) {
                    return Boolean.TRUE;
                }
                if (!"NO".equals(slp)) continue;
                return Boolean.FALSE;
            }
        }
        return null;
    }

    private void associateContours(List<DicomSeries> refSeriesList, Attributes frame, Attributes sharedFG, LazyContourLoader loader, int index, boolean sameFrameOfRef) {
        HashMap<String, List<Integer>> sopUIDToFramesMap = new HashMap<String, List<Integer>>();
        Sequence derivationSeq = frame.getSequence(561444);
        if (derivationSeq != null) {
            Boolean spatialPreserved = SegSpecialElement.isSpatialLocationsPreserved(derivationSeq);
            if (Boolean.FALSE.equals(spatialPreserved)) {
                LOGGER.debug("Segmentation frame {} declares SpatialLocationsPreserved=NO; skipping SOP UID-based pixel overlay, using spatial fallback", (Object)index);
                if (sameFrameOfRef) {
                    this.addPositionMap(frame, sharedFG, loader);
                } else {
                    this.addPositionMapFromRefSeries(frame, sharedFG, refSeriesList, loader);
                }
                return;
            }
            for (Attributes derivation : derivationSeq) {
                HiddenSeriesManager.addSourceImage(derivation, sopUIDToFramesMap);
            }
        }
        if (sopUIDToFramesMap.isEmpty()) {
            if (!refSeriesList.isEmpty() && this.addPositionMapFromRefSeries(frame, sharedFG, refSeriesList, loader)) {
                return;
            }
            this.roiMap.put(index, loader);
            return;
        }
        this.mergeFramesIntoRefMap(sopUIDToFramesMap, loader);
    }

    private void mergeFramesIntoRefMap(Map<String, List<Integer>> sopUIDToFramesMap, LazyContourLoader loader) {
        for (Map<String, Set<LazyContourLoader>> map : this.refMap.values()) {
            sopUIDToFramesMap.forEach((sopUID, frames) -> {
                Set list = (Set)map.get(sopUID);
                if (list == null) {
                    return;
                }
                if (frames.isEmpty()) {
                    list.add(loader);
                } else {
                    for (Integer frameNumber : frames) {
                        map.computeIfAbsent(sopUID + "_" + frameNumber, string -> new LinkedHashSet()).add(loader);
                    }
                }
            });
        }
    }

    private boolean addPositionMapFromRefSeries(Attributes frame, Attributes sharedFG, List<DicomSeries> refSeriesList, LazyContourLoader loader) {
        if (this.addPositionMap(frame, sharedFG, loader)) {
            return true;
        }
        double[] segIPP = this.findSegIpp(frame, sharedFG);
        if (segIPP == null) {
            return false;
        }
        for (DicomSeries refSeries : refSeriesList) {
            for (DicomImageElement dcm : refSeries.getMedias(null, null)) {
                double[] imagePosition = TagD.getTagValue((TagReadable)dcm, 0x200032, double[].class);
                if (!SegSpecialElement.isWithinTolerance(imagePosition, segIPP, 0.01)) continue;
                return this.registerRefSlicePosition(dcm, loader);
            }
        }
        return false;
    }

    private double[] findSegIpp(Attributes frame, Attributes sharedFG) {
        Attributes sharedPos;
        double[] ipp;
        Attributes planePosition = frame.getNestedDataset(2134291);
        double[] dArray = ipp = planePosition == null ? null : planePosition.getDoubles(0x200032);
        if (ipp == null && sharedFG != null && (sharedPos = sharedFG.getNestedDataset(2134291)) != null) {
            ipp = sharedPos.getDoubles(0x200032);
        }
        if (ipp == null) {
            ipp = TagD.getTagValue((TagReadable)this.mediaIO, 0x200032, double[].class);
        }
        return ipp;
    }

    private boolean registerRefSlicePosition(DicomImageElement dcm, LazyContourLoader loader) {
        Double key;
        Double refSlicePos = (Double)dcm.getTagValue(TagW.SlicePosition);
        Double d2 = key = refSlicePos != null ? refSlicePos : TagD.getTagValue((TagReadable)dcm, 2101313, Double.class);
        if (key != null) {
            this.positionMap.computeIfAbsent(this.quantisePositionKey(key), d -> new LinkedHashSet()).add(loader);
        }
        return true;
    }

    private static boolean isWithinTolerance(double[] a, double[] b, double tolerance) {
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; ++i) {
            if (!(Math.abs(a[i] - b[i]) > tolerance)) continue;
            return false;
        }
        return true;
    }

    private boolean addPositionMap(Attributes frame, Attributes sharedFG, LazyContourLoader loader) {
        double[] pos = this.getDoubleArrayWithFallback(frame, sharedFG, 2134291, 0x200032);
        if (pos == null || pos.length != 3) {
            return false;
        }
        double[] orientation = this.getDoubleArrayWithFallback(frame, sharedFG, 2134294, 2097207);
        Double slicePos = this.computeSlicePosition(pos, orientation);
        if (slicePos == null) {
            return false;
        }
        this.positionMap.computeIfAbsent(this.quantisePositionKey(slicePos), d -> new LinkedHashSet()).add(loader);
        this.captureSegSliceSpacing(sharedFG);
        return true;
    }

    private Double computeSlicePosition(double[] pos, double[] orientation) {
        if (orientation != null && orientation.length == 6) {
            Vector3d pPos = new Vector3d(pos);
            Vector3d vr = new Vector3d(orientation);
            Vector3d vc = new Vector3d(orientation[3], orientation[4], orientation[5]);
            Vector3d normal = VectorUtils.computeNormalOfSurface(vr, vc);
            VectorUtils.orientNormalToDominantPositiveAxis(normal);
            Vector3d ref = this.referenceNormal;
            if (ref == null) {
                this.referenceNormal = new Vector3d((Vector3dc)normal);
            } else if (Math.abs(ref.dot((Vector3dc)normal)) < 0.9999) {
                LOGGER.debug("Skipping SEG frame whose orientation diverges from the reference normal");
                return null;
            }
            return normal.dot((Vector3dc)pPos);
        }
        Attributes dicom = this.dicomObject();
        if (dicom == null) {
            return null;
        }
        return DicomUtils.getDoubleFromDicomElement(dicom, 2101313, null);
    }

    private void captureSegSliceSpacing(Attributes sharedFG) {
        Attributes dicom;
        if (this.segSliceSpacing > 0.0) {
            return;
        }
        double sp = SegSpecialElement.readPixelMeasuresSpacing(sharedFG);
        if (sp <= 0.0 && (dicom = this.dicomObject()) != null && (sp = SegSpecialElement.readPixelMeasuresSpacing(dicom)) <= 0.0) {
            Double v = DicomUtils.getDoubleFromDicomElement(dicom, 0x180088, null);
            if (v == null || v <= 0.0) {
                v = DicomUtils.getDoubleFromDicomElement(dicom, 1572944, null);
            }
            if (v != null && v > 0.0) {
                sp = v;
            }
        }
        if (sp > 0.0) {
            this.segSliceSpacing = sp;
        }
    }

    private static double readPixelMeasuresSpacing(Attributes container) {
        if (container == null) {
            return 0.0;
        }
        Attributes pm = container.getNestedDataset(2658576);
        if (pm == null) {
            return 0.0;
        }
        Double v = DicomUtils.getDoubleFromDicomElement(pm, 0x180088, null);
        if (v == null || v <= 0.0) {
            v = DicomUtils.getDoubleFromDicomElement(pm, 1572944, null);
        }
        return v == null ? 0.0 : v;
    }

    private double quantisePositionKey(double slicePos) {
        double sp = this.segSliceSpacing;
        double q = sp > 0.0 ? sp * 0.25 : 1.0E-4;
        return (double)Math.round(slicePos / q) * q;
    }

    private void refineSegSliceSpacingFromPositionMap() {
        int n = this.positionMap.size();
        if (n < 2) {
            return;
        }
        double[] deltas = new double[n - 1];
        int i = 0;
        double prev = Double.NaN;
        for (Double key : this.positionMap.keySet()) {
            if (i > 0) {
                deltas[i - 1] = Math.abs(key - prev);
            }
            prev = key;
            ++i;
        }
        Arrays.sort(deltas);
        double median = deltas[deltas.length / 2];
        if (median > 0.0 && (this.segSliceSpacing <= 0.0 || Math.abs(median - this.segSliceSpacing) / median > 0.05)) {
            this.segSliceSpacing = median;
        }
    }

    private double[] getDoubleArrayWithFallback(Attributes frame, Attributes sharedFG, int seqTag, int valueTag) {
        double[] val = SegSpecialElement.nestedDoubleArray(frame, seqTag, valueTag);
        if (val != null) {
            return val;
        }
        val = SegSpecialElement.nestedDoubleArray(sharedFG, seqTag, valueTag);
        if (val != null) {
            return val;
        }
        return TagD.getTagValue((TagReadable)this.mediaIO, valueTag, double[].class);
    }

    private static double[] nestedDoubleArray(Attributes attrs, int seqTag, int valueTag) {
        if (attrs == null) {
            return null;
        }
        Attributes nested = attrs.getNestedDataset(seqTag);
        return nested == null ? null : DicomUtils.getDoubleArrayFromDicomElement(nested, valueTag, null);
    }

    private SegMeasurableLayer<DicomImageElement> getMeasurableLayer(FrameRange p) {
        if (p == null || p.first < 0 || p.last < 0) {
            return null;
        }
        DicomImageElement[] frames = this.segFrames();
        DicomImageElement first = SegSpecialElement.frameAt(frames, p.first);
        DicomImageElement last = SegSpecialElement.frameAt(frames, p.last);
        DicomImageElement img = SegSpecialElement.frameAt(frames, p.middle());
        if (img == null || first == null || last == null) {
            return null;
        }
        double thickness = DicomMediaUtils.getThickness(first, last);
        thickness = thickness <= 0.0 ? 1.0 : (thickness /= (double)(p.last - p.first + 1));
        return new SegMeasurableLayer((ImageElement)img, thickness);
    }

    public static enum LoadState {
        PENDING,
        LOADING,
        READY,
        FAILED;

    }

    private static enum SegmentationKind {
        BINARY,
        FRACTIONAL,
        LABELMAP;


        static SegmentationKind fromDicom(String type) {
            if (type == null) {
                return BINARY;
            }
            return switch (type.toUpperCase(Locale.US)) {
                case "FRACTIONAL" -> FRACTIONAL;
                case "LABELMAP" -> LABELMAP;
                default -> BINARY;
            };
        }
    }

    private record FrameRange(int first, int last) {
        static final FrameRange EMPTY = new FrameRange(-1, -1);

        FrameRange withFrame(int idx) {
            return this.first == -1 ? new FrameRange(idx, idx) : new FrameRange(this.first, idx);
        }

        int middle() {
            return (this.first + this.last) / 2;
        }
    }
}

