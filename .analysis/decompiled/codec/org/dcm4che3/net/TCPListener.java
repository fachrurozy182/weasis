/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.ConnectionMonitor;
import org.dcm4che3.net.Listener;
import org.dcm4che3.net.TCPProtocolHandler;

class TCPListener
implements Listener {
    private final Connection conn;
    private final TCPProtocolHandler handler;
    private final ServerSocket ss;

    public TCPListener(Connection conn, TCPProtocolHandler handler) throws IOException, GeneralSecurityException {
        try {
            this.conn = conn;
            this.handler = handler;
            this.ss = conn.isTls() ? this.createTLSServerSocket(conn) : new ServerSocket();
            conn.setReceiveBufferSize(this.ss);
            this.ss.bind(conn.getBindPoint(), conn.getBacklog());
            conn.getDevice().execute(new Runnable(){

                @Override
                public void run() {
                    TCPListener.this.listen();
                }
            });
        }
        catch (IOException e) {
            throw new IOException("Unable to start TCPListener on " + conn.getHostname() + ":" + conn.getPort(), e);
        }
    }

    private ServerSocket createTLSServerSocket(Connection conn) throws IOException, GeneralSecurityException {
        SSLContext sslContext = conn.getDevice().sslContext();
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket)ssf.createServerSocket();
        ss.setEnabledProtocols(conn.getTlsProtocols());
        ss.setEnabledCipherSuites(conn.getTlsCipherSuites());
        ss.setNeedClientAuth(conn.isTlsNeedClientAuth());
        return ss;
    }

    private void listen() {
        SocketAddress sockAddr = this.ss.getLocalSocketAddress();
        Connection.LOG.info("Start TCP Listener on {}", (Object)sockAddr);
        block6: while (true) {
            try {
                while (!this.ss.isClosed()) {
                    ConnectionMonitor monitor;
                    Connection.LOG.debug("Wait for connection on {}", (Object)sockAddr);
                    Socket s = this.ss.accept();
                    ConnectionMonitor connectionMonitor = monitor = this.conn.getDevice() != null ? this.conn.getDevice().getConnectionMonitor() : null;
                    if (this.conn.isBlackListed(s.getInetAddress())) {
                        if (monitor != null) {
                            monitor.onConnectionRejectedBlacklisted(this.conn, s);
                        }
                        Connection.LOG.info("Reject blacklisted connection {}", (Object)s);
                        this.conn.close(s);
                        continue;
                    }
                    try {
                        this.conn.setSocketSendOptions(s);
                    }
                    catch (Throwable e) {
                        if (monitor != null) {
                            monitor.onConnectionRejected(this.conn, s, e);
                        }
                        Connection.LOG.warn("Reject connection {}:", (Object)s, (Object)e);
                        this.conn.close(s);
                        continue;
                    }
                    if (monitor != null) {
                        monitor.onConnectionAccepted(this.conn, s);
                    }
                    Connection.LOG.info("Accept connection {}", (Object)s);
                    try {
                        this.handler.onAccept(this.conn, s);
                        continue block6;
                    }
                    catch (Throwable e) {
                        Connection.LOG.warn("Exception on accepted connection {}:", (Object)s, (Object)e);
                        this.conn.close(s);
                    }
                }
                break;
            }
            catch (Throwable e) {
                if (this.ss.isClosed()) break;
                Connection.LOG.error("Exception on listing on {}:", (Object)sockAddr, (Object)e);
                break;
            }
        }
        Connection.LOG.info("Stop TCP Listener on {}", (Object)sockAddr);
    }

    @Override
    public SocketAddress getEndPoint() {
        return this.ss.getLocalSocketAddress();
    }

    @Override
    public void close() throws IOException {
        try {
            this.ss.close();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

