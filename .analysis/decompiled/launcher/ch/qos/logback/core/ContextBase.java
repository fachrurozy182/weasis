/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core;

import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LifeCycleManager;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ConfigurationEventListener;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.ExecutorServiceUtil;
import ch.qos.logback.core.util.NetworkAddressUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class ContextBase
implements Context,
LifeCycle {
    private long birthTime = System.currentTimeMillis();
    private String name;
    private StatusManager sm = new BasicStatusManager();
    Map<String, String> propertyMap = new HashMap<String, String>();
    Map<String, Object> objectMap = new ConcurrentHashMap<String, Object>();
    protected ReentrantLock configurationLock = new ReentrantLock();
    private final List<ConfigurationEventListener> configurationEventListenerList = new CopyOnWriteArrayList<ConfigurationEventListener>();
    private ScheduledExecutorService scheduledExecutorService;
    private ThreadPoolExecutor threadPoolExecutor;
    private ExecutorService alternateExecutorService;
    protected List<ScheduledFuture<?>> scheduledFutures = new ArrayList(1);
    private LifeCycleManager lifeCycleManager;
    private SequenceNumberGenerator sequenceNumberGenerator;
    private volatile boolean started;

    @Override
    public StatusManager getStatusManager() {
        return this.sm;
    }

    public void setStatusManager(StatusManager statusManager) {
        if (statusManager == null) {
            throw new IllegalArgumentException("null StatusManager not allowed");
        }
        this.sm = statusManager;
    }

    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return new HashMap<String, String>(this.propertyMap);
    }

    @Override
    public void putProperty(String key, String val) {
        if ("HOSTNAME".equalsIgnoreCase(key)) {
            this.putHostnameProperty(val);
        } else {
            this.propertyMap.put(key, val);
        }
    }

    @Override
    public void addSubstitutionProperty(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        value = value.trim();
        this.propertyMap.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        if ("CONTEXT_NAME".equals(key)) {
            return this.getName();
        }
        if ("HOSTNAME".equalsIgnoreCase(key)) {
            return this.lazyGetHostname();
        }
        return this.propertyMap.get(key);
    }

    private String lazyGetHostname() {
        String hostname = this.propertyMap.get("HOSTNAME");
        if (hostname == null) {
            hostname = new NetworkAddressUtil(this).safelyGetLocalHostName();
            this.putHostnameProperty(hostname);
        }
        return hostname;
    }

    private void putHostnameProperty(String hostname) {
        String existingHostname = this.propertyMap.get("HOSTNAME");
        if (existingHostname == null) {
            this.propertyMap.put("HOSTNAME", hostname);
        }
    }

    @Override
    public Object getObject(String key) {
        return this.objectMap.get(key);
    }

    @Override
    public void putObject(String key, Object value) {
        this.objectMap.put(key, value);
    }

    public void removeObject(String key) {
        this.objectMap.remove(key);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void start() {
        this.started = true;
    }

    @Override
    public void stop() {
        this.stopExecutorServices();
        this.started = false;
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    public void reset() {
        this.removeShutdownHook();
        this.getLifeCycleManager().reset();
        this.propertyMap.clear();
        this.objectMap.clear();
    }

    @Override
    public void setName(String name) throws IllegalStateException {
        if (name != null && name.equals(this.name)) {
            return;
        }
        if (this.name != null && !"default".equals(this.name)) {
            throw new IllegalStateException("Context has been already given a name");
        }
        this.name = name;
    }

    @Override
    public long getBirthTime() {
        return this.birthTime;
    }

    @Override
    public ReentrantLock getConfigurationLock() {
        return this.configurationLock;
    }

    @Override
    public synchronized ExecutorService getExecutorService() {
        if (this.threadPoolExecutor == null) {
            this.threadPoolExecutor = ExecutorServiceUtil.newThreadPoolExecutor();
        }
        return this.threadPoolExecutor;
    }

    @Override
    public synchronized ExecutorService getAlternateExecutorService() {
        if (this.alternateExecutorService == null) {
            this.alternateExecutorService = ExecutorServiceUtil.newAlternateThreadPoolExecutor();
        }
        return this.alternateExecutorService;
    }

    @Override
    public synchronized ScheduledExecutorService getScheduledExecutorService() {
        if (this.scheduledExecutorService == null) {
            this.scheduledExecutorService = ExecutorServiceUtil.newScheduledExecutorService();
        }
        return this.scheduledExecutorService;
    }

    private synchronized void stopExecutorServices() {
        ExecutorServiceUtil.shutdown(this.scheduledExecutorService);
        this.scheduledExecutorService = null;
        ExecutorServiceUtil.shutdown(this.threadPoolExecutor);
        this.threadPoolExecutor = null;
    }

    private void removeShutdownHook() {
        Thread hook = (Thread)this.getObject("SHUTDOWN_HOOK");
        if (hook != null) {
            this.removeObject("SHUTDOWN_HOOK");
            try {
                this.sm.add(new InfoStatus("Removing shutdownHook " + String.valueOf(hook), this));
                Runtime runtime = Runtime.getRuntime();
                boolean result = runtime.removeShutdownHook(hook);
                this.sm.add(new InfoStatus("ShutdownHook removal result: " + result, this));
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
    }

    @Override
    public void register(LifeCycle component) {
        this.getLifeCycleManager().register(component);
    }

    synchronized LifeCycleManager getLifeCycleManager() {
        if (this.lifeCycleManager == null) {
            this.lifeCycleManager = new LifeCycleManager();
        }
        return this.lifeCycleManager;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public void addScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFutures.add(scheduledFuture);
    }

    @Deprecated
    public List<ScheduledFuture<?>> getScheduledFutures() {
        return this.getCopyOfScheduledFutures();
    }

    public List<ScheduledFuture<?>> getCopyOfScheduledFutures() {
        return new ArrayList(this.scheduledFutures);
    }

    @Override
    public SequenceNumberGenerator getSequenceNumberGenerator() {
        return this.sequenceNumberGenerator;
    }

    @Override
    public void setSequenceNumberGenerator(SequenceNumberGenerator sequenceNumberGenerator) {
        this.sequenceNumberGenerator = sequenceNumberGenerator;
    }

    @Override
    public void addConfigurationEventListener(ConfigurationEventListener listener) {
        this.configurationEventListenerList.add(listener);
    }

    @Override
    public void removeConfigurationEventListener(ConfigurationEventListener listener) {
        this.configurationEventListenerList.remove(listener);
    }

    @Override
    public void fireConfigurationEvent(ConfigurationEvent configurationEvent) {
        this.configurationEventListenerList.forEach(l -> l.listen(configurationEvent));
    }
}

