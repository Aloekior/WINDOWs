#include "headers.h"

String charToString(byte length, char stringToBuild[]) {
   String returnString;
   for (byte i = 0; i < length; i++) {
      returnString += stringToBuild[i];
   }
   return returnString;
}

/*void sendStatusToServer() {
   byte eepromAddress = 0;
   byte buttonGPIO = 4;
   bool status = digitalRead(buttonGPIO);
   wifiToken settings;
   readSettingsFromEEPROM(eepromAddress, &settings);
   
   String ssid = charToString(settings.ssidSize, settings.ssid);
   String password = charToString(settings.passwordSize, settings.password);
   String token = charToString(settings.tokenSize, settings.token);
   
   while(!testWiFiConnection(&ssid, &password)) {
      delay(5000);
   }
}*/
