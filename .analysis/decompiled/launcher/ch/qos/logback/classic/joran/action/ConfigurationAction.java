/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran.action;

import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

public class ConfigurationAction
extends BaseModelAction {
    static final String INTERNAL_DEBUG_ATTR = "debug";
    static final String SCAN_ATTR = "scan";
    static final String SCAN_PERIOD_ATTR = "scanPeriod";
    static final String PACKAGING_DATA_ATTR = "packagingData";

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        ConfigurationModel configurationModel = new ConfigurationModel();
        configurationModel.setDebugStr(attributes.getValue(INTERNAL_DEBUG_ATTR));
        configurationModel.setScanStr(attributes.getValue(SCAN_ATTR));
        configurationModel.setScanPeriodStr(attributes.getValue(SCAN_PERIOD_ATTR));
        configurationModel.setPackagingDataStr(attributes.getValue(PACKAGING_DATA_ATTR));
        return configurationModel;
    }
}

