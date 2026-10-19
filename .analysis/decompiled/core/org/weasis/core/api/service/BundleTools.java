/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.InvalidSyntaxException
 *  org.osgi.framework.ServiceEvent
 *  org.osgi.framework.ServiceReference
 *  org.osgi.service.prefs.Preferences
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.api.service;

import java.util.Collections;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.InsertableFactory;
import org.weasis.core.api.gui.InsertableUtil;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.ui.docking.DockableTool;
import org.weasis.core.ui.editor.SeriesViewerUI;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.util.Toolbar;

public class BundleTools {
    private static final Logger LOGGER = LoggerFactory.getLogger(BundleTools.class);

    private BundleTools() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Codec<MediaElement> getCodec(String mimeType, String preferredCodec) {
        List<Codec<MediaElement>> codecs;
        Codec<MediaElement> codec = null;
        List<Codec<MediaElement>> list = codecs = GuiUtils.getUICore().getCodecPlugins();
        synchronized (list) {
            for (Codec<MediaElement> c : codecs) {
                if (!c.isMimeTypeSupported(mimeType)) continue;
                if (c.getCodecName().equals(preferredCodec)) {
                    codec = c;
                    break;
                }
                if (codec != null) continue;
                codec = c;
            }
            return codec;
        }
    }

    public static String createServiceFilter(Class<?> ... interfaces) {
        StringBuilder builder = new StringBuilder();
        builder.append("( |");
        for (Class<?> clazz : interfaces) {
            builder.append(String.format("(%s=%s) ", "objectClass", clazz.getName()));
        }
        builder.append(" ) ");
        return builder.toString();
    }

    public static void dataExplorerChanged(ServiceEvent event, SeriesViewerUI ui) {
        ServiceReference mref = event.getServiceReference();
        if (Boolean.parseBoolean((String)mref.getProperty(ui.clazz.getName()))) {
            BundleContext context = AppProperties.getBundleContext(ui.clazz);
            if (context == null) {
                return;
            }
            GuiExecutor.execute(() -> {
                Object service = context.getService(mref);
                if (service instanceof InsertableFactory) {
                    InsertableFactory factory = (InsertableFactory)service;
                    if (event.getType() == 1) {
                        BundleTools.registerComponent(factory, ui);
                    } else if (event.getType() == 4) {
                        if (Insertable.Type.TOOLBAR.equals((Object)factory.getType())) {
                            BundleTools.unregisterToolBar(factory, context, ui);
                        } else if (Insertable.Type.TOOL.equals((Object)factory.getType())) {
                            BundleTools.unregisterTool(factory, context, ui);
                        } else if (Insertable.Type.TOOL_EXT.equals((Object)factory.getType())) {
                            BundleTools.unregisterDynamicTool(factory, ui);
                        }
                    }
                }
            });
        }
    }

    public static void registerExistingComponents(BundleContext bundleContext, SeriesViewerUI ui) {
        try {
            for (ServiceReference serviceReference : bundleContext.getServiceReferences(InsertableFactory.class, null)) {
                if (!Boolean.parseBoolean((String)serviceReference.getProperty(ui.clazz.getName()))) continue;
                GuiExecutor.execute(() -> BundleTools.registerComponent((InsertableFactory)bundleContext.getService(serviceReference), ui));
            }
        }
        catch (InvalidSyntaxException e1) {
            LOGGER.error("Register tool and toolbar", (Throwable)e1);
        }
    }

    private static void registerComponent(InsertableFactory factory, SeriesViewerUI ui) {
        if (factory == null) {
            return;
        }
        if (Insertable.Type.TOOLBAR.equals((Object)factory.getType())) {
            BundleTools.registerToolBar(factory.createInstance(null), ui);
        } else if (Insertable.Type.TOOL.equals((Object)factory.getType())) {
            BundleTools.registerTool(factory.createInstance(null), ui);
        } else if (Insertable.Type.TOOL_EXT.equals((Object)factory.getType())) {
            BundleTools.registerToolExt(factory, ui);
        }
    }

    private static void registerToolBar(Insertable instance, SeriesViewerUI ui) {
        List<Toolbar> toolBars = ui.getToolBars();
        if (instance instanceof Toolbar) {
            Toolbar bar = (Toolbar)instance;
            if (!toolBars.contains(instance)) {
                toolBars.add(bar);
                if (ui.clazz.getPackageName().contains("dicom")) {
                    BundleTools.notifyDicomModel(ObservableEvent.BasicAction.UPDATE_TOOLBARS, ui);
                } else {
                    BundleTools.notifyDefaultDataModel(ObservableEvent.BasicAction.UPDATE_TOOLBARS, ui);
                }
                LOGGER.debug("Add Toolbar [{}] for {}", (Object)bar, (Object)ui.clazz.getName());
            }
        }
    }

    private static void registerTool(Insertable instance, SeriesViewerUI ui) {
        DockableTool tool;
        List<DockableTool> tools = ui.getTools();
        if (instance instanceof DockableTool && !tools.contains(tool = (DockableTool)instance)) {
            tools.add(tool);
            LOGGER.debug("Add Tool [{}] for {}", (Object)tool, (Object)ui.clazz.getName());
        }
    }

    private static void registerToolExt(InsertableFactory factory, SeriesViewerUI ui) {
        List<InsertableFactory> tools = ui.getDynamicTools();
        if (!tools.contains(factory)) {
            tools.add(factory);
            LOGGER.debug("Add Tool Extension [{}] for {}", (Object)factory, (Object)ui.clazz.getName());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void unregisterToolBar(InsertableFactory factory, BundleContext context, SeriesViewerUI ui) {
        List<Toolbar> toolBars;
        boolean updateGUI = false;
        List<Toolbar> list = toolBars = ui.getToolBars();
        synchronized (list) {
            for (int i = toolBars.size() - 1; i >= 0; --i) {
                Insertable b = toolBars.get(i);
                if (!factory.isComponentCreatedByThisFactory(b)) continue;
                Preferences prefs = BundlePreferences.getDefaultPreferences(context);
                if (prefs != null) {
                    List<Insertable> list2 = Collections.singletonList(b);
                    InsertableUtil.savePreferences(list2, prefs.node(ui.clazz.getSimpleName().toLowerCase()), Insertable.Type.TOOLBAR);
                }
                toolBars.remove(i);
                factory.dispose(b);
                updateGUI = true;
            }
        }
        if (updateGUI) {
            BundleTools.notifyDicomModel(ObservableEvent.BasicAction.UPDATE_TOOLBARS, ui);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void unregisterTool(InsertableFactory factory, BundleContext context, SeriesViewerUI ui) {
        List<DockableTool> tools;
        List<DockableTool> list = tools = ui.getTools();
        synchronized (list) {
            for (int i = tools.size() - 1; i >= 0; --i) {
                DockableTool t = tools.get(i);
                if (!factory.isComponentCreatedByThisFactory(t)) continue;
                Preferences prefs = BundlePreferences.getDefaultPreferences(context);
                if (prefs != null) {
                    Preferences containerNode = prefs.node(ui.clazz.getSimpleName().toLowerCase());
                    InsertableUtil.savePreferences(Collections.singletonList(t), containerNode, Insertable.Type.TOOL);
                }
                tools.remove(i);
                factory.dispose(t);
                t.closeDockable();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void unregisterDynamicTool(InsertableFactory factory, SeriesViewerUI ui) {
        List<InsertableFactory> tools;
        List<InsertableFactory> list = tools = ui.getDynamicTools();
        synchronized (list) {
            for (int i = tools.size() - 1; i >= 0; --i) {
                InsertableFactory t = tools.get(i);
                if (!factory.equals(t)) continue;
                tools.remove(i);
            }
        }
    }

    public static void notifyDicomModel(ObservableEvent.BasicAction action, SeriesViewerUI ui) {
        if (ui != null) {
            List<DataExplorerView> explorerPlugins = GuiUtils.getUICore().getExplorerPlugins();
            explorerPlugins.stream().map(DataExplorerView::getDataExplorerModel).filter(m -> "DICOM".equals(m.toString())).findFirst().ifPresent(model -> model.firePropertyChange(new ObservableEvent(action, ui, null, (Object)ui)));
        }
    }

    public static void notifyDefaultDataModel(ObservableEvent.BasicAction action, SeriesViewerUI ui) {
        if (ui != null) {
            ViewerPluginBuilder.DefaultDataModel.firePropertyChange(new ObservableEvent(action, ui, null, (Object)ui));
        }
    }
}

