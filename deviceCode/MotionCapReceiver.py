from microbit import *

import radio

uart.init(115200)

radio.on()

missed = 0
skipped = 0
seq = 0
last = 0
display.show(Image.ASLEEP)
incoming = radio.receive()
if not incoming == None:
    part = str(incoming).split(',', 1)
    last = int(part[0])

while True:
    incoming = radio.receive()
    if not incoming == None:
        missed=0
        part = str(incoming).split(',', 1)
        seq = int(part[0])
        if seq == 0:
            last =0
        if (seq - 1) == last:
            skipped-=1
            if skipped == 0:
                display.show(Image.HAPPY)
                
            for letter in part[1]:
                if(letter != ':'):    
                    uart.write(letter)

            if button_a.is_pressed():
                uart.write(',1')
            else:
                uart.write(',0')
        
            if button_b.is_pressed():
                uart.write(',1:')
            else:
                uart.write(',0:')
                
        else:
            skipped = 10
            display.show(Image.CONFUSED)
        last = seq
    else:
        missed+=1
        if missed > 10:
            display.show(Image.SAD)
    sleep(10)