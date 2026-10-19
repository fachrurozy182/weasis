/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.image;

import java.util.Map;
import java.util.Optional;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.util.Copyable;

public interface ImageOpNode
extends Copyable<ImageOpNode> {
    public void process() throws Exception;

    public boolean isEnabled();

    public void setEnabled(boolean var1);

    public String getName();

    public void setName(String var1);

    default public Optional<Object> getParamOptional(String key) {
        return Optional.ofNullable(this.getParam(key));
    }

    public Object getParam(String var1);

    public void setParam(String var1, Object var2);

    public <T> T getParam(String var1, Class<T> var2);

    public void setAllParameters(Map<String, Object> var1);

    public void removeParam(String var1);

    public void clearParams();

    public void clearIOCache();

    public void handleImageOpEvent(ImageOpEvent var1);

    public static final class Param {
        public static final String NAME = "op.display.name";
        public static final String ENABLE = "op.enable";
        public static final String INPUT_IMG = "op.input.img";
        public static final String OUTPUT_IMG = "op.output.img";

        private Param() {
        }
    }
}

