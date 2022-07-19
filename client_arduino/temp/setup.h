#include <headers.h>

bool askForSetup();

bool setupInitialise();

wifiSettings setupWiFi();

String getServerToken(String* token);

void prepareSettingsForEEPROM(int address, wifiSettings* wifi, serverItems* serverItems);

void writeSettingsToEEPROM(int address, wifiToken* settings);

void writeSingleObjectToEEPROM(int address, char object[], byte size);

void writeIPToEEPROM(int address, IPAddress ip);

void writeIPBlockToEEPROM(int address, byte ipBlock);

