/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.Loader;

public abstract class RollingPolicyBase
extends ContextAwareBase
implements RollingPolicy {
    protected CompressionMode compressionMode = CompressionMode.NONE;
    FileNamePattern fileNamePattern;
    protected String fileNamePatternStr;
    private FileAppender<?> parent;
    FileNamePattern zipEntryFileNamePattern;
    private boolean started;

    @Deprecated
    protected void determineCompressionMode() {
        this.compressionMode = this.fileNamePatternStrToCompressionMode(this.fileNamePatternStr);
        this.outputCompressionModeMessage(this.compressionMode);
    }

    protected void outputCompressionModeMessage(CompressionMode compressionMode) {
        switch (compressionMode) {
            case GZ: {
                this.addInfo("Will use gz compression");
                break;
            }
            case ZIP: {
                this.addInfo("Will use zip compression");
                break;
            }
            case XZ: {
                this.addInfo("Will use xz compression");
                break;
            }
            case NONE: {
                this.addInfo("No compression will be used");
            }
        }
    }

    protected CompressionMode fileNamePatternStrToCompressionMode(String aFileNamePatternStr) {
        if (aFileNamePatternStr.endsWith(CompressionMode.GZ_SUFFIX)) {
            return CompressionMode.GZ;
        }
        if (aFileNamePatternStr.endsWith(CompressionMode.ZIP_SUFFIX)) {
            return CompressionMode.ZIP;
        }
        if (aFileNamePatternStr.endsWith(CompressionMode.XZ_SUFFIX)) {
            return CompressionMode.XZ;
        }
        return CompressionMode.NONE;
    }

    protected void adjustCompressionModeAndFileNamePatternStrIfNecessary() {
        boolean xzLibraryLoadable;
        if (this.compressionMode == CompressionMode.XZ && !(xzLibraryLoadable = Loader.isClassLoadable("org.tukaani.xz.XZOutputStream", this.getContext()))) {
            this.addWarn("XZ library missing, falling back to GZ compression");
            this.compressionMode = CompressionMode.GZ;
            this.fileNamePatternStr = this.replaceSuffix(this.fileNamePatternStr, CompressionMode.XZ_SUFFIX, CompressionMode.GZ_SUFFIX);
        }
    }

    private String replaceSuffix(String input, String existingSuffix, String newSuffix) {
        int existingSuffixLen = existingSuffix.length();
        if (input.endsWith(existingSuffix)) {
            return input.substring(0, input.length() - existingSuffixLen) + newSuffix;
        }
        throw new IllegalArgumentException("[" + input + "] should end with " + existingSuffix);
    }

    public void setFileNamePattern(String fnp) {
        this.fileNamePatternStr = fnp;
    }

    public String getFileNamePattern() {
        return this.fileNamePatternStr;
    }

    @Override
    public CompressionMode getCompressionMode() {
        return this.compressionMode;
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public void start() {
        this.started = true;
    }

    @Override
    public void stop() {
        this.started = false;
    }

    @Override
    public void setParent(FileAppender<?> appender) {
        this.parent = appender;
    }

    public boolean isParentPrudent() {
        return this.parent.isPrudent();
    }

    public String getParentsRawFileProperty() {
        return this.parent.rawFileProperty();
    }
}

