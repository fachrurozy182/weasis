/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.api.net.auth;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.net.auth.AcceptCallbackHandler;

public final class AsyncCallbackServerHandler
implements Runnable,
AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncCallbackServerHandler.class);
    private final AtomicBoolean stopped = new AtomicBoolean(true);
    private final AcceptCallbackHandler responseHandler;
    private volatile AsynchronousServerSocketChannel socketChannel;
    private final int port;
    private final ExecutorService executor;
    private volatile CountDownLatch latch;

    public AsyncCallbackServerHandler(int port, AcceptCallbackHandler responseHandler) {
        this.port = port;
        this.responseHandler = responseHandler;
        this.latch = new CountDownLatch(1);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void run() {
        this.stopped.set(false);
        try {
            this.socketChannel.accept(this, this.responseHandler);
            this.latch.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.debug("Server handler interrupted", (Throwable)e);
        }
        finally {
            this.stopped.set(true);
        }
    }

    public synchronized void start() throws IOException {
        if (this.isStopped()) {
            this.latch = new CountDownLatch(1);
            this.socketChannel = this.openChannel();
            LOGGER.info("Async callback server initialized on port {}", (Object)this.port);
            this.executor.submit(this);
        }
    }

    private AsynchronousServerSocketChannel openChannel() throws IOException {
        AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open();
        try {
            return channel.bind(new InetSocketAddress(this.port));
        }
        catch (IOException e) {
            channel.close();
            throw e;
        }
    }

    public boolean isStopped() {
        return this.stopped.get();
    }

    @Override
    public void close() {
        try {
            if (this.socketChannel != null && this.socketChannel.isOpen()) {
                this.socketChannel.close();
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to close async callback server socket", (Throwable)e);
        }
        this.latch.countDown();
        this.executor.close();
        LOGGER.info("Async callback server shutdown completed");
    }

    AsynchronousServerSocketChannel getSocketChannel() {
        return this.socketChannel;
    }

    public AcceptCallbackHandler getResponseHandler() {
        return this.responseHandler;
    }

    public int getPort() {
        return this.port;
    }
}

