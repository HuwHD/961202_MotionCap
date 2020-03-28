# Add your Python code here. E.g.
from microbit import *

import radio
import array as arr

dot = Image("00000:"
             "00000:"
             "00600:"
             "00000:"
             "00000")
radio.on()

seq = 0
hd = arr.array('i', [0, 0, 0, 0])
xd = arr.array('i', [0, 0, 0, 0])
yd = arr.array('i', [0, 0, 0, 0])
ind = 0
flashCount = 0

while True:
    flashCount+=1
    if flashCount>=6:
        display.show(dot)
        flashCount = 0
    else:
        display.clear()
    
    
    hd[ind] = compass.heading()
    xd[ind] = accelerometer.get_x()   
    yd[ind] = accelerometer.get_y()   
    ind = ind+1
    if ind > 3:
        ind = 0
    
    sumH = 0
    sumX = 0
    sumY = 0
    
    for i in range(0,3):
        sumH = sumH + hd[i]
        sumX = sumX + xd[i]
        sumY = sumY + yd[i]
        
    outH = int(sumH / 4)
    outX = int(sumX / 4)
    outY = int(sumY / 4)
    
    if button_a.is_pressed():
        ba = '1'
    else:
        ba = '0'

    if button_b.is_pressed():
        bb = '1'
    else:
        bb = '0'
    
    radio.send(str(seq) + ',' + str(outX) + ',' + str(outY) + ',' + str(outH) + ',' + ba + ',' + bb + ':')
    seq = seq + 1
    
    sleep(10)
    