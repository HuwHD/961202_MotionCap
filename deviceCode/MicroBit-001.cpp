/*
The MIT License (MIT)

Copyright (c) 2016 British Broadcasting Corporation.
This software is provided by Lancaster University by arrangement with the BBC.

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.
*/

#include "MicroBit.h"

#define LIST_SIZE 2

MicroBit uBit;

MicroBitI2C i2c(I2C_SDA0, I2C_SCL0);

MicroBitAccelerometer accelerometer(i2c);

MicroBitStorage storage;

MicroBitCompass compass(i2c, accelerometer, storage);

MicroBitSerial serial(USBTX, USBRX);
MicroBitButton buttonA(MICROBIT_PIN_BUTTON_A, MICROBIT_ID_BUTTON_A);
MicroBitButton buttonB(MICROBIT_PIN_BUTTON_B, MICROBIT_ID_BUTTON_B);

int xlist [LIST_SIZE];
int ylist [LIST_SIZE];
int nslist [LIST_SIZE];
int welist [LIST_SIZE];
int hlist [LIST_SIZE];
int x = 0;
int y = 0;
int ns = 0;
int we = 0;
int h = 0;
int a = 0;
int b = 0;

int xO = 0;
int yO = 0;
int nsO = 0;
int weO = 0;
int hO = 0;


int pos = 0;

int main()
{
    // Initialise the micro:bit runtime.
    uBit.init();

    /*
    Clear the data buffers
    */
    for (int i =0; i<LIST_SIZE; i++) {
        hlist[i] = 0;
        xlist[i] = 0;
        ylist[i] = 0;
        nslist[i] = 0;
        welist[i] = 0;
    }

    if (buttonA.isPressed()) {
        uBit.compass.clearCalibration();
    }

    //
    // Loop forever
    //
    while(1) {
        /*
        Read the buttons
        */
        a = buttonA.isPressed() ? 1 : 0;
        b = buttonB.isPressed() ? 1 : 0;
        if (buttonA.isPressed()&&buttonB.isPressed()) {
            uBit.compass.calibrate();
        }
        /*
         Add each value in to a ring buffer
         */
        xlist[pos] = uBit.accelerometer.getX();
        ylist[pos] = uBit.accelerometer.getY();
        nslist[pos] = uBit.compass.getX();
        welist[pos] = uBit.compass.getY();
        hlist[pos] = uBit.compass.heading();
        /*
        Increment the ring pointer. When too big set to the begining
        */
        pos++;
        if ( pos >= LIST_SIZE ) {
            pos= 0;
        }
        /*
        Calculate the average values
        */
        x = 0;
        y = 0;
        ns = 0;
        we = 0;
        h = 0;
        for (int i =0; i<LIST_SIZE; i++) {
            x = x + xlist[i];
            y = y + ylist[i];
            ns = ns + nslist[i];
            we = we + welist[i];
            h = h + hlist[i];
        }
        xO = x / LIST_SIZE;
        yO = y / LIST_SIZE;
        nsO = ns / LIST_SIZE;
        weO = we / LIST_SIZE;
        hO = h / LIST_SIZE;
        /*
        Send the data
        */
        serial.printf("%d,%d,%d,%d,%d,%d,%d:", xO,yO,nsO,weO,hO,a,b);
        /*
        Sleep for a bit
        */
        uBit.sleep(300);
    }
}


