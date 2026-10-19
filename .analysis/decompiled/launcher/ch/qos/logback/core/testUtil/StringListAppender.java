/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import java.util.ArrayList;
import java.util.List;

public class StringListAppender<E>
extends AppenderBase<E> {
    Layout<E> layout;
    public List<String> strList = new ArrayList<String>();

    @Override
    public void start() {
        this.strList.clear();
        if (this.layout == null || !this.layout.isStarted()) {
            return;
        }
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    protected void append(E eventObject) {
        String res = this.layout.doLayout(eventObject);
        this.strList.add(res);
    }

    public Layout<E> getLayout() {
        return this.layout;
    }

    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }
}

