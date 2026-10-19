/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 */
package org.weasis.core.ui.editor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.command.Option;
import org.weasis.core.api.command.Options;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.AbstractFileModel;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.net.ClosableURLConnection;
import org.weasis.core.api.net.NetworkUtil;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.util.FileUtil;

public class FileModel
extends AbstractFileModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileModel.class);
    public static final Path IMAGE_CACHE_DIR = AppProperties.buildAccessibleTempDirectory("cache", "image");

    private Path getFile(String url) {
        Path outFile;
        try (ClosableURLConnection http = NetworkUtil.getUrlConnection(url, URLParameters.DEFAULT);
             InputStream in = http.getInputStream();){
            outFile = Files.createTempFile(IMAGE_CACHE_DIR, "img_", FileUtil.getExtension((String)url), new FileAttribute[0]);
            LOGGER.debug("Start to download image {} to {}.", (Object)url, (Object)outFile);
            FileUtil.writeStreamWithIOException((InputStream)in, (Path)outFile);
        }
        catch (IOException e) {
            LOGGER.error("Dowloading image", (Throwable)e);
            return null;
        }
        return outFile;
    }

    @Override
    public void get(String[] argv) throws IOException {
        String[] usage = new String[]{"Load images remotely or locally", "Usage: image:get ([-f file]... [-u url]...)", "  -f --file=FILE     open an image from a file", "  -u --url=URL       open an image from an URL", "  -? --help          show help"};
        Option opt = Options.compile(usage).parse(argv);
        List<String> fargs = opt.getList("file");
        List<String> uargs = opt.getList("url");
        if (opt.isSet("help") || fargs.isEmpty() && uargs.isEmpty()) {
            opt.usage();
            return;
        }
        GuiExecutor.execute(() -> {
            FileModel dataModel = ViewerPluginBuilder.DefaultDataModel;
            dataModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.SELECT, dataModel, null, (Object)dataModel));
            if (opt.isSet("file")) {
                fargs.stream().map(x$0 -> Path.of(x$0, new String[0])).filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).forEach(f -> ViewerPluginBuilder.openInDefaultViewer(f, ViewerOpenOptions.defaults()));
            }
            if (opt.isSet("url")) {
                uargs.stream().map(this::getFile).forEach(f -> ViewerPluginBuilder.openInDefaultViewer(f, ViewerOpenOptions.defaults()));
            }
        });
    }
}

