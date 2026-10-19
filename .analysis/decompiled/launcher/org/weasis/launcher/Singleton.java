/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.FileUtil;
import org.weasis.launcher.Utils;
import org.weasis.pref.ConfigData;

public class Singleton {
    private static final Logger LOGGER = LoggerFactory.getLogger(Singleton.class);
    private static final String LOCALHOST = "127.0.0.1";
    private static final File SI_FILEDIR = new File(System.getProperty("user.home") + File.separator + ".weasis", "singleton");
    private static final String SI_MAGICWORD = "si.init";
    private static final String SI_ARG = "si.arg";
    private static final String SI_PROP = "si.prop";
    private static final String SI_ACK = "si.ack";
    private static final String SI_EXIT = "si.exit";
    private static final String SI_STOP = "si.stop";
    private static final String SI_EOF = "si.EOF";
    private static final int ENCODING_PLATFORM = 1;
    private static final int ENCODING_UNICODE = 2;
    private static final String ENCODING_UNICODE_NAME = "UTF-16LE";
    private static int currPort;
    private static String stringId;
    private static String randomNumberString;
    private static SingletonServer siServer;
    private static SingletonApp siApp;
    private static final SecureRandom random;
    private static volatile boolean serverStarted;
    private static int randomNumber;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void start(SingletonApp sia, String id) {
        Objects.requireNonNull(sia);
        Objects.requireNonNull(id);
        Class<Singleton> clazz = Singleton.class;
        synchronized (Singleton.class) {
            if (!serverStarted) {
                try {
                    siServer = new SingletonServer(id);
                    siServer.start();
                }
                catch (Exception e) {
                    LOGGER.error("Create a Singleton server", e);
                    // ** MonitorExit[var2_2] (shouldn't be in output)
                    return;
                }
                siApp = sia;
                serverStarted = true;
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void stop() {
        if (siApp == null) {
            LOGGER.warn("Singleton.stop() called when not running, id: {}", (Object)stringId);
            return;
        }
        Class<Singleton> clazz = Singleton.class;
        synchronized (Singleton.class) {
            siApp = null;
            try (Socket socket = new Socket(LOCALHOST, siServer.getPort());
                 OutputStream os = socket.getOutputStream();
                 PrintStream out = new PrintStream(os, true, Charset.defaultCharset());){
                byte[] encoding = new byte[]{1};
                os.write(encoding);
                out.println(randomNumber);
                out.println(SI_STOP);
                out.flush();
                serverStarted = false;
            }
            catch (IOException e) {
                LOGGER.error("Stopping Singleton server", e);
            }
            Singleton.siServer.runnable.removeSiFile();
            // ** MonitorExit[var0] (shouldn't be in output)
            return;
        }
    }

    public static boolean running(String id) {
        LOGGER.debug("Check if another instance running for id: {}", (Object)id);
        String[] fList = SI_FILEDIR.list();
        if (fList != null) {
            for (String file : fList) {
                if (!file.startsWith(id)) continue;
                try {
                    currPort = Integer.parseInt(file.substring(file.lastIndexOf(95) + 1));
                }
                catch (NumberFormatException nfe) {
                    LOGGER.error("Cannot parse port", nfe);
                    return false;
                }
                File siFile = new File(SI_FILEDIR, file);
                try (BufferedReader br = new BufferedReader(new FileReader(siFile, StandardCharsets.UTF_8));){
                    randomNumberString = br.readLine();
                }
                catch (IOException ioe) {
                    LOGGER.error("Cannot read random numbrer from file {}", (Object)siFile.getPath());
                }
                stringId = id;
                LOGGER.info("Singleton server {} already running on port {}", (Object)stringId, (Object)currPort);
                return true;
            }
        }
        return false;
    }

    public static boolean invoke(ConfigData configData) {
        return Singleton.running(configData.getSourceID()) && Singleton.connectToServer(configData);
    }

    private static void printProperty(PrintStream out, String key, Properties p) {
        out.printf("%s=%s%n", key, p.getProperty(key, ""));
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static boolean connectToServer(ConfigData configData) {
        LOGGER.info("Connect to {} on port {}", (Object)stringId, (Object)currPort);
        if (randomNumberString == null) {
            LOGGER.error("MAGIC number is null, cannot connect.");
            return false;
        }
        String encodingName = Charset.defaultCharset().name();
        try (Socket socket = new Socket(LOCALHOST, currPort);
             OutputStream os = socket.getOutputStream();
             PrintStream out = new PrintStream(socket.getOutputStream(), true, encodingName);
             InputStreamReader isr = new InputStreamReader(socket.getInputStream(), encodingName);
             BufferedReader br = new BufferedReader(isr);){
            byte[] encoding = new byte[]{1};
            os.write(encoding);
            out.println(randomNumberString);
            out.println(SI_MAGICWORD);
            out.println(SI_ARG);
            for (String arg : configData.getArguments()) {
                out.println(arg);
            }
            out.println(SI_PROP);
            Properties p = configData.getProperties();
            Singleton.printProperty(out, "weasis.user", p);
            Singleton.printProperty(out, "weasis.config.hash", p);
            out.println(SI_EOF);
            out.flush();
            LOGGER.debug("Waiting for ACK");
            int tries = 5;
            for (int i = 0; i < 5; ++i) {
                String str = br.readLine();
                if (SI_ACK.equals(str)) {
                    LOGGER.debug("Got ACK");
                    boolean bl = true;
                    return bl;
                }
                if (!SI_EXIT.equals(str)) continue;
                File file = Singleton.getSiFile(stringId, currPort);
                Singleton.waitDeletedFile(file);
                boolean bl = false;
                return bl;
            }
            LOGGER.error("No ACK from server");
            return false;
        }
        catch (SocketException se) {
            LOGGER.error("No server is running", se);
            return false;
        }
        catch (Exception ioe) {
            LOGGER.error("Cannot connect to server", ioe);
        }
        return false;
    }

    private static void waitDeletedFile(File file) {
        int loop = 0;
        boolean runLoop = true;
        while (runLoop) {
            LOGGER.info("Wait 100 ms to start a the new instance once the previous has stopped");
            try {
                if (!file.exists()) break;
                TimeUnit.MILLISECONDS.sleep(100L);
                if (++loop <= 300) continue;
                LOGGER.error("The pid of the singleton still exists. Try to start the new instance.");
                runLoop = false;
            }
            catch (InterruptedException e) {
                runLoop = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    private static File getSiFile(String id, int port) {
        return new File(SI_FILEDIR, id + "_" + port);
    }

    static {
        stringId = null;
        randomNumberString = null;
        siApp = null;
        random = new SecureRandom();
        serverStarted = false;
    }

    private static class SingletonServer {
        private final SingletonServerRunnable runnable;
        private final Thread thread;

        SingletonServer(SingletonServerRunnable runnable) throws Exception {
            this.thread = new Thread(null, runnable, "SIThread", 0L, false);
            this.thread.setDaemon(true);
            this.runnable = runnable;
        }

        SingletonServer(String stringId) throws Exception {
            this(new SingletonServerRunnable(stringId));
        }

        int getPort() {
            return this.runnable.getPort();
        }

        void start() {
            this.thread.start();
        }
    }

    public static interface SingletonApp {
        public boolean canStartNewActivation(Properties var1);

        public void newActivation(List<String> var1);
    }

    private static class SingletonServerRunnable
    implements Runnable {
        final ServerSocket ss;
        final int port;
        final String stringId;

        int getPort() {
            return this.port;
        }

        SingletonServerRunnable(String id) throws IOException {
            this.stringId = id;
            this.ss = new ServerSocket(0, 0, InetAddress.getByName(Singleton.LOCALHOST));
            this.port = this.ss.getLocalPort();
            LOGGER.info("Local port of the Singleton server: {}", (Object)this.port);
            SingletonServerRunnable.createSingletonFile(this.stringId, this.port);
        }

        private void removeSiFile() {
            File file = Singleton.getSiFile(this.stringId, this.port);
            LOGGER.info("Removing SingletonFile: {}", (Object)file);
            FileUtil.delete(file.toPath());
        }

        private static void createSingletonFile(String id, int port) {
            File siFile = Singleton.getSiFile(id, port);
            SI_FILEDIR.mkdirs();
            String[] fList = SI_FILEDIR.list();
            if (fList != null) {
                for (String file : fList) {
                    if (!file.startsWith(id)) continue;
                    LOGGER.info("Remove file with same prefix {}", (Object)file);
                    FileUtil.delete(Path.of(SI_FILEDIR.getPath(), file));
                }
            }
            try (PrintStream out = new PrintStream(new FileOutputStream(siFile));){
                siFile.deleteOnExit();
                randomNumber = random.nextInt();
                out.print(randomNumber);
            }
            catch (IOException e) {
                LOGGER.error("Cannot add random number to file", e);
            }
        }

        @Override
        public void run() {
            this.runSingletonServer();
        }

        private static String getStreamEncoding(InputStream is) throws IOException {
            int encoding = is.read();
            switch (encoding) {
                case 1: {
                    return Charset.defaultCharset().name();
                }
                case 2: {
                    return Singleton.ENCODING_UNICODE_NAME;
                }
            }
            LOGGER.error("Unknown encoding: {}", (Object)encoding);
            return null;
        }

        private Void runSingletonServer() {
            try (ExecutorService executor = Executors.newFixedThreadPool(1);){
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket socket = this.ss.accept();
                        executor.submit(() -> this.handleClient(socket));
                    }
                    catch (IOException e) {
                        if (this.ss.isClosed()) {
                            break;
                        }
                        LOGGER.error("Error accepting connection", e);
                    }
                }
            }
            return null;
        }

        private void handleClient(Socket socket) {
            try (InputStream is = socket.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is, SingletonServerRunnable.getStreamEncoding(is));
                 BufferedReader in = new BufferedReader(isr);){
                LOGGER.debug("Singleton server is waiting for a connection");
                String line = in.readLine();
                if (line.equals(String.valueOf(randomNumber))) {
                    line = in.readLine();
                    LOGGER.debug("Receive message: {}", (Object)line);
                    if (Singleton.SI_MAGICWORD.equals(line)) {
                        this.handleMagicWord(in, socket, isr);
                    } else if (Singleton.SI_STOP.equals(line)) {
                        this.removeSiFile();
                        this.ss.close();
                    }
                } else {
                    this.handleUnexpectedError();
                }
            }
            catch (IOException ex) {
                LOGGER.error("Error handling client", ex);
            }
        }

        private void handleMagicWord(BufferedReader in, Socket s, InputStreamReader isr) throws IOException {
            String line;
            ArrayList<String> args = new ArrayList<String>();
            Properties props = new Properties();
            boolean arg = false;
            while (!Singleton.SI_EOF.equals(line = in.readLine())) {
                if (Singleton.SI_ARG.equals(line)) {
                    arg = true;
                    continue;
                }
                if (Singleton.SI_PROP.equals(line)) {
                    arg = false;
                    continue;
                }
                if (!Utils.hasText(line)) continue;
                if (arg) {
                    args.add(line);
                    continue;
                }
                String[] vals = line.split("=", 2);
                if (vals.length != 2) continue;
                props.put(vals[0], vals[1]);
            }
            if (siApp.canStartNewActivation(props)) {
                siApp.newActivation(args);
                LOGGER.debug("Sending ACK for loading args");
                this.sendResponse(s, isr, Singleton.SI_ACK);
            } else {
                LOGGER.debug("Sending EXIT");
                this.sendResponse(s, isr, Singleton.SI_EXIT);
                System.exit(0);
            }
        }

        private void sendResponse(Socket s, InputStreamReader isr, String response) throws IOException {
            try (OutputStream os = s.getOutputStream();
                 PrintStream ps = new PrintStream(os, true, isr.getEncoding());){
                ps.println(response);
                ps.flush();
            }
        }

        private void handleUnexpectedError() throws IOException {
            this.removeSiFile();
            this.ss.close();
            serverStarted = false;
            LOGGER.error("Unexpected error: Singleton {} disabled", (Object)this.stringId);
        }
    }
}

