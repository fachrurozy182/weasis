/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.boolex.PropertyConditionBase;
import ch.qos.logback.core.util.IntHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ExpressionPropertyCondition
extends PropertyConditionBase {
    public static final String MALFORMED_EXPRESSION = "Malformed expression: ";
    protected Map<String, Function<String, Boolean>> functionMap = new HashMap<String, Function<String, Boolean>>();
    protected Map<String, BiFunction<String, String, Boolean>> biFunctionMap = new HashMap<String, BiFunction<String, String, Boolean>>();
    private static final String IS_NULL_FUNCTION_KEY = "isNull";
    private static final String IS_DEFINED_FUNCTION_KEY = "isDefined";
    private static final String PROPERTY_EQUALS_FUNCTION_KEY = "propertyEquals";
    private static final String PROPERTY_CONTAINS_FUNCTION_KEY = "propertyContains";
    private static final String TRUE_LITERAL = "true";
    private static final String FALSE_LITERAL = "false";
    private static final char QUOTE = '\"';
    private static final char COMMA = ',';
    private static final char LEFT_PAREN = '(';
    private static final char RIGHT_PAREN = ')';
    private static final char NOT_CHAR = '!';
    private static final char AMPERSAND_CHAR = '&';
    private static final char OR_CHAR = '|';
    String expression;
    List<Token> rpn;

    public ExpressionPropertyCondition() {
        this.functionMap.put(IS_NULL_FUNCTION_KEY, this::isNull);
        this.functionMap.put(IS_DEFINED_FUNCTION_KEY, this::isDefined);
        this.biFunctionMap.put(PROPERTY_EQUALS_FUNCTION_KEY, this::propertyEquals);
        this.biFunctionMap.put(PROPERTY_CONTAINS_FUNCTION_KEY, this::propertyContains);
    }

    @Override
    public void start() {
        if (this.expression == null || this.expression.isEmpty()) {
            this.addError("Empty expression");
            return;
        }
        try {
            List<Token> tokens = this.tokenize(this.expression.trim());
            this.rpn = this.infixToReversePolishNotation(tokens);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            this.addError(MALFORMED_EXPRESSION + e.getMessage());
            return;
        }
        super.start();
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean evaluate() {
        if (!this.isStarted()) {
            return false;
        }
        return this.evaluateRPN(this.rpn);
    }

    private List<Token> tokenize(String expr) throws IllegalArgumentException, IllegalStateException {
        ArrayList<Token> tokens = new ArrayList<Token>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) {
                ++i;
                continue;
            }
            if (c == '(' || c == ')') {
                tokens.add(Token.valueOf(c));
                ++i;
                continue;
            }
            if (c == '!') {
                tokens.add(new Token(TokenType.NOT));
                ++i;
                continue;
            }
            if (c == '&') {
                if ((c = expr.charAt(++i)) == '&') {
                    tokens.add(new Token(TokenType.AND));
                    ++i;
                    continue;
                }
                throw new IllegalArgumentException("Expected '&' after '&'");
            }
            if (c == '|') {
                if ((c = expr.charAt(++i)) == '|') {
                    tokens.add(new Token(TokenType.OR));
                    ++i;
                    continue;
                }
                throw new IllegalArgumentException("Expected '|' after '|'");
            }
            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && Character.isLetter(expr.charAt(i))) {
                    sb.append(expr.charAt(i++));
                }
                String identifier = sb.toString();
                if (TRUE_LITERAL.equals(identifier)) {
                    tokens.add(new Token(TokenType.TRUE));
                    continue;
                }
                if (FALSE_LITERAL.equals(identifier)) {
                    tokens.add(new Token(TokenType.FALSE));
                    continue;
                }
                String functionName = identifier;
                i = this.skipWhitespaces(i);
                this.checkExpectedCharacter('(', i);
                IntHolder intHolder = new IntHolder(++i);
                String param0 = this.extractQuotedString(intHolder);
                i = intHolder.value;
                i = this.skipWhitespaces(i);
                if (this.biFunctionMap.containsKey(functionName)) {
                    this.checkExpectedCharacter(',', i);
                    intHolder.set(++i);
                    String param1 = this.extractQuotedString(intHolder);
                    i = intHolder.get();
                    i = this.skipWhitespaces(i);
                    tokens.add(new Token(TokenType.BI_FUNCTION, functionName, param0, param1));
                } else {
                    tokens.add(new Token(TokenType.FUNCTION, functionName, param0, null));
                }
                this.checkExpectedCharacter(')', i);
                ++i;
                continue;
            }
            throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + i + " in [" + expr + "]");
        }
        return tokens;
    }

    private String extractQuotedString(IntHolder intHolder) {
        int i = intHolder.get();
        i = this.skipWhitespaces(i);
        this.checkExpectedCharacter('\"', i);
        int start = ++i;
        i = this.findIndexOfClosingQuote(i);
        String param = this.expression.substring(start, i);
        intHolder.set(++i);
        return param;
    }

    private int findIndexOfClosingQuote(int i) throws IllegalStateException {
        while (i < this.expression.length() && this.expression.charAt(i) != '\"') {
            ++i;
        }
        if (i >= this.expression.length()) {
            throw new IllegalStateException("Missing closing quote");
        }
        return i;
    }

    void checkExpectedCharacter(char expectedChar, int i) throws IllegalArgumentException {
        if (i >= this.expression.length() || this.expression.charAt(i) != expectedChar) {
            throw new IllegalArgumentException("In [" + this.expression + "] expecting '" + expectedChar + "' at position " + i);
        }
    }

    private int skipWhitespaces(int i) {
        while (i < this.expression.length() && Character.isWhitespace(this.expression.charAt(i))) {
            ++i;
        }
        return i;
    }

    private List<Token> infixToReversePolishNotation(List<Token> tokens) {
        ArrayList<Token> output = new ArrayList<Token>();
        Stack<Token> operatorStack = new Stack<Token>();
        for (Token token : tokens) {
            TokenType tokenType = token.tokenType;
            if (this.isPredicate(token)) {
                output.add(token);
                continue;
            }
            if (tokenType.isLogicalOperator()) {
                while (!operatorStack.isEmpty() && this.precedence((Token)operatorStack.peek()) >= this.precedence(token) && this.operatorAssociativity(token) == Associativity.LEFT) {
                    output.add((Token)operatorStack.pop());
                }
                operatorStack.push(token);
                continue;
            }
            if (tokenType == TokenType.LEFT_PAREN) {
                operatorStack.push(token);
                continue;
            }
            if (tokenType != TokenType.RIGHT_PAREN) continue;
            while (!operatorStack.isEmpty() && ((Token)operatorStack.peek()).tokenType != TokenType.LEFT_PAREN) {
                output.add((Token)operatorStack.pop());
            }
            if (operatorStack.isEmpty()) {
                throw new IllegalArgumentException("Mismatched parentheses, expecting '('");
            }
            operatorStack.pop();
        }
        while (!operatorStack.isEmpty()) {
            Token token = (Token)operatorStack.pop();
            TokenType tokenType = token.tokenType;
            if (tokenType == TokenType.LEFT_PAREN) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            output.add(token);
        }
        return output;
    }

    private boolean isPredicate(Token token) {
        TokenType tokenType = token.tokenType;
        return tokenType == TokenType.FUNCTION || tokenType == TokenType.BI_FUNCTION || tokenType == TokenType.TRUE || tokenType == TokenType.FALSE;
    }

    private int precedence(Token token) {
        TokenType tokenType = token.tokenType;
        switch (tokenType.ordinal()) {
            case 0: {
                return 3;
            }
            case 1: {
                return 2;
            }
            case 2: {
                return 1;
            }
        }
        return 0;
    }

    private Associativity operatorAssociativity(Token token) {
        TokenType tokenType = token.tokenType;
        return tokenType == TokenType.NOT ? Associativity.RIGHT : Associativity.LEFT;
    }

    private boolean evaluateRPN(List<Token> rpn) throws IllegalStateException {
        Stack<Boolean> resultStack = new Stack<Boolean>();
        for (Token token : rpn) {
            if (this.isPredicate(token)) {
                boolean value = this.evaluateFunctions(token);
                resultStack.push(value);
                continue;
            }
            switch (token.tokenType.ordinal()) {
                case 0: {
                    boolean a3 = (Boolean)resultStack.pop();
                    resultStack.push(!a3);
                    break;
                }
                case 1: {
                    boolean b2 = (Boolean)resultStack.pop();
                    boolean a2 = (Boolean)resultStack.pop();
                    resultStack.push(a2 && b2);
                    break;
                }
                case 2: {
                    boolean b1 = (Boolean)resultStack.pop();
                    boolean a1 = (Boolean)resultStack.pop();
                    resultStack.push(a1 || b1);
                }
            }
        }
        return (Boolean)resultStack.pop();
    }

    private boolean evaluateFunctions(Token token) throws IllegalStateException {
        if (token.tokenType == TokenType.TRUE) {
            return true;
        }
        if (token.tokenType == TokenType.FALSE) {
            return false;
        }
        String functionName = token.functionName;
        String param0 = token.param0;
        String param1 = token.param1;
        Function<String, Boolean> function = this.functionMap.get(functionName);
        if (function != null) {
            return function.apply(param0);
        }
        BiFunction<String, String, Boolean> biFunction = this.biFunctionMap.get(functionName);
        if (biFunction != null) {
            return biFunction.apply(param0, param1);
        }
        throw new IllegalStateException("Unknown function: " + String.valueOf(token));
    }

    static class Token {
        TokenType tokenType;
        String functionName;
        String param0;
        String param1;

        Token(TokenType tokenType) {
            this.tokenType = tokenType;
            switch (tokenType.ordinal()) {
                case 0: 
                case 1: 
                case 2: 
                case 3: 
                case 4: 
                case 7: 
                case 8: {
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected value: " + String.valueOf((Object)tokenType));
                }
            }
        }

        Token(TokenType tokenType, String functionName, String propertyKey, String value) {
            this.tokenType = tokenType;
            this.functionName = functionName;
            this.param0 = propertyKey;
            this.param1 = value;
        }

        public static Token valueOf(char c) {
            if (c == '(') {
                return new Token(TokenType.LEFT_PAREN);
            }
            if (c == ')') {
                return new Token(TokenType.RIGHT_PAREN);
            }
            throw new IllegalArgumentException("Unexpected char: " + c);
        }
    }

    static enum TokenType {
        NOT,
        AND,
        OR,
        TRUE,
        FALSE,
        FUNCTION,
        BI_FUNCTION,
        LEFT_PAREN,
        RIGHT_PAREN;


        boolean isLogicalOperator() {
            return this == NOT || this == AND || this == OR;
        }
    }

    static enum Associativity {
        LEFT,
        RIGHT;

    }
}

