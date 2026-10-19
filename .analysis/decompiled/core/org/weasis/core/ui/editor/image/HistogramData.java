/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.opencv.core.MatOfFloat
 *  org.opencv.core.MatOfInt
 *  org.opencv.imgproc.Imgproc
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.lut.ByteLut
 *  org.weasis.opencv.op.lut.ColorLut
 *  org.weasis.opencv.op.lut.PresentationStateLut
 *  org.weasis.opencv.op.lut.WlParams
 */
package org.weasis.core.ui.editor.image;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;
import org.weasis.core.Messages;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.WindLevelParameters;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.DisplayByteLut;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.lut.ByteLut;
import org.weasis.opencv.op.lut.ColorLut;
import org.weasis.opencv.op.lut.PresentationStateLut;
import org.weasis.opencv.op.lut.WlParams;

public class HistogramData {
    private final float[] histValues;
    private final MeasurableLayer layer;
    private final int bandIndex;
    private final double pixMin;
    private final double pixMax;
    private final Model colorModel;
    private WlParams windLevel;
    private DisplayByteLut lut;
    private LookupTableCV voiLut;

    public HistogramData(float[] histValues, DisplayByteLut lut, int bandIndex, Model colorModel, WlParams windLevel, double pixMin, double pixMax, MeasurableLayer layer) {
        this.histValues = Objects.requireNonNull(histValues);
        this.layer = Objects.requireNonNull(layer);
        this.colorModel = colorModel;
        this.lut = lut;
        this.windLevel = windLevel;
        this.bandIndex = bandIndex;
        this.pixMin = pixMin;
        this.pixMax = pixMax;
    }

    public float[] getHistValues() {
        return this.histValues;
    }

    public DisplayByteLut getLut() {
        return this.lut;
    }

    public Model getColorModel() {
        return this.colorModel;
    }

    public void setLut(DisplayByteLut lut) {
        this.lut = lut;
    }

    public WlParams getWindLevel() {
        return this.windLevel;
    }

    public void setWindLevel(WindLevelParameters windLevel) {
        this.windLevel = windLevel;
    }

    public MeasurableLayer getLayer() {
        return this.layer;
    }

    public LookupTableCV getVoiLut() {
        return this.voiLut;
    }

    public void setVoiLut(LookupTableCV voiLut) {
        this.voiLut = voiLut;
    }

    public int getBandIndex() {
        return this.bandIndex;
    }

    public double getPixMin() {
        return this.pixMin;
    }

    public double getPixMax() {
        return this.pixMax;
    }

    public int getFinalVoiLutIndex(Number level) {
        if (this.lut == null || this.windLevel == null) {
            return 0;
        }
        Integer index = null;
        int dynamic = this.lut.getByteLut().lutTable()[0].length - 1;
        int datatype = ImageConversion.convertToDataType((int)this.layer.getSourceRenderedImage().type());
        if (datatype >= 0 && datatype < 3) {
            Optional prLutData;
            PresentationStateLut pr;
            if (this.voiLut != null) {
                int b = this.voiLut.getNumBands() <= this.bandIndex ? 0 : this.bandIndex;
                index = this.voiLut.lookup(b, level.intValue());
            }
            if ((pr = this.windLevel.getPresentationState()) != null && (prLutData = pr.getPrLut()).isPresent()) {
                int val = index == null ? level.intValue() : index.intValue();
                int b = ((LookupTableCV)prLutData.get()).getNumBands() <= this.bandIndex ? 0 : this.bandIndex;
                index = ((LookupTableCV)prLutData.get()).lookup(b, val);
            }
        }
        if (index == null) {
            double low = this.windLevel.getLevel() - this.windLevel.getWindow() / 2.0;
            double high = this.windLevel.getLevel() + this.windLevel.getWindow() / 2.0;
            double range = high - low;
            if (range < 1.0 && datatype == 3) {
                range = 1.0;
            }
            double slope = 255.0 / range;
            double yint = 255.0 - slope * high;
            index = (int)Math.round(level.doubleValue() * slope + yint);
        }
        if (index < 0) {
            index = 0;
        }
        if (index > dynamic) {
            index = dynamic;
        }
        if (this.lut.isInvert()) {
            index = dynamic - index;
        }
        return index;
    }

    public Color getFinalVoiLutColor(Number level) {
        if (this.lut == null) {
            return Color.BLACK;
        }
        int index = this.getFinalVoiLutIndex(level);
        byte[][] table = this.lut.getByteLut().lutTable();
        return new Color(table[2][index] & 0xFF, table[1][index] & 0xFF, table[0][index] & 0xFF);
    }

    public void updateVoiLut(ViewCanvas<? extends ImageElement> view2DPane) {
        if (this.windLevel != null) {
            Object img = view2DPane.getImage();
            PlanarImage imageSource = view2DPane.getSourceImage();
            int datatype = ImageConversion.convertToDataType((int)imageSource.type());
            if (datatype >= 0 && datatype < 3) {
                Optional prLutData = Optional.empty();
                PresentationStateLut pr = this.windLevel.getPresentationState();
                if (pr != null) {
                    prLutData = pr.getPrLut();
                }
                if (prLutData.isEmpty() || this.windLevel.getLutShape().getLookup() != null) {
                    this.setVoiLut(((ImageElement)img).getVOILookup(this.windLevel));
                }
            }
        }
    }

    public static List<Mat> computeHistogram(Mat imageSource, Mat mask, int nbBins, int[] selChannels, Model model, double pixMin, double pixMax) {
        Mat msk;
        Mat img;
        if (selChannels.length == 0) {
            return Collections.emptyList();
        }
        MatOfInt histSize = new MatOfInt(new int[]{nbBins});
        MatOfFloat histRange = new MatOfFloat(new float[]{(float)pixMin, (float)pixMax + 1.0f});
        int cvType = CvType.depth((int)imageSource.type());
        if (cvType == 3 || cvType == 4) {
            Mat floatImage = new Mat(imageSource.height(), imageSource.width(), 5);
            imageSource.convertTo(floatImage, 5);
            img = floatImage;
        } else {
            img = imageSource;
        }
        ArrayList<Mat> channels = new ArrayList<Mat>();
        if (selChannels.length == 1) {
            channels.add(img);
        } else if (Model.RGB == model) {
            Core.split((Mat)img, channels);
            Collections.reverse(channels);
        } else {
            ImageCV dstImg = new ImageCV();
            int code = Model.HSV == model ? 40 : (Model.HLS == model ? 52 : (Model.GRAY == model ? 6 : 4));
            Imgproc.cvtColor((Mat)img, (Mat)dstImg, (int)code);
            Core.split((Mat)dstImg, channels);
        }
        Mat mat = msk = mask == null ? new Mat() : mask;
        if (channels.size() == 1) {
            Mat hist = new Mat();
            Imgproc.calcHist(channels, (MatOfInt)new MatOfInt(new int[]{0}), (Mat)msk, (Mat)hist, (MatOfInt)histSize, (MatOfFloat)histRange, (boolean)false);
            return Collections.singletonList(HistogramData.asColumn(hist));
        }
        ArrayList<Mat> histograms = new ArrayList<Mat>(selChannels.length);
        for (int selChannel : selChannels) {
            Mat hist = new Mat();
            Imgproc.calcHist(channels, (MatOfInt)new MatOfInt(new int[]{selChannel}), (Mat)msk, (Mat)hist, (MatOfInt)histSize, (MatOfFloat)histRange, (boolean)false);
            histograms.add(HistogramData.asColumn(hist));
        }
        return histograms;
    }

    private static Mat asColumn(Mat hist) {
        return hist.rows() == 1 && hist.cols() > 1 ? hist.reshape(0, hist.cols()) : hist;
    }

    public static enum Model {
        GRAY(Messages.getString("HistogramData.lum"), Model.buildLut(ColorLut.GRAY)),
        RGB(Messages.getString("HistogramData.rgb"), Model.buildLut(ColorLut.RED), Model.buildLut(ColorLut.GREEN), Model.buildLut(ColorLut.BLUE)),
        HSV(Messages.getString("HistogramData.hsv"), Model.buildLut(ColorLut.HUE), Model.buildLut(Messages.getString("HistogramData.saturation"), ColorLut.GRAY), Model.buildLut(Messages.getString("HistogramData.val"), ColorLut.GRAY)),
        HLS(Messages.getString("HistogramData.hls"), Model.buildLut(ColorLut.HUE), Model.buildLut(Messages.getString("HistogramData.lightness"), ColorLut.GRAY), Model.buildLut(Messages.getString("HistogramData.saturation"), ColorLut.GRAY));

        private final ByteLut[] byteLut;
        private final String title;

        private Model(String name, ByteLut ... luts) {
            this.title = name + " (" + Arrays.stream(luts).map(ByteLut::name).collect(Collectors.joining(",")) + ")";
            this.byteLut = luts;
        }

        public ByteLut[] getByteLut() {
            return this.byteLut;
        }

        public String getTitle() {
            return this.title;
        }

        public String toString() {
            return this.title;
        }

        private static ByteLut buildLut(ColorLut lut) {
            return Model.buildLut(lut.getName(), lut);
        }

        private static ByteLut buildLut(String name, ColorLut lut) {
            return new ByteLut(name, lut.getByteLut().lutTable());
        }

        private static ByteLut buildLut(String name, byte[][] slut) {
            return new ByteLut(name, slut);
        }
    }
}

