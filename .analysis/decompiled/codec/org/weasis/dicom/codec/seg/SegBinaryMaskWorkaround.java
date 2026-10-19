/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.dicom.codec.seg;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.dcm4che3.data.Attributes;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;

public final class SegBinaryMaskWorkaround {
    private static final Logger LOGGER = LoggerFactory.getLogger(SegBinaryMaskWorkaround.class);
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
    private static final Map<String, AtomicBoolean> NEEDS_WORKAROUND = new ConcurrentHashMap<String, AtomicBoolean>();

    private SegBinaryMaskWorkaround() {
    }

    public static boolean isAffected(Attributes dicom) {
        if (dicom == null) {
            return false;
        }
        int bitsAllocated = dicom.getInt(2621696, 0);
        if (bitsAllocated != 1) {
            return false;
        }
        int columns = dicom.getInt(2621457, 0);
        return columns > 0 && columns % 8 != 0;
    }

    public static PlanarImage reDecodeFrame(Attributes dicom, int frameIndex, int width, int height) {
        byte[] pixelData;
        if (dicom == null || frameIndex < 0 || width <= 0 || height <= 0) {
            return null;
        }
        try {
            pixelData = dicom.getBytes(2145386512);
        }
        catch (IOException ioe) {
            SegBinaryMaskWorkaround.logOnce(dicom, "BINARY SEG Pixel Data read failed: " + ioe.getMessage());
            return null;
        }
        if (pixelData == null) {
            SegBinaryMaskWorkaround.logOnce(dicom, "BINARY SEG with width % 8 != 0 but Pixel Data is not a contiguous byte array \u2014 workaround skipped");
            return null;
        }
        long framePixels = (long)width * (long)height;
        long firstBit = framePixels * (long)frameIndex;
        long lastBit = firstBit + framePixels;
        long totalBitsAvailable = (long)pixelData.length * 8L;
        if (lastBit > totalBitsAvailable) {
            SegBinaryMaskWorkaround.logOnce(dicom, "BINARY SEG Pixel Data truncated (need " + lastBit + " bits, have " + totalBitsAvailable + ") \u2014 workaround skipped for frame " + frameIndex);
            return null;
        }
        ImageCV out = new ImageCV(height, width, CvType.CV_8UC1);
        Mat mat = out.toMat();
        byte[] row = new byte[width];
        long bit = firstBit;
        for (int y = 0; y < height; ++y) {
            int x = 0;
            while (x < width) {
                int b = pixelData[(int)(bit >> 3)] & 0xFF;
                int v = b >> (int)(bit & 7L) & 1;
                row[x] = v == 0 ? 0 : -1;
                ++x;
                ++bit;
            }
            mat.put(y, 0, row);
        }
        SegBinaryMaskWorkaround.logOnce(dicom, "Re-decoded BINARY SEG with width=" + width + " (workaround active)");
        return out;
    }

    private static void logOnce(Attributes dicom, String message) {
        String key = dicom.getString(524312, "<no-uid>");
        NEEDS_WORKAROUND.computeIfAbsent(key, string -> new AtomicBoolean(false));
        if (WARNED.add(key)) {
            LOGGER.info("SEG {}: {}", (Object)key, (Object)message);
        }
    }
}

