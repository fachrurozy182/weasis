/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.codec.seg;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ForkJoinPool;
import org.weasis.dicom.codec.seg.SegSpecialElement;
import org.weasis.dicom.codec.seg.SegmentationVolume;

@FunctionalInterface
public interface SegmentationVolumeBuildExecutor {
    public static final SegmentationVolumeBuildExecutor DEFAULT = (seg, work) -> CompletableFuture.supplyAsync(() -> {
        try {
            return (SegmentationVolume)work.call();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new CompletionException(e);
        }
    }, ForkJoinPool.commonPool());

    public CompletableFuture<SegmentationVolume> schedule(SegSpecialElement var1, Callable<SegmentationVolume> var2);
}

