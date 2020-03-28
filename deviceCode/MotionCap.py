# Add your Python code here. E.g.
from microbit import *

import array as arr

dot1 = Image("00000:"
             "00400:"
             "04440:"
             "00400:"
             "00000")
             
dot2 = Image("00000:"
             "00600:"
             "06660:"
             "00600:"
             "00000")
             
uart.init(115200)

hd = arr.array('i', [0, 0, 0, 0])
xd = arr.array('i', [0, 0, 0, 0])
yd = arr.array('i', [0, 0, 0, 0])
ind = 0
flashCount = 0

while True:
    
    flashCount+=1
    if flashCount>=20:
        flashCount = 0
        
    if flashCount>10:
        display.show(dot1)
    else:
        display.show(dot2)

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
    
    uart.write(str(outX) + ',' + str(outY) + ',' + str(outH) + ',' + ba + ',' + bb + ',0,0:')
    
    sleep(10)
    