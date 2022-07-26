#include "header.h"

void setup() {
  int eepromAddress = 0;

  if (askForSetup()) {
    runSetup();
  } else {
    wifiToken wifi = readSettingsFromEEPROM(eepromAddress);
    connectToWiFi(&wifi.ssid, &wifi.password);

    // sendStatusToServer();
  }

  enterSleep();
}

void loop() {
  /*
   * setup() runs the program once, then enters sleep for a set amount of time — after that, it is rerun.
   */
}
