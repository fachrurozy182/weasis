/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras.components;

import com.formdev.flatlaf.extras.components.FlatComponentExtension;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;
import javax.swing.JProgressBar;

public class FlatProgressBar
extends JProgressBar
implements FlatComponentExtension,
FlatStyleableComponent {
    public boolean isLargeHeight() {
        return this.getClientPropertyBoolean((Object)"JProgressBar.largeHeight", false);
    }

    public void setLargeHeight(boolean largeHeight) {
        this.putClientPropertyBoolean("JProgressBar.largeHeight", largeHeight, false);
    }

    public boolean isSquare() {
        return this.getClientPropertyBoolean((Object)"JProgressBar.square", false);
    }

    public void setSquare(boolean square) {
        this.putClientPropertyBoolean("JProgressBar.square", square, false);
    }
}

