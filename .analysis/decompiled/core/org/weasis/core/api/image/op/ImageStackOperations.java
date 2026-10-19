/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.image.op;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.weasis.core.api.image.cv.CvUtil;
import org.weasis.opencv.data.PlanarImage;

public final class ImageStackOperations {
    private ImageStackOperations() {
    }

    public static PlanarImage max(List<PlanarImage> sources) {
        return ImageStackOperations.applyStackOperation(sources, CvUtil::maxStack);
    }

    public static PlanarImage min(List<PlanarImage> sources) {
        return ImageStackOperations.applyStackOperation(sources, CvUtil::minStack);
    }

    public static PlanarImage mean(List<PlanarImage> sources) {
        return ImageStackOperations.applyStackOperation(sources, CvUtil::meanStack);
    }

    public static PlanarImage apply(List<PlanarImage> sources, Function<List<PlanarImage>, PlanarImage> operation) {
        Objects.requireNonNull(operation, "Operation cannot be null");
        return ImageStackOperations.applyStackOperation(sources, operation);
    }

    private static PlanarImage applyStackOperation(List<PlanarImage> sources, Function<List<PlanarImage>, PlanarImage> operation) {
        ImageStackOperations.validateSources(sources);
        return operation.apply(sources);
    }

    private static void validateSources(List<PlanarImage> sources) {
        if (Objects.requireNonNull(sources, "Sources cannot be null").isEmpty()) {
            throw new IllegalArgumentException("Sources cannot be empty");
        }
    }
}

