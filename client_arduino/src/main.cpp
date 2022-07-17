#include <Arduino.h>
#include <globalFunctions.h>
#include <setup.h>
#include <statusUpdate.h>

void setup() {
  int LED = 2;
  
  pinMode(LED, HIGH);
  if (askForSetup()) {
    if (setupInitialise()) {
      Serial.println("Setup successful. Entering sleep...");
    } else {
      Serial.println("Setup failed, please try again");
    }
  }
  pinMode(LED, LOW);
  
  // sendStatusToServer();
  
  enterSleep();
}

void loop() {
}
