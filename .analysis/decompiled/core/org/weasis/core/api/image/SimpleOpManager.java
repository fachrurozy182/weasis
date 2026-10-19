/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.OpManager;
import org.weasis.opencv.data.PlanarImage;

public class SimpleOpManager
implements OpManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleOpManager.class);
    private static final String IMAGE_OP_NAME = Messages.getString("SimpleOpManager.img_op");
    private final Map<String, ImageOpNode> nodesByName;
    private final List<ImageOpNode> operations = new ArrayList<ImageOpNode>();
    private volatile String name;

    public SimpleOpManager() {
        this(IMAGE_OP_NAME);
    }

    public SimpleOpManager(String name) {
        this.nodesByName = new HashMap<String, ImageOpNode>();
        this.setName(name);
    }

    public SimpleOpManager(SimpleOpManager other) {
        this(other.name);
        other.operations.forEach(node -> {
            ImageOpNode copiedNode = (ImageOpNode)node.copy();
            this.operations.add(copiedNode);
            this.nodesByName.put(copiedNode.getName(), copiedNode);
        });
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, IMAGE_OP_NAME);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public List<ImageOpNode> getOperations() {
        return List.copyOf(this.operations);
    }

    public void addImageOperationAction(ImageOpNode action) {
        this.addImageOperationAction(action, null, null);
    }

    public void addImageOperationAction(ImageOpNode action, Position pos, ImageOpNode positionRef) {
        if (action == null) {
            return;
        }
        String uniqueName = this.ensureUniqueName(action.getName());
        action.setName(uniqueName);
        this.nodesByName.put(uniqueName, action);
        int index = this.calculateInsertIndex(pos, positionRef);
        if (index >= 0 && index < this.operations.size()) {
            this.operations.add(index, action);
        } else {
            this.operations.add(action);
        }
    }

    private String ensureUniqueName(String baseName) {
        if (!this.nodesByName.containsKey(baseName)) {
            return baseName;
        }
        LOGGER.warn("Operation name '{}' already exists, generating unique name", (Object)baseName);
        return baseName + " " + this.findNextAvailableSuffix(baseName);
    }

    private int findNextAvailableSuffix(String baseName) {
        return IntStream.iterate(2, i -> i + 1).filter(i -> !this.nodesByName.containsKey("%s %d".formatted(new Object[]{baseName, i}))).findFirst().orElseThrow();
    }

    private int calculateInsertIndex(Position pos, ImageOpNode positionRef) {
        if (positionRef == null) {
            return -1;
        }
        int index = this.operations.indexOf(positionRef);
        return index < 0 ? -1 : (pos == Position.AFTER ? index + 1 : index);
    }

    public void removeImageOperationAction(ImageOpNode action) {
        if (action == null) {
            return;
        }
        this.operations.remove(action);
        this.nodesByName.values().removeIf(node -> node == action);
    }

    @Override
    public void removeAllImageOperationAction() {
        this.clearNodeParams();
        this.operations.clear();
        this.nodesByName.clear();
    }

    @Override
    public void clearNodeParams() {
        this.operations.forEach(ImageOpNode::clearParams);
    }

    @Override
    public void clearNodeIOCache() {
        this.operations.forEach(ImageOpNode::clearIOCache);
    }

    @Override
    public void setFirstNode(PlanarImage imgSource) {
        this.getFirstNode().ifPresent(node -> node.setParam("op.input.img", imgSource));
    }

    @Override
    public Optional<PlanarImage> getFirstNodeInputImage() {
        return this.getFirstNode().map(node -> (PlanarImage)node.getParam("op.input.img"));
    }

    @Override
    public Optional<ImageOpNode> getFirstNode() {
        return this.operations.isEmpty() ? Optional.empty() : Optional.of((ImageOpNode)this.operations.getFirst());
    }

    @Override
    public Optional<ImageOpNode> getNode(String opName) {
        return Optional.ofNullable(this.nodesByName.get(opName));
    }

    @Override
    public Optional<ImageOpNode> getLastNode() {
        return this.operations.isEmpty() ? Optional.empty() : Optional.of((ImageOpNode)this.operations.getLast());
    }

    @Override
    public Optional<PlanarImage> getLastNodeOutputImage() {
        return this.getLastNode().map(node -> (PlanarImage)node.getParam("op.output.img"));
    }

    public void resetLastNodeOutputImage() {
        this.getLastNode().ifPresent(node -> node.setParam("op.output.img", null));
    }

    @Override
    public Optional<PlanarImage> process() {
        PlanarImage source = this.getFirstNodeInputImage().orElse(null);
        if (source == null || source.width() <= 0) {
            this.clearNodeIOCache();
            return this.getLastNodeOutputImage();
        }
        for (int i = 0; i < this.operations.size(); ++i) {
            ImageOpNode current = this.operations.get(i);
            if (i > 0) {
                ImageOpNode previous = this.operations.get(i - 1);
                current.setParam("op.input.img", previous.getParam("op.output.img"));
            }
            this.executeOperation(current);
        }
        return this.getLastNodeOutputImage();
    }

    private void executeOperation(ImageOpNode operation) {
        try {
            if (operation.isEnabled()) {
                operation.process();
            } else {
                operation.setParam("op.output.img", operation.getParam("op.input.img"));
            }
        }
        catch (Exception e) {
            LOGGER.error("Operation '{}' failed", operation.getParam("op.display.name"), (Object)e);
            operation.setParam("op.output.img", operation.getParam("op.input.img"));
        }
    }

    @Override
    public Optional<Object> getParamValue(String opName, String param) {
        return this.getNode(opName).map(node -> node.getParam(param));
    }

    @Override
    public <T> Optional<T> getParamValue(String opName, String param, Class<T> ignored) {
        return this.getNode(opName).map(node -> node.getParam(param, ignored));
    }

    @Override
    public boolean setParamValue(String opName, String param, Object value) {
        if (opName == null || param == null) {
            return false;
        }
        return this.getNode(opName).map(node -> {
            node.setParam(param, value);
            return true;
        }).orElse(false);
    }

    @Override
    public void removeParam(String opName, String param) {
        if (opName != null && param != null) {
            this.getNode(opName).ifPresent(node -> node.removeParam(param));
        }
    }

    @Override
    public void handleImageOpEvent(ImageOpEvent event) {
        this.operations.forEach(node -> node.handleImageOpEvent(event));
    }

    @Override
    public SimpleOpManager copy() {
        return new SimpleOpManager(this);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleOpManager)) return false;
        SimpleOpManager other = (SimpleOpManager)obj;
        if (!Objects.equals(this.name, other.name)) return false;
        if (!Objects.equals(this.operations, other.operations)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.name, this.operations);
    }

    public static enum Position {
        BEFORE,
        AFTER;

    }
}

