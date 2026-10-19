/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.util;

import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class PropertyModelHandlerHelper
extends ContextAwareBase {
    public static final String HANDLE_PROPERTY_MODEL_METHOD_NAME = "handlePropertyModel";

    public PropertyModelHandlerHelper(ContextAware declaredOrigin) {
        super(declaredOrigin);
    }

    public void handlePropertyModel(ContextAwarePropertyContainer capcm, String nameStr, String valueStr, String fileStr, String resourceStr, String scopeStr) {
        PropertyModel propertyModel = new PropertyModel();
        propertyModel.setName(nameStr);
        propertyModel.setValue(valueStr);
        propertyModel.setFile(fileStr);
        propertyModel.setResource(resourceStr);
        propertyModel.setScopeStr(scopeStr);
        this.handlePropertyModel(capcm, propertyModel);
    }

    public void handlePropertyModel(ContextAwarePropertyContainer capc, PropertyModel propertyModel) {
        ActionUtil.Scope scope = ActionUtil.stringToScope(propertyModel.getScopeStr());
        if (PropertyModelHandlerHelper.checkFileAttributeSanity(propertyModel)) {
            String file = propertyModel.getFile();
            file = capc.subst(file);
            try (FileInputStream istream = new FileInputStream(file);){
                PropertyModelHandlerHelper.loadAndSetProperties(capc, istream, scope);
            }
            catch (FileNotFoundException e) {
                this.addError("Could not find properties file [" + file + "].");
            }
            catch (IOException | IllegalArgumentException e1) {
                this.addError("Could not read properties file [" + file + "].", e1);
            }
        } else if (PropertyModelHandlerHelper.checkResourceAttributeSanity(propertyModel)) {
            String resource = propertyModel.getResource();
            URL resourceURL = Loader.getResourceBySelfClassLoader(resource = capc.subst(resource));
            if (resourceURL == null) {
                this.addError("Could not find resource [" + resource + "].");
            } else {
                try (InputStream istream = resourceURL.openStream();){
                    PropertyModelHandlerHelper.loadAndSetProperties(capc, istream, scope);
                }
                catch (IOException e) {
                    this.addError("Could not read resource file [" + resource + "].", e);
                }
            }
        } else if (PropertyModelHandlerHelper.checkValueNameAttributesSanity(propertyModel)) {
            String value = propertyModel.getValue();
            value = value.trim();
            value = capc.subst(value);
            ActionUtil.setProperty(capc, propertyModel.getName(), value, scope);
        } else {
            this.addError("In <property> element, either the \"file\" attribute alone, or the \"resource\" element alone, or both the \"name\" and \"value\" attributes must be set.");
        }
    }

    public static boolean checkFileAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();
        return !OptionHelper.isNullOrEmptyOrAllSpaces(file) && OptionHelper.isNullOrEmptyOrAllSpaces(name) && OptionHelper.isNullOrEmptyOrAllSpaces(value) && OptionHelper.isNullOrEmptyOrAllSpaces(resource);
    }

    public static boolean checkResourceAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();
        return !OptionHelper.isNullOrEmptyOrAllSpaces(resource) && OptionHelper.isNullOrEmptyOrAllSpaces(name) && OptionHelper.isNullOrEmptyOrAllSpaces(value) && OptionHelper.isNullOrEmptyOrAllSpaces(file);
    }

    public static boolean checkValueNameAttributesSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();
        return !OptionHelper.isNullOrEmptyOrAllSpaces(name) && !OptionHelper.isNullOrEmptyOrAllSpaces(value) && OptionHelper.isNullOrEmptyOrAllSpaces(file) && OptionHelper.isNullOrEmptyOrAllSpaces(resource);
    }

    public static void setProperty(ContextAwarePropertyContainer capc, String key, String value, ActionUtil.Scope scope) {
        switch (scope) {
            case LOCAL: {
                capc.addSubstitutionProperty(key, value);
                break;
            }
            case CONTEXT: {
                capc.getContext().putProperty(key, value);
                break;
            }
            case SYSTEM: {
                OptionHelper.setSystemProperty(capc, key, value);
            }
        }
    }

    public static void setProperties(ContextAwarePropertyContainer capc, Properties props, ActionUtil.Scope scope) {
        switch (scope) {
            case LOCAL: {
                capc.addSubstitutionProperties(props);
                break;
            }
            case CONTEXT: {
                ContextUtil cu = new ContextUtil(capc.getContext());
                cu.addProperties(props);
                break;
            }
            case SYSTEM: {
                OptionHelper.setSystemProperties(capc, props);
            }
        }
    }

    public static void loadAndSetProperties(ContextAwarePropertyContainer capc, InputStream istream, ActionUtil.Scope scope) throws IOException {
        Properties props = new Properties();
        props.load(istream);
        PropertyModelHandlerHelper.setProperties(capc, props, scope);
    }
}

