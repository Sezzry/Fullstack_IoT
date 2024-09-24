#include <dht.h>

dht DHT;

#define DHT11_PIN 2

void setup() {
  Serial.begin(9600);
}

void loop() {
  int chk = DHT.read11(DHT11_PIN);
  // Omvandla temperaturen till Celsius
  float temperatureCelsius = DHT.temperature;
  // Humidity i celcius XDDDDDD
  // Omvandla luftfuktighet till Celsius
  float humidityCelsius = DHT.humidity;
  
  Serial.print("Temperatur = ");
  Serial.print(temperatureCelsius);
  Serial.println(" °C");
  
  Serial.print("Luftfuktighet = ");
  Serial.print(humidityCelsius);
  Serial.println(" %");
  
  delay(60000);
}
