/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.SeriesImporter;

public interface MediaSeries<E>
extends MediaSeriesGroup,
Transferable {
    public List<E> getSortedMedias(Comparator<E> var1);

    public void addMedia(E var1);

    public void add(E var1);

    public void add(int var1, E var2);

    public void addAll(Collection<? extends E> var1);

    public void addAll(int var1, Collection<? extends E> var2);

    public E getMedia(MEDIA_POSITION var1, Filter<E> var2, Comparator<E> var3);

    public Iterable<E> getMedias(Filter<E> var1, Comparator<E> var2);

    public List<E> copyOfMedias(Filter<E> var1, Comparator<E> var2);

    public E getMedia(int var1, Filter<E> var2, Comparator<E> var3);

    public MediaElement getFirstSpecialElement();

    @Override
    public void dispose();

    public int size(Filter<E> var1);

    public SeriesImporter getSeriesLoader();

    public void setSeriesLoader(SeriesImporter var1);

    public String getToolTips();

    public String getSeriesNumber();

    public boolean isOpen();

    public boolean isSelected();

    public boolean isFocused();

    public String getMimeType();

    public void setOpen(boolean var1);

    public void setSelected(boolean var1, E var2);

    public void setFocused(boolean var1);

    public E getNearestImage(double var1, int var3, Filter<E> var4, Comparator<E> var5);

    public int getNearestImageIndex(double var1, int var3, Filter<E> var4, Comparator<E> var5);

    public long getFileSize();

    default public boolean isSuitableFor3d() {
        return false;
    }

    public static enum MEDIA_POSITION {
        FIRST,
        MIDDLE,
        LAST,
        RANDOM;

    }
}

