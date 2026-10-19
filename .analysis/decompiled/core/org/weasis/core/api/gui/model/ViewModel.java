/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.model;

import java.awt.geom.Rectangle2D;
import org.weasis.core.api.gui.model.ViewModelChangeListener;

public interface ViewModel {
    public double getModelOffsetX();

    public double getModelOffsetY();

    public void setModelOffset(double var1, double var3);

    public double getViewScale();

    public void setViewScale(double var1);

    public double getViewScaleMin();

    public void setViewScaleMin(double var1);

    public double getViewScaleMax();

    public void setViewScaleMax(double var1);

    public void setModelOffset(double var1, double var3, double var5);

    public Rectangle2D getModelArea();

    public void setModelArea(Rectangle2D var1);

    public ViewModelChangeListener[] getViewModelChangeListeners();

    public void addViewModelChangeListener(ViewModelChangeListener var1);

    public void removeViewModelChangeListener(ViewModelChangeListener var1);
}

