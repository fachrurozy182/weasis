/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.ui.FlatNativeLinuxLibrary;
import com.formdev.flatlaf.ui.FlatNativeMacLibrary;
import com.formdev.flatlaf.ui.FlatNativeWindowsLibrary;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

public class SystemFileChooser {
    public static final int OPEN_DIALOG = 0;
    public static final int SAVE_DIALOG = 1;
    public static final int CANCEL_OPTION = 1;
    public static final int APPROVE_OPTION = 0;
    public static final int FILES_ONLY = 0;
    public static final int DIRECTORIES_ONLY = 1;
    private int dialogType = 0;
    private String dialogTitle;
    private String approveButtonText;
    private int approveButtonMnemonic = 0;
    private int fileSelectionMode = 0;
    private boolean multiSelection;
    private boolean useFileHiding = true;
    private File currentDirectory;
    private File selectedFile;
    private File[] selectedFiles;
    private final ArrayList<FileFilter> filters = new ArrayList();
    private FileFilter fileFilter;
    private AcceptAllFileFilter acceptAllFileFilter;
    private boolean useAcceptAllFileFilter = true;
    private boolean keepAcceptAllAtEnd = true;
    private ApproveCallback approveCallback;
    private int approveResult = 0;
    public static final String WINDOWS_FILE_NAME_LABEL = "windows.fileNameLabel";
    public static final String WINDOWS_DEFAULT_FOLDER = "windows.defaultFolder";
    public static final String WINDOWS_DEFAULT_EXTENSION = "windows.defaultExtension";
    public static final String MAC_MESSAGE = "mac.message";
    public static final String MAC_FILTER_FIELD_LABEL = "mac.filterFieldLabel";
    public static final String MAC_NAME_FIELD_LABEL = "mac.nameFieldLabel";
    public static final String MAC_TREATS_FILE_PACKAGES_AS_DIRECTORIES = "mac.treatsFilePackagesAsDirectories";
    public static final String WINDOWS_OPTIONS_SET = "windows.optionsSet";
    public static final String WINDOWS_OPTIONS_CLEAR = "windows.optionsClear";
    public static final String MAC_OPTIONS_SET = "mac.optionsSet";
    public static final String MAC_OPTIONS_CLEAR = "mac.optionsClear";
    public static final String LINUX_OPTIONS_SET = "linux.optionsSet";
    public static final String LINUX_OPTIONS_CLEAR = "linux.optionsClear";
    private Map<String, Object> platformProperties;
    private static final StateStore inMemoryStateStore = new StateStore(){
        private final Map<String, String> state = new HashMap<String, String>();

        @Override
        public String get(String key, String def) {
            return this.state.getOrDefault(key, def);
        }

        @Override
        public void put(String key, String value) {
            if (value != null) {
                this.state.put(key, value);
            } else {
                this.state.remove(key);
            }
        }
    };
    private static StateStore stateStore;
    private String stateStoreID;

    public SystemFileChooser() {
        this((File)null);
    }

    public SystemFileChooser(String currentDirectoryPath) {
        this(currentDirectoryPath != null ? FileSystemView.getFileSystemView().createFileObject(currentDirectoryPath) : null);
    }

    public SystemFileChooser(File currentDirectory) {
        this.setCurrentDirectory(currentDirectory);
        this.addChoosableFileFilter(this.getAcceptAllFileFilter());
        this.keepAcceptAllAtEnd = true;
    }

    public int showOpenDialog(Component parent) {
        this.setDialogType(0);
        return this.showDialogImpl(parent);
    }

    public int showSaveDialog(Component parent) {
        this.setDialogType(1);
        return this.showDialogImpl(parent);
    }

    public int showDialog(Component parent, String approveButtonText) {
        if (approveButtonText != null) {
            this.setApproveButtonText(approveButtonText);
        }
        return this.showDialogImpl(parent);
    }

    public int getDialogType() {
        return this.dialogType;
    }

    public void setDialogType(int dialogType) {
        if (dialogType != 0 && dialogType != 1) {
            throw new IllegalArgumentException("Invalid dialog type " + dialogType);
        }
        this.dialogType = dialogType;
    }

    public String getDialogTitle() {
        return this.dialogTitle;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public String getApproveButtonText() {
        return this.approveButtonText;
    }

    public void setApproveButtonText(String approveButtonText) {
        this.approveButtonText = approveButtonText;
    }

    public int getApproveButtonMnemonic() {
        return this.approveButtonMnemonic;
    }

    public void setApproveButtonMnemonic(int mnemonic) {
        this.approveButtonMnemonic = mnemonic;
    }

    public void setApproveButtonMnemonic(char mnemonic) {
        int vk = mnemonic;
        if (vk >= 97 && vk <= 122) {
            vk -= 32;
        }
        this.setApproveButtonMnemonic(vk);
    }

    public int getFileSelectionMode() {
        return this.fileSelectionMode;
    }

    public void setFileSelectionMode(int fileSelectionMode) {
        if (fileSelectionMode != 0 && fileSelectionMode != 1) {
            throw new IllegalArgumentException("Invalid file selection mode " + fileSelectionMode);
        }
        this.fileSelectionMode = fileSelectionMode;
    }

    public boolean isFileSelectionEnabled() {
        return this.fileSelectionMode == 0;
    }

    public boolean isDirectorySelectionEnabled() {
        return this.fileSelectionMode == 1;
    }

    public boolean isMultiSelectionEnabled() {
        return this.multiSelection;
    }

    public void setMultiSelectionEnabled(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }

    public boolean isFileHidingEnabled() {
        return this.useFileHiding;
    }

    public void setFileHidingEnabled(boolean useFileHiding) {
        this.useFileHiding = useFileHiding;
    }

    public File getCurrentDirectory() {
        if (this.currentDirectory == null) {
            StateStore store = stateStore != null ? stateStore : inMemoryStateStore;
            String path = store.get(this.buildStateKey("currentDirectory"), null);
            if (path != null) {
                this.currentDirectory = this.getTraversableDirectory(FileSystemView.getFileSystemView().createFileObject(path));
            }
            if (this.currentDirectory == null) {
                this.currentDirectory = this.getTraversableDirectory(FileSystemView.getFileSystemView().getDefaultDirectory());
            }
        }
        return this.currentDirectory;
    }

    public void setCurrentDirectory(File dir) {
        this.currentDirectory = this.getTraversableDirectory(dir);
    }

    private File getTraversableDirectory(File dir) {
        if (dir == null) {
            return null;
        }
        FileSystemView fsv = FileSystemView.getFileSystemView();
        while (dir != null && !fsv.isTraversable(dir).booleanValue()) {
            dir = fsv.getParentDirectory(dir);
        }
        return dir;
    }

    public File getSelectedFile() {
        return this.selectedFile;
    }

    public void setSelectedFile(File file) {
        this.selectedFile = file;
        if (file != null && file.isAbsolute() && !FileSystemView.getFileSystemView().isParent(this.getCurrentDirectory(), file)) {
            this.setCurrentDirectory(file.getParentFile());
        }
    }

    public File[] getSelectedFiles() {
        return this.selectedFiles != null ? (File[])this.selectedFiles.clone() : new File[]{};
    }

    public void setSelectedFiles(File[] selectedFiles) {
        if (selectedFiles != null && selectedFiles.length > 0) {
            this.selectedFiles = (File[])selectedFiles.clone();
            this.setSelectedFile(selectedFiles[0]);
        } else {
            this.selectedFiles = null;
            this.setSelectedFile(null);
        }
    }

    public FileFilter[] getChoosableFileFilters() {
        return this.filters.toArray(new FileFilter[this.filters.size()]);
    }

    public void addChoosableFileFilter(FileFilter filter) {
        if (filter == this.getAcceptAllFileFilter()) {
            this.keepAcceptAllAtEnd = false;
        }
        if (filter == null || this.filters.contains(filter)) {
            return;
        }
        this.checkSupportedFileFilter(filter);
        int size = this.filters.size();
        if (this.keepAcceptAllAtEnd && size > 0 && this.filters.get(size - 1) == this.getAcceptAllFileFilter()) {
            this.filters.add(size - 1, filter);
        } else {
            this.filters.add(filter);
        }
        if (this.fileFilter == null || this.filters.size() == 2 && this.filters.get(1) == this.getAcceptAllFileFilter()) {
            this.setFileFilter(filter);
        }
    }

    public boolean removeChoosableFileFilter(FileFilter filter) {
        if (!this.filters.remove(filter)) {
            return false;
        }
        if (filter == this.getFileFilter()) {
            if (this.isAcceptAllFileFilterUsed() && filter != this.getAcceptAllFileFilter()) {
                this.setFileFilter(this.getAcceptAllFileFilter());
            } else {
                this.setFileFilter(!this.filters.isEmpty() ? this.filters.get(0) : null);
            }
        }
        return true;
    }

    public void resetChoosableFileFilters() {
        this.filters.clear();
        this.setFileFilter(null);
        if (this.isAcceptAllFileFilterUsed()) {
            this.addChoosableFileFilter(this.getAcceptAllFileFilter());
            this.keepAcceptAllAtEnd = true;
        }
    }

    public FileFilter getAcceptAllFileFilter() {
        if (this.acceptAllFileFilter == null) {
            this.acceptAllFileFilter = new AcceptAllFileFilter();
        }
        return this.acceptAllFileFilter;
    }

    public boolean isAcceptAllFileFilterUsed() {
        return this.useAcceptAllFileFilter;
    }

    public void setAcceptAllFileFilterUsed(boolean acceptAll) {
        this.useAcceptAllFileFilter = acceptAll;
        this.removeChoosableFileFilter(this.getAcceptAllFileFilter());
        if (acceptAll) {
            this.addChoosableFileFilter(this.getAcceptAllFileFilter());
            this.keepAcceptAllAtEnd = true;
        }
    }

    public FileFilter getFileFilter() {
        return this.fileFilter;
    }

    public void setFileFilter(FileFilter filter) {
        this.checkSupportedFileFilter(filter);
        this.fileFilter = filter;
    }

    private void checkSupportedFileFilter(FileFilter filter) throws IllegalArgumentException {
        if (filter == null || filter instanceof FileNameExtensionFilter || filter instanceof PatternFilter || filter instanceof AcceptAllFileFilter) {
            return;
        }
        throw new IllegalArgumentException("Filter class not supported: " + filter.getClass().getName());
    }

    private List<FileFilter> getFiltersForDialog() {
        if (this.filters.size() == 1 && this.filters.get(0) == this.getAcceptAllFileFilter() && (this.fileFilter == this.getAcceptAllFileFilter() || this.fileFilter == null)) {
            return new ArrayList<FileFilter>();
        }
        if (this.fileFilter != null && this.filters.contains(this.fileFilter) || this.fileFilter == null) {
            return this.filters;
        }
        ArrayList<FileFilter> filters2 = new ArrayList<FileFilter>(this.filters);
        filters2.add(0, this.fileFilter);
        return filters2;
    }

    private void updateFileFilter(List<FileFilter> filters, int index) {
        if (index >= 0 && index < filters.size()) {
            this.setFileFilter(filters.get(index));
        }
    }

    public ApproveCallback getApproveCallback() {
        return this.approveCallback;
    }

    public void setApproveCallback(ApproveCallback approveCallback) {
        this.approveCallback = approveCallback;
    }

    public <T> T getPlatformProperty(String key) {
        return (T)(this.platformProperties != null ? this.platformProperties.get(key) : null);
    }

    public void putPlatformProperty(String key, Object value) {
        if (this.platformProperties == null) {
            this.platformProperties = new HashMap<String, Object>();
        }
        if (value != null) {
            this.platformProperties.put(key, value);
        } else {
            this.platformProperties.remove(key);
        }
    }

    private int getPlatformOptions(String key, int optionsBlocked) {
        Object value = this.getPlatformProperty(key);
        return value instanceof Integer ? (Integer)value & ~optionsBlocked : 0;
    }

    public static StateStore getStateStore() {
        return stateStore;
    }

    public static void setStateStore(StateStore stateStore) {
        SystemFileChooser.stateStore = stateStore;
    }

    public String getStateStoreID() {
        return this.stateStoreID;
    }

    public void setStateStoreID(String stateStoreID) {
        this.stateStoreID = stateStoreID;
    }

    private String buildStateKey(String key) {
        return this.stateStoreID != null ? this.stateStoreID + '.' + key : key;
    }

    private int showDialogImpl(Component parent) {
        Window owner;
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Must be invoked from the AWT/Swing event dispatch thread");
        }
        Window window = parent instanceof Window ? (Window)parent : (owner = parent != null ? SwingUtilities.windowForComponent(parent) : null);
        if (owner == null) {
            owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        }
        this.approveResult = 0;
        File[] files = this.getProvider().showDialog(owner, this);
        this.setSelectedFiles(files);
        if (files == null) {
            return 1;
        }
        File currentDirectory = this.getCurrentDirectory();
        StateStore store = stateStore != null ? stateStore : inMemoryStateStore;
        store.put(this.buildStateKey("currentDirectory"), currentDirectory != null ? currentDirectory.getAbsolutePath() : null);
        return this.approveResult;
    }

    private FileChooserProvider getProvider() {
        if (!FlatSystemProperties.getBoolean("flatlaf.useSystemFileChooser", true) || SystemInfo.isProjector || SystemInfo.isWebswing || SystemInfo.isWinPE) {
            return new SwingFileChooserProvider();
        }
        if (SystemInfo.isWindows_10_orLater && FlatNativeWindowsLibrary.isLoaded()) {
            return new WindowsFileChooserProvider();
        }
        if (SystemInfo.isMacOS && FlatNativeMacLibrary.isLoaded()) {
            return new MacFileChooserProvider();
        }
        if (SystemInfo.isLinux && FlatNativeLinuxLibrary.isLoaded() && FlatNativeLinuxLibrary.isGtk3Available() && !Toolkit.getDefaultToolkit().getClass().getName().endsWith(".WLToolkit")) {
            return new LinuxFileChooserProvider();
        }
        return new SwingFileChooserProvider();
    }

    public static interface StateStore {
        public static final String KEY_CURRENT_DIRECTORY = "currentDirectory";

        public String get(String var1, String var2);

        public void put(String var1, String var2);
    }

    public static abstract class ApproveContext {
        public abstract int showMessageDialog(int var1, String var2, String var3, int var4, String ... var5);
    }

    public static interface ApproveCallback {
        public int approve(File[] var1, ApproveContext var2);
    }

    private static final class AcceptAllFileFilter
    extends FileFilter {
        private AcceptAllFileFilter() {
        }

        @Override
        public String getDescription() {
            return UIManager.getString("FileChooser.acceptAllFileFilterText");
        }
    }

    public static final class PatternFilter
    extends FileFilter {
        private final String description;
        private final String[] patterns;

        public PatternFilter(String description, String ... patterns) {
            if (patterns == null || patterns.length == 0) {
                throw new IllegalArgumentException("Missing patterns");
            }
            for (String extension : patterns) {
                if (extension != null && !extension.isEmpty()) continue;
                throw new IllegalArgumentException("Pattern is null or empty string");
            }
            this.description = description;
            this.patterns = (String[])patterns.clone();
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        public String[] getPatterns() {
            return (String[])this.patterns.clone();
        }

        public String toString() {
            return super.toString() + "[description=" + this.description + " patterns=" + Arrays.toString(this.patterns) + "]";
        }
    }

    public static final class FileNameExtensionFilter
    extends FileFilter {
        private final String description;
        private final String[] extensions;

        public FileNameExtensionFilter(String description, String ... extensions) {
            if (extensions == null || extensions.length == 0) {
                throw new IllegalArgumentException("Missing extensions");
            }
            for (String extension : extensions) {
                if (extension == null || extension.isEmpty()) {
                    throw new IllegalArgumentException("Extension is null or empty string");
                }
                if (extension.indexOf(46) < 0 && extension.indexOf(42) < 0) continue;
                throw new IllegalArgumentException("Extension must not contain '.' or '*'");
            }
            this.description = description;
            this.extensions = (String[])extensions.clone();
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        public String[] getExtensions() {
            return (String[])this.extensions.clone();
        }

        public String toString() {
            return super.toString() + "[description=" + this.description + " extensions=" + Arrays.toString(this.extensions) + "]";
        }
    }

    public static abstract class FileFilter {
        public abstract String getDescription();
    }

    private static class SwingFileChooserProvider
    implements FileChooserProvider {
        private SwingFileChooserProvider() {
        }

        @Override
        public File[] showDialog(Window owner, final SystemFileChooser fc) {
            File[] fileArray;
            Object jfilter;
            List filters;
            final IdentityHashMap<javax.swing.filechooser.FileFilter, FileFilter> filterMap = new IdentityHashMap<javax.swing.filechooser.FileFilter, FileFilter>();
            JFileChooser chooser = new JFileChooser(){

                @Override
                public void approveSelection() {
                    File[] fileArray;
                    if (this.isMultiSelectionEnabled()) {
                        fileArray = this.getSelectedFiles();
                    } else {
                        File[] fileArray2 = new File[1];
                        fileArray = fileArray2;
                        fileArray2[0] = this.getSelectedFile();
                    }
                    File[] files = fileArray;
                    if (files == null || files.length == 0) {
                        return;
                    }
                    if (this.getDialogType() == 0 || this.isDirectorySelectionEnabled() ? !SwingFileChooserProvider.checkMustExist(this, files) : !SwingFileChooserProvider.checkOverwrite(this, files)) {
                        return;
                    }
                    ApproveCallback approveCallback = fc.getApproveCallback();
                    if (approveCallback != null) {
                        this.updateFileFilter(fc, this, filterMap);
                        int result = approveCallback.approve(files, new SwingApproveContext(this));
                        if (result == 1) {
                            return;
                        }
                        fc.approveResult = result;
                    }
                    super.approveSelection();
                }
            };
            chooser.setDialogType(fc.getDialogType());
            chooser.setDialogTitle(fc.getDialogTitle());
            chooser.setApproveButtonText(fc.getApproveButtonText());
            chooser.setApproveButtonMnemonic(fc.getApproveButtonMnemonic());
            chooser.setFileSelectionMode(fc.getFileSelectionMode());
            chooser.setMultiSelectionEnabled(fc.isMultiSelectionEnabled());
            chooser.setFileHidingEnabled(fc.isFileHidingEnabled());
            chooser.setAcceptAllFileFilterUsed(fc.isAcceptAllFileFilterUsed());
            if (chooser.isMultiSelectionEnabled() && chooser.getDialogType() == 1 && !chooser.isDirectorySelectionEnabled()) {
                chooser.setMultiSelectionEnabled(false);
            }
            if (!fc.isDirectorySelectionEnabled() && !(filters = fc.getFiltersForDialog()).isEmpty()) {
                FileFilter currentFilter = fc.getFileFilter();
                chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
                for (FileFilter filter : filters) {
                    jfilter = this.convertFilter(filter, chooser);
                    if (jfilter == null) continue;
                    chooser.addChoosableFileFilter((javax.swing.filechooser.FileFilter)jfilter);
                    if (filter == currentFilter) {
                        chooser.setFileFilter((javax.swing.filechooser.FileFilter)jfilter);
                    }
                    filterMap.put((javax.swing.filechooser.FileFilter)jfilter, filter);
                }
            }
            chooser.setCurrentDirectory(fc.getCurrentDirectory());
            chooser.setSelectedFile(fc.getSelectedFile());
            StateStore store = stateStore != null ? stateStore : inMemoryStateStore;
            String keyLastWindowSize = fc.buildStateKey("lastWindowSize");
            String lastWindowSize = store.get(keyLastWindowSize, null);
            if (lastWindowSize != null) {
                try {
                    Scanner s = new Scanner(lastWindowSize);
                    jfilter = null;
                    try {
                        int width = UIScale.scale(s.nextInt());
                        int height = UIScale.scale(s.nextInt());
                        Dimension prefSize = chooser.getPreferredSize();
                        chooser.setPreferredSize(new Dimension(Math.max(width, prefSize.width), Math.max(height, prefSize.height)));
                    }
                    catch (Throwable throwable) {
                        jfilter = throwable;
                        throw throwable;
                    }
                    finally {
                        if (s != null) {
                            if (jfilter != null) {
                                try {
                                    s.close();
                                }
                                catch (Throwable throwable) {
                                    ((Throwable)jfilter).addSuppressed(throwable);
                                }
                            } else {
                                s.close();
                            }
                        }
                    }
                }
                catch (RuntimeException s) {
                    // empty catch block
                }
            }
            int result = chooser.showDialog(owner, null);
            this.updateFileFilter(fc, chooser, filterMap);
            Dimension windowSize = chooser.getSize();
            if (windowSize.width > 0 && windowSize.height > 0) {
                store.put(keyLastWindowSize, UIScale.unscale(windowSize.width) + " " + UIScale.unscale(windowSize.height));
            }
            if (result != 0) {
                return null;
            }
            if (chooser.isMultiSelectionEnabled()) {
                fileArray = chooser.getSelectedFiles();
            } else {
                File[] fileArray2 = new File[1];
                fileArray = fileArray2;
                fileArray2[0] = chooser.getSelectedFile();
            }
            return fileArray;
        }

        private javax.swing.filechooser.FileFilter convertFilter(FileFilter filter, JFileChooser chooser) {
            if (filter instanceof FileNameExtensionFilter) {
                return new javax.swing.filechooser.FileNameExtensionFilter(((FileNameExtensionFilter)filter).getDescription(), ((FileNameExtensionFilter)filter).getExtensions());
            }
            if (filter instanceof PatternFilter) {
                return new SwingGlobFilter(((PatternFilter)filter).getDescription(), ((PatternFilter)filter).getPatterns());
            }
            if (filter instanceof AcceptAllFileFilter) {
                return chooser.getAcceptAllFileFilter();
            }
            return null;
        }

        private void updateFileFilter(SystemFileChooser fc, JFileChooser chooser, IdentityHashMap<javax.swing.filechooser.FileFilter, FileFilter> filterMap) {
            FileFilter fileFilter = filterMap.get(chooser.getFileFilter());
            if (fileFilter != null) {
                fc.setFileFilter(fileFilter);
            }
        }

        private static boolean checkMustExist(JFileChooser chooser, File[] files) {
            for (File file : files) {
                if (file.exists()) continue;
                String title = chooser.getDialogTitle();
                JOptionPane.showMessageDialog(chooser, file.getName() + (chooser.isDirectorySelectionEnabled() ? "\nPath does not exist.\nCheck the path and try again." : "\nFile not found.\nCheck the file name and try again."), title != null ? title : "Open", 2);
                return false;
            }
            return true;
        }

        private static boolean checkOverwrite(JFileChooser chooser, File[] files) {
            for (File file : files) {
                if (!file.exists()) continue;
                String title = chooser.getDialogTitle();
                Locale l = chooser.getLocale();
                Object[] options = new Object[]{UIManager.getString((Object)"OptionPane.yesButtonText", l), UIManager.getString((Object)"OptionPane.noButtonText", l)};
                int result = JOptionPane.showOptionDialog(chooser, file.getName() + " already exists.\nDo you want to replace it?", "Confirm " + (title != null ? title : "Save"), 0, 2, null, options, options[1]);
                return result == 0;
            }
            return true;
        }

        private static class SwingGlobFilter
        extends javax.swing.filechooser.FileFilter {
            private final String description;
            private final String[] patterns;
            private Pattern regexPattern;

            SwingGlobFilter(String description, String ... patterns) {
                this.description = description;
                this.patterns = patterns;
            }

            @Override
            public String getDescription() {
                return this.description;
            }

            @Override
            public boolean accept(File f) {
                if (f == null) {
                    return false;
                }
                if (f.isDirectory()) {
                    return true;
                }
                this.initRegexPattern();
                return this.regexPattern.matcher(f.getName()).matches();
            }

            private void initRegexPattern() {
                if (this.regexPattern != null) {
                    return;
                }
                StringBuilder buf = new StringBuilder();
                for (String pattern : this.patterns) {
                    if (buf.length() > 0) {
                        buf.append('|');
                    }
                    SwingGlobFilter.glob2regexPattern(pattern, buf);
                }
                this.regexPattern = Pattern.compile(buf.toString(), 2);
            }

            private static void glob2regexPattern(String globPattern, StringBuilder buf) {
                int globLength = globPattern.length();
                if (SystemInfo.isWindows && globPattern.endsWith("*.*")) {
                    globLength -= 2;
                }
                block5: for (int i = 0; i < globLength; ++i) {
                    char ch = globPattern.charAt(i);
                    switch (ch) {
                        case '*': {
                            buf.append(".*");
                            continue block5;
                        }
                        case '?': {
                            buf.append('.');
                            continue block5;
                        }
                        case '$': 
                        case '(': 
                        case ')': 
                        case '+': 
                        case '.': 
                        case '[': 
                        case '\\': 
                        case ']': 
                        case '^': 
                        case '{': 
                        case '|': 
                        case '}': {
                            buf.append('\\').append(ch);
                            continue block5;
                        }
                        default: {
                            buf.append(ch);
                        }
                    }
                }
            }
        }

        private static class SwingApproveContext
        extends ApproveContext {
            private final JFileChooser chooser;

            SwingApproveContext(JFileChooser chooser) {
                this.chooser = chooser;
            }

            @Override
            public int showMessageDialog(int messageType, String primaryText, String secondaryText, int defaultButton, String ... buttons) {
                Window window;
                String title = this.chooser.getDialogTitle();
                if (title == null && (window = SwingUtilities.windowForComponent(this.chooser)) instanceof JDialog) {
                    title = ((JDialog)window).getTitle();
                }
                if (secondaryText != null) {
                    primaryText = primaryText + "\n\n" + secondaryText;
                }
                for (int i = 0; i < buttons.length; ++i) {
                    buttons[i] = buttons[i].replace("__", "\u0001").replace("_", "").replace("\u0001", "_");
                }
                if (buttons.length == 0) {
                    buttons = new String[]{UIManager.getString((Object)"OptionPane.okButtonText", Locale.getDefault())};
                }
                return JOptionPane.showOptionDialog(this.chooser, primaryText, title, 0, messageType, null, buttons, buttons[Math.min(Math.max(defaultButton, 0), buttons.length - 1)]);
            }
        }
    }

    private static class LinuxFileChooserProvider
    extends SystemFileChooserProvider {
        private static Boolean fxinitialized;

        private LinuxFileChooserProvider() {
        }

        @Override
        public File[] showDialog(Window owner, SystemFileChooser fc) {
            if (this.isFXinitialized()) {
                return new SwingFileChooserProvider().showDialog(owner, fc);
            }
            return super.showDialog(owner, fc);
        }

        @Override
        String[] showSystemDialog(Window owner, SystemFileChooser fc) {
            List filters;
            int dark = FlatLaf.isLafDark() ? 1 : 0;
            boolean open = fc.getDialogType() == 0;
            String approveButtonText = fc.getApproveButtonText();
            int approveButtonMnemonic = fc.getApproveButtonMnemonic();
            String currentName = null;
            String currentFolder = null;
            if (approveButtonText != null) {
                int mnemonicIndex;
                approveButtonText = approveButtonText.replace("_", "__");
                if (approveButtonMnemonic > 0 && (mnemonicIndex = approveButtonText.toUpperCase(Locale.ENGLISH).indexOf(approveButtonMnemonic)) >= 0) {
                    approveButtonText = approveButtonText.substring(0, mnemonicIndex) + '_' + approveButtonText.substring(mnemonicIndex);
                }
            }
            File currentDirectory = fc.getCurrentDirectory();
            File selectedFile = fc.getSelectedFile();
            if (selectedFile != null) {
                if (selectedFile.isDirectory()) {
                    currentFolder = selectedFile.getAbsolutePath();
                } else {
                    currentName = selectedFile.getName();
                    currentFolder = selectedFile.getParent();
                }
            } else if (currentDirectory != null) {
                currentFolder = currentDirectory.getAbsolutePath();
            }
            int optionsBlocked = 7;
            int optionsSet = fc.getPlatformOptions(SystemFileChooser.LINUX_OPTIONS_SET, optionsBlocked);
            int optionsClear = fc.getPlatformOptions(SystemFileChooser.LINUX_OPTIONS_CLEAR, optionsBlocked);
            if ((optionsClear & 0x10) == 0) {
                optionsSet |= 0x10;
            }
            if (fc.isDirectorySelectionEnabled()) {
                optionsSet |= 1;
            }
            if (fc.isMultiSelectionEnabled()) {
                optionsSet |= 2;
            }
            if (!fc.isFileHidingEnabled()) {
                optionsSet |= 4;
            } else {
                optionsClear |= 4;
            }
            int fileTypeIndex = 0;
            ArrayList<String> fileTypes = new ArrayList<String>();
            ArrayList<FileFilter> fileTypeFilters = new ArrayList<FileFilter>();
            if (!fc.isDirectorySelectionEnabled() && !(filters = fc.getFiltersForDialog()).isEmpty()) {
                fileTypeIndex = filters.indexOf(fc.getFileFilter());
                for (FileFilter filter : filters) {
                    if (filter instanceof FileNameExtensionFilter) {
                        fileTypes.add(filter.getDescription());
                        for (String ext : ((FileNameExtensionFilter)filter).getExtensions()) {
                            fileTypes.add("*." + this.caseInsensitiveGlobPattern(ext));
                        }
                        fileTypes.add(null);
                        fileTypeFilters.add(filter);
                        continue;
                    }
                    if (filter instanceof PatternFilter) {
                        fileTypes.add(filter.getDescription());
                        for (String pattern : ((PatternFilter)filter).getPatterns()) {
                            fileTypes.add(this.caseInsensitiveGlobPattern(pattern));
                        }
                        fileTypeFilters.add(filter);
                        continue;
                    }
                    if (!(filter instanceof AcceptAllFileFilter)) continue;
                    fileTypes.add(filter.getDescription());
                    fileTypes.add("*");
                    fileTypes.add(null);
                    fileTypeFilters.add(filter);
                }
            }
            FlatNativeLinuxLibrary.FileChooserCallback callback = fc.getApproveCallback() != null ? (files, fileTypeIndex2, hwndFileDialog) -> {
                fc.updateFileFilter(fileTypeFilters, fileTypeIndex2);
                return this.invokeApproveCallback(fc, files, new LinuxApproveContext(hwndFileDialog));
            } : null;
            int[] retFileTypeIndex = new int[]{-1};
            String[] result = FlatNativeLinuxLibrary.showFileChooser(owner, dark, open, fc.getDialogTitle(), approveButtonText, currentName, currentFolder, optionsSet, optionsClear, callback, fileTypeIndex, fileTypes.toArray(new String[fileTypes.size()]), retFileTypeIndex);
            if (result != null) {
                fc.updateFileFilter(fileTypeFilters, retFileTypeIndex[0]);
            }
            return result;
        }

        private String caseInsensitiveGlobPattern(String ext) {
            StringBuilder buf = new StringBuilder();
            int len = ext.length();
            for (int i = 0; i < len; ++i) {
                char ch = ext.charAt(i);
                if (Character.isLetter(ch)) {
                    buf.append('[').append(Character.toLowerCase(ch)).append(Character.toUpperCase(ch)).append(']');
                    continue;
                }
                buf.append(ch);
            }
            return buf.toString();
        }

        boolean isFXinitialized() {
            if (fxinitialized != null) {
                return fxinitialized;
            }
            try {
                Class.forName("javafx.application.Platform", false, this.getClass().getClassLoader());
            }
            catch (ClassNotFoundException ex) {
                fxinitialized = false;
                return fxinitialized;
            }
            try {
                Class<?> cls = Class.forName("javafx.application.Platform");
                Method m = cls.getMethod("runLater", Runnable.class);
                m.invoke(null, () -> {});
                fxinitialized = true;
                return fxinitialized;
            }
            catch (InvocationTargetException ex) {
                if (ex.getCause() instanceof IllegalStateException) {
                    return false;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            fxinitialized = false;
            return fxinitialized;
        }

        private static class LinuxApproveContext
        extends ApproveContext {
            private final long hwndFileDialog;

            LinuxApproveContext(long hwndFileDialog) {
                this.hwndFileDialog = hwndFileDialog;
            }

            @Override
            public int showMessageDialog(int messageType, String primaryText, String secondaryText, int defaultButton, String ... buttons) {
                return FlatNativeLinuxLibrary.showMessageDialog(this.hwndFileDialog, messageType, primaryText, secondaryText, defaultButton, buttons);
            }
        }
    }

    private static class MacFileChooserProvider
    extends SystemFileChooserProvider {
        private MacFileChooserProvider() {
        }

        @Override
        public File[] showDialog(Window owner, SystemFileChooser fc) {
            boolean usesPatternFilter = fc.getFileFilter() instanceof PatternFilter;
            if (!usesPatternFilter) {
                for (FileFilter filter : fc.getChoosableFileFilters()) {
                    if (!(filter instanceof PatternFilter)) continue;
                    usesPatternFilter = true;
                    break;
                }
            }
            if (usesPatternFilter) {
                LoggingFacade.INSTANCE.logSevere("FlatLaf: SystemFileChooser.PatternFilter is not supported on macOS. Using Swing JFileChooser.", null);
                return new SwingFileChooserProvider().showDialog(owner, fc);
            }
            return super.showDialog(owner, fc);
        }

        @Override
        String[] showSystemDialog(Window owner, SystemFileChooser fc) {
            List filters;
            int dark = FlatLaf.isLafDark() ? 1 : 0;
            boolean open = fc.getDialogType() == 0;
            String nameFieldStringValue = null;
            String directoryURL = null;
            File currentDirectory = fc.getCurrentDirectory();
            File selectedFile = fc.getSelectedFile();
            if (selectedFile != null) {
                if (selectedFile.isDirectory()) {
                    directoryURL = selectedFile.getAbsolutePath();
                } else {
                    nameFieldStringValue = selectedFile.getName();
                    directoryURL = selectedFile.getParent();
                }
            } else if (currentDirectory != null) {
                directoryURL = currentDirectory.getAbsolutePath();
            }
            int optionsBlocked = 2059;
            int optionsSet = fc.getPlatformOptions(SystemFileChooser.MAC_OPTIONS_SET, optionsBlocked);
            int optionsClear = fc.getPlatformOptions(SystemFileChooser.MAC_OPTIONS_CLEAR, optionsBlocked);
            if ((optionsClear & 0x10) == 0) {
                optionsSet |= 0x10;
            }
            if (fc.isDirectorySelectionEnabled()) {
                optionsSet |= 0x202;
                optionsClear |= 1;
                open = true;
            }
            if (fc.isMultiSelectionEnabled()) {
                optionsSet |= 8;
            }
            if (!fc.isFileHidingEnabled()) {
                optionsSet |= 0x800;
            }
            if (Boolean.TRUE.equals(fc.getPlatformProperty(SystemFileChooser.MAC_TREATS_FILE_PACKAGES_AS_DIRECTORIES))) {
                optionsSet |= 0x4000;
            }
            int fileTypeIndex = 0;
            ArrayList<String> fileTypes = new ArrayList<String>();
            ArrayList<FileFilter> fileTypeFilters = new ArrayList<FileFilter>();
            if (!fc.isDirectorySelectionEnabled() && !(filters = fc.getFiltersForDialog()).isEmpty()) {
                fileTypeIndex = filters.indexOf(fc.getFileFilter());
                for (FileFilter filter : filters) {
                    if (filter instanceof FileNameExtensionFilter) {
                        fileTypes.add(filter.getDescription());
                        for (String ext : ((FileNameExtensionFilter)filter).getExtensions()) {
                            fileTypes.add(ext);
                        }
                        fileTypes.add(null);
                        fileTypeFilters.add(filter);
                        continue;
                    }
                    if (!(filter instanceof AcceptAllFileFilter)) continue;
                    fileTypes.add(filter.getDescription());
                    fileTypes.add("*");
                    fileTypes.add(null);
                    fileTypeFilters.add(filter);
                }
            }
            FlatNativeMacLibrary.FileChooserCallback callback = fc.getApproveCallback() != null ? (files, fileTypeIndex2, hwndFileDialog) -> {
                fc.updateFileFilter(fileTypeFilters, fileTypeIndex2);
                return this.invokeApproveCallback(fc, files, new MacApproveContext(hwndFileDialog));
            } : null;
            int[] retFileTypeIndex = new int[]{-1};
            String[] result = FlatNativeMacLibrary.showFileChooser(owner, dark, open, fc.getDialogTitle(), fc.getApproveButtonText(), (String)fc.getPlatformProperty(SystemFileChooser.MAC_MESSAGE), (String)fc.getPlatformProperty(SystemFileChooser.MAC_FILTER_FIELD_LABEL), (String)fc.getPlatformProperty(SystemFileChooser.MAC_NAME_FIELD_LABEL), nameFieldStringValue, directoryURL, optionsSet, optionsClear, callback, fileTypeIndex, fileTypes.toArray(new String[fileTypes.size()]), retFileTypeIndex);
            if (result != null) {
                fc.updateFileFilter(fileTypeFilters, retFileTypeIndex[0]);
            }
            return result;
        }

        private static class MacApproveContext
        extends ApproveContext {
            private final long hwndFileDialog;

            MacApproveContext(long hwndFileDialog) {
                this.hwndFileDialog = hwndFileDialog;
            }

            @Override
            public int showMessageDialog(int messageType, String primaryText, String secondaryText, int defaultButton, String ... buttons) {
                for (int i = 0; i < buttons.length; ++i) {
                    buttons[i] = buttons[i].replace("__", "\u0001").replace("_", "").replace("\u0001", "_");
                }
                return FlatNativeMacLibrary.showMessageDialog(this.hwndFileDialog, messageType, primaryText, secondaryText, defaultButton, buttons);
            }
        }
    }

    private static class WindowsFileChooserProvider
    extends SystemFileChooserProvider {
        private WindowsFileChooserProvider() {
        }

        @Override
        String[] showSystemDialog(Window owner, SystemFileChooser fc) {
            boolean open = fc.getDialogType() == 0;
            String approveButtonText = fc.getApproveButtonText();
            int approveButtonMnemonic = fc.getApproveButtonMnemonic();
            String fileName = null;
            String folder = null;
            String saveAsItem = null;
            if (approveButtonText != null) {
                int mnemonicIndex;
                approveButtonText = approveButtonText.replace("&", "&&");
                if (approveButtonMnemonic > 0 && (mnemonicIndex = approveButtonText.toUpperCase(Locale.ENGLISH).indexOf(approveButtonMnemonic)) >= 0) {
                    approveButtonText = approveButtonText.substring(0, mnemonicIndex) + '&' + approveButtonText.substring(mnemonicIndex);
                }
            }
            File currentDirectory = fc.getCurrentDirectory();
            File selectedFile = fc.getSelectedFile();
            if (selectedFile != null) {
                if (selectedFile.exists() && !open) {
                    saveAsItem = selectedFile.getAbsolutePath();
                } else {
                    fileName = selectedFile.getName();
                    folder = selectedFile.getParent();
                }
            } else if (currentDirectory != null) {
                folder = currentDirectory.getAbsolutePath();
            }
            int optionsBlocked = 268436072;
            int optionsSet = fc.getPlatformOptions(SystemFileChooser.WINDOWS_OPTIONS_SET, optionsBlocked) | 0x40 | 8;
            int optionsClear = fc.getPlatformOptions(SystemFileChooser.WINDOWS_OPTIONS_CLEAR, optionsBlocked);
            if ((optionsClear & 2) == 0) {
                optionsSet |= 2;
            }
            if (fc.isDirectorySelectionEnabled()) {
                optionsSet |= 0x20;
            }
            if (fc.isMultiSelectionEnabled()) {
                optionsSet |= 0x200;
            }
            if (!fc.isFileHidingEnabled()) {
                optionsSet |= 0x10000000;
            }
            int fileTypeIndex = 0;
            ArrayList<String> fileTypes = new ArrayList<String>();
            ArrayList<FileFilter> fileTypeFilters = new ArrayList<FileFilter>();
            if (!fc.isDirectorySelectionEnabled()) {
                List filters = fc.getFiltersForDialog();
                if (!filters.isEmpty()) {
                    fileTypeIndex = filters.indexOf(fc.getFileFilter());
                    for (FileFilter filter : filters) {
                        if (filter instanceof FileNameExtensionFilter) {
                            fileTypes.add(filter.getDescription());
                            fileTypes.add("*." + String.join((CharSequence)";*.", ((FileNameExtensionFilter)filter).getExtensions()));
                            fileTypeFilters.add(filter);
                            continue;
                        }
                        if (filter instanceof PatternFilter) {
                            fileTypes.add(filter.getDescription());
                            fileTypes.add(String.join((CharSequence)";", ((PatternFilter)filter).getPatterns()));
                            fileTypeFilters.add(filter);
                            continue;
                        }
                        if (!(filter instanceof AcceptAllFileFilter)) continue;
                        fileTypes.add(filter.getDescription());
                        fileTypes.add("*.*");
                        fileTypeFilters.add(filter);
                    }
                }
                if (!open && fileTypes.isEmpty()) {
                    fileTypes.add(fc.getAcceptAllFileFilter().getDescription());
                    fileTypes.add("*.*");
                }
            }
            FlatNativeWindowsLibrary.FileChooserCallback callback = fc.getApproveCallback() != null ? (files, fileTypeIndex2, hwndFileDialog) -> {
                fc.updateFileFilter(fileTypeFilters, fileTypeIndex2);
                return this.invokeApproveCallback(fc, files, new WindowsApproveContext(hwndFileDialog));
            } : null;
            int[] retFileTypeIndex = new int[]{-1};
            String[] result = FlatNativeWindowsLibrary.showFileChooser(owner, open, fc.getDialogTitle(), approveButtonText, (String)fc.getPlatformProperty(SystemFileChooser.WINDOWS_FILE_NAME_LABEL), fileName, folder, saveAsItem, (String)fc.getPlatformProperty(SystemFileChooser.WINDOWS_DEFAULT_FOLDER), (String)fc.getPlatformProperty(SystemFileChooser.WINDOWS_DEFAULT_EXTENSION), optionsSet, optionsClear, callback, fileTypeIndex, fileTypes.toArray(new String[fileTypes.size()]), retFileTypeIndex);
            if (result != null) {
                fc.updateFileFilter(fileTypeFilters, retFileTypeIndex[0]);
            }
            return result;
        }

        private static class WindowsApproveContext
        extends ApproveContext {
            private final long hwndFileDialog;

            WindowsApproveContext(long hwndFileDialog) {
                this.hwndFileDialog = hwndFileDialog;
            }

            @Override
            public int showMessageDialog(int messageType, String primaryText, String secondaryText, int defaultButton, String ... buttons) {
                if (secondaryText != null) {
                    primaryText = primaryText + "\n\n" + secondaryText;
                }
                for (int i = 0; i < buttons.length; ++i) {
                    buttons[i] = buttons[i].replace("&", "&&").replace("__", "\u0001").replace('_', '&').replace('\u0001', '_');
                }
                if (buttons.length == 0) {
                    buttons = new String[]{UIManager.getString((Object)"OptionPane.okButtonText", Locale.getDefault())};
                }
                return FlatNativeWindowsLibrary.showMessageDialog(this.hwndFileDialog, messageType, null, primaryText, defaultButton, buttons);
            }
        }
    }

    private static abstract class SystemFileChooserProvider
    implements FileChooserProvider {
        private SystemFileChooserProvider() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public File[] showDialog(Window owner, SystemFileChooser fc) {
            String[] filenames;
            Window dummyWindow = null;
            if (!SystemFileChooserProvider.hasDisplayableWindow(owner)) {
                dummyWindow = new Window((Frame)null);
                dummyWindow.addNotify();
            }
            AtomicReference filenamesRef = new AtomicReference();
            InputBlockingEventQueue queue = InputBlockingEventQueue.install();
            try {
                SecondaryLoop secondaryLoop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
                new Thread(() -> {
                    try {
                        filenamesRef.set(this.showSystemDialog(owner, fc));
                    }
                    finally {
                        secondaryLoop.exit();
                    }
                }, "FlatLaf SystemFileChooser").start();
                secondaryLoop.enter();
            }
            finally {
                if (queue != null) {
                    queue.uninstall();
                }
            }
            if (dummyWindow != null) {
                dummyWindow.dispose();
            }
            if ((filenames = (String[])filenamesRef.get()) == null) {
                return new SwingFileChooserProvider().showDialog(owner, fc);
            }
            if (filenames.length == 0) {
                return null;
            }
            return SystemFileChooserProvider.filenames2files(filenames);
        }

        abstract String[] showSystemDialog(Window var1, SystemFileChooser var2);

        boolean invokeApproveCallback(SystemFileChooser fc, String[] files, ApproveContext context) {
            if (files == null || files.length == 0) {
                return false;
            }
            ApproveCallback approveCallback = fc.getApproveCallback();
            int result = approveCallback.approve(SystemFileChooserProvider.filenames2files(files), context);
            if (result == 1) {
                return false;
            }
            fc.approveResult = result;
            return true;
        }

        private static File[] filenames2files(String[] filenames) {
            FileSystemView fsv = FileSystemView.getFileSystemView();
            File[] files = new File[filenames.length];
            for (int i = 0; i < filenames.length; ++i) {
                files[i] = fsv.createFileObject(filenames[i]);
            }
            return files;
        }

        private static boolean hasDisplayableWindow(Window owner) {
            if (owner != null && owner.isDisplayable()) {
                return true;
            }
            for (Window window : Window.getWindows()) {
                if (!window.isDisplayable()) continue;
                return true;
            }
            return false;
        }

        private static class InputBlockingEventQueue
        extends EventQueue {
            private static boolean problemLogged;

            private InputBlockingEventQueue() {
            }

            static InputBlockingEventQueue install() {
                InputBlockingEventQueue queue = new InputBlockingEventQueue();
                try {
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    toolkit.getSystemEventQueue().push(queue);
                    if (toolkit.getSystemEventQueue() != queue) {
                        InputBlockingEventQueue.logEventQueuePushProblem(null);
                        return null;
                    }
                    return queue;
                }
                catch (RuntimeException ex) {
                    InputBlockingEventQueue.logEventQueuePushProblem(ex);
                    return null;
                }
            }

            private static void logEventQueuePushProblem(Throwable t) {
                if (problemLogged) {
                    return;
                }
                problemLogged = true;
                LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to push input-blocking event queue.", t);
            }

            void uninstall() {
                super.pop();
            }

            @Override
            protected void dispatchEvent(AWTEvent event) {
                int eventID = event.getID();
                if (eventID >= 500 && eventID <= 507 || eventID >= 400 && eventID <= 402 || eventID >= 1001 && eventID <= 1001) {
                    return;
                }
                super.dispatchEvent(event);
            }
        }
    }

    private static interface FileChooserProvider {
        public File[] showDialog(Window var1, SystemFileChooser var2);
    }
}

