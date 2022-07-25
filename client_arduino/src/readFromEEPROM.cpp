#include "header.h"

wifiToken readSettingsFromEEPROM(int address) {
   eepromSettings set;
   EEPROM.begin(sizeof(eepromSettings));
   EEPROM.get(address, set);
   String ssid = charToString(set.ssid,set.ssidLength);
   String password = charToString(set.password,set.passwordLength);
   String token = charToString(set.token,set.tokenLength);
   IPAddress ip = {set.ipAddress[0],set.ipAddress[1],set.ipAddress[2],set.ipAddress[3]};
   
   return {ssid, password, token, ip};
}

void readFromEEPROM() {
   int address = 0;
   wifiToken wifi = readSettingsFromEEPROM(address);
   
   connectToWiFi(&wifi.ssid, &wifi.password);
   if (WiFi.status() == 3) {
      blinkSuccess();
   } else {
      blinkError();
   }
}
