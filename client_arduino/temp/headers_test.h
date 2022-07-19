#ifndef headers_h
#define headers_h

#include <Arduino.h>
#include <globalFunctions.h>
#include <setup.h>
#include <statusUpdate.h>
#include <customStructs.h>
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
  char ssid[31];
  byte passwordSize;
  char password[63];
  byte tokenSize = 4;
  char token[4];
  IPAddress ipAddress;
};

#endif