/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.seg.Region
 *  org.weasis.opencv.seg.Segment
 */
package org.weasis.core.ui.model.graphic.imp.seg;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import org.weasis.core.ui.model.graphic.imp.seg.SegGraphic;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.seg.Region;
import org.weasis.opencv.seg.Segment;

public class SegContour
extends Region {
    private PlanarImage fractionalMask;

    public SegContour(String id, List<Segment> segmentList) {
        super(id, segmentList);
    }

    public SegContour(String id, List<Segment> segmentList, int numberOfPixels) {
        super(id, segmentList, (long)numberOfPixels);
    }

    public SegContour(String id, PlanarImage fractionalMask, double weightedPixelCount) {
        super(id, List.of(), 0L);
        this.fractionalMask = fractionalMask;
        this.numberOfPixels = Math.round(weightedPixelCount);
    }

    public PlanarImage getFractionalMask() {
        return this.fractionalMask;
    }

    public void setFractionalMask(PlanarImage fractionalMask) {
        this.fractionalMask = fractionalMask;
    }

    public boolean isFractional() {
        return this.fractionalMask != null;
    }

    public SegGraphic getSegGraphic() {
        if (this.segmentList.isEmpty() || !this.attributes.isVisible()) {
            return null;
        }
        Path2D path = this.buildPath();
        if (path == null) {
            return null;
        }
        SegGraphic graphic = new SegGraphic(path);
        graphic.setContour(this);
        graphic.setFilled(this.attributes.isFilled());
        graphic.setLineThickness(Float.valueOf(this.attributes.getLineThickness()));
        graphic.setFillOpacity(Float.valueOf(this.attributes.getInteriorOpacity()));
        graphic.setPaint(this.attributes.getColor());
        graphic.setLayerType(LayerType.DICOM_SEG);
        return graphic;
    }

    private Path2D buildPath() {
        Path2D.Double path = new Path2D.Double(1);
        for (Segment segment : this.segmentList) {
            this.addRecursivelySegment(path, segment);
        }
        return path.getCurrentPoint() == null ? null : path;
    }

    private void addRecursivelySegment(Path2D path, Segment segment) {
        this.addPointsToPath(path, segment);
        for (Segment hole : segment.getChildren()) {
            this.addRecursivelySegment(path, hole);
        }
    }

    private void addPointsToPath(Path2D path, Segment segment) {
        if (segment.isEmpty()) {
            return;
        }
        Point2D p = (Point2D)segment.getFirst();
        path.moveTo(p.getX(), p.getY());
        int n = segment.size();
        for (int i = 1; i < n; ++i) {
            p = (Point2D)segment.get(i);
            path.lineTo(p.getX(), p.getY());
        }
        path.closePath();
    }
}

