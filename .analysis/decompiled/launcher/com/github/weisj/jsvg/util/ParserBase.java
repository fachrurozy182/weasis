/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.util;

import org.jetbrains.annotations.NotNull;

public class ParserBase {
    @NotNull
    protected final String input;
    private final int inputLength;
    private int index;

    public ParserBase(@NotNull String input, int startIndex) {
        this.input = input;
        this.inputLength = input.length();
        this.index = startIndex;
    }

    protected char peek() {
        return this.input.charAt(this.index);
    }

    protected void consume() {
        ++this.index;
    }

    public boolean hasNext() {
        return this.index < this.inputLength;
    }

    public void consumeWhiteSpaceOrSeparator() {
        while (this.hasNext() && this.isWhiteSpaceOrSeparator(this.peek())) {
            this.consume();
        }
    }

    private boolean isWhiteSpaceOrSeparator(char c) {
        return c == ',' || Character.isWhitespace(c);
    }

    private String nextNumberString() {
        int start = this.index;
        NumberCharState state = new NumberCharState();
        while (this.hasNext() && this.isValidNumberChar(this.peek(), state)) {
            this.consume();
        }
        int end = this.index;
        return this.input.substring(start, end);
    }

    protected float nextFloatOrUnspecified() {
        if (!this.hasNext()) {
            return Float.NaN;
        }
        return this.nextFloat();
    }

    public float nextFloat() throws NumberFormatException {
        int start = this.index;
        String token = this.nextNumberString();
        try {
            return Float.parseFloat(token);
        }
        catch (NumberFormatException e) {
            String msg = "Unexpected token '" + token + "' rest=" + this.input.substring(start, Math.min(this.input.length(), start + 10)) + this.currentLocation();
            throw new IllegalStateException(msg, e);
        }
    }

    public double nextDouble() throws NumberFormatException {
        int start = this.index;
        String token = this.nextNumberString();
        try {
            return Double.parseDouble(token);
        }
        catch (NumberFormatException e) {
            String msg = "Unexpected token '" + token + "' rest=" + this.input.substring(start, Math.min(this.input.length(), start + 10)) + this.currentLocation();
            throw new IllegalStateException(msg, e);
        }
    }

    @NotNull
    protected String currentLocation() {
        return "(index=" + this.index + " in input=" + this.input + ")";
    }

    private boolean isValidNumberChar(char c, NumberCharState state) {
        boolean valid;
        boolean bl = valid = '0' <= c && c <= '9';
        if (valid && state.iteration == 1 && this.input.charAt(this.index - 1) == '0') {
            return false;
        }
        boolean bl2 = state.signAllowed = state.signAllowed && !valid;
        if (state.dotAllowed && !valid) {
            valid = c == '.';
            boolean bl3 = state.dotAllowed = !valid;
        }
        if (state.signAllowed && !valid) {
            state.signAllowed = valid = c == '+' || c == '-';
        }
        if (state.exponentAllowed && !valid) {
            valid = c == 'e' || c == 'E';
            state.exponentAllowed = !valid;
            state.signAllowed = valid;
            state.dotAllowed = !valid;
        }
        ++state.iteration;
        return valid;
    }

    private static final class NumberCharState {
        int iteration = 0;
        boolean dotAllowed = true;
        boolean signAllowed = true;
        boolean exponentAllowed = true;

        private NumberCharState() {
        }
    }
}

