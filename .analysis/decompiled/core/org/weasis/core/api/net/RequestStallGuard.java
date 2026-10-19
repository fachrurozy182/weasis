/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.net;

import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.weasis.core.api.net.StallTimeoutException;
import org.weasis.core.api.net.StallWatchdog;

public final class RequestStallGuard {
    private final int stallTimeoutMillis;
    private final long stallNanos;
    private final AtomicLong lastProgressNanos = new AtomicLong(System.nanoTime());

    public RequestStallGuard(int stallTimeoutMillis) {
        this.stallTimeoutMillis = stallTimeoutMillis;
        this.stallNanos = TimeUnit.MILLISECONDS.toNanos(Math.max(0, stallTimeoutMillis));
    }

    public HttpRequest.BodyPublisher track(HttpRequest.BodyPublisher delegate) {
        return this.stallTimeoutMillis > 0 ? new ProgressPublisher(this, delegate) : delegate;
    }

    public <T> CompletableFuture<T> guard(CompletableFuture<T> future) {
        if (this.stallTimeoutMillis <= 0) {
            return future;
        }
        this.lastProgressNanos.set(System.nanoTime());
        CompletableFuture guarded = new CompletableFuture();
        long poll = StallWatchdog.pollIntervalMillis(this.stallTimeoutMillis);
        ScheduledFuture<?> watchdog = StallWatchdog.EXECUTOR.scheduleWithFixedDelay(() -> this.abortIfStalled(guarded), poll, poll, TimeUnit.MILLISECONDS);
        future.whenComplete((value, error) -> {
            watchdog.cancel(false);
            if (error == null) {
                guarded.complete(value);
            } else {
                guarded.completeExceptionally((Throwable)error);
            }
        });
        guarded.whenComplete((value, error) -> {
            watchdog.cancel(false);
            if (error instanceof StallTimeoutException) {
                future.cancel(true);
            }
        });
        return guarded;
    }

    private void abortIfStalled(CompletableFuture<?> guarded) {
        if (System.nanoTime() - this.lastProgressNanos.get() >= this.stallNanos) {
            guarded.completeExceptionally(new StallTimeoutException("Request made no progress for " + this.stallTimeoutMillis + " ms"));
        }
    }

    private final class ProgressPublisher
    implements HttpRequest.BodyPublisher {
        private final HttpRequest.BodyPublisher delegate;
        final /* synthetic */ RequestStallGuard this$0;

        ProgressPublisher(RequestStallGuard requestStallGuard, HttpRequest.BodyPublisher delegate) {
            RequestStallGuard requestStallGuard2 = requestStallGuard;
            Objects.requireNonNull(requestStallGuard2);
            this.this$0 = requestStallGuard2;
            this.delegate = delegate;
        }

        @Override
        public long contentLength() {
            return this.delegate.contentLength();
        }

        @Override
        public void subscribe(final Flow.Subscriber<? super ByteBuffer> subscriber) {
            this.delegate.subscribe(new Flow.Subscriber<ByteBuffer>(this){
                final /* synthetic */ ProgressPublisher this$1;
                {
                    ProgressPublisher progressPublisher = this$1;
                    Objects.requireNonNull(progressPublisher);
                    this.this$1 = progressPublisher;
                }

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.this$1.this$0.lastProgressNanos.set(System.nanoTime());
                    subscriber.onSubscribe(subscription);
                }

                @Override
                public void onNext(ByteBuffer item) {
                    this.this$1.this$0.lastProgressNanos.set(System.nanoTime());
                    subscriber.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    this.this$1.this$0.lastProgressNanos.set(System.nanoTime());
                    subscriber.onComplete();
                }
            });
        }
    }
}

