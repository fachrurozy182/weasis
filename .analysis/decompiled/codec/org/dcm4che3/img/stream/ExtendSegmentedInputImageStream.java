/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.img.stream;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import org.dcm4che3.img.stream.ImageDescriptor;

public record ExtendSegmentedInputImageStream(Path path, long[] segmentPositions, int[] segmentLengths, ImageDescriptor imageDescriptor) {
    private final long[] segmentPositions;
    private final int[] segmentLengths;

    public ExtendSegmentedInputImageStream {
        Objects.requireNonNull(path, "Path cannot be null");
        Objects.requireNonNull(segmentPositions, "Segment positions cannot be null");
        Objects.requireNonNull(segmentLengths, "Segment lengths cannot be null");
        Objects.requireNonNull(imageDescriptor, "Image descriptor cannot be null");
        ExtendSegmentedInputImageStream.validateArraysConsistency(segmentPositions, segmentLengths);
        ExtendSegmentedInputImageStream.validateSegmentValues(segmentPositions, segmentLengths);
    }

    public int getSegmentCount() {
        return this.segmentPositions.length;
    }

    public long getTotalLength() {
        return Arrays.stream(this.segmentLengths).asLongStream().sum();
    }

    public long[] segmentPositions() {
        return (long[])this.segmentPositions.clone();
    }

    public int[] segmentLengths() {
        return (int[])this.segmentLengths.clone();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ExtendSegmentedInputImageStream)) return false;
        ExtendSegmentedInputImageStream other = (ExtendSegmentedInputImageStream)((Object)obj);
        if (!Objects.equals(this.path, other.path)) return false;
        if (!Arrays.equals(this.segmentPositions, other.segmentPositions)) return false;
        if (!Arrays.equals(this.segmentLengths, other.segmentLengths)) return false;
        if (!Objects.equals(this.imageDescriptor, other.imageDescriptor)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.path, this.imageDescriptor, Arrays.hashCode(this.segmentPositions), Arrays.hashCode(this.segmentLengths));
    }

    public String toString() {
        return "ExtendSegmentedInputImageStream{path=%s, segments=%d, totalLength=%d}".formatted(new Object[]{this.path, this.getSegmentCount(), this.getTotalLength()});
    }

    private static void validateArraysConsistency(long[] segmentPositions, int[] segmentLengths) {
        if (segmentPositions.length != segmentLengths.length) {
            throw new IllegalArgumentException("Segment positions and lengths arrays must have the same length");
        }
        if (segmentPositions.length == 0) {
            throw new IllegalArgumentException("At least one segment must be defined");
        }
    }

    private static void validateSegmentValues(long[] segmentPositions, int[] segmentLengths) {
        IntStream.range(0, segmentPositions.length).forEach(i -> {
            if (segmentPositions[i] < 0L) {
                throw new IllegalArgumentException("Segment position cannot be negative: " + segmentPositions[i]);
            }
            if (segmentLengths[i] <= 0) {
                throw new IllegalArgumentException("Segment length must be positive: " + segmentLengths[i]);
            }
        });
    }
}

