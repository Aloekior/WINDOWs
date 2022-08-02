#include "header.h"

bool eepromCheck() {
  int eepromAddress = 0;
  EEPROM.begin(sizeof(eepromSettings));

  return (EEPROM.read(eepromAddress) > 0);
}

void clearEEPROM() {
  int eepromMaxSize = 4096;
  EEPROM.begin(eepromMaxSize);

  for (int i = 0; i < eepromMaxSize; i++) {
    EEPROM.write(i, 0);
  }
  EEPROM.end();
}

void storeSettingsInEEPROM(int address, wifiSettings *wifiInput, serverItems *serverItems) {

  int l1 = (int) wifiInput->ssid.length();
  int l2 = (int) wifiInput->password.length();
  int l3 = (int) serverItems->token.length();
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
  eepromSettings set = EEPROM.get(address, set);
  EEPROM.begin(sizeof(eepromSettings));

  String ssid = charToString(set.ssid, set.ssidLength);
  String password = charToString(set.password, set.passwordLength);
  String token = charToString(set.token, set.tokenLength);
  IPAddress ip = {set.ipAddress[0], set.ipAddress[1], set.ipAddress[2], set.ipAddress[3]};

  return {ssid, password, token, ip};
}
