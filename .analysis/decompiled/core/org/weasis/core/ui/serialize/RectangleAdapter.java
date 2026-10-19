/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAttribute
 *  jakarta.xml.bind.annotation.adapters.XmlAdapter
 */
package org.weasis.core.ui.serialize;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

public class RectangleAdapter {
    private RectangleAdapter() {
    }

    public static class Rectangle2DAdapter
    extends XmlAdapter<RectanglePt, Rectangle2D> {
        public RectanglePt marshal(Rectangle2D v) throws Exception {
            if (Objects.isNull(v)) {
                return null;
            }
            RectanglePt p = new RectanglePt();
            p.x = v.getX();
            p.y = v.getY();
            p.width = v.getWidth();
            p.height = v.getHeight();
            return p;
        }

        public Rectangle2D unmarshal(RectanglePt v) throws Exception {
            return new Rectangle2D.Double(v.x, v.y, v.width, v.height);
        }
    }

    static class RectanglePt {
        @XmlAttribute(required=true)
        double x;
        @XmlAttribute(required=true)
        double y;
        @XmlAttribute(required=true)
        double width;
        @XmlAttribute
        public double height;

        RectanglePt() {
        }
    }
}

