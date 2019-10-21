int ledpin = 13;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);

}

void loop() {
  if(Serial.available()){
    Serial.write(Serial.read());
    Serial.println();  
    
  }
  

}
