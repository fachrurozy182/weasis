/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.Abbreviator;

public class TargetLengthBasedClassNameAbbreviator
implements Abbreviator {
    final int targetLength;

    public TargetLengthBasedClassNameAbbreviator(int targetLength) {
        this.targetLength = targetLength;
    }

    @Override
    public String abbreviate(String fqClassName) {
        int i;
        if (fqClassName == null) {
            throw new IllegalArgumentException("Class name may not be null");
        }
        int inLen = fqClassName.length();
        if (inLen < this.targetLength) {
            return fqClassName;
        }
        StringBuilder buf = new StringBuilder(inLen);
        int rightMostDotIndex = fqClassName.lastIndexOf(46);
        if (rightMostDotIndex == -1) {
            return fqClassName;
        }
        int lastSegmentLength = inLen - rightMostDotIndex;
        int leftSegments_TargetLen = this.targetLength - lastSegmentLength;
        if (leftSegments_TargetLen < 0) {
            leftSegments_TargetLen = 0;
        }
        int leftSegmentsLen = inLen - lastSegmentLength;
        int maxPossibleTrim = leftSegmentsLen - leftSegments_TargetLen;
        int trimmed = 0;
        boolean inDotState = true;
        for (i = 0; i < rightMostDotIndex; ++i) {
            char c = fqClassName.charAt(i);
            if (c == '.') {
                if (trimmed >= maxPossibleTrim) break;
                buf.append(c);
                inDotState = true;
                continue;
            }
            if (inDotState) {
                buf.append(c);
                inDotState = false;
                continue;
            }
            ++trimmed;
        }
        buf.append(fqClassName.substring(i));
        return buf.toString();
    }
}

