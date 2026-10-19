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
import java.awt.geom.Point2D;
import java.util.Objects;

public class PointAdapter {
    private PointAdapter() {
    }

    public static class Point2DAdapter
    extends XmlAdapter<Pt, Point2D> {
        public Pt marshal(Point2D v) throws Exception {
            if (Objects.isNull(v)) {
                return null;
            }
            Pt p = new Pt();
            p.x = v.getX();
            p.y = v.getY();
            return p;
        }

        public Point2D unmarshal(Pt v) throws Exception {
            return new Point2D.Double(v.x, v.y);
        }
    }

    static class Pt {
        @XmlAttribute(required=true)
        double x;
        @XmlAttribute(required=true)
        double y;

        Pt() {
        }
    }
}

