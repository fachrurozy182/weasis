/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.image.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.weasis.core.Messages;

public final class KernelData {
    private static final float EPSILON = 1.0E-6f;
    private static final float MIN_SIGMA = 1.0E-5f;
    public static final KernelData NONE = new KernelData(Messages.getString("KernelData.0"), KernelType.IDENTITY, 1, 1, new float[]{1.0f});
    public static final KernelData MEAN = new KernelData(Messages.getString("KernelData.1"), KernelType.SMOOTHING, 3, 3, 1, 1, new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}, 9);
    public static final KernelData BLUR = new KernelData(Messages.getString("KernelData.2"), KernelType.SMOOTHING, 3, 3, 1, 1, new float[]{0.0f, 1.0f, 0.0f, 1.0f, 4.0f, 1.0f, 0.0f, 1.0f, 0.0f}, 8);
    public static final KernelData BLUR_MORE = new KernelData(Messages.getString("KernelData.3"), KernelType.SMOOTHING, 3, 3, 1, 1, new float[]{1.0f, 2.0f, 1.0f, 2.0f, 2.0f, 2.0f, 1.0f, 2.0f, 1.0f}, 14);
    public static final KernelData SHARPEN = new KernelData(Messages.getString("KernelData.4"), KernelType.SHARPENING, 3, 3, 1, 1, new float[]{0.0f, -1.0f, 0.0f, -1.0f, 8.0f, -1.0f, 0.0f, -1.0f, 0.0f}, 4);
    public static final KernelData SHARPEN_MORE = new KernelData(Messages.getString("KernelData.5"), KernelType.SHARPENING, 3, 3, 1, 1, new float[]{-1.0f, -1.0f, -1.0f, -1.0f, 12.0f, -1.0f, -1.0f, -1.0f, -1.0f}, 4);
    public static final KernelData EDGE_DETECT_1 = new KernelData(Messages.getString("KernelData.7"), KernelType.EDGE_DETECTION, 3, 3, new float[]{0.0f, -1.0f, 0.0f, -1.0f, 4.0f, -1.0f, 0.0f, -1.0f, 0.0f});
    public static final KernelData EDGE_DETECT_2 = new KernelData(Messages.getString("KernelData.8"), KernelType.EDGE_DETECTION, 3, 3, new float[]{-1.0f, -1.0f, -1.0f, -1.0f, 8.0f, -1.0f, -1.0f, -1.0f, -1.0f});
    public static final KernelData STRONG_EDGE = new KernelData(Messages.getString("KernelData.9"), KernelType.EDGE_DETECTION, 5, 5, new float[]{-2.0f, -2.0f, -2.0f, -2.0f, -2.0f, -2.0f, -3.0f, -3.0f, -3.0f, -2.0f, -2.0f, -3.0f, 53.0f, -3.0f, -2.0f, -2.0f, -3.0f, -3.0f, -3.0f, -2.0f, -2.0f, -2.0f, -2.0f, -2.0f, -2.0f});
    public static final KernelData DEFOCUS = new KernelData(Messages.getString("KernelData.6"), KernelType.SPECIAL_EFFECT, 3, 3, new float[]{1.0f, 1.0f, 1.0f, 1.0f, -7.0f, 1.0f, 1.0f, 1.0f, 1.0f});
    public static final KernelData OUTLINE = new KernelData(Messages.getString("KernelData.10"), KernelType.SPECIAL_EFFECT, 5, 5, new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, -16.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f});
    public static final KernelData EMBOSS = new KernelData(Messages.getString("KernelData.11"), KernelType.SPECIAL_EFFECT, 3, 3, new float[]{-5.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 5.0f});
    public static final KernelData GAUSSIAN_3 = KernelData.createGaussianKernel(Messages.getString("KernelData.12"), 3);
    public static final KernelData GAUSSIAN_5 = KernelData.createGaussianKernel(Messages.getString("KernelData.13"), 5);
    public static final KernelData GAUSSIAN_7 = KernelData.createGaussianKernel(Messages.getString("KernelData.14"), 7);
    public static final KernelData GAUSSIAN_9 = KernelData.createGaussianKernel(Messages.getString("KernelData.15"), 9);
    public static final KernelData LAPLACIAN_GAUSSIAN_3 = KernelData.createLaplacianGaussianKernel(Messages.getString("KernelData.16"), 3);
    public static final KernelData LAPLACIAN_GAUSSIAN_5 = KernelData.createLaplacianGaussianKernel(Messages.getString("KernelData.17"), 5);
    public static final KernelData LAPLACIAN_GAUSSIAN_7 = KernelData.createLaplacianGaussianKernel(Messages.getString("KernelData.18"), 7);
    private static final List<KernelData> ALL_FILTERS = List.of(NONE, MEAN, BLUR, BLUR_MORE, SHARPEN, SHARPEN_MORE, DEFOCUS, EDGE_DETECT_1, EDGE_DETECT_2, STRONG_EDGE, OUTLINE, EMBOSS, GAUSSIAN_3, GAUSSIAN_5, GAUSSIAN_7, GAUSSIAN_9, LAPLACIAN_GAUSSIAN_3, LAPLACIAN_GAUSSIAN_5, LAPLACIAN_GAUSSIAN_7);
    private final String name;
    private final KernelType type;
    private final int width;
    private final int height;
    private final int xOrigin;
    private final int yOrigin;
    private final float[] data;
    private final boolean isMorphological;

    public KernelData(String name, KernelType type, int width, int height, int xOrigin, int yOrigin, float[] data, int divisor) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.width = KernelData.requirePositive(width, "width");
        this.height = KernelData.requirePositive(height, "height");
        this.xOrigin = KernelData.validateOrigin(xOrigin, width);
        this.yOrigin = KernelData.validateOrigin(yOrigin, height);
        this.isMorphological = type == KernelType.MORPHOLOGICAL;
        this.data = KernelData.processData(data, divisor);
    }

    public KernelData(String name, KernelType type, int width, int height, float[] data) {
        this(name, type, width, height, width / 2, height / 2, data, 1);
    }

    @Deprecated(forRemoval=true)
    public KernelData(String name, boolean morphological, int width, int height, int xOrigin, int yOrigin, float[] data, int divisor) {
        this(name, morphological ? KernelType.MORPHOLOGICAL : KernelType.CONVOLUTION, width, height, xOrigin, yOrigin, data, divisor);
    }

    @Deprecated(forRemoval=true)
    public KernelData(String name, boolean morphological, int width, int height, float[] data) {
        this(name, morphological ? KernelType.MORPHOLOGICAL : KernelType.CONVOLUTION, width, height, data);
    }

    public String getName() {
        return this.name;
    }

    public KernelType getType() {
        return this.type;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getXOrigin() {
        return this.xOrigin;
    }

    public int getYOrigin() {
        return this.yOrigin;
    }

    public boolean isMorphological() {
        return this.isMorphological;
    }

    public float[] getData() {
        return (float[])this.data.clone();
    }

    @Deprecated(forRemoval=true)
    public boolean isMorphologicalFilter() {
        return this.isMorphological;
    }

    public float getValueAt(int x, int y) {
        this.validatePosition(x, y);
        return this.data[y * this.width + x];
    }

    public KernelData withValueAt(int x, int y, float value) {
        this.validatePosition(x, y);
        float[] newData = (float[])this.data.clone();
        newData[y * this.width + x] = value;
        return new KernelData(this.name, this.type, this.width, this.height, this.xOrigin, this.yOrigin, newData, 1);
    }

    public float getSum() {
        float sum = 0.0f;
        for (float value : this.data) {
            sum += value;
        }
        return sum;
    }

    public boolean isNormalized() {
        return Math.abs(this.getSum() - 1.0f) < 1.0E-6f;
    }

    public KernelData normalize() {
        float sum = this.getSum();
        if (Math.abs(sum) < 1.0E-6f) {
            return this;
        }
        float[] normalizedData = new float[this.data.length];
        for (int i = 0; i < this.data.length; ++i) {
            normalizedData[i] = this.data[i] / sum;
        }
        return new KernelData(this.name + " (normalized)", this.type, this.width, this.height, this.xOrigin, this.yOrigin, normalizedData, 1);
    }

    public static KernelData createGaussianKernel(String name, int size) {
        return KernelData.createGaussianKernel(name, size, size);
    }

    public static KernelData createGaussianKernel(String name, int width, int height) {
        int w = KernelData.ensureOdd(width);
        int h = KernelData.ensureOdd(height);
        return KernelData.createGaussianKernel(name, (float)(w - 1) / 6.0f, (float)(h - 1) / 6.0f);
    }

    public static KernelData createGaussianKernel(String name, float sigmaX, float sigmaY) {
        int width = KernelData.ensureOdd(Math.round(6.0f * sigmaX));
        int height = KernelData.ensureOdd(Math.round(6.0f * sigmaY));
        float sigX = Math.max(sigmaX, 1.0E-5f);
        float sigY = Math.max(sigmaY, 1.0E-5f);
        float[] kernel = KernelData.computeGaussian(width, height, sigX, sigY);
        return new KernelData(name, KernelType.SMOOTHING, width, height, kernel);
    }

    public static KernelData createLaplacianGaussianKernel(String name, int size) {
        float sigma = (float)(size - 1) / 6.0f;
        float[] kernel = KernelData.computeLoG(size, sigma);
        return new KernelData(name, KernelType.EDGE_DETECTION, size, size, kernel);
    }

    public static KernelData createFromArray(String name, KernelType type, float[][] kernelArray) {
        int height = kernelArray.length;
        int width = kernelArray[0].length;
        float[] data = new float[width * height];
        for (int y = 0; y < height; ++y) {
            System.arraycopy(kernelArray[y], 0, data, y * width, width);
        }
        return new KernelData(name, type, width, height, data);
    }

    public static List<KernelData> getAllFilters() {
        return ALL_FILTERS;
    }

    public static List<KernelData> getFiltersByType(KernelType type) {
        return ALL_FILTERS.stream().filter(k -> k.getType() == type).toList();
    }

    public static List<KernelData> getSmoothingFilters() {
        return KernelData.getFiltersByType(KernelType.SMOOTHING);
    }

    public static List<KernelData> getSharpeningFilters() {
        return KernelData.getFiltersByType(KernelType.SHARPENING);
    }

    public static List<KernelData> getEdgeDetectionFilters() {
        return KernelData.getFiltersByType(KernelType.EDGE_DETECTION);
    }

    public String toString() {
        return this.name;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof KernelData)) return false;
        KernelData other = (KernelData)obj;
        if (this.width != other.width) return false;
        if (this.height != other.height) return false;
        if (this.xOrigin != other.xOrigin) return false;
        if (this.yOrigin != other.yOrigin) return false;
        if (this.isMorphological != other.isMorphological) return false;
        if (!Objects.equals(this.name, other.name)) return false;
        if (this.type != other.type) return false;
        if (!Arrays.equals(this.data, other.data)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.type, this.width, this.height, this.xOrigin, this.yOrigin, this.isMorphological, Arrays.hashCode(this.data)});
    }

    private static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive: " + value);
        }
        return value;
    }

    private static int validateOrigin(int origin, int dimension) {
        return origin < 0 || origin >= dimension ? dimension / 2 : origin;
    }

    private void validatePosition(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new IndexOutOfBoundsException("Position (%d, %d) is out of bounds".formatted(new Object[]{x, y}));
        }
    }

    private static float[] processData(float[] data, int divisor) {
        if (data == null) {
            return new float[]{1.0f};
        }
        if (divisor == 1) {
            return (float[])data.clone();
        }
        float div = Math.max(divisor, 1);
        float[] processedData = new float[data.length];
        for (int i = 0; i < data.length; ++i) {
            processedData[i] = data[i] / div;
        }
        return processedData;
    }

    private static int ensureOdd(int value) {
        return value % 2 == 0 ? value + 1 : value;
    }

    private static float[] computeGaussian(int width, int height, float sigX, float sigY) {
        float[] kernel = new float[width * height];
        double sum = 0.0;
        int centerX = (width - 1) / 2;
        int centerY = (height - 1) / 2;
        double sigXSq = (double)sigX * (double)sigX;
        double sigYSq = (double)sigY * (double)sigY;
        for (int y = 0; y < height; ++y) {
            double dy = (double)y - (double)centerY;
            double dy2OverSigYSq = dy * dy / sigYSq;
            for (int x = 0; x < width; ++x) {
                double dx = (double)x - (double)centerX;
                double value = Math.exp(-0.5 * (dx * dx / sigXSq + dy2OverSigYSq));
                kernel[y * width + x] = (float)value;
                sum += value;
            }
        }
        if (sum > (double)1.0E-6f) {
            for (int i = 0; i < kernel.length; ++i) {
                kernel[i] = (float)((double)kernel[i] / sum);
            }
        }
        return kernel;
    }

    private static float[] computeLoG(int size, float sigma) {
        float[] kernel = new float[size * size];
        double sum = 0.0;
        int center = (size - 1) / 2;
        double sigmaSq = (double)sigma * (double)sigma;
        double twoSigmaSq = 2.0 * sigmaSq;
        double sigmaSqSq = sigmaSq * sigmaSq;
        for (int y = 0; y < size; ++y) {
            double dy = (double)y - (double)center;
            for (int x = 0; x < size; ++x) {
                double dx = (double)x - (double)center;
                double distSq = dx * dx + dy * dy;
                double value = (distSq - twoSigmaSq) / sigmaSqSq * Math.exp(-distSq / twoSigmaSq);
                kernel[y * size + x] = (float)value;
                sum += value;
            }
        }
        if (Math.abs(sum) > (double)1.0E-6f) {
            for (int i = 0; i < kernel.length; ++i) {
                kernel[i] = (float)((double)kernel[i] / sum);
            }
        }
        return kernel;
    }

    public static enum KernelType {
        IDENTITY("Identity"),
        SMOOTHING("Smoothing"),
        SHARPENING("Sharpening"),
        EDGE_DETECTION("Edge Detection"),
        SPECIAL_EFFECT("Special Effect"),
        MORPHOLOGICAL("Morphological"),
        CONVOLUTION("Convolution");

        private final String displayName;

        private KernelType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String toString() {
            return this.displayName;
        }
    }
}

