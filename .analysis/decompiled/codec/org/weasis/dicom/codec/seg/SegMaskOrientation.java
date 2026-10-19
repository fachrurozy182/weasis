/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  org.opencv.core.Core
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.dicom.codec.seg;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;

public final class SegMaskOrientation {
    private static final Logger LOGGER = LoggerFactory.getLogger(SegMaskOrientation.class);
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private SegMaskOrientation() {
    }

    public static PlanarImage normalize(PlanarImage mask, int declaredCols, int declaredRows, String sopInstanceUid) {
        if (mask == null || declaredCols <= 0 || declaredRows <= 0) {
            return mask;
        }
        int w = mask.width();
        int h = mask.height();
        return switch (SegMaskOrientation.classify(w, h, declaredCols, declaredRows).ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> mask;
            case 1 -> SegMaskOrientation.transpose(mask, w, h, declaredCols, declaredRows, sopInstanceUid);
            case 2 -> {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("SEG {}: decoded mask {}\u00d7{} matches neither declared {}\u00d7{} nor its transpose; frame skipped", new Object[]{sopInstanceUid, w, h, declaredCols, declaredRows});
                }
                yield null;
            }
        };
    }

    private static Orientation classify(int w, int h, int dc, int dr) {
        if (w == dc && h == dr) {
            return Orientation.MATCH;
        }
        if (w == dr && h == dc) {
            return Orientation.TRANSPOSED;
        }
        return Orientation.MISMATCH;
    }

    private static ImageCV transpose(PlanarImage mask, int w, int h, int dc, int dr, String sopInstanceUid) {
        Mat src = mask.toMat();
        ImageCV out = new ImageCV();
        Core.transpose((Mat)src, (Mat)out);
        String key = Objects.requireNonNullElse(sopInstanceUid, "<no-uid>");
        if (WARNED.add(key)) {
            LOGGER.warn("SEG {}: decoded mask delivered transposed ({}\u00d7{}) \u2014 re-aligning to declared {}\u00d7{}", new Object[]{key, w, h, dc, dr});
        }
        return out;
    }

    private static enum Orientation {
        MATCH,
        TRANSPOSED,
        MISMATCH;

    }
}

