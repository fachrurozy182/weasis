/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.model;

public class PerformanceModel {
    private String type;
    private String seriesUID;
    private String modality;
    private int nbImages;
    private long size;
    private long time;
    private String rate;
    private int errors;

    public PerformanceModel(String type, String seriesUID, String modality, int nbImages, long size, long time, String rate, int errors) {
        this.type = type;
        this.seriesUID = seriesUID;
        this.modality = modality;
        this.nbImages = nbImages;
        this.size = size;
        this.time = time;
        this.rate = rate;
        this.errors = errors;
    }

    public int getErrors() {
        return this.errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeriesUID() {
        return this.seriesUID;
    }

    public void setSeriesUID(String seriesUID) {
        this.seriesUID = seriesUID;
    }

    public String getModality() {
        return this.modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public int getNbImages() {
        return this.nbImages;
    }

    public void setNbImages(int nbImages) {
        this.nbImages = nbImages;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getRate() {
        return this.rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}

