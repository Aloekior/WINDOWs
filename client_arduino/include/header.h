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
  char token[37];
  byte ipAddress[4];
};

struct wifiToken {
  String ssid;
  String password;
  String token;
  IPAddress ipAddress;
};

// setup functions

bool askForSetup();

void runSetup();

bool setupInitialise();

wifiSettings setupWiFi();

serverItems getServerToken();

// EEPROM functions

bool eepromCheck();

void clearEEPROM();

void storeSettingsInEEPROM(int address, wifiSettings *wifi, serverItems *serverItems);

wifiToken readSettingsFromEEPROM(int address);

// global

void prepareSerial();

String getStringFromSerial(String comment);

bool testWiFiConnection(String *ssid, String *password);

void connectToWiFi(String *ssid, String *password);

void runStatusUpdate();

bool sendStatusToServer(String* token, IPAddress* ipAddress, bool state);

void stringToChar(String string, char *charArray);

String charToString(char *charArray, int size);

void blink(int times, int delayBetween);

void blinkSuccess();

void blinkError();

void enterSleep();

#endif
