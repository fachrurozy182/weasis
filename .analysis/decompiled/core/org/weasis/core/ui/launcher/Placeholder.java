/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.launcher;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.service.UICore;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.util.StringUtil;

public class Placeholder {
    private static final Logger LOGGER = LoggerFactory.getLogger(Placeholder.class);
    public static final String DICOM_WADO_FOLDER = "wado.folder";
    public static final String DICOM_QR_FOLDER = "qr.folder";
    public static final String DICOM_LAST_FOLDER = "last.folder";
    public static final String DICOM_COPY_FOLDER = "selection.folder";
    public static final Placeholder PREFERENCES_PLACEHOLDER = new Placeholder("\\{pref:(\\S+)}", (pref, imageViewerEventManager) -> {
        String val = UICore.getInstance().getSystemPreferences().getProperty((String)pref);
        if (!StringUtil.hasText((CharSequence)val)) {
            val = UICore.getInstance().getLocalPersistence().getProperty((String)pref);
        }
        return val;
    });
    public static final Placeholder DICOM_COPY_FOLDER_PLACEHOLDER = new Placeholder("\\{dicom:selection.folder}", (path, imageViewerEventManager) -> path);
    private final String patternString;
    private final BiFunction<String, ImageViewerEventManager<?>, String> biFunction;

    public Placeholder(String patternString, BiFunction<String, ImageViewerEventManager<?>, String> biFunction) {
        this.patternString = Objects.requireNonNull(patternString);
        this.biFunction = Objects.requireNonNull(biFunction);
    }

    public String resolvePlaceholders(String template, ImageViewerEventManager<?> eventManager) {
        Pattern pattern = Pattern.compile(this.patternString);
        Matcher matcher = pattern.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = this.biFunction.apply(placeholder, eventManager);
            if (replacement == null) {
                replacement = "";
                LOGGER.warn("Placeholder '{}' not found", (Object)placeholder);
            } else {
                replacement = Matcher.quoteReplacement(replacement);
            }
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

