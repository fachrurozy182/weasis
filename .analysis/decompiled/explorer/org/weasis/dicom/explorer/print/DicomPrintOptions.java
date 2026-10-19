/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.ui.util.PrintOptions
 *  org.weasis.core.ui.util.PrintOptions$DotPerInches
 */
package org.weasis.dicom.explorer.print;

import org.weasis.core.ui.util.PrintOptions;
import org.weasis.dicom.explorer.print.DicomPrintDialog;

public class DicomPrintOptions
extends PrintOptions {
    public static final String DEF_MEDIUM_TYPE = "BLUE FILM";
    public static final String DEF_PRIORITY = "LOW";
    public static final String DEF_FILM_DEST = "MAGAZINE";
    public static final int DEF_NUM_COPIES = 1;
    public static final boolean DEF_COLOR = false;
    public static final String DEF_FILM_ORIENTATION = "PORTRAIT";
    public static final String DEF_IMG_DISP_FORMAT = "STANDARD\\1,1";
    public static final DicomPrintDialog.FilmSize DEF_FILM_SIZE = DicomPrintDialog.FilmSize.IN8X10;
    public static final String DEF_MAGNIFICATION_TYPE = "CUBIC";
    public static final String DEF_SMOOTHING_TYPE = "MEDIUM";
    public static final String DEF_BORDER_DENSITY = "WHITE";
    public static final String DEF_TRIM = "NO";
    public static final String DEF_EMPTY_DENSITY = "BLACK";
    public static final boolean DEF_SHOW_ANNOTATIONS = true;
    public static final boolean DEF_PRINT_SEL_VIEW = false;
    public static final PrintOptions.DotPerInches DEF_DPI = PrintOptions.DotPerInches.DPI_150;
    private String mediumType = "BLUE FILM";
    private String priority = "LOW";
    private String filmDestination = "MAGAZINE";
    private int numOfCopies = 1;
    private String filmOrientation;
    private DicomPrintDialog.FilmSize filmSizeId;
    private String imageDisplayFormat;
    private String magnificationType;
    private String smoothingType;
    private String borderDensity;
    private String trim;
    private String emptyDensity;
    private int minDensity;
    private int maxDensity;
    private boolean printOnlySelectedView;

    public DicomPrintOptions() {
        this.setColorPrint(false);
        this.filmOrientation = DEF_FILM_ORIENTATION;
        this.filmSizeId = DEF_FILM_SIZE;
        this.imageDisplayFormat = DEF_IMG_DISP_FORMAT;
        this.magnificationType = DEF_MAGNIFICATION_TYPE;
        this.smoothingType = DEF_SMOOTHING_TYPE;
        this.borderDensity = DEF_BORDER_DENSITY;
        this.trim = DEF_TRIM;
        this.emptyDensity = DEF_EMPTY_DENSITY;
        this.minDensity = 0;
        this.maxDensity = 255;
        this.setShowingAnnotations(true);
        this.printOnlySelectedView = false;
        this.setDpi(DEF_DPI);
    }

    public String getBorderDensity() {
        return this.borderDensity;
    }

    public void setBorderDensity(String borderDensity) {
        this.borderDensity = borderDensity;
    }

    public String getEmptyDensity() {
        return this.emptyDensity;
    }

    public void setEmptyDensity(String emptyDensity) {
        this.emptyDensity = emptyDensity;
    }

    public String getFilmDestination() {
        return this.filmDestination;
    }

    public void setFilmDestination(String filmDestination) {
        this.filmDestination = filmDestination;
    }

    public String getFilmOrientation() {
        return this.filmOrientation;
    }

    public void setFilmOrientation(String filmOrientation) {
        this.filmOrientation = filmOrientation;
    }

    public DicomPrintDialog.FilmSize getFilmSizeId() {
        return this.filmSizeId;
    }

    public void setFilmSizeId(DicomPrintDialog.FilmSize filmSize) {
        this.filmSizeId = filmSize == null ? DEF_FILM_SIZE : filmSize;
    }

    public String getImageDisplayFormat() {
        return this.imageDisplayFormat;
    }

    public void setImageDisplayFormat(String imageDisplayFormat) {
        this.imageDisplayFormat = imageDisplayFormat;
    }

    public String getMagnificationType() {
        return this.magnificationType;
    }

    public void setMagnificationType(String magnificationType) {
        this.magnificationType = magnificationType;
    }

    public int getMaxDensity() {
        return this.maxDensity;
    }

    public void setMaxDensity(int maxDensity) {
        this.maxDensity = maxDensity;
    }

    public String getMediumType() {
        return this.mediumType;
    }

    public void setMediumType(String mediumType) {
        this.mediumType = mediumType;
    }

    public int getMinDensity() {
        return this.minDensity;
    }

    public void setMinDensity(int minDensity) {
        this.minDensity = minDensity;
    }

    public int getNumOfCopies() {
        return this.numOfCopies;
    }

    public void setNumOfCopies(int numOfCopies) {
        this.numOfCopies = numOfCopies;
    }

    public String getPriority() {
        return this.priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSmoothingType() {
        return this.smoothingType;
    }

    public void setSmoothingType(String smoothingType) {
        this.smoothingType = smoothingType;
    }

    public String getTrim() {
        return this.trim;
    }

    public void setTrim(String trim) {
        this.trim = trim;
    }

    public boolean isPrintOnlySelectedView() {
        return this.printOnlySelectedView;
    }

    public void setPrintOnlySelectedView(boolean printOnlySelectedView) {
        this.printOnlySelectedView = printOnlySelectedView;
    }
}

