/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.Immutable
 */
package com.github.weisj.jsvg.attributes.font;

import com.github.weisj.jsvg.attributes.font.FontWeight;
import com.google.errorprone.annotations.Immutable;

@Immutable
public enum PredefinedFontWeight implements FontWeight
{
    Normal{

        @Override
        public int weight(int parentWeight) {
            return 400;
        }
    }
    ,
    Bold{

        @Override
        public int weight(int parentWeight) {
            return 700;
        }
    }
    ,
    Bolder{

        @Override
        public int weight(int parentWeight) {
            if (parentWeight < 400) {
                return 400;
            }
            if (parentWeight < 600) {
                return 600;
            }
            return Math.max(parentWeight, 900);
        }
    }
    ,
    Lighter{

        @Override
        public int weight(int parentWeight) {
            if (parentWeight > 700) {
                return 700;
            }
            if (parentWeight > 500) {
                return 400;
            }
            return Math.min(parentWeight, 100);
        }
    }
    ,
    Number{

        @Override
        public int weight(int parentWeight) {
            throw new UnsupportedOperationException("Number needs to be parsed explicitly");
        }
    };

    public static final int NORMAL_WEIGHT = 400;
    public static final int BOLD_WEIGHT = 700;
}

