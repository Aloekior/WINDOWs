#include "header.h"

/* Serial functions */

void prepareSerial() {
  Serial.begin(9600);
  Serial.println();
  delay(4000);
  Serial.readString();
}

String getStringFromSerial(String comment) {
  Serial.readString(); // clear Serial buffer before reading to discard leftovers
  Serial.println(comment);
  Serial.flush();
  while (Serial.available() == 0) {}
  return Serial.readStringUntil('\n'); // make sure Serial Monitor is set to end of line == LF (newline)
}

/* Wi-Fi functions */

void waitForWiFi() {
  byte LED = 2;
  byte tries = 0;

  while ((WiFi.status() == 0 || WiFi.status() == 7) && (tries < 31)) {
    pinMode(LED, tries % 2);
    tries++;
    delay(1000);
  }
}

void connectToWiFi(String *ssid, String *password) {
  WiFi.begin(*ssid, *password);
  waitForWiFi();
  if (WiFi.status() == 3) {
    blinkSuccess();
  } else {
    blinkError();
  }
}

/* String manipulation functions */

String charToString(char *charArray, int size) {
  String string;
  for (int i = 0; i < size; i++) {
    string += charArray[i];
  }
  return string;
}

void stringToChar(String source, char *target) {
  for (unsigned int i = 0; i < source.length(); i++) {
    target[i] = source[i];
  }
}

/* LED blink functions */

void blink(int times, int delayBetween) {
  byte LED = 2;
  while (times-- > 0) {
    pinMode(LED, HIGH);
    delay(delayBetween);
    pinMode(LED, LOW);
    delay(delayBetween);
  }
}

void blinkSuccess() {
  blink(3, 500);
}

void blinkError() {
  blink(10, 75);
}

/* Sleep mode functions */

void enterSleep() {
  Serial.println("Entering sleep...");
  Serial.flush();
  Serial.end();
  EspClass::deepSleep(12e8);
}
