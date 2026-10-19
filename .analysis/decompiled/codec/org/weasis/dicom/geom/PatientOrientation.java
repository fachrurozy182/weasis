/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.geom;

import java.awt.Color;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.geom.Orientation;
import org.weasis.dicom.geom.Vector3;

public final class PatientOrientation {
    private static final Logger LOGGER = LoggerFactory.getLogger(PatientOrientation.class);
    public static final Color BLUE = new Color(44783);
    public static final Color RED = new Color(15539236);
    public static final Color GREEN = new Color(897355);
    public static final String VECTOR_CANNOT_BE_NULL = "Vector cannot be null";

    private PatientOrientation() {
    }

    public static Biped getBipedXOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.x() < 0.0 ? Biped.R : Biped.L;
    }

    public static Biped getBipedYOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.y() < 0.0 ? Biped.A : Biped.P;
    }

    public static Biped getBipedZOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.z() < 0.0 ? Biped.F : Biped.H;
    }

    public static Quadruped getQuadrupedXOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.x() < 0.0 ? Quadruped.RT : Quadruped.LE;
    }

    public static Quadruped getQuadrupedYOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.y() < 0.0 ? Quadruped.V : Quadruped.D;
    }

    public static Quadruped getQuadrupedZOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.z() < 0.0 ? Quadruped.CD : Quadruped.CR;
    }

    public static Orientation getAxisOrientation(Vector3 vector, Axis axis, boolean quadruped) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        Objects.requireNonNull(axis, "Axis cannot be null");
        return switch (axis) {
            default -> throw new IncompatibleClassChangeError();
            case Axis.X -> {
                if (quadruped) {
                    yield PatientOrientation.getQuadrupedXOrientation(vector);
                }
                yield PatientOrientation.getBipedXOrientation(vector);
            }
            case Axis.Y -> {
                if (quadruped) {
                    yield PatientOrientation.getQuadrupedYOrientation(vector);
                }
                yield PatientOrientation.getBipedYOrientation(vector);
            }
            case Axis.Z -> quadruped ? PatientOrientation.getQuadrupedZOrientation(vector) : PatientOrientation.getBipedZOrientation(vector);
        };
    }

    public static Biped getOppositeOrientation(Biped orientation) {
        Objects.requireNonNull(orientation, "Orientation cannot be null");
        return switch (orientation) {
            default -> throw new IncompatibleClassChangeError();
            case Biped.R -> Biped.L;
            case Biped.L -> Biped.R;
            case Biped.A -> Biped.P;
            case Biped.P -> Biped.A;
            case Biped.F -> Biped.H;
            case Biped.H -> Biped.F;
        };
    }

    public static Quadruped getOppositeOrientation(Quadruped orientation) {
        Objects.requireNonNull(orientation, "Orientation cannot be null");
        return switch (orientation) {
            default -> throw new IncompatibleClassChangeError();
            case Quadruped.RT -> Quadruped.LE;
            case Quadruped.LE -> Quadruped.RT;
            case Quadruped.V -> Quadruped.D;
            case Quadruped.D -> Quadruped.V;
            case Quadruped.CD -> Quadruped.CR;
            case Quadruped.CR -> Quadruped.CD;
        };
    }

    public static enum Biped implements Orientation
    {
        R("Right", BLUE),
        L("Left", BLUE),
        A("Anterior", RED),
        P("Posterior", RED),
        F("Foot", GREEN),
        H("Head", GREEN);

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

        public static Biped fromString(String value) {
            if (!StringUtil.hasText((CharSequence)value)) {
                return null;
            }
            try {
                return Biped.valueOf(value.trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e) {
                LOGGER.debug("Unknown biped orientation: {}", (Object)value);
                return null;
            }
        }
    }

    public static enum Quadruped implements Orientation
    {
        RT("Right", BLUE),
        LE("Left", BLUE),
        V("Ventral", RED),
        D("Dorsal", RED),
        CD("Caudal", GREEN),
        CR("Cranial", GREEN);

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

        public static Quadruped fromString(String value) {
            if (!StringUtil.hasText((CharSequence)value)) {
                return null;
            }
            try {
                return Quadruped.valueOf(value.trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e) {
                LOGGER.debug("Unknown quadruped orientation: {}", (Object)value);
                return null;
            }
        }
    }

    public static enum Axis {
        X,
        Y,
        Z;

    }
}

