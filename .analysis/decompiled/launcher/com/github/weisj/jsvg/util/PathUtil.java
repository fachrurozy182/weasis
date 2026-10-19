/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.util;

import com.github.weisj.jsvg.attributes.FillRule;
import com.github.weisj.jsvg.attributes.value.ConstantValue;
import com.github.weisj.jsvg.attributes.value.Value;
import com.github.weisj.jsvg.geometry.FillRuleAwareAWTSVGShape;
import com.github.weisj.jsvg.geometry.SVGShape;
import com.github.weisj.jsvg.geometry.path.BuildHistory;
import com.github.weisj.jsvg.geometry.path.PathCommand;
import com.github.weisj.jsvg.geometry.path.PathParser;
import java.awt.geom.Path2D;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PathUtil {
    @Nullable
    private static final MethodHandle trimPathHandle = PathUtil.lookupTrimPathMethod();

    @Nullable
    private static MethodHandle lookupTrimPathMethod() {
        try {
            MethodType methodType = MethodType.methodType(Void.TYPE);
            return MethodHandles.lookup().findVirtual(Path2D.class, "trimToSize", methodType);
        }
        catch (IllegalAccessException | NoSuchMethodException e) {
            return null;
        }
    }

    private PathUtil() {
    }

    @NotNull
    public static SVGShape parseFromPathData(@NotNull String data, FillRule fillRule) {
        PathCommand[] pathCommands = new PathParser(data).parsePathCommand();
        int nodeCount = 2;
        for (PathCommand pathCommand : pathCommands) {
            nodeCount += pathCommand.nodeCount() - 1;
        }
        Path2D.Float path = new Path2D.Float(fillRule.awtWindingRule, nodeCount);
        BuildHistory hist = new BuildHistory();
        for (PathCommand pathCommand : pathCommands) {
            pathCommand.appendPath(path, hist);
        }
        PathUtil.trimPathToSize(path);
        return new FillRuleAwareAWTSVGShape((Value<Path2D>)new ConstantValue<Path2D>(path));
    }

    @NotNull
    public static Path2D setPolyLine(@Nullable Path2D path, float @NotNull [] points, boolean closed) {
        Path2D p;
        if (path == null) {
            p = new Path2D.Float(0, points.length / 2);
        } else {
            p = path;
            p.reset();
        }
        p.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length - 1; i += 2) {
            p.lineTo(points[i], points[i + 1]);
        }
        if (closed) {
            p.closePath();
        }
        return p;
    }

    public static void trimPathToSize(@NotNull Path2D path) {
        if (trimPathHandle != null) {
            try {
                trimPathHandle.invokeExact(path);
            }
            catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}

