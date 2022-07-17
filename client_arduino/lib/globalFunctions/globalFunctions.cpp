#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <EEPROM.h>
#include <customStructs.h>

void enterSleep() {
  Serial.println("Entering sleep...");
  Serial.flush();
  Serial.end();
  ESP.deepSleep(12e8);
}

void prepareSerial() {
  Serial.begin(9600);
  Serial.println();
  delay(2000);
  Serial.readString();
}

String getStringFromSerial(String comment = "Enter String:") {
  Serial.readString(); // clear Serial buffer before reading to discard leftovers
  Serial.println(comment);
  Serial.flush();
  while(Serial.available()==0) {}
  return Serial.readStringUntil('\n'); // make sure Serial Monitor is set to end of line == LF (newline)
}

bool testWiFiConnection (String* ssid, String* password) {
   int wifiConnectedStatus = 3; // status 3 == WL_CONNECTED; see project readme for information
   
  connectToWiFi(ssid, password);
  if (WiFi.status() == wifiConnectedStatus) { 
      Serial.println("WiFi success, IP:");
      Serial.println(WiFi.localIP());
      Serial.flush();
      return true;
  }
  Serial.println("WiFi failed with status: " + WiFi.status());
  /*
   detailed status message was not possible in my testing.
   even entering wrong SSID or wrong password would always result in status 7 instead of 1 or 6
  */
  Serial.flush();
  return false;
}

void connectToWiFi(String* ssid, String* password) {
  int tries = 0;
  
  WiFi.begin(*ssid, *password);
  Serial.println("Connecting to " + *ssid);
  while ((WiFi.status() != 3) && (tries < 21)) {
    tries++;
    Serial.print('*');
    delay(500);
  }
  Serial.println();
}

void readSettingsFromEEPROM(int address, void *wifiToken) {
   EEPROM.get(address, wifiToken);
}
