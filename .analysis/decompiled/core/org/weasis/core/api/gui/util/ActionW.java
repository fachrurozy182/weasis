/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.op.lut.ByteLut
 */
package org.weasis.core.api.gui.util;

import java.awt.Cursor;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.layout.MigLayoutModel;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.image.util.KernelData;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.SeriesComparator;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.opencv.op.lut.ByteLut;

public class ActionW {
    public static final String DRAW_CMD_PREFIX = "draw.sub.";
    public static final Feature.BasicActionStateValue NO_ACTION = new Feature.BasicActionStateValue(Messages.getString("ActionW.no"), "none", 78, 0, null);
    public static final Feature.ComboItemListenerValue<SynchView> SYNCH = new Feature.ComboItemListenerValue(Messages.getString("ActionW.synch"), "synch", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue SYNCH_MODE = new Feature.ToggleButtonListenerValue(Messages.getString("ActionW.synch_mode"), "synch_mode", 0, 0, null);
    public static final Feature.BasicActionStateValue VIEW_MODE = new Feature.BasicActionStateValue(Messages.getString("ActionW.view_mode"), "viewMode", 0, 0, null);
    public static final Feature.SliderChangeListenerValue ZOOM = new Feature.SliderChangeListenerValue(Messages.getString("ActionW.zoom"), "zoom", 90, 0, Feature.getSvgCursor("zoom.svg", Messages.getString("ActionW.zoom"), 0.5f, 0.5f));
    public static final Feature.SliderCineListenerValue SCROLL_SERIES = new Feature.SliderCineListenerValue(Messages.getString("ActionW.scroll"), "sequence", 83, 0, Feature.getSvgCursor("sequence.svg", Messages.getString("ActionW.scroll"), 0.5f, 0.5f));
    public static final Feature.SliderChangeListenerValue ROTATION = new Feature.SliderChangeListenerValue(Messages.getString("ActionW.rotate"), "rotation", 82, 0, Feature.getSvgCursor("rotation.svg", Messages.getString("ActionW.rotate"), 0.5f, 0.5f));
    public static final Feature.BasicActionStateValue CINESPEED = new Feature.BasicActionStateValue(Messages.getString("ActionW.speed"), "cinespeed", 0, 0, null);
    public static final Feature.BasicActionStateValue CINESTART = new Feature.BasicActionStateValue(Messages.getString("ActionW.start"), "cinestart", 67, 0, null);
    public static final Feature.BasicActionStateValue CINESTOP = new Feature.BasicActionStateValue(Messages.getString("ActionW.stop"), "cinestop", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue CINE_SWEEP = new Feature.ToggleButtonListenerValue("Loop Sweep", "cinesweep", 0, 0, null);
    public static final Feature<SliderChangeListener> WINDOW = new Feature.SliderChangeListenerValue(Messages.getString("ActionW.win"), "window", 0, 0, null);
    public static final Feature.SliderChangeListenerValue LEVEL = new Feature.SliderChangeListenerValue(Messages.getString("ActionW.level"), "level", 0, 0, null);
    public static final Feature.BasicActionStateValue WINLEVEL = new Feature.BasicActionStateValue(Messages.getString("ActionW.wl"), "winLevel", 87, 0, Feature.getSvgCursor("winLevel.svg", Messages.getString("ActionW.wl"), 0.5f, 0.5f));
    public static final Feature.DoubleValue LEVEL_MIN = new Feature.DoubleValue("", "level_min", 0, 0, null);
    public static final Feature.DoubleValue LEVEL_MAX = new Feature.DoubleValue("", "level_max", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue FLIP = new Feature.ToggleButtonListenerValue(Messages.getString("ActionW.flip"), "flip", 0, 0, null);
    public static final Feature.ComboItemListenerValue<Object> PRESET = new Feature.ComboItemListenerValue(Messages.getString("ActionW.preset"), "preset", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue DEFAULT_PRESET = new Feature.ToggleButtonListenerValue("", "default_preset", 0, 0, null);
    public static final Feature.ComboItemListenerValue<Object> LUT_SHAPE = new Feature.ComboItemListenerValue(Messages.getString("ActionW.lut_shape"), "lut_shape", 0, 0, null);
    public static final Feature.ComboItemListenerValue<ByteLut> LUT = new Feature.ComboItemListenerValue(Messages.getString("ActionW.lut"), "lut", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue INVERT_LUT = new Feature.ToggleButtonListenerValue(Messages.getString("ActionW.invert_lut"), "inverseLut", 0, 0, null);
    public static final Feature.BasicActionStateValue RESET = new Feature.BasicActionStateValue(Messages.getString("ActionW.Reset"), "reset", 0, 0, null);
    public static final Feature.BasicActionStateValue SHOW_HEADER = new Feature.BasicActionStateValue(Messages.getString("ActionW.show_header"), "show_header", 0, 0, null);
    public static final Feature.BasicActionStateValue EXPORT_VIEW = new Feature.BasicActionStateValue(Messages.getString("exporting.view"), "exportImage", 0, 0, null);
    public static final Feature.PannerListenerValue PAN = new Feature.PannerListenerValue(Messages.getString("ActionW.pan"), "pan", 84, 0, Feature.getSvgCursor("pan.svg", Messages.getString("ActionW.pan"), 0.5f, 0.5f));
    public static final Feature.BasicActionStateValue DRAWINGS = new Feature.BasicActionStateValue(Messages.getString("ActionW.draw"), "drawings", 0, 0, null);
    public static final Feature.BasicActionStateValue MEASURE = new Feature.BasicActionStateValue(Messages.getString("ActionW.measure"), "measure", 77, 0, null){

        @Override
        public boolean isDrawingAction() {
            return true;
        }
    };
    public static final Feature.BasicActionStateValue DRAW = new Feature.BasicActionStateValue(Messages.getString("ActionW.draws"), "draw", 71, 0, null){

        @Override
        public boolean isDrawingAction() {
            return true;
        }
    };
    public static final Feature.ComboItemListenerValue<Graphic> DRAW_MEASURE = new Feature.ComboItemListenerValue(Messages.getString("ActionW.measurement"), "draw.sub." + MEASURE.cmd(), 0, 0, null);
    public static final Feature.ComboItemListenerValue<Graphic> DRAW_GRAPHICS = new Feature.ComboItemListenerValue(Messages.getString("ActionW.draw"), "draw.sub." + DRAW.cmd(), 0, 0, null);
    public static final Feature.ComboItemListenerValue<Unit> SPATIAL_UNIT = new Feature.ComboItemListenerValue(Messages.getString("ActionW.spatial_unit"), "spunit", 0, 0, null);
    public static final Feature.ComboItemListenerValue<SeriesComparator<?>> SORT_STACK = new Feature.ComboItemListenerValue("", "sortStack", 0, 0, null);
    public static final Feature.BasicActionStateValue CONTEXTMENU = new Feature.BasicActionStateValue(Messages.getString("ActionW.context_menu"), "contextMenu", 81, 0, null);
    public static final Feature VIEWING_PROTOCOL = new Feature.BooleanValue("", "viewingProtocol", 0, 0, null);
    public static final Feature.ComboItemListenerValue<MigLayoutModel> LAYOUT = new Feature.ComboItemListenerValue(Messages.getString("ActionW.layout"), "layout", 0, 0, null);
    public static final Feature MODE = new Feature.BooleanValue(Messages.getString("ActionW.switch_mode"), "mode", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue IMAGE_OVERLAY = new Feature.ToggleButtonListenerValue(Messages.getString("ActionW.overlay"), "overlay", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue PR_STATE = new Feature.ToggleButtonListenerValue(Messages.getString("ActionW.PR"), "pr_state", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue KO_TOGGLE_STATE = new Feature.ToggleButtonListenerValue(Messages.getString("ActionW.toggle_ko"), "ko_toogle_state", 75, 0, null);
    public static final Feature.ComboItemListenerValue<Object> KO_SELECTION = new Feature.ComboItemListenerValue(Messages.getString("ActionW.select_ko"), "ko_selection", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue KO_FILTER = new Feature.ToggleButtonListenerValue(Messages.getString("ActionW.filter_ko"), "ko_filter", 0, 0, null);
    public static final Feature IMAGE_PIX_PADDING = new Feature.BooleanValue(Messages.getString("ActionW.pixpad"), "pixel_padding", 0, 0, null);
    public static final Feature IMAGE_SHUTTER = new Feature.BooleanValue(Messages.getString("ActionW.shutter"), "shutter", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue INVERSE_STACK = new Feature.ToggleButtonListenerValue("", "inverseStack", 0, 0, null);
    public static final Feature STACK_OFFSET = new Feature.BooleanValue("", "stackOffset", 0, 0, null);
    public static final Feature SYNCH_LINK = new Feature.BooleanValue("", "synchLink", 0, 0, null);
    public static final Feature SYNCH_CROSSLINE = new Feature.BooleanValue("", "synchCrossline", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue LENS = new Feature.ToggleButtonListenerValue("", "showLens", 0, 0, null);
    public static final Feature.ComboItemListenerValue<KernelData> FILTER = new Feature.ComboItemListenerValue("", "filter", 0, 0, null);
    public static final Feature CROP = new Feature.BooleanValue("", "crop", 0, 0, null);
    public static final Feature PREPROCESSING = new Feature.BooleanValue("", "preprocessing", 0, 0, null);
    public static final Feature.SliderChangeListenerValue LENS_ZOOM = new Feature.SliderChangeListenerValue("", "lensZoom", 0, 0, null);
    public static final Feature LENS_PAN = new Feature.BooleanValue("", "lensPan", 0, 0, null);
    public static final Feature.ToggleButtonListenerValue DRAW_ONLY_ONCE = new Feature.ToggleButtonListenerValue(Messages.getString("ActionW.draw_once"), "drawOnce", 0, 0, null);
    public static final Feature PROGRESSION = new Feature.BooleanValue("", "img_progress", 0, 0, null);
    public static final Feature FILTERED_SERIES = new Feature.BooleanValue("", "filter_series", 0, 0, null);
    public static final Feature.CrosshairListenerValue CROSSHAIR = new Feature.CrosshairListenerValue(Messages.getString("ActionW.crosshair"), "crosshair", 72, 0, new Cursor(1));
    public static final Feature.BasicActionStateValue VOLUME = new Feature.BasicActionStateValue(Messages.getString("ActionW.volume"), "volume.enable", 0, 0, null);
}

