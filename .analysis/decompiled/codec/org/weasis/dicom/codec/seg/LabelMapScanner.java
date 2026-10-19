/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.codec.seg;

import java.util.BitSet;
import java.util.function.IntPredicate;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LabelMapScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelMapScanner.class);
    private static final int[] EMPTY = new int[0];
    public static final IntPredicate ACCEPT_ALL = n -> true;

    private LabelMapScanner() {
    }

    public static int[] sortedDistinctLabels(Mat src) {
        return LabelMapScanner.sortedDistinctLabels(src, ACCEPT_ALL);
    }

    public static int[] sortedDistinctLabels(Mat src, IntPredicate accept) {
        if (src == null || src.channels() != 1) {
            return EMPTY;
        }
        int total = src.rows() * src.cols();
        if (total == 0) {
            return EMPTY;
        }
        int depth = src.depth();
        PixelReader reader = LabelMapScanner.readerFor(src, total, depth);
        if (reader == null) {
            LOGGER.warn("Unsupported label-map depth: {} (frame skipped)", (Object)depth);
            return EMPTY;
        }
        BitSet seen = new BitSet();
        for (int i = 0; i < total; ++i) {
            int v = reader.read(i);
            if (v <= 0 || !accept.test(v)) continue;
            seen.set(v);
        }
        return seen.stream().toArray();
    }

    private static PixelReader readerFor(Mat src, int total, int depth) {
        return switch (depth) {
            case 0, 1 -> {
                byte[] buf = new byte[total];
                src.get(0, 0, buf);
                yield i -> buf[i] & 0xFF;
            }
            case 2, 3 -> {
                short[] buf = new short[total];
                src.get(0, 0, buf);
                if (depth == 2) {
                    yield i -> buf[i] & 0xFFFF;
                }
                yield i -> buf[i];
            }
            case 4 -> {
                int[] buf = new int[total];
                src.get(0, 0, buf);
                yield i -> buf[i];
            }
            case 5 -> {
                float[] buf = new float[total];
                src.get(0, 0, buf);
                yield i -> Math.round(buf[i]);
            }
            default -> null;
        };
    }

    @FunctionalInterface
    private static interface PixelReader {
        public int read(int var1);
    }
}

