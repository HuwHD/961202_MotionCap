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
package serial;

public interface SerialPortListener {
    /**
     * A reading of sensor data has been parsed and a Reading object created. 
     * @param r The Reading containing the sensor data. 
     */
    void reading(Reading r);
    /**
     * A reading of sensor data has been parsed and failed. 
     * 
     * @param e The exception contains the details
     */
    void fail(Exception e);
    /**
     * The sensor has connected successfully.
     * 
     * The GUI component requires this data to update the status on screen.
     * 
     * @param devicePort The connected port
     * @param baud The connected baud rate
     * @param name The name of the sensor. Defined in the configuration data.
     */
    void connectedSensor(String devicePort, int baud, String name);
    /**
     * The sensor has disconnected successfully
     * 
     * The GUI component requires this data to update the status on screen.
     * 
     * @param devicePort The disconnected port
     * @param name The name of the sensor. Defined in the configuration data.
     */
    void disConnectedSensor(String devicePort, String name);
    /**
     * Used for debugging to write the raw data from the sensor to the logs.
     * @param s The String received from the Sensor
     * @return true = consumed (do not scan the data and create a Reading) 
     */
    boolean rawData(String s);
}
