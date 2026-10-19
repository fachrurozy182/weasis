/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.animation.time.Duration;
import com.github.weisj.jsvg.animation.value.AnimatedColor;
import com.github.weisj.jsvg.animation.value.AnimatedFloatList;
import com.github.weisj.jsvg.animation.value.AnimatedLength;
import com.github.weisj.jsvg.animation.value.AnimatedPaint;
import com.github.weisj.jsvg.animation.value.AnimatedPercentage;
import com.github.weisj.jsvg.animation.value.AnimatedTransform;
import com.github.weisj.jsvg.animation.value.NeutralElements;
import com.github.weisj.jsvg.attributes.Animatable;
import com.github.weisj.jsvg.attributes.Inherited;
import com.github.weisj.jsvg.attributes.filter.FilterChannelKey;
import com.github.weisj.jsvg.attributes.transform.TransformPart;
import com.github.weisj.jsvg.attributes.value.ColorValue;
import com.github.weisj.jsvg.attributes.value.ConstantFloatList;
import com.github.weisj.jsvg.attributes.value.ConstantLengthTransform;
import com.github.weisj.jsvg.attributes.value.ConstantTransform;
import com.github.weisj.jsvg.attributes.value.FloatListValue;
import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.attributes.value.PercentageValue;
import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Percentage;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.AnimateTransform;
import com.github.weisj.jsvg.nodes.animation.BaseAnimationNode;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.paint.SVGPaint;
import com.github.weisj.jsvg.paint.impl.PredefinedPaints;
import com.github.weisj.jsvg.parser.PaintParser;
import com.github.weisj.jsvg.parser.css.CssParser;
import com.github.weisj.jsvg.parser.css.StyleSheet;
import com.github.weisj.jsvg.parser.css.impl.SimpleCssParser;
import com.github.weisj.jsvg.parser.impl.AttributeParser;
import com.github.weisj.jsvg.parser.impl.LoadHelper;
import com.github.weisj.jsvg.parser.impl.ParsedDocument;
import com.github.weisj.jsvg.parser.impl.ParsedElement;
import com.github.weisj.jsvg.parser.impl.SeparatorMode;
import com.github.weisj.jsvg.parser.resources.ResourceLoader;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.animation.AnimationState;
import com.github.weisj.jsvg.util.AttributeUtil;
import com.github.weisj.jsvg.view.ViewBox;
import java.awt.Color;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AttributeNode {
    private static final Length Top = new Length(Unit.PERCENTAGE_HEIGHT, 0.0f);
    private static final Length CenterHeight = new Length(Unit.PERCENTAGE_HEIGHT, 50.0f);
    private static final Length Bottom = new Length(Unit.PERCENTAGE_HEIGHT, 100.0f);
    private static final Length Left = new Length(Unit.PERCENTAGE_WIDTH, 0.0f);
    private static final Length CenterWidth = new Length(Unit.PERCENTAGE_WIDTH, 50.0f);
    private static final Length Right = new Length(Unit.PERCENTAGE_WIDTH, 100.0f);
    private static final Length FALLBACK_LENGTH = new Length(Unit.RAW, 0.0f);
    private static final Percentage FALLBACK_PERCENTAGE = new Percentage(1.0f);
    private static final MeasureContext DUMMY_MEASURE_CONTEXT = new MeasureContext(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, new AnimationState(0L, 0L));
    @NotNull
    private final String tagName;
    @NotNull
    private final Map<String, String> attributes;
    @NotNull
    private final @NotNull List<@NotNull StyleSheet> styleSheets;
    private ParsedElement element = null;

    public AttributeNode(@NotNull String tagName, @NotNull Map<String, String> attributes, @NotNull @NotNull List<@NotNull StyleSheet> styleSheets) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.styleSheets = styleSheets;
    }

    void setElement(ParsedElement element) {
        this.element = element;
    }

    @NotNull
    private LoadHelper loadHelper() {
        return this.document().loadHelper();
    }

    @NotNull
    public AttributeNode copy() {
        AttributeNode node = new AttributeNode(this.tagName, new HashMap<String, String>(this.attributes), this.styleSheets);
        node.setElement(this.element);
        return node;
    }

    void prepareForNodeBuilding() {
        HashMap styleSheetAttributes = new HashMap();
        String styleStr = this.attributes.get("style");
        if (styleStr != null && !AttributeUtil.isBlank(styleStr)) {
            CssParser cssParser = this.element.document().loaderContext().cssParser();
            if (cssParser instanceof SimpleCssParser) {
                ((SimpleCssParser)cssParser).parseRules(Collections.singletonList(styleStr.toCharArray())).forEach(p -> styleSheetAttributes.put(p.name(), p.value()));
            } else {
                StyleSheet sheet = cssParser.parse(Arrays.asList(this.element.tagName().toCharArray(), {'{'}, styleStr.toCharArray(), {'}'}));
                sheet.forEachMatchingRule(this.element, p -> styleSheetAttributes.put(p.name(), p.value()));
            }
        }
        List<StyleSheet> sheets = this.styleSheets();
        for (int i = sheets.size() - 1; i >= 0; --i) {
            StyleSheet sheet = sheets.get(i);
            sheet.forEachMatchingRule(this.element, p -> {
                if (!styleSheetAttributes.containsKey(p.name())) {
                    styleSheetAttributes.put(p.name(), p.value());
                }
            });
        }
        this.attributes.putAll(styleSheetAttributes);
    }

    @NotNull
    public ParsedDocument document() {
        return this.element().document();
    }

    @NotNull
    public ParsedElement element() {
        return this.element;
    }

    @NotNull
    @NotNull List<@NotNull StyleSheet> styleSheets() {
        return this.styleSheets;
    }

    @Nullable
    private <T> T getElementByUrl(@NotNull Class<T> type, @Nullable String value) {
        if (value == null) {
            return null;
        }
        return this.loadHelper().elementLoader().loadElement(type, value, this.document());
    }

    private <T> T recordIndirectChild(T child, String value, ElementRelation relation) {
        ParsedElement containingElement;
        if (child != null && relation == ElementRelation.PAINTED_CHILD && (containingElement = this.getElementByUrl(ParsedElement.class, value)) != null) {
            this.element().addIndirectChild(containingElement);
        }
        return child;
    }

    @Nullable
    public <T> T getElementByHref(@NotNull Class<T> type, @Nullable String value, ElementRelation relation) {
        return this.recordIndirectChild(this.getElementByUrl(type, value), value, relation);
    }

    @Nullable
    public <T> T getElementByHref(@NotNull Class<T> type, @NotNull Category category, @Nullable String value, ElementRelation relation) {
        T e = this.getElementByUrl(type, value);
        if (e == null) {
            return null;
        }
        for (Category cat : e.getClass().getAnnotation(ElementCategories.class).value()) {
            if (cat != category) continue;
            return this.recordIndirectChild(e, value, relation);
        }
        return null;
    }

    @NotNull
    public Map<String, String> attributes() {
        return this.attributes;
    }

    @NotNull
    public String tagName() {
        return this.tagName;
    }

    public boolean tagIsOneOf(String ... tags) {
        for (String tag : tags) {
            if (!this.tagName.equals(tag)) continue;
            return true;
        }
        return false;
    }

    @NotNull
    public @NotNull List<@NotNull String> classNames() {
        @NotNull String @NotNull [] classes = this.getStringList("class", SeparatorMode.WHITESPACE_ONLY);
        if (classes.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(classes);
    }

    @Nullable
    public String getValue(@NotNull String key) {
        return this.attributes.get(key);
    }

    @NotNull
    public Color getColor(@NotNull String key) {
        return this.getColor(key, PaintParser.DEFAULT_COLOR);
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    public Color getColor(@NotNull String key, @Nullable Color fallback) {
        String value = this.getValue(key);
        if (value == null) {
            return fallback;
        }
        Color c = this.loadHelper().attributeParser().paintParser().parseColor(value.toLowerCase(Locale.ENGLISH));
        return c != null ? c : fallback;
    }

    @Nullable
    public SVGPaint getPaint(@NotNull String key, Inherited inherited, Animatable animatable) {
        return this.getPaint(key, null, inherited, animatable);
    }

    @Contract(value="_,!null,_,_ -> !null")
    @Nullable
    public SVGPaint getPaint(@NotNull String key, @Nullable SVGPaint fallback, Inherited inherited, Animatable animatable) {
        SVGPaint value = this.getPaintInternal(key, fallback);
        if (animatable == Animatable.YES) {
            AnimatedPaint animatedPaint;
            SVGPaint initial = value;
            if (initial == null) {
                SVGPaint sVGPaint = initial = inherited == Inherited.YES ? PredefinedPaints.INHERITED : PredefinedPaints.DEFAULT_PAINT;
            }
            if ((animatedPaint = this.getAnimatedPaint(key, initial)) != null) {
                return animatedPaint;
            }
        }
        return value;
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    private SVGPaint getPaintInternal(@NotNull String key, @Nullable SVGPaint fallback) {
        SVGPaint paint = this.parsePaint(this.getValue(key));
        if (paint == null) {
            return fallback;
        }
        return paint;
    }

    @Nullable
    public SVGPaint parsePaint(@Nullable String value) {
        if (value == null) {
            return null;
        }
        SVGPaint paint = this.getElementByHref(SVGPaint.class, value, ElementRelation.PAINT_SERVER);
        if (paint != null) {
            return paint;
        }
        return this.loadHelper().attributeParser().parsePaint(value, this);
    }

    @Nullable
    public Length getLength(@NotNull String key, @NotNull PercentageDimension dimension) {
        return this.getLength(key, dimension, null);
    }

    @NotNull
    public Length getLength(@NotNull String key, @NotNull PercentageDimension dimension, float fallback) {
        return this.getLength(key, dimension, Unit.RAW.valueOf(fallback));
    }

    @Contract(value="_,_,!null -> !null")
    @Nullable
    public Length getLength(@NotNull String key, @NotNull PercentageDimension dimension, @Nullable Length fallback) {
        return (Length)this.getLength(key, dimension, fallback, Inherited.NO, Animatable.NO);
    }

    @Nullable
    public LengthValue getLength(@NotNull String key, @NotNull PercentageDimension dimension, Inherited inherited, Animatable animatable) {
        return this.getLength(key, dimension, null, inherited, animatable);
    }

    @Contract(value="_,_,!null,_,_ -> !null")
    @Nullable
    public LengthValue getLength(@NotNull String key, @NotNull PercentageDimension dimension, @Nullable LengthValue fallback, Inherited inherited, Animatable animatable) {
        LengthValue value = this.getLengthInternal(key, dimension);
        if (value == FALLBACK_LENGTH) {
            value = fallback;
        }
        if (animatable == Animatable.YES) {
            AnimatedLength animatedLength;
            LengthValue initial = value;
            if (initial == null) {
                LengthValue lengthValue = initial = inherited == Inherited.YES ? Length.INHERITED : NeutralElements.NEUTRAL_LENGTH;
            }
            if (initial instanceof AnimatedLength) {
                initial = ((AnimatedLength)initial).initial();
            }
            if ((animatedLength = this.getAnimatedLength(key, initial, dimension)) != null) {
                return animatedLength;
            }
        }
        return value;
    }

    @NotNull
    public Duration getDuration(@NotNull String key, @NotNull Duration fallback) {
        return this.loadHelper().attributeParser().parseTimeOffsetValue(this.getValue(key), fallback);
    }

    @NotNull
    private Length getLengthInternal(@NotNull String key, @NotNull PercentageDimension dimension) {
        return this.loadHelper().attributeParser().parseLength(this.getValue(key), FALLBACK_LENGTH, dimension);
    }

    @NotNull
    public Length getHorizontalReferenceLengthFromKey(@NotNull String key) {
        return this.getHorizontalReferenceLength(this.getValue(key));
    }

    @NotNull
    public Length getVerticalReferenceLengthFromKey(@NotNull String key) {
        return this.getVerticalReferenceLength(this.getValue(key));
    }

    @NotNull
    public Length getHorizontalReferenceLength(@Nullable String value) {
        if ("left".equals(value)) {
            return Left;
        }
        if ("center".equals(value)) {
            return CenterWidth;
        }
        if ("right".equals(value)) {
            return Right;
        }
        return this.loadHelper().attributeParser().parseLength(value, Length.ZERO, PercentageDimension.WIDTH);
    }

    @NotNull
    public Length getVerticalReferenceLength(@Nullable String value) {
        if ("top".equals(value)) {
            return Top;
        }
        if ("center".equals(value)) {
            return CenterHeight;
        }
        if ("bottom".equals(value)) {
            return Bottom;
        }
        return this.loadHelper().attributeParser().parseLength(value, Length.ZERO, PercentageDimension.HEIGHT);
    }

    public boolean isHorizontalKeyword(@NotNull String value) {
        return "left".equals(value) || "right".equals(value);
    }

    public boolean isVerticalKeyword(@NotNull String value) {
        return "top".equals(value) || "bottom".equals(value);
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    public Percentage getPercentage(@NotNull String key, @Nullable Percentage fallback) {
        return this.loadHelper().attributeParser().parsePercentage(this.getValue(key), fallback);
    }

    @Nullable
    public PercentageValue getPercentage(@NotNull String key, Inherited inherited, Animatable animatable) {
        return this.getPercentage(key, null, inherited, animatable);
    }

    @Contract(value="_,!null,_,_ -> !null")
    @Nullable
    public PercentageValue getPercentage(@NotNull String key, @Nullable PercentageValue fallback, Inherited inherited, Animatable animatable) {
        PercentageValue value = this.loadHelper().attributeParser().parsePercentage(this.getValue(key), FALLBACK_PERCENTAGE);
        if (value == FALLBACK_PERCENTAGE) {
            value = fallback;
        }
        if (animatable == Animatable.YES) {
            AnimatedPercentage animatedPercentage;
            PercentageValue initial = value;
            if (initial == null) {
                PercentageValue percentageValue = initial = inherited == Inherited.YES ? Percentage.INHERITED : NeutralElements.NEUTRAL_PERCENTAGE;
            }
            if (initial instanceof AnimatedPercentage) {
                initial = ((AnimatedPercentage)initial).initial();
            }
            if ((animatedPercentage = this.getAnimatedPercentage(key, initial)) != null) {
                return animatedPercentage;
            }
        }
        return value;
    }

    @NotNull
    public @NotNull Length @NotNull [] getLengthList(@NotNull String key, @NotNull PercentageDimension dimension) {
        return this.getLengthList(key, new Length[0], dimension);
    }

    @Contract(value="_,!null,_ -> !null")
    public @NotNull Length @Nullable [] getLengthList(@NotNull String key, @NotNull Length @Nullable [] fallback, @NotNull PercentageDimension dimension) {
        return this.loadHelper().attributeParser().parseLengthList(this.getValue(key), fallback, dimension);
    }

    public float @NotNull [] getFloatList(@NotNull String key) {
        return this.loadHelper().attributeParser().parseFloatList(this.getValue(key));
    }

    @NotNull
    public FloatListValue getFloatList(@NotNull String key, Inherited inherited, Animatable animatable) {
        FloatListValue initial;
        String value = this.getValue(key);
        float[] initialRaw = this.loadHelper().attributeParser().parseFloatList(this.getValue(key));
        ConstantFloatList constantFloatList = initial = value != null ? new ConstantFloatList(initialRaw) : null;
        if (animatable == Animatable.YES) {
            AnimatedFloatList animatedLength;
            if (initial == null) {
                if (inherited == Inherited.YES) {
                    throw new IllegalStateException("Inherited values for float lists aren't implemented yet");
                }
                initial = NeutralElements.NEUTRAL_FLOAT_LIST;
            }
            if ((animatedLength = this.getAnimatedFloatList(key, initial)) != null) {
                return animatedLength;
            }
        }
        return initial != null ? initial : ConstantFloatList.EMPTY;
    }

    public double @NotNull [] getDoubleList(@NotNull String key) {
        return this.loadHelper().attributeParser().parseDoubleList(this.getValue(key));
    }

    @NotNull
    public <E extends Enum<E>> E getEnum(@NotNull String key, @NotNull E fallback) {
        return this.loadHelper().attributeParser().parseEnum(this.getValue(key), fallback);
    }

    @Nullable
    public <E extends Enum<E>> E getEnumNullable(@NotNull String key, @NotNull Class<E> enumType) {
        return this.loadHelper().attributeParser().parseEnum(this.getValue(key), enumType);
    }

    @Nullable
    public ClipPath getClipPath() {
        return this.getElementByUrl(ClipPath.class, this.getValue("clip-path"));
    }

    @Nullable
    public Mask getMask() {
        return this.getElementByUrl(Mask.class, this.getValue("mask"));
    }

    @Nullable
    public Filter getFilter() {
        return this.getElementByUrl(Filter.class, this.getValue("filter"));
    }

    @NotNull
    public FilterChannelKey getFilterChannelKey(@NotNull String key, @NotNull FilterChannelKey fallback) {
        String in = this.getValue(key);
        if (in == null) {
            return fallback;
        }
        return new FilterChannelKey.StringKey(in);
    }

    @Nullable
    public TransformValue parseTransform(@NotNull String key) {
        return this.parseTransform(key, Inherited.NO, Animatable.NO);
    }

    @NotNull
    private TransformValue createTransformValueFromParts(@NotNull List<TransformPart> parts) {
        for (TransformPart part : parts) {
            if (part.canBeFlattened()) continue;
            return new ConstantLengthTransform(parts);
        }
        return new ConstantTransform(new ConstantLengthTransform(parts).get(DUMMY_MEASURE_CONTEXT));
    }

    @Nullable
    public TransformValue parseTransform(@NotNull String key, Inherited inherited, Animatable animatable) {
        TransformValue value;
        List<TransformPart> parts = this.loadHelper().attributeParser().parseTransform(this.getValue(key));
        TransformValue transformValue = value = parts != null ? this.createTransformValueFromParts(parts) : null;
        if (animatable == Animatable.YES) {
            AnimatedTransform animatedTransform;
            TransformValue initial = value;
            if (initial == null) {
                TransformValue transformValue2 = initial = inherited == Inherited.YES ? ConstantLengthTransform.INHERITED : NeutralElements.NEUTRAL_TRANSFORM;
            }
            if (initial instanceof AnimatedTransform) {
                initial = ((AnimatedTransform)initial).initial();
            }
            if ((animatedTransform = this.getAnimatedTransform(key, initial)) != null) {
                return animatedTransform;
            }
        }
        return value;
    }

    public boolean hasAttribute(@NotNull String name) {
        return this.attributes.containsKey(name);
    }

    @NotNull
    public @NotNull String @NotNull [] getStringList(@NotNull String name) {
        return this.getStringList(name, SeparatorMode.COMMA_AND_WHITESPACE);
    }

    @NotNull
    public @NotNull String @NotNull [] getStringList(@NotNull String name, SeparatorMode separatorMode) {
        return this.loadHelper().attributeParser().parseStringList(this.getValue(name), separatorMode);
    }

    public float getFloat(@NotNull String name, float fallback) {
        return this.loadHelper().attributeParser().parseFloat(this.getValue(name), fallback);
    }

    public float getNonNegativeFloat(@NotNull String name, float fallback) {
        float value = this.getFloat(name, fallback);
        if (Float.isFinite(value) && value < 0.0f) {
            return fallback;
        }
        return value;
    }

    public int getInt(@NotNull String key, int fallback) {
        return this.loadHelper().attributeParser().parseInt(this.getValue(key), fallback);
    }

    @Nullable
    public String getHref() {
        String href = this.getValue("href");
        if (href == null) {
            return this.getValue("xlink:href");
        }
        return href;
    }

    @Nullable
    public ViewBox getViewBox() {
        float[] viewBoxCords = this.getFloatList("viewBox");
        return viewBoxCords.length == 4 ? new ViewBox(viewBoxCords) : null;
    }

    @NotNull
    public AttributeParser parser() {
        return this.loadHelper().attributeParser();
    }

    @NotNull
    public ResourceLoader resourceLoader() {
        return this.loadHelper().resourceLoader();
    }

    @Nullable
    public URI resolveResourceURI(@NotNull String url) {
        return this.loadHelper().externalResourcePolicy().resolveResourceURI(this.document().rootURI(), url);
    }

    private <T extends BaseAnimationNode> List<@NotNull T> animateNodes(@NotNull String property, Class<T> type) {
        List<ParsedElement> parsedElements = this.element.animationElements().get(property);
        if (parsedElements == null || parsedElements.isEmpty()) {
            return Collections.emptyList();
        }
        List animateNodes = parsedElements.stream().filter(n -> type.isInstance(n.node())).map(n -> (BaseAnimationNode)type.cast(n.nodeEnsuringBuildStatus(this.document().currentNestingDepth()))).collect(Collectors.toList());
        for (BaseAnimationNode animateNode : animateNodes) {
            this.document().registerAnimatedElement(animateNode);
        }
        return animateNodes;
    }

    @Nullable
    private static <T, A extends T, N extends BaseAnimationNode> A makeAnimated(@NotNull List<N> animationNodes, @NotNull T initial, @NotNull BiFunction<N, T, A> factory) {
        if (animationNodes.isEmpty()) {
            return null;
        }
        @NotNull Object currentInitial = initial;
        A lastAnimationValue = null;
        for (BaseAnimationNode animate : animationNodes) {
            A animated = factory.apply((N)animate, (BaseAnimationNode)currentInitial);
            if (animated == null) continue;
            currentInitial = animated;
            lastAnimationValue = animated;
        }
        return lastAnimationValue;
    }

    @Nullable
    public AnimatedLength getAnimatedLength(@NotNull String property, @NotNull LengthValue initial, @NotNull PercentageDimension dimension) {
        return AttributeNode.makeAnimated(this.animateNodes(property, Animate.class), initial, (animate, currentInitial) -> animate.animatedLength((LengthValue)currentInitial, dimension, this));
    }

    @Nullable
    private AnimatedFloatList getAnimatedFloatList(@NotNull String property, @NotNull FloatListValue initial) {
        return AttributeNode.makeAnimated(this.animateNodes(property, Animate.class), initial, (animate, currentInitial) -> animate.animatedFloatList((FloatListValue)currentInitial, this));
    }

    @Nullable
    private AnimatedPercentage getAnimatedPercentage(@NotNull String property, @NotNull PercentageValue initial) {
        return AttributeNode.makeAnimated(this.animateNodes(property, Animate.class), initial, (animate, currentInitial) -> animate.animatedPercentage((PercentageValue)currentInitial, this));
    }

    @Nullable
    private AnimatedPaint getAnimatedPaint(@NotNull String property, @NotNull SVGPaint initial) {
        return AttributeNode.makeAnimated(this.animateNodes(property, Animate.class), initial, (animate, currentInitial) -> animate.animatedPaint((SVGPaint)currentInitial, this));
    }

    @Nullable
    public AnimatedColor getAnimatedColor(@NotNull String property, @NotNull ColorValue initial) {
        return AttributeNode.makeAnimated(this.animateNodes(property, Animate.class), initial, (animate, currentInitial) -> animate.animatedColor((ColorValue)currentInitial, this));
    }

    @Nullable
    public AnimatedTransform getAnimatedTransform(@NotNull String property, @NotNull TransformValue initial) {
        return AttributeNode.makeAnimated(this.animateNodes(property, AnimateTransform.class), initial, (animate, currentInitial) -> animate.animatedTransform((TransformValue)currentInitial, this));
    }

    public static enum ElementRelation {
        GEOMETRY_DATA,
        PAINTED_CHILD,
        TEMPLATE,
        PAINT_SERVER;

    }
}

