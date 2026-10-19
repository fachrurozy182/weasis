/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.mesh;

import com.github.weisj.jsvg.attributes.UnitType;
import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.AnimateTransform;
import com.github.weisj.jsvg.nodes.animation.Set;
import com.github.weisj.jsvg.nodes.container.ContainerNode;
import com.github.weisj.jsvg.nodes.mesh.MeshBuilder;
import com.github.weisj.jsvg.nodes.mesh.MeshPatch;
import com.github.weisj.jsvg.nodes.mesh.MeshRow;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.paint.SVGPaint;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.output.Output;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(value={Category.Gradient})
@PermittedContent(categories={Category.Descriptive}, anyOf={MeshRow.class, Animate.class, AnimateTransform.class, Set.class})
public final class MeshGradient
extends ContainerNode
implements SVGPaint {
    public static final String TAG = "meshgradient";
    private Length x;
    private Length y;
    private UnitType gradientUnits;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.x = attributeNode.getLength("x", PercentageDimension.WIDTH, 0.0f);
        this.y = attributeNode.getLength("y", PercentageDimension.HEIGHT, 0.0f);
        this.gradientUnits = attributeNode.getEnum("gradientUnits", UnitType.ObjectBoundingBox);
        MeshBuilder.buildMesh(this, new Point2D.Float(this.x.raw(), this.y.raw()));
    }

    public void renderMesh(@NotNull MeasureContext measure, @NotNull Output output) {
        Output meshOutput = output.createChild();
        meshOutput.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (SVGNode sVGNode : this.children()) {
            MeshRow row = (MeshRow)sVGNode;
            for (SVGNode sVGNode2 : row.children()) {
                MeshPatch patch = (MeshPatch)sVGNode2;
                patch.renderPath(meshOutput);
            }
        }
        meshOutput.dispose();
    }

    @Override
    public void fillShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        Output.SafeState safeState = output.safeState();
        Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
        output.applyClip(shape);
        output.translate(b.getX(), b.getY());
        this.renderMesh(context.measureContext(), output);
        safeState.restore();
    }

    @Override
    public void drawShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        Output.SafeState safeState = output.safeState();
        Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
        output.applyClip(output.stroke().createStrokedShape(shape));
        output.translate(b.getX(), b.getY());
        this.renderMesh(context.measureContext(), output);
        safeState.restore();
    }
}

