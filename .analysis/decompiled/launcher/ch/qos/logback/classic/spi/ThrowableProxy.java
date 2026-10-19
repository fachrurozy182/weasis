/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.PackagingDataCalculator;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.OptionHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class ThrowableProxy
implements IThrowableProxy {
    static final StackTraceElementProxy[] EMPTY_STEP = new StackTraceElementProxy[0];
    private Throwable throwable;
    private String className;
    private String overridingMessage;
    private String message;
    StackTraceElementProxy[] stackTraceElementProxyArray;
    int commonFrames;
    private ThrowableProxy cause;
    private ThrowableProxy[] suppressed = NO_SUPPRESSED;
    private transient PackagingDataCalculator packagingDataCalculator;
    private boolean calculatedPackageData = false;
    private boolean cyclic;
    private static final ThrowableProxy[] NO_SUPPRESSED = new ThrowableProxy[0];

    public ThrowableProxy(Throwable throwable) {
        this(throwable, Collections.newSetFromMap(new IdentityHashMap()));
    }

    private ThrowableProxy(Throwable circular, boolean isCyclic) {
        this.throwable = circular;
        this.className = circular.getClass().getName();
        this.message = circular.getMessage();
        this.stackTraceElementProxyArray = EMPTY_STEP;
        this.cyclic = true;
    }

    public ThrowableProxy(Throwable throwable, Set<Throwable> alreadyProcessedSet) {
        Object[] throwableSuppressed;
        this.throwable = throwable;
        this.className = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.overridingMessage = this.buildOverridingMessage(throwable);
        this.stackTraceElementProxyArray = ThrowableProxyUtil.steArrayToStepArray(throwable.getStackTrace());
        this.cyclic = false;
        alreadyProcessedSet.add(throwable);
        Throwable nested = throwable.getCause();
        if (nested != null) {
            if (alreadyProcessedSet.contains(nested)) {
                this.cause = new ThrowableProxy(nested, true);
            } else {
                this.cause = new ThrowableProxy(nested, alreadyProcessedSet);
                this.cause.commonFrames = ThrowableProxyUtil.findNumberOfCommonFrames(nested.getStackTrace(), this.stackTraceElementProxyArray);
            }
        }
        if (OptionHelper.isNotEmtpy(throwableSuppressed = throwable.getSuppressed())) {
            ArrayList<ThrowableProxy> suppressedList = new ArrayList<ThrowableProxy>(throwableSuppressed.length);
            for (Object sup : throwableSuppressed) {
                ThrowableProxy throwableProxy;
                if (alreadyProcessedSet.contains(sup)) {
                    throwableProxy = new ThrowableProxy((Throwable)sup, true);
                    suppressedList.add(throwableProxy);
                    continue;
                }
                throwableProxy = new ThrowableProxy((Throwable)sup, alreadyProcessedSet);
                throwableProxy.commonFrames = ThrowableProxyUtil.findNumberOfCommonFrames(((Throwable)sup).getStackTrace(), this.stackTraceElementProxyArray);
                suppressedList.add(throwableProxy);
            }
            this.suppressed = suppressedList.toArray(new ThrowableProxy[suppressedList.size()]);
        }
    }

    private String buildOverridingMessage(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        ThrowableProxyUtil.appendNominalFirstLine(sb, throwable.getClass().getName(), throwable.getMessage());
        String messageFromToString = throwable.toString();
        String nominalMessage = sb.toString();
        if (!nominalMessage.equals(messageFromToString)) {
            return messageFromToString;
        }
        return null;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getOverridingMessage() {
        return this.overridingMessage;
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public StackTraceElementProxy[] getStackTraceElementProxyArray() {
        return this.stackTraceElementProxyArray;
    }

    @Override
    public boolean isCyclic() {
        return this.cyclic;
    }

    @Override
    public int getCommonFrames() {
        return this.commonFrames;
    }

    @Override
    public IThrowableProxy getCause() {
        return this.cause;
    }

    @Override
    public IThrowableProxy[] getSuppressed() {
        return this.suppressed;
    }

    public PackagingDataCalculator getPackagingDataCalculator() {
        if (this.throwable != null && this.packagingDataCalculator == null) {
            this.packagingDataCalculator = new PackagingDataCalculator();
        }
        return this.packagingDataCalculator;
    }

    public void calculatePackagingData() {
        if (this.calculatedPackageData) {
            return;
        }
        PackagingDataCalculator pdc = this.getPackagingDataCalculator();
        if (pdc != null) {
            this.calculatedPackageData = true;
            pdc.calculate(this);
        }
    }

    public void fullDump() {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElementProxy step : this.stackTraceElementProxyArray) {
            String string = step.toString();
            builder.append('\t').append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
            builder.append(CoreConstants.LINE_SEPARATOR);
        }
        System.out.println(builder.toString());
    }
}

