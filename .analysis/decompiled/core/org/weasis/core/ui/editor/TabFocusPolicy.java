/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  java.lang.runtime.SwitchBootstraps
 */
package org.weasis.core.ui.editor;

import java.lang.runtime.SwitchBootstraps;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public sealed interface TabFocusPolicy {
    public static final Duration DEFAULT_AUTO_THRESHOLD = Duration.ofSeconds(3L);

    public static Foreground foreground() {
        return new Foreground();
    }

    public static Background background() {
        return new Background();
    }

    public static AutoByDuration autoByDuration() {
        return new AutoByDuration();
    }

    public static AutoByDuration autoByDuration(Duration threshold) {
        return new AutoByDuration(threshold);
    }

    public static AutoByDuration autoByDuration(Duration threshold, Instant loadStartedAt) {
        return new AutoByDuration(threshold, loadStartedAt);
    }

    default public Instant startLoadingTimestamp() {
        TabFocusPolicy tabFocusPolicy = this;
        if (tabFocusPolicy instanceof AutoByDuration) {
            AutoByDuration auto = (AutoByDuration)tabFocusPolicy;
            return auto.loadStartedAt();
        }
        return null;
    }

    default public boolean shouldBringToFront() {
        TabFocusPolicy tabFocusPolicy = this;
        Objects.requireNonNull(tabFocusPolicy);
        TabFocusPolicy tabFocusPolicy2 = tabFocusPolicy;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Foreground.class, Background.class, AutoByDuration.class}, (TabFocusPolicy)tabFocusPolicy2, (int)n)) {
            default -> throw new MatchException(null, null);
            case 0 -> true;
            case 1 -> false;
            case 2 -> {
                AutoByDuration auto = (AutoByDuration)tabFocusPolicy2;
                Duration elapsed = Duration.between(auto.loadStartedAt(), Instant.now());
                if (elapsed.compareTo(auto.threshold()) <= 0) {
                    yield true;
                }
                yield false;
            }
        };
    }

    public record Foreground() implements TabFocusPolicy
    {
    }

    public record Background() implements TabFocusPolicy
    {
    }

    public record AutoByDuration(Duration threshold, Instant loadStartedAt) implements TabFocusPolicy
    {
        public AutoByDuration() {
            this(DEFAULT_AUTO_THRESHOLD, Instant.now());
        }

        public AutoByDuration(Duration threshold) {
            this(threshold, Instant.now());
        }

        public AutoByDuration {
            if (threshold == null) {
                threshold = DEFAULT_AUTO_THRESHOLD;
            }
            if (loadStartedAt == null) {
                loadStartedAt = Instant.now();
            }
        }
    }
}

