#include "header.h"

void prepareSerial() {
  Serial.begin(9600);
  Serial.println();
  delay(4000);
  Serial.readString();
}

void clearEEPROM() {
    int eepromMaxSize = 4096;
    EEPROM.begin(eepromMaxSize);
    for (int i = 0; i < eepromMaxSize; i++) {
        EEPROM.write(i, 0);
    }
    EEPROM.end();
}

String getStringFromSerial(String comment = "Enter String:") {
  Serial.readString(); // clear Serial buffer before reading to discard leftovers
  Serial.println(comment);
  Serial.flush();
  while(Serial.available()==0) {}
  return Serial.readStringUntil('\n'); // make sure Serial Monitor is set to end of line == LF (newline)
}

bool testWiFiConnection (String* ssid, String* password) {
  byte wifiConnectedStatus = 3; // status 3 == WL_CONNECTED; see project readme for information
  
  Serial.println("Connecting to '" + *ssid + "'");
  
  connectToWiFi(ssid, password);
  
  if (WiFi.status() == wifiConnectedStatus) { 
    Serial.println("Connected to '" + *ssid + "'");
    Serial.flush();
    blinkSuccess();
    return true;
  }
  Serial.println("WiFi FAILED");
  Serial.flush();
  blinkError();
  return false;
}

void connectToWiFi(String* ssid, String* password) {
    byte LED = 2;
    byte tries = 0;

    WiFi.begin(*ssid, *password);
    while ((WiFi.status() == 0) && (tries < 31)) {
        pinMode(LED, tries % 2);
        tries++;
        delay(1000);
    }
}

String charToString(char* charArray, int size) {
  String string;
  for (int i = 0; i < size; i++) {
    string += charArray[i];
  }
  return string;
}

void stringToChar(String source, char* target) {
    for (unsigned int i = 0; i < source.length(); i++) {
        target[i] = source[i];
    }
}

void blink(int times, int delayBetween){
  byte LED = 2;
  while (times-- > 0) {
    pinMode(LED,HIGH);
    delay(delayBetween);
    pinMode(LED,LOW);
    delay(delayBetween);
  }
}

void blinkSuccess() {
  blink(3,300);
}

void blinkError() {
  blink(10,100);
}

void enterSleep() {
  Serial.println("Entering sleep...");
  Serial.flush();
  Serial.end();
  ESP.deepSleep(12e8);
}
