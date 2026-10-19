/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static boolean emptyToFalse(String value) {
        return Utils.hasText(value) && Boolean.parseBoolean(value);
    }

    public static boolean emptyToTrue(String value) {
        return !Utils.hasText(value) || Boolean.parseBoolean(value);
    }

    public static boolean hasLength(CharSequence str) {
        return str != null && !str.isEmpty();
    }

    public static boolean hasText(CharSequence str) {
        return Utils.hasLength(str) && str.chars().anyMatch(c -> !Character.isWhitespace(c));
    }

    public static Pattern getWeasisProtocolPattern() {
        return Pattern.compile("^weasis(-.*)?://.*?");
    }

    public static int getWeasisProtocolIndex(String ... params) {
        Pattern pattern = Utils.getWeasisProtocolPattern();
        for (int i = 0; i < params.length; ++i) {
            if (!pattern.matcher(params[i]).matches()) continue;
            return i;
        }
        return -1;
    }

    public static String removeEnglobingQuotes(String value) {
        return value.replaceAll("(?:^\")|(?:\"$)", "");
    }

    public static String adaptPathToUri(String value) {
        return value.replace("\\", "/").replace(" ", "%20");
    }

    public static List<String> splitSpaceExceptInQuotes(String s) {
        String arg;
        if (s == null) {
            return Collections.emptyList();
        }
        ArrayList<String> matchList = new ArrayList<String>();
        Pattern patternSpaceExceptQuotes = Pattern.compile("'[^']*'|\"[^\"]*\"|( )");
        Matcher m = patternSpaceExceptQuotes.matcher(s);
        StringBuilder b = new StringBuilder();
        while (m.find()) {
            if (m.group(1) != null) continue;
            m.appendReplacement(b, m.group(0));
            arg = b.toString();
            b.setLength(0);
            if (!Utils.hasText(arg)) continue;
            matchList.add(arg.trim());
        }
        b.setLength(0);
        m.appendTail(b);
        arg = b.toString();
        if (Utils.hasText(arg)) {
            matchList.add(arg.trim());
        }
        return matchList;
    }

    public static void openInDefaultBrowser(URL url) {
        if (url != null) {
            if (SystemInfo.isLinux) {
                Utils.openInLinuxBrowser(url);
            } else if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(url.toURI());
                    }
                    catch (IOException | URISyntaxException e) {
                        LOGGER.error("Cannot open URL to the desktop browser", e);
                    }
                }
            } else {
                LOGGER.warn("Cannot open URL to the system browser");
            }
        }
    }

    private static void openInLinuxBrowser(URL url) {
        try {
            Process process = new ProcessBuilder("xdg-open", url.toString()).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            process.getOutputStream().close();
            if (process.waitFor(5L, TimeUnit.SECONDS) && process.exitValue() != 0) {
                LOGGER.error("'xdg-open' failed (exit {}) for {}", (Object)process.exitValue(), (Object)url);
            }
        }
        catch (IOException e) {
            LOGGER.error("Cannot open URL to the system browser", e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

