/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.data;

import java.util.Arrays;
import java.util.StringTokenizer;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonName {
    private static final Logger LOG = LoggerFactory.getLogger(PersonName.class);
    private final String[] fields = new String[15];

    public PersonName() {
    }

    public PersonName(String s) {
        this(s, false);
    }

    public PersonName(String s, boolean lenient) {
        if (s != null) {
            this.parse(s, lenient);
        }
    }

    public void set(Group g, String s) {
        this.set(g, s, false);
    }

    public void set(Group g, String s, boolean lenient) {
        if (s.indexOf(61) >= 0) {
            throw new IllegalArgumentException(s);
        }
        this.parse(g, s, lenient);
    }

    private void parse(String s, boolean lenient) {
        this.parseInternal(null, s, lenient);
    }

    private void parse(Group g, String s, boolean lenient) {
        this.parseInternal(g, s, lenient);
    }

    private void parseInternal(Group g, String s, boolean lenient) {
        int gindex = g == null ? 0 : g.ordinal();
        int cindex = 0;
        StringTokenizer stk = new StringTokenizer(s, g == null ? "^=" : "^", true);
        block4: while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            switch (tk.charAt(0)) {
                case '=': {
                    if (g != null) {
                        throw new IllegalArgumentException("Invalid '=' token in group parsing: " + s);
                    }
                    if (++gindex > 2) {
                        if (lenient) {
                            LOG.info("illegal PN: {} - truncate illegal component group(s)", (Object)s);
                            return;
                        }
                        throw new IllegalArgumentException(s);
                    }
                    cindex = 0;
                    continue block4;
                }
                case '^': {
                    ++cindex;
                    continue block4;
                }
            }
            if (cindex <= 4) {
                this.set(gindex, cindex, tk);
                continue;
            }
            if (lenient) {
                if ((tk = PersonName.trim(tk)) == null) continue;
                LOG.info("illegal PN: {} - subsumes {}th component in suffix", (Object)s, (Object)(cindex + 1));
                this.set(gindex, 4, StringUtils.maskNull(this.get(gindex, 4), "") + ' ' + tk);
                continue;
            }
            throw new IllegalArgumentException(s);
        }
    }

    public String toString() {
        int totLen = 0;
        Group lastGroup = Group.Alphabetic;
        for (Group g : Group.values()) {
            Component lastCompOfGroup = Component.FamilyName;
            for (Component c : Component.values()) {
                String s = this.get(g, c);
                if (s == null) continue;
                totLen += s.length();
                lastGroup = g;
                lastCompOfGroup = c;
            }
            totLen += lastCompOfGroup.ordinal();
        }
        char[] ch = new char[totLen += lastGroup.ordinal()];
        int wpos = 0;
        for (Group g : Group.values()) {
            Component lastCompOfGroup = Component.FamilyName;
            for (Component c : Component.values()) {
                String s = this.get(g, c);
                if (s == null) continue;
                int d = c.ordinal() - lastCompOfGroup.ordinal();
                while (d-- > 0) {
                    ch[wpos++] = 94;
                }
                d = s.length();
                s.getChars(0, d, ch, wpos);
                wpos += d;
                lastCompOfGroup = c;
            }
            if (g == lastGroup) break;
            ch[wpos++] = 61;
        }
        return new String(ch);
    }

    public String toString(Group g, boolean trim) {
        int totLen = 0;
        Component lastCompOfGroup = Component.FamilyName;
        for (Component c : Component.values()) {
            String s = this.get(g, c);
            if (s == null) continue;
            totLen += s.length();
            lastCompOfGroup = c;
        }
        char[] ch = new char[totLen += trim ? lastCompOfGroup.ordinal() : 4];
        int wpos = 0;
        for (Component c : Component.values()) {
            String s = this.get(g, c);
            if (s != null) {
                int d = s.length();
                s.getChars(0, d, ch, wpos);
                wpos += d;
            }
            if (trim && c == lastCompOfGroup) break;
            if (wpos >= ch.length) continue;
            ch[wpos++] = 94;
        }
        return new String(ch);
    }

    public String get(Component c) {
        return this.get(Group.Alphabetic, c);
    }

    public String get(Group g, Component c) {
        return this.get(g.ordinal(), c.ordinal());
    }

    public void set(Component c, String s) {
        this.set(Group.Alphabetic, c, s);
    }

    public void set(Group g, Component c, String s) {
        this.set(g.ordinal(), c.ordinal(), s);
    }

    private String get(int gindex, int cindex) {
        return this.fields[gindex * 5 + cindex];
    }

    private void set(int gindex, int cindex, String s) {
        this.fields[gindex * 5 + cindex] = PersonName.trim(s);
    }

    public boolean isEmpty() {
        for (Group g : Group.values()) {
            if (!this.contains(g)) continue;
            return false;
        }
        return true;
    }

    public boolean contains(Group g) {
        for (Component c : Component.values()) {
            if (!this.contains(g, c)) continue;
            return true;
        }
        return false;
    }

    public boolean contains(Group g, Component c) {
        return this.get(g, c) != null;
    }

    public boolean contains(Component c) {
        return this.contains(Group.Alphabetic, c);
    }

    private static String trim(String s) {
        return s == null || (s = s.trim()).isEmpty() ? null : s;
    }

    public int hashCode() {
        return Arrays.hashCode(this.fields);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PersonName)) {
            return false;
        }
        PersonName other = (PersonName)obj;
        return Arrays.equals(this.fields, other.fields);
    }

    public static enum Group {
        Alphabetic,
        Ideographic,
        Phonetic;

    }

    public static enum Component {
        FamilyName,
        GivenName,
        MiddleName,
        NamePrefix,
        NameSuffix;

    }
}

