/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.CoreConstants;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFilterUtil {
    public static void sortFileArrayByName(File[] fileArray) {
        Arrays.sort(fileArray, new Comparator<File>(){

            @Override
            public int compare(File o1, File o2) {
                String o1Name = o1.getName();
                String o2Name = o2.getName();
                return o1Name.compareTo(o2Name);
            }
        });
    }

    public static void reverseSortFileArrayByName(File[] fileArray) {
        Arrays.sort(fileArray, new Comparator<File>(){

            @Override
            public int compare(File o1, File o2) {
                String o1Name = o1.getName();
                String o2Name = o2.getName();
                return o2Name.compareTo(o1Name);
            }
        });
    }

    public static String afterLastSlash(String sregex) {
        int i = sregex.lastIndexOf(47);
        if (i == -1) {
            return sregex;
        }
        return sregex.substring(i + 1);
    }

    public static boolean isEmptyDirectory(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("[" + String.valueOf(dir) + "] must be a directory");
        }
        String[] filesInDir = dir.list();
        return filesInDir == null || filesInDir.length == 0;
    }

    public static File[] filesInFolderMatchingStemRegex(File file, String stemRegex) {
        if (file == null) {
            return CoreConstants.EMPTY_FILE_ARRAY;
        }
        if (!file.isDirectory()) {
            return CoreConstants.EMPTY_FILE_ARRAY;
        }
        Pattern pattern = Pattern.compile(stemRegex);
        return file.listFiles((dir, name) -> pattern.matcher(name).matches());
    }

    public static int findHighestCounter(File[] matchingFileArray, String stemRegex) {
        int max = Integer.MIN_VALUE;
        for (File aFile : matchingFileArray) {
            int aCounter = FileFilterUtil.extractCounter(aFile, stemRegex);
            if (max >= aCounter) continue;
            max = aCounter;
        }
        return max;
    }

    public static int extractCounter(File file, String stemRegex) {
        String lastFileName;
        Pattern p = Pattern.compile(stemRegex);
        Matcher m = p.matcher(lastFileName = file.getName());
        if (!m.matches()) {
            throw new IllegalStateException("The regex [" + stemRegex + "] should match [" + lastFileName + "]");
        }
        String counterAsStr = m.group(1);
        return Integer.parseInt(counterAsStr);
    }

    public static String slashify(String in) {
        return in.replace('\\', '/');
    }

    public static void removeEmptyParentDirectories(File file, int recursivityCount) {
        if (recursivityCount >= 3) {
            return;
        }
        File parent = file.getParentFile();
        if (parent.isDirectory() && FileFilterUtil.isEmptyDirectory(parent)) {
            parent.delete();
            FileFilterUtil.removeEmptyParentDirectories(parent, recursivityCount + 1);
        }
    }
}

