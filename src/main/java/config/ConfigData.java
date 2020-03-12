/*
 * Copyright (C) 2019 Huw Hudson-Davies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manage a property file as configuration data.
 * <pre>
 * Names prefixed with <b>'os.[osname].'</b> take precedence.
 * For example: <b>os.linux.app.name</b> wins over <b>app.name</b>
 *              <b>os.windows.app.name</b> wins over <b>app.name</b>
 * </pre>
 *
 * @author dev
 */
public class ConfigData {

    private static final String SETTINGS_FILE_KEY = "settings.file";
    private static final String DEFAULT_PORT_KEY = "default.port";
    private static final String DEFAULT_BAUD = "default.baud";
    private static final String CLICK_DELAY_KEY = "delay.ms.click";
    private static final String TYPE_DELAY_KEY = "delay.ms.type";
    private static final String MOVE_DELAY_KEY = "delay.ms.move";

    private static final String OSNAME = getOsNameFromEnvironment();
    /*
    The file name so we can display error messages
     */
    private static String fileName;
    /*
    A properties object is a simple Name value pair construct.
     */
    private static Properties properties;
    /*
    A properties object to contain the settings. These override the properties.
     */
    private static Properties settings;

    /**
     * Get a MANDATORY list value from the configuration data
     * <pre>
     * Names prefixed with <b>'os.[osname].'</b> take precedence.
     * For example: <b>os.linux.app.name</b> wins over <b>app.name</b>
     *              <b>os.windows.app.name</b> wins over <b>app.name</b>
     * </pre>
     *
     * @param name The name of the list value
     * @return The values in a String[] array
     */
    public static String[] getListValue(String name) {
        String[] array = getValue(name).split("\\|");
        if (array.length == 0) {
            throw new ConfigException("Config data [" + fileName + "] property [" + name + "] or [os." + OSNAME + "." + name + "] is an empty list");
        }
        return array;
    }

    /**
     * Get a MANDATORY value from the configuration data
     * <pre>
     * Names prefixed with <b>'os.[osname].'</b> take precedence.
     * For example: <b>os.linux.app.name</b> wins over <b>app.name</b>
     *              <b>os.windows.app.name</b> wins over <b>app.name</b>
     * </pre>
     *
     * @param name The name of the value
     * @return The value
     */
    public static String getValue(String name) {
        String s = getValue(name, null);
        if (s == null) {
            throw new ConfigException("Config data [" + fileName + "] property [" + name + "] or [os." + OSNAME + "." + name + "] is undefined");
        }
        return s;
    }

    /**
     * Add a value to the settings and update the settings file.
     *
     * The settings file name must be defined in the configuration data using
     * the SETTINGS_FILE_KEY name.
     *
     * If the value is null the setting is cleared.
     *
     * @param name The name of the setting
     * @param value The value of the setting
     */
    public static void set(String name, String value) {
        /*
        Clear or set the value.
        
        Values are always OS specific so different values dont overlap.
         */
        String settingName = "os." + OSNAME + "." + name;
        if (value == null) {
            settings.remove(settingName);
        } else {
            settings.setProperty(settingName, value);
        }
        /*
        Get the settings file name. If not defined then do nothing.
         */
        String settingsFileName = getValue(SETTINGS_FILE_KEY, null);
        if (settingsFileName == null) {
            return;
        }
        /*
        Write the settings to the file.
        
        Note it will create the file if it does not exist.
         */
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(settingsFileName);
            settings.store(fos, name);
        } catch (IOException ex) {
            throw new ConfigException(ex.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get an OPTIONAL value from the configuration data
     * <pre>
     * Names prefixed with <b>'os.[osname].'</b> take precedence.
     * For example: <b>os.linux.app.name</b> wins over <b>app.name</b>
     *              <b>os.windows.app.name</b> wins over <b>app.name</b>
     *
     * Settings are returned first so they override the configuration data.
     * </pre>
     *
     * @param name The name of the value
     * @param defaultValue The value returned if NOT FOUND!
     * @return The value or defaultValue value if NOT FOUND!
     */
    public static String getValue(String name, String defaultValue) {
        /*
        Get the OS specific settings value.
         */
        String settingName = "os." + OSNAME + "." + name;
        String s = settings.getProperty(settingName, null);
        if (s == null) {
            /*
            Get the config data OS specific value
             */
            s = properties.getProperty(settingName, null);
            if (s == null) {
                /*
                Finally get the non OS value.
                 */
                s = properties.getProperty(name, null);
                if (s == null) {
                    /*
                    If all else fails return null.
                     */
                    return defaultValue;
                }
            }
        }
        /*
        Return the non null value.
         */
        return s;
    }

    /**
     * Get an integer property of a default value if not found.
     * <pre>
     * If found it will convert to an integer.
     * If the conversion fails an exception is thrown.
     * </pre>
     *
     * @param name The property name
     * @param defaultValue The value returned if not found
     * @return The int value
     */
    public static int getInt(String name, int defaultValue) {
        String s = getValue(name, "" + defaultValue);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            throw new ConfigException("Config data [" + fileName + "] property [" + name + "] or [os." + OSNAME + "." + name + "] = [" + s + "] is an invalid integer");
        }
    }

    public static int getInt(String name) {
        String s = getValue(name);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            throw new ConfigException("Config data [" + fileName + "] property [" + name + "] or [os." + OSNAME + "." + name + "] = [" + s + "] is an invalid integer");
        }
    }

    /**
     * Load the configuration data.This should only be done ONCE!
     *
     * Load Configuration data first.
     *
     * If the settings file is defined then load that. It is not required to
     * exist as a file.
     *
     * @param configFileName The file name of the configuration data
     * (properties) file
     */
    public static void load(String configFileName) {
        fileName = configFileName;
        File f = new File(fileName);
        if (!f.exists()) {
            throw new ConfigException("File [" + f.getAbsolutePath() + "] does not exist");
        }
        properties = new Properties();
        try {
            properties.load(new FileInputStream(f));
        } catch (IOException ex) {
            throw new ConfigException("Failed to  load file [" + f.getAbsolutePath() + "]", ex);
        }
        /*
        Create an empty settings file
         */
        settings = new Properties();
        /*
        Exit if the file is not defined in the configuration data
         */
        String settingsFile = getValue(SETTINGS_FILE_KEY, null);
        if (settingsFile == null) {
            return;
        }
        File sf = new File(settingsFile);
        if (!sf.exists()) {
            /*
            Settings file does not exist so return. settings will be empty!
             */
            return;
        }
        /*
        Load the settings.
         */
        try {
            settings.load(new FileInputStream(sf));
        } catch (IOException ex) {
            throw new ConfigException("Failed to  load file [" + sf.getAbsolutePath() + "]", ex);
        }

    }

    /**
     * Return the operating systems name
     *
     * @return linux,windows,solaris or mac
     */
    public static String getOsName() {
        return OSNAME;
    }

    /**
     * Easy method to get the default port
     *
     * @return the port
     */
    public static String getDefaultPort() {
        return getValue(DEFAULT_PORT_KEY);
    }

    public static int getDefaultBaud() {
        return getInt(DEFAULT_BAUD);
    }

    /**
     * Easy method to set the default port. Called when the port is selected.
     *
     * @param port the selected port
     */
    public static void setDefaultPort(String port) {
        set(DEFAULT_PORT_KEY, port);
    }
    
    public static int getClickDelay() {
        return getInt(CLICK_DELAY_KEY, 10);
    }
    
    public static int getTypeDelay() {
        return getInt(TYPE_DELAY_KEY, 10);
    }

    public static int getMoveDelay() {
        return getInt(MOVE_DELAY_KEY, 1);
    }

    /**
     * Work out the operating systems name
     *
     * @return linux,windows,solaris or mac
     */
    private static String getOsNameFromEnvironment() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        }
        if (os.contains("mac")) {
            return "mac";
        }
        if ((os.contains("nix") || os.contains("nux") || os.contains("aix"))) {
            return "linux";
        }
        if (os.contains("sunos")) {
            return "solaris";
        }
        return "windows";
    }

}
