/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.rolling.helper.CompressionStrategy;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.FileUtil;
import java.io.File;

public abstract class CompressionStrategyBase
extends ContextAwareBase
implements CompressionStrategy {
    static final int BUFFER_SIZE = 65536;

    void createMissingTargetDirsIfNecessary(File file) {
        boolean result = FileUtil.createMissingParentDirectories(file);
        if (!result) {
            this.addError("Failed to create parent directories for [" + file.getAbsolutePath() + "]");
        }
    }
}

