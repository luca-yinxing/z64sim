/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SettingsException;
import it.uniroma2.pellegrini.z64sim.util.log.LogLevel;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.util.queue.Dispatcher;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;
import it.uniroma2.pellegrini.z64sim.util.sys.OS;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

public class SettingsController extends Controller {
    private static final Logger log = LoggerFactory.getLogger();
    private static SettingsController instance = null;

    // To validate configuration file and to match JComboBoxes in SettingsWindow
    // WARNING: The model in the JComboBox *must* match the order in these arrays!
    private static final String[] locales = {"en", "es", "fr", "it"};
    private static final String[] themes = {"light", "dark"};
    private static final String[] logLevels = {"trace", "debug", "info", "warn", "error"};

    // Actual members of the singleton
    private Settings settings = null;
    private Locale locale = Locale.getDefault();

    private SettingsController() {}

    public static void init() {
        instance = new SettingsController();

        try {
            instance.settings = Settings.loadConfiguration();
        } catch (SettingsException e) {
            log.warn(e.getMessage());
            // Try to create the default configuration
            instance.settings = Settings.getDefaultConfiguration();
        }

        validateConfig();

        // Immediately rewrite the configuration to file. This allows to "upgrade" the configuration
        // file, for example when moving from a previous version to a newer version, storing the defaults.
        try {
            instance.settings.persist();
        } catch (SettingsException e) {
            log.warn(e.getMessage());
        }

        // Update locale
        instance.locale = new Locale(instance.settings.getUiLang());
        Locale.setDefault(instance.locale);
    }

    private static void validateConfig() {
        if(!Arrays.asList(locales).contains(getInstance().settings.getUiLang())) {
            log.warn(PropertyBroker.getMessageFromBundle("unexpected.configuration.value.for.uilang.0.using.default"), getInstance().settings.getUiLang());
            getInstance().settings.setUiLang(PropertyBroker.getPropertyValue("z64sim.ui.defaultLanguage"));
        }
        if(!Arrays.asList(themes).contains(getInstance().settings.getTheme())) {
            log.warn(PropertyBroker.getMessageFromBundle("unexpected.configuration.value.for.theme.0.using.default"), getInstance().settings.getTheme());
            getInstance().settings.setTheme(PropertyBroker.getPropertyValue("z64sim.ui.defaultTheme"));
        }
        if(!Arrays.asList(logLevels).contains(getInstance().settings.getLogLevel())) {
            log.warn(PropertyBroker.getMessageFromBundle("unexpected.configuration.value.for.loglevel.0.using.default"), getInstance().settings.getLogLevel());
            getInstance().settings.setLogLevel(PropertyBroker.getPropertyValue("z64sim.log.level"));
        }
    }

    @Override
    public boolean dispatch(Events command) {
        return false;
    }

    private static SettingsController getInstance() {
        if(instance == null)
            init();
        return instance;
    }

    public static void persist() {
        try {
            getInstance().settings.persist();
        } catch(SettingsException e) {
            log.error(PropertyBroker.getMessageFromBundle("unable.to.store.configuration.file.0", e.getMessage()));
        }
    }

    public static Locale getLocale() {
        return getInstance().locale;
    }

    public static LogLevel getLogLevel() {
        return LogLevel.stringToLevel(getInstance().settings.getLogLevel());
    }

    public static int getLogLevelIdx() {
        return Arrays.asList(logLevels).indexOf(getLogLevel().toString().toLowerCase());
    }

    public static void setLogLevelIdx(int idx) {
        getInstance().settings.setLogLevel(logLevels[idx]);
        // TODO: dispatch log level
    }

    public static boolean getLogShowDateTime() {
        return getInstance().settings.getLogShowDateTime();
    }

    public static void setLogShowDateTime(boolean show) {
        getInstance().settings.setLogShowDateTime(show);
    }

    public static String getLogFile() {
        return getInstance().settings.getLogOutFile();
    }

    public static void setLogFile(String path) {
        getInstance().settings.setLogOutFile(path);
        // TODO: dispatch log file
    }

    public static String getUiLang() {
        return getInstance().settings.getUiLang();
    }

    public static int getUiLangIdx() {
        return Arrays.asList(locales).indexOf(getUiLang());
    }

    public static void setUiLangIdx(int idx) {
        getInstance().settings.setUiLang(locales[idx]);
    }

    public static String getTheme() {
        return getInstance().settings.getTheme();
    }

    public static int getThemeIdx() {
        return Arrays.asList(themes).indexOf(getTheme());
    }

    public static void setThemeIdx(int idx) {
        getInstance().settings.setTheme(themes[idx]);
        if(idx == 0)
            Dispatcher.dispatch(Events.SET_THEME_LIGHT);
        else
            Dispatcher.dispatch(Events.SET_THEME_DARK);
    }

    public static Dimension getWindowSize() {
        return new Dimension(getInstance().settings.getWindowSizeX(), getInstance().settings.getWindowSizeY());
    }

    public static void setWindowSize(Dimension dimension) {
        getInstance().settings.setWindowSizeX(dimension.width);
        getInstance().settings.setWindowSizeY(dimension.height);
    }
    public static void setFontSizeCompiler(int value) {
        getInstance().settings.setFontSizeCompiler(value);
    }

    public static void setFontSizeEditor(int value) {
        getInstance().settings.setFontSizeEditor(value);
    }
    public static int getFontSizeEditor() {
        return getInstance().settings.getFontSizeEditor();
    }

    public static int getFontSizeCompiler() {
        return getInstance().settings.getFontSizeCompiler();
    }

    public static String getFileLastDir() {
        return getInstance().settings.getFileLastDir();
    }

    public static void setFileLastDir(String value) {
        getInstance().settings.setFileLastDir(value);
    }

    private static class Settings implements Serializable {

        private static final long serialVersionUID = 1L;

        static final String configurationDirectoryPath = OS.getConfigDir();
        static final String configurationFilePath = OS.getConfigFilePath();

        // Configuration options
        private String uiLang;
        private String theme;
        private String logLevel;
        private boolean logShowDateTime;
        private String logOutFile;
        private int windowSizeX;
        private int windowSizeY;
        private String fileLastDir;
        private int fontSizeEditor;
        private  int fontSizeCompiler;

        private Settings() {
            // Configuration defaults
            this.uiLang = PropertyBroker.getPropertyValue("z64sim.ui.defaultLanguage");
            this.theme = PropertyBroker.getPropertyValue("z64sim.ui.defaultTheme");
            this.logLevel = PropertyBroker.getPropertyValue("z64sim.log.level");
            this.logShowDateTime = Boolean.parseBoolean(PropertyBroker.getPropertyValue("z64sim.log.showDateTime"));
            this.logOutFile = null;
            this.windowSizeX = Integer.parseInt(PropertyBroker.getPropertyValue("z64sim.ui.minSizeX"));
            this.windowSizeY = Integer.parseInt(PropertyBroker.getPropertyValue("z64sim.ui.minSizeY"));
            this.fileLastDir = null;
            this.fontSizeEditor = Integer.parseInt(PropertyBroker.getPropertyValue("z64sim.ui.fontSizeEditor"));
            this.fontSizeCompiler = Integer.parseInt(PropertyBroker.getPropertyValue("z64sim.ui.fontSizeCompiler"));
        }

        protected static Settings loadConfiguration() throws SettingsException {
            Settings settings;
            if (configurationDirectoryPath.isEmpty()) {
                throw new SettingsException("Unable to find a configuration directory for: " + OS.getNameOS());
            }

            try {
                Files.createDirectories(Paths.get(configurationDirectoryPath));
            } catch (IOException e) {
                throw new SettingsException("Unable to create configuration file path: " + configurationDirectoryPath);
            }

            try {
                FileInputStream fileInputStream = new FileInputStream(configurationFilePath);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                settings = (Settings) objectInputStream.readObject();
                objectInputStream.close();
            } catch(IOException | ClassNotFoundException e) {
                throw new SettingsException("Unable to load configuration file " + configurationFilePath + ": " + e.getMessage());
            }

            return settings;
        }


        protected static Settings getDefaultConfiguration() {
            return new Settings();
        }

        protected void persist() throws SettingsException {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(configurationFilePath);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(this);
                objectOutputStream.flush();
                objectOutputStream.close();
            } catch(IOException e) {
                throw new SettingsException("Unable to store configuration in: " + configurationFilePath + ": " + e.getMessage());
            }
        }

        public String getUiLang() {
            return uiLang;
        }

        public void setUiLang(String uiLang) {
            this.uiLang = uiLang;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public boolean getLogShowDateTime() {
            return logShowDateTime;
        }

        public void setLogShowDateTime(boolean logShowDateTime) {
            this.logShowDateTime = logShowDateTime;
        }

        public boolean isLogShowDateTime() {
            return logShowDateTime;
        }

        public String getLogOutFile() {
            return logOutFile;
        }

        public void setLogOutFile(String logOutFile) {
            this.logOutFile = logOutFile;
        }

        public int getWindowSizeX() {
            return windowSizeX;
        }

        public void setWindowSizeX(int windowSizeX) {
            this.windowSizeX = windowSizeX;
        }

        public int getWindowSizeY() {
            return windowSizeY;
        }

        public void setWindowSizeY(int windowSizeY) {
            this.windowSizeY = windowSizeY;
        }

        public String getFileLastDir() {
            return fileLastDir;
        }

        public void setFileLastDir(String fileLastDir) {
            this.fileLastDir = fileLastDir;
        }
        
        public int getFontSizeEditor() {
            return fontSizeEditor;
        }

        public void setFontSizeEditor(int fontSizeEditor) {
            this.fontSizeEditor = fontSizeEditor;
        }

        public int getFontSizeCompiler() {
            return fontSizeCompiler;
        }

        public void setFontSizeCompiler(int fontSizeCompiler) {
            this.fontSizeCompiler = fontSizeCompiler;
        }
    }
}
