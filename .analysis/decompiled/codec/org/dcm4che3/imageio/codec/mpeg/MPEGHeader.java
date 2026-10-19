/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.imageio.codec.mpeg;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;

public class MPEGHeader {
    private static final String[] ASPECT_RATIO_1_1 = new String[]{"1", "1"};
    private static final String[] ASPECT_RATIO_4_3 = new String[]{"4", "3"};
    private static final String[] ASPECT_RATIO_16_9 = new String[]{"16", "9"};
    private static final String[] ASPECT_RATIO_221_100 = new String[]{"221", "100"};
    private static final String[][] ASPECT_RATIOS = new String[][]{ASPECT_RATIO_1_1, ASPECT_RATIO_4_3, ASPECT_RATIO_16_9, ASPECT_RATIO_221_100};
    static final int[] FPS = new int[]{24, 1001, 24, 1000, 25, 1000, 30, 1001, 30, 1000, 50, 1000, 60, 1001, 60, 1000};
    private final byte[] data;
    private final int seqHeaderOffset;

    public MPEGHeader(byte[] data) {
        this.data = data;
        this.seqHeaderOffset = MPEGHeader.findSequenceHeaderOffset(data);
    }

    private static int findSequenceHeaderOffset(byte[] data) {
        if (data == null || data.length < 4) {
            return -1;
        }
        for (int i = 0; i <= data.length - 4; ++i) {
            if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 1 || data[i + 3] != -77) continue;
            return i + 4;
        }
        return -1;
    }

    public boolean isValid() {
        return this.seqHeaderOffset != -1;
    }

    public Attributes toAttributes(Attributes attrs, long length) {
        if (this.seqHeaderOffset == -1) {
            return null;
        }
        if (attrs == null) {
            attrs = new Attributes(15);
        }
        int off = this.seqHeaderOffset;
        int x = (this.data[off] & 0xFF) << 4 | (this.data[off + 1] & 0xF0) >> 4;
        int y = (this.data[off + 1] & 0xF) << 8 | this.data[off + 2] & 0xFF;
        int aspectRatio = this.data[off + 4] >> 4 & 0xF;
        int frameRate = this.data[off + 4] & 0xF;
        int bitRate = (this.data[off + 5] & 0xFF) << 10 | (this.data[off + 6] & 0xFF) << 2 | (this.data[off + 7] & 0xC0) >> 6;
        int numFrames = 9999;
        if (frameRate > 0 && frameRate < 9) {
            int frameRate2 = frameRate - 1 << 1;
            attrs.setInt(1572928, VR.IS, FPS[frameRate2]);
            attrs.setFloat(1577059, VR.DS, (float)FPS[frameRate2 + 1] / (float)FPS[frameRate2]);
            if (bitRate > 0) {
                numFrames = (int)(20L * length * (long)FPS[frameRate2] / (long)FPS[frameRate2 + 1] / (long)bitRate);
            }
        }
        if (aspectRatio > 0 && aspectRatio < 5) {
            attrs.setString(2621492, VR.IS, ASPECT_RATIOS[aspectRatio - 1]);
        }
        return MPEGHeader.setImageAttributes(attrs, numFrames, y, x);
    }

    public static Attributes setImageAttributes(Attributes attrs, int numFrames, int rows, int columns) {
        attrs.setInt(0x280002, VR.US, 3);
        attrs.setString(2621444, VR.CS, "YBR_PARTIAL_420");
        attrs.setInt(2621446, VR.US, 0);
        attrs.setInt(2621449, VR.AT, 1577059);
        attrs.setInt(0x280008, VR.IS, numFrames);
        attrs.setInt(2621456, VR.US, rows);
        attrs.setInt(2621457, VR.US, columns);
        attrs.setInt(2621696, VR.US, 8);
        attrs.setInt(2621697, VR.US, 8);
        attrs.setInt(2621698, VR.US, 7);
        attrs.setInt(2621699, VR.US, 0);
        attrs.setString(2629904, VR.CS, "01");
        return attrs;
    }
}

