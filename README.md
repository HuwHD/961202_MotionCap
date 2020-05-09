# 961202_MotionCap
## Motion tracking Visor for Alternate Mouse Input

This application and hardware are designed for people who have restricted movment and are unable to use a normal mouse as a pointing device.

It allows the user to move the mouse using natural head movements.

The low cost motion tracking sensors are attached to a hat or helmet and once connected the mouse can be moved by moving the head leftto right and up and down.

# Hardware
The application uses two BBC MicroBit devices. One to track the movement and transmit the data, the other to receive the data and send it to the computer.

# Software
The application is written using standard Java APIs and can run on Windows, Linux and MAC.

# Build:
To build from source you will require Java Development Kit (JDK) 11 with JavaFX libraries.

1. Clone the project into a local directory.
2. cd into that directory.
3. On Linux and MAC:
   1. chmod +x gradlew
   2. ./gradlew clean build
4. On Windows:
   1. gradlew.bat clean build.

This will generate an executable file in:
```bash
build/libs/961202_MotionCap.jar
```
# Run
1. To run the application you will require Java 11 runtime installed on your computer.
2. Connect the hardware.
1. On Linux and MAC:
   1. java -jar build/libs/961202_MotionCap.jar config.properties
2. On Windows:
   1. java -jar build\libs\961202_MotionCap.jar config.properties

# Using the Graphical User Interface
The GUI is designed to display the sensor data to allow the user to understand how moving the sensor changes that data. 

The GUI has three areas:
1.  **Top:** The controls for connecting to the sensor, calibrating the sensor and changing sensor options.
2.  **Middle:** The Graphical display displaying live data from the sensor.
3.  **Bottom:** The status area displaying raw connection data and connection status.

![Screen Shot](https://github.com/HuwHD/961202_MotionCap/blob/master/ScreenShots.png)

## Connection controls
This displays a Drop dow list of currently available ports that the sensor is connected to.

The easy way to fine the port is to start tha application without the sensor receiver plugged in and not the list of ports.

Quit the applicaton and plug the sensor in. 

Restart the application and select the port that has been added to the list.

You can not press the Connect button.

The sensor you have selected will now be saved fornext time you run the application.

## Calibration controls
Therre are two calibration controls, one for Heading (Left to Right) and one for vertical movement (Up and Down).

The graphical display will be displaying a track of the last 50 vertical readings from the sensor and 5 lines that show the boundaries. 

Pressing the Vertical calibrate button will position the boundaries around the latest vertical sensor reading.

The graphical display will be displaying a compass (like a clock face) with hands that show the current Heading and the heading boundaries. 

Pressing the Heading calibrate button will position the boundaries around the latest heading sensor reading.

The calibrations are saved for the next time the application runs.

## Options 
**Start:** This will connect the mouce cursor to the sensor and the mouse will start moving. The text on the button now changes to **Stop**

**Stop:** This will stop the mouse moving.

The mouse can also be stopped by moving the real mouse.

**Swap L/R Buttons:** This will swap the buttons on the sensor (currently labled A and B). 

This is required is the mechanism for pressing the buttons changes. The effect is to swap the A and B buttons and thus change the Left and Right click options. 

The option is saved for the next time the application runs.

**Swap UP/DOWN** This changes the vertical motion. When the sensor sends data that is interpreted as 'looking up' the mouse should muve UP. 

If this is not happening then select this option.

The option is saved for the next time the application runs.

## The Graphical display:
This displays graphically the status of the sensor and the button options.

**See the Screen Shot above for details**

## The Status area:
This displays the raw data coming from the sensor and the connection status.

# Configuration
The application uses a simple configuration properties file. The name of this file is passed to the application when run.

The current file is called config.properties. This is an annotated copy of that file to describe what each value does.

```properties
##
# If you are running Linux then properties prefixed with 
#   os.linux. are read first.
# If you are running Windows then properties prefixed with 
#   os.windows. are read first.
# If you are running MAC OS then properties prefixed with 
#   os.mac. are read first.
# If the property is still not found then the property without a prefix is read.

###
# The default sensor port. This is selected in the port list if it is available.
# The default port is operating system (OS) specific so would normally be prefixed
#    with the OS name.
#
os.linux.default.port=ttyACM0
os.windows.default.port=COM5

##
# The name of the sensor. This is displayed in the User Interface (UI)
#    It does not effect the operation of the application.
#
sensorName=MicroBit Sensor

##
# The spped that the sensor communicates with teh PC. 
#     This is currently fixed and should NOT be changed.
#
default.baud=115200

##
# Connect to the sensor automaticallky when the apllication loads.
#
connectOnLoad=true

##
# Start the GUI when the application loads.
# If false the default port will be connected automatically.
#
launchGUI=true

##
# Save calibration data and options in this file.
# If this is not defined then the data will not be saved.
# The contents of this file take presedence over the main configuration file.
#
settings.file=settings.properties

##
# Define the heading boundaries.
#  The first parameter is the center of the boundaries.
#  The second parameter is half the width of the null zone.
#  The third parameter is half the width of the active zone.
#
calibrate.heading.data=0,8,32

##
# Define the speed of the mouse for changes in heading.
#
calibrate.heading.speed=11.0

##
# Define the vertical boundaries.
#  The first parameter is the center of the boundaries.
#  The second parameter is half the width of the null zone.
#  The third parameter is half the width of the active zone.
#
calibrate.vertical.data=0,80,330

##
# Define the speed of the mouse for vertical movement.
#
calibrate.vertical.speed=2.0

##
# If true the sensor buttons are swapped left for right
#
calibrate.swapleftright=false

##
# If true the vertical movement is inverted Up for Down.
#
calibrate.swapUpDown=false

##
# This will output the raw sensor data to the console.
#
debug.sensordata=false
```
