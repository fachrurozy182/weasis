/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.opencv.core.Scalar
 *  org.opencv.imgproc.Imgproc
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.image.cv;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.weasis.core.api.image.util.KernelData;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;

public final class CvUtil {
    private static final int MIN_STACK_SIZE = 2;
    private static final int DEFAULT_DEPTH = -1;

    private CvUtil() {
    }

    public static void runGarbageCollectorAndWait(long ms) {
        System.gc();
        System.gc();
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static ImageCV filter(Mat source, KernelData kernel) {
        Objects.requireNonNull(kernel, "Kernel cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");
        Mat kernelMatrix = CvUtil.createKernelMatrix(kernel);
        ImageCV result = new ImageCV();
        Imgproc.filter2D((Mat)source, (Mat)result, (int)-1, (Mat)kernelMatrix);
        return result;
    }

    public static ImageCV meanStack(List<PlanarImage> sources) {
        if (CvUtil.isInvalidStackSize(sources)) {
            return null;
        }
        StackContext context = CvUtil.createStackContext(sources);
        Mat accumulator = CvUtil.initializeAccumulator(context);
        for (int i = 1; i < context.sourceCount(); ++i) {
            PlanarImage image = sources.get(i);
            CvUtil.accumulateFloatStack(image, context.referenceImage(), accumulator);
        }
        return CvUtil.computeMean(accumulator, context);
    }

    public static ImageCV minStack(List<PlanarImage> sources) {
        return CvUtil.processStackOperation(sources, (result, img) -> {
            Core.min((Mat)result, (Mat)img, (Mat)result);
            return result;
        });
    }

    public static ImageCV maxStack(List<PlanarImage> sources) {
        return CvUtil.processStackOperation(sources, (result, img) -> {
            Core.max((Mat)result, (Mat)img, (Mat)result);
            return result;
        });
    }

    public static void accumulateFloatStack(PlanarImage image, PlanarImage reference, Mat accumulator) {
        if (!CvUtil.hasSameDimensions(image, reference)) {
            return;
        }
        Mat mat = (Mat)image;
        if (CvType.depth((int)image.type()) == 3) {
            CvUtil.accumulateWith16BitConversion(mat, reference, accumulator);
        } else {
            Imgproc.accumulate((Mat)mat, (Mat)accumulator);
        }
    }

    private static Mat createKernelMatrix(KernelData kernel) {
        Mat matrix = new Mat(kernel.getHeight(), kernel.getWidth(), 5);
        matrix.put(0, 0, kernel.getData());
        return matrix;
    }

    private static boolean isInvalidStackSize(List<PlanarImage> sources) {
        return sources == null || sources.size() < 2;
    }

    private static StackContext createStackContext(List<PlanarImage> sources) {
        PlanarImage firstImage = (PlanarImage)sources.getFirst();
        return new StackContext(firstImage, sources.size());
    }

    private static Mat initializeAccumulator(StackContext context) {
        Mat accumulator = new Mat(context.referenceImage.height(), context.referenceImage.width(), 5);
        context.referenceImage.toMat().convertTo(accumulator, 5);
        return accumulator;
    }

    private static ImageCV computeMean(Mat accumulator, StackContext context) {
        ImageCV result = new ImageCV();
        Core.divide((Mat)accumulator, (Scalar)Scalar.all((double)context.sourceCount()), (Mat)accumulator);
        accumulator.convertTo((Mat)result, context.referenceImage.type());
        return result;
    }

    private static ImageCV processStackOperation(List<PlanarImage> sources, BinaryOperator<Mat> operation) {
        if (CvUtil.isInvalidStackSize(sources)) {
            return null;
        }
        StackContext context = CvUtil.createStackContext(sources);
        if (context.referenceImage == null) {
            return null;
        }
        ImageCV result = new ImageCV();
        context.referenceImage.toMat().copyTo((Mat)result);
        for (int i = 1; i < context.sourceCount(); ++i) {
            PlanarImage image = sources.get(i);
            if (image == null || !CvUtil.hasSameDimensions(image, (PlanarImage)result)) continue;
            operation.apply((Mat)result, image.toImageCV());
        }
        return result;
    }

    private static boolean hasSameDimensions(PlanarImage image, PlanarImage reference) {
        return image.width() == reference.width() && image.height() == reference.height() && image.type() == reference.type();
    }

    private static void accumulateWith16BitConversion(Mat mat, PlanarImage reference, Mat accumulator) {
        Mat floatImage = new Mat(reference.height(), reference.width(), 5);
        mat.convertTo(floatImage, 5);
        Imgproc.accumulate((Mat)floatImage, (Mat)accumulator);
    }

    private record StackContext(PlanarImage referenceImage, int sourceCount) {
    }
}

