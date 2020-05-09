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
