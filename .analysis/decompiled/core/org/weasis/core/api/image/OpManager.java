/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.image;

import java.util.List;
import java.util.Optional;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.OpEventListener;
import org.weasis.core.api.util.Copyable;
import org.weasis.opencv.data.PlanarImage;

public interface OpManager
extends OpEventListener,
Copyable<OpManager> {
    public void removeAllImageOperationAction();

    public void clearNodeParams();

    public void clearNodeIOCache();

    public List<ImageOpNode> getOperations();

    public void setFirstNode(PlanarImage var1);

    public Optional<PlanarImage> getFirstNodeInputImage();

    public Optional<ImageOpNode> getFirstNode();

    public Optional<ImageOpNode> getNode(String var1);

    public Optional<ImageOpNode> getLastNode();

    public Optional<PlanarImage> getLastNodeOutputImage();

    public Optional<PlanarImage> process();

    public Optional<Object> getParamValue(String var1, String var2);

    public <T> Optional<T> getParamValue(String var1, String var2, Class<T> var3);

    public boolean setParamValue(String var1, String var2, Object var3);

    public void removeParam(String var1, String var2);

    default public boolean needProcessing() {
        return this.getOperations().stream().anyMatch(op -> op.getParam("op.input.img") == null || op.getParam("op.output.img") == null);
    }

    default public boolean hasOperations() {
        return !this.getOperations().isEmpty();
    }

    default public int getOperationCount() {
        return this.getOperations().size();
    }
}

