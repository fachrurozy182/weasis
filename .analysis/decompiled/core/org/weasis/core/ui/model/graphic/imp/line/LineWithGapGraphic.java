/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAttribute
 *  jakarta.xml.bind.annotation.XmlElement
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 *  jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
 */
package org.weasis.core.ui.model.graphic.imp.line;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Optional;
import javax.swing.Icon;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.imp.line.LineGraphic;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.serialize.PointAdapter;
import org.weasis.core.ui.util.MouseEventDouble;

@XmlType(name="lineWithGap")
@XmlRootElement(name="lineWithGap")
public class LineWithGapGraphic
extends AbstractDragGraphic {
    public static final Integer POINTS_NUMBER = 2;
    public static final Integer DEFAULT_GAP_SIZE = 0;
    protected Integer gapSize = DEFAULT_GAP_SIZE;
    protected Point2D ptA;
    protected Point2D ptB;
    protected Point2D centerGap;
    protected Boolean lineABvalid;

    public LineWithGapGraphic() {
        super(POINTS_NUMBER);
    }

    public LineWithGapGraphic(LineWithGapGraphic graphic) {
        super(graphic);
    }

    @Override
    public LineWithGapGraphic copy() {
        return new LineWithGapGraphic(this);
    }

    @Override
    protected void initCopy(Graphic graphic) {
        super.initCopy(graphic);
        this.gapSize = ((LineWithGapGraphic)graphic).gapSize;
    }

    @Override
    protected void prepareShape() throws InvalidShapeException {
        if (!this.isShapeValid()) {
            throw new InvalidShapeException("This shape cannot be drawn");
        }
        this.buildShape(null);
    }

    @Override
    public boolean isShapeValid() {
        this.updateTool();
        return super.isShapeValid();
    }

    @XmlElement(name="centerGap")
    @XmlJavaTypeAdapter(value=PointAdapter.Point2DAdapter.class)
    public Point2D getCenterGap() {
        return this.centerGap;
    }

    public void setCenterGap(Point2D centerGap) {
        this.centerGap = centerGap;
    }

    @XmlAttribute(name="gapSize")
    public Integer getGapSize() {
        return this.gapSize;
    }

    public void setGapSize(Integer gapSize) {
        this.gapSize = Optional.ofNullable(gapSize).orElse(DEFAULT_GAP_SIZE);
    }

    protected void setHandlePointList(Point2D ptStart, Point2D ptEnd) {
        this.setHandlePoint(0, ptStart == null ? null : (Point2D)ptStart.clone());
        this.setHandlePoint(1, ptEnd == null ? null : (Point2D)ptEnd.clone());
        this.buildShape(null);
    }

    @Override
    public Icon getIcon() {
        return LineGraphic.ICON;
    }

    @Override
    public String getUIName() {
        return "";
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        Path2D.Double newShape = null;
        if (this.lineABvalid.booleanValue()) {
            if (this.centerGap == null) {
                this.centerGap = GeomUtil.getCollinearPointWithRatio(this.ptA, this.ptB, 0.5);
            }
            double dist = this.ptA.distance(this.ptB);
            double distCenterGap = this.ptA.distance(this.centerGap);
            double distCenterB = this.ptB.distance(this.centerGap);
            Point2D ptap = null;
            Point2D ptbp = null;
            if (distCenterGap < dist && distCenterB < dist) {
                double distGap = 0.5 * (double)this.gapSize.intValue() / dist;
                ptap = GeomUtil.getCollinearPointWithRatio(this.ptA, this.ptB, distCenterGap / dist - distGap);
                ptbp = GeomUtil.getCollinearPointWithRatio(this.ptA, this.ptB, distCenterGap / dist + distGap);
            }
            Path2D.Double path = new Path2D.Double(1, 4);
            ((Path2D)path).moveTo(this.ptA.getX(), this.ptA.getY());
            if (ptap != null) {
                ((Path2D)path).lineTo(ptap.getX(), ptap.getY());
                ((Path2D)path).moveTo(ptbp.getX(), ptbp.getY());
            }
            ((Path2D)path).lineTo(this.ptB.getX(), this.ptB.getY());
            newShape = path;
        }
        this.setShape(newShape, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    protected void updateTool() {
        this.ptA = this.getHandlePoint(0);
        this.ptB = this.getHandlePoint(1);
        this.lineABvalid = this.ptA != null && this.ptB != null && !this.ptB.equals(this.ptA);
    }

    public Point2D getStartPoint() {
        this.updateTool();
        return this.ptA == null ? null : (Point2D)this.ptA.clone();
    }

    public Point2D getEndPoint() {
        this.updateTool();
        return this.ptB == null ? null : (Point2D)this.ptB.clone();
    }
}

