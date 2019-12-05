
const int tempsval[] = {
-107,
-51,
-38,
-30,
-25,
-20,
-16,
-12,
-9,
-7,
-4,
-2,
0,
2,
4,
6,
7,
9,
11,
12,
13,
15,
16,
17,
19,
20,
21,
22,
23,
24,
25,
26,
27,
28,
29,
30,
31,
32,
33,
34,
35,
36,
37,
37,
38,
39,
40,
41,
42,
42,
43,
44,
45,
46,
46,
47,
48,
49,
50,
50,
51,
52,
53,
53,
54,
55,
56,
57,
57,
58,
59,
60,
60,
61,
62,
63,
64,
64,
65,
66,
67,
68,
68,
69,
70,
71,
72,
73,
73,
74,
75,
76,
77,
78,
79,
80,
81,
82,
83,
84,
85,
86,
87,
88,
89,
90,
91,
92,
93,
94,
96,
97,
98,
99,
101,
102,
104,
105,
107,
108,
110,
112,
114,
115,
117,
119,
122,
124,
127,
129,
132,
135,
139,
142,
146,
151,
156,
163,
170,
180,
192,
211,
247,
305

};

char i = 'n';
void setup() {
  Serial.begin(115200); //Needed for bluetooth connection
  
}

void loop() {
  int value = analogRead(A0); //Analog input
  float valueprime = value/7;
  int valueprime1 = round(valueprime);
  delay(10);
  float voltage = value * (5/1023.0); //To read voltage value For testing
  int temp_result = tempsval[valueprime1];

 
  //Sends data when needed
  //if(Serial.available()){
    //i = Serial.read();
  //}

  //Send data when y is received
  //if(i=='1'){  
    //Serial.println(voltage);
    Serial.print(temp_result);
    //Serial.println(value);
    //Serial.println(valueprime);
    //Serial.println(valueprime1);
    delay(500);
 //}  

}
