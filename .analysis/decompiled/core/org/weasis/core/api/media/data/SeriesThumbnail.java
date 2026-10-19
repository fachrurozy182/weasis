/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 */
package org.weasis.core.api.media.data;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GhostGlassPane;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.SeriesImporter;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.PlayViewButton;
import org.weasis.core.ui.editor.image.ViewButton;
import org.weasis.core.util.FileUtil;

public class SeriesThumbnail
extends Thumbnail
implements MouseListener,
MouseMotionListener,
DragGestureListener,
DragSourceListener,
DragSourceMotionListener,
FocusListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeriesThumbnail.class);
    private MediaSeries.MEDIA_POSITION mediaPosition = MediaSeries.MEDIA_POSITION.MIDDLE;
    private final Border thumbnailBorder = new EmptyBorder(5, 5, 0, 5);
    private JProgressBar progressBar;
    private final MediaSeries<? extends MediaElement> series;
    private final Function<String, Set<ResourceUtil.ResourceIconPath>> drawIcons;
    private final PlayViewButton playBtn;
    private Point dragPressed = null;
    private DragSource dragSource = null;

    public SeriesThumbnail(MediaSeries<? extends MediaElement> sequence, int thumbnailSize, Function<String, Set<ResourceUtil.ResourceIconPath>> drawIcons) {
        super(thumbnailSize);
        List specialElements;
        if (sequence == null) {
            throw new IllegalArgumentException("Sequence cannot be null");
        }
        this.series = sequence;
        this.drawIcons = drawIcons;
        this.playBtn = new PlayViewButton((invoker, x, y) -> {
            PlayViewButton b;
            SeriesImporter loader = this.series.getSeriesLoader();
            if (loader != null && (b = this.getPlayButton()) != null) {
                if (b.getState() == PlayViewButton.eState.PAUSE) {
                    b.setState(PlayViewButton.eState.PLAY);
                    loader.stop();
                } else {
                    b.setState(PlayViewButton.eState.PAUSE);
                    loader.resume();
                }
            }
        });
        MediaElement media = sequence.getMedia(MediaSeries.MEDIA_POSITION.MIDDLE, null, null);
        if (media == null && (specialElements = (List)this.series.getTagValue(TagW.DicomSpecialElementList)) != null && !specialElements.isEmpty()) {
            media = (MediaElement)specialElements.getFirst();
        }
        this.init(media, this.series.getTagValue(TagW.ExplorerModel) != null, null);
    }

    @Override
    protected void init(MediaElement media, boolean keepMediaCache, OpManager opManager) {
        super.init(media, keepMediaCache, opManager);
        this.setBorder(this.thumbnailBorder);
    }

    public static int getThumbnailSizeFromPreferences() {
        return GuiUtils.getUICore().getSystemPreferences().getIntProperty("explorer.thumbnail.size", 144);
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }

    public void setProgressBar(JProgressBar progressBar) {
        if (progressBar == null) {
            this.removeMouseListener(this);
        } else if (!Arrays.asList(this.getMouseListeners()).contains(this)) {
            this.addMouseListener(this);
        }
        this.progressBar = progressBar;
    }

    public PlayViewButton getPlayButton() {
        return this.playBtn;
    }

    @Override
    public void registerListeners() {
        super.registerListeners();
        ToolTipManager.sharedInstance().registerComponent(this);
        if (this.dragSource != null) {
            this.dragSource.removeDragSourceListener(this);
            this.dragSource.removeDragSourceMotionListener(this);
            this.removeFocusListener(this);
        }
        this.addFocusListener(this);
        this.setFocusable(true);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.dragSource = new DragSource();
        this.dragSource.createDefaultDragGestureRecognizer(this, 1, this);
        this.dragSource.addDragSourceMotionListener(this);
    }

    public void reBuildThumbnail(MediaSeries.MEDIA_POSITION position) {
        this.reBuildThumbnail(null, position);
    }

    public void reBuildThumbnail() {
        this.reBuildThumbnail(null, this.mediaPosition);
    }

    public synchronized void reBuildThumbnail(File file, MediaSeries.MEDIA_POSITION position) {
        MediaElement media = this.series.getMedia(position, null, null);
        if (media == null) {
            media = this.series.getFirstSpecialElement();
        }
        if (file != null || media != null) {
            this.mediaPosition = position;
            if (this.thumbnailPath != null && this.thumbnailPath.getPath().startsWith(AppProperties.FILE_CACHE_DIR.toString())) {
                FileUtil.delete((Path)this.thumbnailPath.toPath());
            }
            this.removeImageFromCache();
            this.thumbnailPath = file;
            this.readable = true;
            this.buildThumbnail(media, this.series.getTagValue(TagW.ExplorerModel) != null, null);
            this.revalidate();
            this.repaint();
        }
    }

    public synchronized int getThumbnailSize() {
        return this.thumbnailSize;
    }

    public synchronized void setThumbnailSize(int thumbnailSize) {
        int size = GuiUtils.getScaleLength(thumbnailSize);
        if (this.thumbnailSize != size) {
            this.thumbnailSize = GuiUtils.getScaleLength(size);
            MediaElement media = this.series.getMedia(this.mediaPosition, null, null);
            if (media == null) {
                media = this.series.getFirstSpecialElement();
            }
            this.removeImageFromCache();
            this.buildThumbnail(media, this.series.getTagValue(TagW.ExplorerModel) != null, null);
        }
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        Component comp = dge.getComponent();
        try {
            GhostGlassPane glassPane = AppProperties.glassPane;
            glassPane.setIcon(this.getIcon());
            Point p = (Point)dge.getDragOrigin().clone();
            this.dragPressed = new Point(p.x - 4, p.y - 4);
            SwingUtilities.convertPointToScreen(p, comp);
            this.drawGlassPane(p);
            glassPane.setVisible(true);
            dge.startDrag(null, this.series, this);
        }
        catch (Exception e) {
            LOGGER.error("Prepare to drag", (Throwable)e);
        }
    }

    @Override
    public void dragMouseMoved(DragSourceDragEvent dsde) {
        this.drawGlassPane(dsde.getLocation());
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dsde) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        GhostGlassPane glassPane = AppProperties.glassPane;
        this.dragPressed = null;
        glassPane.setImagePosition(null);
        glassPane.setIcon(null);
        glassPane.setVisible(false);
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void drawGlassPane(Point p) {
        if (this.dragPressed != null) {
            GhostGlassPane glassPane = AppProperties.glassPane;
            SwingUtilities.convertPointFromScreen(p, glassPane);
            p.translate(-this.dragPressed.x, -this.dragPressed.y);
            glassPane.setImagePosition(p);
        }
    }

    public MediaSeries<MediaElement> getSeries() {
        return this.series;
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (!e.isTemporary()) {
            SeriesImporter loader;
            JPanel container = this.getScrollPane();
            if (container != null) {
                Rectangle bound = this.getBounds();
                Point p1 = SwingUtilities.convertPoint(this, this.getX(), this.getY(), container);
                bound.x = p1.x;
                bound.y = p1.y;
                container.scrollRectToVisible(bound);
            }
            if ((loader = this.series.getSeriesLoader()) != null) {
                loader.setPriority();
            }
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    private JPanel getScrollPane() {
        for (Container container = this.getParent(); container != null; container = container.getParent()) {
            if (!(container.getParent() instanceof JViewport)) continue;
            return (JPanel)container;
        }
        return null;
    }

    @Override
    public String getToolTipText() {
        return this.series.getToolTips();
    }

    @Override
    protected void drawOverIcon(Graphics2D g2d, int x, int y, int width, int height) {
        if (this.dragPressed == null) {
            JProgressBar bar;
            int number;
            Object[] oldRenderingHints = GuiUtils.setRenderingHints(g2d, true, false, true);
            int inset = GuiUtils.getScaleLength(2);
            if (this.series.isOpen()) {
                if (this.series.isSelected() && this.series.isFocused()) {
                    g2d.setPaint(GuiUtils.IconColor.ACTIONS_YELLOW.getColor());
                } else {
                    g2d.setPaint(GuiUtils.IconColor.ACTIONS_GREEN.getColor());
                }
                int size = inset * 5;
                g2d.fillArc(x + inset, y + inset, size, size, 0, 360);
                g2d.setPaint(Color.BLACK);
                g2d.drawArc(x + inset, y + inset, size, size, 0, 360);
            }
            g2d.setFont(width > 144 ? FontItem.MINI_SEMIBOLD.getFont() : FontItem.MICRO_SEMIBOLD.getFont());
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int fontHeight = fontMetrics.getHeight();
            int descent = g2d.getFontMetrics().getDescent();
            Integer splitNb = (Integer)this.series.getTagValue(TagW.SplitSeriesNumber);
            if (splitNb != null) {
                String nb = "#" + splitNb;
                int w = g2d.getFontMetrics().stringWidth(nb);
                int sx = x + width - inset - w;
                FontTools.paintColorFontOutline(g2d, nb, sx, (float)(y + inset + fontHeight) - (float)descent, GuiUtils.IconColor.ACTIONS_BLUE.getColor());
            }
            if ((number = this.series.size(null)) > 0) {
                FontTools.paintColorFontOutline(g2d, String.valueOf(number), (float)x + (float)inset, (float)(y + height) - (float)(inset + descent), GuiUtils.IconColor.ACTIONS_BLUE.getColor());
            }
            if ((bar = this.progressBar) != null) {
                this.drawProgression(g2d, x, y, height, bar, inset, fontHeight);
            } else {
                String seriesUID = (String)this.series.getTagValue(TagW.get("SeriesInstanceUID"));
                Set<ResourceUtil.ResourceIconPath> paths = this.drawIcons.apply(seriesUID);
                if (paths != null && !paths.isEmpty()) {
                    double yPos = height;
                    for (ResourceUtil.ResourceIconPath path : paths) {
                        FlatSVGIcon icon = ResourceUtil.getIcon(path);
                        double shiftX = (double)width - (double)icon.getIconWidth();
                        g2d.translate(shiftX, yPos -= (double)(icon.getIconHeight() + inset));
                        icon.paintIcon((Component)this, (Graphics)g2d, x, y);
                        g2d.translate(-shiftX, -yPos);
                    }
                }
            }
            GuiUtils.resetRenderingHints(g2d, oldRenderingHints);
        }
    }

    private void drawProgression(Graphics2D g2d, int x, int y, int height, JProgressBar bar, int inset, int fontHeight) {
        if ((double)this.series.getFileSize() > 0.0) {
            g2d.drawString(FileUtil.humanReadableByte((long)this.series.getFileSize(), (boolean)false), x + inset, y + height - fontHeight - inset * 2);
        }
        if (bar.isVisible()) {
            double shiftX = (double)this.thumbnailSize - (double)bar.getWidth();
            double shiftY = (double)this.thumbnailSize - (double)bar.getHeight();
            g2d.translate(shiftX, shiftY);
            bar.paint(g2d);
            g2d.translate(-shiftX, -shiftY);
            SeriesImporter seriesLoader = this.series.getSeriesLoader();
            this.playBtn.setVisible(seriesLoader != null);
            if (seriesLoader != null) {
                if (seriesLoader.isStopped()) {
                    this.playBtn.setState(PlayViewButton.eState.PLAY);
                } else {
                    this.playBtn.setState(PlayViewButton.eState.PAUSE);
                }
                Icon icon = this.playBtn.getIcon();
                this.playBtn.x = (double)this.thumbnailSize - (double)icon.getIconWidth() - (double)inset * 3.0;
                this.playBtn.y = (double)(this.thumbnailSize - bar.getHeight() - icon.getIconHeight()) - (double)inset * 5.0;
                ViewButton.drawButtonBackground(g2d, this, this.playBtn, icon);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (this.playBtn.isVisible() && this.playBtn.contains(e.getPoint())) {
            e.consume();
            this.playBtn.showPopup(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        boolean hover;
        if (this.playBtn.isVisible() && (hover = this.playBtn.contains(e.getPoint())) != this.playBtn.isHover()) {
            this.playBtn.setHover(hover);
            this.repaint();
        }
    }
}

