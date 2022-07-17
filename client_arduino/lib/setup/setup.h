struct wifiSettings {
  String ssid;
  String password;
};

void setupConnectToWiFi(String* ssid, String* password);

int setupTestWiFi (String* ssid, String* password);

String getStringFromSerial(String comment = "Enter String:");

wifiSettings setupWiFi ();

void getServerToken(String* token);

int setupInitialise();

bool askForSetup();