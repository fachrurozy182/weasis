/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Listener;
import org.dcm4che3.net.UDPProtocolHandler;

class UDPListener
implements Listener {
    private static final int MAX_PACKAGE_LEN = 65536;
    private final Connection conn;
    private final UDPProtocolHandler handler;
    private final DatagramSocket ds;

    public UDPListener(Connection conn, UDPProtocolHandler handler) throws IOException, GeneralSecurityException {
        this.conn = conn;
        this.handler = handler;
        try {
            this.ds = new DatagramSocket(conn.getBindPoint());
        }
        catch (BindException e) {
            throw new IOException("Cannot start UDP listener on " + conn.getBindPoint().getHostName() + ":" + conn.getBindPoint().getPort(), e);
        }
        conn.setReceiveBufferSize(this.ds);
        conn.getDevice().execute(new Runnable(){

            @Override
            public void run() {
                UDPListener.this.listen();
            }
        });
    }

    private void listen() {
        SocketAddress sockAddr = this.ds.getLocalSocketAddress();
        Connection.LOG.info("Start UDP listener on {}", (Object)sockAddr);
        byte[] data = new byte[65536];
        block4: while (true) {
            try {
                while (!this.ds.isClosed()) {
                    Connection.LOG.debug("Wait for UDP datagram package on {}", (Object)sockAddr);
                    DatagramPacket dp = new DatagramPacket(data, 65536);
                    this.ds.receive(dp);
                    InetAddress senderAddr = dp.getAddress();
                    if (this.conn.isBlackListed(dp.getAddress())) {
                        Connection.LOG.info("Ignore UDP datagram package received from blacklisted {}", (Object)senderAddr);
                        continue;
                    }
                    Connection.LOG.info("Received UDP datagram package from {}", (Object)senderAddr);
                    try {
                        this.handler.onReceive(this.conn, dp);
                        continue block4;
                    }
                    catch (Throwable e) {
                        Connection.LOG.warn("Exception processing UDP received from {}:", (Object)senderAddr, (Object)e);
                    }
                }
                break;
            }
            catch (Throwable e) {
                if (this.ds.isClosed()) break;
                Connection.LOG.error("Exception on listing on {}:", (Object)sockAddr, (Object)e);
                break;
            }
        }
        Connection.LOG.info("Stop UDP listener on {}", (Object)sockAddr);
    }

    @Override
    public SocketAddress getEndPoint() {
        return this.ds.getLocalSocketAddress();
    }

    @Override
    public void close() throws IOException {
        try {
            this.ds.close();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

