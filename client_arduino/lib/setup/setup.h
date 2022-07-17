#include <Arduino.h>
#include <globalFunctions.h>

bool askForSetup();

int setupInitialise();

wifiSettings setupWiFi();

String getServerToken(String* token);

int storeSettingsToEEPROM(wifiSettings* wifi, String* token);
