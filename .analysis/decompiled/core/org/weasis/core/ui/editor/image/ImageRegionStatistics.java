/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageAnalyzer
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.lut.ByteLut
 */
package org.weasis.core.ui.editor.image;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.editor.image.DisplayByteLut;
import org.weasis.core.ui.editor.image.HistogramData;
import org.weasis.core.ui.model.graphic.GraphicArea;
import org.weasis.core.ui.model.utils.ImageStatistics;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageAnalyzer;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.lut.ByteLut;

public class ImageRegionStatistics {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageRegionStatistics.class);

    private ImageRegionStatistics() {
    }

    public static List<Mat> prepareInputImages(GraphicArea graphic, MeasurableLayer layer) {
        if (layer != null && layer.hasContent()) {
            PlanarImage image;
            Shape shape = null;
            if (graphic != null) {
                if (!graphic.isShapeValid()) {
                    return Collections.emptyList();
                }
                shape = graphic.getShape();
            }
            if ((image = layer.getSourceRenderedImage()) == null) {
                return Collections.emptyList();
            }
            Shape roi = ImageRegionStatistics.getShape(layer, shape);
            Integer paddingValue = (Integer)layer.getSourceTagValue(TagW.get("PixelPaddingValue"));
            Integer paddingLimit = (Integer)layer.getSourceTagValue(TagW.get("PixelPaddingRangeLimit"));
            return ImageAnalyzer.getMaskImage((Mat)image.toMat(), (Shape)roi, (Integer)paddingValue, (Integer)paddingLimit);
        }
        return Collections.emptyList();
    }

    private static Shape getShape(MeasurableLayer layer, Shape shape) {
        Shape roi = null;
        if (shape != null) {
            AffineTransform transform = layer.getShapeTransform();
            Point offset = layer.getOffset();
            if (offset != null) {
                if (transform == null) {
                    transform = AffineTransform.getTranslateInstance(-offset.getX(), -offset.getY());
                } else {
                    transform.translate(-offset.getX(), -offset.getY());
                }
            }
            roi = transform != null ? transform.createTransformedShape(shape) : shape;
        }
        return roi;
    }

    public static List<MeasureItem> getImageStatistics(MeasurableLayer layer) {
        return ImageRegionStatistics.getImageStatistics(null, layer, true);
    }

    public static List<HistogramData> getHistogram(GraphicArea graphic, MeasurableLayer layer) {
        List<Mat> imgPr = ImageRegionStatistics.prepareInputImages(graphic, layer);
        if (layer != null && layer.hasContent() && imgPr != null && imgPr.size() == 2) {
            return ImageRegionStatistics.getHistogram(imgPr.get(0), imgPr.get(1), layer);
        }
        return Collections.emptyList();
    }

    public static List<HistogramData> getHistogram(Mat srcImg, Mat mask, MeasurableLayer layer) {
        if (srcImg == null || layer == null || !layer.hasContent()) {
            return Collections.emptyList();
        }
        double pixMin = layer.getPixelMin();
        double pixMax = layer.getPixelMax();
        int channels = srcImg.channels();
        HistogramData.Model colorModel = channels > 1 ? HistogramData.Model.RGB : HistogramData.Model.GRAY;
        int[] selChannels = new int[channels];
        for (int i = 0; i < selChannels.length; ++i) {
            selChannels[i] = i;
        }
        ArrayList<HistogramData> data = new ArrayList<HistogramData>();
        int datatype = ImageConversion.convertToDataType((int)srcImg.type());
        boolean intVal = datatype >= 0 && datatype < 3;
        try {
            int binCount = intVal ? (int)pixMax - (int)pixMin + 1 : 1024;
            List<Mat> histograms = HistogramData.computeHistogram(srcImg, mask, binCount, selChannels, colorModel, pixMin, pixMax);
            ByteLut[] lut = colorModel.getByteLut();
            DisplayByteLut[] luts = new DisplayByteLut[lut.length];
            for (int i = 0; i < lut.length; ++i) {
                luts[i] = new DisplayByteLut(lut[i]);
                Mat h = histograms.get(i);
                float[] histValues = new float[h.rows()];
                h.get(0, 0, histValues);
                data.add(new HistogramData(histValues, luts[i], i, colorModel, null, pixMin, pixMax, layer));
            }
        }
        catch (Exception e) {
            LOGGER.error("Build histogram", (Throwable)e);
        }
        return data;
    }

    public static List<MeasureItem> getImageStatistics(GraphicArea graphic, MeasurableLayer layer, boolean releaseEvent) {
        if (layer != null && layer.hasContent()) {
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>();
            if (releaseEvent && ImageRegionStatistics.isOneComputed()) {
                List<Mat> imgPr = ImageRegionStatistics.prepareInputImages(graphic, layer);
                if (imgPr.size() == 2) {
                    Mat srcImg = imgPr.get(0);
                    Mat mask = imgPr.get(1);
                    RoiPixelStats direct = srcImg.channels() == 1 ? ImageRegionStatistics.computeRoiPixelStats(srcImg, mask) : null;
                    List<HistogramData> hists = ImageRegionStatistics.getHistogram(srcImg, mask, layer);
                    for (int i = 0; i < hists.size(); ++i) {
                        HistogramData data = hists.get(i);
                        Integer bandIndex = hists.size() == 1 ? null : Integer.valueOf(data.getBandIndex());
                        measVal.addAll(ImageRegionStatistics.getStatistics(data, bandIndex, i == 0, bandIndex == null ? direct : null));
                    }
                }
                for (MeasurableLayer secondary : layer.getSecondaryLayers()) {
                    measVal.addAll(ImageRegionStatistics.getSuvStatistics(graphic, secondary));
                }
            }
            return measVal;
        }
        return Collections.emptyList();
    }

    private static RoiPixelStats computeRoiPixelStats(Mat srcImg, Mat mask) {
        try {
            double[][] s = ImageAnalyzer.meanStdDev((Mat)srcImg, (Mat)mask, null, null);
            if (s.length < 5 || s[0].length == 0 || s[4][0] <= 0.0) {
                return null;
            }
            return new RoiPixelStats(s[0][0], s[1][0], s[2][0], s[3][0], s[4][0]);
        }
        catch (Exception e) {
            LOGGER.error("Compute ROI pixel statistics", (Throwable)e);
            return null;
        }
    }

    private static List<MeasureItem> getSuvStatistics(GraphicArea graphic, MeasurableLayer petLayer) {
        if (petLayer == null || !petLayer.hasContent()) {
            return Collections.emptyList();
        }
        Double suv = (Double)petLayer.getSourceTagValue(TagW.SuvFactor);
        if (suv == null || !ImageStatistics.IMAGE_MIN.getComputed().booleanValue() && !ImageStatistics.IMAGE_MAX.getComputed().booleanValue() && !ImageStatistics.IMAGE_MEAN.getComputed().booleanValue()) {
            return Collections.emptyList();
        }
        List<Mat> imgPr = ImageRegionStatistics.prepareInputImages(graphic, petLayer);
        if (imgPr.size() != 2) {
            return Collections.emptyList();
        }
        Mat mask = imgPr.get(1);
        Mat src = new Mat();
        imgPr.get(0).convertTo(src, 5);
        int rows = src.rows();
        int cols = src.cols();
        float[] values = new float[cols];
        byte[] selected = mask == null ? null : new byte[cols];
        double min = Double.MAX_VALUE;
        double max = -1.7976931348623157E308;
        double sum = 0.0;
        long count = 0L;
        for (int r = 0; r < rows; ++r) {
            src.get(r, 0, values);
            if (mask != null) {
                mask.get(r, 0, selected);
            }
            for (int c = 0; c < cols; ++c) {
                if (selected != null && selected[c] == 0 || Float.isNaN(values[c])) continue;
                double v = values[c];
                min = Math.min(min, v);
                max = Math.max(max, v);
                sum += v;
                ++count;
            }
        }
        src.release();
        if (count == 0L) {
            return Collections.emptyList();
        }
        String unit = "SUVbw, g/ml";
        String label = petLayer.getStatLabel();
        String ext = " " + (label == null ? "PT" : label);
        ArrayList<MeasureItem> measList = new ArrayList<MeasureItem>(3);
        ImageRegionStatistics.addSuvMeasure(measList, ImageStatistics.IMAGE_MIN, ext, petLayer.pixelToRealValue(min) * suv, unit);
        ImageRegionStatistics.addSuvMeasure(measList, ImageStatistics.IMAGE_MAX, ext, petLayer.pixelToRealValue(max) * suv, unit);
        ImageRegionStatistics.addSuvMeasure(measList, ImageStatistics.IMAGE_MEAN, ext, petLayer.pixelToRealValue(sum / (double)count) * suv, unit);
        return measList;
    }

    private static void addSuvMeasure(List<MeasureItem> measList, Measurement measure, String ext, double value, String unit) {
        if (measure.getComputed().booleanValue()) {
            measList.add(new MeasureItem(measure, ext, value, unit));
        }
    }

    private static boolean isOneComputed() {
        return ImageStatistics.IMAGE_PIXELS.getComputed() != false || ImageStatistics.IMAGE_MIN.getComputed() != false || ImageStatistics.IMAGE_MAX.getComputed() != false || ImageStatistics.IMAGE_MEDIAN.getComputed() != false || ImageStatistics.IMAGE_MEAN.getComputed() != false || ImageStatistics.IMAGE_STD.getComputed() != false || ImageStatistics.IMAGE_SKEW.getComputed() != false || ImageStatistics.IMAGE_KURTOSIS.getComputed() != false || ImageStatistics.IMAGE_ENTROPY.getComputed() != false;
    }

    private static void addMeasure(List<MeasureItem> measVal, Measurement measure, Integer channelIndex, Double val, String unit) {
        if (measure.getComputed().booleanValue()) {
            if (channelIndex == null) {
                measVal.add(new MeasureItem(measure, val, unit));
            } else {
                measVal.add(new MeasureItem(measure, " " + (channelIndex + 1), val, unit));
            }
        }
    }

    public static List<MeasureItem> getStatistics(HistogramData data, Integer channelIndex, boolean imagePixels) {
        return ImageRegionStatistics.getStatistics(data, channelIndex, imagePixels, null);
    }

    private static List<MeasureItem> getStatistics(HistogramData data, Integer channelIndex, boolean imagePixels, RoiPixelStats direct) {
        MeasurableLayer layer = data.getLayer();
        if (layer != null && layer.hasContent()) {
            float[] bins = data.getHistValues();
            double offset = data.getPixMin();
            ArrayList<MeasureItem> measList = new ArrayList<MeasureItem>();
            double sum = 0.0;
            double min = 3.4028234663852886E38;
            double max = -3.4028234663852886E38;
            double mean = 0.0;
            double binFactor = bins.length > 1 ? (data.getPixMax() - offset) / (double)(bins.length - 1) : 0.0;
            for (int k = 0; k < bins.length; ++k) {
                boolean valid = MathUtil.isDifferentFromZero((float)bins[k]) && bins[k] > 0.0f;
                float val = bins[k];
                double level = layer.pixelToRealValue((double)k * binFactor + offset);
                if (valid && level < min) {
                    min = level;
                }
                if (valid && level > max) {
                    max = level;
                }
                sum += (double)val;
                mean += (double)val * level;
            }
            mean /= sum;
            double m2 = 0.0;
            double skew = 0.0;
            double kurtosis = 0.0;
            double entropy = 0.0;
            double log2 = Math.log(2.0);
            for (int k = 0; k < bins.length; ++k) {
                double val = bins[k];
                double level = layer.pixelToRealValue((double)k * binFactor + offset) - mean;
                m2 += val * Math.pow(level, 2.0);
                skew += val * Math.pow(level, 3.0);
                kurtosis += val * Math.pow(level, 4.0);
                double h = val / sum;
                if (!MathUtil.isDifferentFromZero((double)h)) continue;
                entropy -= h * (Math.log(h) / log2);
            }
            double variance = m2 / (sum - 1.0);
            double stdev = Math.sqrt(variance);
            if (bins.length > 3 && variance > 1.0E-9) {
                skew = sum * skew / ((sum - 1.0) * (sum - 2.0) * stdev * variance);
                kurtosis = (sum * (sum + 1.0) * kurtosis - 3.0 * m2 * m2 * (sum - 1.0)) / ((sum - 1.0) * (sum - 2.0) * (sum - 3.0) * variance * variance);
            } else {
                skew = 0.0;
                kurtosis = 0.0;
            }
            double pixelCount = sum;
            double dispMin = min;
            double dispMax = max;
            double dispMean = mean;
            double dispStd = stdev;
            if (direct != null) {
                pixelCount = direct.count();
                double realA = layer.pixelToRealValue(direct.min());
                double realB = layer.pixelToRealValue(direct.max());
                dispMin = Math.min(realA, realB);
                dispMax = Math.max(realA, realB);
                dispMean = layer.pixelToRealValue(direct.mean());
                double scale = direct.max() > direct.min() ? Math.abs(realB - realA) / (direct.max() - direct.min()) : 1.0;
                dispStd = direct.stdDev() * scale;
            }
            String unit = layer.getPixelValueUnit();
            if (imagePixels) {
                ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_PIXELS, channelIndex, pixelCount, Unit.PIXEL.getAbbreviation());
            }
            ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_MIN, channelIndex, dispMin, unit);
            ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_MAX, channelIndex, dispMax, unit);
            ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_MEDIAN, channelIndex, layer.pixelToRealValue(ImageRegionStatistics.medianBin(bins, sum / 2.0) * binFactor + offset), unit);
            ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_MEAN, channelIndex, dispMean, unit);
            ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_STD, channelIndex, dispStd, null);
            ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_SKEW, channelIndex, skew, null);
            ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_KURTOSIS, channelIndex, kurtosis, null);
            ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_ENTROPY, channelIndex, entropy, null);
            Double suv = (Double)layer.getSourceTagValue(TagW.SuvFactor);
            if (channelIndex == null && Objects.nonNull(suv)) {
                unit = "SUVbw, g/ml";
                ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_MIN, null, dispMin * suv, unit);
                ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_MAX, null, dispMax * suv, unit);
                ImageRegionStatistics.addMeasure(measList, ImageStatistics.IMAGE_MEAN, null, dispMean * suv, unit);
            }
            return measList;
        }
        return Collections.emptyList();
    }

    public static double medianBin(float[] bin, double halfEntries) {
        if (bin == null || bin.length < 1) {
            return 0.0;
        }
        double sumBin = 0.0;
        for (int i = 0; i < bin.length; ++i) {
            double sum = sumBin + (double)bin[i];
            if (sum >= halfEntries) {
                double dif = halfEntries - sumBin;
                double frac = 0.0;
                if (bin[i] > 0.0f) {
                    frac = dif / (double)bin[i];
                }
                return (double)i + frac;
            }
            sumBin = sum;
        }
        return 0.0;
    }

    private record RoiPixelStats(double min, double max, double mean, double stdDev, double count) {
    }
}

