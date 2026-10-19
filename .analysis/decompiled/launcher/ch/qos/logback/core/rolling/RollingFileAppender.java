/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.LengthCounter;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.RollingPolicyBase;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.TriggeringPolicy;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RollingFileAppender<E>
extends FileAppender<E> {
    File currentlyActiveFile;
    TriggeringPolicy<E> triggeringPolicy;
    RollingPolicy rollingPolicy;
    Lock triggeringPolicyLock = new ReentrantLock();
    private static String RFA_NO_TP_URL = "https://logback.qos.ch/codes.html#rfa_no_tp";
    private static String RFA_NO_RP_URL = "https://logback.qos.ch/codes.html#rfa_no_rp";
    private static String COLLISION_URL = "https://logback.qos.ch/codes.html#rfa_collision";
    private static String RFA_LATE_FILE_URL = "https://logback.qos.ch/codes.html#rfa_file_after";
    private static String RFA_RESET_RP_OR_TP = "https://logback.qos.ch/codes.html#rfa_reset_rp_or_tp";

    @Override
    public void start() {
        if (this.triggeringPolicy == null) {
            this.addWarn("No TriggeringPolicy was set for the RollingFileAppender named " + this.getName());
            this.addWarn("For more information, please visit " + RFA_NO_TP_URL);
            return;
        }
        if (!this.triggeringPolicy.isStarted()) {
            this.addWarn("TriggeringPolicy has not started. RollingFileAppender will not start");
            return;
        }
        if (!this.append) {
            this.addWarn("Append mode is mandatory for RollingFileAppender. Defaulting to append=true.");
            this.append = true;
        }
        if (this.rollingPolicy == null) {
            this.addError("No RollingPolicy was set for the RollingFileAppender named " + this.getName());
            this.addError("For more information, please visit " + RFA_NO_RP_URL);
            return;
        }
        if (this.checkForFileAndPatternCollisions()) {
            this.addError("File property collides with fileNamePattern. Aborting.");
            this.addError("For more information, please visit " + COLLISION_URL);
            return;
        }
        if (this.isPrudent()) {
            if (this.rawFileProperty() != null) {
                this.addWarn("Setting \"File\" property to null on account of prudent mode");
                this.setFile(null);
            }
            if (this.rollingPolicy.getCompressionMode() != CompressionMode.NONE) {
                this.addError("Compression is not supported in prudent mode. Aborting");
                return;
            }
        }
        this.addInfo("Active log file name: " + this.getFile());
        this.currentlyActiveFile = new File(this.getFile());
        this.initializeLengthCounter();
        super.start();
    }

    private boolean checkForFileAndPatternCollisions() {
        if (this.triggeringPolicy instanceof RollingPolicyBase) {
            RollingPolicyBase base = (RollingPolicyBase)((Object)this.triggeringPolicy);
            FileNamePattern fileNamePattern = base.fileNamePattern;
            if (fileNamePattern != null && this.fileName != null) {
                String regex = fileNamePattern.toRegex();
                return this.fileName.matches(regex);
            }
        }
        return false;
    }

    private void initializeLengthCounter() {
        if (this.getLengthCounter() != null && this.currentlyActiveFile.exists()) {
            long currentFileLength = this.currentlyActiveFile.length();
            this.addInfo("Setting currentFileLength to " + currentFileLength + " for " + String.valueOf(this.currentlyActiveFile));
            this.incrementByteCount(currentFileLength);
        }
    }

    @Override
    public void stop() {
        if (!this.isStarted()) {
            return;
        }
        super.stop();
        if (this.rollingPolicy != null) {
            this.rollingPolicy.stop();
        }
        if (this.triggeringPolicy != null) {
            this.triggeringPolicy.stop();
        }
    }

    @Override
    public void setFile(String file) {
        if (file != null && (this.triggeringPolicy != null || this.rollingPolicy != null)) {
            this.addError("File property must be set before any triggeringPolicy or rollingPolicy properties");
            this.addError("For more information, please visit " + RFA_LATE_FILE_URL);
        }
        super.setFile(file);
    }

    @Override
    public String getFile() {
        return this.rollingPolicy.getActiveFileName();
    }

    public void rollover() {
        this.streamWriteLock.lock();
        try {
            this.closeOutputStream();
            this.attemptRollover();
            this.attemptOpenFile();
        }
        finally {
            this.streamWriteLock.unlock();
        }
    }

    private void attemptOpenFile() {
        try {
            this.currentlyActiveFile = new File(this.rollingPolicy.getActiveFileName());
            this.openFile(this.rollingPolicy.getActiveFileName());
        }
        catch (IOException e) {
            this.addError("setFile(" + this.fileName + ", false) call failed.", e);
        }
    }

    private void attemptRollover() {
        try {
            this.rollingPolicy.rollover();
        }
        catch (RolloverFailure rf) {
            this.addWarn("RolloverFailure occurred. Deferring roll-over.");
            this.append = true;
        }
    }

    @Override
    protected void subAppend(E event) {
        this.triggeringPolicyLock.lock();
        try {
            if (this.triggeringPolicy.isTriggeringEvent(this.currentlyActiveFile, event)) {
                this.rollover();
            }
        }
        finally {
            this.triggeringPolicyLock.unlock();
        }
        super.subAppend(event);
    }

    public RollingPolicy getRollingPolicy() {
        return this.rollingPolicy;
    }

    public TriggeringPolicy<E> getTriggeringPolicy() {
        return this.triggeringPolicy;
    }

    public void setRollingPolicy(RollingPolicy policy) {
        if (this.rollingPolicy instanceof TriggeringPolicy) {
            String className = this.rollingPolicy.getClass().getSimpleName();
            this.addWarn("A rolling policy of type " + className + " was already set.");
            this.addWarn("Note that " + className + " doubles as a TriggeringPolicy");
            this.addWarn("See also " + RFA_RESET_RP_OR_TP);
        }
        this.rollingPolicy = policy;
        if (this.rollingPolicy instanceof TriggeringPolicy) {
            this.triggeringPolicy = (TriggeringPolicy)((Object)policy);
        }
    }

    public void setTriggeringPolicy(TriggeringPolicy<E> policy) {
        if (this.triggeringPolicy instanceof RollingPolicy) {
            String className = this.triggeringPolicy.getClass().getSimpleName();
            this.addWarn("A triggering policy of type " + className + " was already set.");
            this.addWarn("Note that " + className + " doubles as a RollingPolicy");
            this.addWarn("See also " + RFA_RESET_RP_OR_TP);
        }
        this.triggeringPolicy = policy;
        if (policy instanceof RollingPolicy) {
            this.rollingPolicy = (RollingPolicy)((Object)policy);
        }
    }

    @Override
    protected void updateByteCount(byte[] byteArray) {
        if (byteArray == null) {
            return;
        }
        this.incrementByteCount(byteArray.length);
    }

    void incrementByteCount(long increment) {
        LengthCounter lengthCounter = this.getLengthCounter();
        if (lengthCounter == null) {
            return;
        }
        if (increment > 0L) {
            lengthCounter.add(increment);
        }
    }

    private LengthCounter getLengthCounter() {
        return this.triggeringPolicy.getLengthCounter();
    }
}

