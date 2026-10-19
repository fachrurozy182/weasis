/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.TagD
 */
package org.weasis.dicom.explorer.main;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.Messages;

public class SeriesFilter {
    private static final int[] SERIES_TEXT_TAGS = new int[]{528446, 524384, 1572885, 1577008, 0x20000E};
    private static final int[] STUDY_TEXT_TAGS = new int[]{528432, 0x200010, 524368, 0x20000D};
    private Mode mode = Mode.TEXT;
    private String text = "";
    private final Set<String> modalities = new LinkedHashSet<String>();
    private MediaSeriesGroup study;

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = Objects.requireNonNull(mode);
        this.clear();
    }

    public boolean isActive() {
        return switch (this.mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (!this.text.isEmpty()) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                if (this.study != null) {
                    yield true;
                }
                yield false;
            }
            case 2 -> !this.modalities.isEmpty();
        };
    }

    public String getText() {
        return this.text;
    }

    public void setText(String value) {
        this.text = value == null ? "" : value.trim().toLowerCase();
    }

    public Set<String> getModalities() {
        return Set.copyOf(this.modalities);
    }

    public void setModalities(Collection<String> values) {
        this.modalities.clear();
        if (values != null) {
            values.stream().filter(StringUtil::hasText).forEach(this.modalities::add);
        }
    }

    public MediaSeriesGroup getStudy() {
        return this.study;
    }

    public void setStudy(MediaSeriesGroup study) {
        this.study = study;
    }

    public void clear() {
        this.text = "";
        this.modalities.clear();
        this.study = null;
    }

    public boolean test(MediaSeriesGroup series, MediaSeriesGroup study) {
        if (series == null) {
            return false;
        }
        return switch (this.mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this.text.isEmpty() || this.matchesText(series, study)) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                if (this.study == null || this.study.equals((Object)study)) {
                    yield true;
                }
                yield false;
            }
            case 2 -> this.modalities.isEmpty() || this.modalities.contains(SeriesFilter.modalityOf(series));
        };
    }

    private boolean matchesText(MediaSeriesGroup series, MediaSeriesGroup study) {
        for (int tag : SERIES_TEXT_TAGS) {
            if (!this.contains((String)TagD.getTagValue((TagReadable)series, (int)tag, String.class))) continue;
            return true;
        }
        Integer number = (Integer)TagD.getTagValue((TagReadable)series, (int)0x200011, Integer.class);
        if (number != null && this.contains(number.toString())) {
            return true;
        }
        for (int tag : STUDY_TEXT_TAGS) {
            if (!this.contains((String)TagD.getTagValue((TagReadable)study, (int)tag, String.class))) continue;
            return true;
        }
        return false;
    }

    private static String modalityOf(MediaSeriesGroup series) {
        return (String)TagD.getTagValue((TagReadable)series, (int)524384, String.class);
    }

    private boolean contains(String value) {
        return value != null && value.toLowerCase().contains(this.text);
    }

    public static enum Mode {
        TEXT(Messages.getString("DicomExplorer.filter_mode_text")),
        DATE(Messages.getString("DicomExplorer.filter_mode_date")),
        MODALITY(Messages.getString("DicomExplorer.filter_mode_modality"));

        private final String title;

        private Mode(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        public String toString() {
            return this.title;
        }
    }
}

