#include <Arduino.h>
#include <customStructs.h>

void prepareSerial();

void enterSleep();

String getStringFromSerial(String comment = "Enter String:");

int testWiFiConnection (String* ssid, String* password);

void connectToWiFi(String* ssid, String* password);

void sendServerUpdate();

void readSettingsFromEEPROM(int address, void* wifiToken);
