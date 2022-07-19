#include "headers.h"

void setup() {
  byte LED = 2;
  
  if (askForSetup()) {
    pinMode(LED, HIGH);
    if (setupInitialise()) {
      Serial.println("Setup successful.");
    } else {
      Serial.println("Setup failed, please try again");
    }
    pinMode(LED, LOW);
  }
  
  readFromEEPROM();

  // sendStatusToServer();
  
  enterSleep();
}

void loop() {
}
