/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.EndEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import java.util.ArrayList;
import java.util.List;

public class EventPlayer {
    final SaxEventInterpreter interpreter;
    final List<SaxEvent> saxEvents;
    int currentIndex;

    public EventPlayer(SaxEventInterpreter interpreter, List<SaxEvent> saxEvents) {
        this.interpreter = interpreter;
        this.saxEvents = saxEvents;
    }

    public List<SaxEvent> getCopyOfPlayerEventList() {
        return new ArrayList<SaxEvent>(this.saxEvents);
    }

    public void play() {
        this.currentIndex = 0;
        while (this.currentIndex < this.saxEvents.size()) {
            SaxEvent se = this.saxEvents.get(this.currentIndex);
            if (se instanceof StartEvent) {
                this.interpreter.startElement((StartEvent)se);
            } else if (se instanceof BodyEvent) {
                this.interpreter.characters((BodyEvent)se);
            } else if (se instanceof EndEvent) {
                this.interpreter.endElement((EndEvent)se);
            }
            ++this.currentIndex;
        }
    }

    public void addEventsDynamically(List<SaxEvent> eventList, int offset) {
        this.saxEvents.addAll(this.currentIndex + offset, eventList);
    }
}

