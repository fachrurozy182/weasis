/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.utils.imp;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.weasis.core.api.gui.model.ViewModel;
import org.weasis.core.api.gui.model.ViewModelChangeListener;
import org.weasis.core.util.MathUtil;

public class DefaultViewModel
implements ViewModel {
    public static final double SCALE_MAX = 12.0;
    public static final double SCALE_MIN = 0.08333333333333333;
    private double modelOffsetX = 0.0;
    private double modelOffsetY = 0.0;
    private double viewScale = 1.0;
    private double viewScaleMax;
    private double viewScaleMin;
    private Rectangle2D modelArea;
    private final List<ViewModelChangeListener> viewModelChangeListenerList;
    private boolean enableViewModelChangeListeners;

    public DefaultViewModel(double viewScaleMin, double viewScaleMax) {
        this.viewScaleMin = viewScaleMin;
        this.viewScaleMax = viewScaleMax;
        this.modelArea = new Rectangle2D.Double();
        this.viewModelChangeListenerList = new ArrayList<ViewModelChangeListener>();
        this.enableViewModelChangeListeners = true;
    }

    public DefaultViewModel() {
        this(0.08333333333333333, 12.0);
    }

    public boolean isEnableViewModelChangeListeners() {
        return this.enableViewModelChangeListeners;
    }

    public void setEnableViewModelChangeListeners(boolean enableViewModelChangeListeners) {
        this.enableViewModelChangeListeners = enableViewModelChangeListeners;
    }

    @Override
    public double getModelOffsetX() {
        return this.modelOffsetX;
    }

    @Override
    public double getModelOffsetY() {
        return this.modelOffsetY;
    }

    @Override
    public void setModelOffset(double modelOffsetX, double modelOffsetY) {
        if (MathUtil.isDifferent((double)this.modelOffsetX, (double)modelOffsetX) || MathUtil.isDifferent((double)this.modelOffsetY, (double)modelOffsetY)) {
            this.modelOffsetX = modelOffsetX;
            this.modelOffsetY = modelOffsetY;
            this.fireViewModelChanged();
        }
    }

    @Override
    public void setModelOffset(double modelOffsetX, double modelOffsetY, double viewScale) {
        double scale = this.maybeCropViewScale(viewScale);
        if (MathUtil.isDifferent((double)this.modelOffsetX, (double)modelOffsetX) || MathUtil.isDifferent((double)this.modelOffsetY, (double)modelOffsetY) || MathUtil.isDifferent((double)this.viewScale, (double)scale)) {
            this.modelOffsetX = modelOffsetX;
            this.modelOffsetY = modelOffsetY;
            this.viewScale = scale;
            this.fireViewModelChanged();
        }
    }

    @Override
    public double getViewScale() {
        return this.viewScale;
    }

    @Override
    public void setViewScale(double viewScale) {
        double scale = this.maybeCropViewScale(viewScale);
        if (MathUtil.isDifferent((double)this.viewScale, (double)scale)) {
            this.viewScale = scale;
            this.fireViewModelChanged();
        }
    }

    @Override
    public double getViewScaleMax() {
        return this.viewScaleMax;
    }

    @Override
    public void setViewScaleMax(double viewScaleMax) {
        this.viewScaleMax = viewScaleMax;
    }

    @Override
    public Rectangle2D getModelArea() {
        return new Rectangle2D.Double(this.modelArea.getX(), this.modelArea.getY(), this.modelArea.getWidth(), this.modelArea.getHeight());
    }

    @Override
    public double getViewScaleMin() {
        return this.viewScaleMin;
    }

    @Override
    public void setModelArea(Rectangle2D modelArea) {
        if (!this.modelArea.equals(modelArea)) {
            this.modelArea = new Rectangle2D.Double(modelArea.getX(), modelArea.getY(), modelArea.getWidth(), modelArea.getHeight());
            this.fireViewModelChanged();
        }
    }

    @Override
    public void setViewScaleMin(double viewScaleMin) {
        this.viewScaleMin = viewScaleMin;
    }

    @Override
    public ViewModelChangeListener[] getViewModelChangeListeners() {
        ViewModelChangeListener[] viewModelChangeListeners = new ViewModelChangeListener[this.viewModelChangeListenerList.size()];
        return this.viewModelChangeListenerList.toArray(viewModelChangeListeners);
    }

    @Override
    public void addViewModelChangeListener(ViewModelChangeListener l) {
        if (l != null && !this.viewModelChangeListenerList.contains(l)) {
            this.viewModelChangeListenerList.add(l);
        }
    }

    @Override
    public void removeViewModelChangeListener(ViewModelChangeListener l) {
        if (l != null) {
            this.viewModelChangeListenerList.remove(l);
        }
    }

    protected void fireViewModelChanged() {
        if (this.enableViewModelChangeListeners) {
            for (ViewModelChangeListener l : this.viewModelChangeListenerList) {
                l.handleViewModelChanged(this);
            }
        }
    }

    public static double cropViewScale(double viewScale, double viewScaleMin, double viewScaleMax) {
        if (viewScaleMax > 1.0) {
            if (viewScale < viewScaleMin) {
                return viewScaleMin;
            }
            if (viewScale > viewScaleMax) {
                return viewScaleMax;
            }
        }
        return viewScale;
    }

    private double maybeCropViewScale(double viewScale) {
        return DefaultViewModel.cropViewScale(viewScale, this.getViewScaleMin(), this.getViewScaleMax());
    }

    public void adjustMinViewScaleFromImage(int width, int height) {
        double ratio = 250.0 / (double)Math.max(width, height);
        if (ratio < this.viewScaleMin) {
            this.viewScaleMin = ratio;
        }
    }
}

