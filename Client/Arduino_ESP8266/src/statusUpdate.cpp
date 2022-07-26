#include "header.h"

void runStatusUpdate() {
  byte eepromAddress = 0;
  byte buttonGPIO = 4;
  int tries = 0;
  bool state = digitalRead(buttonGPIO);
  
  wifiToken wifi = readSettingsFromEEPROM(eepromAddress);
  connectToWiFi(&wifi.ssid, &wifi.password);

  while (!sendStatusToServer(&wifi.token, &wifi.ipAddress, state) && tries < 3) {
    blinkError();
    tries++;
  }
}

bool sendStatusToServer(String* token, IPAddress* serverIP, bool state) {
  int serverPort = 57335;
  int tries = 0;
  WiFiClient serverConnection;
  String confirmation = "";
  
  while (serverConnection.connect(*serverIP, serverPort) == 0 && tries < 3) {
    blink(3,100);
    tries++;
  }
  if (serverConnection.connected()) {
    tries = 0;
    
    serverConnection.println(WiFi.macAddress());
    delay(50);
    serverConnection.println(*token);
    delay(50);
    serverConnection.println(state);

    confirmation = serverConnection.readStringUntil('\n');
    
    while(!confirmation.equals("OK") && tries < 3) {
      blink(3,150);
      confirmation = serverConnection.readStringUntil('\n');
      tries++;
    }
    if (confirmation.equals("OK")) {
      return true;
    }
  }
  return false;
}
