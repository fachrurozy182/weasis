/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;
import java.net.URL;

public class ConfigurationWatchListUtil {
    static final ConfigurationWatchListUtil ORIGIN = new ConfigurationWatchListUtil();

    private ConfigurationWatchListUtil() {
    }

    public static void registerConfigurationWatchList(Context context, ConfigurationWatchList cwl) {
        context.putObject("CONFIGURATION_WATCH_LIST", cwl);
    }

    public static void setMainWatchURL(Context context, URL url) {
        if (url == null) {
            return;
        }
        ConfigurationWatchList cwl = ConfigurationWatchListUtil.getConfigurationWatchList(context);
        if (cwl == null) {
            ConfigurationWatchListUtil.addError(context, "ConfigurationWatchList should have been initialized at this stage.");
            return;
        }
        cwl.clear();
        cwl.setTopURL(url);
    }

    public static boolean watchPredicateFulfilled(Context context) {
        ConfigurationWatchList cwl = ConfigurationWatchListUtil.getConfigurationWatchList(context);
        if (cwl == null) {
            return false;
        }
        return cwl.watchPredicateFulfilled();
    }

    public static URL getMainWatchURL(Context context) {
        ConfigurationWatchList cwl = ConfigurationWatchListUtil.getConfigurationWatchList(context);
        if (cwl == null) {
            return null;
        }
        return cwl.getTopURL();
    }

    public static void addToWatchList(Context context, URL url) {
        ConfigurationWatchListUtil.addToWatchList(context, url, false);
    }

    public static void addToWatchList(Context context, URL url, boolean createCWL) {
        ConfigurationWatchList cwl = ConfigurationWatchListUtil.getConfigurationWatchList(context);
        if (cwl == null) {
            if (createCWL && ConfigurationWatchList.isWatchableProtocol(url)) {
                cwl = ConfigurationWatchListUtil.registerNewConfigurationWatchListWithContext(context);
            } else {
                ConfigurationWatchListUtil.addInfo(context, "ConfigurationWatchList not initialized due to absence of scan directive. Will not add " + String.valueOf(url));
                return;
            }
        }
        String protocol = url.getProtocol();
        if (ConfigurationWatchList.isWatchableProtocol(protocol)) {
            ConfigurationWatchListUtil.addInfo(context, "Will add [" + String.valueOf(url) + "] to configuration watch list.");
            cwl.addToWatchList(url);
        } else {
            ConfigurationWatchListUtil.addInfo(context, "Will not add configuration file [" + String.valueOf(url) + "] to watch list, because '" + protocol + "' protocol is not watchable.");
            ConfigurationWatchListUtil.addInfo(context, "Only the protocols 'file', 'http' and 'https' are watchable.");
        }
    }

    public static ConfigurationWatchList registerNewConfigurationWatchListWithContext(Context context) {
        ConfigurationWatchList cwl = new ConfigurationWatchList();
        cwl.setContext(context);
        context.putObject("CONFIGURATION_WATCH_LIST", cwl);
        return cwl;
    }

    public static ConfigurationWatchList getConfigurationWatchList(Context context) {
        return (ConfigurationWatchList)context.getObject("CONFIGURATION_WATCH_LIST");
    }

    static void addStatus(Context context, Status s) {
        if (context == null) {
            System.out.println("Null context in " + ConfigurationWatchList.class.getName());
            return;
        }
        StatusManager sm = context.getStatusManager();
        if (sm == null) {
            return;
        }
        sm.add(s);
    }

    static void addInfo(Context context, String msg) {
        ConfigurationWatchListUtil.addStatus(context, new InfoStatus(msg, ORIGIN));
    }

    static void addWarn(Context context, String msg) {
        ConfigurationWatchListUtil.addStatus(context, new WarnStatus(msg, ORIGIN));
    }

    static void addError(Context context, String msg) {
        ConfigurationWatchListUtil.addStatus(context, new ErrorStatus(msg, ORIGIN));
    }
}

