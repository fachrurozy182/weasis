/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.img.data;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import org.dcm4che3.data.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record EmbeddedOverlay(int groupOffset, int bitPosition) {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedOverlay.class);
    private static final int DEFAULT_BITS_ALLOCATED = 8;
    private static final int DEFAULT_OVERLAY_BITS_ALLOCATED = 1;
    private static final int DEFAULT_BIT_POSITION = 0;

    public static List<EmbeddedOverlay> getEmbeddedOverlay(Attributes dcm) {
        if (dcm == null) {
            return List.of();
        }
        DicomBitConfiguration bitConfig = new DicomBitConfiguration(dcm);
        return IntStream.range(0, 16).mapToObj(groupIndex -> EmbeddedOverlay.createOverlayIfValid(dcm, groupIndex, bitConfig)).filter(Objects::nonNull).toList();
    }

    private static EmbeddedOverlay createOverlayIfValid(Attributes dcm, int groupIndex, DicomBitConfiguration bitConfig) {
        int groupOffset = groupIndex << 17;
        if (!EmbeddedOverlay.isEmbeddedOverlay(dcm, groupOffset)) {
            return null;
        }
        int bitPosition = dcm.getInt(0x60000102 | groupOffset, 0);
        if (bitPosition < bitConfig.bitsStored()) {
            LOGGER.info("Ignoring embedded overlay #{} from bit #{} (< bits stored: {})", new Object[]{groupIndex + 1, bitPosition, bitConfig.bitsStored()});
            return null;
        }
        return new EmbeddedOverlay(groupOffset, bitPosition);
    }

    private static boolean isEmbeddedOverlay(Attributes dcm, int groupOffset) {
        return dcm.getInt(0x60000100 | groupOffset, 1) != 1;
    }

    private record DicomBitConfiguration(int bitsAllocated, int bitsStored) {
        DicomBitConfiguration(Attributes dcm) {
            this(dcm.getInt(2621696, 8), dcm.getInt(2621697, dcm.getInt(2621696, 8)));
        }
    }
}

