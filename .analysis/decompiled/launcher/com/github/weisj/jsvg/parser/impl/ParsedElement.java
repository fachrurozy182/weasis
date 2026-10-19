/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.animation.BaseAnimationNode;
import com.github.weisj.jsvg.nodes.prototype.Container;
import com.github.weisj.jsvg.nodes.prototype.Renderable;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.DomElement;
import com.github.weisj.jsvg.parser.TextContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.parser.impl.CharacterDataParser;
import com.github.weisj.jsvg.parser.impl.ParsedDocument;
import com.github.weisj.jsvg.parser.impl.TextContentImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParsedElement
implements DomElement {
    @Nullable
    private final String id;
    @NotNull
    private final ParsedDocument document;
    @Nullable
    private final ParsedElement parent;
    @NotNull
    private final AttributeNode attributeNode;
    @NotNull
    private final SVGNode node;
    @NotNull
    private final @NotNull List<@NotNull ParsedElement> children = new ArrayList<ParsedElement>();
    @NotNull
    private final @NotNull List<@NotNull ParsedElement> indirectChildren = new ArrayList<ParsedElement>();
    @NotNull
    private final @NotNull Map<String, @NotNull List<@NotNull ParsedElement>> animationElements = new HashMap<String, List<ParsedElement>>();
    private TextContentImpl textContent = null;
    final CharacterDataParser characterDataParser;
    @NotNull
    private BuildStatus buildStatus = BuildStatus.NOT_BUILT;
    private int outgoingPaths = -1;

    ParsedElement(@Nullable String id, @NotNull ParsedDocument document, @Nullable ParsedElement parent, @NotNull AttributeNode element, @NotNull SVGNode node) {
        this.document = document;
        this.parent = parent;
        this.attributeNode = element;
        this.node = node;
        this.id = id;
        PermittedContent permittedContent = node.getClass().getAnnotation(PermittedContent.class);
        if (permittedContent == null) {
            throw new IllegalStateException("Element <" + node.tagName() + "> doesn't specify permitted content");
        }
        this.characterDataParser = permittedContent.charData() ? new CharacterDataParser() : null;
    }

    @Override
    @Nullable
    public String id() {
        return this.id;
    }

    @Override
    @NotNull
    public String tagName() {
        return this.attributeNode.tagName();
    }

    @Override
    @NotNull
    public @NotNull List<@NotNull String> classNames() {
        return this.attributeNode.classNames();
    }

    @Override
    @NotNull
    public ParsedDocument document() {
        return this.document;
    }

    @NotNull
    public List<ParsedElement> children() {
        return this.children;
    }

    @Override
    @Nullable
    public String attribute(@NotNull String name) {
        return this.attributeNode.getValue(name);
    }

    @Override
    public void setAttribute(@NotNull String name, @Nullable String value) {
        if (value == null) {
            this.attributeNode.attributes().remove(name);
        } else {
            this.attributeNode.attributes().put(name, value);
        }
    }

    @Override
    @NotNull
    public TextContentImpl textContent() {
        if (this.textContent == null) {
            this.textContent = new TextContentImpl(this);
        }
        return this.textContent;
    }

    @NotNull
    public Map<String, List<ParsedElement>> animationElements() {
        return this.animationElements;
    }

    @Override
    @Nullable
    public ParsedElement parent() {
        return this.parent;
    }

    @NotNull
    public SVGNode node() {
        return this.node;
    }

    @NotNull
    public SVGNode nodeEnsuringBuildStatus(int depth) {
        if (this.buildStatus == BuildStatus.IN_PROGRESS) {
            this.cyclicDependencyDetected();
        } else if (this.buildStatus == BuildStatus.NOT_BUILT) {
            this.build(depth);
        }
        return this.node;
    }

    @NotNull
    public AttributeNode attributeNode() {
        return this.attributeNode;
    }

    void addChild(@NotNull ParsedElement parsedElement) {
        if (Category.hasCategory(parsedElement.node, Category.Animation)) {
            String attributeName = BaseAnimationNode.attributeName(parsedElement.attributeNode());
            this.animationElements.computeIfAbsent(attributeName, k -> new ArrayList()).add(parsedElement);
        }
        this.children.add(parsedElement);
    }

    void addIndirectChild(@NotNull ParsedElement parsedElement) {
        this.indirectChildren.add(parsedElement);
    }

    private void addChildrenAndContent() {
        block7: {
            block6: {
                int i;
                if (!(this.node instanceof Container)) break block6;
                int contentListsSize = this.textContent == null ? 0 : this.textContent.contentLists().size();
                for (i = 0; i < this.children.size(); ++i) {
                    if (i < contentListsSize) {
                        assert (this.textContent != null);
                        this.addContentList(this.textContent.contentLists().get(i));
                    }
                    ParsedElement child = this.children.get(i);
                    ((Container)((Object)this.node)).addChild(child.id, child.node);
                }
                for (i = this.children.size(); i < contentListsSize; ++i) {
                    assert (this.textContent != null);
                    this.addContentList(this.textContent.contentLists().get(i));
                }
                break block7;
            }
            if (this.textContent == null) break block7;
            for (List<TextContent.Segment> contentList : this.textContent.contentLists()) {
                this.addContentList(contentList);
            }
        }
    }

    private void addContentList(@NotNull @NotNull List<@NotNull TextContent.Segment> contentList) {
        for (TextContent.Segment text : contentList) {
            this.node.addContent(text);
        }
    }

    void build(int depth) {
        if (this.buildStatus == BuildStatus.FINISHED) {
            return;
        }
        if (this.buildStatus == BuildStatus.IN_PROGRESS) {
            this.cyclicDependencyDetected();
            return;
        }
        this.buildStatus = BuildStatus.IN_PROGRESS;
        int maxNestingDepth = this.attributeNode.document().loaderContext().documentLimits().maxNestingDepth();
        if (depth > maxNestingDepth) {
            throw new IllegalStateException(String.format("Maximum nesting depth reached %d > %d.%n", depth, maxNestingDepth) + "Note: You can configure this using LoaderContext#documentLimits()");
        }
        this.addChildrenAndContent();
        this.attributeNode.prepareForNodeBuilding();
        for (ParsedElement child : this.children) {
            child.build(depth + 1);
        }
        this.document().setCurrentNestingDepth(depth);
        this.node.build(this.attributeNode);
        this.buildStatus = BuildStatus.FINISHED;
    }

    int outgoingPaths() {
        if (this.outgoingPaths == -1) {
            this.outgoingPaths = 0;
            for (ParsedElement child : this.children) {
                if (!(child.node instanceof Renderable)) continue;
                this.outgoingPaths += child.outgoingPaths();
            }
            for (ParsedElement child : this.indirectChildren) {
                this.outgoingPaths += child.outgoingPaths();
            }
            this.outgoingPaths = Math.max(this.outgoingPaths, 1);
        }
        return this.outgoingPaths;
    }

    public String toString() {
        return "ParsedElement{node=" + this.node + '}';
    }

    private void cyclicDependencyDetected() {
        throw new IllegalStateException("Cyclic dependency involving node '" + this.id + "' detected.");
    }

    private static enum BuildStatus {
        NOT_BUILT,
        IN_PROGRESS,
        FINISHED;

    }
}

