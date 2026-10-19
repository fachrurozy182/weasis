/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 *  org.opencv.core.Mat
 *  org.opencv.core.MatOfInt
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.ImageIOHandler
 */
package org.weasis.core.api.media.data;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.EventListener;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.NativeCache;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnailable;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.api.util.ThreadUtil;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.ImageIOHandler;

public class Thumbnail
extends JLabel
implements Thumbnailable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Thumbnail.class);
    public static final Path THUMBNAIL_CACHE_DIR = AppProperties.buildAccessibleTempDirectory("cache", "thumb");
    public static final ExecutorService THUMB_LOADER = ThreadUtil.newManagedImageIOThreadPool("ThumbnailLoader");
    public static final String KEY_SIZE = "explorer.thumbnail.size";
    public static final int MIN_SIZE = 48;
    public static final int DEFAULT_SIZE = 144;
    public static final int MAX_SIZE = 256;
    private static final NativeCache<Thumbnail, PlanarImage> mCache = new NativeCache<Thumbnail, PlanarImage>(30000000L){

        @Override
        protected void afterEntryRemove(Thumbnail key, PlanarImage img) {
            if (img != null) {
                img.release();
            }
        }
    };
    protected volatile boolean readable = true;
    protected AtomicBoolean loading = new AtomicBoolean(false);
    protected File thumbnailPath = null;
    protected int thumbnailSize;

    public Thumbnail(int thumbnailSize) {
        super(null, null, 0);
        this.thumbnailSize = GuiUtils.getScaleLength(thumbnailSize);
    }

    public Thumbnail(MediaElement media, int thumbnailSize, boolean keepMediaCache, OpManager opManager) {
        super(null, null, 0);
        if (media == null) {
            throw new IllegalArgumentException("image cannot be null");
        }
        this.thumbnailSize = GuiUtils.getScaleLength(thumbnailSize);
        this.init(media, keepMediaCache, opManager);
    }

    protected void init(MediaElement media, boolean keepMediaCache, OpManager opManager) {
        this.buildThumbnail(media, keepMediaCache, opManager);
    }

    @Override
    public void registerListeners() {
        this.removeMouseAndKeyListener();
    }

    public static PlanarImage createThumbnail(PlanarImage source) {
        if (source == null) {
            return null;
        }
        return ImageIOHandler.buildThumbnail((PlanarImage)source, (Dimension)new Dimension(256, 256), (boolean)true);
    }

    protected synchronized void buildThumbnail(MediaElement media, boolean keepMediaCache, OpManager opManager) {
        String mime;
        ResourceUtil.FileIcon fileIcon = null;
        String type = "";
        if (media != null && (mime = media.getMimeType()) != null) {
            if (mime.startsWith("image")) {
                type = Messages.getString("Thumbnail.img");
                fileIcon = ResourceUtil.FileIcon.IMAGE;
            } else if (mime.startsWith("video")) {
                type = Messages.getString("Thumbnail.video");
                fileIcon = ResourceUtil.FileIcon.VIDEO;
            } else if (mime.equals("sr/dicom")) {
                type = Messages.getString("Thumbnail.dicom_sr");
                fileIcon = ResourceUtil.FileIcon.TEXT;
            } else if (mime.startsWith("txt")) {
                type = Messages.getString("Thumbnail.text");
                fileIcon = ResourceUtil.FileIcon.TEXT;
            } else if (mime.endsWith("html")) {
                type = Messages.getString("Thumbnail.html");
                fileIcon = ResourceUtil.FileIcon.XML;
            } else if (mime.equals("application/pdf")) {
                type = Messages.getString("Thumbnail.pdf");
                fileIcon = ResourceUtil.FileIcon.PDF;
            } else if (mime.equals("wf/dicom")) {
                type = "ECG";
                fileIcon = ResourceUtil.FileIcon.ECG;
            } else if (mime.startsWith("audio") || mime.equals("au/dicom")) {
                type = Messages.getString("Thumbnail.audio");
                fileIcon = ResourceUtil.FileIcon.AUDIO;
            } else {
                type = mime;
            }
        }
        if (fileIcon == null) {
            fileIcon = ResourceUtil.FileIcon.UNKNOWN;
        }
        this.setIcon(media, ResourceUtil.getIcon(fileIcon, 64, 64), type, keepMediaCache, opManager);
    }

    private void setIcon(final MediaElement media, final FlatSVGIcon mimeIcon, final String description, final boolean keepMediaCache, final OpManager opManager) {
        ImageIcon imageIcon = new ImageIcon(this){
            final /* synthetic */ Thumbnail this$0;
            {
                Thumbnail thumbnail = this$0;
                Objects.requireNonNull(thumbnail);
                this.this$0 = thumbnail;
            }

            @Override
            public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D)g;
                int width = this.this$0.thumbnailSize;
                int height = this.this$0.thumbnailSize;
                PlanarImage thumbnail = this.this$0.getImage(media, keepMediaCache, opManager);
                if (thumbnail == null) {
                    boolean displayText;
                    FontMetrics fm = g2d.getFontMetrics();
                    int insetY = 5;
                    int textLength = fm.stringWidth(description);
                    int fontHeight = 0;
                    boolean bl = displayText = StringUtil.hasText((CharSequence)description) && textLength + insetY < width;
                    if (displayText) {
                        fontHeight = fm.getHeight();
                    }
                    int fy = y + (this.this$0.thumbnailSize - fontHeight - mimeIcon.getIconHeight()) / 2;
                    mimeIcon.paintIcon(c, (Graphics)g2d, x + (this.this$0.thumbnailSize - mimeIcon.getIconWidth()) / 2, fy);
                    if (displayText) {
                        Object[] oldRenderingHints = GuiUtils.setRenderingHints(g2d, true, false, true);
                        int startX = x + (this.this$0.thumbnailSize - textLength) / 2;
                        int startY = fm.getAscent() - fm.getDescent() - fm.getLeading();
                        g2d.drawString(description, startX, fy + mimeIcon.getIconHeight() + startY + insetY);
                        GuiUtils.resetRenderingHints(g2d, oldRenderingHints);
                    }
                } else {
                    width = thumbnail.width();
                    height = thumbnail.height();
                    x += (this.this$0.thumbnailSize - width) / 2;
                    y += (this.this$0.thumbnailSize - height) / 2;
                    if (g2d.getDeviceConfiguration().getDefaultTransform().getScaleX() > 1.0) {
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    }
                    g2d.drawImage(ImageConversion.toBufferedImage((PlanarImage)thumbnail), AffineTransform.getTranslateInstance(x, y), null);
                }
                this.this$0.drawOverIcon(g2d, x, y, width, height);
            }

            @Override
            public int getIconWidth() {
                return this.this$0.thumbnailSize;
            }

            @Override
            public int getIconHeight() {
                return this.this$0.thumbnailSize;
            }
        };
        GuiExecutor.execute(() -> {
            this.setSize(this.thumbnailSize, this.thumbnailSize);
            this.setIcon(imageIcon);
        });
    }

    protected void drawOverIcon(Graphics2D g2d, int x, int y, int width, int height) {
    }

    @Override
    public File getThumbnailPath() {
        return this.thumbnailPath;
    }

    protected PlanarImage getImage(final MediaElement media, final boolean keepMediaCache, final OpManager opManager) {
        PlanarImage cacheImage = mCache.get(this);
        if (cacheImage == null && this.readable && this.loading.compareAndSet(false, true)) {
            try {
                SwingWorker<Boolean, String> thumbnailReader = new SwingWorker<Boolean, String>(this){
                    final /* synthetic */ Thumbnail this$0;
                    {
                        Thumbnail thumbnail = this$0;
                        Objects.requireNonNull(thumbnail);
                        this.this$0 = thumbnail;
                    }

                    @Override
                    protected void done() {
                        this.this$0.repaint();
                    }

                    @Override
                    protected Boolean doInBackground() {
                        this.this$0.loadThumbnail(media, keepMediaCache, opManager);
                        return Boolean.TRUE;
                    }
                };
                THUMB_LOADER.execute(thumbnailReader);
            }
            catch (Exception e) {
                LOGGER.error("Cannot build thumbnail!", (Throwable)e);
                this.loading.set(false);
            }
        }
        return cacheImage;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadThumbnail(MediaElement media, boolean keepMediaCache, OpManager opManager) {
        block25: {
            try {
                String path;
                boolean noPath;
                File file = this.thumbnailPath;
                boolean bl = noPath = file == null || !file.canRead();
                if (noPath && media != null && (path = (String)media.getTagValue(TagW.ThumbnailPath)) != null && (file = new File(path)).canRead()) {
                    noPath = false;
                    this.thumbnailPath = file;
                }
                if (noPath) {
                    if (!(media instanceof ImageElement)) break block25;
                    ImageElement image = (ImageElement)media;
                    PlanarImage imgPl = image.getImage(opManager);
                    if (imgPl != null) {
                        PlanarImage img = image.getRenderedImage(imgPl);
                        PlanarImage thumb = Thumbnail.createThumbnail(img);
                        if (thumb != null) {
                            try {
                                file = Files.createTempFile(THUMBNAIL_CACHE_DIR, "tumb_", ".jpg", new FileAttribute[0]).toFile();
                            }
                            catch (IOException e) {
                                LOGGER.error("Cannot create file for thumbnail!", (Throwable)e);
                            }
                        }
                        try {
                            if (thumb != null && file != null && thumb.width() > 0) {
                                MatOfInt map = new MatOfInt(new int[]{1, 80});
                                if (ImageIOHandler.writeImage((Mat)thumb.toMat(), (Path)file.toPath(), (MatOfInt)map)) {
                                    image.setTag(TagW.ThumbnailPath, file.getPath());
                                    this.thumbnailPath = file;
                                    return;
                                }
                            }
                            if (thumb == null || thumb.width() <= 0) {
                                this.readable = false;
                                ImageConversion.releasePlanarImage((PlanarImage)thumb);
                                keepMediaCache = false;
                            } else {
                                mCache.put(this, thumb);
                            }
                            break block25;
                        }
                        finally {
                            if (!keepMediaCache) {
                                image.removeImageFromCache();
                            }
                        }
                    }
                    this.readable = false;
                    break block25;
                }
                Load ref = new Load(file);
                Future<PlanarImage> future = ImageElement.IMAGE_LOADER.submit(ref);
                Object thumb = null;
                try {
                    PlanarImage img = future.get();
                    if (img != null) {
                        int width = img.width();
                        int height = img.height();
                        thumb = width > this.thumbnailSize || height > this.thumbnailSize ? ImageIOHandler.buildThumbnail((PlanarImage)img, (Dimension)new Dimension(this.thumbnailSize, this.thumbnailSize), (boolean)true) : img;
                    }
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    future.cancel(true);
                }
                catch (ExecutionException e) {
                    LOGGER.error("Cannot read thumbnail pixel data!: {}", (Object)file, (Object)e);
                }
                if (thumb == null || thumb.width() <= 0) {
                    this.readable = false;
                } else {
                    mCache.put(this, (PlanarImage)thumb);
                }
            }
            finally {
                this.loading.set(false);
            }
        }
    }

    protected void removeImageFromCache() {
        mCache.remove(this);
    }

    @Override
    public void dispose() {
        this.removeImageFromCache();
        if (this.thumbnailPath != null && this.thumbnailPath.getPath().startsWith(AppProperties.FILE_CACHE_DIR.toString())) {
            FileUtil.delete((Path)this.thumbnailPath.toPath());
        }
        this.removeMouseAndKeyListener();
    }

    @Override
    public void removeMouseAndKeyListener() {
        MouseListener[] listener = this.getMouseListeners();
        MouseMotionListener[] motionListeners = this.getMouseMotionListeners();
        KeyListener[] keyListeners = this.getKeyListeners();
        MouseWheelListener[] wheelListeners = this.getMouseWheelListeners();
        for (MouseListener mouseListener : listener) {
            this.removeMouseListener(mouseListener);
        }
        for (EventListener eventListener : motionListeners) {
            this.removeMouseMotionListener((MouseMotionListener)eventListener);
        }
        for (EventListener eventListener : keyListeners) {
            this.removeKeyListener((KeyListener)eventListener);
        }
        for (EventListener eventListener : wheelListeners) {
            this.removeMouseWheelListener((MouseWheelListener)eventListener);
        }
    }

    static class Load
    implements Callable<PlanarImage> {
        private final File path;

        public Load(File path) {
            this.path = path;
        }

        @Override
        public PlanarImage call() throws Exception {
            return ImageIOHandler.readImageWithCvException((Path)this.path.toPath(), null);
        }
    }
}

