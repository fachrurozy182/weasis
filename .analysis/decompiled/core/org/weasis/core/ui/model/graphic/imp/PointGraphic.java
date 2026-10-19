/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAttribute
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 */
package org.weasis.core.ui.model.graphic.imp;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;
import org.weasis.core.Messages;
import org.weasis.core.ui.model.graphic.AbstractGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;

@XmlType(name="point")
@XmlRootElement(name="point")
public class PointGraphic
extends AbstractGraphic {
    static final Integer DEFAULT_POINT_SIZE = 1;
    private Integer pointSize = DEFAULT_POINT_SIZE;

    public PointGraphic() {
        super(1);
    }

    public PointGraphic(PointGraphic pointGraphic) {
        super(pointGraphic);
    }

    @Override
    public Graphic copy() {
        return new PointGraphic(this);
    }

    @Override
    protected void initCopy(Graphic graphic) {
        if (graphic instanceof PointGraphic) {
            PointGraphic pointGraphic = (PointGraphic)graphic;
            this.setPointSize(pointGraphic.getPointSize());
        }
    }

    @Override
    protected void prepareShape() throws InvalidShapeException {
        if (!this.isShapeValid()) {
            throw new InvalidShapeException("This shape cannot be drawn");
        }
        this.buildShape();
    }

    @Override
    public void buildShape() {
        this.pts.stream().findFirst().ifPresent(p -> {
            Ellipse2D.Double ellipse = new Ellipse2D.Double(p.getX() - (double)((float)this.pointSize.intValue() / 2.0f), p.getY() - (double)((float)this.pointSize.intValue() / 2.0f), this.pointSize.intValue(), this.pointSize.intValue());
            this.setShape(ellipse, null);
            this.updateLabel(null, null);
        });
    }

    @Override
    public String getUIName() {
        return Messages.getString("PointGraphic.point");
    }

    public Point2D getPoint() {
        return this.pts.stream().findFirst().map(p -> (Point2D)p.clone()).orElse(null);
    }

    @XmlAttribute
    public Integer getPointSize() {
        return this.pointSize;
    }

    public void setPointSize(Integer pointSize) {
        this.pointSize = Optional.ofNullable(pointSize).orElse(DEFAULT_POINT_SIZE);
    }
}

