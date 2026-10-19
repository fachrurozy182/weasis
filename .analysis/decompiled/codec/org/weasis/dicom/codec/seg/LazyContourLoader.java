/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.ui.model.graphic.imp.seg.SegContour
 */
package org.weasis.dicom.codec.seg;

import java.util.Set;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;

public interface LazyContourLoader {
    public Set<SegContour> getLazyContours();
}

