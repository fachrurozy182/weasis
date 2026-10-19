/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.tool.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;

public final class CLIUtils {
    private CLIUtils() {
    }

    public static Properties loadProperties(String url, Properties properties) throws IOException {
        Properties props = Objects.requireNonNullElseGet(properties, Properties::new);
        try (InputStream in = StreamUtils.openFileOrURL(url);){
            props.load(in);
        }
        return props;
    }

    public static int[] toTags(String[] tagOrKeywords) {
        return Arrays.stream(tagOrKeywords).mapToInt(CLIUtils::toTag).toArray();
    }

    public static int toTag(String tagOrKeyword) {
        try {
            return Integer.parseInt(tagOrKeyword, 16);
        }
        catch (NumberFormatException e) {
            int tag = ElementDictionary.tagForKeyword(tagOrKeyword, null);
            if (tag == -1) {
                throw new IllegalArgumentException("Unknown tag or keyword: " + tagOrKeyword, e);
            }
            return tag;
        }
    }

    public static void addAttributes(Attributes attrs, int[] tags, String ... values) {
        if (tags.length == 0) {
            return;
        }
        Attributes targetItem = CLIUtils.navigateToTargetItem(attrs, tags);
        CLIUtils.setAttributeValue(targetItem, tags[tags.length - 1], values);
    }

    private static Attributes navigateToTargetItem(Attributes attrs, int[] tags) {
        Attributes item = attrs;
        for (int i = 0; i < tags.length - 1; ++i) {
            item = CLIUtils.getOrCreateSequenceItem(item, tags[i]);
        }
        return item;
    }

    private static Attributes getOrCreateSequenceItem(Attributes item, int tag) {
        Sequence sequence = item.getSequence(tag);
        if (sequence == null) {
            sequence = item.newSequence(tag, 1);
        }
        if (sequence.isEmpty()) {
            sequence.add(new Attributes());
        }
        return (Attributes)sequence.get(0);
    }

    private static void setAttributeValue(Attributes item, int tag, String[] values) {
        VR vr = ElementDictionary.vrOf(tag, item.getPrivateCreator(tag));
        if (CLIUtils.isEmptyValue(values)) {
            if (vr == VR.SQ) {
                item.newSequence(tag, 1).add(new Attributes(0));
            } else {
                item.setNull(tag, vr);
            }
        } else {
            item.setString(tag, vr, values);
        }
    }

    private static boolean isEmptyValue(String[] values) {
        return values.length == 0 || values.length == 1 && values[0].isEmpty();
    }

    public static void addAttributes(Attributes attrs, String[] optVals) {
        if (optVals == null) {
            return;
        }
        Arrays.stream(optVals).forEach(optVal -> CLIUtils.processOptionValue(attrs, optVal));
    }

    private static void processOptionValue(Attributes attrs, String optVal) {
        int delimIndex = optVal.indexOf(61);
        if (delimIndex < 0) {
            CLIUtils.addAttributes(attrs, CLIUtils.toTags(StringUtils.split(optVal, '/')), new String[0]);
        } else {
            String tagPath = optVal.substring(0, delimIndex);
            String value = optVal.substring(delimIndex + 1);
            CLIUtils.addAttributes(attrs, CLIUtils.toTags(StringUtils.split(tagPath, '/')), value);
        }
    }

    public static void addEmptyAttributes(Attributes attrs, String[] optVals) {
        if (optVals == null) {
            return;
        }
        Arrays.stream(optVals).map(optVal -> StringUtils.split(optVal, '/')).map(CLIUtils::toTags).forEach(tags -> CLIUtils.addAttributes(attrs, tags, new String[0]));
    }

    public static boolean updateAttributes(Attributes data, Attributes attrs, String uidSuffix) {
        if (attrs.isEmpty() && uidSuffix == null) {
            return false;
        }
        if (uidSuffix != null) {
            CLIUtils.updateInstanceUIDs(data, uidSuffix);
        }
        data.update(Attributes.UpdatePolicy.OVERWRITE, attrs, null);
        return true;
    }

    private static void updateInstanceUIDs(Attributes data, String uidSuffix) {
        CLIUtils.updateUID(data, 0x20000D, uidSuffix);
        CLIUtils.updateUID(data, 0x20000E, uidSuffix);
        CLIUtils.updateUID(data, 524312, uidSuffix);
    }

    private static void updateUID(Attributes data, int tag, String suffix) {
        String currentUID = data.getString(tag);
        if (currentUID != null) {
            data.setString(tag, VR.UI, currentUID + suffix);
        }
    }

    public static String[] toUIDs(String uidString) {
        if ("*".equals(uidString)) {
            return new String[]{"*"};
        }
        return (String[])Arrays.stream(StringUtils.split(uidString, ',')).map(CLIUtils::toUID).toArray(String[]::new);
    }

    public static String toUID(String uid) {
        String trimmedUID = uid.trim();
        return "*".equals(trimmedUID) || Character.isDigit(trimmedUID.charAt(0)) ? trimmedUID : UID.forName(trimmedUID);
    }
}

