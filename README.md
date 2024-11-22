# Ozon
Ozon portable solar protection sensor. Easy to use Arduino base UV index sensor used to track UV index and notifiy user about overexposure.

<img src="https://github.com/jurecej/Ozon/blob/main/pictures/2.jpeg" width="228"/>

# Project structure

  1. Android application
  2. Firmware
  3. Hardware
  4. Support scripts

# 1. Android application

Code for user friendly Android application. Application enables the daily track of UV exposure and calculation of daily dose. User can select skin type, sunscreen factor.
Extra feature was added to sensor to track max air temperature. The communication with sensor is carried out via BLE.

Current UV index and temperature with sensor battery level. Under the indicatiors is the sunscreen factor selector.
<img src="https://github.com/jurecej/Ozon/blob/main/pictures/3.png" width="228"/>

Daily UV dose and max temperature.

<img src="https://github.com/jurecej/Ozon/blob/main/pictures/4.png" width="228"/>

Username and skin type selection.

<img src="https://github.com/jurecej/Ozon/blob/main/pictures/5.png" width="228"/>

Connection page.

<img src="https://github.com/jurecej/Ozon/blob/main/pictures/6.png" width="228"/>

 # 2. Firmware

 Code uses arduino libraries and is uploaded on ATmega328.

 ## Requirements
   • Arduino Uno or similar for uploading the code to sensor (Arduino ISP)
   
   • PCB Programming clip kit

#  3. Hardware

  • Schematics and PCB gerber files
  • STL files for housing
  • for assembly additional screws, plastic circle and o-ring are needed

<img src="https://github.com/jurecej/Ozon/blob/main/pictures/2024-03-20-UV-sensor-assembly.png" width="228"/>
  
#  4. Support scripts

Python script used to upload code to the sensor easily.
