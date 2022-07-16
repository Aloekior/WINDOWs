#include <ESP8266WiFi.h>
#include <EEPROM.h>

struct WifiSettings {
  String ssid;
  String password;
};

WifiSettings wifiSetup () {
  Serial.readString();
  Serial.println("Enter WiFi-SSID");
  Serial.flush();
  while (Serial.available() == 0) {}
  String ssid = Serial.readStringUntil('\n');
  
  Serial.println("Enter WiFi-password");
  Serial.flush();
  while (Serial.available() == 0) {}
  String password = Serial.readStringUntil('\n');

  return {ssid, password};
}

int initialiseWiFi (WifiSettings wifi) {
  WiFi.begin(wifi.ssid, wifi.password);
  int tries = 0;
  Serial.printf("Connecting to %s \n", wifi.ssid);
  while ((WiFi.status() != 3) && (tries < 16)) {
    tries++;
    Serial.print('*');
    delay(500);
  }
  Serial.println();

  if (WiFi.status() == 3) {
      Serial.println("WiFi success, IP:");
      Serial.println(WiFi.localIP());
      Serial.flush();
      return 0;
  }
  Serial.printf("WiFi failed, status: %d \n", WiFi.status());
  Serial.flush();
  return -1;
}

String getServerToken() {
  Serial.println("Hello from getServerToken()");
  return "TODO: implement";
}

int initialSetup() {
  WifiSettings wifi;
  do {
  wifi = wifiSetup();
  Serial.println(wifi.ssid);
  Serial.println(wifi.password);
  } while (initialiseWiFi(wifi) != 0);
  String token = getServerToken();
  /*if (storeSettingsToEEPROM(wifi, token)) {
    return 0;
  }*/
  
  return -1;
}

void setup() {
//  if (!EEPROM.get(0)) {
    Serial.begin(9600);
    Serial.println();
    initialSetup();
    Serial.println("Setup complete. Shutting down...");
    Serial.flush();
    Serial.end();
    ESP.deepSleepMax();
//  }
}

void loop() {
  // put your main code here, to run repeatedly:
}
