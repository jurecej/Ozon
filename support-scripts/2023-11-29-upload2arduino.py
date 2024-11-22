# -*- coding: utf-8 -*-
"""
Created on Wed Mar 29 23:41:40 2023

@author: jurecej
"""

import os
import datetime
import time


# Specify the path to the Arduino sketch file
sketch_file_path = "path/BTE-AT-command-change-name-BTOZON.ino"

# Use arduino-cli to compile and upload the modified sketch to the Arduino board
os.system(f"arduino-cli compile --fqbn arduino:avr:nano {sketch_file_path}")
os.system(f"arduino-cli upload -p COM3 --fqbn arduino:avr:nano {sketch_file_path} -P arduinoasisp")

time.sleep(10)

# Specify the path to the Arduino sketch file
sketch_file_path = "path/01-BLE-TX-RX-maincode-17-low-power.ino"

# Specify the new date (in yyyy-mm-dd format)
new_date = int(datetime.datetime.now().timestamp())

# Read the contents of the sketch file
with open(sketch_file_path, "r") as f:
    sketch_code = f.read()

# Replace the existing date with the new date
sketch_code = sketch_code.replace("unsigned long t_2 = 0;", f"unsigned long t_2 = {new_date};")


# Write the modified code to a temporary file
tmp_file_path = "path/2023-02-17-UV-index-BLE-D2-D3-pins-T_update-time-tmp.ino"
with open(tmp_file_path, "w") as f:
    f.write(sketch_code)

# Use arduino-cli to compile and upload the modified sketch to the Arduino board
os.system(f"arduino-cli compile --fqbn arduino:avr:nano {tmp_file_path}")
os.system(f"arduino-cli upload -p COM3 --fqbn arduino:avr:nano {tmp_file_path} -P arduinoasisp")
