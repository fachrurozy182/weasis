/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.image;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.api.util.Copyable;

public final class LayoutConstraints
extends GridBagConstraints
implements Comparable<LayoutConstraints>,
Copyable<LayoutConstraints> {
    public static final int DEFAULT_SPACE = 3;
    private final String type;
    private final int layoutID;
    private Color color;

    public LayoutConstraints(String type, int layoutID, int gridX, int gridY, int gridWidth, int gridHeight, double weightX, double weightY, int anchor, int fill) {
        super(gridX, gridY, gridWidth, gridHeight, weightX, weightY, anchor, fill, LayoutConstraints.calculateInsets(gridX, gridY), 0, 0);
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.layoutID = layoutID;
    }

    public LayoutConstraints(LayoutConstraints source) {
        super(source.gridx, source.gridy, source.gridwidth, source.gridheight, source.weightx, source.weighty, source.anchor, source.fill, new Insets(source.insets.top, source.insets.left, source.insets.bottom, source.insets.right), source.ipadx, source.ipady);
        this.type = source.type;
        this.layoutID = source.layoutID;
        this.color = source.color;
    }

    private static Insets calculateInsets(int gridX, int gridY) {
        return new Insets(gridY == 0 ? 0 : 3, gridX == 0 ? 0 : 3, 0, 0);
    }

    public String type() {
        return this.type;
    }

    public int layoutID() {
        return this.layoutID;
    }

    public Optional<Color> colorOptional() {
        return Optional.ofNullable(this.color);
    }

    public Color color() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean hasColor() {
        return this.color != null;
    }

    public boolean isSpanning() {
        return this.gridwidth > 1 || this.gridheight > 1;
    }

    public boolean hasWeight() {
        return this.weightx > 0.0 || this.weighty > 0.0;
    }

    @Override
    public int compareTo(LayoutConstraints other) {
        if (other == null) {
            return 1;
        }
        int result = Integer.compare(this.layoutID, other.layoutID);
        if (result != 0) {
            return result;
        }
        result = Integer.compare(this.gridy, other.gridy);
        if (result != 0) {
            return result;
        }
        return Integer.compare(this.gridx, other.gridx);
    }

    @Override
    public LayoutConstraints copy() {
        return new LayoutConstraints(this);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof LayoutConstraints)) return false;
        LayoutConstraints that = (LayoutConstraints)obj;
        if (this.layoutID != that.layoutID) return false;
        if (this.gridx != that.gridx) return false;
        if (this.gridy != that.gridy) return false;
        if (this.gridwidth != that.gridwidth) return false;
        if (this.gridheight != that.gridheight) return false;
        if (Double.compare(this.weightx, that.weightx) != 0) return false;
        if (Double.compare(this.weighty, that.weighty) != 0) return false;
        if (this.anchor != that.anchor) return false;
        if (this.fill != that.fill) return false;
        if (!Objects.equals(this.type, that.type)) return false;
        if (!Objects.equals(this.color, that.color)) return false;
        if (!Objects.equals(this.insets, that.insets)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.type, this.layoutID, this.gridx, this.gridy, this.gridwidth, this.gridheight, this.weightx, this.weighty, this.anchor, this.fill, this.color, this.insets);
    }

    public String toString() {
        return "LayoutConstraints[type=%s, id=%d, grid=(%d,%d), size=%dx%d, weight=(%.2f,%.2f), anchor=%d, fill=%d%s]".formatted(new Object[]{this.type, this.layoutID, this.gridx, this.gridy, this.gridwidth, this.gridheight, this.weightx, this.weighty, this.anchor, this.fill, this.hasColor() ? ", color=" + String.valueOf(this.color) : ""});
    }

    public static Builder builder(String type, int layoutID) {
        return new Builder(type, layoutID);
    }

    public static final class Builder {
        private final String type;
        private final int layoutID;
        private int gridX;
        private int gridY;
        private int gridWidth = 1;
        private int gridHeight = 1;
        private double weightX;
        private double weightY;
        private int anchor = 10;
        private int fill = 0;
        private Color color;

        private Builder(String type, int layoutID) {
            this.type = Objects.requireNonNull(type, "Type cannot be null");
            this.layoutID = layoutID;
        }

        public Builder gridPosition(int x, int y) {
            this.gridX = x;
            this.gridY = y;
            return this;
        }

        public Builder gridSize(int width, int height) {
            this.gridWidth = Math.max(1, width);
            this.gridHeight = Math.max(1, height);
            return this;
        }

        public Builder weights(double weightX, double weightY) {
            this.weightX = Math.max(0.0, weightX);
            this.weightY = Math.max(0.0, weightY);
            return this;
        }

        public Builder anchor(int anchor) {
            this.anchor = anchor;
            return this;
        }

        public Builder fill(int fill) {
            this.fill = fill;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public LayoutConstraints build() {
            LayoutConstraints constraints = new LayoutConstraints(this.type, this.layoutID, this.gridX, this.gridY, this.gridWidth, this.gridHeight, this.weightX, this.weightY, this.anchor, this.fill);
            constraints.color = this.color;
            return constraints;
        }
    }
}

