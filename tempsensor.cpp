#include <dht.h>

dht DHT;

#define DHT11_PIN 2

void setup() {
  Serial.begin(9600);
}

void loop() {
  int chk = DHT.read11(DHT11_PIN);
  float temperatureCelsius = DHT.temperature;
  float humidityCelsius = DHT.humidity;

  
  Serial.print("Temperature: ");
  Serial.print(temperatureCelsius);
  Serial.print(", Humidity: ");
  Serial.println(humidityCelsius);

  delay(60000);
}
