#include "header.h"

bool askForSetup() {
  unsigned long timeOut = 10000;
  
  prepareSerial();
  
  Serial.print("\nPress enter to begin setup");
  delay(1000);
  while (Serial.available() <= 0 && millis() < timeOut) {
    Serial.print(".");
    delay(1000);
  }
  Serial.println();
  return (Serial.available()>0);  
}

bool setupInitialise() {
  clearEEPROM();
    
  byte eepromAddress = 0;
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
    int serverPort = 57332;
    unsigned long messageDelay = 30000;
    bool messageDisplay = false;
    IPAddress serverIP;
    WiFiClient serverConnection;
    
    serverIP.fromString(getStringFromSerial("Please enter server IP-address"));
    Serial.print("Server setup initiated... Connecting to ");
    Serial.println(serverIP);
    
    while (serverConnection.connect(serverIP, serverPort) == 0) {
        if (millis() > messageDelay && !messageDisplay) {
            messageDisplay = true;
            Serial.println("Is the server IP-Address correct?");
            Serial.println("Is the server awaiting a new client?");
            Serial.flush();
        }
        delay(100);
    }
    Serial.println(serverConnection.available());

    serverConnection.println(WiFi.macAddress());
    
    while (serverConnection.available() == 0) {
        delay(50);
    }
    String token = serverConnection.readStringUntil('\n');
    
    Serial.println("Token received: '" + token + "'");
    
    serverConnection.stop();
    return {serverIP, token};
}

void storeSettingsInEEPROM(int address, wifiSettings* wifiInput, serverItems* serverItems) {  
  
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
