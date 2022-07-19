#ifndef headers_h
#define headers_h

#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <EEPROM.h>
#include <WiFiClient.h>

struct wifiSettings {
  String ssid;
  String password;
};

struct serverItems {
  IPAddress ipAddress;
  String token;
};

struct wifiToken {
  byte ssidSize;
  char* ssid;
  byte passwordSize;
  char* password;
  byte tokenSize = 4;
  char* token;
  IPAddress ipAddress;
};


bool askForSetup();

bool setupInitialise();

wifiSettings setupWiFi();

serverItems getServerToken();

void prepareSettingsForEEPROM(int address, wifiSettings* wifi, serverItems* serverItems);

void writeSettingsToEEPROM(int address, wifiToken* settings);

void writeSingleObjectToEEPROM(int address, char object[], byte size);

void writeIPToEEPROM(int address, IPAddress ip);

void writeIPBlockToEEPROM(int address, byte ipBlock);

void prepareSerial();

void enterSleep();

String getStringFromSerial(String comment);

bool testWiFiConnection (String* ssid, String* password);

void connectToWiFi(String* ssid, String* password);

void sendServerUpdate();

void readSettingsFromEEPROM(byte address, void* wifiToken);

void sendStatusToServer();

#endif