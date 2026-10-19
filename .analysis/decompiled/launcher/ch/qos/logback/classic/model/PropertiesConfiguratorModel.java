/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.ResourceModel;

public class PropertiesConfiguratorModel
extends ResourceModel {
    private static final long serialVersionUID = -2009536798661734346L;
    String scanStr;

    public String getScanStr() {
        return this.scanStr;
    }

    public void setScanStr(String scanStr) {
        this.scanStr = scanStr;
    }
}

