/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 */
package org.weasis.core.ui.model.graphic.imp;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.PixelInfo;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.imp.AnnotationGraphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.util.MouseEventDouble;

@XmlType(name="pixelInfo")
@XmlRootElement(name="pixelInfo")
public class PixelInfoGraphic
extends AnnotationGraphic {
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_PIXEL_INFO);
    public static final Measurement ANCHOR_POINT_X = new Measurement(Messages.getString("PixelInfoGraphic.x"), 1, true, true, false);
    public static final Measurement ANCHOR_POINT_Y = new Measurement(Messages.getString("PixelInfoGraphic.y"), 2, true, true, false);
    private PixelInfo pixelInfo;

    public PixelInfoGraphic() {
    }

    public PixelInfoGraphic(PixelInfoGraphic graphic) {
        super(graphic);
    }

    public PixelInfo getPixelInfo() {
        return this.pixelInfo;
    }

    public void setPixelInfo(PixelInfo pixelInfo) {
        this.pixelInfo = pixelInfo;
    }

    @Override
    public PixelInfoGraphic copy() {
        return new PixelInfoGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("Tools.pixelInfo");
    }

    @Override
    public int getKeyCode() {
        return 0;
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        AdvancedShape newShape = null;
        ViewCanvas view2d = this.getDefaultView2d(mouseEvent);
        if (this.ptBox != null) {
            if (this.labels == null) {
                if (view2d != null) {
                    this.setLabel(null, view2d, this.ptBox);
                    return;
                }
                if (this.labelHeight == 0.0 || this.labelWidth == 0.0) {
                    this.fireRemoveAction();
                    return;
                }
            }
            if (view2d != null && this.ptAnchor != null) {
                this.setLabel(this.labels, view2d, this.ptBox);
                return;
            }
            newShape = new AdvancedShape(this, 2);
            Line2D.Double line = null;
            if (this.lineABvalid.booleanValue()) {
                line = new Line2D.Double(this.ptBox, this.ptAnchor);
            }
            this.labelBounds = new Rectangle2D.Double();
            this.labelBounds.setFrameFromCenter(this.ptBox.getX(), this.ptBox.getY(), this.ptBox.getX() + this.labelWidth / 2.0 + 3.0, this.ptBox.getY() + this.labelHeight * (double)(this.labels == null ? 1 : this.labels.length) / 2.0 + 3.0);
            GeomUtil.growRectangle(this.labelBounds, 3.0);
            if (line != null) {
                newShape.addLinkSegmentToInvariantShape(line, this.ptBox, this.labelBounds, this.getDashStroke(this.lineThickness), true);
                AdvancedShape.ScaleInvariantShape arrow = newShape.addScaleInvShape(GeomUtil.getArrowShape(this.ptAnchor, this.ptBox, 15.0, 8.0), this.ptAnchor, this.getStroke(this.lineThickness), true);
                arrow.setFilled(true);
            }
            newShape.addAllInvShape(this.labelBounds, this.ptBox, this.getStroke(this.lineThickness), true);
        }
        this.setShape(newShape, mouseEvent);
    }

    @Override
    public void setLabel(String[] labels, ViewCanvas<?> view2d, Point2D pos) {
        String[] lbs = null;
        if (view2d != null && this.ptAnchor != null) {
            this.pixelInfo = view2d.getPixelInfo(new Point((int)Math.floor(this.ptAnchor.getX()), (int)Math.floor(this.ptAnchor.getY())));
            if (this.pixelInfo != null) {
                lbs = new String[]{this.pixelInfo.getPixelValueText()};
            }
        } else {
            lbs = labels;
        }
        if (lbs == null) {
            lbs = new String[]{Messages.getString("PixelInfoGraphic.no_val")};
        }
        super.setLabel(lbs, view2d, pos);
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            String unit;
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>();
            if (ANCHOR_POINT_X.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(ANCHOR_POINT_X, adapter.getXCalibratedValue(this.ptAnchor.getX()), adapter.unit()));
            }
            if (ANCHOR_POINT_Y.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(ANCHOR_POINT_Y, adapter.getYCalibratedValue(this.ptAnchor.getY()), adapter.unit()));
            }
            if (!(unit = Unit.PIXEL.getAbbreviation()).equals(adapter.unit())) {
                if (ANCHOR_POINT_X.getComputed().booleanValue()) {
                    measVal.add(new MeasureItem(ANCHOR_POINT_X, this.ptAnchor.getX() + (double)adapter.offsetX(), unit));
                }
                if (ANCHOR_POINT_Y.getComputed().booleanValue()) {
                    measVal.add(new MeasureItem(ANCHOR_POINT_Y, this.ptAnchor.getY() + (double)adapter.offsetY(), unit));
                }
            }
            if (Objects.nonNull(this.pixelInfo)) {
                double[] values = this.pixelInfo.getValues();
                String[] channelNames = this.pixelInfo.getChannelNames();
                if (Objects.nonNull(values)) {
                    for (int i = 0; i < values.length; ++i) {
                        Measurement m = new Measurement(channelNames == null || i >= channelNames.length ? Messages.getString("PixelInfoGraphic.unknown") : channelNames[i], i + 2, true, true, false);
                        measVal.add(new MeasureItem(m, values[i], this.pixelInfo.getPixelValueUnit()));
                    }
                }
            }
            return measVal;
        }
        return Collections.emptyList();
    }
}

