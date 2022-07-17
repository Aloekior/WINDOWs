#include <Arduino.h>
#include "setup.h"

void setup() {
  int LED = 2;
  pinMode(LED, HIGH);
  if (askForSetup()) {
    setupInitialise();
    Serial.println("Setup complete. Entering sleep...");
  }
  pinMode(LED, LOW);
  Serial.println("Entering sleep...");
  Serial.flush();
  Serial.end();
  ESP.deepSleep(12e8);
}

void loop() {
  // put your main code here, to run repeatedly:
}
