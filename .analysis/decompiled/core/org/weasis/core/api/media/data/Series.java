/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.media.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.SeriesImporter;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.util.LangUtil;
import org.weasis.opencv.data.PlanarImage;

public abstract class Series<E extends MediaElement>
extends MediaSeriesGroupNode
implements MediaSeries<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Series.class);
    public static final DataFlavor sequenceDataFlavor = Series.createConstant("application/x-java-jvm-local-objectref;class=" + Series.class.getName(), null);
    private static final Random RANDOM = new SecureRandom();
    private static final DataFlavor[] flavors = new DataFlavor[]{sequenceDataFlavor};
    private PropertyChangeSupport propertyChange = null;
    protected final List<E> medias;
    protected final Map<Comparator<E>, List<E>> sortedMedias = new HashMap<Comparator<E>, List<E>>(6);
    protected final Comparator<E> mediaOrder;
    protected SeriesImporter seriesLoader;
    private long fileSize;

    protected Series(TagW tagID, Object identifier, TagView displayTag) {
        this(tagID, identifier, displayTag, null);
    }

    protected Series(TagW tagID, Object identifier, TagView displayTag, int initialCapacity) {
        this(tagID, identifier, displayTag, new ArrayList(initialCapacity));
    }

    protected Series(TagW tagID, Object identifier, TagView displayTag, List<E> list) {
        this(tagID, identifier, displayTag, list, null);
    }

    protected Series(TagW tagID, Object identifier, TagView displayTag, List<E> list, Comparator<E> mediaOrder) {
        super(tagID, identifier, displayTag);
        this.mediaOrder = mediaOrder;
        List<E> ls = list;
        if (ls == null) {
            ls = new ArrayList();
            this.fileSize = 0L;
        } else if (mediaOrder != null) {
            ls.sort(mediaOrder);
        }
        this.medias = Collections.synchronizedList(ls);
    }

    private static DataFlavor createConstant(String mt, String prn) {
        try {
            return new DataFlavor(mt, prn, Series.class.getClassLoader());
        }
        catch (Exception e) {
            LOGGER.error("Build series flavor", (Throwable)e);
            return null;
        }
    }

    protected void resetSortedMediasMap() {
        if (!this.sortedMedias.isEmpty()) {
            this.sortedMedias.clear();
        }
    }

    public void removeAllMedias() {
        this.medias.clear();
        this.resetSortedMediasMap();
    }

    @Override
    public List<E> getSortedMedias(Comparator<E> comparator) {
        if (comparator != null && !comparator.equals(this.mediaOrder)) {
            return this.sortedMedias.computeIfAbsent(comparator, k -> {
                ArrayList<E> sorted = new ArrayList<E>(this.medias);
                sorted.sort(comparator);
                return sorted;
            });
        }
        return this.medias;
    }

    @Override
    public void add(E media) {
        this.medias.add(media);
        this.resetSortedMediasMap();
    }

    @Override
    public void add(int index, E media) {
        this.medias.add(index, media);
        this.resetSortedMediasMap();
    }

    @Override
    public void addAll(Collection<? extends E> c) {
        this.medias.addAll(c);
        this.resetSortedMediasMap();
    }

    @Override
    public void addAll(int index, Collection<? extends E> c) {
        this.medias.addAll(index, c);
        this.resetSortedMediasMap();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final E getMedia(MediaSeries.MEDIA_POSITION position, Filter<E> filter, Comparator<E> sort) {
        List<E> sortedList = this.getSortedMedias(sort);
        Series series = this;
        synchronized (series) {
            if (filter == null) {
                int size = sortedList.size();
                if (size == 0) {
                    return null;
                }
                int pos = 0;
                if (MediaSeries.MEDIA_POSITION.FIRST.equals((Object)position)) {
                    pos = 0;
                } else if (MediaSeries.MEDIA_POSITION.MIDDLE.equals((Object)position)) {
                    pos = size / 2;
                } else if (MediaSeries.MEDIA_POSITION.LAST.equals((Object)position)) {
                    pos = size - 1;
                } else if (MediaSeries.MEDIA_POSITION.RANDOM.equals((Object)position)) {
                    pos = RANDOM.nextInt(size);
                }
                return (E)((MediaElement)sortedList.get(pos));
            }
            Iterable<E> iter = filter.filter(sortedList);
            Iterator<E> list = iter.iterator();
            if (list.hasNext()) {
                MediaElement val = (MediaElement)list.next();
                if (MediaSeries.MEDIA_POSITION.FIRST.equals((Object)position)) {
                    return (E)val;
                }
                int pos = 0;
                int size = Filter.size(iter);
                if (MediaSeries.MEDIA_POSITION.MIDDLE.equals((Object)position)) {
                    pos = size / 2;
                } else if (MediaSeries.MEDIA_POSITION.LAST.equals((Object)position)) {
                    pos = size - 1;
                } else if (MediaSeries.MEDIA_POSITION.RANDOM.equals((Object)position)) {
                    pos = RANDOM.nextInt(size);
                }
                int k = 0;
                for (MediaElement elem : iter) {
                    if (k != pos) continue;
                    return (E)elem;
                }
                return (E)val;
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final int getImageIndex(E source, Filter<E> filter, Comparator<E> sort) {
        if (source == null) {
            return -1;
        }
        Iterable<E> list = this.getMedias(filter, sort);
        List<E> list2 = this.medias;
        synchronized (list2) {
            int index = 0;
            for (MediaElement e : list) {
                if (e == source) {
                    return index;
                }
                ++index;
            }
        }
        return -1;
    }

    @Override
    public final Iterable<E> getMedias(Filter<E> filter, Comparator<E> sort) {
        List<E> sortedList = this.getSortedMedias(sort);
        return filter == null ? sortedList : filter.filter(sortedList);
    }

    @Override
    public final List<E> copyOfMedias(Filter<E> filter, Comparator<E> sort) {
        List<E> sortedList = this.getSortedMedias(sort);
        return filter == null ? new ArrayList<E>(sortedList) : Filter.makeList(filter.filter(sortedList));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final E getMedia(int index, Filter<E> filter, Comparator<E> sort) {
        List<E> sortedList = this.getSortedMedias(sort);
        Series series = this;
        synchronized (series) {
            if (filter == null) {
                if (index >= 0 && index < sortedList.size()) {
                    return (E)((MediaElement)sortedList.get(index));
                }
            } else if (index >= 0) {
                Iterable<E> iter = filter.filter(sortedList);
                int k = 0;
                for (MediaElement elem : iter) {
                    if (k == index) {
                        return (E)elem;
                    }
                    ++k;
                }
            }
        }
        return null;
    }

    @Override
    public synchronized void dispose() {
        this.medias.forEach(m -> {
            if (m instanceof ImageElement) {
                ImageElement imageElement = (ImageElement)m;
                imageElement.removeImageFromCache();
            }
            m.dispose();
        });
        this.removeAllMedias();
        Optional.ofNullable((Thumbnail)this.getTagValue(TagW.Thumbnail)).ifPresent(Thumbnail::dispose);
        if (this.propertyChange != null) {
            Arrays.asList(this.propertyChange.getPropertyChangeListeners()).forEach(this.propertyChange::removePropertyChangeListener);
        }
        this.seriesLoader = null;
    }

    @Override
    public SeriesImporter getSeriesLoader() {
        return this.seriesLoader;
    }

    @Override
    public void setSeriesLoader(SeriesImporter seriesLoader) {
        this.seriesLoader = seriesLoader;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return sequenceDataFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (sequenceDataFlavor.equals(flavor)) {
            return this;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    public void addPropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (this.propertyChange == null) {
            this.propertyChange = new PropertyChangeSupport(this);
        }
        this.propertyChange.addPropertyChangeListener(propertychangelistener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (this.propertyChange != null) {
            this.propertyChange.removePropertyChangeListener(propertychangelistener);
        }
    }

    public void firePropertyChange(ObservableEvent event) {
        if (this.propertyChange != null) {
            if (event == null) {
                throw new NullPointerException();
            }
            if (SwingUtilities.isEventDispatchThread()) {
                this.propertyChange.firePropertyChange(event);
            } else {
                SwingUtilities.invokeLater(() -> this.propertyChange.firePropertyChange(event));
            }
        }
    }

    @Override
    public synchronized int size(Filter<E> filter) {
        return filter == null ? this.medias.size() : Filter.size(filter.filter(this.medias));
    }

    @Override
    public boolean isOpen() {
        Boolean open = (Boolean)this.getTagValue(TagW.SeriesOpen);
        return open != null && open != false;
    }

    @Override
    public String getToolTips() {
        ImageElement image;
        PlanarImage img;
        Object media = this.getMedia(MediaSeries.MEDIA_POSITION.MIDDLE, (Filter)null, (Comparator)null);
        if (media instanceof ImageElement && (img = (image = (ImageElement)media).getImage()) != null) {
            return "<html>%s: %sx%s</html>\n".formatted(new Object[]{Messages.getString("Series.img_size"), img.width(), img.height()});
        }
        return "";
    }

    public void addToolTipsElement(StringBuilder toolTips, String title, TagW tag) {
        toolTips.append(title);
        toolTips.append(": ");
        if (tag != null) {
            toolTips.append(tag.getFormattedTagValue(this.getTagValue(tag), null));
        }
        toolTips.append("<br>");
    }

    @Override
    public void setOpen(boolean open) {
        if (this.isOpen() != open) {
            this.setTag(TagW.SeriesOpen, open);
            Thumbnail thumb = (Thumbnail)this.getTagValue(TagW.Thumbnail);
            if (thumb != null) {
                thumb.repaint();
            }
            if (!open) {
                this.resetSortedMediasMap();
            }
        }
    }

    @Override
    public boolean isSelected() {
        return LangUtil.nullToFalse((Boolean)((Boolean)this.getTagValue(TagW.SeriesSelected)));
    }

    @Override
    public void setSelected(boolean selected, E selectedImage) {
        if (this.isSelected() != selected) {
            this.setTag(TagW.SeriesSelected, selected);
            Thumbnail thumb = (Thumbnail)this.getTagValue(TagW.Thumbnail);
            if (thumb != null) {
                thumb.repaint();
            }
        }
    }

    @Override
    public boolean isFocused() {
        return LangUtil.nullToFalse((Boolean)((Boolean)this.getTagValue(TagW.SeriesFocused)));
    }

    @Override
    public void setFocused(boolean focused) {
        if (this.isFocused() != focused) {
            this.setTag(TagW.SeriesFocused, focused);
            Thumbnail thumb = (Thumbnail)this.getTagValue(TagW.Thumbnail);
            if (thumb != null) {
                thumb.repaint();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean hasMediaContains(TagW tag, Object val) {
        if (val != null) {
            List<E> list = this.medias;
            synchronized (list) {
                for (MediaElement media : this.medias) {
                    if (!val.equals(media.getTagValue(tag))) continue;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public E getNearestImage(double location, int offset, Filter<E> filter, Comparator<E> sort) {
        return null;
    }

    @Override
    public int getNearestImageIndex(double location, int offset, Filter<E> filter, Comparator<E> sort) {
        return -1;
    }

    public synchronized void setFileSize(long size) {
        this.fileSize = size;
    }

    @Override
    public synchronized long getFileSize() {
        return this.fileSize;
    }

    @Override
    public String getSeriesNumber() {
        Integer val = (Integer)this.getTagValue(TagW.get("SeriesNumber"));
        return Optional.ofNullable(val).map(String::valueOf).orElse("");
    }
}

