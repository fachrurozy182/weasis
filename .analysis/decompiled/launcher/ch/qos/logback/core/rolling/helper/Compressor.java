/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.CompressionStrategy;
import ch.qos.logback.core.rolling.helper.GZCompressionStrategy;
import ch.qos.logback.core.rolling.helper.ZipCompressionStrategy;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;
import ch.qos.logback.core.util.OptionHelper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Compressor
extends ContextAwareBase {
    public static final String COULD_NOT_OBTAIN_COMPRESSION_STRATEGY_MESSAGE = "Could not obtain compression strategy";
    public static final String XZ_COMPRESSION_STRATEGY_CLASS_NAME = "ch.qos.logback.core.rolling.helper.XZCompressionStrategy";
    final CompressionMode compressionMode;
    static final int BUFFER_SIZE = 8192;

    public Compressor(CompressionMode compressionMode) {
        this.compressionMode = compressionMode;
    }

    public void compress(String originalFileName, String compressedFileName, String innerEntryName) {
        CompressionStrategy compressionStrategy = this.makeCompressionStrategy(this.compressionMode);
        if (compressionStrategy == null) {
            this.addWarn(COULD_NOT_OBTAIN_COMPRESSION_STRATEGY_MESSAGE);
            return;
        }
        compressionStrategy.setContext(this.getContext());
        compressionStrategy.compress(originalFileName, compressedFileName, innerEntryName);
    }

    CompressionStrategy makeCompressionStrategy(CompressionMode compressionMode) {
        switch (compressionMode) {
            case GZ: {
                return new GZCompressionStrategy();
            }
            case ZIP: {
                return new ZipCompressionStrategy();
            }
            case XZ: {
                return this.dynamicInstantiation(XZ_COMPRESSION_STRATEGY_CLASS_NAME);
            }
            case NONE: {
                throw new UnsupportedOperationException("compress method called in NONE compression mode");
            }
        }
        return null;
    }

    private CompressionStrategy dynamicInstantiation(String className) {
        try {
            return (CompressionStrategy)OptionHelper.instantiateByClassName(className, CompressionStrategy.class, this.getContext());
        }
        catch (DynamicClassLoadingException | IncompatibleClassException e) {
            this.addError("Could not instantiate " + className, e);
            return null;
        }
    }

    public static String computeFileNameStrWithoutCompSuffix(String fileNamePatternStr, CompressionMode compressionMode) {
        int len = fileNamePatternStr.length();
        switch (compressionMode) {
            case GZ: {
                if (fileNamePatternStr.endsWith(CompressionMode.GZ_SUFFIX)) {
                    return fileNamePatternStr.substring(0, len - CompressionMode.GZ_SUFFIX.length());
                }
                return fileNamePatternStr;
            }
            case ZIP: {
                if (fileNamePatternStr.endsWith(CompressionMode.ZIP_SUFFIX)) {
                    return fileNamePatternStr.substring(0, len - CompressionMode.ZIP_SUFFIX.length());
                }
                return fileNamePatternStr;
            }
            case XZ: {
                if (fileNamePatternStr.endsWith(CompressionMode.XZ_SUFFIX)) {
                    return fileNamePatternStr.substring(0, len - CompressionMode.XZ_SUFFIX.length());
                }
                return fileNamePatternStr;
            }
            case NONE: {
                return fileNamePatternStr;
            }
        }
        throw new IllegalStateException("Execution should not reach this point");
    }

    public String toString() {
        return this.getClass().getName();
    }

    public Future<?> asyncCompress(String nameOfFile2Compress, String nameOfCompressedFile, String innerEntryName) throws RolloverFailure {
        CompressionRunnable runnable = new CompressionRunnable(nameOfFile2Compress, nameOfCompressedFile, innerEntryName);
        ExecutorService executorService = this.context.getExecutorService();
        Future<?> future = executorService.submit(runnable);
        return future;
    }

    class CompressionRunnable
    implements Runnable {
        final String nameOfFile2Compress;
        final String nameOfCompressedFile;
        final String innerEntryName;

        public CompressionRunnable(String nameOfFile2Compress, String nameOfCompressedFile, String innerEntryName) {
            this.nameOfFile2Compress = nameOfFile2Compress;
            this.nameOfCompressedFile = nameOfCompressedFile;
            this.innerEntryName = innerEntryName;
        }

        @Override
        public void run() {
            Compressor.this.compress(this.nameOfFile2Compress, this.nameOfCompressedFile, this.innerEntryName);
        }
    }
}

