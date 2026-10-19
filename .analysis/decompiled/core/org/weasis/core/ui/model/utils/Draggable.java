/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.utils;

import org.weasis.core.ui.util.MouseEventDouble;

public interface Draggable {
    public void startDrag(MouseEventDouble var1);

    public void drag(MouseEventDouble var1);

    public Boolean completeDrag(MouseEventDouble var1);
}

