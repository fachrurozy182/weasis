/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 */
package org.weasis.core.ui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JPanel;
import javax.swing.plaf.PanelUI;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.gui.layout.LayoutCellManager;
import org.weasis.core.api.gui.layout.MigCell;
import org.weasis.core.api.gui.layout.MigLayoutModel;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.ExportImage;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewCanvas;

public class ExportLayout<E extends ImageElement>
extends JPanel {
    protected final JPanel grid = new JPanel();
    protected final LayoutCellManager<E> cellManager;

    public ExportLayout(ImageViewerPlugin<E> container) {
        super(new BorderLayout());
        MigLayoutModel layoutModel = container.getLayoutModel().copy();
        this.cellManager = new LayoutCellManager(layoutModel);
        this.initGrid();
        this.grid.setLayout((LayoutManager)new MigLayout(layoutModel.getLayoutConstraints(), layoutModel.getColumnConstraints(), layoutModel.getRowConstraints()));
        List<LayoutCellManager.CellEntry<E>> entries = container.getCellManager().getAllEntries();
        for (LayoutCellManager.CellEntry<E> sourceEntry : entries) {
            MigCell cell = sourceEntry.getCell();
            Optional<MigCell> modelCell = this.findCellInModel(layoutModel, cell.position());
            if (modelCell.isEmpty()) continue;
            String constraints = modelCell.get().getFullConstraints();
            if (sourceEntry.isViewCanvas()) {
                ViewCanvas viewCanvas = sourceEntry.getViewCanvas().orElse(null);
                if (viewCanvas == null) continue;
                ExportImage export = new ExportImage(viewCanvas);
                export.getInfoLayer().setBorder(3);
                this.cellManager.addComponent(cell.position(), export);
                this.grid.add(export, constraints);
                continue;
            }
            Component comp = sourceEntry.getComponent();
            this.cellManager.addComponent(cell.position(), comp);
            this.grid.add(comp, constraints);
        }
        layoutModel.applyConstraintsToLayout(this.grid);
        this.grid.revalidate();
    }

    public ExportLayout(ViewCanvas<E> viewCanvas) {
        super(new BorderLayout());
        MigLayoutModel layoutModel = ImageViewerPlugin.VIEWS_1x1.copy();
        this.cellManager = new LayoutCellManager(layoutModel);
        this.initGrid();
        this.grid.setLayout((LayoutManager)new MigLayout(layoutModel.getLayoutConstraints(), layoutModel.getColumnConstraints(), layoutModel.getRowConstraints()));
        MigCell cell = (MigCell)layoutModel.getCells().getFirst();
        ExportImage<E> export = new ExportImage<E>(viewCanvas);
        export.getInfoLayer().setBorder(3);
        this.cellManager.addComponent(cell.position(), export);
        this.grid.add(export, cell.getFullConstraints());
        this.grid.revalidate();
    }

    private void initGrid() {
        this.grid.setUI(new PanelUI(this){
            {
                Objects.requireNonNull(this$0);
            }
        });
        this.setGridBackground(Color.BLACK);
        this.add((Component)this.grid, "Center");
    }

    public void setGridBackground(Color bg) {
        this.grid.setBackground(bg);
    }

    public MigLayoutModel getLayoutModel() {
        return this.cellManager.getLayoutModel();
    }

    private Optional<MigCell> findCellInModel(MigLayoutModel layoutModel, int position) {
        return layoutModel.getCells().stream().filter(c -> c.position() == position).findFirst();
    }

    public Component findComponentForCell(MigCell cell) {
        return this.cellManager.getComponent(cell.position());
    }

    public void dispose() {
        for (Component c : this.cellManager.getAllComponents()) {
            if (!(c instanceof ExportImage)) continue;
            ExportImage exportImage = (ExportImage)c;
            exportImage.disposeView();
        }
        this.cellManager.clear();
    }
}

