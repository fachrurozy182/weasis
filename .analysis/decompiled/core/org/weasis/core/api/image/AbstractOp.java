/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.image;

import java.lang.runtime.SwitchBootstraps;
import java.util.HashMap;
import java.util.Map;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.util.LangUtil;
import org.weasis.opencv.data.PlanarImage;

public abstract class AbstractOp
implements ImageOpNode {
    protected static final String INPUT_PREFIX = "op.input";
    protected static final String OUTPUT_PREFIX = "op.output";
    protected final Map<String, Object> params;

    protected AbstractOp() {
        this.params = new HashMap<String, Object>();
    }

    protected AbstractOp(AbstractOp op) {
        this.params = new HashMap<String, Object>(op.params);
        this.clearIOCache();
    }

    @Override
    public void clearParams() {
        this.params.clear();
    }

    @Override
    public void clearIOCache() {
        this.params.keySet().removeIf(this::isIOCacheKey);
    }

    private boolean isIOCacheKey(String key) {
        return key.startsWith(INPUT_PREFIX) || key.startsWith(OUTPUT_PREFIX);
    }

    @Override
    public Object getParam(String key) {
        return this.params.get(key);
    }

    @Override
    public void setParam(String key, Object value) {
        if (key != null) {
            this.params.put(key, value);
        }
    }

    @Override
    public <T> T getParam(String key, Class<T> ignored) {
        return (T)this.params.get(key);
    }

    protected <T> T getParam(String key, Class<T> type, T defaultValue) {
        T value = this.getParam(key, type);
        return value != null ? value : defaultValue;
    }

    @Override
    public void setAllParameters(Map<String, Object> map) {
        if (map != null) {
            this.params.putAll(map);
        }
    }

    @Override
    public void removeParam(String key) {
        this.params.remove(key);
    }

    @Override
    public boolean isEnabled() {
        return LangUtil.nullToTrue((Boolean)((Boolean)this.params.get("op.enable")));
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.params.put("op.enable", enabled);
    }

    @Override
    public String getName() {
        return (String)this.params.get("op.display.name");
    }

    @Override
    public void setName(String name) {
        if (name != null) {
            this.params.put("op.display.name", name);
        }
    }

    @Override
    public void handleImageOpEvent(ImageOpEvent event) {
    }

    protected PlanarImage getSourceImage() {
        Object object = this.params.get("op.input.img");
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PlanarImage.class}, (Object)object, (int)n)) {
            case 0 -> (PlanarImage)object;
            case -1 -> throw new IllegalArgumentException("INPUT_IMG parameter is missing");
            default -> throw new IllegalArgumentException("INPUT_IMG parameter must be a PlanarImage");
        };
    }
}

