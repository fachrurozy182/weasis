/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.util.ByteUtils;

public class UIDUtils {
    private static final String UUID_ROOT = "2.25";
    private static final Pattern PATTERN = Pattern.compile("[012]((\\.0)|(\\.[1-9]\\d*))+");
    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static String root = "2.25";

    public static final String getRoot() {
        return root;
    }

    public static final void setRoot(String root) {
        UIDUtils.checkRoot(root);
        UIDUtils.root = root;
    }

    private static void checkRoot(String root) {
        if (root.length() > 24) {
            throw new IllegalArgumentException("root length > 24");
        }
        if (!UIDUtils.isValid(root)) {
            throw new IllegalArgumentException(root);
        }
    }

    public static boolean isValid(String uid) {
        return uid.length() <= 64 && PATTERN.matcher(uid).matches();
    }

    public static String createUID() {
        return UIDUtils.randomUID(root);
    }

    public static String createNameBasedUID(byte[] name) {
        return UIDUtils.nameBasedUID(name, root);
    }

    public static String createNameBasedUID(byte[] name, String root) {
        UIDUtils.checkRoot(root);
        return UIDUtils.nameBasedUID(name, root);
    }

    public static String createUID(String root) {
        UIDUtils.checkRoot(root);
        return UIDUtils.randomUID(root);
    }

    public static String createUIDIfNull(String uid) {
        return uid == null ? UIDUtils.randomUID(root) : uid;
    }

    public static String createUIDIfNull(String uid, String root) {
        UIDUtils.checkRoot(root);
        return uid == null ? UIDUtils.randomUID(root) : uid;
    }

    public static String remapUID(String uid) {
        return UIDUtils.nameBasedUID(uid.getBytes(ASCII), root);
    }

    public static String remapUID(String uid, String root) {
        UIDUtils.checkRoot(root);
        return UIDUtils.nameBasedUID(uid.getBytes(ASCII), root);
    }

    private static String randomUID(String root) {
        return UIDUtils.toUID(root, UUID.randomUUID());
    }

    private static String nameBasedUID(byte[] name, String root) {
        return UIDUtils.toUID(root, UUID.nameUUIDFromBytes(name));
    }

    private static String toUID(String root, UUID uuid) {
        byte[] b17 = new byte[17];
        ByteUtils.longToBytesBE(uuid.getMostSignificantBits(), b17, 1);
        ByteUtils.longToBytesBE(uuid.getLeastSignificantBits(), b17, 9);
        String uuidStr = new BigInteger(b17).toString();
        int rootlen = root.length();
        int uuidlen = uuidStr.length();
        char[] cs = new char[rootlen + uuidlen + 1];
        root.getChars(0, rootlen, cs, 0);
        cs[rootlen] = 46;
        uuidStr.getChars(0, uuidlen, cs, rootlen + 1);
        return new String(cs);
    }

    public static StringBuilder promptTo(String uid, StringBuilder sb) {
        return sb.append(uid).append(" - ").append(UID.nameOf(uid));
    }

    public static String[] findUIDs(String regex) {
        Pattern p = Pattern.compile(regex);
        Field[] fields = UID.class.getFields();
        String[] uids = new String[fields.length];
        int j = 0;
        for (int i = 0; i < fields.length; ++i) {
            Field field = fields[i];
            if (!p.matcher(field.getName()).matches()) continue;
            try {
                uids[j++] = (String)field.get(null);
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return Arrays.copyOf(uids, j);
    }

    public static int remapUIDs(Attributes attrs, Map<String, String> uidMap) {
        return UIDUtils.remapUIDs(attrs, uidMap, null);
    }

    public static int remapUIDs(Attributes attrs, Map<String, String> uidMap, Attributes modified) {
        Visitor visitor = new Visitor(uidMap, modified);
        try {
            attrs.accept(visitor, true);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return visitor.replaced;
    }

    private static class Visitor
    implements Attributes.Visitor {
        private final Map<String, String> uidMap;
        private final Attributes modified;
        private int replaced;
        private int rootSeqTag;

        public Visitor(Map<String, String> uidMap, Attributes modified) {
            this.uidMap = uidMap;
            this.modified = modified;
        }

        @Override
        public boolean visit(Attributes attrs, int tag, VR vr, Object val) {
            String[] ss;
            if (vr != VR.UI || val == Value.NULL) {
                if (attrs.isRoot()) {
                    this.rootSeqTag = vr == VR.SQ ? tag : 0;
                }
                return true;
            }
            if (val instanceof byte[]) {
                ss = attrs.getStrings(tag);
                Object object = val = ss.length == 1 ? ss[0] : ss;
            }
            if (val instanceof String[]) {
                ss = val;
                int c = 0;
                for (int i = 0; i < ss.length; ++i) {
                    String uid = this.uidMap.get(ss[i]);
                    if (uid == null) continue;
                    if (c++ == 0) {
                        this.modified(attrs, tag, vr, ss.clone());
                    }
                    ss[i] = uid;
                    ++this.replaced;
                }
            } else {
                String uid = this.uidMap.get(val);
                if (uid != null) {
                    this.modified(attrs, tag, vr, val);
                    attrs.setString(tag, VR.UI, uid);
                    ++this.replaced;
                }
            }
            return true;
        }

        private void modified(Attributes attrs, int tag, VR vr, Object val) {
            if (this.modified == null) {
                return;
            }
            if (attrs.isRoot()) {
                this.modified.setValue(tag, vr, val);
            } else if (!this.modified.contains(this.rootSeqTag)) {
                Sequence src = attrs.getRoot().getSequence(this.rootSeqTag);
                Sequence dst = this.modified.newSequence(this.rootSeqTag, src.size());
                for (Attributes item : src) {
                    dst.add(new Attributes(item));
                }
            }
        }
    }
}

