/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.image.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.weasis.core.Messages;

public enum Unit {
    PIXEL(-5, "Unit.pix", "Unit.pix_s", 1.0, UnitSystem.DIGITAL),
    NANOMETER(-2, "Unit.nano", "Unit.nano_S", 1.0E-9, UnitSystem.METRIC),
    MICROMETER(-1, "Unit.micro", "Unit.micro_S", 1.0E-6, UnitSystem.METRIC),
    MILLIMETER(0, "Unit.milli", "Unit.milli_s", 0.001, UnitSystem.METRIC),
    CENTIMETER(1, "Unit.cent", "Unit.cent_s", 0.01, UnitSystem.METRIC),
    METER(2, "Unit.meter", "Unit.meter_s", 1.0, UnitSystem.METRIC),
    KILOMETER(3, "Unit.kilo", "Unit.kilo_s", 1000.0, UnitSystem.METRIC),
    MICROINCH(9, "Unit.minch", "Unit.minch_s", 2.54E-8, UnitSystem.IMPERIAL),
    MILLIINCH(10, "Unit.mil_inch", "Unit.mil", 2.54E-5, UnitSystem.IMPERIAL),
    INCH(11, "Unit.inch", "Unit.inch_s", 0.0254, UnitSystem.IMPERIAL),
    FEET(12, "Unit.feet", "Unit.feet_s", 0.3048, UnitSystem.IMPERIAL),
    YARD(13, "Unit.yard", "Unit.yard_s", 0.9144, UnitSystem.IMPERIAL),
    MILE(14, "Unit.mile", "Unit.mile_s", 1609.344, UnitSystem.IMPERIAL);

    private static final Map<Integer, Unit> ID_TO_UNIT;
    private static final Map<String, Unit> NAME_TO_UNIT;
    private static final Map<String, Unit> ABBREV_TO_UNIT;
    private final int id;
    private final String fullName;
    private final String abbreviation;
    private final double factorToMeters;
    private final UnitSystem system;

    private Unit(int id, String fullNameKey, String abbreviationKey, double factorToMeters, UnitSystem system) {
        this.id = id;
        this.fullName = Messages.getString(fullNameKey);
        this.abbreviation = Messages.getString(abbreviationKey);
        this.factorToMeters = factorToMeters;
        this.system = system;
    }

    public int getId() {
        return this.id;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public double getFactorToMeters() {
        return this.factorToMeters;
    }

    public UnitSystem getSystem() {
        return this.system;
    }

    public String toString() {
        return this.fullName;
    }

    public double getConversionRatio(double calibrationRatio) {
        return calibrationRatio / this.factorToMeters;
    }

    public double convertTo(double value, Unit targetUnit) {
        if (this == targetUnit) {
            return value;
        }
        if (this == PIXEL || targetUnit == PIXEL) {
            throw new IllegalArgumentException("Cannot convert between pixel and physical units without calibration");
        }
        return this.toMeters(value) / targetUnit.factorToMeters;
    }

    public double toMeters(double value) {
        return value * this.factorToMeters;
    }

    public double fromMeters(double meters) {
        return meters / this.factorToMeters;
    }

    public Optional<Unit> getNextLargerUnit() {
        return this.findAdjacentUnit(this.id + 1);
    }

    public Optional<Unit> getNextSmallerUnit() {
        return this.findAdjacentUnit(this.id - 1);
    }

    private Optional<Unit> findAdjacentUnit(int targetId) {
        return this.streamSameSystem().filter(u -> u.id == targetId).findFirst();
    }

    public List<Unit> getUnitsInSameSystem() {
        return this.streamSameSystem().sorted(Comparator.comparingInt(Unit::getId)).toList();
    }

    private Stream<Unit> streamSameSystem() {
        return Arrays.stream(Unit.values()).filter(u -> u.system == this.system);
    }

    public static Unit getById(int id) {
        return ID_TO_UNIT.getOrDefault(id, PIXEL);
    }

    public static Optional<Unit> getByName(String name) {
        return Optional.ofNullable(NAME_TO_UNIT.get(name));
    }

    public static Optional<Unit> getByAbbreviation(String abbreviation) {
        return Optional.ofNullable(ABBREV_TO_UNIT.get(abbreviation));
    }

    public static List<Unit> getPhysicalUnits() {
        return Unit.streamFiltered(u -> u != PIXEL);
    }

    public static List<Unit> getMetricUnits() {
        return Unit.getUnitsBySystem(UnitSystem.METRIC);
    }

    public static List<Unit> getImperialUnits() {
        return Unit.getUnitsBySystem(UnitSystem.IMPERIAL);
    }

    public static List<Unit> getUnitsBySystem(UnitSystem system) {
        return Unit.streamFiltered(u -> u.system == system);
    }

    private static List<Unit> streamFiltered(Predicate<Unit> filter) {
        return Arrays.stream(Unit.values()).filter(filter).sorted(Comparator.comparingInt(Unit::getId)).toList();
    }

    public static Unit findBestUnit(double valueInMeters, UnitSystem preferredSystem) {
        return Unit.getUnitsBySystem(preferredSystem).stream().filter(u -> u != PIXEL).min(Comparator.comparingDouble(u -> Unit.calculateScore(u.fromMeters(valueInMeters)))).orElse(METER);
    }

    private static double calculateScore(double value) {
        double logValue = Math.abs(Math.log10(Math.abs(value)));
        return value >= 0.1 && value <= 1000.0 ? logValue : logValue + 10.0;
    }

    static {
        ID_TO_UNIT = Arrays.stream(Unit.values()).collect(Collectors.toMap(Unit::getId, u -> u));
        NAME_TO_UNIT = Arrays.stream(Unit.values()).collect(Collectors.toMap(Unit::getFullName, u -> u));
        ABBREV_TO_UNIT = Arrays.stream(Unit.values()).collect(Collectors.toMap(Unit::getAbbreviation, u -> u));
    }

    public static enum UnitSystem {
        METRIC("Metric System"),
        IMPERIAL("Imperial System"),
        DIGITAL("Digital Units");

        private final String displayName;

        private UnitSystem(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String toString() {
            return this.displayName;
        }
    }
}

