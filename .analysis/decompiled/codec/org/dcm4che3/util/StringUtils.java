/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.net.URL;
import java.security.AccessController;
import java.util.Collection;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class StringUtils {
    public static String LINE_SEPARATOR = AccessController.doPrivileged(() -> System.getProperty("line.separator"));
    public static String[] EMPTY_STRING = new String[0];

    public static StringBuilder appendLine(StringBuilder sb, Object ... ss) {
        for (Object s : ss) {
            sb.append(s);
        }
        return sb.append(LINE_SEPARATOR);
    }

    public static String concat(String[] ss, char delim) {
        int n = ss.length;
        if (n == 0) {
            return "";
        }
        if (n == 1) {
            String s = ss[0];
            return s != null ? s : "";
        }
        int len = n - 1;
        for (String s : ss) {
            if (s == null) continue;
            len += s.length();
        }
        char[] cs = new char[len];
        int off = 0;
        int i = 0;
        for (String s : ss) {
            if (i++ != 0) {
                cs[off++] = delim;
            }
            if (s == null) continue;
            int l = s.length();
            s.getChars(0, l, cs, off);
            off += l;
        }
        return new String(cs);
    }

    public static String concat(Collection<String> ss, char delim) {
        int n = ss.size();
        if (n == 0) {
            return "";
        }
        if (n == 1) {
            String s = ss.iterator().next();
            return s != null ? s : "";
        }
        int len = n - 1;
        for (String s : ss) {
            if (s == null) continue;
            len += s.length();
        }
        char[] cs = new char[len];
        int off = 0;
        int i = 0;
        for (String s : ss) {
            if (i++ != 0) {
                cs[off++] = delim;
            }
            if (s == null) continue;
            int l = s.length();
            s.getChars(0, l, cs, off);
            off += l;
        }
        return new String(cs);
    }

    public static Object splitAndTrim(String s, char delim) {
        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos + 1)) >= 0) {
            ++count;
        }
        if (count == 1) {
            return StringUtils.substring(s, 0, s.length());
        }
        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2 - 1);
            ss[count] = StringUtils.substring(s, delimPos + 1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    public static String[] split(String s, char delim) {
        if (s == null || s.isEmpty()) {
            return EMPTY_STRING;
        }
        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos + 1)) >= 0) {
            ++count;
        }
        if (count == 1) {
            return new String[]{s};
        }
        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2 - 1);
            ss[count] = s.substring(delimPos + 1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    public static String cut(String s, int index, char delim) {
        int end;
        int i = 0;
        int begin = 0;
        while ((end = s.indexOf(delim, begin)) >= 0) {
            if (i++ == index) {
                return s.substring(begin, end);
            }
            begin = end + 1;
        }
        return i == index ? s.substring(begin) : "";
    }

    private static String substring(String s, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && s.charAt(beginIndex) <= ' ') {
            ++beginIndex;
        }
        while (beginIndex < endIndex && s.charAt(endIndex - 1) <= ' ') {
            --endIndex;
        }
        return beginIndex < endIndex ? s.substring(beginIndex, endIndex) : "";
    }

    public static String trimTrailing(String s) {
        int endIndex;
        for (endIndex = s.length(); endIndex > 0 && s.charAt(endIndex - 1) <= ' '; --endIndex) {
        }
        return s.substring(0, endIndex);
    }

    public static long parseIS(String s) {
        return s != null && s.length() != 0 ? Long.parseLong(s) : 0L;
    }

    public static long parseUV(String s) {
        return s != null && s.length() != 0 ? Long.parseUnsignedLong(s) : 0L;
    }

    public static double parseDS(String s) {
        return s != null && s.length() != 0 ? Double.parseDouble(s.replace(',', '.')) : 0.0;
    }

    public static String formatDS(float f) {
        int l;
        String s = Float.toString(f);
        if (s.startsWith(".0", (l = s.length()) - 2)) {
            return s.substring(0, l - 2);
        }
        int e = s.indexOf(69, l - 5);
        return e > 0 && s.startsWith(".0", e - 2) ? StringUtils.cut(s, e - 2, e) : s;
    }

    public static String formatDS(double d) {
        int l;
        String s = Double.toString(d);
        if (s.startsWith(".0", (l = s.length()) - 2)) {
            return s.substring(0, l - 2);
        }
        int skip = l - 16;
        int e = s.indexOf(69, l - 5);
        return e < 0 ? (skip > 0 ? s.substring(0, 16) : s) : (s.startsWith(".0", e - 2) ? StringUtils.cut(s, e - 2, e) : (skip > 0 ? StringUtils.cut(s, e - skip, e) : s));
    }

    private static String cut(String s, int begin, int end) {
        int l = s.length();
        char[] ch = new char[l - (end - begin)];
        s.getChars(0, begin, ch, 0);
        s.getChars(end, l, ch, begin);
        return new String(ch);
    }

    public static boolean matches(String s, String key, boolean matchNullOrEmpty, boolean ignoreCase) {
        if (key == null || key.isEmpty()) {
            return true;
        }
        if (s == null || s.isEmpty()) {
            return matchNullOrEmpty;
        }
        return StringUtils.containsWildCard(key) ? StringUtils.compilePattern(key, ignoreCase).matcher(s).matches() : (ignoreCase ? key.equalsIgnoreCase(s) : key.equals(s));
    }

    public static Pattern compilePattern(String key, boolean ignoreCase) {
        StringTokenizer stk = new StringTokenizer(key, "*?", true);
        StringBuilder regex = new StringBuilder();
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            char ch1 = tk.charAt(0);
            if (ch1 == '*') {
                regex.append(".*");
                continue;
            }
            if (ch1 == '?') {
                regex.append(".");
                continue;
            }
            regex.append("\\Q").append(tk).append("\\E");
        }
        return Pattern.compile(regex.toString(), ignoreCase ? 2 : 0);
    }

    public static boolean containsWildCard(String s) {
        return s.indexOf(42) >= 0 || s.indexOf(63) >= 0;
    }

    public static String[] maskNull(String[] ss) {
        return StringUtils.maskNull(ss, EMPTY_STRING);
    }

    public static <T> T maskNull(T o, T mask) {
        return o == null ? mask : o;
    }

    public static <T> T nullify(T o, T val) {
        return val.equals(o) ? null : (T)o;
    }

    public static String maskEmpty(String s, String mask) {
        return s == null || s.isEmpty() ? mask : s;
    }

    public static String truncate(String s, int maxlen) {
        return s.length() > maxlen ? s.substring(0, maxlen) : s;
    }

    public static String replaceNonPrintASCII(String s, char replacement) {
        char[] cs = s.toCharArray();
        int count = 0;
        for (int i = 0; i < cs.length; ++i) {
            if (cs[i] > ' ' && cs[i] < '\u007f') continue;
            cs[i] = replacement;
            ++count;
        }
        return count > 0 ? new String(cs) : s;
    }

    public static <T> boolean equals(T o1, T o2) {
        return o1 == o2 || o1 != null && o1.equals(o2);
    }

    public static String replaceSystemProperties(String s) {
        int i = s.indexOf("${");
        if (i == -1) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s.length());
        int j = -1;
        do {
            String val;
            sb.append(s.substring(j + 1, i));
            j = s.indexOf(125, i + 2);
            if (j == -1) {
                j = i - 1;
                break;
            }
            int k = s.lastIndexOf(58, j);
            String string = s.startsWith("env.", i + 2) ? System.getenv(s.substring(i + 6, k < i ? j : k)) : (val = System.getProperty(s.substring(i + 2, k < i ? j : k)));
            sb.append(val != null ? val : (k < 0 ? s.substring(i, j + 1) : s.substring(k + 1, j)));
        } while ((i = s.indexOf("${", j + 1)) != -1);
        sb.append(s.substring(j + 1));
        return sb.toString();
    }

    @Deprecated
    public static String resourceURL(String name) {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        URL url = tcl.getResource(name);
        return url != null ? url.toString() : null;
    }

    public static boolean isUpperCase(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            if (Character.toUpperCase(ch) == ch) continue;
            return false;
        }
        return len != 0;
    }

    public static boolean isIPAddr(String s) {
        String[] ss = StringUtils.split(s, ':');
        if (ss.length > 1) {
            return ss.length == 8;
        }
        ss = StringUtils.split(s, '.');
        if (ss.length != 4) {
            return false;
        }
        block5: for (String s1 : ss) {
            if (s1.length() > 3) {
                return false;
            }
            int i = 0;
            switch (s1.length()) {
                case 3: {
                    char ch = s1.charAt(i++);
                    if (ch != '1' && ch != '2') {
                        return false;
                    }
                }
                case 2: {
                    char ch = s1.charAt(i++);
                    if (ch < '0' || ch > '9') {
                        return false;
                    }
                }
                case 1: {
                    char ch = s1.charAt(i++);
                    if (ch >= '0' && ch <= '9') continue block5;
                    return false;
                }
                default: {
                    return false;
                }
            }
        }
        return true;
    }

    public static <T> boolean contains(T[] a, T o) {
        for (T t : a) {
            if (!Objects.equals(t, o)) continue;
            return true;
        }
        return false;
    }

    public static <T> T[] requireNotEmpty(T[] a, String message) {
        if (a.length == 0) {
            throw new IllegalArgumentException(message);
        }
        return a;
    }

    public static String requireNotEmpty(String s, String message) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return s;
    }

    public static String[] requireContainsNoEmpty(String[] ss, String message) {
        for (String s : ss) {
            StringUtils.requireNotEmpty(s, message);
        }
        return ss;
    }

    public static String getPropertyOrEnv(String property, String env, String def) {
        String value = System.getProperty(property);
        if (value == null && (value = System.getenv(env)) == null) {
            value = def;
        }
        return value;
    }
}

