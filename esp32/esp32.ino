#include <Wire.h>
#include "MAX30100_PulseOximeter.h"

// reporting interval 10ms
#define CALLBACK_INTERVAL 100

// driver entity
PulseOximeter oximeter;

// timestap of the last output
int lastOutputTimestamp;

void setup() {
  // intit the serial interface for 115200 Baud
  Serial.begin(115200);
  while(!Serial);

  // try to init the oximeter and count the failed tries and delay 500ms.
  Serial.println("Begin initializing the oximeter");

  int initFailCounter = 0;

  while (!oximeter.begin(ESP_32)) {
    Serial.print("Init failed ");
    Serial.print(++initFailCounter);
    Serial.println(" times!");
    delay(500);
  }

  Serial.println("Oximeter initialized");
}

void loop() {
  // get values from oximeter.
  oximeter.update();

  // print heartrate and o2 ratio
  Serial.print(oximeter.getHeartRate());
  Serial.print(";");
  Serial.println(oximeter.getSpO2());

  delay(CALLBACK_INTERVAL);
}