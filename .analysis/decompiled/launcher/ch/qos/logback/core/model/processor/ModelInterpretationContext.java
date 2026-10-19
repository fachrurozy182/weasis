/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.GenericXMLConfigurator;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.DependencyDefinition;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;

public class ModelInterpretationContext
extends ContextAwareBase
implements ContextAwarePropertyContainer {
    Stack<Object> objectStack;
    Stack<Model> modelStack;
    URL topURL;
    Boolean topScanBoolean = null;
    Supplier<? extends GenericXMLConfigurator> configuratorSupplier;
    Map<String, Object> objectMap;
    protected VariableSubstitutionsHelper variableSubstitutionsHelper;
    protected Map<String, String> importMap;
    private final BeanDescriptionCache beanDescriptionCache;
    final DefaultNestedComponentRegistry defaultNestedComponentRegistry = new DefaultNestedComponentRegistry();
    List<DependencyDefinition> dependencyDefinitionList = new ArrayList<DependencyDefinition>();
    final List<String> startedDependees = new ArrayList<String>();
    Object configuratorHint;
    Model topModel;

    public ModelInterpretationContext(Context context) {
        this(context, null);
    }

    public ModelInterpretationContext(Context context, Object configuratorHint) {
        this.context = context;
        this.configuratorHint = configuratorHint;
        this.objectStack = new Stack();
        this.modelStack = new Stack();
        this.beanDescriptionCache = new BeanDescriptionCache(context);
        this.objectMap = new HashMap<String, Object>(5);
        this.variableSubstitutionsHelper = new VariableSubstitutionsHelper(context);
        this.importMap = new HashMap<String, String>(5);
    }

    public ModelInterpretationContext(ModelInterpretationContext otherMic) {
        this(otherMic.context, otherMic.configuratorHint);
        this.importMap = new HashMap<String, String>(otherMic.importMap);
        this.variableSubstitutionsHelper = new VariableSubstitutionsHelper(this.context, otherMic.getCopyOfPropertyMap());
        this.defaultNestedComponentRegistry.duplicate(otherMic.getDefaultNestedComponentRegistry());
        this.createAppenderBags();
    }

    public Map<String, Object> getObjectMap() {
        return this.objectMap;
    }

    public void createAppenderBags() {
        this.objectMap.put("APPENDER_BAG", new HashMap());
        this.objectMap.put("APPENDER_REF_BAG", new HashMap());
    }

    public Model getTopModel() {
        return this.topModel;
    }

    public void setTopModel(Model topModel) {
        this.topModel = topModel;
    }

    public void pushModel(Model m) {
        this.modelStack.push(m);
    }

    public Model peekModel() {
        return this.modelStack.peek();
    }

    public boolean isModelStackEmpty() {
        return this.modelStack.isEmpty();
    }

    public Model popModel() {
        return this.modelStack.pop();
    }

    public Stack<Object> getObjectStack() {
        return this.objectStack;
    }

    public boolean isObjectStackEmpty() {
        return this.objectStack.isEmpty();
    }

    public Object peekObject() {
        return this.objectStack.peek();
    }

    public void pushObject(Object o) {
        this.objectStack.push(o);
    }

    public Object popObject() {
        return this.objectStack.pop();
    }

    public Object getObject(int i) {
        return this.objectStack.get(i);
    }

    public Object getConfiguratorHint() {
        return this.configuratorHint;
    }

    public void setConfiguratorHint(Object configuratorHint) {
        this.configuratorHint = configuratorHint;
    }

    public BeanDescriptionCache getBeanDescriptionCache() {
        return this.beanDescriptionCache;
    }

    @Override
    public String subst(String ref) {
        String substituted = this.variableSubstitutionsHelper.subst(ref);
        if (ref != null && !ref.equals(substituted)) {
            String sanitized = this.variableSubstitutionsHelper.sanitizeIfConfidential(ref, substituted);
            this.addInfo("value \"" + sanitized + "\" substituted for \"" + ref + "\"");
        }
        return substituted;
    }

    public DefaultNestedComponentRegistry getDefaultNestedComponentRegistry() {
        return this.defaultNestedComponentRegistry;
    }

    public void addDependencyDefinition(DependencyDefinition dd) {
        this.dependencyDefinitionList.add(dd);
    }

    public List<DependencyDefinition> getDependencyDefinitions() {
        return Collections.unmodifiableList(this.dependencyDefinitionList);
    }

    public List<String> getDependencyNamesForModel(Model model) {
        ArrayList<String> dependencyList = new ArrayList<String>();
        for (DependencyDefinition dd : this.dependencyDefinitionList) {
            if (dd.getDepender() != model) continue;
            dependencyList.add(dd.getDependency());
        }
        return dependencyList;
    }

    public boolean hasDependers(String dependencyName) {
        if (dependencyName == null || dependencyName.trim().length() == 0) {
            new IllegalArgumentException("Empty dependeeName name not allowed here");
        }
        for (DependencyDefinition dd : this.dependencyDefinitionList) {
            if (!dd.dependency.equals(dependencyName)) continue;
            return true;
        }
        return false;
    }

    public void markStartOfNamedDependee(String name) {
        this.startedDependees.add(name);
    }

    public boolean isNamedDependemcyStarted(String name) {
        return this.startedDependees.contains(name);
    }

    @Override
    public void addSubstitutionProperty(String key, String value) {
        this.variableSubstitutionsHelper.addSubstitutionProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        return this.variableSubstitutionsHelper.getProperty(key);
    }

    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return this.variableSubstitutionsHelper.getCopyOfPropertyMap();
    }

    public void addImport(String stem, String fqcn) {
        this.importMap.put(stem, fqcn);
    }

    public Map<String, String> getImportMapCopy() {
        return new HashMap<String, String>(this.importMap);
    }

    public String getImport(String stem) {
        if (stem == null) {
            return null;
        }
        String result = this.importMap.get(stem);
        if (result == null) {
            return stem;
        }
        return result;
    }

    @Override
    public Supplier<? extends GenericXMLConfigurator> getConfiguratorSupplier() {
        return this.configuratorSupplier;
    }

    public void setConfiguratorSupplier(Supplier<? extends GenericXMLConfigurator> configuratorSupplier) {
        this.configuratorSupplier = configuratorSupplier;
    }

    public URL getTopURL() {
        return this.topURL;
    }

    public void setTopURL(URL topURL) {
        this.topURL = topURL;
    }

    public Boolean getTopScanBoolean() {
        return this.topScanBoolean;
    }

    public void setTopScanBoolean(Boolean topScanBoolean) {
        this.topScanBoolean = topScanBoolean;
    }
}

