/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAttribute
 *  jakarta.xml.bind.annotation.adapters.XmlAdapter
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.serialize;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.awt.Color;
import java.awt.Paint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.service.WProperties;

public class ColorModelAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorModelAdapter.class);

    private ColorModelAdapter() {
    }

    public static class PaintAdapter
    extends XmlAdapter<ColorModel, Paint> {
        public ColorModel marshal(Paint paint) throws Exception {
            ColorModel m = new ColorModel();
            if (paint instanceof Color) {
                Color color = (Color)paint;
                m.rgb = WProperties.color2Hexadecimal(color, true);
            } else {
                LOGGER.warn("The serialization of {} is not supported.", paint.getClass());
            }
            return m;
        }

        public Color unmarshal(ColorModel c) throws Exception {
            return WProperties.hexadecimal2Color(c.rgb);
        }
    }

    public static class ColorAdapter
    extends XmlAdapter<ColorModel, Color> {
        public ColorModel marshal(Color color) throws Exception {
            ColorModel m = new ColorModel();
            m.rgb = WProperties.color2Hexadecimal(color, true);
            return m;
        }

        public Color unmarshal(ColorModel c) throws Exception {
            return WProperties.hexadecimal2Color(c.rgb);
        }
    }

    static class ColorModel {
        @XmlAttribute(required=true)
        String rgb;

        ColorModel() {
        }
    }
}

