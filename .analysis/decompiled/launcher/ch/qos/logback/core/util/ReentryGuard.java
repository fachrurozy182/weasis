/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

public interface ReentryGuard {
    public boolean isLocked();

    public void lock();

    public void unlock();

    public static class NOPRentryGuard
    implements ReentryGuard {
        @Override
        public boolean isLocked() {
            return false;
        }

        @Override
        public void lock() {
        }

        @Override
        public void unlock() {
        }
    }

    public static class ReentryGuardImpl
    implements ReentryGuard {
        private ThreadLocal<Boolean> guard = new ThreadLocal();

        @Override
        public boolean isLocked() {
            return Boolean.TRUE.equals(this.guard.get());
        }

        @Override
        public void lock() {
            this.guard.set(Boolean.TRUE);
        }

        @Override
        public void unlock() {
            this.guard.set(Boolean.FALSE);
        }
    }
}

