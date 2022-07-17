#include <Arduino.h>
#include <WiFiClient.h>

struct wifiSettings {
  String ssid;
  String password;
};

struct serverItems {
  IPAddress ip;
  String token;
};

struct wifiToken {
  String ssid;
  String password;
  IPAddress ip;
  String token;
};
