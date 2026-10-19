/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class SafeClose {
    public static void close(Closeable io) {
        if (io != null) {
            try {
                io.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public static void close(Socket sock) {
        if (sock != null) {
            try {
                sock.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }
}

