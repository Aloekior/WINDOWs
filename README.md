# WINDOWs

A project based on Raspberry-like combined with ESP8266 Arduino-like microcomputers to track the state of opened windows in a whole floor.

## Documentation

ESP8266:
<https://arduino-esp8266.readthedocs.io/en/latest/>

## Libraries

ESP8266:
<https://arduino.esp8266.com/stable/package_esp8266com_index.json>

### Flashing Firmware to AZ-Delivery D1 MINI ESP8266

---
Espressif NON-OS SDK:
<https://github.com/espressif/ESP8266_NONOS_SDK/tree/release/v2.2.x>

extract following files:

- bin/boot_v1.7.bin
- bin/at/512+512/user1.1024.new.2.bin (4MB Flash = 32 Mbit, 512+512)
- bin/esp_init_data_default_v08.bin
- bin/blank.bin

NodeMCU Flasher Tool:
<https://github.com/nodemcu/nodemcu-flasher>

### WiFi status codes

---

- 0: **WL_IDLE_STATUS** when Wi-Fi is in process of changing between statuses
- 1: **WL_NO_SSID_AVAIL** in case configured SSID cannot be reached
- 3: **WL_CONNECTED** after successful connection is established
- 4: **WL_CONNECT_FAILED** if connection failed
- 6: **WL_CONNECT_WRONG_PASSWORD** if password is incorrect
- 7: **WL_DISCONNECTED** if module is not configured in station mode
