/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.component.ComponentContext
 *  org.osgi.service.component.annotations.Activate
 *  org.osgi.service.component.annotations.Deactivate
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.docking;

import java.util.Hashtable;
import java.util.Objects;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.InsertableFactory;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.docking.DockableTool;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;

public abstract class ExtToolFactory<E extends ImageElement>
implements InsertableFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtToolFactory.class);
    protected final String name;
    protected Insertable toolPane = null;

    protected ExtToolFactory(String name) {
        this.name = Objects.requireNonNull(name);
    }

    protected abstract ImageViewerEventManager<E> getImageViewerEventManager();

    protected abstract boolean isCompatible(Hashtable<String, Object> var1);

    protected abstract Insertable getInstance(Hashtable<String, Object> var1);

    public void hideTool() {
        Insertable insertable = this.toolPane;
        if (insertable instanceof DockableTool) {
            DockableTool dockableTool = (DockableTool)insertable;
            dockableTool.closeDockable();
        }
    }

    @Override
    public Insertable createInstance(Hashtable<String, Object> properties) {
        Insertable insertable;
        boolean compatible = this.isCompatible(properties);
        if (!compatible) {
            return null;
        }
        if (this.toolPane == null && (insertable = (this.toolPane = this.getInstance(properties))) instanceof SeriesViewerListener) {
            SeriesViewerListener listener = (SeriesViewerListener)((Object)insertable);
            ImageViewerEventManager<E> manager = this.getImageViewerEventManager();
            if (manager != null) {
                manager.addSeriesViewerListener(listener);
            }
        }
        return this.toolPane;
    }

    @Override
    public void dispose(Insertable tool) {
        if (tool instanceof SeriesViewerListener) {
            SeriesViewerListener listener = (SeriesViewerListener)((Object)tool);
            ImageViewerEventManager<E> manager = this.getImageViewerEventManager();
            if (manager != null) {
                manager.removeSeriesViewerListener(listener);
            }
        }
        this.toolPane = null;
    }

    @Override
    public Insertable.Type getType() {
        return Insertable.Type.TOOL_EXT;
    }

    @Activate
    protected void activate(ComponentContext context) {
        LOGGER.info("Activate the {} panel", (Object)this.name);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        LOGGER.info("Deactivate the {} panel", (Object)this.name);
    }
}

