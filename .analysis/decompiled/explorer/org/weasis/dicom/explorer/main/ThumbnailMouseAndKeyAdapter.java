/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.ShortcutManager
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.SeriesThumbnail
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ActionIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.ui.editor.ExternalDisplay
 *  org.weasis.core.ui.editor.SeriesViewer
 *  org.weasis.core.ui.editor.SeriesViewerFactory
 *  org.weasis.core.ui.editor.ViewerOpenOptions
 *  org.weasis.core.ui.editor.ViewerPlacement
 *  org.weasis.core.ui.editor.ViewerPluginBuilder
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.geometry.ImageOrientation
 */
package org.weasis.dicom.explorer.main;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.ExternalDisplay;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.core.ui.editor.ViewerPlacement;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.geometry.ImageOrientation;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.DicomSeriesHandler;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.MimeSystemAppFactory;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.main.SeriesSelectionModel;
import org.weasis.dicom.explorer.tag.DicomFieldsView;
import org.weasis.dicom.explorer.wado.LoadSeries;

public class ThumbnailMouseAndKeyAdapter
extends MouseAdapter
implements KeyListener {
    private final DicomSeries series;
    private final DicomModel dicomModel;
    private final LoadSeries loadSeries;

    public ThumbnailMouseAndKeyAdapter(DicomSeries series, DicomModel dicomModel, LoadSeries loadSeries) {
        this.series = Objects.requireNonNull(series);
        this.dicomModel = Objects.requireNonNull(dicomModel);
        this.loadSeries = loadSeries;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            ThumbnailMouseAndKeyAdapter.openSeriesInDefaultPlugin(this.series, this.dicomModel);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.requestFocusIfNeeded(e.getComponent());
        DicomExplorer explorer = ThumbnailMouseAndKeyAdapter.getDicomExplorer();
        explorer.ensurePatientComboSelection();
        SeriesSelectionModel selList = explorer.getSelectionList();
        if (SwingUtilities.isRightMouseButton(e)) {
            this.showContextMenu(e, selList);
        } else {
            selList.adjustSelection(e, this.series);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        DicomExplorer explorer = ThumbnailMouseAndKeyAdapter.getDicomExplorer();
        explorer.ensurePatientComboSelection();
        SeriesSelectionModel selList = explorer.getSelectionList();
        ShortcutManager sm = ShortcutManager.getInstance();
        if (sm.matches("explorer.open", e)) {
            this.handleEnterKey(e, selList);
        } else if (sm.matches("explorer.selectNext", e)) {
            selList.selectNext();
        } else if (sm.matches("explorer.selectPrevious", e)) {
            selList.selectPrevious();
        } else if (sm.matches("explorer.selectLast", e) || sm.matches("explorer.selectLastAlt", e)) {
            selList.selectLast();
        } else if (sm.matches("explorer.selectFirst", e) || sm.matches("explorer.selectFirstAlt", e)) {
            selList.selectFirst();
        } else if (sm.matches("explorer.selectAll", e)) {
            selList.selectAll();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void requestFocusIfNeeded(Component component) {
        if (!component.isFocusOwner()) {
            component.requestFocusInWindow();
        }
    }

    private void handleEnterKey(KeyEvent e, SeriesSelectionModel selList) {
        Object object;
        selList.ensureSelection(this.series);
        if (selList.openSelectedSeries(this.dicomModel) && (object = e.getSource()) instanceof JComponent) {
            JComponent jComponent = (JComponent)object;
            jComponent.requestFocusInWindow();
        }
        e.consume();
    }

    private void showContextMenu(MouseEvent e, SeriesSelectionModel selList) {
        JPopupMenu popupMenu = new JPopupMenu();
        this.ensureSeriesSelected(selList);
        List<MediaSeries<? extends MediaElement>> seriesList = this.getFilteredSeriesList(selList);
        this.addViewerMenuItems(popupMenu, seriesList);
        this.addSelectionMenuItems(popupMenu, selList);
        this.addLoadSeriesMenuItems(popupMenu, selList);
        this.addMergeMenuItem(popupMenu, selList, seriesList);
        this.addRemovalMenuItems(popupMenu, selList);
        this.addSplitPhases(popupMenu, selList);
        this.addThumbnailMenuItems(popupMenu, selList);
        popupMenu.show(e.getComponent(), e.getX() - 5, e.getY() - 5);
    }

    private void ensureSeriesSelected(SeriesSelectionModel selList) {
        if (!selList.contains(this.series)) {
            selList.add(this.series);
        }
    }

    private List<MediaSeries<? extends MediaElement>> getFilteredSeriesList(SeriesSelectionModel selList) {
        String mime = this.series.getMimeType();
        boolean multipleMimes = this.hasMultipleMimeTypes(selList, mime);
        if (multipleMimes) {
            return selList.stream().filter(s -> mime.equals(s.getMimeType())).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        return new ArrayList<DicomSeries>(selList);
    }

    private boolean hasMultipleMimeTypes(SeriesSelectionModel selList, String mime) {
        return selList.stream().anyMatch(s -> !mime.equals(s.getMimeType()));
    }

    private void addViewerMenuItems(JPopupMenu popupMenu, List<MediaSeries<? extends MediaElement>> seriesList) {
        List plugins = GuiUtils.getUICore().getViewerFactoryList(new String[]{this.series.getMimeType()});
        for (SeriesViewerFactory viewerFactory : plugins) {
            if (!viewerFactory.canReadSeries((MediaSeries)this.series)) continue;
            JMenu menuFactory = this.createViewerFactoryMenu(viewerFactory, seriesList);
            popupMenu.add(menuFactory);
            if (!(viewerFactory instanceof MimeSystemAppFactory)) continue;
            this.addMetadataMenuItem(popupMenu, viewerFactory);
        }
    }

    private JMenu createViewerFactoryMenu(SeriesViewerFactory viewerFactory, List<MediaSeries<? extends MediaElement>> seriesList) {
        JMenu menuFactory = new JMenu(viewerFactory.getUIName());
        menuFactory.setIcon(viewerFactory.getIcon());
        GuiUtils.applySelectedIconEffect((AbstractButton)menuFactory);
        this.addOpenMenuItem(menuFactory, viewerFactory, seriesList);
        if (viewerFactory.canExternalizeSeries()) {
            this.addExternalizeMenuItems(menuFactory, viewerFactory, seriesList);
        }
        if (viewerFactory.canAddSeries()) {
            this.addAddMenuItem(menuFactory, viewerFactory, seriesList);
        }
        return menuFactory;
    }

    private void addOpenMenuItem(JMenu menuFactory, SeriesViewerFactory viewerFactory, List<MediaSeries<? extends MediaElement>> seriesList) {
        JMenuItem openItem = new JMenuItem(Messages.getString("DicomExplorer.open"));
        openItem.addActionListener(e -> this.executeWithOpeningSeries(() -> new ViewerPluginBuilder(viewerFactory, seriesList, (DataExplorerModel)this.dicomModel, ViewerOpenOptions.defaults()).open()));
        menuFactory.add(openItem);
    }

    private void addExternalizeMenuItems(JMenu menuFactory, SeriesViewerFactory viewerFactory, List<MediaSeries<? extends MediaElement>> seriesList) {
        JMenuItem newWindowItem = new JMenuItem(Messages.getString("DicomExplorer.open_win"), (Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.OPEN_NEW_TAB));
        GuiUtils.applySelectedIconEffect((AbstractButton)newWindowItem);
        newWindowItem.addActionListener(e -> this.executeWithOpeningSeries(() -> new ViewerPluginBuilder(viewerFactory, seriesList, (DataExplorerModel)this.dicomModel, ViewerOpenOptions.builder().placement((ViewerPlacement)ViewerPlacement.newTab()).build()).open()));
        menuFactory.add(newWindowItem);
        this.addScreenMenuItems(menuFactory, viewerFactory, seriesList);
    }

    private void addScreenMenuItems(JMenu menuFactory, SeriesViewerFactory viewerFactory, List<MediaSeries<? extends MediaElement>> seriesList) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        if (gd.length > 1) {
            JMenu subMenu = new JMenu(Messages.getString("DicomExplorer.open_screen"));
            for (GraphicsDevice graphicsDevice : gd) {
                JMenuItem screenItem = new JMenuItem(graphicsDevice.toString());
                screenItem.addActionListener(e -> this.executeWithOpeningSeries(() -> new ViewerPluginBuilder(viewerFactory, seriesList, (DataExplorerModel)this.dicomModel, ViewerOpenOptions.builder().placement((ViewerPlacement)ViewerPlacement.external((ExternalDisplay)ExternalDisplay.onScreen((GraphicsDevice)graphicsDevice))).build()).open()));
                subMenu.add(screenItem);
            }
            menuFactory.add(subMenu);
        }
    }

    private void addAddMenuItem(JMenu menuFactory, SeriesViewerFactory viewerFactory, List<MediaSeries<? extends MediaElement>> seriesList) {
        JMenuItem addItem = new JMenuItem(Messages.getString("DicomExplorer.add"));
        addItem.addActionListener(e -> this.executeWithOpeningSeries(() -> new ViewerPluginBuilder(viewerFactory, seriesList, (DataExplorerModel)this.dicomModel, ViewerOpenOptions.builder().placement((ViewerPlacement)ViewerPlacement.reuseViewer((boolean)false, (boolean)false)).build()).open()));
        menuFactory.add(addItem);
    }

    private void addMetadataMenuItem(JPopupMenu popupMenu, SeriesViewerFactory viewerFactory) {
        JMenuItem metadataItem = new JMenuItem(Messages.getString("DicomExplorer.open_info"), (Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.METADATA));
        GuiUtils.applySelectedIconEffect((AbstractButton)metadataItem);
        metadataItem.addActionListener(e -> {
            SeriesViewer viewer = viewerFactory.createSeriesViewer(null, null);
            MediaElement dcm = this.series.getMedia(MediaSeries.MEDIA_POSITION.FIRST, null, null);
            DicomFieldsView.showHeaderDialog(viewer, (MediaSeries<? extends MediaElement>)this.series, dcm);
        });
        popupMenu.add(metadataItem);
    }

    private void addSelectionMenuItems(JPopupMenu popupMenu, SeriesSelectionModel selList) {
        if (selList.size() == 1) {
            popupMenu.add(new JSeparator());
            this.addRelatedSeriesMenuItem(popupMenu, selList);
            this.addRelatedSeriesAxisMenuItem(popupMenu, selList);
        }
    }

    private void addRelatedSeriesMenuItem(JPopupMenu popupMenu, SeriesSelectionModel selList) {
        JMenuItem relatedItem = new JMenuItem(Messages.getString("DicomExplorer.sel_rel_series"));
        relatedItem.addActionListener(e -> this.selectRelatedSeries(selList, false));
        popupMenu.add(relatedItem);
    }

    private void addRelatedSeriesAxisMenuItem(JPopupMenu popupMenu, SeriesSelectionModel selList) {
        JMenuItem relatedAxisItem = new JMenuItem(Messages.getString("DicomExplorer.sel_rel_series_axis"));
        relatedAxisItem.addActionListener(e -> this.selectRelatedSeries(selList, true));
        popupMenu.add(relatedAxisItem);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void selectRelatedSeries(SeriesSelectionModel selList, boolean checkOrientation) {
        String fruid = (String)TagD.getTagValue((TagReadable)this.series, (int)0x200052, String.class);
        if (fruid == null) {
            return;
        }
        selList.clear();
        MediaSeriesGroup studyGroup = this.dicomModel.getParent((MediaSeriesGroup)this.series, DicomModel.study);
        DicomModel dicomModel = this.dicomModel;
        synchronized (dicomModel) {
            this.dicomModel.getChildren(studyGroup).stream().filter(seq -> seq instanceof DicomSeries).map(seq -> (DicomSeries)seq).filter(s -> fruid.equals(TagD.getTagValue((TagReadable)s, (int)0x200052))).filter(s -> !checkOrientation || ImageOrientation.hasSameOrientation((MediaSeries)this.series, (MediaSeries)s)).forEach(selList::add);
        }
    }

    private void addLoadSeriesMenuItems(JPopupMenu popupMenu, SeriesSelectionModel selList) {
        if (selList.size() == 1 && this.loadSeries != null) {
            if (this.loadSeries.isStopped()) {
                popupMenu.add(new JSeparator());
                JMenuItem resumeItem = new JMenuItem(Messages.getString("LoadSeries.resume"), (Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.EXECUTE));
                resumeItem.addActionListener(e -> this.loadSeries.resume());
                popupMenu.add(resumeItem);
            } else if (!this.loadSeries.isDone()) {
                popupMenu.add(new JSeparator());
                JMenuItem stopItem = new JMenuItem(Messages.getString("LoadSeries.stop"), (Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.SUSPEND));
                stopItem.addActionListener(e -> this.loadSeries.stop());
                popupMenu.add(stopItem);
            }
        }
    }

    private void addMergeMenuItem(JPopupMenu popupMenu, SeriesSelectionModel selList, List<MediaSeries<? extends MediaElement>> seriesList) {
        Object splitNb = this.series.getTagValue(TagW.SplitSeriesNumber);
        if (splitNb != null && seriesList.size() > 1 && this.canMergeSeries(seriesList)) {
            popupMenu.add(new JSeparator());
            JMenuItem mergeItem = new JMenuItem(Messages.getString("DicomExplorer.merge"));
            mergeItem.addActionListener(e -> {
                selList.clear();
                this.dicomModel.mergeSeries(seriesList);
            });
            popupMenu.add(mergeItem);
        }
    }

    private boolean canMergeSeries(List<MediaSeries<? extends MediaElement>> seriesList) {
        String uid = (String)TagD.getTagValue((TagReadable)this.series, (int)0x20000E, String.class);
        return uid != null && seriesList.stream().allMatch(s -> uid.equals(TagD.getTagValue((TagReadable)s, (int)0x20000E)));
    }

    private void addRemovalMenuItems(JPopupMenu popupMenu, SeriesSelectionModel selList) {
        popupMenu.add(new JSeparator());
        JMenuItem removeSeriesItem = new JMenuItem(Messages.getString("DicomExplorer.rem_series"));
        removeSeriesItem.addActionListener(e -> {
            for (int i = selList.size() - 1; i >= 0; --i) {
                this.dicomModel.removeSeries((MediaSeriesGroup)selList.get(i));
            }
            selList.clear();
        });
        popupMenu.add(removeSeriesItem);
        if (selList.size() == 1) {
            this.addRemoveStudyMenuItem(popupMenu, selList);
            this.addRemovePatientMenuItem(popupMenu, selList);
        }
    }

    private void addRemoveStudyMenuItem(JPopupMenu popupMenu, SeriesSelectionModel selList) {
        JMenuItem removeStudyItem = new JMenuItem(Messages.getString("DicomExplorer.rem_study"));
        removeStudyItem.addActionListener(e -> {
            MediaSeriesGroup studyGroup = this.dicomModel.getParent((MediaSeriesGroup)this.series, DicomModel.study);
            this.dicomModel.removeStudy(studyGroup);
            selList.clear();
        });
        popupMenu.add(removeStudyItem);
    }

    private void addRemovePatientMenuItem(JPopupMenu popupMenu, SeriesSelectionModel selList) {
        JMenuItem removePatientItem = new JMenuItem(Messages.getString("DicomExplorer.rem_pat"));
        removePatientItem.addActionListener(e -> {
            MediaSeriesGroup patientGroup = this.dicomModel.getParent(this.dicomModel.getParent((MediaSeriesGroup)this.series, DicomModel.study), DicomModel.patient);
            this.dicomModel.removePatient(patientGroup);
            selList.clear();
        });
        popupMenu.add(removePatientItem);
    }

    private void addSplitPhases(JPopupMenu popupMenu, SeriesSelectionModel selList) {
        if (selList.size() == 1 && this.series.size(null) > 1 && LoadLocalDicom.isMultiPhaseSeries(this.series)) {
            popupMenu.add(new JSeparator());
            JMenuItem item2 = new JMenuItem(Messages.getString("separate.phases"));
            item2.addActionListener(actionEvent -> LoadLocalDicom.seriesPostProcessing(this.series, this.dicomModel, true));
            popupMenu.add(item2);
        }
    }

    private void addThumbnailMenuItems(JPopupMenu popupMenu, SeriesSelectionModel selList) {
        if (selList.size() == 1 && this.series.size(null) > 1) {
            popupMenu.add(new JSeparator());
            JMenu thumbnailMenu = new JMenu(Messages.getString("DicomExplorer.build_thumb"));
            this.addThumbnailMenuItem(thumbnailMenu, "DicomExplorer.from_1", MediaSeries.MEDIA_POSITION.FIRST);
            this.addThumbnailMenuItem(thumbnailMenu, "DicomExplorer.from_mid", MediaSeries.MEDIA_POSITION.MIDDLE);
            this.addThumbnailMenuItem(thumbnailMenu, "DicomExplorer.from_last", MediaSeries.MEDIA_POSITION.LAST);
            popupMenu.add(thumbnailMenu);
        }
    }

    private void addThumbnailMenuItem(JMenu menu, String messageKey, MediaSeries.MEDIA_POSITION position) {
        JMenuItem item = new JMenuItem(Messages.getString(messageKey));
        item.addActionListener(actionEvent -> {
            SeriesThumbnail thumbnail = (SeriesThumbnail)this.series.getTagValue(TagW.Thumbnail);
            if (thumbnail != null) {
                thumbnail.reBuildThumbnail(position);
            }
        });
        menu.add(item);
    }

    private void executeWithOpeningSeries(Runnable action) {
        SeriesSelectionModel selList = ThumbnailMouseAndKeyAdapter.getSeriesSelectionModel();
        selList.setOpeningSeries(true);
        try {
            action.run();
        }
        finally {
            selList.setOpeningSeries(false);
        }
    }

    public static DicomExplorer getDicomExplorer() {
        DataExplorerView explorer = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME);
        if (explorer instanceof DicomExplorer) {
            DicomExplorer dicomExplorer = (DicomExplorer)explorer;
            return dicomExplorer;
        }
        throw new IllegalStateException("DicomExplorer plugin is not available, cannot get SeriesSelectionModel");
    }

    public static SeriesSelectionModel getSeriesSelectionModel() {
        return ThumbnailMouseAndKeyAdapter.getDicomExplorer().getSelectionList();
    }

    public static void openSeriesInDefaultPlugin(DicomSeries series, DicomModel dicomModel) {
        SeriesSelectionModel selList = ThumbnailMouseAndKeyAdapter.getSeriesSelectionModel();
        selList.setOpeningSeries(true);
        try {
            DicomSeriesHandler.openDicomSeriesInViewer(series, dicomModel);
        }
        finally {
            selList.setOpeningSeries(false);
        }
    }
}

