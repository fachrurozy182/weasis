/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import java.io.File;

public class FileTestUtil {
    public static void makeTestOutputDir() {
        File target = new File("target/");
        if (target.exists() && target.isDirectory()) {
            boolean result;
            File testoutput = new File("target/test-output/");
            if (!testoutput.exists() && !(result = testoutput.mkdir())) {
                throw new IllegalStateException("Failed to create " + String.valueOf(testoutput));
            }
        } else {
            throw new IllegalStateException("target/ does not exist");
        }
    }
}

