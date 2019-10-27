
char i = 'n';
void setup() {
  Serial.begin(115200); //Needed for bluetooth connection

}

void loop() {
  int value = analogRead(A0); //Analog input
  delay(10);
  float voltage = value * (5/1023.0); //To read voltage value For testing

  //Sends data when needed
  if(Serial.available()){
    i = Serial.read();
  }

  //Send data when y is received
  if(i=='y'){  
    Serial.print(voltage);
    delay(400);
    Serial.println();
 }  

}
