/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.attributes.filter.CompositeMode;
import com.github.weisj.jsvg.nodes.filter.AbstractBlendComposite;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import java.awt.AlphaComposite;
import java.awt.Composite;
import org.jetbrains.annotations.NotNull;

public final class CompositeModeComposite {
    @NotNull
    private final Composite composite;

    public CompositeModeComposite(@NotNull AttributeNode attributeNode) {
        this.composite = CompositeModeComposite.createComposite(attributeNode);
    }

    @NotNull
    public Composite composite() {
        return this.composite;
    }

    @NotNull
    private static Composite createComposite(@NotNull AttributeNode attributeNode) {
        CompositeMode compositeMode = attributeNode.getEnum("operator", CompositeMode.Over);
        switch (compositeMode) {
            case Over: {
                return AlphaComposite.SrcOver;
            }
            case In: {
                return AlphaComposite.SrcIn;
            }
            case Out: {
                return AlphaComposite.SrcOut;
            }
            case Atop: {
                return AlphaComposite.SrcAtop;
            }
            case Xor: {
                return AlphaComposite.Xor;
            }
            case Lighter: {
                return new LighterComposite();
            }
            case Arithmetic: {
                return new ArithmeticComposite(attributeNode.getInt("k1", 0), attributeNode.getInt("k2", 0), attributeNode.getInt("k3", 0), attributeNode.getInt("k4", 0));
            }
        }
        throw new IllegalStateException();
    }

    private static final class LighterComposite
    extends AbstractBlendComposite
    implements AbstractBlendComposite.Blender {
        private LighterComposite() {
        }

        @Override
        @NotNull
        protected AbstractBlendComposite.Blender blender() {
            return this;
        }

        @Override
        public void blend(int @NotNull [] src, int @NotNull [] dst, int @NotNull [] result) {
            result[0] = Math.min(255, src[0] + dst[0]);
            result[1] = Math.min(255, src[1] + dst[1]);
            result[2] = Math.min(255, src[2] + dst[2]);
            result[3] = Math.min(255, src[3] + dst[3]);
        }
    }

    private static final class ArithmeticComposite
    extends AbstractBlendComposite
    implements AbstractBlendComposite.Blender {
        private final int k1;
        private final int k2;
        private final int k3;
        private final int k4;

        private ArithmeticComposite(int k1, int k2, int k3, int k4) {
            this.k1 = k1;
            this.k2 = k2;
            this.k3 = k3;
            this.k4 = k4;
        }

        @Override
        @NotNull
        protected AbstractBlendComposite.Blender blender() {
            return this;
        }

        @Override
        public void blend(int @NotNull [] src, int @NotNull [] dst, int @NotNull [] result) {
            result[0] = Math.max(0, Math.min(255, this.k1 * src[0] * dst[0] + this.k2 * src[0] + this.k3 * dst[0] + this.k4));
            result[1] = Math.max(0, Math.min(255, this.k1 * src[1] * dst[1] + this.k2 * src[1] + this.k3 * dst[1] + this.k4));
            result[2] = Math.max(0, Math.min(255, this.k1 * src[2] * dst[2] + this.k2 * src[2] + this.k3 * dst[2] + this.k4));
            result[3] = Math.max(0, Math.min(255, this.k1 * src[3] * dst[3] + this.k2 * src[3] + this.k3 * dst[3] + this.k4));
        }
    }
}

