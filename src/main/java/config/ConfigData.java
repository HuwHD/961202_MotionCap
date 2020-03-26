/*
 * Copyright (C) 2020 Huw Hudson-Davies
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manage a property file as configuration data.
 * <pre>
 * Names prefixed with <b>'os.[osname].'</b> take precedence.
 * For example: <b>os.linux.app.name</b> wins over <b>app.name</b>
 *              <b>os.windows.app.name</b> wins over <b>app.name</b>
 * </pre>
 *
 * @author huw
 */
public class ConfigData {

    /*
    Define a set of standard properties for this application
    
    These are public so any class can get the property names.
     */

    public static final String DEBUG_SENSOR_DATA = "debug.sensordata";
    public static final String CALIB_HEADING_DATA = "calibrate.heading.data";
    public static final String CALIB_VERTICAL_DATA = "calibrate.vertical.data";
    public static final String SETTINGS_FILE_KEY = "settings.file";
    public static final String DEFAULT_PORT_KEY = "default.port";
    public static final String DEFAULT_BAUD = "default.baud";
    public static final String CLICK_DELAY_KEY = "delay.ms.click";
    public static final String TYPE_DELAY_KEY = "delay.ms.type";
    public static final String MOVE_DELAY_KEY = "delay.ms.move";
    public static final String CONNECT_ON_LOAD = "connectOnLoad";
    public static final String SENSOR_NAME = "sensorName";
    public static final String LAUNCH_GUI = "launchGUI";
    public static final String SENSOR_TO_CONSOLE = "sendSensorReadingsToConsole";

    Map<String, String> sm = new HashMap<>();

    private static final String OSNAME = getOsNameFromEnvironment();
    /*
    The file name so we can display error messages
     */
    private static String fileName;
    /*
    A properties object is a simple Name value pair construct.
    These are the main properties.
     */
    private static Properties properties;
    /*
    A properties object to contain the settings from the setting file defined is the "settings.file' property of the main file.
    If "settings.file' is undefined then no save takes place.
    These "settings.file' override the main properties.
     */
    private static Properties settings;

    /**
     * Get a MANDATORY list value from the configuration data List is formatted
     * as a '|' separated list. A|B|c etc. Any leading or trailing spaces are
     * retained.
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

    public static long[] getLongs(String name, int count) {
        String[] values = getValue(name).split("\\,");
        if (values.length != count) {
            throw new ConfigException("Config data [" + fileName + "] property [" + name + "] must have "+count+" numbers. It must be a valid list of comma separated numbers");
        }
        long[] longs = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                longs[i] = Long.parseLong(values[i].trim());
            } catch (NumberFormatException nfe) {
                throw new ConfigException("Config data [" + fileName + "] property [" + name + "] value ["+values[i]+"] is invalid. It must be a valid list of comma separated numbers");
            }
        }
        return longs;
    }

    /**
     * Get an optional integer property or a default value if not found.
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

    /**
     * Get a mandatory integer property.
     * <pre>
     * If found it will convert to an integer.
     * If the conversion fails an exception is thrown.
     * </pre>
     *
     * @param name The property name
     * @return The int value
     */
    public static int getInt(String name) {
        String s = getValue(name);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            throw new ConfigException("Config data [" + fileName + "] property [" + name + "] or [os." + OSNAME + "." + name + "] = [" + s + "] is an invalid integer");
        }
    }

    /**
     * Get a boolean value. If not defined then return defaultValue.
     *
     * @param name The name of the property
     * @param defaultValue If not defined then return this.
     * @return The property value as a boolean.
     */
    public static boolean getBoolean(String name, boolean defaultValue) {
        return getValue(name, Boolean.toString(defaultValue)).toLowerCase().startsWith("true");
    }

    /**
     * Get a boolean value. If not defined then getValue(name) will throw an
     * exception.
     *
     * @param name The name of the property
     * @return The property value as a boolean. Throws exception if undefined.
     */
    public static boolean getBoolean(String name) {
        return getValue(name).toLowerCase().startsWith("true");
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
     * Short cut method to set the default port.
     *
     * @param port the selected port
     */
    public static void setDefaultPort(String port) {
        set(DEFAULT_PORT_KEY, port);
    }

    /**
     * Short cut method to get the click delay value for the mouse
     *
     * @return the integer value in Milli Seconds
     */
    public static int getClickDelay() {
        return getInt(CLICK_DELAY_KEY, 10);
    }

    /**
     * Short cut method to get the type delay value for the mouse
     *
     * @return the integer value in Milli Seconds
     */
    public static int getTypeDelay() {
        return getInt(TYPE_DELAY_KEY, 10);
    }

    /**
     * Short cut method to get the move delay value for the mouse
     *
     * @return the integer value in Milli Seconds
     */
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
