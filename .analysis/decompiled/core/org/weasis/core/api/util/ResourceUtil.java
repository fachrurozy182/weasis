/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import org.weasis.core.util.StringUtil;

public class ResourceUtil {
    public static final int TOOLBAR_ICON_SIZE = 30;
    private static final AtomicReference<Path> path = new AtomicReference<Path>(Path.of("", new String[0]));

    private ResourceUtil() {
    }

    public static URL getResourceURL(String resource, Class<?> c) {
        ClassLoader classLoader;
        URL url = null;
        if (c != null && (classLoader = c.getClassLoader()) != null) {
            url = classLoader.getResource(resource);
        }
        if (url == null && (classLoader = Thread.currentThread().getContextClassLoader()) != null) {
            url = classLoader.getResource(resource);
        }
        if (url == null) {
            url = ClassLoader.getSystemResource(resource);
        }
        return url;
    }

    public static void setResourcePath(String path) {
        if (!StringUtil.hasText((CharSequence)path)) {
            throw new IllegalArgumentException("No value for property: weasis.resources.path");
        }
        ResourceUtil.path.set(Path.of(path, new String[0]));
    }

    private static Path getResourcePath() {
        return path.get();
    }

    public static FlatSVGIcon getIcon(String path) {
        return new FlatSVGIcon(ResourceUtil.getResourcePath().resolve(path).toUri());
    }

    public static FlatSVGIcon getIcon(ResourceIconPath path) {
        return new FlatSVGIcon(ResourceUtil.getResourcePath().resolve(path.getPath()).toUri());
    }

    public static FlatSVGIcon getToolBarIcon(ResourceIconPath path) {
        return ResourceUtil.getIcon(path, 30, 30);
    }

    public static FlatSVGIcon getIcon(ResourceIconPath path, int width, int height) {
        return new FlatSVGIcon(ResourceUtil.getResourcePath().resolve(path.getPath()).toUri()).derive(width, height);
    }

    public static File getResource(String filename) {
        if (!StringUtil.hasText((CharSequence)filename)) {
            throw new IllegalArgumentException("Empty filename");
        }
        return ResourceUtil.getResourcePath().resolve(filename).toFile();
    }

    public static Path getResource(Path endOfPath) {
        return ResourceUtil.getResourcePath().resolve(endOfPath);
    }

    public static File getResource(ResourcePath path) {
        return ResourceUtil.getResourcePath().resolve(path.getPath()).toFile();
    }

    public static interface ResourceIconPath
    extends ResourcePath {
    }

    public static interface ResourcePath {
        public String getPath();
    }

    public static enum OtherIcon implements ResourceIconPath
    {
        AUDIO("svg/other/audio.svg"),
        CALENDAR("svg/other/calendar.svg"),
        CDROM("svg/other/cdrom.svg"),
        CLOCK("svg/other/clock.svg"),
        ECG("svg/other/ecg.svg"),
        HISTOGRAM("svg/other/histogram.svg"),
        IMAGE_EDIT("svg/other/imageEdit.svg"),
        IMAGE_PRESENTATION("svg/other/imagePresentation.svg"),
        KEY_IMAGE("svg/other/keyImage.svg"),
        MODALITY("svg/other/modality.svg"),
        PATIENT("svg/other/patient.svg"),
        PRESENTATION("svg/other/presentation.svg"),
        KEY("svg/other/key.svg"),
        SEGMENTATION("svg/other/segmentation.svg"),
        RADIOACTIVE("svg/other/radioactive.svg"),
        RASTER_IMAGE("svg/other/rasterImage.svg"),
        TICK_OFF("svg/other/tickOff.svg"),
        TICK_ON("svg/other/tickOn.svg"),
        VIEW_MIP("svg/other/veins.svg"),
        VIEW_3D("svg/other/view3D.svg"),
        VIEW_SETTING("svg/other/viewSettings.svg"),
        XRAY("svg/other/xray.svg");

        private final String path;

        private OtherIcon(String path) {
            this.path = path;
        }

        @Override
        public String getPath() {
            return this.path;
        }
    }

    public static enum ActionIcon implements ResourceIconPath
    {
        CONTEXT_MENU("svg/action/contextMenu.svg"),
        CROSSHAIR("svg/action/crosshair.svg"),
        DRAW("svg/action/draw.svg"),
        DRAW_4POINTS_ANGLE("svg/action/draw4PointsAngle.svg"),
        DRAW_ANGLE("svg/action/drawAngle.svg"),
        DRAW_CIRCLE("svg/action/drawCircle.svg"),
        DRAW_COBB("svg/action/drawCobb.svg"),
        DRAW_ELLIPSE("svg/action/drawEllipse.svg"),
        DRAW_LINE("svg/action/drawLine.svg"),
        DRAW_OPEN_ANGLE("svg/action/drawOpenAngle.svg"),
        DRAW_PARALLEL("svg/action/drawParallel.svg"),
        DRAW_PERPENDICULAR("svg/action/drawPerpendicular.svg"),
        DRAW_PIXEL_INFO("svg/action/drawPixelInfo.svg"),
        DRAW_POLYGON("svg/action/drawPolygon.svg"),
        DRAW_POLYLINE("svg/action/drawPolyline.svg"),
        DRAW_RECTANGLE("svg/action/drawRectangle.svg"),
        DRAW_SELECTION("svg/action/drawSelection.svg"),
        DRAW_TEXT("svg/action/drawText.svg"),
        DRAW_TOP_LEFT("svg/action/drawTopLeft.svg"),
        EDIT_KEY_IMAGE("svg/action/editKeyImage.svg"),
        EXECUTE("svg/action/execute.svg"),
        EXPORT_CLIPBOARD("svg/action/exportClipboard.svg"),
        EXPORT_DICOM("svg/action/exportDicom.svg"),
        EXPORT_IMAGE("svg/action/exportImage.svg"),
        EXPORT_ANNOTATIONS("svg/action/exportAnnotations.svg"),
        FILTER("svg/action/filter.svg"),
        FLIP("svg/action/flip.svg"),
        HAND("svg/action/hand.svg"),
        HELP("svg/action/help.svg"),
        IMPORT_CD("svg/action/importCd.svg"),
        IMPORT_DICOM("svg/action/importDicom.svg"),
        IMPORT_IMAGE("svg/action/importImage.svg"),
        LAUNCH("svg/action/launch.svg"),
        INVERSE_LUT("svg/action/inverseLut.svg"),
        LAYOUT("svg/action/layout.svg"),
        LOAD_VOLUME("svg/action/loadVolume.svg"),
        LOOP("svg/action/loop.svg"),
        LUT("svg/action/lut.svg"),
        MEASURE("svg/action/measure.svg"),
        MEASURE_TOP_LEFT("svg/action/measureTopLeft.svg"),
        METADATA("svg/action/metadata.svg"),
        MINUS("svg/action/minus.svg"),
        MORE_H("svg/action/moreHorizontal.svg"),
        MORE_V("svg/action/moreVertical.svg"),
        MOUSE_LEFT("svg/action/mouseLeft.svg"),
        MOUSE_MIDDLE("svg/action/mouseMiddle.svg"),
        MOUSE_RIGHT("svg/action/mouseRight.svg"),
        MOUSE_WHEEL("svg/action/mouseWheel.svg"),
        NEXT("svg/action/next.svg"),
        NONE("svg/action/none.svg"),
        OPEN_EXTERNAL("svg/action/openExternal.svg"),
        OPEN_NEW_TAB("svg/action/openNewTab.svg"),
        ORTHOGRAPHIC("svg/action/orthographic.svg"),
        PAN("svg/action/pan.svg"),
        PIPETTE("svg/action/pipette.svg"),
        PLUS("svg/action/plus.svg"),
        PRINT("svg/action/print.svg"),
        PREVIOUS("svg/action/previous.svg"),
        RESET("svg/action/reset.svg"),
        ROTATE_CLOCKWISE("svg/action/rotateClockwise.svg"),
        ROTATE_COUNTERCLOCKWISE("svg/action/rotateCounterclockwise.svg"),
        ROTATION("svg/action/rotation.svg"),
        SELECTION("svg/action/selection.svg"),
        SELECTION_DELETE("svg/action/selectionDelete.svg"),
        SEQUENCE("svg/action/sequence.svg"),
        SETTINGS("svg/action/settings.svg"),
        STAR("svg/action/star.svg"),
        SKIP_END("svg/action/skipEnd.svg"),
        SKIP_START("svg/action/skipStart.svg"),
        STAR_ALL("svg/action/starAll.svg"),
        SUSPEND("svg/action/suspend.svg"),
        SYNCH("svg/action/synch.svg"),
        SYNCH_LARGE("svg/action/synchLarge.svg"),
        SYNCH_STAR("svg/action/synchStar.svg"),
        TILE("svg/action/tile.svg"),
        VOLUME("svg/action/volume.svg"),
        VOLUME_CUT("svg/action/volumeCut.svg"),
        VOLUME_SETTINGS("svg/action/volumeSettings.svg"),
        VOLUME_SLICING("svg/action/volumeSlicing.svg"),
        WINDOW_LEVEL("svg/action/winLevel.svg"),
        ZOOM("svg/action/zoom.svg"),
        ZOOM_AREA("svg/action/zoomArea.svg"),
        ZOOM_BEST_FIT("svg/action/zoomBestFit.svg"),
        ZOOM_ORIGINAL("svg/action/zoomOriginal.svg"),
        ZOOM_PAN("svg/action/zoomPan.svg"),
        ZOOM_REAL_WORLD("svg/action/zoomRealWorld.svg");

        private final String path;

        private ActionIcon(String path) {
            this.path = path;
        }

        @Override
        public String getPath() {
            return this.path;
        }
    }

    public static enum FileIcon implements ResourceIconPath
    {
        AUDIO("svg/file/audio.svg"),
        DICOM("svg/file/dicom.svg"),
        ECG("svg/file/ecg.svg"),
        IMAGE("svg/file/image.svg"),
        PDF("svg/file/pdf.svg"),
        TEXT("svg/file/text.svg"),
        UNKNOWN("svg/file/unknown.svg"),
        VIDEO("svg/file/video.svg"),
        XML("svg/file/xml.svg");

        private final String path;

        private FileIcon(String path) {
            this.path = path;
        }

        @Override
        public String getPath() {
            return this.path;
        }
    }

    public static enum LogoIcon implements ResourceIconPath
    {
        LARGE("svg/logo/WeasisAbout.svg"),
        SMALL("svg/logo/Weasis.svg"),
        SMALL_DICOMIZER("svg/logo/Dicomizer.svg");

        private final String path;

        private LogoIcon(String path) {
            this.path = path;
        }

        @Override
        public String getPath() {
            return this.path;
        }
    }
}

