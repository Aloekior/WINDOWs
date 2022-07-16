#include <ESP8266WiFi.h>
#include <EEPROM.h>

struct wifiSettings {
  String ssid;
  String password;
};

void setupConnectToWiFi(String* ssid, String* password) {
  WiFi.begin(*ssid, *password);
  int tries = 0;
  Serial.printf("Connecting to '%s' \n", *ssid);
  while ((WiFi.status() != 3) && (tries < 21)) {
    tries++;
    Serial.print('*');
    delay(500);
  }
  Serial.println();
}

int setupTestWiFi (String* ssid, String* password) {
  setupConnectToWiFi(ssid, password);
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

String getStringFromSerial(String comment = "Enter String:") {
  Serial.readString();
  Serial.println(comment);
  Serial.flush();
  while(Serial.available()==0) {}
  return Serial.readStringUntil('\n');
}

wifiSettings setupWiFi () {
  String ssid;
  String password;
  do {
    ssid = getStringFromSerial("Enter WiFi-SSID");
    password = getStringFromSerial("Enter WiFi-password");
  } while (setupTestWiFi(&ssid, &password) != 0);
  
  return {ssid, password};
}

void getServerToken(String* token) {
  Serial.println("Hello from getServerToken()");
  *token = "hallo";
}

int setupInitialise() {
  wifiSettings wifi = setupWiFi();
//  getServerToken(token);
  /*if (storeSettingsToEEPROM(wifi, token)) {
    return 0;
  }*/
  return -1;
}

bool askForSetup() {
  Serial.begin(9600);
  Serial.println();
  delay(2000);
  Serial.readString();
  Serial.print("\nPress any key to enter setup");
  delay(1000);
  while (Serial.available() <= 0 && millis() < 10000) {
    Serial.print(". ");
    delay(1000);
  }
  Serial.println();
  return (Serial.available()>0);  
}

void setup() {
  if (askForSetup()) {
    setupInitialise();
    Serial.println("Setup complete. Entering sleep...");
  }
  Serial.println("Entering sleep...");
  Serial.flush();
  Serial.end();
  ESP.deepSleep(12e8);
}

void loop() {
  // put your main code here, to run repeatedly:
}
