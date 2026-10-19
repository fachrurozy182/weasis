/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.boolex;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

public class ExceptionMatchEvaluator
extends EventEvaluatorBase<ILoggingEvent> {
    String exceptionClass;
    private boolean start = false;

    @Override
    public void start() {
        if (this.exceptionClass == null) {
            this.addError("The exceptionClass must be set");
            return;
        }
        this.start = true;
    }

    @Override
    public void stop() {
        this.start = false;
    }

    @Override
    public boolean isStarted() {
        return this.start;
    }

    @Override
    public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null) {
            return false;
        }
        return throwableProxy.getClassName().equalsIgnoreCase(this.exceptionClass);
    }

    public String getExceptionClass() {
        return this.exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }
}

