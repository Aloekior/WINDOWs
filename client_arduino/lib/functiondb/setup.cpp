#include "headers.h"

bool askForSetup() {
  unsigned long timeOut = 10000;
  
  prepareSerial();
  
  Serial.print("\nPress any key to enter setup");
  delay(1000);
  while (Serial.available() <= 0 && millis() < timeOut) {
    Serial.print(". ");
    delay(1000);
  }
  Serial.println();
  return (Serial.available()>0);  
}

bool setupInitialise() {
  int eepromAddress = 0;
  wifiSettings wifi = setupWiFi();
  serverItems serverConfig = getServerToken();
  
  prepareSettingsForEEPROM(eepromAddress, &wifi, &serverConfig);
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

void stringToChar(byte length, String input, char* storageChar) {
  for (byte i = 0; i < length; i++) {
    storageChar[i] = input[i];
  }
}

void prepareSettingsForEEPROM(int address, wifiSettings* wifi, serverItems* serverItems) {
  byte charSize = 1;
  byte ssidSize = wifi->ssid.length() + charSize;
  char ssid[31];
  byte passwordSize = wifi->password.length() + charSize;
  char password[63];
  byte tokenSize = 4;
  char token[4];
  
  stringToChar(ssidSize, wifi->ssid, ssid);
  stringToChar(passwordSize, wifi->password, password);
  stringToChar(tokenSize, serverItems->token, token);
  
  wifiToken setupConfiguration = {ssidSize, ssid, passwordSize, password, tokenSize, token, *serverItems->ipAddress};
  
  writeSettingsToEEPROM(address, &setupConfiguration);
}

void writeSettingsToEEPROM(int address, wifiToken* settings) {
  int currentAddress = address;
  
  writeSingleObjectToEEPROM(currentAddress, settings->ssid, settings->ssidSize); // ssid

  writeSingleObjectToEEPROM(currentAddress, settings->password, settings->passwordSize); // password
  
  writeSingleObjectToEEPROM(currentAddress, settings->token, settings->tokenSize); // token

  writeIPToEEPROM(currentAddress, *settings->ipAddress); // ip
}

void writeSingleObjectToEEPROM(int* address, char object[], byte size) {
  byte tempAddress;
  
  EEPROM.write(*address, size);
  *address++;
  for (byte i = 0; i < size; i++) {
    EEPROM.write(tempAddress + i, object[i]);
  }
  *address += size;
}

void writeIPToEEPROM(int address, IPAddress ip) {
  for (byte i = 0; i < 4; i++) {
    EEPROM.write(address++, ip[i]);
  }
}
