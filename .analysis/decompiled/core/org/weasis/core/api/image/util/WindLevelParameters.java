/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.op.lut.DefaultWlPresentation
 *  org.weasis.opencv.op.lut.LutShape
 *  org.weasis.opencv.op.lut.PresentationStateLut
 *  org.weasis.opencv.op.lut.WlParams
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.weasis.core.api.image.util;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.PseudoColorOp;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.opencv.op.lut.DefaultWlPresentation;
import org.weasis.opencv.op.lut.LutShape;
import org.weasis.opencv.op.lut.PresentationStateLut;
import org.weasis.opencv.op.lut.WlParams;
import org.weasis.opencv.op.lut.WlPresentation;

public final class WindLevelParameters
implements WlParams {
    private static final String PRESENTATION_STATE_KEY = "pr.element";
    private final double window;
    private final double level;
    private final double levelMin;
    private final double levelMax;
    private final boolean pixelPadding;
    private final boolean inverseLut;
    private final boolean fillOutsideLutRange;
    private final boolean allowWinLevelOnColorImage;
    private final LutShape lutShape;
    private final PresentationStateLut presentationStateLut;

    public WindLevelParameters(ImageElement imageElement, Map<String, Object> params) {
        Objects.requireNonNull(imageElement, "Image element cannot be null");
        ParameterExtractor extractor = new ParameterExtractor(params);
        DefaultWlPresentation presentation = this.createPresentation(extractor);
        this.presentationStateLut = extractor.getPresentationState();
        this.pixelPadding = extractor.getPixelPadding();
        this.inverseLut = extractor.getInverseLut();
        this.fillOutsideLutRange = extractor.getFillOutsideLutRange();
        this.allowWinLevelOnColorImage = extractor.getAllowWinLevelOnColorImage();
        this.window = extractor.getWindow().orElseGet(() -> imageElement.getDefaultWindow((WlPresentation)presentation));
        this.level = extractor.getLevel().orElseGet(() -> imageElement.getDefaultLevel((WlPresentation)presentation));
        this.lutShape = extractor.getLutShape().orElseGet(() -> imageElement.getDefaultShape((WlPresentation)presentation));
        LevelRange levelRange = this.calculateLevelRange(extractor, imageElement, presentation);
        this.levelMin = levelRange.min();
        this.levelMax = levelRange.max();
    }

    private WindLevelParameters(Builder builder) {
        this.window = builder.window;
        this.level = builder.level;
        this.levelMin = builder.levelMin;
        this.levelMax = builder.levelMax;
        this.pixelPadding = builder.pixelPadding;
        this.inverseLut = builder.inverseLut;
        this.fillOutsideLutRange = builder.fillOutsideLutRange;
        this.allowWinLevelOnColorImage = builder.allowWinLevelOnColorImage;
        this.lutShape = builder.lutShape;
        this.presentationStateLut = builder.presentationStateLut;
    }

    public double getWindow() {
        return this.window;
    }

    public double getLevel() {
        return this.level;
    }

    public double getLevelMin() {
        return this.levelMin;
    }

    public double getLevelMax() {
        return this.levelMax;
    }

    public boolean isPixelPadding() {
        return this.pixelPadding;
    }

    public boolean isInverseLut() {
        return this.inverseLut;
    }

    public boolean isFillOutsideLutRange() {
        return this.fillOutsideLutRange;
    }

    public boolean isAllowWinLevelOnColorImage() {
        return this.allowWinLevelOnColorImage;
    }

    public LutShape getLutShape() {
        return this.lutShape;
    }

    public PresentationStateLut getPresentationState() {
        return this.presentationStateLut;
    }

    public double[] getWindowRange() {
        double halfWindow = this.window / 2.0;
        return new double[]{this.level - halfWindow, this.level + halfWindow};
    }

    public double[] getLevelRange() {
        return new double[]{this.levelMin, this.levelMax};
    }

    public boolean isValid() {
        return Double.isFinite(this.window) && this.window > 0.0 && Double.isFinite(this.level) && Double.isFinite(this.levelMin) && Double.isFinite(this.levelMax) && this.levelMin <= this.levelMax && this.lutShape != null;
    }

    public WindLevelParameters withWindowLevel(double newWindow, double newLevel) {
        return WindLevelParameters.builder().from(this).window(newWindow).level(newLevel).build();
    }

    public WindLevelParameters withLutShape(LutShape newLutShape) {
        return WindLevelParameters.builder().from(this).lutShape(newLutShape).build();
    }

    public String toString() {
        return "WindLevelParameters[window=%.2f, level=%.2f, range=[%.2f-%.2f], pixelPadding=%s, inverseLut=%s, lutShape=%s]".formatted(new Object[]{this.window, this.level, this.levelMin, this.levelMax, this.pixelPadding, this.inverseLut, this.lutShape});
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof WindLevelParameters)) return false;
        WindLevelParameters that = (WindLevelParameters)obj;
        if (Double.compare(that.window, this.window) != 0) return false;
        if (Double.compare(that.level, this.level) != 0) return false;
        if (Double.compare(that.levelMin, this.levelMin) != 0) return false;
        if (Double.compare(that.levelMax, this.levelMax) != 0) return false;
        if (this.pixelPadding != that.pixelPadding) return false;
        if (this.inverseLut != that.inverseLut) return false;
        if (this.fillOutsideLutRange != that.fillOutsideLutRange) return false;
        if (this.allowWinLevelOnColorImage != that.allowWinLevelOnColorImage) return false;
        if (!Objects.equals(this.lutShape, that.lutShape)) return false;
        if (!Objects.equals(this.presentationStateLut, that.presentationStateLut)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.window, this.level, this.levelMin, this.levelMax, this.pixelPadding, this.inverseLut, this.fillOutsideLutRange, this.allowWinLevelOnColorImage, this.lutShape, this.presentationStateLut);
    }

    private DefaultWlPresentation createPresentation(ParameterExtractor extractor) {
        return new DefaultWlPresentation(extractor.getPresentationState(), extractor.getPixelPadding());
    }

    private LevelRange calculateLevelRange(ParameterExtractor extractor, ImageElement imageElement, DefaultWlPresentation presentation) {
        double imageMin = imageElement.getMinValue((WlPresentation)presentation);
        double imageMax = imageElement.getMaxValue((WlPresentation)presentation);
        return extractor.getLevelMin().flatMap(min -> extractor.getLevelMax().map(max -> new LevelRange(Math.min(min, imageMin), Math.max(max, imageMax)))).orElseGet(() -> {
            double halfWindow = this.window / 2.0;
            return new LevelRange(Math.min(this.level - halfWindow, imageMin), Math.max(this.level + halfWindow, imageMax));
        });
    }

    public static Builder builder() {
        return new Builder();
    }

    private record ParameterExtractor(Map<String, Object> params) {
        private <T> Optional<T> getParam(String key, Class<T> type) {
            return Optional.ofNullable(this.params).map(p -> p.get(key)).filter(type::isInstance).map(type::cast);
        }

        Optional<Double> getWindow() {
            return this.getParam(ActionW.WINDOW.cmd(), Double.class);
        }

        Optional<Double> getLevel() {
            return this.getParam(ActionW.LEVEL.cmd(), Double.class);
        }

        Optional<Double> getLevelMin() {
            return this.getParam(ActionW.LEVEL_MIN.cmd(), Double.class);
        }

        Optional<Double> getLevelMax() {
            return this.getParam(ActionW.LEVEL_MAX.cmd(), Double.class);
        }

        Optional<LutShape> getLutShape() {
            return this.getParam(ActionW.LUT_SHAPE.cmd(), LutShape.class);
        }

        boolean getPixelPadding() {
            return this.getParam(ActionW.IMAGE_PIX_PADDING.cmd(), Boolean.class).orElse(true);
        }

        boolean getInverseLut() {
            return this.getParam(PseudoColorOp.P_LUT_INVERSE, Boolean.class).orElse(false);
        }

        boolean getFillOutsideLutRange() {
            return this.getParam("fill.outside.lut", Boolean.class).orElse(false);
        }

        boolean getAllowWinLevelOnColorImage() {
            return this.getParam("weasis.color.wl.apply", Boolean.class).orElse(false);
        }

        PresentationStateLut getPresentationState() {
            return this.getParam(WindLevelParameters.PRESENTATION_STATE_KEY, PresentationStateLut.class).orElse(null);
        }
    }

    private record LevelRange(double min, double max) {
        LevelRange {
            if (!Double.isFinite(min) || !Double.isFinite(max) || min > max) {
                throw new IllegalArgumentException("Invalid level range: [%f, %f]".formatted(new Object[]{min, max}));
            }
        }
    }

    public static class Builder {
        private double window = 1.0;
        private double level = 0.0;
        private double levelMin = 0.0;
        private double levelMax = 1.0;
        private boolean pixelPadding = true;
        private boolean inverseLut = false;
        private boolean fillOutsideLutRange = false;
        private boolean allowWinLevelOnColorImage = false;
        private LutShape lutShape = LutShape.LINEAR;
        private PresentationStateLut presentationStateLut = null;

        public Builder window(double window) {
            this.window = window;
            return this;
        }

        public Builder level(double level) {
            this.level = level;
            return this;
        }

        public Builder levelMin(double levelMin) {
            this.levelMin = levelMin;
            return this;
        }

        public Builder levelMax(double levelMax) {
            this.levelMax = levelMax;
            return this;
        }

        public Builder pixelPadding(boolean pixelPadding) {
            this.pixelPadding = pixelPadding;
            return this;
        }

        public Builder inverseLut(boolean inverseLut) {
            this.inverseLut = inverseLut;
            return this;
        }

        public Builder fillOutsideLutRange(boolean fillOutsideLutRange) {
            this.fillOutsideLutRange = fillOutsideLutRange;
            return this;
        }

        public Builder allowWinLevelOnColorImage(boolean allowWinLevelOnColorImage) {
            this.allowWinLevelOnColorImage = allowWinLevelOnColorImage;
            return this;
        }

        public Builder lutShape(LutShape lutShape) {
            this.lutShape = lutShape;
            return this;
        }

        public Builder presentationStateLut(PresentationStateLut presentationStateLut) {
            this.presentationStateLut = presentationStateLut;
            return this;
        }

        public Builder from(WindLevelParameters params) {
            this.window = params.window;
            this.level = params.level;
            this.levelMin = params.levelMin;
            this.levelMax = params.levelMax;
            this.pixelPadding = params.pixelPadding;
            this.inverseLut = params.inverseLut;
            this.fillOutsideLutRange = params.fillOutsideLutRange;
            this.allowWinLevelOnColorImage = params.allowWinLevelOnColorImage;
            this.lutShape = params.lutShape;
            this.presentationStateLut = params.presentationStateLut;
            return this;
        }

        public WindLevelParameters build() {
            return new WindLevelParameters(this);
        }
    }
}

