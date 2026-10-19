/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import ch.qos.logback.core.rolling.helper.TimeBasedArchiveRemover;
import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SizeAndTimeBasedArchiveRemover
extends TimeBasedArchiveRemover {
    protected static final int NO_INDEX = -1;

    public SizeAndTimeBasedArchiveRemover(FileNamePattern fileNamePattern, RollingCalendar rc) {
        super(fileNamePattern, rc);
    }

    @Override
    protected File[] getFilesInPeriod(Instant instantOfPeriodToClean) {
        File archive0 = new File(this.fileNamePattern.convertMultipleArguments(instantOfPeriodToClean, 0));
        File parentDir = this.getParentDir(archive0);
        String stemRegex = this.createStemRegex(instantOfPeriodToClean);
        File[] matchingFileArray = FileFilterUtil.filesInFolderMatchingStemRegex(parentDir, stemRegex);
        return matchingFileArray;
    }

    @Override
    protected void descendingSort(File[] matchingFileArray, Instant instant) {
        String regexForIndexExtreaction = this.createStemRegex(instant);
        final Pattern pattern = Pattern.compile(regexForIndexExtreaction);
        Arrays.sort(matchingFileArray, new Comparator<File>(){

            @Override
            public int compare(File f1, File f2) {
                int index2;
                int index1 = this.extractIndex(pattern, f1);
                if (index1 == (index2 = this.extractIndex(pattern, f2))) {
                    return 0;
                }
                if (index2 < index1) {
                    return -1;
                }
                return 1;
            }

            private int extractIndex(Pattern pattern2, File f1) {
                Matcher matcher = pattern2.matcher(f1.getName());
                if (matcher.find()) {
                    String indexAsStr = matcher.group(1);
                    if (indexAsStr == null || indexAsStr.isEmpty()) {
                        return -1;
                    }
                    return Integer.parseInt(indexAsStr);
                }
                return -1;
            }
        });
    }

    private String createStemRegex(Instant instantOfPeriodToClean) {
        String regex = this.fileNamePattern.toRegexForFixedDate(instantOfPeriodToClean);
        return FileFilterUtil.afterLastSlash(regex);
    }
}

