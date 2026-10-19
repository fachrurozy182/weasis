/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.prefs.Preferences
 */
package org.weasis.core.ui.pref;

import java.awt.Color;
import org.osgi.service.prefs.Preferences;
import org.weasis.core.api.image.ZoomOp;
import org.weasis.core.api.service.BundlePreferences;

public class ZoomSetting {
    public static final String PREFERENCE_NODE = "zoom";
    private boolean lensShowDrawings = true;
    private boolean lensSynchronize = false;
    private int lensWidth = 200;
    private int lensHeight = 200;
    private int interpolation = 1;
    private boolean lensRound = false;
    private int lensLineWidth = 2;
    private Color lensLineColor = new Color(195, 109, 254);

    public void applyPreferences(Preferences prefs) {
        if (prefs != null) {
            Preferences p = prefs.node(PREFERENCE_NODE);
            this.setInterpolation(p.getInt("interpolation", ZoomOp.Interpolation.BILINEAR.ordinal()));
        }
    }

    public void savePreferences(Preferences prefs) {
        if (prefs != null) {
            Preferences p = prefs.node(PREFERENCE_NODE);
            BundlePreferences.putIntPreferences(p, "interpolation", this.interpolation);
        }
    }

    public boolean isLensShowDrawings() {
        return this.lensShowDrawings;
    }

    public void setLensShowDrawings(boolean lensShowDrawings) {
        this.lensShowDrawings = lensShowDrawings;
    }

    public boolean isLensSynchronize() {
        return this.lensSynchronize;
    }

    public void setLensSynchronize(boolean lensSynchronize) {
        this.lensSynchronize = lensSynchronize;
    }

    public int getLensWidth() {
        return this.lensWidth;
    }

    public void setLensWidth(int lensWidth) {
        this.lensWidth = lensWidth;
    }

    public int getLensHeight() {
        return this.lensHeight;
    }

    public void setLensHeight(int lensHeight) {
        this.lensHeight = lensHeight;
    }

    public int getInterpolation() {
        return this.interpolation;
    }

    public void setInterpolation(int interpolation) {
        this.interpolation = interpolation < 0 || interpolation >= ZoomOp.Interpolation.values().length ? ZoomOp.Interpolation.BILINEAR.ordinal() : interpolation;
    }

    public boolean isLensRound() {
        return this.lensRound;
    }

    public void setLensRound(boolean lensRound) {
        this.lensRound = lensRound;
    }

    public int getLensLineWidth() {
        return this.lensLineWidth;
    }

    public void setLensLineWidth(int lensLineWidth) {
        this.lensLineWidth = lensLineWidth;
    }

    public Color getLensLineColor() {
        return this.lensLineColor;
    }

    public void setLensLineColor(Color lensLineColor) {
        this.lensLineColor = lensLineColor;
    }
}

