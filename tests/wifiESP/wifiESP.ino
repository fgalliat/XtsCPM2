#include "connect.h"


// #include "xts_string.h"

#define dbug Serial.println

#ifndef HEADERS
 #define HEADERS "Authorization: Bearer eyJhbGciOi"
#endif

char* wifi_getHomeServer() {
    #ifdef HOME_SERVER
    return (char*)HOME_SERVER;
    #else
    return (char*)"myserver";
    #endif
}

char* __WIFI_GET_PSK(char* ssid) {
    #ifdef PSK
    return (char*)PSK;
    #else
    return (char*)"MyPSK";
    #endif
}

char* __WIFI_GET_KNWON_SSIDS() {
    #ifdef _SSID
    return (char*)_SSID;
    #else
    return (char*)"MyBox";
    #endif
}

int _kbhit() { return Serial.available(); }
uint8_t _getch() {
    while (_kbhit() == 0)
    {
        delay(5);
    }
    return (uint8_t)Serial.read();
}
uint8_t _getche() {
    while (_kbhit() == 0)
    {
        delay(5);
    }
    int c = Serial.read();
    Serial.write( (char)c );
    return (uint8_t)c;
}

// wifi for esp by serial
#include "WiFiEsp.h"


char ssid[] = _SSID;            // your network SSID (name)
char pass[] = PSK;        // your network password
int status = WL_IDLE_STATUS;     // the Wifi radio's status

char server[] = "arduino.cc";
int port = 80;
// char server[] = HOME_SERVER;
// int port = 8666;

// Initialize the Ethernet client object
WiFiEspClient client;

#define WIFI_SERIAL Serial5
#define WIFI_SERIAL_BDS 115200

void setup()
{
  // initialize serial for debugging
  Serial.begin(115200);
  // initialize serial for ESP module
  WIFI_SERIAL.begin(WIFI_SERIAL_BDS);
  // initialize ESP module
  WiFi.init(&WIFI_SERIAL);

  // check for the presence of the shield
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    // don't continue
    while (true);
  }

  // attempt to connect to WiFi network
  while ( status != WL_CONNECTED) {
    Serial.print("Attempting to connect to WPA SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network
    status = WiFi.begin(ssid, pass);
  }

  // you're connected now, so print out the data
  Serial.println("You're connected to the network");
  
  printWifiStatus();

  Serial.println();
  Serial.println("Starting connection to server...");
  // if you get a connection, report back via serial
  if (client.connect(server, port)) {
    Serial.println("Connected to server");
    // Make a HTTP request
    client.println("GET /asciilogo.txt HTTP/1.1");

    // char* api = (char*)"/sensors/sensor/1";
    // client.print("GET ");
    // client.print(api);
    // client.println(" HTTP/1.1");

    client.print("Host: ");
    client.println(server);

    // client.println(HEADERS);

    client.println("Connection: close");
    client.println();

delay(1000);

  int tlen; char buff[1024+1]; 
  while ( (tlen = client.available() ) > 0) {
      memset( buff, 0x00, 1024+1 );
      if ( tlen > 1024 ) { tlen = 1024; }
    int cpt = client.readBytes(buff, tlen);
    Serial.print( buff );
  }

  if (!client.connected()) {
    Serial.println();
    Serial.println("Disconnecting from server...");
    client.stop();

    // do nothing forevermore
    while (true);
  }


  }
}

void loop()
{

delay(2000);

//   // if there are incoming bytes available
//   // from the server, read them and print them
//   while (client.available()) {
//     char c = client.read();
//     Serial.write(c);
//   }

//   // if the server's disconnected, stop the client
//   if (!client.connected()) {
//     Serial.println();
//     Serial.println("Disconnecting from server...");
//     client.stop();

//     // do nothing forevermore
//     while (true);
//   }
}


void printWifiStatus()
{
  // print the SSID of the network you're attached to
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength
  long rssi = WiFi.RSSI();
  Serial.print("Signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}

// void loop() {
//     // Serial.println( "loop" );
//     char* api = (char*)"'http://192.168.1.134:8666/sensors/sensor/1'";
//     char* ignored = wifi_wget((char*)"$home", 8666, api, (char*)HEADERS);
//     Serial.println( ignored );
// Serial.println( "-- EOF --" );
//     while(true) delay(20000);
// }