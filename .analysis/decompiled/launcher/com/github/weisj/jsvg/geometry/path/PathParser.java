/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.path;

import com.github.weisj.jsvg.geometry.path.Arc;
import com.github.weisj.jsvg.geometry.path.BezierPathCommand;
import com.github.weisj.jsvg.geometry.path.Cubic;
import com.github.weisj.jsvg.geometry.path.CubicBezierCommand;
import com.github.weisj.jsvg.geometry.path.CubicSmooth;
import com.github.weisj.jsvg.geometry.path.Horizontal;
import com.github.weisj.jsvg.geometry.path.LineTo;
import com.github.weisj.jsvg.geometry.path.LineToBezier;
import com.github.weisj.jsvg.geometry.path.MoveTo;
import com.github.weisj.jsvg.geometry.path.PathCommand;
import com.github.weisj.jsvg.geometry.path.Quadratic;
import com.github.weisj.jsvg.geometry.path.QuadraticSmooth;
import com.github.weisj.jsvg.geometry.path.Terminal;
import com.github.weisj.jsvg.geometry.path.Vertical;
import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import com.github.weisj.jsvg.util.ParserBase;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class PathParser
extends ParserBase {
    private static final Logger LOGGER = LogFactory.createLogger(PathParser.class);
    private char currentCommand;

    public PathParser(@NotNull String input) {
        super(input, 0);
    }

    private boolean isCommandChar(char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    @Override
    public float nextFloat() throws NumberFormatException {
        float f = super.nextFloat();
        this.consumeWhiteSpaceOrSeparator();
        return f;
    }

    private boolean nextFlag() {
        char c = this.peek();
        this.consume();
        this.consumeWhiteSpaceOrSeparator();
        if (c == '1') {
            return true;
        }
        if (c == '0') {
            return false;
        }
        throw new IllegalStateException("Invalid flag value '" + c + "' " + this.currentLocation());
    }

    @NotNull
    public BezierPathCommand parseMeshCommand() {
        char peekChar = this.peek();
        this.currentCommand = (char)122;
        if (this.isCommandChar(peekChar)) {
            this.consume();
            this.currentCommand = peekChar;
        }
        this.consumeWhiteSpaceOrSeparator();
        switch (this.currentCommand) {
            case 'l': {
                return new LineToBezier(true, this.nextFloatOrUnspecified(), this.nextFloatOrUnspecified());
            }
            case 'L': {
                return new LineToBezier(false, this.nextFloatOrUnspecified(), this.nextFloatOrUnspecified());
            }
            case 'c': {
                return new CubicBezierCommand(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloatOrUnspecified(), this.nextFloatOrUnspecified());
            }
            case 'C': {
                return new CubicBezierCommand(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloatOrUnspecified(), this.nextFloatOrUnspecified());
            }
        }
        throw new IllegalStateException("Only commands c C l L allowed");
    }

    public PathCommand[] parsePathCommand() {
        if ("none".equals(this.input)) {
            return new PathCommand[0];
        }
        ArrayList<PathCommand> commands = new ArrayList<PathCommand>();
        try {
            this.parsePathCommandInto(commands);
        }
        catch (Exception e) {
            LOGGER.log(Logger.Level.INFO, "Error parsing path command: " + e.getMessage());
        }
        return commands.toArray(new PathCommand[0]);
    }

    private void parsePathCommandInto(List<PathCommand> commands) {
        this.currentCommand = (char)90;
        while (this.hasNext()) {
            PathCommand cmd;
            char peekChar = this.peek();
            if (this.isCommandChar(peekChar)) {
                this.consume();
                this.currentCommand = peekChar;
            }
            this.consumeWhiteSpaceOrSeparator();
            if (this.currentCommand != 'M' && this.currentCommand != 'm' && commands.isEmpty()) {
                throw new IllegalArgumentException("Path must start with a move command " + this.currentLocation());
            }
            switch (this.currentCommand) {
                case 'M': {
                    cmd = new MoveTo(false, this.nextFloat(), this.nextFloat());
                    this.currentCommand = (char)76;
                    break;
                }
                case 'm': {
                    cmd = new MoveTo(true, this.nextFloat(), this.nextFloat());
                    this.currentCommand = (char)108;
                    break;
                }
                case 'L': {
                    cmd = new LineTo(false, this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'l': {
                    cmd = new LineTo(true, this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'H': {
                    cmd = new Horizontal(false, this.nextFloat());
                    break;
                }
                case 'h': {
                    cmd = new Horizontal(true, this.nextFloat());
                    break;
                }
                case 'V': {
                    cmd = new Vertical(false, this.nextFloat());
                    break;
                }
                case 'v': {
                    cmd = new Vertical(true, this.nextFloat());
                    break;
                }
                case 'A': {
                    cmd = new Arc(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFlag(), this.nextFlag(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'a': {
                    cmd = new Arc(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFlag(), this.nextFlag(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'Q': {
                    cmd = new Quadratic(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'q': {
                    cmd = new Quadratic(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'T': {
                    cmd = new QuadraticSmooth(false, this.nextFloat(), this.nextFloat());
                    break;
                }
                case 't': {
                    cmd = new QuadraticSmooth(true, this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'C': {
                    cmd = new Cubic(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'c': {
                    cmd = new Cubic(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'S': {
                    cmd = new CubicSmooth(false, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 's': {
                    cmd = new CubicSmooth(true, this.nextFloat(), this.nextFloat(), this.nextFloat(), this.nextFloat());
                    break;
                }
                case 'Z': 
                case 'z': {
                    cmd = new Terminal();
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Invalid path element " + this.currentCommand + this.currentLocation());
                }
            }
            commands.add(cmd);
        }
    }
}

