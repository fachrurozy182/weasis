/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 */
package org.weasis.core.ui.model.graphic.imp;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.ui.model.graphic.AbstractGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;

@XmlRootElement(name="nonEditable")
public class NonEditableGraphic
extends AbstractGraphic {
    private Stroke stroke;

    public NonEditableGraphic(Shape path) {
        this(path, null);
    }

    public NonEditableGraphic(Shape path, Stroke stroke) {
        super(0);
        this.stroke = stroke;
        this.setShape(path, null);
        this.updateLabel(null, null);
    }

    public NonEditableGraphic(NonEditableGraphic graphic) {
        super(graphic);
    }

    @Override
    protected void initCopy(Graphic graphic) {
        super.initCopy(graphic);
        if (graphic instanceof NonEditableGraphic) {
            NonEditableGraphic nonEditableGraphic = (NonEditableGraphic)graphic;
            this.stroke = nonEditableGraphic.stroke;
        }
    }

    @Override
    public NonEditableGraphic copy() {
        return new NonEditableGraphic(this);
    }

    @Override
    public void setFilled(Boolean filled) {
        if (!Objects.equals(this.filled, filled)) {
            this.filled = Optional.ofNullable(filled).orElse(DEFAULT_FILLED);
            this.fireDrawingChanged();
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
        this.updateLabel(null, null);
    }

    @Override
    public String getUIName() {
        return "";
    }

    @Override
    public boolean isOnGraphicLabel(MouseEventDouble mouseevent) {
        return false;
    }

    @Override
    public String getDescription() {
        return this.getUIName();
    }

    @Override
    public Area getArea(AffineTransform transform) {
        return new Area();
    }

    public Stroke getStroke() {
        return this.stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    @Override
    public Stroke getStroke(Float lineThickness) {
        if (this.stroke != null) {
            return this.stroke;
        }
        return super.getStroke(lineThickness);
    }
}

