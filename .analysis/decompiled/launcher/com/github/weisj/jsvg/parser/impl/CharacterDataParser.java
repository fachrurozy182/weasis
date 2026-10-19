/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import org.jetbrains.annotations.NotNull;

final class CharacterDataParser {
    private static final Logger LOGGER = LogFactory.createLogger(CharacterDataParser.class);
    private State state = State.SEGMENT_START;
    private StringBuilder buffer = new StringBuilder();
    private boolean hadAnyContent = false;
    private char[] data;
    private int begin;
    private int end;

    CharacterDataParser() {
    }

    public void append(char[] ch, int offset, int length) {
        int segmentBreaks;
        if (length == 0) {
            return;
        }
        this.hadAnyContent = true;
        LOGGER.log(Logger.Level.DEBUG, () -> String.format("Append: [%s]", new String(ch, offset, length).replace("\n", "\\n")));
        this.data = ch;
        this.begin = offset;
        this.end = offset + length;
        if (CharacterDataParser.isSegmentBreak(this.data[this.begin])) {
            segmentBreaks = this.trimLeadingWhiteSpace();
            if (this.state == State.SEGMENT_BREAK) {
                ++segmentBreaks;
            }
            if (this.begin > offset && segmentBreaks > 1) {
                --this.begin;
                this.data[this.begin] = 32;
                if (this.state == State.CHARACTER || this.state == State.SEGMENT_BREAK) {
                    this.state = State.WHITESPACE_AFTER_CHAR;
                }
            }
        }
        segmentBreaks = this.trimTrailingWhiteSpace();
        if (this.end < offset + length) {
            this.data[this.end] = segmentBreaks > 0 ? 10 : 32;
            ++this.end;
        }
        if (this.begin >= this.end) {
            return;
        }
        LOGGER.log(Logger.Level.DEBUG, () -> String.format("Portion: [%s]", new String(ch, this.begin, this.end - this.begin).replace("\n", "\\n")));
        this.buffer.ensureCapacity(this.buffer.length() + this.end - this.begin);
        this.appendData();
    }

    private void appendData() {
        int initialOffset = this.begin;
        while (this.begin < this.end) {
            char c = this.data[this.begin];
            boolean segmentBreak = CharacterDataParser.isSegmentBreak(c);
            boolean whiteSpace = CharacterDataParser.isWhitespace(c);
            if (!segmentBreak && !whiteSpace) {
                if (this.state == State.WHITESPACE_AFTER_CHAR || this.state.isVisualSpace && this.begin > initialOffset) {
                    this.buffer.append(' ');
                }
                this.state = State.CHARACTER;
                this.buffer.append(c);
            } else if (whiteSpace) {
                switch (this.state.ordinal()) {
                    case 2: 
                    case 4: {
                        this.state = State.WHITESPACE_AFTER_CHAR;
                        break;
                    }
                    case 1: 
                    case 3: {
                        this.state = State.WHITESPACE_AFTER_SEGMENT_BREAK;
                        break;
                    }
                }
            } else {
                this.state = State.SEGMENT_BREAK;
            }
            ++this.begin;
        }
    }

    public boolean canFlush(boolean dueToSegmentBreak) {
        if (this.state == State.SEGMENT_START && !this.hadAnyContent) {
            return false;
        }
        return dueToSegmentBreak || this.buffer.length() > 0;
    }

    @NotNull
    public String flush(boolean dueToSegmentBreak) {
        this.hadAnyContent = false;
        if (dueToSegmentBreak && this.state != State.CHARACTER) {
            this.buffer.append(' ');
        }
        if (dueToSegmentBreak) {
            this.state = State.SEGMENT_BREAK;
        }
        String result = this.buffer.toString();
        LOGGER.log(Logger.Level.DEBUG, () -> String.format("Flush segBreak=%s[%s]", dueToSegmentBreak, this.buffer));
        this.buffer = new StringBuilder();
        return result;
    }

    private int trimLeadingWhiteSpace() {
        int segmentBreakCount = 0;
        while (this.begin < this.end) {
            if (CharacterDataParser.isSegmentBreak(this.data[this.begin])) {
                ++segmentBreakCount;
                ++this.begin;
                continue;
            }
            if (!CharacterDataParser.isWhitespace(this.data[this.begin])) break;
            ++this.begin;
        }
        return segmentBreakCount;
    }

    private int trimTrailingWhiteSpace() {
        int segmentBreakCount = 0;
        while (this.begin < this.end) {
            if (CharacterDataParser.isSegmentBreak(this.data[this.end - 1])) {
                ++segmentBreakCount;
                --this.end;
                continue;
            }
            if (!CharacterDataParser.isWhitespace(this.data[this.end - 1])) break;
            --this.end;
        }
        return segmentBreakCount;
    }

    private static boolean isSegmentBreak(char c) {
        return c == '\n' || c == '\r';
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }

    private static enum State {
        SEGMENT_START(false),
        SEGMENT_BREAK(true),
        WHITESPACE_AFTER_CHAR(true),
        WHITESPACE_AFTER_SEGMENT_BREAK(true),
        CHARACTER(false);

        private final boolean isVisualSpace;

        private State(boolean isVisualSpace) {
            this.isVisualSpace = isVisualSpace;
        }
    }
}

