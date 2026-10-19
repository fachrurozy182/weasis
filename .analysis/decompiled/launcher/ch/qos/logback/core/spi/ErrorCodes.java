/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.spi;

public class ErrorCodes {
    public static final String EMPTY_MODEL_STACK = "Could not find valid configuration instructions. Exiting.";
    public static final String MISSING_IF_EMPTY_MODEL_STACK = "Unexpected empty model stack. Have you omitted the <if> part?";
    public static final String PARENT_MODEL_NOT_FOUND = "Could not find parent model.";
    public static final String SKIPPING_IMPLICIT_MODEL_ADDITION = " Will not add current implicit model as subModel.";
    public static final String ROOT_LEVEL_CANNOT_BE_SET_TO_NULL = "The level for the ROOT logger cannot be set to NULL or INHERITED. Ignoring.";
}

