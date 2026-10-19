/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.dcm4che3.net.ConnectionMonitor;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DicomProtocolHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Listener;
import org.dcm4che3.net.TCPListener;
import org.dcm4che3.net.TCPProtocolHandler;
import org.dcm4che3.net.UDPListener;
import org.dcm4che3.net.UDPProtocolHandler;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection
implements Serializable {
    private static final long serialVersionUID = -7814748788035232055L;
    public static final Logger LOG = LoggerFactory.getLogger(Connection.class);
    public static final int NO_TIMEOUT = 0;
    public static final int SYNCHRONOUS_MODE = 1;
    public static final int NOT_LISTENING = -1;
    public static final int DEF_BACKLOG = 50;
    public static final int DEF_SOCKETDELAY = 50;
    public static final int DEF_ABORT_TIMEOUT = 1000;
    public static final int DEF_BUFFERSIZE = 0;
    public static final int DEF_MAX_PDU_LENGTH = 16378;
    public static final String TLS_RSA_WITH_NULL_SHA = "SSL_RSA_WITH_NULL_SHA";
    public static final String TLS_RSA_WITH_3DES_EDE_CBC_SHA = "SSL_RSA_WITH_3DES_EDE_CBC_SHA";
    public static final String TLS_RSA_WITH_AES_128_CBC_SHA = "TLS_RSA_WITH_AES_128_CBC_SHA";
    public static final String[] DEFAULT_TLS_PROTOCOLS = new String[]{"TLSv1.2"};
    private Device device;
    private String commonName;
    private String hostname;
    private String bindAddress;
    private String clientBindAddress;
    private String httpProxy;
    private int port = -1;
    private int backlog = 50;
    private int connectTimeout;
    private int requestTimeout;
    private int acceptTimeout;
    private int releaseTimeout;
    private int sendTimeout;
    private int storeTimeout;
    private int responseTimeout;
    private int retrieveTimeout;
    private boolean retrieveTimeoutTotal;
    private int idleTimeout;
    private int abortTimeout = 1000;
    private int socketCloseDelay = 50;
    private int sendBufferSize;
    private int receiveBufferSize;
    private int sendPDULength = 16378;
    private int receivePDULength = 16378;
    private int maxOpsPerformed = 1;
    private int maxOpsInvoked = 1;
    private boolean packPDV = true;
    private boolean tcpNoDelay = true;
    private boolean tlsNeedClientAuth = true;
    private String[] tlsCipherSuites = new String[0];
    private String[] tlsProtocols = DEFAULT_TLS_PROTOCOLS;
    private String[] blacklist = new String[0];
    private Boolean installed;
    private Protocol protocol = Protocol.DICOM;
    private EndpointIdentificationAlgorithm tlsEndpointIdentificationAlgorithm;
    private static final EnumMap<Protocol, TCPProtocolHandler> tcpHandlers = new EnumMap(Protocol.class);
    private static final EnumMap<Protocol, UDPProtocolHandler> udpHandlers = new EnumMap(Protocol.class);
    private transient List<InetAddress> blacklistAddrs;
    private transient InetAddress hostAddr;
    private transient InetAddress bindAddr;
    private transient InetAddress clientBindAddr;
    private volatile transient Listener listener;
    private transient boolean rebindNeeded;

    public Connection() {
    }

    public Connection(String commonName, String hostname) {
        this(commonName, hostname, -1);
    }

    public Connection(String commonName, String hostname, int port) {
        this.commonName = commonName;
        this.hostname = hostname;
        this.port = port;
    }

    public Connection(Connection from) {
        this.reconfigure(from);
    }

    public static TCPProtocolHandler registerTCPProtocolHandler(Protocol protocol, TCPProtocolHandler handler) {
        return tcpHandlers.put(protocol, handler);
    }

    public static TCPProtocolHandler unregisterTCPProtocolHandler(Protocol protocol) {
        return tcpHandlers.remove((Object)protocol);
    }

    public static UDPProtocolHandler registerUDPProtocolHandler(Protocol protocol, UDPProtocolHandler handler) {
        return udpHandlers.put(protocol, handler);
    }

    public static UDPProtocolHandler unregisterUDPProtocolHandler(Protocol protocol) {
        return udpHandlers.remove((Object)protocol);
    }

    public final Device getDevice() {
        return this.device;
    }

    final void setDevice(Device device) {
        if (device != null && this.device != null) {
            throw new IllegalStateException("already owned by " + device);
        }
        this.device = device;
    }

    public final String getHostname() {
        return this.hostname;
    }

    public final void setHostname(String hostname) {
        if (hostname != null ? hostname.equals(this.hostname) : this.hostname == null) {
            return;
        }
        this.hostname = hostname;
        this.needRebind();
    }

    public final String getBindAddress() {
        return this.bindAddress;
    }

    public final void setBindAddress(String bindAddress) {
        if (bindAddress != null ? bindAddress.equals(this.bindAddress) : this.bindAddress == null) {
            return;
        }
        this.bindAddress = bindAddress;
        this.bindAddr = null;
        this.needRebind();
    }

    public String getClientBindAddress() {
        return this.clientBindAddress;
    }

    public void setClientBindAddress(String bindAddress) {
        if (bindAddress != null ? bindAddress.equals(this.clientBindAddress) : this.clientBindAddress == null) {
            return;
        }
        this.clientBindAddress = bindAddress;
        this.clientBindAddr = null;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public void setProtocol(Protocol protocol) {
        if (protocol == null) {
            throw new NullPointerException();
        }
        if (this.protocol == protocol) {
            return;
        }
        this.protocol = protocol;
        this.needRebind();
    }

    public EndpointIdentificationAlgorithm getTlsEndpointIdentificationAlgorithm() {
        return this.tlsEndpointIdentificationAlgorithm;
    }

    public void setTlsEndpointIdentificationAlgorithm(EndpointIdentificationAlgorithm tlsEndpointIdentificationAlgorithm) {
        this.tlsEndpointIdentificationAlgorithm = tlsEndpointIdentificationAlgorithm;
    }

    boolean isRebindNeeded() {
        return this.rebindNeeded;
    }

    void needRebind() {
        this.rebindNeeded = true;
    }

    public final String getCommonName() {
        return this.commonName;
    }

    public final void setCommonName(String name) {
        this.commonName = name;
    }

    public final int getPort() {
        return this.port;
    }

    public final void setPort(int port) {
        if (this.port == port) {
            return;
        }
        if ((port <= 0 || port > 65535) && port != -1) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        this.port = port;
        this.needRebind();
    }

    public final String getHttpProxy() {
        return this.httpProxy;
    }

    public final void setHttpProxy(String proxy) {
        this.httpProxy = proxy;
    }

    public final boolean useHttpProxy() {
        return this.httpProxy != null;
    }

    public final boolean isServer() {
        return this.port > 0;
    }

    public final int getBacklog() {
        return this.backlog;
    }

    public final void setBacklog(int backlog) {
        if (this.backlog == backlog) {
            return;
        }
        if (backlog < 1) {
            throw new IllegalArgumentException("backlog: " + backlog);
        }
        this.backlog = backlog;
        this.needRebind();
    }

    public final int getConnectTimeout() {
        return this.connectTimeout;
    }

    public final void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.connectTimeout = timeout;
    }

    public final int getRequestTimeout() {
        return this.requestTimeout;
    }

    public final void setRequestTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.requestTimeout = timeout;
    }

    public final int getAcceptTimeout() {
        return this.acceptTimeout;
    }

    public final void setAcceptTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.acceptTimeout = timeout;
    }

    public final int getReleaseTimeout() {
        return this.releaseTimeout;
    }

    public final void setReleaseTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.releaseTimeout = timeout;
    }

    public int getAbortTimeout() {
        return this.abortTimeout;
    }

    public void setAbortTimeout(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay: " + delay);
        }
        this.abortTimeout = delay;
    }

    public final int getSocketCloseDelay() {
        return this.socketCloseDelay;
    }

    public final void setSocketCloseDelay(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay: " + delay);
        }
        this.socketCloseDelay = delay;
    }

    public int getSendTimeout() {
        return this.sendTimeout;
    }

    public void setSendTimeout(int timeout) {
        this.sendTimeout = timeout;
    }

    public int getStoreTimeout() {
        return this.storeTimeout;
    }

    public void setStoreTimeout(int timeout) {
        this.storeTimeout = timeout;
    }

    public final int getResponseTimeout() {
        return this.responseTimeout;
    }

    public final void setResponseTimeout(int timeout) {
        this.responseTimeout = timeout;
    }

    public final int getRetrieveTimeout() {
        return this.retrieveTimeout;
    }

    public final void setRetrieveTimeout(int timeout) {
        this.retrieveTimeout = timeout;
    }

    public final boolean isRetrieveTimeoutTotal() {
        return this.retrieveTimeoutTotal;
    }

    public final void setRetrieveTimeoutTotal(boolean total) {
        this.retrieveTimeoutTotal = total;
    }

    public final int getIdleTimeout() {
        return this.idleTimeout;
    }

    public final void setIdleTimeout(int timeout) {
        this.idleTimeout = timeout;
    }

    public String[] getTlsCipherSuites() {
        return this.tlsCipherSuites;
    }

    public void setTlsCipherSuites(String ... tlsCipherSuites) {
        if (Arrays.equals(this.tlsCipherSuites, tlsCipherSuites)) {
            return;
        }
        this.tlsCipherSuites = tlsCipherSuites;
        this.needRebind();
    }

    public final boolean isTls() {
        return this.tlsCipherSuites.length > 0;
    }

    public final String[] getTlsProtocols() {
        return this.tlsProtocols;
    }

    public final void setTlsProtocols(String ... tlsProtocols) {
        if (Arrays.equals(this.tlsProtocols, tlsProtocols)) {
            return;
        }
        this.tlsProtocols = tlsProtocols;
        this.needRebind();
    }

    public final boolean isTlsNeedClientAuth() {
        return this.tlsNeedClientAuth;
    }

    public final void setTlsNeedClientAuth(boolean tlsNeedClientAuth) {
        if (this.tlsNeedClientAuth == tlsNeedClientAuth) {
            return;
        }
        this.tlsNeedClientAuth = tlsNeedClientAuth;
        this.needRebind();
    }

    public final int getReceiveBufferSize() {
        return this.receiveBufferSize;
    }

    public final void setReceiveBufferSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size: " + size);
        }
        this.receiveBufferSize = size;
    }

    public final int getSendBufferSize() {
        return this.sendBufferSize;
    }

    public final void setSendBufferSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size: " + size);
        }
        this.sendBufferSize = size;
    }

    public final int getSendPDULength() {
        return this.sendPDULength;
    }

    public final void setSendPDULength(int sendPDULength) {
        this.sendPDULength = sendPDULength;
    }

    public final int getReceivePDULength() {
        return this.receivePDULength;
    }

    public final void setReceivePDULength(int receivePDULength) {
        this.receivePDULength = receivePDULength;
    }

    public final int getMaxOpsPerformed() {
        return this.maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public final int getMaxOpsInvoked() {
        return this.maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public final boolean isPackPDV() {
        return this.packPDV;
    }

    public final void setPackPDV(boolean packPDV) {
        this.packPDV = packPDV;
    }

    public final boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isInstalled() {
        return this.device != null && this.device.isInstalled() && (this.installed == null || this.installed != false);
    }

    public Boolean getInstalled() {
        return this.installed;
    }

    public void setInstalled(Boolean installed) {
        if (this.installed == installed) {
            return;
        }
        boolean prev = this.isInstalled();
        this.installed = installed;
        if (this.isInstalled() != prev) {
            this.needRebind();
        }
    }

    synchronized void rebind() throws IOException, GeneralSecurityException {
        this.unbind();
        this.bind();
    }

    public final String[] getBlacklist() {
        return this.blacklist;
    }

    public final void setBlacklist(String[] blacklist) {
        this.blacklist = blacklist;
        this.blacklistAddrs = null;
    }

    public String toString() {
        return this.promptTo(new StringBuilder(), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        StringUtils.appendLine(sb, indent, "Connection[cn: ", this.commonName);
        StringUtils.appendLine(sb, indent2, "host: ", this.hostname);
        StringUtils.appendLine(sb, indent2, "port: ", this.port);
        StringUtils.appendLine(sb, indent2, "ciphers: ", Arrays.toString(this.tlsCipherSuites));
        StringUtils.appendLine(sb, indent2, "installed: ", this.getInstalled());
        return sb.append(indent).append(']');
    }

    void setSocketSendOptions(Socket s) throws SocketException {
        int size = s.getSendBufferSize();
        if (this.sendBufferSize == 0) {
            this.sendBufferSize = size;
        } else if (this.sendBufferSize != size) {
            s.setSendBufferSize(this.sendBufferSize);
            this.sendBufferSize = s.getSendBufferSize();
        }
        if (s.getTcpNoDelay() != this.tcpNoDelay) {
            s.setTcpNoDelay(this.tcpNoDelay);
        }
    }

    private void setReceiveBufferSize(Socket s) throws SocketException {
        int size = s.getReceiveBufferSize();
        if (this.receiveBufferSize == 0) {
            this.receiveBufferSize = size;
        } else if (this.receiveBufferSize != size) {
            s.setReceiveBufferSize(this.receiveBufferSize);
            this.receiveBufferSize = s.getReceiveBufferSize();
        }
    }

    void setReceiveBufferSize(ServerSocket ss) throws SocketException {
        int size = ss.getReceiveBufferSize();
        if (this.receiveBufferSize == 0) {
            this.receiveBufferSize = size;
        } else if (this.receiveBufferSize != size) {
            ss.setReceiveBufferSize(this.receiveBufferSize);
            this.receiveBufferSize = ss.getReceiveBufferSize();
        }
    }

    public void setReceiveBufferSize(DatagramSocket ds) throws SocketException {
        int size = ds.getReceiveBufferSize();
        if (this.receiveBufferSize == 0) {
            this.receiveBufferSize = size;
        } else if (this.receiveBufferSize != size) {
            ds.setReceiveBufferSize(this.receiveBufferSize);
            this.receiveBufferSize = ds.getReceiveBufferSize();
        }
    }

    private InetAddress hostAddr() throws UnknownHostException {
        if (this.hostAddr == null && this.hostname != null) {
            this.hostAddr = InetAddress.getByName(this.hostname);
        }
        return this.hostAddr;
    }

    private InetAddress bindAddr() throws UnknownHostException {
        if (this.bindAddress == null) {
            return this.hostAddr();
        }
        if (this.bindAddr == null) {
            this.bindAddr = InetAddress.getByName(this.bindAddress);
        }
        return this.bindAddr;
    }

    private InetAddress clientBindAddr() throws UnknownHostException {
        if (this.clientBindAddress == null) {
            return this.hostAddr();
        }
        if (this.clientBindAddr == null) {
            this.clientBindAddr = InetAddress.getByName(this.clientBindAddress);
        }
        return this.clientBindAddr;
    }

    private List<InetAddress> blacklistAddrs() {
        if (this.blacklistAddrs == null) {
            this.blacklistAddrs = new ArrayList<InetAddress>(this.blacklist.length);
            for (String hostname : this.blacklist) {
                try {
                    this.blacklistAddrs.add(InetAddress.getByName(hostname));
                }
                catch (UnknownHostException e) {
                    LOG.warn("Failed to lookup InetAddress of " + hostname, (Throwable)e);
                }
            }
        }
        return this.blacklistAddrs;
    }

    public InetSocketAddress getEndPoint() throws UnknownHostException {
        return new InetSocketAddress(this.hostAddr(), this.port);
    }

    public InetSocketAddress getBindPoint() throws UnknownHostException {
        return new InetSocketAddress(this.bindAddr(), this.port);
    }

    public InetSocketAddress getClientBindPoint() throws UnknownHostException {
        return new InetSocketAddress(this.clientBindAddr(), 0);
    }

    private void checkInstalled() {
        if (!this.isInstalled()) {
            throw new IllegalStateException("Not installed");
        }
    }

    private void checkCompatible(Connection remoteConn) throws IncompatibleConnectionException {
        if (!this.isCompatible(remoteConn)) {
            throw new IncompatibleConnectionException(remoteConn.toString());
        }
    }

    public synchronized boolean bind() throws IOException, GeneralSecurityException {
        if (!this.isInstalled() || !this.isServer()) {
            this.rebindNeeded = false;
            return false;
        }
        if (this.device == null) {
            throw new IllegalStateException("Not attached to Device");
        }
        if (this.isListening()) {
            throw new IllegalStateException("Already listening - " + this.listener);
        }
        if (this.protocol.isTCP()) {
            TCPProtocolHandler handler = tcpHandlers.get((Object)this.protocol);
            if (handler == null) {
                LOG.info("No TCP Protocol Handler for protocol {}", (Object)this.protocol);
                return false;
            }
            this.listener = new TCPListener(this, handler);
        } else {
            UDPProtocolHandler handler = udpHandlers.get((Object)this.protocol);
            if (handler == null) {
                LOG.info("No UDP Protocol Handler for protocol {}", (Object)this.protocol);
                return false;
            }
            this.listener = new UDPListener(this, handler);
        }
        this.rebindNeeded = false;
        return true;
    }

    public final boolean isListening() {
        return this.listener != null;
    }

    public boolean isBlackListed(InetAddress ia) {
        return this.blacklistAddrs().contains(ia);
    }

    public synchronized void unbind() {
        Listener tmp = this.listener;
        if (tmp == null) {
            return;
        }
        this.listener = null;
        try {
            tmp.close();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public Socket connect(Connection remoteConn) throws IOException, IncompatibleConnectionException, GeneralSecurityException {
        this.checkInstalled();
        if (!this.protocol.isTCP()) {
            throw new IllegalStateException("Not a TCP Connection");
        }
        this.checkCompatible(remoteConn);
        InetSocketAddress bindPoint = this.getClientBindPoint();
        String remoteHostname = remoteConn.getHostname();
        int remotePort = remoteConn.getPort();
        LOG.info("Initiate connection from {} to {}:{}", new Object[]{bindPoint, remoteHostname, remotePort});
        Socket s = new Socket();
        ConnectionMonitor monitor = this.device != null ? this.device.getConnectionMonitor() : null;
        try {
            s.bind(bindPoint);
            this.setReceiveBufferSize(s);
            this.setSocketSendOptions(s);
            String remoteProxy = remoteConn.getHttpProxy();
            if (remoteProxy != null) {
                String userauth = null;
                String[] ss = StringUtils.split(remoteProxy, '@');
                if (ss.length > 1) {
                    userauth = ss[0];
                    remoteProxy = ss[1];
                }
                int proxyPort = (ss = StringUtils.split(remoteProxy, ':')).length > 1 ? Integer.parseInt(ss[1]) : 8080;
                s.connect(new InetSocketAddress(ss[0], proxyPort), this.connectTimeout);
                try {
                    this.doProxyHandshake(s, remoteHostname, remotePort, userauth, this.connectTimeout);
                }
                catch (IOException e) {
                    SafeClose.close(s);
                    throw e;
                }
            } else {
                s.connect(remoteConn.getEndPoint(), this.connectTimeout);
            }
            if (this.isTls()) {
                s = this.createTLSSocket(s, remoteConn);
            }
            if (monitor != null) {
                monitor.onConnectionEstablished(this, remoteConn, s);
            }
            LOG.info("Established connection {}", (Object)s);
            return s;
        }
        catch (GeneralSecurityException e) {
            if (monitor != null) {
                monitor.onConnectionFailed(this, remoteConn, s, e);
            }
            SafeClose.close(s);
            throw e;
        }
        catch (IOException e) {
            if (monitor != null) {
                monitor.onConnectionFailed(this, remoteConn, s, e);
            }
            SafeClose.close(s);
            throw e;
        }
    }

    public DatagramSocket createDatagramSocket() throws IOException {
        this.checkInstalled();
        if (this.protocol.isTCP()) {
            throw new IllegalStateException("Not a UDP Connection");
        }
        DatagramSocket ds = new DatagramSocket(this.getClientBindPoint());
        int size = ds.getSendBufferSize();
        if (this.sendBufferSize == 0) {
            this.sendBufferSize = size;
        } else if (this.sendBufferSize != size) {
            ds.setSendBufferSize(this.sendBufferSize);
            this.sendBufferSize = ds.getSendBufferSize();
        }
        return ds;
    }

    public Listener getListener() {
        return this.listener;
    }

    private void doProxyHandshake(Socket s, String hostname, int port, String userauth, int connectTimeout) throws IOException {
        StringBuilder request = new StringBuilder(128);
        request.append("CONNECT ").append(hostname).append(':').append(port).append(" HTTP/1.1\r\nHost: ").append(hostname).append(':').append(port);
        if (userauth != null) {
            byte[] b = userauth.getBytes("UTF-8");
            char[] base64 = new char[(b.length + 2) / 3 * 4];
            Base64.encode(b, 0, b.length, base64, 0);
            request.append("\r\nProxy-Authorization: basic ").append(base64);
        }
        request.append("\r\n\r\n");
        OutputStream out = s.getOutputStream();
        out.write(request.toString().getBytes("US-ASCII"));
        out.flush();
        s.setSoTimeout(connectTimeout);
        String response = new HTTPResponse(s).toString();
        s.setSoTimeout(0);
        if (!response.startsWith("HTTP/1.1 2")) {
            throw new IOException("Unable to tunnel through " + s + ". Proxy returns \"" + response + '\"');
        }
    }

    private SSLSocket createTLSSocket(Socket s, Connection remoteConn) throws GeneralSecurityException, IOException {
        SSLContext sslContext = this.device.sslContext();
        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket ssl = (SSLSocket)sf.createSocket(s, remoteConn.getHostname(), remoteConn.getPort(), true);
        ssl.setEnabledProtocols(Connection.intersect(remoteConn.getTlsProtocols(), this.getTlsProtocols()));
        ssl.setEnabledCipherSuites(Connection.intersect(remoteConn.getTlsCipherSuites(), this.getTlsCipherSuites()));
        if (this.tlsEndpointIdentificationAlgorithm != null) {
            SSLParameters parameters = ssl.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm(this.tlsEndpointIdentificationAlgorithm.name());
            ssl.setSSLParameters(parameters);
        }
        ssl.startHandshake();
        return ssl;
    }

    public void close(Socket s) {
        LOG.info("Close connection {}", (Object)s);
        SafeClose.close(s);
    }

    public boolean isCompatible(Connection remoteConn) {
        if (remoteConn.protocol != this.protocol) {
            return false;
        }
        if (!this.protocol.isTCP()) {
            return true;
        }
        if (!this.isTls()) {
            return !remoteConn.isTls();
        }
        return this.hasCommon(remoteConn.getTlsProtocols(), this.getTlsProtocols()) && this.hasCommon(remoteConn.tlsCipherSuites, this.tlsCipherSuites);
    }

    private boolean hasCommon(String[] ss1, String[] ss2) {
        for (String s1 : ss1) {
            for (String s2 : ss2) {
                if (!s1.equals(s2)) continue;
                return true;
            }
        }
        return false;
    }

    private static String[] intersect(String[] ss1, String[] ss2) {
        String[] ss = new String[Math.min(ss1.length, ss2.length)];
        int len = 0;
        block0: for (String s1 : ss1) {
            for (String s2 : ss2) {
                if (!s1.equals(s2)) continue;
                ss[len++] = s1;
                continue block0;
            }
        }
        if (len == ss.length) {
            return ss;
        }
        String[] dest = new String[len];
        System.arraycopy(ss, 0, dest, 0, len);
        return dest;
    }

    boolean equalsRDN(Connection other) {
        return this.commonName != null ? this.commonName.equals(other.commonName) : other.commonName == null && this.hostname.equals(other.hostname) && this.port == other.port && this.protocol == other.protocol;
    }

    void reconfigure(Connection from) {
        this.setCommonName(from.commonName);
        this.setHostname(from.hostname);
        this.setPort(from.port);
        this.setBindAddress(from.bindAddress);
        this.setClientBindAddress(from.clientBindAddress);
        this.setProtocol(from.protocol);
        this.setHttpProxy(from.httpProxy);
        this.setBacklog(from.backlog);
        this.setConnectTimeout(from.connectTimeout);
        this.setRequestTimeout(from.requestTimeout);
        this.setAcceptTimeout(from.acceptTimeout);
        this.setReleaseTimeout(from.releaseTimeout);
        this.setSendTimeout(from.sendTimeout);
        this.setStoreTimeout(from.storeTimeout);
        this.setResponseTimeout(from.responseTimeout);
        this.setRetrieveTimeout(from.retrieveTimeout);
        this.setIdleTimeout(from.idleTimeout);
        this.setAbortTimeout(from.abortTimeout);
        this.setSocketCloseDelay(from.socketCloseDelay);
        this.setSendBufferSize(from.sendBufferSize);
        this.setReceiveBufferSize(from.receiveBufferSize);
        this.setSendPDULength(from.sendPDULength);
        this.setReceivePDULength(from.receivePDULength);
        this.setMaxOpsPerformed(from.maxOpsPerformed);
        this.setMaxOpsInvoked(from.maxOpsInvoked);
        this.setPackPDV(from.packPDV);
        this.setTcpNoDelay(from.tcpNoDelay);
        this.setTlsNeedClientAuth(from.tlsNeedClientAuth);
        this.setTlsCipherSuites(from.tlsCipherSuites);
        this.setTlsProtocols(from.tlsProtocols);
        this.setTlsEndpointIdentificationAlgorithm(from.tlsEndpointIdentificationAlgorithm);
        this.setBlacklist(from.blacklist);
        this.setInstalled(from.installed);
    }

    static {
        Connection.registerTCPProtocolHandler(Protocol.DICOM, DicomProtocolHandler.INSTANCE);
    }

    public static enum Protocol {
        DICOM,
        HL7,
        HL7_MLLP2,
        SYSLOG_TLS,
        SYSLOG_UDP,
        HTTP;


        public boolean isTCP() {
            return this != SYSLOG_UDP;
        }

        public boolean isHL7() {
            return this == HL7 || this == HL7_MLLP2;
        }

        public boolean isSyslog() {
            return this == SYSLOG_TLS || this == SYSLOG_UDP;
        }
    }

    public static enum EndpointIdentificationAlgorithm {
        HTTPS,
        LDAPS;

    }

    private static class HTTPResponse
    extends ByteArrayOutputStream {
        private final String rsp;

        public HTTPResponse(Socket s) throws IOException {
            super(64);
            int b;
            InputStream in = s.getInputStream();
            boolean eol = false;
            while ((b = in.read()) != -1) {
                this.write(b);
                if (b == 10) {
                    if (eol) {
                        this.rsp = new String(this.buf, 0, this.count, "US-ASCII");
                        return;
                    }
                    eol = true;
                    continue;
                }
                if (b == 13) continue;
                eol = false;
            }
            throw new IOException("Unexpected EOF from " + s);
        }

        @Override
        public String toString() {
            return this.rsp;
        }
    }
}

