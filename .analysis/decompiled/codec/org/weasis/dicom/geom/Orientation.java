/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.geom;

import java.awt.Color;

/*
 * Uses 'sealed' constructs - enablewith --sealed true
 */
public interface Orientation {
    public String name();

    public String getFullName();

    public Color getColor();
}

