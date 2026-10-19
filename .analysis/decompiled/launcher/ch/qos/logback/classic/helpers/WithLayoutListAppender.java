/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.helpers;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import java.util.List;

public class WithLayoutListAppender
extends AppenderBase<ILoggingEvent> {
    public List<String> list = new ArrayList<String>();
    String pattern;
    PatternLayout patternLayout;

    @Override
    public void start() {
        if (this.pattern == null) {
            this.addError("null pattern disallowed");
            return;
        }
        this.patternLayout = new PatternLayout();
        this.patternLayout.setContext(this.context);
        this.patternLayout.setPattern(this.pattern);
        this.patternLayout.start();
        if (this.patternLayout.isStarted()) {
            super.start();
        }
    }

    @Override
    protected void append(ILoggingEvent e) {
        String result = this.patternLayout.doLayout(e);
        this.list.add(result);
    }

    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}

