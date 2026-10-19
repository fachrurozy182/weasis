/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.gui.layout;

import java.util.Objects;
import org.weasis.core.api.util.Copyable;
import org.weasis.core.util.StringUtil;

public record MigCell(int position, String type, String constraints, int x, int y, int spanX, int spanY) implements Copyable<MigCell>
{
    public MigCell {
        Objects.requireNonNull(type, "type cannot be null");
        if (spanX < 1 || spanY < 1) {
            throw new IllegalArgumentException("Span values must be positive");
        }
    }

    public String getFullConstraints() {
        StringBuilder sb = new StringBuilder();
        if (!(this.spanX <= 1 && this.spanY <= 1 || this.hasSpanConstraint())) {
            sb.append("span ").append(this.spanX);
            if (this.spanY > 1) {
                sb.append(" ").append(this.spanY);
            }
        }
        if (StringUtil.hasText((CharSequence)this.constraints)) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(this.constraints);
        }
        return sb.toString();
    }

    private boolean hasSpanConstraint() {
        return StringUtil.hasText((CharSequence)this.constraints) && this.constraints.toLowerCase().contains("span");
    }

    @Override
    public MigCell copy() {
        return new MigCell(this.position, this.type, this.constraints, this.x, this.y, this.spanX, this.spanY);
    }
}

