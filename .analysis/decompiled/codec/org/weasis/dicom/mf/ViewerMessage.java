/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.mf;

import java.util.Locale;
import java.util.Objects;

public record ViewerMessage(String title, String message, Level level) {
    public static final String TAG_DOCUMENT_MSG = "Message";
    public static final String MSG_ATTRIBUTE_TITLE = "title";
    public static final String MSG_ATTRIBUTE_DESC = "description";
    public static final String MSG_ATTRIBUTE_LEVEL = "severity";

    public ViewerMessage {
        Objects.requireNonNull(title, "Message title cannot be null");
        Objects.requireNonNull(message, "Message content cannot be null");
        Objects.requireNonNull(level, "Message level cannot be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("Message title cannot be blank");
        }
    }

    public static ViewerMessage info(String title, String message) {
        return new ViewerMessage(title, message, Level.INFO);
    }

    public static ViewerMessage warn(String title, String message) {
        return new ViewerMessage(title, message, Level.WARN);
    }

    public static ViewerMessage error(String title, String message) {
        return new ViewerMessage(title, message, Level.ERROR);
    }

    public static ViewerMessage fromException(String title, Exception exception) {
        String exceptionMessage = exception.getMessage();
        String message = exceptionMessage != null ? exceptionMessage : exception.getClass().getSimpleName();
        return ViewerMessage.error(title, message);
    }

    public boolean isProblem() {
        return this.level.isProblem();
    }

    public boolean isInfo() {
        return this.level == Level.INFO;
    }

    public boolean isWarning() {
        return this.level == Level.WARN;
    }

    public boolean isError() {
        return this.level == Level.ERROR;
    }

    public String getDisplayText() {
        return String.format("[%s] %s: %s", this.level.getDisplayName(), this.title, this.message);
    }

    public String toString() {
        return this.getDisplayText();
    }

    public static enum Level {
        INFO("Information", "info"),
        WARN("Warning", "warning"),
        ERROR("Error", "error");

        private final String displayName;
        private final String xmlValue;

        private Level(String displayName, String xmlValue) {
            this.displayName = displayName;
            this.xmlValue = xmlValue;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getXmlValue() {
            return this.xmlValue;
        }

        public static Level fromXmlValue(String xmlValue) {
            if (xmlValue == null) {
                return INFO;
            }
            return switch (xmlValue.toLowerCase(Locale.ROOT)) {
                case "warning", "warn" -> WARN;
                case "error", "err" -> ERROR;
                default -> INFO;
            };
        }

        public boolean isProblem() {
            return this != INFO;
        }

        public String toString() {
            return this.displayName;
        }
    }
}

