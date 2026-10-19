/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.tyler;

import ch.qos.logback.classic.tyler.TylerConfiguratorBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.util.PropertyModelHandlerHelper;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class VariableModelHelper
extends ContextAwareBase {
    TylerConfiguratorBase tylerConfiguratorBase;
    VariableSubstitutionsHelper variableSubstitutionsHelper;

    VariableModelHelper(Context context, TylerConfiguratorBase tylerConfiguratorBase) {
        super(tylerConfiguratorBase);
        this.context = context;
        this.tylerConfiguratorBase = tylerConfiguratorBase;
        this.variableSubstitutionsHelper = new VariableSubstitutionsHelper(context);
    }

    void updateProperties(PropertyModel propertyModel) {
        ActionUtil.Scope scope = ActionUtil.stringToScope(propertyModel.getScopeStr());
        if (PropertyModelHandlerHelper.checkFileAttributeSanity(propertyModel)) {
            String file = propertyModel.getFile();
            file = this.tylerConfiguratorBase.subst(file);
            try (FileInputStream istream = new FileInputStream(file);){
                this.loadAndSetProperties(istream, scope);
            }
            catch (FileNotFoundException e) {
                this.addError("Could not find properties file [" + file + "].");
            }
            catch (IOException | IllegalArgumentException e1) {
                this.addError("Could not read properties file [" + file + "].", e1);
            }
        } else if (PropertyModelHandlerHelper.checkResourceAttributeSanity(propertyModel)) {
            String resource = propertyModel.getResource();
            URL resourceURL = Loader.getResourceBySelfClassLoader(resource = this.tylerConfiguratorBase.subst(resource));
            if (resourceURL == null) {
                this.addError("Could not find resource [" + resource + "].");
            } else {
                try (InputStream istream = resourceURL.openStream();){
                    this.loadAndSetProperties(istream, scope);
                }
                catch (IOException e) {
                    this.addError("Could not read resource file [" + resource + "].", e);
                }
            }
        } else if (PropertyModelHandlerHelper.checkValueNameAttributesSanity(propertyModel)) {
            String value = propertyModel.getValue();
            value = value.trim();
            value = this.tylerConfiguratorBase.subst(value);
            this.setProperty(propertyModel.getName(), value, scope);
        } else {
            this.addError("In <property> element, either the \"file\" attribute alone, or the \"resource\" element alone, or both the \"name\" and \"value\" attributes must be set.");
        }
    }

    void loadAndSetProperties(InputStream istream, ActionUtil.Scope scope) throws IOException {
        Properties props = new Properties();
        props.load(istream);
        this.setProperties(props, scope);
    }

    public void setProperties(Properties props, ActionUtil.Scope scope) {
        switch (scope) {
            case LOCAL: {
                this.variableSubstitutionsHelper.addSubstitutionProperties(props);
                break;
            }
            case CONTEXT: {
                ContextUtil cu = new ContextUtil(this.getContext());
                cu.addProperties(props);
                break;
            }
            case SYSTEM: {
                OptionHelper.setSystemProperties(this, props);
            }
        }
    }

    public void setProperty(String key, String value, ActionUtil.Scope scope) {
        switch (scope) {
            case LOCAL: {
                this.variableSubstitutionsHelper.addSubstitutionProperty(key, value);
                break;
            }
            case CONTEXT: {
                this.getContext().putProperty(key, value);
                break;
            }
            case SYSTEM: {
                OptionHelper.setSystemProperty(this, key, value);
            }
        }
    }
}

