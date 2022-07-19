#ifndef headers_h
#define headers_h

#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP_EEPROM.h>
#include <WiFiClient.h>

struct wifiSettings {
  String ssid;
  String password;
};

struct serverItems {
  IPAddress ipAddress;
  String token;
};

struct eepromSettings {
  int ssidLength;
  char ssid[31];
  int passwordLength;
  char password[63];
  int tokenLength;
  char token[5];
  byte ipAddress[4];
};

struct wifiToken {
  String ssid;
  String password;
  String token;
  IPAddress ipAddress;
};

// setup

bool askForSetup();

bool setupInitialise();

// global

wifiSettings setupWiFi();

serverItems getServerToken();

void prepareSerial();

void enterSleep();

String getStringFromSerial(String comment);

bool testWiFiConnection (String* ssid, String* password);

void connectToWiFi(String* ssid, String* password);

void sendServerUpdate();

void readSettingsFromEEPROM(int address, void* wifiToken);

void sendStatusToServer();

String charToString(char* charArray, int size);

void stringToChar(char* charArray, String string);

// write to EEPROM
void storeSettingsInEEPROM(int address, wifiSettings wifi, serverItems serverItems);


// read from EEPROM test
wifiToken readSettingsFromEEPROM(int address);

void readFromEEPROM();

void printEEP();

#endif