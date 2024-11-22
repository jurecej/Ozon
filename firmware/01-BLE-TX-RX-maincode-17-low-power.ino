//2023-07-09
//Jure Cej

//Updated code to save data to SRAM (not EEPROM)
#include "LowPower.h"

int i = 1024;

unsigned long readData;
unsigned long readTime;
int resetValue;

//unsigned long t_0 = 0; //initial Unix time
//unsigned long t_1; //updated time
//unsigned long t_3 = 1685999708; 
unsigned long t;
//unsigned long t_reset = 0;

unsigned long t_0 = 0;
unsigned long t_1;
unsigned long t_2 = 1692306701; //change to upload time when uploading sketch using python upload code
unsigned long t_reset = 0;

const int NUM_BYTES_TO_SKIP = 3;

int sensorValue = 0; //UVI sensor value
int batteryValue = 0; //sensor battery value
int tempValue = 0; //temperature sensor value
int smoothedBatteryValue;


int stateValue = 0; //BLE state value (if BLE is connected value is 1)
int chrgValue = 0;
int inPin = 7; //BLE state pin
int chrgPin = 9; //BLE state pin

const int windowSize = 10; // Number of values to average
int sensorValues[windowSize]; // Array to store recent sensor values
int batteryValues[windowSize]; // Array to store recent battery values
int tempValues[windowSize]; // Array to store recent temperature values
int currentIndex = 0; // Current position in the arrays

bool executed = false;

struct ValueWithTimestamp {
  byte value;
  unsigned long timestamp;
};

ValueWithTimestamp myValue;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600); //begin BLE comunication and set baud rate 9600
  

  pinMode(inPin, INPUT);  //set digital pin mode
  pinMode(chrgPin, INPUT);  //set digital chrg pin mode


  // Initialize executed flag
  for (int j = 1024; j < 2048; j += 2) {
      memcpy((byte*)j, 0, 2);
  }
  
}

void loop() {
  // put your main code here, to run repeatedly:
  stateValue = digitalRead(inPin); //read digial pin inPin (7)


  byte byteArray[12]; // Byte array to store analog readings
  
  byte receivedBytes[4];
  int currentIndexTime = 0;
  
  t_1 = millis(); //get millis

  //get digital pin value and check if equal to stateValue  
  if (stateValue == 1){

      // wait until at least 4 bytes are available
      if (Serial.available() == 4 + NUM_BYTES_TO_SKIP){
        
      t_reset = millis();
      // skip the first 3 bytes
      for (int i = 0; i < NUM_BYTES_TO_SKIP; i++) {
        Serial.read();
        }

      // read the next 4 bytes
      while (currentIndexTime < 4) {
        byte b = Serial.read();
        receivedBytes[currentIndexTime++] = b;
        }
    
      // combine the 4 bytes into an integer in big endian format
      uint32_t receivedInt = ((uint32_t)receivedBytes[0] << 24) | ((uint32_t)receivedBytes[1] << 16) | ((uint32_t)receivedBytes[2] << 8) | (uint32_t)receivedBytes[3];
      t_2 = receivedInt;
      }  
     t_1 = millis() - t_reset;
    
    if (executed==false) {
  
    for (int j = 1024; j < 2048; j += 6) {
      memcpy(&readData, (byte*)j, 2);
      memcpy(&readTime, (byte*)(j+2), 4);
      if (readData > 0) {
        //Serial.print(readTime);
        //Serial.print("\t");
        //Serial.print(readData);
        //Serial.print("\t");  
        //Serial.print(0);
        //Serial.print("\t");  
        //Serial.println(0);
        resetValue = 0;
        
        //time to byte array
        byteArray[0] = readTime & 0xFF; // Extract the least significant byte
        byteArray[1] = (readTime >> 8) & 0xFF;
        byteArray[2] = (readTime >> 16) & 0xFF;
        byteArray[3] = (readTime >> 24) & 0xFF; // Extract the most significant byte

        //data to byte array
        byteArray[4] = readData & 0xFF; // Extract the least significant byte
        byteArray[5] = (readData >> 8) & 0xFF;

        int zeroValue = 0;
        
        //data to byte array
        byteArray[6] = zeroValue & 0xFF; // Extract the least significant byte
        byteArray[7] = (zeroValue >> 8) & 0xFF;

        //data to byte array
        byteArray[8] = zeroValue & 0xFF; // Extract the least significant byte
        byteArray[9] = (zeroValue >> 8) & 0xFF;

        int transferState = 1;

        //transfering data signal (from microcontroler memory)
        byteArray[10] = transferState & 0xFF; // Extract the least significant byte
        byteArray[11] = (transferState >> 8) & 0xFF;
        

       Serial.write(byteArray, sizeof(byteArray));

       memcpy((byte*)j, &resetValue, 2); // reset SRAM values
      }
      delay(50);
    } 
    int i = 0; //reset SRAM address index
    executed = true;  // Set the flag to indicate that the code has been executed
    delay(50);
    
  }


    
    //set measurement frequiency in milliseconds while the sensor is connected to phone
   if (t_1 - t_0 >= 1000 && executed==true){
      
      // Read the analog inputs
      int sensorValue = analogRead(0);
      int batteryValue = analogRead(4);
      int tempValue = analogRead(3);
      chrgValue = digitalRead(chrgPin); //read digial pin inPin (13)

       // Add the new values to the arrays
       sensorValues[currentIndex] = sensorValue;
       batteryValues[currentIndex] = batteryValue;
       tempValues[currentIndex] = tempValue;
       
       // Increment the index (wrap around if necessary)
       currentIndex = (currentIndex + 1) % windowSize;
       
       // Calculate the averages
       int sensorSum = 0;
       int batterySum = 0;
       int tempSum = 0;
       for (int i = 0; i < windowSize; i++) {
           sensorSum += sensorValues[i];
           batterySum += batteryValues[i];
           //tempSum += tempValues[i];
       }
       int smoothedSensorValue = sensorSum / windowSize;
       //int smoothedBatteryValue = batterySum / windowSize;
       //int smoothedBatteryValue = chrgValue;
       //if (chrgValue==1){
        if (batteryValue>706){
          smoothedBatteryValue = 2000;
       }
       else {
          smoothedBatteryValue = batterySum / windowSize;
          //smoothedBatteryValue = batteryValue;
          
       }
       
       //int smoothedTempValue = tempSum / windowSize;
       int smoothedTempValue = tempValue;
       
       
       t = t_1/1000 + t_2;
       // Print the values to the Serial monitor
       //Serial.print(t);
       //Serial.print("\t");
       //Serial.print(smoothedSensorValue);
       //Serial.print("\t");
       //Serial.print(smoothedTempValue);
       //Serial.print("\t");
       //Serial.println(smoothedBatteryValue);
        //time to byte array
        byteArray[0] = t & 0xFF; // Extract the least significant byte
        byteArray[1] = (t >> 8) & 0xFF;
        byteArray[2] = (t >> 16) & 0xFF;
        byteArray[3] = (t >> 24) & 0xFF; // Extract the most significant byte

        //data to byte array
        byteArray[4] = smoothedSensorValue & 0xFF; // Extract the least significant byte
        byteArray[5] = (smoothedSensorValue >> 8) & 0xFF;

        //data to byte array
        byteArray[6] = smoothedTempValue & 0xFF; // Extract the least significant byte
        byteArray[7] = (smoothedTempValue >> 8) & 0xFF;

        //data to byte array
        byteArray[8] = smoothedBatteryValue & 0xFF; // Extract the least significant byte
        byteArray[9] = (smoothedBatteryValue >> 8) & 0xFF;

        int transferState = 0;

        //transfering data signal (from microcontroler memory)
        byteArray[10] = transferState & 0xFF; // Extract the least significant byte
        byteArray[11] = (transferState >> 8) & 0xFF;

       Serial.write(byteArray, sizeof(byteArray));
       t_0 = t_1; //save old time t_1 to t_0        
       
      }
    }
    
  if (stateValue == 0){
    executed = false;
    t_1 = millis() - t_reset;
    
    //set measurement frequiency in milliseconds while the sensor is not connected to phone (dt =  1 s)
    if (t_1 - t_0 >= 1000){ 
      
      // Read the analog inputs
      int sensorValue = analogRead(0);

       if (sensorValue > 0) {
          t = t_1/1000 + t_2;
          memcpy((byte*)i, &sensorValue, 2); // save the updated struct to SRAM
          memcpy((byte*)(i+2), &t, 4); // save the updated struct to SRAM
           
       }
       
       i += 6;
       if (i > 2048){
        i = 1024;
       }
       
       t_0 = t_1; //save old time t_1 to t_0
       // Sleep after each measurement when not connected to the phone
       LowPower.powerDown(SLEEP_1S, ADC_OFF, BOD_OFF); // Sleep for 1 second
      
      }   
    }  
    
}
