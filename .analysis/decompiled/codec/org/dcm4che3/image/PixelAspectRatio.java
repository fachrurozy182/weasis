/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;

public class PixelAspectRatio {
    public static float forImage(Attributes attrs) {
        return PixelAspectRatio.forImage(attrs, 2621492, 2621488, 1577316, 1581072);
    }

    public static float forPresentationState(Attributes attrs) {
        return PixelAspectRatio.forImage(attrs, 7340290, 0x700101);
    }

    private static float forImage(Attributes attrs, int aspectRatioTag, int ... pixelSpacingTags) {
        int[] ratio = attrs.getInts(aspectRatioTag);
        if (ratio != null && ratio.length == 2 && ratio[0] > 0 && ratio[1] > 0) {
            return (float)ratio[0] / (float)ratio[1];
        }
        for (int pixelSpacingTag : pixelSpacingTags) {
            float[] spaces = attrs.getFloats(pixelSpacingTag);
            if (spaces == null || spaces.length != 2 || !(spaces[0] > 0.0f) || !(spaces[1] > 0.0f)) continue;
            return spaces[0] / spaces[1];
        }
        return 1.0f;
    }
}

