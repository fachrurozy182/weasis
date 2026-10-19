/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EnvUtilForTests {
    static String GITHUB_HOME;
    static String LOCAL_REPOSITORY_PREFIX;

    public static boolean isGithubAction() {
        String userHome = System.getProperty("user.home");
        String localRepository = System.getProperty("localRepository");
        if (GITHUB_HOME.equals(userHome)) {
            return true;
        }
        return localRepository != null && localRepository.startsWith(LOCAL_REPOSITORY_PREFIX);
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").indexOf("Windows") != -1;
    }

    public static boolean isMac() {
        return System.getProperty("os.name").indexOf("Mac") != -1;
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").indexOf("Linux") != -1;
    }

    public static boolean isRunningOnSlowJenkins() {
        return System.getProperty("slowJenkins") != null;
    }

    public static String getLocalHostName() {
        try {
            InetAddress localhostIA = InetAddress.getLocalHost();
            return localhostIA.getHostName();
        }
        catch (UnknownHostException e) {
            return null;
        }
    }

    public static boolean isLocalHostNameInList(String[] hostList) {
        String localHostName = EnvUtilForTests.getLocalHostName();
        if (localHostName == null) {
            return false;
        }
        for (String host : hostList) {
            if (!host.equalsIgnoreCase(localHostName)) continue;
            return true;
        }
        return false;
    }

    public static String getPathToBash() {
        if (EnvUtilForTests.isLinux()) {
            return "bash";
        }
        if (EnvUtilForTests.isLocalHostNameInList(new String[]{"hetz", "het"})) {
            return "c:/cygwin/bin/bash";
        }
        return null;
    }

    static {
        LOCAL_REPOSITORY_PREFIX = GITHUB_HOME = "/home/runner";
    }
}

