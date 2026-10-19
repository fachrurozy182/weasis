/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.css.impl;

import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import com.github.weisj.jsvg.parser.css.impl.ParserException;
import com.github.weisj.jsvg.parser.css.impl.SimpleCssParser;
import com.github.weisj.jsvg.parser.css.impl.Token;
import com.github.weisj.jsvg.parser.css.impl.TokenType;
import java.text.MessageFormat;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public final class Lexer {
    private static final Logger LOGGER = LogFactory.createLogger(Lexer.class);
    @NotNull
    private final List<char[]> input;
    private int listIndex = 0;
    private int index = 0;
    private boolean inRuleDefinition;
    private boolean parsingRaw;
    private char current;

    public Lexer(@NotNull List<char[]> input, SimpleCssParser.Mode mode) {
        this.input = input;
        this.inRuleDefinition = mode == SimpleCssParser.Mode.SINGLE_RULE;
        this.current = '\u0000';
    }

    @NotNull
    public Token nextToken() {
        this.consumeWhiteSpace();
        if (this.inRuleDefinition && this.parsingRaw) {
            this.parsingRaw = false;
            return new Token(TokenType.RAW_DATA, this.readWhile(c -> c.charValue() != ';' && c.charValue() != '}'));
        }
        if (this.isEof()) {
            return new Token(TokenType.EOF);
        }
        char c2 = this.current();
        switch (c2) {
            case '{': {
                this.inRuleDefinition = true;
                this.parsingRaw = false;
                this.next();
                return new Token(TokenType.CURLY_OPEN);
            }
            case '}': {
                this.inRuleDefinition = false;
                this.parsingRaw = false;
                this.next();
                return new Token(TokenType.CURLY_CLOSE);
            }
            case ':': {
                this.parsingRaw = true;
                this.next();
                return new Token(TokenType.COLON);
            }
            case ';': {
                this.next();
                return new Token(TokenType.SEMICOLON);
            }
            case ',': {
                this.next();
                return new Token(TokenType.COMMA);
            }
            case '.': {
                this.next();
                return new Token(TokenType.CLASS_NAME, this.readIdentifier());
            }
            case '#': {
                this.next();
                return new Token(TokenType.ID_NAME, this.readIdentifier());
            }
            case '/': {
                if (this.peekNext() != '*') break;
                this.next();
                this.next();
                String comment = this.readWhile(n -> n.charValue() != '*' || this.peekNext() != '/');
                this.next();
                this.next();
                return new Token(TokenType.COMMENT, comment);
            }
        }
        return new Token(TokenType.IDENTIFIER, this.readIdentifier());
    }

    private boolean isEof() {
        return this.listIndex >= this.input.size() || this.listIndex == this.input.size() - 1 && this.index >= this.input.get(this.listIndex).length;
    }

    private void consumeWhiteSpace() {
        while (Character.isWhitespace(this.current())) {
            this.next();
        }
    }

    private boolean isIdentifierCharStart(char c) {
        if ('A' <= c && c <= 'Z') {
            return true;
        }
        if ('a' <= c && c <= 'z') {
            return true;
        }
        if (c == '-') {
            return true;
        }
        return c == '_';
    }

    private boolean isIdentifierChar(char c) {
        if (this.isIdentifierCharStart(c)) {
            return true;
        }
        return '0' <= c && c <= '9';
    }

    @NotNull
    private String readIdentifier() {
        if (!this.isIdentifierCharStart(this.current()) || !this.isIdentifierChar(this.current())) {
            LOGGER.log(Logger.Level.WARNING, () -> MessageFormat.format("Identifier starting with unexpected char ''{0}''", Character.valueOf(this.current())));
            if (this.readWhile(this::isIdentifierChar).isEmpty()) {
                this.next();
            }
            throw new ParserException();
        }
        return this.readWhile(this::isIdentifierChar);
    }

    @NotNull
    private String readWhile(@NotNull Predicate<Character> filter) {
        if (this.isEof()) {
            return "";
        }
        int startListIndex = this.listIndex;
        int startIndex = this.index;
        while (!this.isEof() && filter.test(Character.valueOf(this.current()))) {
            this.next();
        }
        int endListIndex = this.isEof() ? this.input.size() - 1 : this.listIndex;
        int endIndex = this.isEof() ? this.input.get(endListIndex).length : this.index;
        StringBuilder builder = new StringBuilder();
        int start = startIndex;
        for (int i = startListIndex; i <= endListIndex; ++i) {
            char[] segment = this.input.get(i);
            int end = i == endListIndex ? endIndex : segment.length;
            builder.append(String.valueOf(segment, start, end - start));
            start = 0;
        }
        return builder.toString();
    }

    private char current() {
        if (this.current == '\u0000') {
            this.current = this.nextChar();
        }
        return this.current;
    }

    private char nextChar() {
        if (this.isEof()) {
            return '\u0000';
        }
        return this.input.get(this.listIndex)[this.index];
    }

    private char peekNext() {
        if (this.isEof()) {
            return '\u0000';
        }
        if (this.index + 1 < this.input.get(this.listIndex).length) {
            return this.input.get(this.listIndex)[this.index + 1];
        }
        for (int currentIndex = this.listIndex + 1; currentIndex < this.input.size(); ++currentIndex) {
            if (this.input.get(currentIndex).length <= 0) continue;
            return this.input.get(currentIndex)[0];
        }
        return '\u0000';
    }

    private void next() {
        ++this.index;
        if (this.index >= this.input.get(this.listIndex).length && this.listIndex + 1 < this.input.size()) {
            this.index = 0;
            ++this.listIndex;
        }
        this.current = this.nextChar();
    }
}

