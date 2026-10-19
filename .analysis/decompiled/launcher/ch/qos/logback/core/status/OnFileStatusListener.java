/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.status;

import ch.qos.logback.core.status.OnPrintStreamStatusListenerBase;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class OnFileStatusListener
extends OnPrintStreamStatusListenerBase {
    String filename;
    PrintStream ps;

    @Override
    public void start() {
        if (this.filename == null) {
            this.addInfo("File option not set. Defaulting to \"status.txt\"");
            this.filename = "status.txt";
        }
        try {
            FileOutputStream fos = new FileOutputStream(this.filename, true);
            this.ps = new PrintStream(fos, true);
        }
        catch (FileNotFoundException e) {
            this.addError("Failed to open [" + this.filename + "]", e);
            return;
        }
        super.start();
    }

    @Override
    public void stop() {
        if (!this.isStarted) {
            return;
        }
        if (this.ps != null) {
            this.ps.close();
        }
        super.stop();
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    protected PrintStream getPrintStream() {
        return this.ps;
    }
}

