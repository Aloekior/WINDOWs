#include "header.h"

void clearEEPROM() {
  int eepromMaxSize = 4096;
  EEPROM.begin(eepromMaxSize);
  for (int i = 0; i < eepromMaxSize; i++) {
    EEPROM.write(i, 0);
  }
  EEPROM.end();
}

void storeSettingsInEEPROM(int address, wifiSettings *wifiInput, serverItems *serverItems) {

  unsigned int l1 = wifiInput->ssid.length();
  unsigned int l2 = wifiInput->password.length();
  unsigned int l3 = serverItems->token.length();
  IPAddress ip = serverItems->ipAddress;

  eepromSettings set = {l1, {}, l2, {}, l3, {}, {ip[0], ip[1], ip[2], ip[3]}};

  stringToChar(wifiInput->ssid, set.ssid);
  stringToChar(wifiInput->password, set.password);
  stringToChar(serverItems->token, set.token);

  EEPROM.begin(sizeof(eepromSettings));

  EEPROM.put(address, set);

  EEPROM.end();
}

wifiToken readSettingsFromEEPROM(int address) {
  eepromSettings set;
  EEPROM.begin(sizeof(eepromSettings));
  EEPROM.get(address, set);

  String ssid = charToString(set.ssid, set.ssidLength);
  String password = charToString(set.password, set.passwordLength);
  String token = charToString(set.token, set.tokenLength);
  IPAddress ip = {set.ipAddress[0], set.ipAddress[1], set.ipAddress[2], set.ipAddress[3]};

  return {ssid, password, token, ip};
}
