/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor;

import org.weasis.core.ui.editor.ExternalDisplay;
import org.weasis.core.ui.editor.SplitLayout;

public sealed interface ViewerPlacement {
    public static ReuseViewer reuseViewer() {
        return ReuseViewer.DEFAULT;
    }

    public static ReuseViewer reuseViewer(boolean bestDefaultLayout, boolean openInSelection) {
        return new ReuseViewer(bestDefaultLayout, openInSelection);
    }

    public static NewTab newTab() {
        return NewTab.DEFAULT;
    }

    public static NewTab newTab(boolean bestDefaultLayout) {
        return new NewTab(bestDefaultLayout);
    }

    public static Split split(SplitLayout splitLayout) {
        return new Split(splitLayout);
    }

    public static External external(ExternalDisplay externalDisplay) {
        return new External(externalDisplay);
    }

    public record ReuseViewer(boolean bestDefaultLayout, boolean openInSelection) implements ViewerPlacement
    {
        public static final ReuseViewer DEFAULT = new ReuseViewer(true, false);
    }

    public record NewTab(boolean bestDefaultLayout) implements ViewerPlacement
    {
        public static final NewTab DEFAULT = new NewTab(false);
    }

    public record Split(SplitLayout splitLayout) implements ViewerPlacement
    {
        public Split {
            if (splitLayout == null) {
                throw new IllegalArgumentException("splitLayout must not be null");
            }
        }
    }

    public record External(ExternalDisplay externalDisplay) implements ViewerPlacement
    {
        public External {
            if (externalDisplay == null) {
                throw new IllegalArgumentException("externalDisplay must not be null");
            }
        }
    }
}

