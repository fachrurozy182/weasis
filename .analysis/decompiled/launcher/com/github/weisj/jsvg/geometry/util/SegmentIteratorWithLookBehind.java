/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.util;

import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.util.GeometryUtil;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public final class SegmentIteratorWithLookBehind {
    @NotNull
    private final PathIterator pathIterator;
    private float maxLookBehindLength;
    private float currentLookBehindLength = 0.0f;
    private final ArrayList<Segment> lookBehind = new ArrayList();
    private Segment currentSegment;
    private final float[] cords = new float[2];
    private float moveToX;
    private float moveToY;
    private int lookBehindCursor = -1;

    public SegmentIteratorWithLookBehind(@NotNull PathIterator pathIterator, float maxLookBehindLength) {
        this.pathIterator = pathIterator;
        this.maxLookBehindLength = maxLookBehindLength;
        this.prepareFirstSegment();
    }

    private void prepareFirstSegment() {
        this.currentSegment = new Segment(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
        this.moveToNext();
        if (Length.isUnspecified(this.currentSegment.xStart) || Length.isUnspecified(this.currentSegment.yStart)) {
            throw new IllegalStateException("Path iterator did not establish starting position");
        }
    }

    @NotNull
    public PathIterator pathIterator() {
        return this.pathIterator;
    }

    public void setMaxLookBehindLength(float maxLookBehindLength) {
        this.maxLookBehindLength = maxLookBehindLength;
        this.trimLookBehindIfNecessary();
    }

    public float maxLookBehindLength() {
        return this.maxLookBehindLength;
    }

    public boolean hasNext() {
        return this.lookBehindCursor >= 0 || !this.pathIterator.isDone();
    }

    public boolean isDone() {
        return !this.hasNext();
    }

    public boolean hasPrevious() {
        return this.lookBehindCursor < this.lookBehind.size() - 1;
    }

    @NotNull
    public Segment currentSegment() {
        if (this.lookBehindCursor >= 0) {
            return this.lookBehind.get(this.lookBehind.size() - 1 - this.lookBehindCursor);
        }
        return this.currentSegment;
    }

    public void moveToPrevious() {
        if (!this.hasPrevious()) {
            throw new IllegalStateException("Can't move back anymore. Maximum capacity is " + this.maxLookBehindLength);
        }
        ++this.lookBehindCursor;
    }

    public void moveToNext() {
        if (this.lookBehindCursor >= 0) {
            --this.lookBehindCursor;
            return;
        }
        Segment nextSegment = new Segment(this.currentSegment.xEnd, this.currentSegment.yEnd, Float.NaN, Float.NaN);
        block5: while (!this.pathIterator.isDone()) {
            switch (this.pathIterator.currentSegment(this.cords)) {
                case 0: {
                    nextSegment.xStart = this.moveToX = this.cords[0];
                    nextSegment.yStart = this.moveToY = this.cords[1];
                    nextSegment.moveHappened = true;
                    this.pathIterator.next();
                    continue block5;
                }
                case 4: {
                    nextSegment.xEnd = this.moveToX;
                    nextSegment.yEnd = this.moveToY;
                    this.pathIterator.next();
                    break block5;
                }
                case 1: {
                    nextSegment.xEnd = this.cords[0];
                    nextSegment.yEnd = this.cords[1];
                    this.pathIterator.next();
                    break block5;
                }
                default: {
                    throw new IllegalStateException("Unsupported segment type");
                }
            }
        }
        if (Float.isNaN(nextSegment.xEnd) || Float.isNaN(nextSegment.yEnd)) {
            nextSegment.xEnd = nextSegment.xStart;
            nextSegment.yEnd = nextSegment.yStart;
        }
        if (this.maxLookBehindLength > 0.0f) {
            this.lookBehind.add(this.currentSegment);
            this.currentLookBehindLength += (float)this.currentSegment.length();
            this.trimLookBehindIfNecessary();
        }
        this.currentSegment = nextSegment;
    }

    private void trimLookBehindIfNecessary() {
        Segment segment;
        double segmentLength;
        while (GeometryUtil.notablyGreater(this.currentLookBehindLength, this.maxLookBehindLength) && !((double)this.currentLookBehindLength - (segmentLength = (segment = this.lookBehind.get(0)).length()) < (double)this.maxLookBehindLength)) {
            this.currentLookBehindLength -= (float)segmentLength;
            this.lookBehind.remove(0);
        }
    }

    public static final class Segment {
        public float xStart;
        public float yStart;
        public float xEnd;
        public float yEnd;
        public boolean moveHappened;

        private Segment(float xStart, float yStart, float xEnd, float yEnd) {
            this.xStart = xStart;
            this.yStart = yStart;
            this.xEnd = xEnd;
            this.yEnd = yEnd;
        }

        public double length() {
            return GeometryUtil.lineLength(this.xStart, this.yStart, this.xEnd, this.yEnd);
        }

        public String toString() {
            return String.format("[%.2f,%.2f] -> [%.2f,%.2f] (moved: %b)", Float.valueOf(this.xStart), Float.valueOf(this.yStart), Float.valueOf(this.xEnd), Float.valueOf(this.yEnd), this.moveHappened);
        }
    }
}

