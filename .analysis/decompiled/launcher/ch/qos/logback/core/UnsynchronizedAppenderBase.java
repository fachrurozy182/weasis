/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.ReentryGuard;
import ch.qos.logback.core.util.ReentryGuardFactory;
import ch.qos.logback.core.util.SimpleTimeBasedGuard;
import java.util.List;

public abstract class UnsynchronizedAppenderBase<E>
extends ContextAwareBase
implements Appender<E> {
    protected volatile boolean started = false;
    private ReentryGuard reentryGuard;
    protected String name;
    private FilterAttachableImpl<E> fai = new FilterAttachableImpl();
    private SimpleTimeBasedGuard notStartedGuard = new SimpleTimeBasedGuard();
    private SimpleTimeBasedGuard exceptionGuard = new SimpleTimeBasedGuard();

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void doAppend(E eventObject) {
        if (!this.started) {
            if (this.notStartedGuard.allow()) {
                this.addStatus(new WarnStatus("Attempted to append to non started appender [" + this.name + "].", this));
            }
            return;
        }
        if (this.reentryGuard.isLocked()) {
            return;
        }
        try {
            this.reentryGuard.lock();
            if (this.getFilterChainDecision(eventObject) == FilterReply.DENY) {
                return;
            }
            this.append(eventObject);
        }
        catch (Exception e) {
            if (this.exceptionGuard.allow()) {
                this.addError("Appender [" + this.name + "] failed to append.", e);
            }
        }
        finally {
            this.reentryGuard.unlock();
        }
    }

    protected abstract void append(E var1);

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void start() {
        this.reentryGuard = this.buildReentryGuard();
        this.started = true;
    }

    protected ReentryGuard buildReentryGuard() {
        return ReentryGuardFactory.makeGuard(ReentryGuardFactory.GuardType.NOP);
    }

    @Override
    public void stop() {
        this.started = false;
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.name + "]";
    }

    @Override
    public void addFilter(Filter<E> newFilter) {
        this.fai.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        this.fai.clearAllFilters();
    }

    @Override
    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return this.fai.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(E event) {
        return this.fai.getFilterChainDecision(event);
    }
}

