/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.layout;

public record ConstraintSpec(double growWeight, double shrinkWeight, boolean fill, Integer minSize, Integer maxSize) {
    public ConstraintSpec {
        if (growWeight < 0.0 || shrinkWeight < 0.0) {
            throw new IllegalArgumentException("Weights must be non-negative");
        }
        if (minSize != null && minSize < 0) {
            throw new IllegalArgumentException("Min size must be non-negative");
        }
        if (maxSize != null && maxSize < 0) {
            throw new IllegalArgumentException("Max size must be non-negative");
        }
    }

    public ConstraintSpec(double growWeight) {
        this(growWeight, 100.0, true, null, null);
    }

    public ConstraintSpec(double growWeight, double shrinkWeight) {
        this(growWeight, shrinkWeight, true, null, null);
    }

    public static ConstraintSpec fixed(int size) {
        return new ConstraintSpec(0.0, 0.0, true, size, size);
    }

    public static ConstraintSpec withBounds(int minSize, int maxSize) {
        return new ConstraintSpec(100.0, 100.0, true, minSize, maxSize);
    }

    public ConstraintSpec withGrowWeight(double newGrowWeight) {
        return new ConstraintSpec(newGrowWeight, this.shrinkWeight, this.fill, this.minSize, this.maxSize);
    }

    public ConstraintSpec withShrinkWeight(double newShrinkWeight) {
        return new ConstraintSpec(this.growWeight, newShrinkWeight, this.fill, this.minSize, this.maxSize);
    }

    public ConstraintSpec withFill(boolean newFill) {
        return new ConstraintSpec(this.growWeight, this.shrinkWeight, newFill, this.minSize, this.maxSize);
    }

    public ConstraintSpec withSizeBounds(Integer newMin, Integer newMax) {
        return new ConstraintSpec(this.growWeight, this.shrinkWeight, this.fill, newMin, newMax);
    }

    public String toConstraintString() {
        return "[%s,shrink %s%s%s]".formatted(new Object[]{this.formatGrow(), this.formatShrink(), this.fill ? ",fill" : "", this.formatSizeBounds()});
    }

    private String formatGrow() {
        return this.growWeight > 0.0 ? "grow " + this.growWeight : "grow 0";
    }

    private String formatShrink() {
        return this.shrinkWeight > 0.0 ? String.valueOf(this.shrinkWeight) : "0";
    }

    private String formatSizeBounds() {
        if (this.minSize == null && this.maxSize == null) {
            return "";
        }
        return ",%s::%s".formatted(new Object[]{this.minSize != null ? this.minSize : "", this.maxSize != null ? this.maxSize : ""});
    }

    public static ConstraintSpec parse(String spec) {
        Builder builder = new Builder();
        for (String part : spec.split(",")) {
            if ((part = part.trim()).startsWith("grow")) {
                builder.growWeight(ConstraintSpec.parseWeightValue(part));
                continue;
            }
            if (part.startsWith("shrink")) {
                builder.shrinkWeight(ConstraintSpec.parseWeightValue(part));
                continue;
            }
            if (part.equals("fill")) {
                builder.fill(true);
                continue;
            }
            if (!part.contains(":")) continue;
            ConstraintSpec.parseSizeBounds(part, builder);
        }
        return builder.build();
    }

    public static ConstraintSpec[] parseAll(String constraintString, int count) {
        int close;
        int open;
        double defaultWeight = 100.0 / (double)count;
        if (constraintString == null || constraintString.isEmpty()) {
            return ConstraintSpec.createDefaults(count, defaultWeight);
        }
        ConstraintSpec[] specs = new ConstraintSpec[count];
        int index = 0;
        int pos = 0;
        while (pos < constraintString.length() && index < count && (open = constraintString.indexOf(91, pos)) != -1 && (close = constraintString.indexOf(93, open)) != -1) {
            specs[index++] = ConstraintSpec.parse(constraintString.substring(open + 1, close));
            pos = close + 1;
        }
        for (int i = index; i < count; ++i) {
            specs[i] = new ConstraintSpec(defaultWeight);
        }
        return specs;
    }

    private static ConstraintSpec[] createDefaults(int count, double weight) {
        ConstraintSpec[] specs = new ConstraintSpec[count];
        for (int i = 0; i < count; ++i) {
            specs[i] = new ConstraintSpec(weight);
        }
        return specs;
    }

    private static double parseWeightValue(String part) {
        String[] tokens = part.split("\\s+");
        if (tokens.length > 1) {
            try {
                return Double.parseDouble(tokens[1]);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return 100.0;
    }

    private static void parseSizeBounds(String part, Builder builder) {
        String[] sizes = part.split(":");
        try {
            if (sizes.length > 0 && !sizes[0].isEmpty()) {
                builder.minSize(Integer.parseInt(sizes[0]));
            }
            if (sizes.length > 2 && !sizes[2].isEmpty()) {
                builder.maxSize(Integer.parseInt(sizes[2]));
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    public static final class Builder {
        private double growWeight = 100.0;
        private double shrinkWeight = 100.0;
        private boolean fill = false;
        private Integer minSize = null;
        private Integer maxSize = null;

        public Builder growWeight(double weight) {
            this.growWeight = weight;
            return this;
        }

        public Builder shrinkWeight(double weight) {
            this.shrinkWeight = weight;
            return this;
        }

        public Builder fill(boolean fill) {
            this.fill = fill;
            return this;
        }

        public Builder minSize(Integer size) {
            this.minSize = size;
            return this;
        }

        public Builder maxSize(Integer size) {
            this.maxSize = size;
            return this;
        }

        public ConstraintSpec build() {
            return new ConstraintSpec(this.growWeight, this.shrinkWeight, this.fill, this.minSize, this.maxSize);
        }
    }
}

