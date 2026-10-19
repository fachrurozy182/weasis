/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.seg.RegionAttributes
 */
package org.weasis.core.ui.model.graphic.imp.seg;

import java.awt.Color;
import java.util.List;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.util.Copyable;
import org.weasis.core.ui.model.graphic.imp.seg.SegMeasurableLayer;
import org.weasis.core.ui.util.StructToolTipTreeNode;
import org.weasis.core.util.StringUtil;
import org.weasis.opencv.seg.RegionAttributes;

public class SegRegion<E extends ImageElement>
extends RegionAttributes
implements Copyable<SegRegion<E>> {
    private SegMeasurableLayer<E> measurableLayer;
    private boolean selected;
    private String algorithmName;
    private List<String> anatomicRegionCodes;
    private List<String> categories;
    private String fractionalType;

    public SegRegion(int id, String label, Color color) {
        super(id, label, color);
        this.selected = true;
        this.resetPixelCount();
    }

    public SegRegion(SegRegion<E> region) {
        super(region.getId(), region.getLabel(), new Color(region.getColor().getRGB()));
        this.copyAttributesFrom(region);
    }

    private void copyAttributesFrom(SegRegion<E> other) {
        this.setDescription(other.getDescription());
        this.setType(other.getType());
        this.setFilled(other.isFilled());
        this.setLineThickness(other.getLineThickness());
        this.setVisible(other.isVisible());
        this.setInteriorOpacity(other.getInteriorOpacity());
        this.algorithmName = other.algorithmName;
        this.anatomicRegionCodes = other.anatomicRegionCodes;
        this.categories = other.categories;
        this.fractionalType = other.fractionalType;
        this.numberOfPixels = other.numberOfPixels;
        this.selected = other.selected;
        this.measurableLayer = other.measurableLayer;
    }

    public SegMeasurableLayer<E> getMeasurableLayer() {
        return this.measurableLayer;
    }

    public void setMeasurableLayer(SegMeasurableLayer<E> measurableLayer) {
        this.measurableLayer = measurableLayer;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setNumberOfPixels(long numberOfPixels) {
        this.numberOfPixels = numberOfPixels;
    }

    @Override
    public SegRegion<E> copy() {
        return new SegRegion<E>(this);
    }

    public String getAlgorithmName() {
        return this.algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void setAnatomicRegionCodes(List<String> anatomicRegions) {
        this.anatomicRegionCodes = anatomicRegions == null ? null : List.copyOf(anatomicRegions);
    }

    public List<String> getAnatomicRegionCodes() {
        return this.anatomicRegionCodes;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories == null ? null : List.copyOf(categories);
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public String getFractionalType() {
        return this.fractionalType;
    }

    public void setFractionalType(String fractionalType) {
        this.fractionalType = fractionalType;
    }

    public boolean isFractional() {
        return this.fractionalType != null;
    }

    public String getToolTipHtml() {
        StringBuilder buf = new StringBuilder();
        buf.append("<html>").append("<b>").append(this.getLabel()).append("</b>");
        buf.append("<br>");
        SegRegion.appendLine(buf, "Algorithm type", this.getType());
        SegRegion.appendLine(buf, "Algorithm name", this.algorithmName);
        SegRegion.appendList(buf, "Categories", this.categories);
        SegRegion.appendList(buf, "Anatomic regions", this.anatomicRegionCodes);
        this.appendVoxelCount(buf);
        this.appendVolume(buf);
        if (this.isFractional()) {
            this.appendFractionalLut(buf);
        }
        buf.append("</html>");
        return buf.toString();
    }

    protected static void appendLine(StringBuilder buf, String label, String value) {
        if (StringUtil.hasText((CharSequence)value)) {
            buf.append(label).append(": ").append(value).append("<br>");
        }
    }

    protected static void appendList(StringBuilder buf, String label, List<String> values) {
        if (values != null && !values.isEmpty()) {
            buf.append(label).append(": ").append(String.join((CharSequence)", ", values)).append("<br>");
        }
    }

    private void appendVoxelCount(StringBuilder buf) {
        buf.append("Voxel count");
        if (this.isFractional()) {
            buf.append(" (weighted");
            if (StringUtil.hasText((CharSequence)this.fractionalType)) {
                buf.append(", ").append(this.fractionalType.toLowerCase());
            }
            buf.append(")");
        }
        buf.append(": ").append(DecFormatter.allNumber(this.getNumberOfPixels())).append("<br>");
    }

    private void appendVolume(StringBuilder buf) {
        if (this.measurableLayer == null) {
            return;
        }
        MeasurementsAdapter adapter = this.measurableLayer.getMeasurementAdapter(((ImageElement)this.measurableLayer.getSourceImage()).getPixelSpacingUnit());
        double ratio = adapter.calibrationRatio();
        buf.append("Volume (%s3)".formatted(new Object[]{adapter.unit()})).append(": ").append(DecFormatter.twoDecimal((double)this.getNumberOfPixels() * ratio * ratio * this.measurableLayer.getThickness())).append("<br>");
    }

    private void appendFractionalLut(StringBuilder buf) {
        boolean occupancy = "OCCUPANCY".equalsIgnoreCase(this.fractionalType);
        String unit = occupancy ? " %" : "";
        String minLabel = "0" + unit;
        String maxLabel = (occupancy ? "100" : "1") + unit;
        buf.append("LUT").append(": ").append("<br>").append(StructToolTipTreeNode.buildHorizontalLutBar(this.getColor(), 24, minLabel, maxLabel));
    }
}

