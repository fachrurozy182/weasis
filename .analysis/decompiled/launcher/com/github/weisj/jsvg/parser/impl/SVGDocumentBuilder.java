/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.nodes.SVG;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.Use;
import com.github.weisj.jsvg.nodes.container.CommonRenderableContainerNode;
import com.github.weisj.jsvg.parser.DomProcessor;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.css.CssParser;
import com.github.weisj.jsvg.parser.css.StyleSheet;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.parser.impl.AttributeParser;
import com.github.weisj.jsvg.parser.impl.DocumentConstructorAccessor;
import com.github.weisj.jsvg.parser.impl.LoadHelper;
import com.github.weisj.jsvg.parser.impl.NodeSupplier;
import com.github.weisj.jsvg.parser.impl.ParsedDocument;
import com.github.weisj.jsvg.parser.impl.ParsedElement;
import com.github.weisj.jsvg.parser.impl.StringSegment;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SVGDocumentBuilder {
    @NotNull
    private final ParsedDocument parsedDocument;
    @NotNull
    private final @NotNull List<@NotNull Use> useElements = new ArrayList<Use>();
    @NotNull
    private final @NotNull List<@NotNull ParsedElement> styleElements = new ArrayList<ParsedElement>();
    @NotNull
    private final @NotNull List<@NotNull StyleSheet> styleSheets = new ArrayList<StyleSheet>();
    @NotNull
    private final @NotNull Deque<@NotNull ParsedElement> currentNodeStack = new ArrayDeque<ParsedElement>();
    @NotNull
    private final LoaderContext loaderContext;
    @NotNull
    private final NodeSupplier nodeSupplier;
    private ParsedElement rootNode;

    public SVGDocumentBuilder(@Nullable URI rootURI, @NotNull LoaderContext loaderContext, @NotNull NodeSupplier nodeSupplier) {
        LoadHelper loadHelper = new LoadHelper(new AttributeParser(loaderContext.paintParser()), loaderContext);
        this.loaderContext = loaderContext;
        this.nodeSupplier = nodeSupplier;
        this.parsedDocument = new ParsedDocument(rootURI, loaderContext, loadHelper);
    }

    @ApiStatus.Internal
    @NotNull
    ParsedDocument parsedDocument() {
        return this.parsedDocument;
    }

    public void startDocument() {
        if (this.rootNode != null) {
            throw new IllegalStateException("Document already started");
        }
    }

    public void endDocument() {
        if (this.rootNode == null) {
            throw new IllegalStateException("Document is empty");
        }
    }

    public boolean startElement(@NotNull String tagName, @NotNull Map<String, String> attributes) {
        SVGNode newNode;
        ParsedElement parentElement;
        ParsedElement parsedElement = parentElement = !this.currentNodeStack.isEmpty() ? this.currentNodeStack.peek() : null;
        if (parentElement != null) {
            this.flushText(parentElement, true);
        }
        if ((newNode = this.nodeSupplier.create(tagName)) == null) {
            return false;
        }
        AttributeNode attributeNode = new AttributeNode(tagName, attributes, this.styleSheets);
        String id = attributes.get("id");
        ParsedElement parsedElement2 = new ParsedElement(id, this.parsedDocument, parentElement, attributeNode, newNode);
        attributeNode.setElement(parsedElement2);
        if (id != null && !this.parsedDocument.hasElementWithId(id)) {
            this.parsedDocument.registerNamedElement(id, parsedElement2);
        }
        if (parentElement != null) {
            parentElement.addChild(parsedElement2);
        }
        if (this.rootNode == null) {
            this.rootNode = parsedElement2;
        }
        if (parsedElement2.node() instanceof Style) {
            this.styleElements.add(parsedElement2);
        }
        if (parsedElement2.node() instanceof Use) {
            this.useElements.add((Use)parsedElement2.node());
        }
        this.currentNodeStack.push(parsedElement2);
        return true;
    }

    public void addTextContent(char @NotNull [] characterData, int startOffset, int endOffset) {
        if (this.currentNodeStack.isEmpty()) {
            throw new IllegalStateException("Adding text content without a current node");
        }
        ParsedElement currentElement = this.currentNodeStack.peek();
        if (currentElement.characterDataParser == null) {
            return;
        }
        currentElement.characterDataParser.append(characterData, startOffset, endOffset);
    }

    public void endElement(@NotNull String tagName) {
        if (this.currentNodeStack.isEmpty()) {
            throw new IllegalStateException("No current node to end");
        }
        ParsedElement currentElement = this.currentNodeStack.pop();
        String currentNodeTagName = currentElement.attributeNode().tagName();
        if (!currentNodeTagName.equals(tagName)) {
            throw new IllegalStateException(String.format("Closing tag %s doesn't match current node %s)", tagName, currentNodeTagName));
        }
        this.flushText(currentElement, false);
    }

    private void flushText(@NotNull ParsedElement element, boolean segmentBreak) {
        if (element.characterDataParser != null && element.characterDataParser.canFlush(segmentBreak)) {
            element.textContent().currentContentList().add(new StringSegment(element.characterDataParser.flush(segmentBreak)));
        }
    }

    void preProcess() {
        if (this.rootNode == null) {
            throw new IllegalStateException("No root node");
        }
        DomProcessor preProcessor = this.loaderContext.preProcessor();
        if (preProcessor != null) {
            preProcessor.process(this.rootNode);
        }
    }

    @NotNull
    public SVGDocument build() {
        this.preProcess();
        this.processStyleSheets();
        this.rootNode.build(0);
        this.validatePathCount();
        this.validateUseElementsDepth();
        return DocumentConstructorAccessor.constructor().create((SVG)this.rootNode.node());
    }

    private void processStyleSheets() {
        if (this.styleElements.isEmpty()) {
            return;
        }
        CssParser cssParser = this.loaderContext.cssParser();
        for (ParsedElement styleElement : this.styleElements) {
            styleElement.build(0);
            Style styleNode = (Style)styleElement.node();
            styleNode.parseStyleSheet(cssParser);
            this.styleSheets.add(styleNode.styleSheet());
        }
    }

    private void validatePathCount() {
        int maxPathCount;
        int pathCount = this.rootNode.outgoingPaths();
        if (pathCount > (maxPathCount = this.parsedDocument.loaderContext().documentLimits().maxPathCount())) {
            throw new IllegalStateException(String.format("Maximum count of rendered element instances exceeded %d > %d.%n", pathCount, maxPathCount) + "Note: You can configure this using LoaderContext#documentLimits()");
        }
    }

    private void validateUseElementsDepth() {
        if (this.useElements.isEmpty()) {
            return;
        }
        HashMap<SVGNode, Integer> checkedNodes = new HashMap<SVGNode, Integer>();
        int useNestingLimit = this.parsedDocument.loaderContext().documentLimits().maxUseNestingDepth();
        for (Use useElement : this.useElements) {
            int depth = this.nestingDepthOf(useElement, checkedNodes);
            if (depth <= useNestingLimit) continue;
            throw new IllegalStateException(String.format("Maximum nesting depth for <use> exceeded %d > %d starting from node with id '%s'%n", depth, useNestingLimit, useElement.id()) + "Note: You can configure this using LoaderContext#documentLimits()");
        }
    }

    private int nestingDepthOf(@NotNull SVGNode node, @NotNull Map<SVGNode, Integer> checkedNodes) {
        int cached = checkedNodes.getOrDefault(node, -1);
        if (cached >= 0) {
            return cached;
        }
        int depth = 0;
        if (node instanceof Use) {
            SVGNode referenced = ((Use)node).referencedNode();
            if (referenced != null) {
                depth = this.nestingDepthOf(referenced, checkedNodes) + 1;
            }
        } else if (node instanceof CommonRenderableContainerNode) {
            for (SVGNode sVGNode : ((CommonRenderableContainerNode)node).children()) {
                depth = Math.max(depth, this.nestingDepthOf(sVGNode, checkedNodes));
            }
        }
        checkedNodes.put(node, depth);
        return depth;
    }
}

