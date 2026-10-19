/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.net;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SimpleSocketServer;
import ch.qos.logback.classic.net.server.HardenedLoggingEventInputStream;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class SocketNode
implements Runnable {
    Socket socket;
    LoggerContext loggerContext;
    HardenedLoggingEventInputStream hardenedLoggingEventInputStream;
    SocketAddress remoteSocketAddress;
    Logger logger;
    boolean closed = false;
    SimpleSocketServer socketServer;

    public SocketNode(SimpleSocketServer socketServer, Socket socket, LoggerContext context) {
        this.socketServer = socketServer;
        this.socket = socket;
        this.remoteSocketAddress = socket.getRemoteSocketAddress();
        this.loggerContext = context;
        this.logger = context.getLogger(SocketNode.class);
    }

    @Override
    public void run() {
        try {
            this.hardenedLoggingEventInputStream = new HardenedLoggingEventInputStream(this.loggerContext, new BufferedInputStream(this.socket.getInputStream()));
        }
        catch (Exception e) {
            this.logger.error("Could not open ObjectInputStream to " + String.valueOf(this.socket), e);
            this.closed = true;
        }
        try {
            while (!this.closed) {
                ILoggingEvent event = (ILoggingEvent)this.hardenedLoggingEventInputStream.readObject();
                Logger remoteLogger = this.loggerContext.getLogger(event.getLoggerName());
                if (!remoteLogger.isEnabledFor(event.getLevel())) continue;
                remoteLogger.callAppenders(event);
            }
        }
        catch (EOFException e) {
            this.logger.info("Caught java.io.EOFException closing connection.");
        }
        catch (SocketException e) {
            this.logger.info("Caught java.net.SocketException closing connection.");
        }
        catch (IOException e) {
            this.logger.info("Caught java.io.IOException: " + String.valueOf(e));
            this.logger.info("Closing connection.");
        }
        catch (Exception e) {
            this.logger.error("Unexpected exception. Closing connection.", e);
        }
        this.socketServer.socketNodeClosing(this);
        this.close();
    }

    void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.hardenedLoggingEventInputStream != null) {
            try {
                this.hardenedLoggingEventInputStream.close();
            }
            catch (IOException e) {
                this.logger.warn("Could not close connection.", e);
            }
            finally {
                this.hardenedLoggingEventInputStream = null;
            }
        }
    }

    public String toString() {
        return this.getClass().getName() + this.remoteSocketAddress.toString();
    }
}

