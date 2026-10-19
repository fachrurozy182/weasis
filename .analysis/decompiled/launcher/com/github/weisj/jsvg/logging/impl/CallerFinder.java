/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.logging.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CallerFinder {
    private static final boolean HAS_STACK_WALKER;
    private static final MethodHandle STACK_WALKER_WALK;
    private static final MethodHandle STACK_FRAME_GET_CLASS_NAME;
    private static final MethodHandle STACK_FRAME_GET_METHOD_NAME;
    private final String dispatcherClassName;
    private boolean lookingForCaller = false;

    public CallerFinder(@NotNull String dispatcherClassName) {
        this.dispatcherClassName = dispatcherClassName;
    }

    public Optional<StackTraceElement> findCaller() {
        StackTraceElement[] stack;
        if (HAS_STACK_WALKER) {
            try {
                return STACK_WALKER_WALK.invoke(stream -> stream.map(f -> {
                    try {
                        String className = STACK_FRAME_GET_CLASS_NAME.invoke(f);
                        String methodName = STACK_FRAME_GET_METHOD_NAME.invoke(f);
                        return new StackTraceElement(className, methodName, null, -1);
                    }
                    catch (Throwable t) {
                        return null;
                    }
                }).filter(this::isCallerFrame).findFirst());
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        for (StackTraceElement element : stack = new Throwable().getStackTrace()) {
            if (!this.isCallerFrame(element)) continue;
            return Optional.of(element);
        }
        return Optional.empty();
    }

    private boolean isCallerFrame(@Nullable StackTraceElement frame) {
        if (frame == null) {
            return false;
        }
        String cname = frame.getClassName();
        if (!this.lookingForCaller) {
            this.lookingForCaller = cname.equals(this.dispatcherClassName);
            return false;
        }
        return true;
    }

    static {
        MethodHandle walk = null;
        MethodHandle getClassName = null;
        MethodHandle getMethodName = null;
        boolean hasStackWalker = false;
        try {
            Class<?> stackWalkerClass = Class.forName("java.lang.StackWalker");
            Class<?> optionClass = Class.forName("java.lang.StackWalker$Option");
            Object retainClassRef = Enum.valueOf(optionClass, "RETAIN_CLASS_REFERENCE");
            MethodHandle getInstance = MethodHandles.publicLookup().findStatic(stackWalkerClass, "getInstance", MethodType.methodType(stackWalkerClass, optionClass));
            Object stackWalkerInstance = getInstance.invoke(retainClassRef);
            walk = MethodHandles.lookup().findVirtual(stackWalkerClass, "walk", MethodType.methodType(Object.class, Function.class));
            walk = walk.bindTo(stackWalkerInstance);
            Class<?> stackFrameClass = Class.forName("java.lang.StackWalker$StackFrame");
            getClassName = MethodHandles.publicLookup().findVirtual(stackFrameClass, "getClassName", MethodType.methodType(String.class));
            getMethodName = MethodHandles.publicLookup().findVirtual(stackFrameClass, "getMethodName", MethodType.methodType(String.class));
            hasStackWalker = true;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        HAS_STACK_WALKER = hasStackWalker;
        STACK_WALKER_WALK = walk;
        STACK_FRAME_GET_CLASS_NAME = getClassName;
        STACK_FRAME_GET_METHOD_NAME = getMethodName;
    }
}

