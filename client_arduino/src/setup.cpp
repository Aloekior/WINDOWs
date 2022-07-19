#include "headers.h"

bool askForSetup() {
  unsigned long timeOut = 10000;
  
  prepareSerial();
  
  Serial.print("\nPress enter to begin setup");
  delay(1000);
  while (Serial.available() <= 0 && millis() < timeOut) {
    Serial.print(". ");
    delay(1000);
  }
  Serial.println();
  return (Serial.available()>0);  
}

bool setupInitialise() {
  byte eepromAddress = 0;
  wifiSettings wifi = setupWiFi();
  // serverItems serverConfig = getServerToken();
  serverItems serverConfig = {IPAddress(111,111,111,111), "a3f4"};
  
  storeSettingsInEEPROM(eepromAddress, wifi, serverConfig);
  return true;
}

wifiSettings setupWiFi() {
  String ssid;
  String password;
  
  do {
    ssid = getStringFromSerial("Enter WiFi-SSID");
    password = getStringFromSerial("Enter WiFi-password");
  } while (!testWiFiConnection(&ssid, &password));
  
  return {ssid, password};
}

serverItems getServerToken() {
  int serverPort = 51337;
  unsigned long timeOut = 30000;
  IPAddress serverIP;
  WiFiClient serverConnection;
  String token;
  
  serverIP.fromString(getStringFromSerial("Please enter server IP-address"));
  Serial.println("Server setup started... Please initiate connection procedure on server within 30 seconds!");
  while (!serverConnection.connect(serverIP, serverPort) && millis() < timeOut) {
    delay(500);
  }
  if (serverConnection.connected()) {
    serverConnection.println(WiFi.macAddress());
    while (serverConnection.connected() && !serverConnection.available()) {
      delay(50);
    }
    token = serverConnection.readStringUntil('\0');
  }
  serverConnection.stop();
  return {serverIP, token};
}

void stringToChar(String source, char* target) {
  for (unsigned int i = 0; i < source.length(); i++) {
    target[i] = source[i];
  }
}

void storeSettingsInEEPROM(int address, wifiSettings wifiInput, serverItems serverItems) {  
  
  int l1 = wifiInput.ssid.length();
  int l2 = wifiInput.password.length();
  int l3 = serverItems.token.length();
  IPAddress ip = serverItems.ipAddress;
  
  eepromSettings set = {l1, {}, l2, {}, l3, {}, {ip[0], ip[1], ip[2], ip[3]}};
  
  stringToChar(wifiInput.ssid, set.ssid);
  stringToChar(wifiInput.password, set.password);
  stringToChar(serverItems.token, set.token);
  
  EEPROM.begin(sizeof(eepromSettings));
  
  EEPROM.put(address, set);
  
  EEPROM.end();
}
