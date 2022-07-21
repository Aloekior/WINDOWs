#ifndef header_h
#define header_h

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
  unsigned int ssidLength;
  char ssid[31];
  unsigned int passwordLength;
  char password[63];
  unsigned int tokenLength;
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

// write to EEPROM

void clearEEPROM();

void storeSettingsInEEPROM(int address, wifiSettings* wifi, serverItems* serverItems);

// global

wifiSettings setupWiFi();

serverItems getServerToken();

void prepareSerial();

void enterSleep();

String getStringFromSerial(String comment);

bool testWiFiConnection (String* ssid, String* password);

void connectToWiFi(String* ssid, String* password);

void sendServerUpdate();

void sendStatusToServer();

void stringToChar(String string, char* charArray);

String charToString(char* charArray, int size);

void blink(int times, int delayBetween);

void blinkSuccess();

void blinkError();

// read from EEPROM

wifiToken readSettingsFromEEPROM(int address);

void readFromEEPROM();

#endif
