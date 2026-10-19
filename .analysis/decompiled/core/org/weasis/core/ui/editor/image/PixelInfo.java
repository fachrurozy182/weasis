/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Point3
 */
package org.weasis.core.ui.editor.image;

import java.awt.Point;
import org.opencv.core.Point3;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.image.util.Unit;

public class PixelInfo {
    private double[] values;
    private String[] channelNames;
    private String pixelValueUnit;
    private Point position;
    private Point3 position3d;
    private Unit pixelSpacingUnit;
    private Double pixelSize;

    public double[] getValues() {
        return this.values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    public String[] getChannelNames() {
        return this.channelNames;
    }

    public void setChannelNames(String[] channelNames) {
        this.channelNames = channelNames;
    }

    public String getPixelValueUnit() {
        return this.pixelValueUnit;
    }

    public void setPixelValueUnit(String pixelValueUnit) {
        this.pixelValueUnit = pixelValueUnit;
    }

    public String getPixelValueText() {
        if (this.values != null) {
            if (this.values.length == 1) {
                StringBuilder text = new StringBuilder();
                text.append(DecFormatter.allNumber(this.values[0]));
                if (this.pixelValueUnit != null) {
                    text.append(" ");
                    text.append(this.pixelValueUnit);
                }
                return text.toString();
            }
            if (this.values.length > 1) {
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < this.values.length; ++i) {
                    text.append(" ");
                    text.append(this.channelNames == null || i >= this.channelNames.length ? Messages.getString("PixelInfo.unknown") : this.channelNames[i].substring(0, 1));
                    text.append("=");
                    text.append(DecFormatter.twoDecimal(this.values[i]));
                }
                return text.toString();
            }
        }
        return Messages.getString("PixelInfo.no_val");
    }

    public Unit getPixelSpacingUnit() {
        return this.pixelSpacingUnit;
    }

    public void setPixelSpacingUnit(Unit pixelSpacingUnit) {
        this.pixelSpacingUnit = pixelSpacingUnit;
    }

    public Double getPixelSize() {
        return this.pixelSize;
    }

    public void setPixelSize(Double pixelSize) {
        this.pixelSize = pixelSize;
    }

    public Point getPosition() {
        return this.position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point3 getPosition3d() {
        return this.position3d;
    }

    public void setPosition3d(Point3 position3d) {
        this.position3d = position3d;
    }

    public String getPixelPositionText() {
        if (this.position == null && this.position3d == null) {
            return Messages.getString("DefaultView2d.out");
        }
        if (this.position3d == null) {
            return "(" + this.position.x + "," + this.position.y + ")";
        }
        return "(" + String.format("%.0f", this.position3d.x) + "," + String.format("%.0f", this.position3d.y) + "," + String.format("%.0f", this.position3d.z) + ")";
    }

    public String getRealPositionText() {
        if (this.position == null || this.pixelSpacingUnit == null || this.pixelSize == null) {
            return this.getPixelPositionText();
        }
        return "(" + DecFormatter.twoDecimal(this.pixelSize * (double)this.position.x) + this.pixelSpacingUnit.getAbbreviation() + "," + DecFormatter.twoDecimal(this.pixelSize * (double)this.position.y) + this.pixelSpacingUnit.getAbbreviation() + ")";
    }
}

