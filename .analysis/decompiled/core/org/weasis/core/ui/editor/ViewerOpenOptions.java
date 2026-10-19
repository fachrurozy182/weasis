/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor;

import javax.swing.Icon;
import org.weasis.core.ui.editor.TabFocusPolicy;
import org.weasis.core.ui.editor.ViewerPlacement;

public record ViewerOpenOptions(ViewerPlacement placement, TabFocusPolicy tabFocusPolicy, Icon icon, String uid, int seriesCount) {
    private static final ViewerOpenOptions DEFAULTS = new ViewerOpenOptions(ViewerPlacement.reuseViewer(), TabFocusPolicy.foreground(), null, null, 1);

    public ViewerOpenOptions {
        if (placement == null) {
            placement = ViewerPlacement.reuseViewer();
        }
        if (tabFocusPolicy == null) {
            tabFocusPolicy = TabFocusPolicy.foreground();
        }
    }

    public static ViewerOpenOptions defaults() {
        return DEFAULTS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ViewerOpenOptions withSeriesCount(int count) {
        return new ViewerOpenOptions(this.placement, this.tabFocusPolicy, this.icon, this.uid, count);
    }

    public ViewerOpenOptions withTabFocusPolicy(TabFocusPolicy policy) {
        return new ViewerOpenOptions(this.placement, policy, this.icon, this.uid, this.seriesCount);
    }

    public static final class Builder {
        private ViewerPlacement placement = ViewerPlacement.reuseViewer();
        private TabFocusPolicy tabFocusPolicy = TabFocusPolicy.foreground();
        private Icon icon = null;
        private String uid = null;
        private int seriesCount = 1;

        private Builder() {
        }

        public Builder placement(ViewerPlacement val) {
            this.placement = val;
            return this;
        }

        public Builder tabFocusPolicy(TabFocusPolicy val) {
            this.tabFocusPolicy = val;
            return this;
        }

        public Builder icon(Icon val) {
            this.icon = val;
            return this;
        }

        public Builder uid(String val) {
            this.uid = val;
            return this;
        }

        public Builder seriesCount(int val) {
            this.seriesCount = val;
            return this;
        }

        public ViewerOpenOptions build() {
            return new ViewerOpenOptions(this.placement, this.tabFocusPolicy, this.icon, this.uid, this.seriesCount);
        }
    }
}

