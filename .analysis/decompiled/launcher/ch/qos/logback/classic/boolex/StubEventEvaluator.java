/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.boolex;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import ch.qos.logback.core.boolex.Matcher;
import java.util.ArrayList;
import java.util.List;

public class StubEventEvaluator
extends EventEvaluatorBase<ILoggingEvent> {
    public static final String MSG_0 = "This class is a stub for JaninoEventEvaluator which was removed in logback version 1.5.13";
    public static final String MSG_1 = "You can migrate existing configurations to Java-only equivalents with the \"Janino Expression migrator\" tool at:";
    public static final String MSG_2 = "https://logback.qos.ch/translator/services/janinoExpressionMigrator.html";
    protected List<Matcher> matcherList = new ArrayList<Matcher>();
    String expression;

    @Override
    public void start() {
        this.stop();
        this.addWarn(MSG_0);
        this.addWarn(MSG_1);
        this.addWarn(MSG_2);
    }

    @Override
    public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {
        return false;
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void addMatcher(Matcher matcher) {
        this.matcherList.add(matcher);
    }
}

