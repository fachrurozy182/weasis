/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

@ConfiguratorRank(value=0)
public class DefaultJoranConfigurator
extends ContextAwareBase
implements Configurator {
    @Override
    public Configurator.ExecutionStatus configure(LoggerContext context) {
        URL url = this.performMultiStepConfigurationFileSearch(true);
        if (url != null) {
            try {
                this.configureByResource(url);
            }
            catch (JoranException e) {
                e.printStackTrace();
            }
            return Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
        }
        return Configurator.ExecutionStatus.INVOKE_NEXT_IF_ANY;
    }

    protected URL performMultiStepConfigurationFileSearch(boolean updateStatus) {
        ClassLoader myClassLoader = Loader.getClassLoaderOfObject(this);
        URL url = this.findConfigFileURLFromSystemProperties(myClassLoader, updateStatus);
        if (url != null) {
            return url;
        }
        url = this.getResource("logback-test.xml", myClassLoader, updateStatus);
        if (url != null) {
            return url;
        }
        return this.getResource("logback.xml", myClassLoader, updateStatus);
    }

    public void configureByResource(URL url) throws JoranException {
        if (url == null) {
            throw new IllegalArgumentException("URL argument cannot be null");
        }
        String urlString = url.toString();
        if (!urlString.endsWith("xml")) {
            throw new LogbackException("Unexpected filename extension of file [" + url.toString() + "]. Should be .xml");
        }
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(this.context);
        configurator.doConfigure(url);
    }

    public URL findURLOfDefaultConfigurationFile(boolean updateStatus) {
        return this.performMultiStepConfigurationFileSearch(updateStatus);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private URL findConfigFileURLFromSystemProperties(ClassLoader classLoader, boolean updateStatus) {
        String logbackConfigFile = OptionHelper.getSystemProperty("logback.configurationFile");
        if (logbackConfigFile != null) {
            URL result = null;
            try {
                URL uRL = result = new URL(logbackConfigFile);
                return uRL;
            }
            catch (MalformedURLException e) {
                result = Loader.getResource(logbackConfigFile, classLoader);
                if (result != null) {
                    URL uRL = result;
                    return uRL;
                }
                File f = new File(logbackConfigFile);
                if (f.exists() && f.isFile()) {
                    try {
                        URL uRL = result = f.toURI().toURL();
                        return uRL;
                    }
                    catch (MalformedURLException malformedURLException) {}
                }
            }
            finally {
                if (updateStatus) {
                    this.statusOnResourceSearch(logbackConfigFile, classLoader, result);
                }
            }
        }
        return null;
    }

    private URL getResource(String filename, ClassLoader myClassLoader, boolean updateStatus) {
        URL url = Loader.getResource(filename, myClassLoader);
        if (updateStatus) {
            this.statusOnResourceSearch(filename, myClassLoader, url);
        }
        return url;
    }

    private void statusOnResourceSearch(String resourceName, ClassLoader classLoader, URL url) {
        StatusManager sm = this.context.getStatusManager();
        if (url == null) {
            sm.add(new InfoStatus("Could NOT find resource [" + resourceName + "]", this.context));
        } else {
            sm.add(new InfoStatus("Found resource [" + resourceName + "] at [" + url.toString() + "]", this.context));
            this.multiplicityWarning(resourceName, classLoader);
        }
    }

    private void multiplicityWarning(String resourceName, ClassLoader classLoader) {
        Set<URL> urlSet = null;
        try {
            urlSet = Loader.getResources(resourceName, classLoader);
        }
        catch (IOException e) {
            this.addError("Failed to get url list for resource [" + resourceName + "]", e);
        }
        if (urlSet != null && urlSet.size() > 1) {
            this.addWarn("Resource [" + resourceName + "] occurs multiple times on the classpath.");
            for (URL url : urlSet) {
                this.addWarn("Resource [" + resourceName + "] occurs at [" + url.toString() + "]");
            }
        }
    }
}

