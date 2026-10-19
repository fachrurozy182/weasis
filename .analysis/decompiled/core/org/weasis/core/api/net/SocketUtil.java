/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.SecureRandom;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.net.ServerSocketFactory;

public final class SocketUtil {
    public static final int PORT_MIN = 1024;
    public static final int PORT_MAX = 65535;
    private static final int PORT_RANGE = 64512;
    private static final int EXTRA_ATTEMPTS = 500;
    private static final String LOCALHOST = "localhost";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SocketUtil() {
    }

    public static boolean isPortAvailable(int port) {
        boolean bl;
        block8: {
            ServerSocket serverSocket = SocketUtil.createServerSocket(port);
            try {
                bl = true;
                if (serverSocket == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException | IllegalArgumentException ex) {
                    return false;
                }
            }
            serverSocket.close();
        }
        return bl;
    }

    public static SortedSet<Integer> findAvailablePorts(int numberOfPorts) {
        if (numberOfPorts <= 0) {
            throw new IllegalArgumentException("Number of ports must be positive: " + numberOfPorts);
        }
        TreeSet<Integer> availablePorts = new TreeSet<Integer>();
        int maxAttempts = numberOfPorts + 500;
        for (int attempt = 0; attempt < maxAttempts && availablePorts.size() < numberOfPorts; ++attempt) {
            availablePorts.add(SocketUtil.findAvailablePort());
        }
        if (availablePorts.size() != numberOfPorts) {
            throw new IllegalStateException("Could not find %d TCP ports available in the range [%d, %d]".formatted(new Object[]{numberOfPorts, 1024, 65535}));
        }
        return availablePorts;
    }

    public static int findAvailablePort() {
        for (int attempt = 0; attempt < 64512; ++attempt) {
            int port = 1024 + RANDOM.nextInt(64512);
            if (!SocketUtil.isPortAvailable(port)) continue;
            return port;
        }
        throw new IllegalStateException("Could not find an available TCP port in the range [%d, %d] after %d attempts".formatted(new Object[]{1024, 65535, 64512}));
    }

    private static ServerSocket createServerSocket(int port) throws IOException {
        return ServerSocketFactory.getDefault().createServerSocket(port, 1, InetAddress.getByName(LOCALHOST));
    }
}

