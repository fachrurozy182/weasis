/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.FileUtil;
import org.weasis.launcher.WeasisLauncher;

public class SwingResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwingResources.class);
    static final String AND_MNEMONIC = "AndMnemonic";
    static final String TITLE_SUFFIX = ".titleAndMnemonic";
    static final String TEXT_SUFFIX = ".textAndMnemonic";
    static final String KEY_MNEMONIC = "Mnemonic";
    static final String KEY_MNEMONIC_INDEX = "DisplayedMnemonicIndex";
    static final String KEY_TEXT = "Text";
    static final String KEY_TITLE = "Title";

    private SwingResources() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadResources(String path) {
        InputStream inStream = WeasisLauncher.class.getResourceAsStream(path);
        if (inStream != null) {
            Properties swingDialogs = new Properties();
            try {
                swingDialogs.load(inStream);
            }
            catch (IOException e) {
                LOGGER.error("Cannot read swing translations", e);
            }
            finally {
                FileUtil.safeClose(inStream);
            }
            for (Map.Entry<Object, Object> entry : swingDialogs.entrySet()) {
                String stringKey = entry.getKey().toString();
                Object value = entry.getValue();
                if (stringKey.endsWith(AND_MNEMONIC)) {
                    String compositeKey;
                    if (value == null) continue;
                    String text = value.toString();
                    String mnemonic = null;
                    int index = text.indexOf(38);
                    if (0 <= index && index < text.length() - 1) {
                        char c = text.charAt(index + 1);
                        mnemonic = Integer.toString(Character.toUpperCase(c));
                    }
                    if (stringKey.endsWith(TEXT_SUFFIX)) {
                        compositeKey = SwingResources.composeKey(stringKey, TEXT_SUFFIX.length(), KEY_TEXT);
                        UIManager.put(compositeKey, SwingResources.getTextFromProperty(text));
                        if (mnemonic == null) continue;
                        if (stringKey.startsWith("ColorChooser")) {
                            compositeKey = SwingResources.composeKey(stringKey, TEXT_SUFFIX.length(), "NameText");
                            UIManager.put(compositeKey, SwingResources.getTextFromProperty(text));
                        }
                        compositeKey = SwingResources.composeKey(stringKey, TEXT_SUFFIX.length(), KEY_MNEMONIC);
                        UIManager.put(compositeKey, mnemonic);
                        compositeKey = SwingResources.composeKey(stringKey, TEXT_SUFFIX.length(), KEY_MNEMONIC_INDEX);
                        UIManager.put(compositeKey, Integer.toString(index));
                        continue;
                    }
                    if (!stringKey.endsWith(TITLE_SUFFIX)) continue;
                    compositeKey = SwingResources.composeKey(stringKey, TITLE_SUFFIX.length(), KEY_TITLE);
                    UIManager.put(compositeKey, SwingResources.getTextFromProperty(text));
                    if (mnemonic == null) continue;
                    compositeKey = SwingResources.composeKey(stringKey, TITLE_SUFFIX.length(), KEY_MNEMONIC);
                    UIManager.put(compositeKey, mnemonic);
                    compositeKey = SwingResources.composeKey(stringKey, TITLE_SUFFIX.length(), KEY_MNEMONIC_INDEX);
                    UIManager.put(compositeKey, Integer.toString(index));
                    continue;
                }
                UIManager.put(entry.getKey(), value);
            }
        }
    }

    static String composeKey(String key, int reduce, String sufix) {
        return key.substring(0, key.length() - reduce) + sufix;
    }

    static String getTextFromProperty(String text) {
        return text.replace("&", "");
    }

    static String getMnemonicFromProperty(String text) {
        int index = text.indexOf(38);
        if (0 <= index && index < text.length() - 1) {
            char c = text.charAt(index + 1);
            return Integer.toString(Character.toUpperCase(c));
        }
        return null;
    }
}

