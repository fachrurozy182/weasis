/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.css.impl;

import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import com.github.weisj.jsvg.parser.css.CssParser;
import com.github.weisj.jsvg.parser.css.StyleProperty;
import com.github.weisj.jsvg.parser.css.impl.Lexer;
import com.github.weisj.jsvg.parser.css.impl.ParserException;
import com.github.weisj.jsvg.parser.css.impl.SimpleStyleSheet;
import com.github.weisj.jsvg.parser.css.impl.Token;
import com.github.weisj.jsvg.parser.css.impl.TokenType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class SimpleCssParser
implements CssParser {
    private static final Logger LOGGER = LogFactory.createLogger(SimpleCssParser.class);

    @Override
    @NotNull
    public SimpleStyleSheet parse(@NotNull List<char[]> input) {
        return new Parser(input, Mode.STYLE_SHEET).parse();
    }

    @NotNull
    public @NotNull List<@NotNull StyleProperty> parseRules(@NotNull List<char[]> input) {
        return new Parser(input, Mode.SINGLE_RULE).parseRules();
    }

    private static final class Parser {
        @NotNull
        private final Lexer lexer;
        @NotNull
        private final SimpleStyleSheet sheet = new SimpleStyleSheet();
        private final TokenType ruleListEndType;
        @NotNull
        private Token current = new Token(TokenType.START);

        private Parser(@NotNull List<char[]> input, Mode mode) {
            this.ruleListEndType = mode == Mode.SINGLE_RULE ? TokenType.EOF : TokenType.CURLY_CLOSE;
            this.lexer = new Lexer(input, mode);
        }

        private void next() {
            Token next;
            while ((next = this.lexer.nextToken()).type() == TokenType.COMMENT) {
            }
            this.current = next;
        }

        private void expected(@NotNull String type) {
            LOGGER.log(Logger.Level.WARNING, () -> MessageFormat.format("Expected ''{0}'' but got ''{1}''", type, this.current));
        }

        private void consumeOrSkipAllowedToken(TokenType type, TokenType allowedTokeToSkip) {
            if (this.current.type() != type) {
                if (this.current.type() != allowedTokeToSkip) {
                    this.expected(type.toString());
                    throw new ParserException();
                }
                return;
            }
            this.next();
        }

        private void consume(TokenType type) {
            this.consumeOrSkipAllowedToken(type, null);
        }

        @NotNull
        private String consumeValue(TokenType type) {
            if (this.current.type() != type) {
                this.expected(type.toString());
                throw new ParserException();
            }
            if (this.current.data() == null) {
                throw new ParserException();
            }
            String value = Objects.requireNonNull(this.current.data());
            this.next();
            return value;
        }

        @NotNull
        private List<Token> readIdentifierList() {
            ArrayList<Token> list = new ArrayList<Token>();
            while (this.current.type() != TokenType.CURLY_OPEN && this.current.type() != TokenType.EOF) {
                TokenType type = this.current.type();
                if (type != TokenType.IDENTIFIER && type != TokenType.ID_NAME && type != TokenType.CLASS_NAME) {
                    this.expected("identifier");
                    throw new ParserException();
                }
                list.add(this.current);
                this.next();
                if (this.current.type() != TokenType.COMMA) break;
                this.next();
            }
            return list;
        }

        @NotNull
        private List<StyleProperty> readProperties() {
            ArrayList<StyleProperty> list = new ArrayList<StyleProperty>();
            while (this.current.type() != TokenType.CURLY_CLOSE && this.current.type() != TokenType.EOF) {
                String name = this.consumeValue(TokenType.IDENTIFIER);
                this.consume(TokenType.COLON);
                String value = this.consumeValue(TokenType.RAW_DATA);
                this.consumeOrSkipAllowedToken(TokenType.SEMICOLON, this.ruleListEndType);
                list.add(new StyleProperty(name, value.trim()));
            }
            return list;
        }

        private void skipToNextDefinition() {
            while (this.current.type() != TokenType.CURLY_CLOSE && this.current.type() != TokenType.EOF) {
                try {
                    this.next();
                }
                catch (ParserException parserException) {}
            }
            if (this.current.type() != TokenType.EOF) {
                this.current = new Token(TokenType.START);
            }
        }

        @NotNull
        @NotNull List<@NotNull StyleProperty> parseRules() {
            try {
                if (this.current.type() == TokenType.START) {
                    this.next();
                }
                return this.readProperties();
            }
            catch (ParserException e) {
                return Collections.emptyList();
            }
        }

        @NotNull
        SimpleStyleSheet parse() {
            do {
                try {
                    if (this.current.type() == TokenType.START) {
                        this.next();
                    }
                    List<Token> identifierList = this.readIdentifierList();
                    this.consume(TokenType.CURLY_OPEN);
                    List<StyleProperty> properties = this.readProperties();
                    this.consume(TokenType.CURLY_CLOSE);
                    block8: for (Token token : identifierList) {
                        switch (token.type()) {
                            case CLASS_NAME: {
                                this.sheet.addClassRules(Objects.requireNonNull(token.data()), properties);
                                continue block8;
                            }
                            case ID_NAME: {
                                this.sheet.addIdRules(Objects.requireNonNull(token.data()), properties);
                                continue block8;
                            }
                            case IDENTIFIER: {
                                this.sheet.addTagNameRules(Objects.requireNonNull(token.data()), properties);
                                continue block8;
                            }
                        }
                        throw new IllegalStateException("Toke = " + token);
                    }
                }
                catch (ParserException e) {
                    this.skipToNextDefinition();
                }
            } while (this.current.type() != TokenType.EOF);
            return this.sheet;
        }
    }

    public static enum Mode {
        STYLE_SHEET,
        SINGLE_RULE;

    }
}

