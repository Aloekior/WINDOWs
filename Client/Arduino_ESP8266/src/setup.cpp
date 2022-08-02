#include "header.h"

bool askForSetup() {
  unsigned int times = 5;
  unsigned int current = 0;

  prepareSerial();

  Serial.print("\nPress enter to begin setup");
  delay(1000);
  while (Serial.available() <= 0 && current++ < times) {
    Serial.print(".");
    delay(1000);
  }
  Serial.println();
  return (Serial.available() > 0);
}

void runSetup() {
  byte LED = 2;
  pinMode(LED, HIGH);
  if (setupInitialise()) {
    blinkSuccess();
  } else {
    blinkError();
  }
  pinMode(LED, LOW);
}

bool setupInitialise() {
  byte eepromAddress = 0;
  clearEEPROM();

  Serial.println("Setup started");
  wifiSettings wifi = setupWiFi();
  serverItems serverConfig = getServerToken();

  storeSettingsInEEPROM(eepromAddress, &wifi, &serverConfig);
  return true;
}

wifiSettings setupWiFi() {
  String ssid;
  String password;

  do {
    String comment = "Enter WiFi-SSID";
    ssid = getStringFromSerial(&comment);
    comment = "Enter WiFi-password";
    password = getStringFromSerial(&comment);
  } while (!testWiFiConnection(&ssid, &password));

  return {ssid, password};
}

bool testWiFiConnection(String *ssid, String *password) {
  byte wifiConnectedStatus = 3; // status 3 == WL_CONNECTED; see project readme for information

  Serial.println("Connecting to '" + *ssid + "'...");

  connectToWiFi(ssid, password);

  if (WiFi.status() == wifiConnectedStatus) {
    Serial.println("Connection to '" + *ssid + "' SUCCESSFUL");
    Serial.flush();
    blinkSuccess();
    return true;
  }
  Serial.println("Connection to '" + *ssid + "' FAILED");
  Serial.flush();
  blinkError();
  return false;
}

serverItems getServerToken() {
  int serverPort = 57336;
  unsigned long messageDelay = 50000;
  bool messageDisplay = false;
  String comment = "Please enter server IP-address";
  IPAddress serverIP;
  WiFiClient serverConnection;

  serverIP.fromString(getStringFromSerial(&comment));
  Serial.print("Server setup initiated... Connecting to ");
  Serial.println(serverIP);

  while (serverConnection.connect(serverIP, serverPort) == 0) {
    if (millis() > messageDelay && !messageDisplay) {
      messageDisplay = true;
      Serial.println("Is the server IP-Address correct?\nIs the server waiting for a new client?");
      Serial.flush();
    }
    delay(100);
  }

  serverConnection.println(WiFi.macAddress());

  while (serverConnection.available() == 0) {
    delay(50);
  }
  String token = serverConnection.readStringUntil('\n');

  Serial.println("Token received: '" + token + "'");

  serverConnection.stop();
  return {serverIP, token};
}
