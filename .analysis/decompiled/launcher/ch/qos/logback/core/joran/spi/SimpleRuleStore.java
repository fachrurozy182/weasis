/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ElementPath;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleRuleStore
extends ContextAwareBase
implements RuleStore {
    static String KLEENE_STAR = "*";
    HashMap<ElementSelector, Supplier<Action>> rules = new HashMap();
    List<String> transparentPathParts = new ArrayList<String>(2);
    Map<String, String> pathPartsMapForRenaming = new HashMap<String, String>(2);

    public SimpleRuleStore(Context context) {
        this.setContext(context);
    }

    @Override
    public void addTransparentPathPart(String pathPart) {
        if (pathPart == null) {
            throw new IllegalArgumentException("pathPart cannot be null");
        }
        if ((pathPart = pathPart.trim()).isEmpty()) {
            throw new IllegalArgumentException("pathPart cannot be empty or to consist of only spaces");
        }
        if (pathPart.contains("/")) {
            throw new IllegalArgumentException("pathPart cannot contain '/', i.e. the forward slash character");
        }
        this.transparentPathParts.add(pathPart);
    }

    @Override
    public void addPathPathMapping(String originalName, String modifiedName) {
        this.pathPartsMapForRenaming.put(originalName, modifiedName);
    }

    @Override
    public void addRule(ElementSelector elementSelector, Supplier<Action> actionSupplier) {
        Supplier<Action> existing = this.rules.get(elementSelector);
        if (existing != null) {
            throw new IllegalStateException(elementSelector.toString() + " already has an associated action supplier");
        }
        this.rules.put(elementSelector, actionSupplier);
    }

    @Override
    public void addRule(ElementSelector elementSelector, String actionClassName) {
        Action action = null;
        try {
            action = (Action)OptionHelper.instantiateByClassName(actionClassName, Action.class, this.context);
        }
        catch (Exception e) {
            this.addError("Could not instantiate class [" + actionClassName + "]", e);
        }
        if (action != null) {
            // empty if block
        }
    }

    @Override
    public Supplier<Action> matchActions(ElementPath elementPath) {
        Supplier<Action> actionSupplier = this.internalMatchAction(elementPath);
        if (actionSupplier != null) {
            return actionSupplier;
        }
        return this.matchActionsWithoutTransparentPartsAndRenamedParts(elementPath);
    }

    private Supplier<Action> matchActionsWithoutTransparentPartsAndRenamedParts(ElementPath elementPath) {
        ElementPath cleanedElementPath = this.removeTransparentPathParts(elementPath);
        ElementPath renamePathParts = this.renamePathParts(cleanedElementPath);
        return this.internalMatchAction(renamePathParts);
    }

    private Supplier<Action> internalMatchAction(ElementPath elementPath) {
        Supplier<Action> actionSupplier = this.fullPathMatch(elementPath);
        if (actionSupplier != null) {
            return actionSupplier;
        }
        actionSupplier = this.suffixMatch(elementPath);
        if (actionSupplier != null) {
            return actionSupplier;
        }
        actionSupplier = this.prefixMatch(elementPath);
        if (actionSupplier != null) {
            return actionSupplier;
        }
        actionSupplier = this.middleMatch(elementPath);
        if (actionSupplier != null) {
            return actionSupplier;
        }
        return null;
    }

    ElementPath removeTransparentPathParts(ElementPath originalElementPath) {
        ArrayList<String> preservedElementList = new ArrayList<String>(originalElementPath.partList.size());
        for (String part : originalElementPath.partList) {
            boolean shouldKeep = this.transparentPathParts.stream().noneMatch(p -> p.equalsIgnoreCase(part));
            if (!shouldKeep) continue;
            preservedElementList.add(part);
        }
        return new ElementPath(preservedElementList);
    }

    ElementPath renamePathParts(ElementPath originalElementPath) {
        ArrayList<String> result = new ArrayList<String>(originalElementPath.partList.size());
        for (String part : originalElementPath.partList) {
            String modifiedName = this.pathPartsMapForRenaming.getOrDefault(part, part);
            result.add(modifiedName);
        }
        return new ElementPath(result);
    }

    Supplier<Action> fullPathMatch(ElementPath elementPath) {
        for (ElementSelector selector : this.rules.keySet()) {
            if (!selector.fullPathMatch(elementPath)) continue;
            return this.rules.get(selector);
        }
        return null;
    }

    Supplier<Action> suffixMatch(ElementPath elementPath) {
        int max = 0;
        ElementSelector longestMatchingElementSelector = null;
        for (ElementSelector selector : this.rules.keySet()) {
            int r;
            if (!this.isSuffixPattern(selector) || (r = selector.getTailMatchLength(elementPath)) <= max) continue;
            max = r;
            longestMatchingElementSelector = selector;
        }
        if (longestMatchingElementSelector != null) {
            return this.rules.get(longestMatchingElementSelector);
        }
        return null;
    }

    private boolean isSuffixPattern(ElementSelector p) {
        return p.size() > 1 && p.get(0).equals(KLEENE_STAR);
    }

    Supplier<Action> prefixMatch(ElementPath elementPath) {
        int max = 0;
        ElementSelector longestMatchingElementSelector = null;
        for (ElementSelector selector : this.rules.keySet()) {
            int r;
            String last = selector.peekLast();
            if (!this.isKleeneStar(last) || (r = selector.getPrefixMatchLength(elementPath)) != selector.size() - 1 || r <= max) continue;
            max = r;
            longestMatchingElementSelector = selector;
        }
        if (longestMatchingElementSelector != null) {
            return this.rules.get(longestMatchingElementSelector);
        }
        return null;
    }

    private boolean isKleeneStar(String last) {
        return KLEENE_STAR.equals(last);
    }

    Supplier<Action> middleMatch(ElementPath path) {
        int max = 0;
        ElementSelector longestMatchingElementSelector = null;
        for (ElementSelector selector : this.rules.keySet()) {
            String last = selector.peekLast();
            String first = null;
            if (selector.size() > 1) {
                first = selector.get(0);
            }
            if (!this.isKleeneStar(last) || !this.isKleeneStar(first)) continue;
            List<String> copyOfPartList = selector.getCopyOfPartList();
            if (copyOfPartList.size() > 2) {
                copyOfPartList.remove(0);
                copyOfPartList.remove(copyOfPartList.size() - 1);
            }
            int r = 0;
            ElementSelector clone = new ElementSelector(copyOfPartList);
            if (clone.isContainedIn(path)) {
                r = clone.size();
            }
            if (r <= max) continue;
            max = r;
            longestMatchingElementSelector = selector;
        }
        if (longestMatchingElementSelector != null) {
            return this.rules.get(longestMatchingElementSelector);
        }
        return null;
    }

    public String toString() {
        String TAB = "  ";
        StringBuilder retValue = new StringBuilder();
        retValue.append("SimpleRuleStore ( ").append("rules = ").append(this.rules).append("  ").append(" )");
        return retValue.toString();
    }
}

