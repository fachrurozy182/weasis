/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import org.dcm4che3.data.Tag;
import org.dcm4che3.util.StringUtils;

public class TagUtils {
    private static char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final byte[] INV_HEX_DIGITS = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15};

    public static String shortToHexString(int n) {
        char[] s = new char[]{HEX_DIGITS[n >>> 12 & 0xF], HEX_DIGITS[n >>> 8 & 0xF], HEX_DIGITS[n >>> 4 & 0xF], HEX_DIGITS[n >>> 0 & 0xF]};
        return new String(s);
    }

    public static String toHexString(int tag) {
        char[] s = new char[]{HEX_DIGITS[tag >>> 28], HEX_DIGITS[tag >>> 24 & 0xF], HEX_DIGITS[tag >>> 20 & 0xF], HEX_DIGITS[tag >>> 16 & 0xF], HEX_DIGITS[tag >>> 12 & 0xF], HEX_DIGITS[tag >>> 8 & 0xF], HEX_DIGITS[tag >>> 4 & 0xF], HEX_DIGITS[tag >>> 0 & 0xF]};
        return new String(s);
    }

    public static String[] toHexStrings(int[] vals) {
        int n = vals.length;
        String[] ss = new String[n];
        for (int i = 0; i < n; ++i) {
            ss[i] = TagUtils.toHexString(vals[i]);
        }
        return ss;
    }

    public static String toHexString(byte[] b) {
        char[] s = new char[b.length << 1];
        int j = 0;
        for (int i = 0; i < b.length; ++i) {
            s[j++] = HEX_DIGITS[b[i] >>> 4 & 0xF];
            s[j++] = HEX_DIGITS[b[i] & 0xF];
        }
        return new String(s);
    }

    public static byte[] fromHexString(String s) {
        char[] chars = s.toCharArray();
        byte[] b = new byte[chars.length / 2];
        try {
            int j = 0;
            for (int i = 0; i < b.length; ++i) {
                b[i] = (byte)(INV_HEX_DIGITS[chars[j++]] << 4 | INV_HEX_DIGITS[chars[j++]]);
            }
        }
        catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(s);
        }
        return b;
    }

    public static int intFromHexString(String s) {
        char[] chars = s.toCharArray();
        int val = 0;
        for (int i = 0; i < chars.length; ++i) {
            val = val << 4 | INV_HEX_DIGITS[chars[i]];
        }
        return val;
    }

    public static int[] fromHexStrings(String[] ss) {
        int n = ss.length;
        int[] vals = new int[n];
        for (int i = 0; i < n; ++i) {
            vals[i] = TagUtils.intFromHexString(ss[i]);
        }
        return vals;
    }

    public static String toString(int tag) {
        char[] s = new char[]{'(', HEX_DIGITS[tag >>> 28], HEX_DIGITS[tag >>> 24 & 0xF], HEX_DIGITS[tag >>> 20 & 0xF], HEX_DIGITS[tag >>> 16 & 0xF], ',', HEX_DIGITS[tag >>> 12 & 0xF], HEX_DIGITS[tag >>> 8 & 0xF], HEX_DIGITS[tag >>> 4 & 0xF], HEX_DIGITS[tag >>> 0 & 0xF], ')'};
        return new String(s);
    }

    public static int groupNumber(int tag) {
        return tag >>> 16;
    }

    public static int elementNumber(int tag) {
        return tag & 0xFFFF;
    }

    public static boolean isGroupLength(int tag) {
        return TagUtils.elementNumber(tag) == 0;
    }

    public static boolean isPrivateCreator(int tag) {
        return (tag & 0x10000) != 0 && (tag & 0xFF00) == 0 && (tag & 0xF0) != 0;
    }

    public static boolean isPrivateGroup(int tag) {
        return (tag & 0x10000) != 0;
    }

    public static boolean isPrivateTag(int tag) {
        return (tag & 0x10000) != 0 && (tag & 0xFF00) != 0;
    }

    public static int toTag(int groupNumber, int elementNumber) {
        return groupNumber << 16 | elementNumber;
    }

    public static int toPrivateTag(int creatorTag, int elementNumber) {
        return creatorTag & 0xFFFF0000 | ((creatorTag & 0xFF) << 8 | elementNumber & 0xFF);
    }

    public static int creatorTagOf(int tag) {
        return tag & 0xFFFF0000 | tag >>> 8 & 0xFF;
    }

    public static int groupLengthTagOf(int tag) {
        return tag & 0xFFFF0000;
    }

    public static boolean isItem(int tag) {
        return tag == -73728 || tag == -73715 || tag == -73507;
    }

    public static boolean isFileMetaInformation(int tag) {
        return (tag & 0xFFFF0000) == 131072;
    }

    public static int normalizeRepeatingGroup(int tag) {
        int gg000000 = tag & 0xFFE00000;
        return gg000000 == 0x50000000 || gg000000 == 0x60000000 ? tag & 0xFFE0FFFF : tag;
    }

    public static int forName(String name) {
        try {
            return Integer.parseInt(name, 16);
        }
        catch (NumberFormatException nfe) {
            try {
                return Tag.class.getField(name).getInt(null);
            }
            catch (Exception e) {
                return -1;
            }
        }
    }

    public static int[] parseTagPath(String tagPath) {
        String[] names = StringUtils.split(tagPath, '.');
        int[] tags = new int[names.length];
        for (int i = 0; i < tags.length; ++i) {
            tags[i] = TagUtils.forName(names[i]);
            if (tags[i] != -1) continue;
            throw new IllegalArgumentException("tagPath: " + tagPath);
        }
        return tags;
    }

    public static enum Type {
        STANDARD,
        PRIVATE,
        PRIVATE_CREATOR;


        public static Type typeOf(int tag) {
            return (tag & 0x10000) != 0 ? ((tag & 0xFF00) != 0 ? PRIVATE : PRIVATE_CREATOR) : STANDARD;
        }
    }
}

