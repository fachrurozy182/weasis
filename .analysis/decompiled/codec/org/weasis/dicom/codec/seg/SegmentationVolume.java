/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3d
 *  org.joml.Matrix4d
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.joml.Vector3i
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.image.cv.CvUtil
 *  org.weasis.core.api.vol.ChunkedArray
 *  org.weasis.core.api.vol.ChunkedMappedBuffer
 *  org.weasis.core.ui.model.graphic.imp.seg.SegContour
 *  org.weasis.core.ui.model.graphic.imp.seg.SegRegion
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.seg.Region
 *  org.weasis.opencv.seg.RegionAttributes
 */
package org.weasis.dicom.codec.seg;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.image.cv.CvUtil;
import org.weasis.core.api.vol.ChunkedArray;
import org.weasis.core.api.vol.ChunkedMappedBuffer;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.seg.Region;
import org.weasis.opencv.seg.RegionAttributes;

public final class SegmentationVolume {
    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentationVolume.class);
    @Deprecated
    public static final int MAX_SEGMENTS_FOR_BITMASK = Integer.MAX_VALUE;
    private static final int MAX_BYTE_ID = 255;
    private static final int MAX_SHORT_ID = 65535;
    private final Vector3i size;
    private final Vector3d pixelSpacing;
    private final Vector3d volumeOrigin;
    private final Vector3d volumeAxisX;
    private final Vector3d volumeAxisY;
    private final Vector3d volumeAxisZ;
    private final long sliceStride;
    private final Map<Integer, ? extends RegionAttributes> segAttributes;
    private final Map<Integer, List<Integer>> idToSegments = new LinkedHashMap<Integer, List<Integer>>();
    private final Map<List<Integer>, Integer> combinationToId = new HashMap<List<Integer>, Integer>();
    private final int[] segNumToSoloId;
    private final Object idLock = new Object();
    private int nextId;
    private final long[] segStampCount;
    private int[][] unionCache;
    private int occMinX = Integer.MAX_VALUE;
    private int occMinY = Integer.MAX_VALUE;
    private int occMinZ = Integer.MAX_VALUE;
    private int occMaxX = Integer.MIN_VALUE;
    private int occMaxY = Integer.MIN_VALUE;
    private int occMaxZ = Integer.MIN_VALUE;
    private volatile boolean occupancyTracked = true;
    private volatile boolean shortMode;
    private final boolean forceExclusiveMode;
    private ChunkedArray<byte[]> byteData;
    private ChunkedArray<short[]> shortData;
    private ChunkedMappedBuffer mappedBuffer;
    private final AtomicInteger useCount = new AtomicInteger(0);

    public SegmentationVolume(int sizeX, int sizeY, int sizeZ, Vector3d pixelSpacing, Vector3d volumeOrigin, Vector3d volumeAxisX, Vector3d volumeAxisY, Vector3d volumeAxisZ, Map<Integer, ? extends RegionAttributes> segAttributes) {
        this(sizeX, sizeY, sizeZ, pixelSpacing, volumeOrigin, volumeAxisX, volumeAxisY, volumeAxisZ, segAttributes, false);
    }

    public SegmentationVolume(int sizeX, int sizeY, int sizeZ, Vector3d pixelSpacing, Vector3d volumeOrigin, Vector3d volumeAxisX, Vector3d volumeAxisY, Vector3d volumeAxisZ, Map<Integer, ? extends RegionAttributes> segAttributes, boolean forceExclusiveMode) {
        this.size = new Vector3i(sizeX, sizeY, sizeZ);
        this.sliceStride = (long)sizeX * (long)sizeY;
        this.pixelSpacing = new Vector3d((Vector3dc)pixelSpacing);
        this.volumeOrigin = new Vector3d((Vector3dc)volumeOrigin);
        this.volumeAxisX = new Vector3d((Vector3dc)volumeAxisX);
        this.volumeAxisY = new Vector3d((Vector3dc)volumeAxisY);
        this.volumeAxisZ = new Vector3d((Vector3dc)volumeAxisZ);
        this.segAttributes = segAttributes;
        this.forceExclusiveMode = forceExclusiveMode;
        ArrayList<Integer> segNumbers = new ArrayList<Integer>(segAttributes.keySet());
        int maxSegNum = segNumbers.stream().mapToInt(Integer::intValue).max().orElse(0);
        this.segNumToSoloId = new int[maxSegNum + 1];
        this.segStampCount = new long[maxSegNum + 1];
        int id = 1;
        for (Integer segNum : segNumbers) {
            this.segNumToSoloId[segNum.intValue()] = id;
            List<Integer> singleton = List.of(segNum);
            this.idToSegments.put(id, singleton);
            this.combinationToId.put(singleton, id);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Segment #{} '{}' -> soloId={}", new Object[]{segNum, this.labelOf(segNum), id});
            }
            ++id;
        }
        this.nextId = id;
        this.createData();
    }

    private void createData() {
        long totalElements = (long)this.size.x * (long)this.size.y * (long)this.size.z;
        boolean startShort = this.nextId - 1 > 255;
        try {
            this.allocateInMemory(totalElements, startShort);
        }
        catch (OutOfMemoryError e) {
            CvUtil.runGarbageCollectorAndWait((long)100L);
            try {
                this.allocateInMemory(totalElements, startShort);
            }
            catch (OutOfMemoryError ex) {
                this.createDataFile();
            }
        }
    }

    private void allocateInMemory(long totalElements, boolean asShort) {
        if (asShort) {
            this.shortData = ChunkedArray.ofShort((long)totalElements);
            this.shortMode = true;
        } else {
            this.byteData = ChunkedArray.ofByte((long)totalElements);
        }
    }

    private void createDataFile() {
        try {
            this.removeData();
            File dataFile = File.createTempFile("seg_volume_", ".tmp", AppProperties.FILE_CACHE_DIR.toFile());
            this.mappedBuffer = new ChunkedMappedBuffer(dataFile, this.totalVoxels() * 2L);
            this.shortMode = true;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create disk-backed segmentation volume", e);
        }
    }

    private long totalVoxels() {
        return (long)this.size.x * (long)this.size.y * (long)this.size.z;
    }

    private void promoteToShortMode() {
        ChunkedArray newData;
        if (this.shortMode) {
            return;
        }
        long totalElements = this.totalVoxels();
        try {
            newData = ChunkedArray.ofShort((long)totalElements);
        }
        catch (OutOfMemoryError e) {
            CvUtil.runGarbageCollectorAndWait((long)100L);
            try {
                newData = ChunkedArray.ofShort((long)totalElements);
            }
            catch (OutOfMemoryError ex) {
                this.promoteToDiskShortMode();
                return;
            }
        }
        this.copyByteDataInto(SegmentationVolume.allocateShortSink((ChunkedArray<short[]>)newData));
        this.shortData = newData;
        this.shortMode = true;
        LOGGER.info("SegmentationVolume promoted to short storage (>{} distinct IDs)", (Object)255);
    }

    private static ShortSink allocateShortSink(ChunkedArray<short[]> dst) {
        return (i, v) -> {
            ((short[])dst.getChunk((int)dst.chunkIndex((long)i)))[dst.chunkOffset((long)i)] = v;
        };
    }

    private void promoteToDiskShortMode() {
        try {
            File dataFile = File.createTempFile("seg_volume_", ".tmp", AppProperties.FILE_CACHE_DIR.toFile());
            ChunkedMappedBuffer newBuffer = new ChunkedMappedBuffer(dataFile, this.totalVoxels() * 2L);
            this.copyByteDataInto((i, v) -> newBuffer.putShort(i * 2L, v));
            this.mappedBuffer = newBuffer;
            this.shortMode = true;
            LOGGER.info("SegmentationVolume promoted to disk-backed short storage (memory pressure during ID promotion)");
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create disk-backed short segmentation volume", e);
        }
    }

    private void copyByteDataInto(ShortSink sink) {
        if (this.byteData == null) {
            return;
        }
        long totalElements = this.totalVoxels();
        for (long i = 0L; i < totalElements; ++i) {
            byte b = ((byte[])this.byteData.getChunk(this.byteData.chunkIndex(i)))[this.byteData.chunkOffset(i)];
            if (b == 0) continue;
            sink.put(i, (short)(b & 0xFF));
        }
        this.byteData = null;
    }

    private long linearIndex(int x, int y, int z) {
        return (long)z * this.sliceStride + (long)y * (long)this.size.x + (long)x;
    }

    private int readId(long index) {
        if (this.shortMode) {
            if (this.shortData != null) {
                return ((short[])this.shortData.getChunk(this.shortData.chunkIndex(index)))[this.shortData.chunkOffset(index)] & 0xFFFF;
            }
            if (this.mappedBuffer != null) {
                return this.mappedBuffer.getShort(index * 2L) & 0xFFFF;
            }
        } else if (this.byteData != null) {
            return ((byte[])this.byteData.getChunk(this.byteData.chunkIndex(index)))[this.byteData.chunkOffset(index)] & 0xFF;
        }
        return 0;
    }

    private void writeId(long index, int newId) {
        if (this.shortMode) {
            if (this.shortData != null) {
                ((short[])this.shortData.getChunk((int)this.shortData.chunkIndex((long)index)))[this.shortData.chunkOffset((long)index)] = (short)newId;
            } else if (this.mappedBuffer != null) {
                this.mappedBuffer.putShort(index * 2L, (short)newId);
            }
        } else if (this.byteData != null) {
            ((byte[])this.byteData.getChunk((int)this.byteData.chunkIndex((long)index)))[this.byteData.chunkOffset((long)index)] = (byte)newId;
        }
    }

    public void addLabel(int x, int y, int z, int segmentNumber) {
        if (x < 0 || x >= this.size.x || y < 0 || y >= this.size.y || z < 0 || z >= this.size.z) {
            return;
        }
        this.markOccupied(x, y, z);
        this.addLabelAt(this.linearIndex(x, y, z), segmentNumber);
    }

    private void markOccupied(int x, int y, int z) {
        if (x < this.occMinX) {
            this.occMinX = x;
        }
        if (x > this.occMaxX) {
            this.occMaxX = x;
        }
        if (y < this.occMinY) {
            this.occMinY = y;
        }
        if (y > this.occMaxY) {
            this.occMaxY = y;
        }
        if (z < this.occMinZ) {
            this.occMinZ = z;
        }
        if (z > this.occMaxZ) {
            this.occMaxZ = z;
        }
    }

    private void clearOccupancyTracking() {
        this.occupancyTracked = false;
    }

    private void addLabelAt(long index, int segmentNumber) {
        if (segmentNumber < 0 || segmentNumber >= this.segNumToSoloId.length || this.segNumToSoloId[segmentNumber] == 0) {
            return;
        }
        int soloId = this.segNumToSoloId[segmentNumber];
        int n = segmentNumber;
        this.segStampCount[n] = this.segStampCount[n] + 1L;
        if (this.forceExclusiveMode) {
            this.writeId(index, soloId);
            return;
        }
        int currentId = this.readId(index);
        if (currentId == 0 || currentId == soloId) {
            this.writeId(index, soloId);
            return;
        }
        List<Integer> currentSegs = this.idToSegments.get(currentId);
        if (currentSegs != null && currentSegs.contains(segmentNumber)) {
            return;
        }
        int mergedId = this.getOrAllocateCombination(currentSegs, segmentNumber);
        if (mergedId > 0) {
            this.writeId(index, mergedId);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int getOrAllocateCombination(List<Integer> currentSegs, int newSeg) {
        List<Integer> merged = SegmentationVolume.mergeSorted(currentSegs, newSeg);
        Integer existing = this.combinationToId.get(merged);
        if (existing != null) {
            return existing;
        }
        Object object = this.idLock;
        synchronized (object) {
            existing = this.combinationToId.get(merged);
            if (existing != null) {
                return existing;
            }
            int candidate = this.nextId;
            if (!this.shortMode && candidate > 255) {
                this.promoteToShortMode();
            }
            if (candidate > 65535) {
                LOGGER.warn("SegmentationVolume ID space exhausted ({} IDs); voxel skipped for combination {}", (Object)65535, merged);
                return 0;
            }
            this.nextId = candidate + 1;
            this.registerCombinationId(candidate, merged);
            return candidate;
        }
    }

    private void registerCombinationId(int id, List<Integer> sortedSegs) {
        this.idToSegments.put(id, sortedSegs);
        this.combinationToId.put(sortedSegs, id);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int idForUnion(int idA, int idB) {
        if (idA == idB || idB == 0) {
            return idA;
        }
        if (idA == 0) {
            return idB;
        }
        List<Integer> a = this.idToSegments.get(idA);
        List<Integer> b = this.idToSegments.get(idB);
        if (a == null) {
            return idB;
        }
        if (b == null) {
            return idA;
        }
        List<Integer> merged = SegmentationVolume.mergeSortedLists(a, b);
        if (merged.size() == a.size()) {
            return idA;
        }
        if (merged.size() == b.size()) {
            return idB;
        }
        Integer existing = this.combinationToId.get(merged);
        if (existing != null) {
            return existing;
        }
        Object object = this.idLock;
        synchronized (object) {
            existing = this.combinationToId.get(merged);
            if (existing != null) {
                return existing;
            }
            int candidate = this.nextId;
            if (!this.shortMode && candidate > 255) {
                this.promoteToShortMode();
            }
            if (candidate > 65535) {
                return idA;
            }
            this.nextId = candidate + 1;
            this.registerCombinationId(candidate, merged);
            return candidate;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int cachedUnionId(int idA, int idB) {
        int[] row;
        if (idA == idB || idB == 0) {
            return idA;
        }
        if (idA == 0) {
            return idB;
        }
        int[][] cache = this.unionCache;
        if (cache != null && idA < cache.length && (row = cache[idA]) != null && idB < row.length && row[idB] != 0) {
            return row[idB];
        }
        Object object = this.idLock;
        synchronized (object) {
            int[] row2;
            if (this.unionCache == null || idA >= this.unionCache.length) {
                this.unionCache = (int[][])Arrays.copyOf(this.unionCache == null ? new int[][]{} : (int[][])this.unionCache, idA + 1);
            }
            if ((row2 = this.unionCache[idA]) == null || idB >= row2.length) {
                row2 = row2 == null ? new int[idB + 1] : Arrays.copyOf(row2, idB + 1);
                this.unionCache[idA] = row2;
            }
            if (row2[idB] == 0) {
                row2[idB] = this.idForUnion(idA, idB);
            }
            return row2[idB];
        }
    }

    private void warmUnionCache(int[] incomingIds) {
        int allocated = this.nextId;
        boolean[] done = new boolean[allocated];
        for (int incoming : incomingIds) {
            if (incoming <= 0 || incoming >= done.length || done[incoming]) continue;
            done[incoming] = true;
            for (int existing = 1; existing < allocated; ++existing) {
                this.cachedUnionId(existing, incoming);
            }
        }
    }

    private static List<Integer> mergeSorted(List<Integer> base, int value) {
        if (base == null || base.isEmpty()) {
            return List.of(Integer.valueOf(value));
        }
        Integer[] merged = new Integer[base.size() + 1];
        int i = 0;
        boolean inserted = false;
        for (Integer v : base) {
            if (!inserted && value < v) {
                merged[i++] = value;
                inserted = true;
            }
            merged[i++] = v;
        }
        if (!inserted) {
            merged[i] = value;
        }
        return List.of(merged);
    }

    private static List<Integer> mergeSortedLists(List<Integer> a, List<Integer> b) {
        int sa = a.size();
        int sb = b.size();
        Integer[] out = new Integer[sa + sb];
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < sa && j < sb) {
            int vb;
            int va = a.get(i);
            if (va < (vb = b.get(j).intValue())) {
                out[k++] = va;
                ++i;
                continue;
            }
            if (va > vb) {
                out[k++] = vb;
                ++j;
                continue;
            }
            out[k++] = va;
            ++i;
            ++j;
        }
        while (i < sa) {
            out[k++] = a.get(i++);
        }
        while (j < sb) {
            out[k++] = b.get(j++);
        }
        return List.of(Arrays.copyOf(out, k));
    }

    public SegmentationVolume createCompatible(Map<Integer, ? extends RegionAttributes> attributes) {
        return new SegmentationVolume(this.size.x, this.size.y, this.size.z, this.pixelSpacing, this.volumeOrigin, this.volumeAxisX, this.volumeAxisY, this.volumeAxisZ, attributes);
    }

    public long mergeInto(SegmentationVolume target, IntUnaryOperator segmentRemap) {
        if (target == null || segmentRemap == null || target.size.x != this.size.x || target.size.y != this.size.y || target.size.z != this.size.z) {
            return 0L;
        }
        IdMapping mapping = this.mapIdsInto(target, segmentRemap);
        target.warmUnionCache(mapping.targetIds());
        target.clearOccupancyTracking();
        return IntStream.range(0, this.size.z).parallel().mapToLong(z -> {
            long[] stamps = new long[target.segStampCount.length];
            long from = (long)z * this.sliceStride;
            long to = from + this.sliceStride;
            long stamped = 0L;
            for (long i = from; i < to; ++i) {
                int id = this.readId(i);
                if (!mapping.maps(id)) continue;
                mapping.stampInto(target, i, id, stamps);
                ++stamped;
            }
            target.addStampCounts(stamps);
            return stamped;
        }).sum();
    }

    private synchronized void addStampCounts(long[] stamps) {
        for (int segNum = 0; segNum < stamps.length; ++segNum) {
            int n = segNum;
            this.segStampCount[n] = this.segStampCount[n] + stamps[segNum];
        }
    }

    private IdMapping mapIdsInto(SegmentationVolume target, IntUnaryOperator segmentRemap) {
        List<Integer>[] segsById = this.segmentsByIdTable();
        int[] targetIds = new int[segsById.length];
        int[][] targetSegs = new int[segsById.length][];
        for (int id = 1; id < segsById.length; ++id) {
            List segs = segsById[id];
            if (segs != null && segmentRemap != null) {
                segs = segs.stream().map(segmentRemap::applyAsInt).toList();
            }
            targetIds[id] = target.idForSegments(segs);
            targetSegs[id] = target.declaredSegments(segs);
        }
        return new IdMapping(targetIds, targetSegs);
    }

    private int getStorageId(int x, int y, int z) {
        if (x < 0 || x >= this.size.x || y < 0 || y >= this.size.y || z < 0 || z >= this.size.z) {
            return 0;
        }
        return this.readId(this.linearIndex(x, y, z));
    }

    public long resampleInto(SegmentationVolume target, VoxelToLps targetVoxelToLps) {
        Vector3d stepZ;
        Vector3d stepY;
        Vector3d stepX;
        if (target == null || targetVoxelToLps == null) {
            return 0L;
        }
        Vector3d origin = this.toSourceVoxel(targetVoxelToLps, 0.0, 0.0, 0.0);
        VoxelBox box = this.targetBounds(origin, stepX = this.toSourceVoxel(targetVoxelToLps, 1.0, 0.0, 0.0).sub((Vector3dc)origin), stepY = this.toSourceVoxel(targetVoxelToLps, 0.0, 1.0, 0.0).sub((Vector3dc)origin), stepZ = this.toSourceVoxel(targetVoxelToLps, 0.0, 0.0, 1.0).sub((Vector3dc)origin), target.size);
        if (box == null) {
            return 0L;
        }
        IdMapping mapping = this.mapIdsInto(target, null);
        target.warmUnionCache(mapping.targetIds());
        target.clearOccupancyTracking();
        return IntStream.rangeClosed(box.z0(), box.z1()).parallel().mapToLong(z -> {
            long[] stamps = new long[target.segStampCount.length];
            long stamped = 0L;
            for (int y = box.y0(); y <= box.y1(); ++y) {
                double px = origin.x + stepZ.x * (double)z + stepY.x * (double)y + stepX.x * (double)box.x0();
                double py = origin.y + stepZ.y * (double)z + stepY.y * (double)y + stepX.y * (double)box.x0();
                double pz = origin.z + stepZ.z * (double)z + stepY.z * (double)y + stepX.z * (double)box.x0();
                long targetIndex = target.linearIndex(box.x0(), y, z);
                int x = box.x0();
                while (x <= box.x1()) {
                    int id = this.getStorageId((int)Math.round(px), (int)Math.round(py), (int)Math.round(pz));
                    if (mapping.maps(id)) {
                        mapping.stampInto(target, targetIndex, id, stamps);
                        ++stamped;
                    }
                    ++x;
                    ++targetIndex;
                    px += stepX.x;
                    py += stepX.y;
                    pz += stepX.z;
                }
            }
            target.addStampCounts(stamps);
            return stamped;
        }).sum();
    }

    private int idForSegments(List<Integer> segs) {
        int id = 0;
        for (int segNum : this.declaredSegments(segs)) {
            int n = id = id == 0 ? this.segNumToSoloId[segNum] : this.getOrAllocateCombination(this.idToSegments.get(id), segNum);
            if (id != 0) continue;
            return 0;
        }
        return id;
    }

    private int[] declaredSegments(List<Integer> segs) {
        if (segs == null || segs.isEmpty()) {
            return new int[0];
        }
        return segs.stream().mapToInt(Integer::intValue).filter(s -> s >= 0 && s < this.segNumToSoloId.length && this.segNumToSoloId[s] != 0).toArray();
    }

    private Vector3d toSourceVoxel(VoxelToLps targetVoxelToLps, double x, double y, double z) {
        Vector3d lps = targetVoxelToLps.apply(x, y, z, new Vector3d());
        double dx = lps.x - this.volumeOrigin.x;
        double dy = lps.y - this.volumeOrigin.y;
        double dz = lps.z - this.volumeOrigin.z;
        return new Vector3d((dx * this.volumeAxisX.x + dy * this.volumeAxisX.y + dz * this.volumeAxisX.z) / this.pixelSpacing.x, (dx * this.volumeAxisY.x + dy * this.volumeAxisY.y + dz * this.volumeAxisY.z) / this.pixelSpacing.y, (dx * this.volumeAxisZ.x + dy * this.volumeAxisZ.y + dz * this.volumeAxisZ.z) / this.pixelSpacing.z);
    }

    private VoxelBox targetBounds(Vector3d origin, Vector3d stepX, Vector3d stepY, Vector3d stepZ, Vector3i targetSize) {
        VoxelBox full = new VoxelBox(0, targetSize.x - 1, 0, targetSize.y - 1, 0, targetSize.z - 1);
        Matrix3d m = new Matrix3d((Vector3dc)stepX, (Vector3dc)stepY, (Vector3dc)stepZ);
        double det = m.determinant();
        if (!Double.isFinite(det) || det == 0.0) {
            return full;
        }
        Matrix3d inv = m.invert(new Matrix3d());
        double[] bounds = null;
        Vector3d corner = new Vector3d();
        for (int i = 0; i < 8; ++i) {
            corner.set((i & 1) == 0 ? -0.5 : (double)this.size.x - 0.5, (i & 2) == 0 ? -0.5 : (double)this.size.y - 0.5, (i & 4) == 0 ? -0.5 : (double)this.size.z - 0.5);
            inv.transform(corner.sub((Vector3dc)origin));
            bounds = SegmentationVolume.expandBounds(bounds, corner);
        }
        for (void var16_17 : bounds) {
            if (Double.isFinite((double)var16_17)) continue;
            return full;
        }
        int x0 = Math.max(0, (int)Math.ceil((double)bounds[0]));
        int x1 = Math.min(targetSize.x - 1, (int)Math.floor((double)bounds[1]));
        int y0 = Math.max(0, (int)Math.ceil(bounds[2]));
        int n = Math.min(targetSize.y - 1, (int)Math.floor(bounds[3]));
        int z0 = Math.max(0, (int)Math.ceil(bounds[4]));
        int z1 = Math.min(targetSize.z - 1, (int)Math.floor(bounds[5]));
        if (x0 > x1 || y0 > n || z0 > z1) {
            return null;
        }
        return new VoxelBox(x0, x1, y0, n, z0, z1);
    }

    private static double[] expandBounds(double[] bounds, Vector3d p) {
        if (bounds == null) {
            return new double[]{p.x, p.x, p.y, p.y, p.z, p.z};
        }
        bounds[0] = Math.min(bounds[0], p.x);
        bounds[1] = Math.max(bounds[1], p.x);
        bounds[2] = Math.min(bounds[2], p.y);
        bounds[3] = Math.max(bounds[3], p.y);
        bounds[4] = Math.min(bounds[4], p.z);
        bounds[5] = Math.max(bounds[5], p.z);
        return bounds;
    }

    private List<Integer>[] segmentsByIdTable() {
        int max = 0;
        for (Integer id2 : this.idToSegments.keySet()) {
            max = Math.max(max, id2);
        }
        List[] table = new List[max + 1];
        this.idToSegments.forEach((id, segs) -> {
            table[id.intValue()] = segs;
        });
        return table;
    }

    private static int forEachNonZero(Mat mat, NonZeroPixelConsumer consumer) {
        if (mat.channels() != 1) {
            return -1;
        }
        int total = mat.rows() * mat.cols();
        if (total == 0) {
            return 0;
        }
        PixelExtractor extractor = SegmentationVolume.createExtractor(mat, total, mat.depth());
        return extractor == null ? -1 : SegmentationVolume.walkPixels(total, mat.cols(), extractor, consumer);
    }

    private static PixelExtractor createExtractor(Mat mat, int total, int depth) {
        return switch (depth) {
            case 0, 1 -> {
                byte[] buf = new byte[total];
                mat.get(0, 0, buf);
                yield i -> buf[i];
            }
            case 2, 3 -> {
                short[] buf = new short[total];
                mat.get(0, 0, buf);
                if (depth == 2) {
                    yield i -> buf[i] & 0xFFFF;
                }
                yield i -> buf[i];
            }
            case 5 -> {
                float[] buf = new float[total];
                mat.get(0, 0, buf);
                yield i -> buf[i] != 0.0f ? 1 : 0;
            }
            default -> null;
        };
    }

    private static int walkPixels(int total, int cols, PixelExtractor extractor, NonZeroPixelConsumer consumer) {
        int count = 0;
        int x = 0;
        int y = 0;
        for (int i = 0; i < total; ++i) {
            if (extractor.valueAt(i) != 0) {
                consumer.accept(i, x, y);
                ++count;
            }
            if (++x != cols) continue;
            x = 0;
            ++y;
        }
        return count;
    }

    private static int forEachNonZeroGeneric(Mat mat, NonZeroPixelConsumer consumer) {
        int rows = mat.rows();
        int cols = mat.cols();
        int count = 0;
        for (int y = 0; y < rows; ++y) {
            for (int x = 0; x < cols; ++x) {
                double[] pixel = mat.get(y, x);
                if (pixel == null || pixel.length <= 0 || pixel[0] == 0.0) continue;
                consumer.accept(y * cols + x, x, y);
                ++count;
            }
        }
        return count;
    }

    public void stampAxialMask(PlanarImage mask, int sliceZ, int segmentNumber) {
        long base;
        NonZeroPixelConsumer stamp;
        if (mask == null || sliceZ < 0 || sliceZ >= this.size.z) {
            return;
        }
        int cols = mask.width();
        int rows = mask.height();
        if (cols != this.size.x || rows != this.size.y) {
            LOGGER.warn("Mask dimensions {}x{} do not match volume X/Y {}x{} at slice {} \u2014 frame skipped. Decoded SEG mask should have been re-aligned to declared (Columns, Rows) upstream (see SegMaskOrientation.normalize); reaching this branch indicates a regression in the SEG builder.", new Object[]{cols, rows, this.size.x, this.size.y, sliceZ});
            return;
        }
        Mat mat = mask.toMat();
        int stamped = SegmentationVolume.forEachNonZero(mat, stamp = (arg_0, arg_1, arg_2) -> this.lambda$stampAxialMask$0(sliceZ, base = (long)sliceZ * this.sliceStride, segmentNumber, arg_0, arg_1, arg_2));
        if (stamped < 0) {
            stamped = SegmentationVolume.forEachNonZeroGeneric(mat, stamp);
        }
        if (LOGGER.isDebugEnabled()) {
            int total = rows * cols;
            LOGGER.debug("Stamped frame seg={} sliceZ={} type=ch{}depth{} non-zero={}/{} ({}%)", new Object[]{segmentNumber, sliceZ, mat.channels(), mat.depth(), stamped, total, total == 0 ? 0L : (long)stamped * 100L / (long)total});
        }
    }

    public void stampMaskWithTransform(PlanarImage mask, Vector3d maskOrigin, Vector3d maskRowDir, Vector3d maskColDir, double[] maskPixelSpacing, int segmentNumber) {
        double invSz;
        double invSy;
        double invSx;
        double stepYz;
        double stepXz;
        double stepYy;
        double stepXy;
        double rowSpacing;
        double stepYx;
        double colSpacing;
        double stepXx;
        NonZeroPixelConsumer stamp;
        if (mask == null) {
            return;
        }
        Mat mat = mask.toMat();
        int stamped = SegmentationVolume.forEachNonZero(mat, stamp = (arg_0, arg_1, arg_2) -> this.lambda$stampMaskWithTransform$0(maskOrigin, stepXx = maskRowDir.x * (colSpacing = maskPixelSpacing[1]), stepYx = maskColDir.x * (rowSpacing = maskPixelSpacing[0]), stepXy = maskRowDir.y * colSpacing, stepYy = maskColDir.y * rowSpacing, stepXz = maskRowDir.z * colSpacing, stepYz = maskColDir.z * rowSpacing, invSx = 1.0 / this.pixelSpacing.x, invSy = 1.0 / this.pixelSpacing.y, invSz = 1.0 / this.pixelSpacing.z, segmentNumber, arg_0, arg_1, arg_2));
        if (stamped < 0) {
            SegmentationVolume.forEachNonZeroGeneric(mat, stamp);
        }
    }

    public Map<Integer, List<SegContour>> getContoursForImagePlane(Vector3d imgOrigin, Vector3d rowDir, Vector3d colDir, double colSpacing, double rowSpacing, int width, int height) {
        if (imgOrigin == null || rowDir == null || colDir == null || width <= 0 || height <= 0) {
            return Collections.emptyMap();
        }
        double minSegSpacing = Math.min(this.pixelSpacing.x, Math.min(this.pixelSpacing.y, this.pixelSpacing.z));
        int factorU = Math.max(1, (int)Math.ceil(colSpacing / minSegSpacing));
        int factorV = Math.max(1, (int)Math.ceil(rowSpacing / minSegSpacing));
        int factor = Math.clamp((long)Math.max(factorU, factorV), (int)1, (int)4);
        PlaneBox box = this.planeBounds(imgOrigin, rowDir, colDir, colSpacing, rowSpacing, width, height);
        if (box == null) {
            return Collections.emptyMap();
        }
        int[] raster = this.sampleImagePlane(imgOrigin, rowDir, colDir, colSpacing, rowSpacing, width, height, factor, box);
        return this.buildContours(raster, width, height);
    }

    private PlaneBox planeBounds(Vector3d imgOrigin, Vector3d rowDir, Vector3d colDir, double colSpacing, double rowSpacing, int width, int height) {
        boolean occupied;
        Vector3d o = this.lpsToVoxel(imgOrigin, new Vector3d());
        Vector3d stepU = this.lpsToVoxel(new Vector3d((Vector3dc)rowDir).mul(colSpacing).add((Vector3dc)imgOrigin), new Vector3d()).sub((Vector3dc)o);
        Vector3d stepV = this.lpsToVoxel(new Vector3d((Vector3dc)colDir).mul(rowSpacing).add((Vector3dc)imgOrigin), new Vector3d()).sub((Vector3dc)o);
        double[] poly = new double[]{-0.5, -0.5, (double)width - 0.5, -0.5, (double)width - 0.5, (double)height - 0.5, -0.5, (double)height - 0.5};
        boolean bl = occupied = this.occupancyTracked && this.occMinX <= this.occMaxX;
        if (this.occupancyTracked && !occupied) {
            return null;
        }
        int count = 4;
        for (int axis = 0; axis < 3 && count > 0; ++axis) {
            double a = SegmentationVolume.component(stepU, axis);
            double b = SegmentationVolume.component(stepV, axis);
            double c = SegmentationVolume.component(o, axis);
            double lo = occupied ? (double)this.occMin(axis) : 0.0;
            double hi = occupied ? (double)this.occMax(axis) : (double)(this.extent(axis) - 1);
            double[] clipped = SegmentationVolume.clipHalfPlane(poly, count, a, b, c - lo + 0.5);
            count = clipped.length / 2;
            clipped = SegmentationVolume.clipHalfPlane(clipped, count, -a, -b, hi + 0.5 - c);
            count = clipped.length / 2;
            poly = clipped;
        }
        if (count == 0) {
            return null;
        }
        double minU = Double.POSITIVE_INFINITY;
        double maxU = Double.NEGATIVE_INFINITY;
        double minV = Double.POSITIVE_INFINITY;
        double maxV = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < count; ++i) {
            double u = poly[2 * i];
            double v = poly[2 * i + 1];
            if (!Double.isFinite(u) || !Double.isFinite(v)) {
                return new PlaneBox(0, width - 1, 0, height - 1);
            }
            minU = Math.min(minU, u);
            maxU = Math.max(maxU, u);
            minV = Math.min(minV, v);
            maxV = Math.max(maxV, v);
        }
        int u0 = Math.max(0, (int)Math.floor(minU));
        int u1 = Math.min(width - 1, (int)Math.ceil(maxU));
        int v0 = Math.max(0, (int)Math.floor(minV));
        int v1 = Math.min(height - 1, (int)Math.ceil(maxV));
        return u0 > u1 || v0 > v1 ? null : new PlaneBox(u0, u1, v0, v1);
    }

    private static double component(Vector3d v, int axis) {
        return axis == 0 ? v.x : (axis == 1 ? v.y : v.z);
    }

    private int extent(int axis) {
        return axis == 0 ? this.size.x : (axis == 1 ? this.size.y : this.size.z);
    }

    private int occMin(int axis) {
        return axis == 0 ? this.occMinX : (axis == 1 ? this.occMinY : this.occMinZ);
    }

    private int occMax(int axis) {
        return axis == 0 ? this.occMaxX : (axis == 1 ? this.occMaxY : this.occMaxZ);
    }

    private static double[] clipHalfPlane(double[] poly, int count, double a, double b, double c) {
        if (count == 0) {
            return new double[0];
        }
        double[] out = new double[(count + 2) * 2];
        int n = 0;
        for (int i = 0; i < count; ++i) {
            int j = (i + 1) % count;
            double ui = poly[2 * i];
            double vi = poly[2 * i + 1];
            double uj = poly[2 * j];
            double vj = poly[2 * j + 1];
            double di = a * ui + b * vi + c;
            double dj = a * uj + b * vj + c;
            if (di >= 0.0) {
                out[n++] = ui;
                out[n++] = vi;
            }
            if (di >= 0.0 == dj >= 0.0) continue;
            double t = di / (di - dj);
            out[n++] = ui + t * (uj - ui);
            out[n++] = vi + t * (vj - vi);
        }
        return Arrays.copyOf(out, n);
    }

    private Vector3d lpsToVoxel(Vector3d lps, Vector3d dst) {
        double dx = lps.x - this.volumeOrigin.x;
        double dy = lps.y - this.volumeOrigin.y;
        double dz = lps.z - this.volumeOrigin.z;
        return dst.set((dx * this.volumeAxisX.x + dy * this.volumeAxisX.y + dz * this.volumeAxisX.z) / this.pixelSpacing.x, (dx * this.volumeAxisY.x + dy * this.volumeAxisY.y + dz * this.volumeAxisY.z) / this.pixelSpacing.y, (dx * this.volumeAxisZ.x + dy * this.volumeAxisZ.y + dz * this.volumeAxisZ.z) / this.pixelSpacing.z);
    }

    private int[] sampleImagePlane(Vector3d imgOrigin, Vector3d rowDir, Vector3d colDir, double colSpacing, double rowSpacing, int width, int height, int oversample, PlaneBox box) {
        int[] raster = new int[width * height];
        double invSx = 1.0 / this.pixelSpacing.x;
        double invSy = 1.0 / this.pixelSpacing.y;
        double invSz = 1.0 / this.pixelSpacing.z;
        double colStepX = rowDir.x * colSpacing;
        double colStepY = rowDir.y * colSpacing;
        double colStepZ = rowDir.z * colSpacing;
        double rowStepX = colDir.x * rowSpacing;
        double rowStepY = colDir.y * rowSpacing;
        double rowStepZ = colDir.z * rowSpacing;
        int n = Math.max(1, oversample);
        double[] subOffsets = new double[n];
        for (int i = 0; i < n; ++i) {
            subOffsets[i] = ((double)i + 0.5) / (double)n - 0.5;
        }
        for (int v = box.v0(); v <= box.v1(); ++v) {
            int rowBase = v * width;
            for (int u = box.u0(); u <= box.u1(); ++u) {
                int combined = 0;
                for (int sv = 0; sv < n; ++sv) {
                    double vv = (double)v + subOffsets[sv];
                    double oxV = imgOrigin.x + vv * rowStepX;
                    double oyV = imgOrigin.y + vv * rowStepY;
                    double ozV = imgOrigin.z + vv * rowStepZ;
                    for (int su = 0; su < n; ++su) {
                        int vz;
                        int vy;
                        double uu = (double)u + subOffsets[su];
                        double dx = oxV + uu * colStepX - this.volumeOrigin.x;
                        double dy = oyV + uu * colStepY - this.volumeOrigin.y;
                        double dz = ozV + uu * colStepZ - this.volumeOrigin.z;
                        int vx = (int)Math.round((dx * this.volumeAxisX.x + dy * this.volumeAxisX.y + dz * this.volumeAxisX.z) * invSx);
                        int id = this.getStorageId(vx, vy = (int)Math.round((dx * this.volumeAxisY.x + dy * this.volumeAxisY.y + dz * this.volumeAxisY.z) * invSy), vz = (int)Math.round((dx * this.volumeAxisZ.x + dy * this.volumeAxisZ.y + dz * this.volumeAxisZ.z) * invSz));
                        if (id == 0) continue;
                        combined = this.idForUnion(combined, id);
                    }
                }
                if (combined == 0) continue;
                raster[rowBase + u] = combined;
            }
        }
        return raster;
    }

    public Map<Integer, List<SegContour>> getSliceContours(Matrix4d combinedTransform, int outputSize, Vector3d voxelRatio) {
        if (combinedTransform == null || outputSize <= 0) {
            return Collections.emptyMap();
        }
        int totalPixels = outputSize * outputSize;
        int[] raster = new int[totalPixels];
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(new BitmaskSliceTask(0, totalPixels, outputSize, combinedTransform, voxelRatio, raster, this));
        return this.buildContours(raster, outputSize, outputSize);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Map<Integer, List<SegContour>> buildContours(int[] raster, int width, int height) {
        if (raster == null || raster.length == 0) {
            return Collections.emptyMap();
        }
        boolean[] visibleSeg = new boolean[this.segNumToSoloId.length];
        RegionAttributes[] attrsBySeg = new RegionAttributes[this.segNumToSoloId.length];
        boolean anyVisible = false;
        for (int s = 0; s < visibleSeg.length; ++s) {
            RegionAttributes a;
            if (this.segNumToSoloId[s] == 0) continue;
            attrsBySeg[s] = a = this.segAttributes.get(s);
            if (a == null || !a.isVisible()) continue;
            visibleSeg[s] = true;
            anyVisible = true;
        }
        if (!anyVisible) {
            return Collections.emptyMap();
        }
        LinkedHashMap<Integer, byte[]> segMask = new LinkedHashMap<Integer, byte[]>();
        LinkedHashMap<Integer, int[]> segPixelCount = new LinkedHashMap<Integer, int[]>();
        int totalNonZero = 0;
        for (int i = 0; i < raster.length; ++i) {
            List<Integer> segs;
            int id = raster[i];
            if (id == 0 || (segs = this.idToSegments.get(id)) == null) continue;
            ++totalNonZero;
            for (int segNum : segs) {
                if (segNum < 0 || segNum >= visibleSeg.length || !visibleSeg[segNum]) continue;
                byte[] mask = (byte[])segMask.get(segNum);
                if (mask == null) {
                    mask = new byte[raster.length];
                    segMask.put(segNum, mask);
                    segPixelCount.put(segNum, new int[1]);
                }
                if (mask[i] != 0) continue;
                mask[i] = -1;
                int[] nArray = (int[])segPixelCount.get(segNum);
                nArray[0] = nArray[0] + 1;
            }
        }
        if (segMask.isEmpty()) {
            this.logBuildContoursDiagnostics();
            return Collections.emptyMap();
        }
        LinkedHashMap<Integer, List<SegContour>> result = new LinkedHashMap<Integer, List<SegContour>>();
        for (Map.Entry entry : segMask.entrySet()) {
            int segNum = (Integer)entry.getKey();
            byte[] maskData = (byte[])entry.getValue();
            int pixelCount = ((int[])segPixelCount.get(segNum))[0];
            RegionAttributes attrs = attrsBySeg[segNum];
            ImageCV binaryMask = new ImageCV(height, width, CvType.CV_8UC1);
            try {
                binaryMask.toMat().put(0, 0, maskData);
                List segmentList = Region.buildSegmentList((PlanarImage)binaryMask);
                if (!segmentList.isEmpty()) {
                    SegContour contour = new SegContour(String.valueOf(segNum), segmentList, pixelCount);
                    contour.setAttributes(attrs);
                    result.computeIfAbsent(segNum, n -> new ArrayList()).add(contour);
                    continue;
                }
                if (!LOGGER.isDebugEnabled()) continue;
                LOGGER.debug("buildContours: segment #{} '{}' had {} pixels in the resliced raster but Region.buildSegmentList returned no contour", new Object[]{segNum, this.labelOf(segNum), pixelCount});
            }
            finally {
                binaryMask.release();
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("buildContours: {} non-zero raster pixels, {} visible segment(s) emitted out of {} stamped", new Object[]{totalNonZero, result.size(), this.countStampedSegments()});
        }
        this.logBuildContoursDiagnostics();
        return result;
    }

    private int countStampedSegments() {
        int n = 0;
        for (long c : this.segStampCount) {
            if (c <= 0L) continue;
            ++n;
        }
        return n;
    }

    private void logBuildContoursDiagnostics() {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        for (int segNum = 0; segNum < this.segStampCount.length; ++segNum) {
            long stamped = this.segStampCount[segNum];
            if (stamped <= 0L || this.segNumToSoloId[segNum] != 0) continue;
            LOGGER.debug("buildContours: segment #{} '{}' has {} stamped voxels but no soloId allocated", new Object[]{segNum, this.labelOf(segNum), stamped});
        }
    }

    private String labelOf(int segNum) {
        RegionAttributes attrs = this.segAttributes == null ? null : this.segAttributes.get(segNum);
        return attrs == null ? "?" : attrs.getLabel();
    }

    public int getSizeX() {
        return this.size.x;
    }

    public int getSizeY() {
        return this.size.y;
    }

    public int getSizeZ() {
        return this.size.z;
    }

    public boolean isShortMode() {
        return this.shortMode;
    }

    public boolean isEmpty() {
        long total = this.totalVoxels();
        for (long i = 0L; i < total; ++i) {
            if (this.readId(i) == 0) continue;
            return false;
        }
        return true;
    }

    public int getSegmentNumber(RegionAttributes attributes) {
        if (attributes == null || this.segAttributes == null) {
            return -1;
        }
        int equalKey = -1;
        for (Map.Entry<Integer, ? extends RegionAttributes> entry : this.segAttributes.entrySet()) {
            if (entry.getValue() == attributes) {
                return entry.getKey();
            }
            if (equalKey >= 0 || !attributes.equals((Object)entry.getValue())) continue;
            equalKey = entry.getKey();
        }
        return equalKey;
    }

    public Vector3i findSegmentCenter(int segmentNumber, BiConsumer<Integer, Integer> progress) {
        boolean[] matchingIds = this.buildMatchingIds(segmentNumber);
        if (matchingIds == null || this.isDisposed()) {
            return null;
        }
        AtomicInteger scanned = new AtomicInteger();
        SliceStat best = IntStream.range(0, this.size.z).parallel().mapToObj(z -> {
            SliceStat stat = this.scanSlice(z, matchingIds);
            if (progress != null) {
                progress.accept(scanned.incrementAndGet(), this.size.z);
            }
            return stat;
        }).filter(s -> s.count() > 0L).max(Comparator.comparingLong(SliceStat::count)).orElse(null);
        if (best == null) {
            return null;
        }
        return new Vector3i((int)Math.round(best.sumX() / (double)best.count()), (int)Math.round(best.sumY() / (double)best.count()), best.z());
    }

    private SliceStat scanSlice(int z, boolean[] matchingIds) {
        long count = 0L;
        double sumX = 0.0;
        double sumY = 0.0;
        long base = (long)z * this.sliceStride;
        for (int y = 0; y < this.size.y; ++y) {
            long row = base + (long)y * (long)this.size.x;
            for (int x = 0; x < this.size.x; ++x) {
                int id = this.readId(row + (long)x);
                if (id <= 0 || id >= matchingIds.length || !matchingIds[id]) continue;
                ++count;
                sumX += (double)x;
                sumY += (double)y;
            }
        }
        return new SliceStat(z, count, sumX, sumY);
    }

    public Vector3d voxelToLps(int x, int y, int z) {
        return new Vector3d((Vector3dc)this.volumeOrigin).fma((double)x * this.pixelSpacing.x, (Vector3dc)this.volumeAxisX).fma((double)y * this.pixelSpacing.y, (Vector3dc)this.volumeAxisY).fma((double)z * this.pixelSpacing.z, (Vector3dc)this.volumeAxisZ);
    }

    private boolean[] buildMatchingIds(int segmentNumber) {
        boolean[] matchingIds = new boolean[this.nextId];
        boolean found = false;
        for (Map.Entry<Integer, List<Integer>> entry : this.idToSegments.entrySet()) {
            int id = entry.getKey();
            if (id <= 0 || id >= matchingIds.length || !entry.getValue().contains(segmentNumber)) continue;
            matchingIds[id] = true;
            found = true;
        }
        return (boolean[])(found ? matchingIds : null);
    }

    public SegmentationVolume retain() {
        this.useCount.incrementAndGet();
        return this;
    }

    public void release() {
        int n = this.useCount.decrementAndGet();
        if (n <= 0) {
            if (n < 0) {
                LOGGER.warn("SegmentationVolume.release() called more times than retain() (count={}) \u2014 resetting to 0", (Object)n);
                this.useCount.set(0);
            }
            this.removeData();
        }
    }

    public int getUseCount() {
        return this.useCount.get();
    }

    public void removeData() {
        this.byteData = null;
        this.shortData = null;
        if (this.mappedBuffer != null) {
            this.mappedBuffer.close();
            this.mappedBuffer = null;
        }
    }

    public long heapBytes() {
        if (this.byteData != null) {
            return this.totalVoxels();
        }
        return this.shortData != null ? this.totalVoxels() * 2L : 0L;
    }

    public boolean isDisposed() {
        return this.byteData == null && this.shortData == null && this.mappedBuffer == null;
    }

    public void applySegmentVoxelCounts() {
        if (this.segAttributes == null || this.segAttributes.isEmpty() || this.isDisposed()) {
            return;
        }
        long[] idCounts = new long[this.nextId];
        long total = this.totalVoxels();
        for (long i = 0L; i < total; ++i) {
            int id = this.readId(i);
            if (id <= 0 || id >= idCounts.length) continue;
            int n = id;
            idCounts[n] = idCounts[n] + 1L;
        }
        HashMap<Integer, Long> segCounts = new HashMap<Integer, Long>();
        for (int id = 1; id < idCounts.length; ++id) {
            List<Integer> segs;
            long c = idCounts[id];
            if (c == 0L || (segs = this.idToSegments.get(id)) == null) continue;
            for (int segNum : segs) {
                segCounts.merge(segNum, c, Long::sum);
            }
        }
        for (Map.Entry<Integer, ? extends RegionAttributes> e : this.segAttributes.entrySet()) {
            RegionAttributes regionAttributes = e.getValue();
            if (!(regionAttributes instanceof SegRegion)) continue;
            SegRegion region = (SegRegion)regionAttributes;
            region.setNumberOfPixels(segCounts.getOrDefault(e.getKey(), 0L).longValue());
        }
    }

    public int getSegmentCount() {
        return this.segAttributes == null ? 0 : this.segAttributes.size();
    }

    public int[] exportSliceBitmask(int z) {
        if (z < 0 || z >= this.size.z) {
            return null;
        }
        int sliceSize = this.size.x * this.size.y;
        int[] raster = new int[sliceSize];
        long base = (long)z * this.sliceStride;
        for (int i = 0; i < sliceSize; ++i) {
            int id = this.readId(base + (long)i);
            if (id == 0) continue;
            raster[i] = id;
        }
        return raster;
    }

    public boolean exportSliceInto(int z, ByteBuffer dst) {
        int byteLength;
        if (z < 0 || z >= this.size.z || dst == null) {
            return false;
        }
        int sliceSize = this.size.x * this.size.y;
        int n = byteLength = this.shortMode ? sliceSize * 2 : sliceSize;
        if (dst.remaining() < byteLength) {
            return false;
        }
        long base = (long)z * this.sliceStride;
        if (this.shortMode && this.shortData != null) {
            short[] staging = new short[sliceSize];
            this.shortData.copyTo(base, (Object)staging, 0, (long)sliceSize);
            dst.slice().order(dst.order()).asShortBuffer().put(staging);
            dst.position(dst.position() + byteLength);
        } else if (!this.shortMode && this.byteData != null) {
            byte[] staging = new byte[sliceSize];
            this.byteData.copyTo(base, (Object)staging, 0, (long)sliceSize);
            dst.put(staging);
        } else {
            for (int i = 0; i < sliceSize; ++i) {
                int id = this.readId(base + (long)i);
                if (this.shortMode) {
                    dst.putShort((short)id);
                    continue;
                }
                dst.put((byte)id);
            }
        }
        return true;
    }

    public byte[] buildSegmentColorLUT() {
        return this.buildSegmentColorLUT(null);
    }

    public byte[] buildSegmentColorLUT(Map<Integer, ? extends RegionAttributes> overrideAttrs) {
        int idCount = this.nextId;
        byte[] lut = new byte[idCount * 4];
        for (int id = 1; id < idCount; ++id) {
            List<Integer> segs = this.idToSegments.get(id);
            if (segs == null) continue;
            float r = 0.0f;
            float g = 0.0f;
            float b = 0.0f;
            float a = 0.0f;
            for (int segNum : segs) {
                RegionAttributes attrs;
                RegionAttributes regionAttributes = attrs = overrideAttrs != null ? overrideAttrs.get(segNum) : null;
                if (attrs == null) {
                    attrs = this.segAttributes.get(segNum);
                }
                if (attrs == null || !attrs.isVisible()) continue;
                Color c = attrs.getColor();
                float segA = attrs.getInteriorOpacity();
                float effA = (1.0f - a) * segA;
                r += effA * ((float)c.getRed() / 255.0f);
                g += effA * ((float)c.getGreen() / 255.0f);
                b += effA * ((float)c.getBlue() / 255.0f);
                if (!((a += effA) >= 0.99f)) continue;
                break;
            }
            if (MathUtil.isEqualToZero((float)a, (float)0.001f)) continue;
            lut[id * 4] = (byte)Math.round(r / a * 255.0f);
            lut[id * 4 + 1] = (byte)Math.round(g / a * 255.0f);
            lut[id * 4 + 2] = (byte)Math.round(b / a * 255.0f);
            lut[id * 4 + 3] = (byte)Math.round(a * 255.0f);
        }
        return lut;
    }

    private /* synthetic */ void lambda$stampMaskWithTransform$0(Vector3d maskOrigin, double stepXx, double stepYx, double stepXy, double stepYy, double stepXz, double stepYz, double invSx, double invSy, double invSz, int segmentNumber, int i, int mx, int my) {
        double dx = maskOrigin.x + (double)mx * stepXx + (double)my * stepYx - this.volumeOrigin.x;
        double dy = maskOrigin.y + (double)mx * stepXy + (double)my * stepYy - this.volumeOrigin.y;
        double dz = maskOrigin.z + (double)mx * stepXz + (double)my * stepYz - this.volumeOrigin.z;
        int ix = (int)Math.round((dx * this.volumeAxisX.x + dy * this.volumeAxisX.y + dz * this.volumeAxisX.z) * invSx);
        int iy = (int)Math.round((dx * this.volumeAxisY.x + dy * this.volumeAxisY.y + dz * this.volumeAxisY.z) * invSy);
        int iz = (int)Math.round((dx * this.volumeAxisZ.x + dy * this.volumeAxisZ.y + dz * this.volumeAxisZ.z) * invSz);
        this.addLabel(ix, iy, iz, segmentNumber);
    }

    private /* synthetic */ void lambda$stampAxialMask$0(int sliceZ, long base, int segmentNumber, int i, int x, int y) {
        this.markOccupied(x, y, sliceZ);
        this.addLabelAt(base + (long)i, segmentNumber);
    }

    @FunctionalInterface
    private static interface ShortSink {
        public void put(long var1, short var3);
    }

    private record IdMapping(int[] targetIds, int[][] targetSegs) {
        boolean maps(int id) {
            return id > 0 && id < this.targetIds.length && this.targetIds[id] != 0;
        }

        void stampInto(SegmentationVolume target, long targetIndex, int id, long[] stamps) {
            int merged;
            int current = target.readId(targetIndex);
            int n = merged = current == 0 ? this.targetIds[id] : target.cachedUnionId(current, this.targetIds[id]);
            if (merged != current) {
                target.writeId(targetIndex, merged);
            }
            int[] nArray = this.targetSegs[id];
            int n2 = nArray.length;
            for (int i = 0; i < n2; ++i) {
                int segNum;
                int n3 = segNum = nArray[i];
                stamps[n3] = stamps[n3] + 1L;
            }
        }
    }

    @FunctionalInterface
    public static interface VoxelToLps {
        public Vector3d apply(double var1, double var3, double var5, Vector3d var7);
    }

    private record VoxelBox(int x0, int x1, int y0, int y1, int z0, int z1) {
    }

    @FunctionalInterface
    private static interface PixelExtractor {
        public int valueAt(int var1);
    }

    @FunctionalInterface
    private static interface NonZeroPixelConsumer {
        public void accept(int var1, int var2, int var3);
    }

    private record PlaneBox(int u0, int u1, int v0, int v1) {
    }

    private static final class BitmaskSliceTask
    extends RecursiveAction {
        private static final int THRESHOLD = 4096;
        private final int start;
        private final int end;
        private final int width;
        private final Matrix4d combinedTransform;
        private final Vector3d voxelRatio;
        private final int[] raster;
        private final SegmentationVolume volume;

        BitmaskSliceTask(int start, int end, int width, Matrix4d combinedTransform, Vector3d voxelRatio, int[] raster, SegmentationVolume volume) {
            this.start = start;
            this.end = end;
            this.width = width;
            this.combinedTransform = combinedTransform;
            this.voxelRatio = voxelRatio;
            this.raster = raster;
            this.volume = volume;
        }

        @Override
        protected void compute() {
            if (this.end - this.start <= 4096) {
                Vector3d coord = new Vector3d();
                int x = this.start % this.width;
                int y = this.start / this.width;
                for (int i = this.start; i < this.end; ++i) {
                    coord.set((double)x, (double)y, 0.0);
                    this.combinedTransform.transformPosition(coord);
                    int vx = (int)Math.round(coord.x / this.voxelRatio.x);
                    int vy = (int)Math.round(coord.y / this.voxelRatio.y);
                    int vz = (int)Math.round(coord.z / this.voxelRatio.z);
                    int id = this.volume.getStorageId(vx, vy, vz);
                    if (id != 0) {
                        this.raster[i] = id;
                    }
                    if (++x < this.width) continue;
                    x = 0;
                    ++y;
                }
            } else {
                int mid = (this.start + this.end) / 2;
                BitmaskSliceTask.invokeAll(new BitmaskSliceTask(this.start, mid, this.width, this.combinedTransform, this.voxelRatio, this.raster, this.volume), new BitmaskSliceTask(mid, this.end, this.width, this.combinedTransform, this.voxelRatio, this.raster, this.volume));
            }
        }
    }

    private record SliceStat(int z, long count, double sumX, double sumY) {
    }
}

