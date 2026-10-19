/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  org.joml.Vector3d
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec.geometry;

import java.awt.Color;
import java.util.Objects;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.geometry.Orientation;

public class PatientOrientation {
    private static final Logger LOGGER = LoggerFactory.getLogger(PatientOrientation.class);
    public static final Color blue = new Color(44783);
    public static final Color red = new Color(15539236);
    public static final Color green = new Color(897355);

    public static Biped getBipedXOrientation(Vector3d v) {
        return v.x < 0.0 ? Biped.R : Biped.L;
    }

    public static Biped getBipedYOrientation(Vector3d v) {
        return v.y < 0.0 ? Biped.A : Biped.P;
    }

    public static Biped getBipedZOrientation(Vector3d v) {
        return v.z < 0.0 ? Biped.F : Biped.H;
    }

    public static Quadruped getQuadrupedXOrientation(Vector3d v) {
        return v.x < 0.0 ? Quadruped.RT : Quadruped.LE;
    }

    public static Quadruped getQuadrupedYOrientation(Vector3d v) {
        return v.y < 0.0 ? Quadruped.V : Quadruped.D;
    }

    public static Quadruped getQuadrupedZOrientation(Vector3d v) {
        return v.z < 0.0 ? Quadruped.CD : Quadruped.CR;
    }

    public static Biped getOppositeOrientation(Biped val) {
        return switch (val.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Biped.L;
            case 1 -> Biped.R;
            case 2 -> Biped.P;
            case 3 -> Biped.A;
            case 4 -> Biped.H;
            case 5 -> Biped.F;
        };
    }

    public static Quadruped getOppositeOrientation(Quadruped val) {
        return switch (val.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Quadruped.LE;
            case 1 -> Quadruped.RT;
            case 2 -> Quadruped.D;
            case 3 -> Quadruped.V;
            case 4 -> Quadruped.CR;
            case 5 -> Quadruped.CD;
        };
    }

    public static Vector3d getPatientPosition(TagReadable taggable) {
        double[] patientPosition = TagD.getTagValue(Objects.requireNonNull(taggable), 0x200032, double[].class);
        if (patientPosition != null && patientPosition.length == 3) {
            return new Vector3d(patientPosition);
        }
        return null;
    }

    public static enum Biped implements Orientation
    {
        R("Right", blue),
        L("Left", blue),
        A("Anterior", red),
        P("Posterior", red),
        F("Foot", green),
        H("Head", green);

        private final String fullName;
        private final Color color;

        private Biped(String fullName, Color color) {
            this.fullName = fullName;
            this.color = color;
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        public String toString() {
            return this.fullName;
        }

        @Override
        public Color getColor() {
            return this.color;
        }

        public static Biped getBiped(String val) {
            if (StringUtil.hasText((CharSequence)val)) {
                try {
                    return Biped.valueOf(val);
                }
                catch (Exception e) {
                    LOGGER.error("Cannot find Quadruped: {}", (Object)val);
                }
            }
            return null;
        }
    }

    public static enum Quadruped implements Orientation
    {
        RT("Right", blue),
        LE("Left", blue),
        V("Ventral", red),
        D("Dorsal", red),
        CD("Caudal", green),
        CR("Cranial", green);

        private final String fullName;
        private final Color color;

        private Quadruped(String fullName, Color color) {
            this.fullName = fullName;
            this.color = color;
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        public String toString() {
            return this.fullName;
        }

        @Override
        public Color getColor() {
            return this.color;
        }

        public static Quadruped getQuadruped(String val) {
            if (StringUtil.hasText((CharSequence)val)) {
                try {
                    return Quadruped.valueOf(val);
                }
                catch (Exception e) {
                    LOGGER.error("Cannot find Quadruped: {}", (Object)val, (Object)e);
                }
            }
            return null;
        }
    }
}

