/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.Model;

public class ModelUtil {
    public static void resetForReuse(Model model) {
        if (model == null) {
            return;
        }
        model.resetForReuse();
    }
}

