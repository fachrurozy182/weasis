/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.attributes.filter.TransferFunctionType;
import com.github.weisj.jsvg.nodes.AbstractSVGNode;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.Set;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TransferFunctionElement
extends AbstractSVGNode {
    static final byte[] IDENTITY_LOOKUP_TABLE = new byte[256];
    private final Channel channel;
    private TransferFunctionType type;
    private byte[] lookupTable;

    private TransferFunctionElement(Channel channel) {
        this.channel = channel;
    }

    public Channel channel() {
        return this.channel;
    }

    public TransferFunctionType type() {
        return this.type;
    }

    public byte @NotNull [] lookupTable() {
        return this.lookupTable;
    }

    @Override
    @MustBeInvokedByOverriders
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.type = attributeNode.getEnum("type", TransferFunctionType.Identity);
        byte[] table = TransferFunctionElement.createLookupTable(this.type, attributeNode);
        if (table == null) {
            this.type = TransferFunctionType.Identity;
            this.lookupTable = IDENTITY_LOOKUP_TABLE;
        } else {
            this.lookupTable = table;
        }
    }

    private static byte @Nullable [] createLookupTable(TransferFunctionType type, @NotNull AttributeNode attributeNode) {
        switch (type) {
            case Table: 
            case Discrete: {
                float[] table = attributeNode.getFloatList("tableValues");
                if (table.length == 0) {
                    return null;
                }
                int[] intTable = new int[table.length];
                for (int i = 0; i < table.length; ++i) {
                    intTable[i] = (int)(255.0f * table[i]);
                }
                return TransferFunctionElement.createTableBasedLookupTable(type, intTable);
            }
            case Linear: {
                float slope = attributeNode.getFloat("slope", 1.0f);
                float intercept = attributeNode.getFloat("intercept", 0.0f);
                if (slope == 1.0f && intercept == 0.0f) {
                    return null;
                }
                return TransferFunctionElement.createLinearLookupTable(intercept, slope);
            }
            case Gamma: {
                float amplitude = attributeNode.getFloat("amplitude", 1.0f);
                float exponent = attributeNode.getFloat("exponent", 1.0f);
                float offset = attributeNode.getFloat("offset", 0.0f);
                if (amplitude == 1.0f && exponent == 1.0f && offset == 0.0f) {
                    return null;
                }
                return TransferFunctionElement.createGammaLookupTable(amplitude, exponent, offset);
            }
            case Identity: {
                return IDENTITY_LOOKUP_TABLE;
            }
        }
        return null;
    }

    private static byte @Nullable [] createTableBasedLookupTable(TransferFunctionType type, int[] intTable) {
        int n = intTable.length;
        byte[] lookupTable = new byte[256];
        switch (type) {
            case Table: {
                for (int j = 0; j <= 255; ++j) {
                    float fi = (float)(j * (n - 1)) / 255.0f;
                    int k = (int)Math.floor(fi);
                    int kNext = Math.min(k + 1, n - 1);
                    float r = fi - (float)k;
                    int value = (int)((float)intTable[k] + r * (float)(intTable[kNext] - intTable[k])) & 0xFF;
                    lookupTable[j] = (byte)value;
                }
                break;
            }
            case Discrete: {
                for (int j = 0; j <= 255; ++j) {
                    int i = (int)Math.floor((float)(j * n) / 255.0f);
                    if (i == n) {
                        i = n - 1;
                    }
                    lookupTable[j] = (byte)(intTable[i] & 0xFF);
                }
                break;
            }
            default: {
                return null;
            }
        }
        return lookupTable;
    }

    private static byte @Nullable [] createLinearLookupTable(float intercept, float slope) {
        byte[] table = new byte[256];
        float intIntercept = intercept * 255.0f + 0.5f;
        for (int j = 0; j <= 255; ++j) {
            int value = (int)(slope * (float)j + intIntercept);
            value = Math.max(0, Math.min(255, value));
            table[j] = (byte)(0xFF & value);
        }
        return table;
    }

    private static byte @Nullable [] createGammaLookupTable(float amplitude, float exponent, float offset) {
        byte[] table = new byte[256];
        for (int j = 0; j <= 255; ++j) {
            int value = (int)Math.round(255.0 * ((double)amplitude * Math.pow((float)j / 255.0f, exponent) + (double)offset));
            value = Math.max(0, Math.min(255, value));
            table[j] = (byte)(value & 0xFF);
        }
        return table;
    }

    static {
        for (int i = 0; i < 256; ++i) {
            TransferFunctionElement.IDENTITY_LOOKUP_TABLE[i] = (byte)i;
        }
    }

    public static enum Channel {
        Red,
        Green,
        Blue,
        Alpha;

    }

    @ElementCategories(value={Category.TransferFunctionElement})
    @PermittedContent(anyOf={Animate.class, Set.class})
    public static final class FeFuncA
    extends TransferFunctionElement {
        public static final String TAG = "fefunca";

        public FeFuncA() {
            super(Channel.Alpha);
        }

        @Override
        @NotNull
        public String tagName() {
            return TAG;
        }
    }

    @ElementCategories(value={Category.TransferFunctionElement})
    @PermittedContent(anyOf={Animate.class, Set.class})
    public static final class FeFuncB
    extends TransferFunctionElement {
        public static final String TAG = "fefuncb";

        public FeFuncB() {
            super(Channel.Blue);
        }

        @Override
        @NotNull
        public String tagName() {
            return TAG;
        }
    }

    @ElementCategories(value={Category.TransferFunctionElement})
    @PermittedContent(anyOf={Animate.class, Set.class})
    public static final class FeFuncG
    extends TransferFunctionElement {
        public static final String TAG = "fefuncg";

        public FeFuncG() {
            super(Channel.Green);
        }

        @Override
        @NotNull
        public String tagName() {
            return TAG;
        }
    }

    @ElementCategories(value={Category.TransferFunctionElement})
    @PermittedContent(anyOf={Animate.class, Set.class})
    public static final class FeFuncR
    extends TransferFunctionElement {
        public static final String TAG = "fefuncr";

        public FeFuncR() {
            super(Channel.Red);
        }

        @Override
        @NotNull
        public String tagName() {
            return TAG;
        }
    }
}

