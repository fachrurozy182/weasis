/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core;

import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileAppender<E>
extends OutputStreamAppender<E> {
    public static final long DEFAULT_BUFFER_SIZE = 8192L;
    protected static String COLLISION_WITH_EARLIER_APPENDER_URL = "https://logback.qos.ch/codes.html#earlier_fa_collision";
    protected boolean append = true;
    protected String fileName = null;
    private boolean prudent = false;
    private FileSize bufferSize = new FileSize(8192L);

    public void setFile(String file) {
        this.fileName = file == null ? file : file.trim();
    }

    public boolean isAppend() {
        return this.append;
    }

    public final String rawFileProperty() {
        return this.fileName;
    }

    public String getFile() {
        return this.fileName;
    }

    @Override
    public void start() {
        int errors = 0;
        if (this.getFile() != null) {
            this.addInfo("File property is set to [" + this.fileName + "]");
            if (this.prudent && !this.isAppend()) {
                this.setAppend(true);
                this.addWarn("Setting \"Append\" property to true on account of \"Prudent\" mode");
            }
            try {
                this.openFile(this.getFile());
            }
            catch (IOException e) {
                ++errors;
                this.addError("openFile(" + this.fileName + "," + this.append + ") call failed.", e);
            }
        } else {
            ++errors;
            this.addError("\"File\" property not set for appender named [" + this.name + "].");
        }
        if (errors == 0) {
            super.start();
        }
    }

    @Override
    public void stop() {
        if (!this.isStarted()) {
            return;
        }
        super.stop();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void openFile(String file_name) throws IOException {
        this.streamWriteLock.lock();
        try {
            File file = new File(file_name);
            boolean result = FileUtil.createMissingParentDirectories(file);
            if (!result) {
                this.addError("Failed to create parent directories for [" + file.getAbsolutePath() + "]");
            }
            ResilientFileOutputStream resilientFos = new ResilientFileOutputStream(file, this.append, this.bufferSize.getSize());
            resilientFos.setContext(this.context);
            this.setOutputStream(resilientFos);
        }
        finally {
            this.streamWriteLock.unlock();
        }
    }

    public boolean isPrudent() {
        return this.prudent;
    }

    public void setPrudent(boolean prudent) {
        this.prudent = prudent;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public void setBufferSize(FileSize bufferSize) {
        this.addInfo("Setting bufferSize to [" + bufferSize.toString() + "]");
        this.bufferSize = bufferSize;
    }

    @Override
    protected void writeOut(E event) throws IOException {
        if (this.prudent) {
            this.safeWriteOut(event);
        } else {
            super.writeOut(event);
        }
    }

    private void safeWriteOut(E event) {
        byte[] byteArray = this.encoder.encode(event);
        if (byteArray == null || byteArray.length == 0) {
            return;
        }
        this.streamWriteLock.lock();
        try {
            this.safeWriteBytes(byteArray);
        }
        finally {
            this.streamWriteLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void safeWriteBytes(byte[] byteArray) {
        ResilientFileOutputStream resilientFOS = (ResilientFileOutputStream)this.getOutputStream();
        FileChannel fileChannel = resilientFOS.getChannel();
        if (fileChannel == null) {
            return;
        }
        boolean interrupted = Thread.interrupted();
        FileLock fileLock = null;
        try {
            fileLock = fileChannel.lock();
            long position = fileChannel.position();
            long size = fileChannel.size();
            if (size != position) {
                fileChannel.position(size);
            }
            this.writeByteArrayToOutputStreamWithPossibleFlush(byteArray);
        }
        catch (IOException e) {
            resilientFOS.postIOFailure(e);
        }
        finally {
            this.releaseFileLock(fileLock);
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void releaseFileLock(FileLock fileLock) {
        if (fileLock != null && fileLock.isValid()) {
            try {
                fileLock.release();
            }
            catch (IOException e) {
                this.addError("failed to release lock", e);
            }
        }
    }
}

