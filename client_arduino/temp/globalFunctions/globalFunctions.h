#include <headers.h>

void prepareSerial();

void enterSleep();

String getStringFromSerial(String comment);

int testWiFiConnection (String* ssid, String* password);

void connectToWiFi(String* ssid, String* password);

void sendServerUpdate();

void readSettingsFromEEPROM(byte address, void* wifiToken);
