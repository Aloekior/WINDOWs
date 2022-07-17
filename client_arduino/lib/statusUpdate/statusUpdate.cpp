#include <Arduino.h>
#include <globalFunctions.h>
#include <customStructs.h>

void sendStatusToServer() {
   int eepromAddress = 0;
   int buttonGPIO = 4;
   bool status = digitalRead(buttonGPIO);
   wifiToken settings;
   readSettingsFromEEPROM(eepromAddress, &settings);
   
   while(!testWiFiConnection(&settings.ssid, &settings.password)) {
      delay(5000);
   }
   
   
}
