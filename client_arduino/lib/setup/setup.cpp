#include <ESP8266WiFi.h>
#include <EEPROM.h>
#include <WiFiClient.h>
#include <globalFunctions.h>

bool askForSetup() {
  int timeOut = 10000;
  
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
  
  storeSettingsInEEPROM(eepromAddress, &wifi, &serverConfig);
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
  int timeOut = 30000;
  IPAddress serverIP;
  WiFiClient serverConnection;
  String token;
  
  serverIP.fromString(getStringFromSerial("Please enter server IP-address"));
  Serial.println("Server setup started... Please initiate connection procedure on server within 30 seconds!");
  while (!serverConnection.connect(serverIP, serverPort) && millis() < timeOut) {
    delay(503);
  }
  if (serverConnection.connected()) {
    serverConnection.println(WiFi.macAddress());
    while (serverConnection.connected() && !serverConnection.available()) {
      delay(47);
    }
    token = serverConnection.readStringUntil('\0');
  }
  serverConnection.stop();
  return {serverIP, token};
}

void storeSettingsInEEPROM(int address, wifiSettings* wifi, serverItems* serverItems) {
  wifiToken setupConfiguration = {wifi->ssid, wifi->password, serverItems->ip, serverItems->token};
  EEPROM.put(address, &setupConfiguration);
}
