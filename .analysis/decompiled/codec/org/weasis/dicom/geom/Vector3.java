/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.geom;

import java.util.Objects;

public record Vector3(double x, double y, double z) {
    public static final Vector3 ZERO = new Vector3(0.0, 0.0, 0.0);
    public static final Vector3 UNIT_X = new Vector3(1.0, 0.0, 0.0);
    public static final Vector3 UNIT_Y = new Vector3(0.0, 1.0, 0.0);
    public static final Vector3 UNIT_Z = new Vector3(0.0, 0.0, 1.0);
    private static final double EPSILON = 1.0E-10;
    private static final String MESSAGE = "Other vector cannot be null";

    public Vector3 {
        x = Vector3.normalizeZero(x);
        y = Vector3.normalizeZero(y);
        z = Vector3.normalizeZero(z);
    }

    private static double normalizeZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    private static double normalizeZeroWithEpsilon(double value) {
        return Math.abs(value) < 1.0E-10 ? 0.0 : value;
    }

    public static Vector3 of(double[] coords) {
        return Vector3.of(coords, 0);
    }

    public static Vector3 of(double[] coords, int offset) {
        Objects.requireNonNull(coords, "Coordinate array cannot be null");
        Vector3.validateArrayBounds(coords, offset);
        return new Vector3(coords[offset], coords[offset + 1], coords[offset + 2]);
    }

    public double magnitude() {
        return Math.sqrt(this.magnitudeSquared());
    }

    public double magnitudeSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double dot(Vector3 other) {
        Objects.requireNonNull(other, MESSAGE);
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3 normalize() {
        double mag = this.magnitude();
        if (Vector3.isZero(mag)) {
            return ZERO;
        }
        double invMag = 1.0 / mag;
        return new Vector3(Vector3.normalizeZero(this.x * invMag), Vector3.normalizeZero(this.y * invMag), Vector3.normalizeZero(this.z * invMag));
    }

    public Vector3 cross(Vector3 other) {
        Objects.requireNonNull(other, MESSAGE);
        return new Vector3(Vector3.normalizeZero(this.y * other.z - this.z * other.y), Vector3.normalizeZero(this.z * other.x - this.x * other.z), Vector3.normalizeZero(this.x * other.y - this.y * other.x));
    }

    public Vector3 add(Vector3 other) {
        Objects.requireNonNull(other, MESSAGE);
        return new Vector3(Vector3.normalizeZero(this.x + other.x), Vector3.normalizeZero(this.y + other.y), Vector3.normalizeZero(this.z + other.z));
    }

    public Vector3 subtract(Vector3 other) {
        Objects.requireNonNull(other, MESSAGE);
        return new Vector3(Vector3.normalizeZero(this.x - other.x), Vector3.normalizeZero(this.y - other.y), Vector3.normalizeZero(this.z - other.z));
    }

    public Vector3 multiply(double scalar) {
        return new Vector3(Vector3.normalizeZero(this.x * scalar), Vector3.normalizeZero(this.y * scalar), Vector3.normalizeZero(this.z * scalar));
    }

    public Vector3 negate() {
        return new Vector3(Vector3.normalizeZero(-this.x), Vector3.normalizeZero(-this.y), Vector3.normalizeZero(-this.z));
    }

    public double[] toArray() {
        return new double[]{this.x, this.y, this.z};
    }

    public boolean equals(Vector3 other, double tolerance) {
        Objects.requireNonNull(other, MESSAGE);
        if (tolerance < 0.0) {
            throw new IllegalArgumentException("Tolerance cannot be negative: " + tolerance);
        }
        return Math.abs(this.x - other.x) <= tolerance && Math.abs(this.y - other.y) <= tolerance && Math.abs(this.z - other.z) <= tolerance;
    }

    public String toString() {
        return "Vector3(%.6f, %.6f, %.6f)".formatted(new Object[]{this.x, this.y, this.z});
    }

    private static void validateArrayBounds(double[] coords, int offset) {
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("Offset cannot be negative: " + offset);
        }
        if (coords.length < offset + 3) {
            throw new IllegalArgumentException("Array must contain at least %d elements, but has %d".formatted(new Object[]{offset + 3, coords.length}));
        }
    }

    private static boolean isZero(double value) {
        return Math.abs(value) < 1.0E-10;
    }
}

