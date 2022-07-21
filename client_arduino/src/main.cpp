#include "header.h"

void setup() {
  if (askForSetup()) {
    byte LED = 2;
    pinMode(LED, HIGH);
    if (setupInitialise()) {
      blinkSuccess();
    } else {
      blinkError();
    }
    pinMode(LED, LOW);
  }
  
  readFromEEPROM();

  // sendStatusToServer();
  
  enterSleep();
}

void loop() {
    /*
     * setup() runs the program once, then enters sleep for a set amount of time — after that, it is rerun.
     */
}
