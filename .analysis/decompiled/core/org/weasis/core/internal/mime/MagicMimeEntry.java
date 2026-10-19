/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.internal.mime;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.weasis.core.internal.mime.InvalidMagicMimeEntryException;
import org.weasis.core.util.StringUtil;

public class MagicMimeEntry {
    public static final int STRING_TYPE = 1;
    public static final int BELONG_TYPE = 2;
    public static final int SHORT_TYPE = 3;
    public static final int LELONG_TYPE = 4;
    public static final int BESHORT_TYPE = 5;
    public static final int LESHORT_TYPE = 6;
    public static final int BYTE_TYPE = 7;
    public static final int UNKNOWN_TYPE = 20;
    private final ArrayList<MagicMimeEntry> subEntries = new ArrayList();
    private int checkBytesFrom;
    private int type;
    private String typeStr;
    private String content;
    private String mimeType;
    private String mimeEnc;
    boolean isBetween;

    public MagicMimeEntry(List<String> entries) throws InvalidMagicMimeEntryException {
        this(0, null, entries);
    }

    private MagicMimeEntry(int level, MagicMimeEntry parent, List<String> entries) throws InvalidMagicMimeEntryException {
        int thisLevel;
        if (entries == null || entries.isEmpty()) {
            return;
        }
        try {
            this.addEntry(entries.get(0));
        }
        catch (Exception e) {
            throw new InvalidMagicMimeEntryException(entries, (Throwable)e);
        }
        entries.remove(0);
        if (parent != null) {
            parent.subEntries.add(this);
        }
        while (!entries.isEmpty() && (thisLevel = this.howManyGreaterThans(entries.get(0))) > level) {
            new MagicMimeEntry(thisLevel, this, entries);
        }
    }

    public String toString() {
        return "MimeMagicType: " + this.checkBytesFrom + ", " + this.type + ", " + this.content + ", " + this.mimeType + ", " + this.mimeEnc;
    }

    private int howManyGreaterThans(String aLine) {
        int i;
        if (aLine == null) {
            return -1;
        }
        int len = aLine.length();
        for (i = 0; i < len && aLine.charAt(i) == '>'; ++i) {
        }
        return i;
    }

    void addEntry(String aLine) {
        String trimmed = aLine.replaceAll("^>*", "");
        String[] tokens = trimmed.split("\t");
        ArrayList<String> entries = new ArrayList<String>();
        for (String token : tokens) {
            if (!StringUtil.hasText((CharSequence)token)) continue;
            entries.add(token);
        }
        tokens = entries.toArray(new String[0]);
        if (tokens.length > 0) {
            String tok = tokens[0].trim();
            try {
                this.checkBytesFrom = tok.startsWith("0x") ? Integer.parseInt(tok.substring(2), 16) : Integer.parseInt(tok);
            }
            catch (NumberFormatException e) {
                this.addEntry(trimmed.replaceAll(" {2}", "\t"));
                return;
            }
        }
        if (tokens.length > 1) {
            this.typeStr = tokens[1].trim();
            this.type = this.getType(this.typeStr);
        }
        if (tokens.length > 2) {
            this.content = this.ltrim(tokens[2]);
            this.content = MagicMimeEntry.stringWithEscapeSubstitutions(this.content);
        }
        if (tokens.length > 3) {
            this.mimeType = tokens[3].trim();
        }
        if (tokens.length > 4) {
            this.mimeEnc = tokens[4].trim();
        }
    }

    private String ltrim(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == ' ') continue;
            return s.substring(i);
        }
        return s;
    }

    private int getType(String tok) {
        if (tok.startsWith("string")) {
            return 1;
        }
        if (tok.startsWith("belong")) {
            return 2;
        }
        if (tok.equals("short")) {
            return 3;
        }
        if (tok.startsWith("lelong")) {
            return 4;
        }
        if (tok.startsWith("beshort")) {
            return 5;
        }
        if (tok.startsWith("leshort")) {
            return 6;
        }
        if (tok.equals("byte")) {
            return 7;
        }
        return 20;
    }

    public int getCheckBytesFrom() {
        return this.checkBytesFrom;
    }

    public int getType() {
        return this.type;
    }

    public String getContent() {
        return this.content;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public String getMatch(byte[] content) throws IOException {
        ByteBuffer buf = this.readBuffer(content);
        if (buf == null) {
            return null;
        }
        buf.position(0);
        boolean matches = this.match(buf);
        if (matches) {
            int subLen = this.subEntries.size();
            String myMimeType = this.getMimeType();
            if (subLen > 0) {
                for (MagicMimeEntry me : this.subEntries) {
                    String mtype = me.getMatch(content);
                    if (mtype == null) continue;
                    return mtype;
                }
                return myMimeType;
            }
            return myMimeType;
        }
        return null;
    }

    public String getMatch(RandomAccessFile raf) throws IOException {
        ByteBuffer buf = this.readBuffer(raf);
        if (buf == null) {
            return null;
        }
        boolean matches = this.match(buf);
        if (matches) {
            String myMimeType = this.getMimeType();
            if (!this.subEntries.isEmpty()) {
                for (MagicMimeEntry me : this.subEntries) {
                    String mtype = me.getMatch(raf);
                    if (mtype == null) continue;
                    return mtype;
                }
                return myMimeType;
            }
            return myMimeType;
        }
        return null;
    }

    private ByteBuffer readBuffer(byte[] content) {
        ByteBuffer buf;
        int startPos = this.getCheckBytesFrom();
        if (startPos > content.length) {
            return null;
        }
        if (1 == this.type) {
            int len = this.getContent().length();
            buf = ByteBuffer.allocate(len + 1);
            buf.put(content, startPos, len);
        } else if (3 == this.type || 6 == this.type || 5 == this.type) {
            buf = ByteBuffer.allocate(2);
            buf.put(content, startPos, 2);
        } else if (4 == this.type || 2 == this.type) {
            buf = ByteBuffer.allocate(4);
            buf.put(content, startPos, 4);
        } else if (7 == this.type) {
            buf = ByteBuffer.allocate(1);
            buf.put(buf.array(), startPos, 1);
        } else {
            buf = null;
        }
        return buf;
    }

    private ByteBuffer readBuffer(RandomAccessFile raf) throws IOException {
        ByteBuffer buf;
        int startPos = this.getCheckBytesFrom();
        if ((long)startPos > raf.length()) {
            return null;
        }
        raf.seek(startPos);
        if (1 == this.type) {
            int len;
            int index = this.typeStr.indexOf(62);
            if (index != -1) {
                len = Integer.parseInt(this.typeStr.substring(index + 1, this.typeStr.length() - 1));
                this.isBetween = true;
            } else {
                len = this.getContent().length();
            }
            buf = ByteBuffer.allocate(len + 1);
            raf.read(buf.array(), 0, len);
        } else if (3 == this.type || 6 == this.type || 5 == this.type) {
            buf = ByteBuffer.allocate(2);
            raf.read(buf.array(), 0, 2);
        } else if (4 == this.type || 2 == this.type) {
            buf = ByteBuffer.allocate(4);
            raf.read(buf.array(), 0, 4);
        } else if (7 == this.type) {
            buf = ByteBuffer.allocate(1);
            raf.read(buf.array(), 0, 1);
        } else {
            buf = null;
        }
        return buf;
    }

    private boolean match(ByteBuffer buf) {
        boolean matches;
        if (1 == this.type) {
            matches = this.matchString(buf);
        } else if (3 == this.type) {
            matches = this.matchShort(buf, ByteOrder.BIG_ENDIAN, false, (short)255);
        } else if (6 == this.type || 5 == this.type) {
            ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
            if (this.getType() == 6) {
                byteOrder = ByteOrder.LITTLE_ENDIAN;
            }
            boolean needMask = false;
            short sMask = 255;
            int indx = this.typeStr.indexOf(38);
            if (indx >= 0) {
                sMask = (short)Integer.parseInt(this.typeStr.substring(indx + 3), 16);
                needMask = true;
            } else if (this.getContent().startsWith("&")) {
                sMask = (short)Integer.parseInt(this.getContent().substring(3), 16);
                needMask = true;
            }
            matches = this.matchShort(buf, byteOrder, needMask, sMask);
        } else if (4 == this.type || 2 == this.type) {
            ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
            if (this.getType() == 4) {
                byteOrder = ByteOrder.LITTLE_ENDIAN;
            }
            boolean needMask = false;
            long lMask = -1L;
            int indx = this.typeStr.indexOf(38);
            if (indx >= 0) {
                lMask = Long.parseLong(this.typeStr.substring(indx + 3), 16);
                needMask = true;
            } else if (this.getContent().startsWith("&")) {
                lMask = Long.parseLong(this.getContent().substring(3), 16);
                needMask = true;
            }
            matches = this.matchLong(buf, byteOrder, needMask, lMask);
        } else {
            matches = 7 == this.type ? this.matchByte(buf) : false;
        }
        return matches;
    }

    private boolean matchString(ByteBuffer bbuf) {
        if (this.isBetween) {
            String buffer = new String(bbuf.array(), StandardCharsets.UTF_8);
            return buffer.contains(this.getContent());
        }
        int read = this.getContent().length();
        for (int j = 0; j < read; ++j) {
            if ((bbuf.get(j) & 0xFF) == this.getContent().charAt(j)) continue;
            return false;
        }
        return true;
    }

    private boolean matchByte(ByteBuffer bbuf) {
        byte b = bbuf.get(0);
        return b == this.getContent().charAt(0);
    }

    private boolean matchShort(ByteBuffer bbuf, ByteOrder bo, boolean needMask, short sMask) {
        bbuf.order(bo);
        String testContent = this.getContent();
        short got = testContent.startsWith("0x") ? (short)Integer.parseInt(testContent.substring(2), 16) : (testContent.startsWith("&") ? (short)Integer.parseInt(testContent.substring(3), 16) : (short)Integer.parseInt(testContent));
        short found = bbuf.getShort();
        if (needMask) {
            found = (short)(found & sMask);
        }
        return got == found;
    }

    private boolean matchLong(ByteBuffer bbuf, ByteOrder bo, boolean needMask, long lMask) {
        bbuf.order(bo);
        String testContent = this.getContent();
        long got = testContent.startsWith("0x") ? Long.parseLong(testContent.substring(2), 16) : (testContent.startsWith("&") ? Long.parseLong(testContent.substring(3), 16) : Long.parseLong(testContent));
        long found = Integer.toUnsignedLong(bbuf.getInt());
        if (needMask) {
            found &= lMask;
        }
        return got == found;
    }

    private static String stringWithEscapeSubstitutions(String s) {
        char c;
        StringBuilder ret = new StringBuilder();
        int len = s.length();
        for (int indx = 0; indx < len && (c = s.charAt(indx)) != '\n'; ++indx) {
            if (c == '\\') {
                if (++indx >= len) {
                    ret.append(c);
                    break;
                }
                char cn = s.charAt(indx);
                if (cn == '\\') {
                    ret.append('\\');
                    continue;
                }
                if (cn == ' ') {
                    ret.append(' ');
                    continue;
                }
                if (cn == 't') {
                    ret.append('\t');
                    continue;
                }
                if (cn == 'n') {
                    ret.append('\n');
                    continue;
                }
                if (cn == 'r') {
                    ret.append('\r');
                    continue;
                }
                if (cn >= '0' && cn <= '7') {
                    int escape = cn - 48;
                    if (++indx >= len) {
                        ret.append((char)escape);
                        break;
                    }
                    cn = s.charAt(indx);
                    if (cn >= '0' && cn <= '7') {
                        escape <<= 3;
                        escape |= cn - 48;
                        if (++indx >= len) {
                            ret.append((char)escape);
                            break;
                        }
                        cn = s.charAt(indx);
                        if (cn >= '0' && cn <= '7') {
                            escape <<= 3;
                            escape |= cn - 48;
                        } else {
                            --indx;
                        }
                    } else {
                        --indx;
                    }
                    ret.append((char)escape);
                    continue;
                }
                ret.append(cn);
                continue;
            }
            ret.append(c);
        }
        return new String(ret);
    }
}

