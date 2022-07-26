#include "header.h"

void setup() {
  if (eepromCheck()) {
    delay(2000);
    if (askForSetup()) {
      runSetup();
    } else {
      runStatusUpdate();
    }
  } else {
    prepareSerial();
    runSetup();
  }

  enterSleep();
}

void loop() {
  /*
   * setup() runs the program once, then enters sleep for a set amount of time — after that, it is rerun.
   */
}
