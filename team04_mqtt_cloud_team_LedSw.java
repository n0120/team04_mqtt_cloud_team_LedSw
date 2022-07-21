#include <WiFi.h>
#include "Ambient.h"
#include <PubSubClient.h>

// Update these with values suitable for your network.

//const char* ssid = "........";
//const char* password = "........";
const char* ssid = "GL-AR300M-054";
const char* password = "goodlife";
const char* mqtt_server = "osakahitech-group3.cloud.shiftr.io";

const int ledPin = 4; //赤色LED
const int btnPin = 17; // GPIO IO17pin
const int swPin = 17; //const...初期化した後代入されない
int count = 0; //カウンタ変数
int num = 0;
int flag = 0; //フラグ初期値 0:押されていない状態

WiFiClient espClient;
PubSubClient client(espClient);
unsigned long lastMsg = 0;
#define MSG_BUFFER_SIZE  (50)
char msg[MSG_BUFFER_SIZE];
int value = 0;

void setup_wifi() {

  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();

  // Switch on the LED if an 1 was received as first character
  if ((char)payload[0] == 'y') {
    if ((char)payload[1] == '1') {
      digitalWrite(ledPin, HIGH);   // Turn the LED on (Note that LOW is the voltage level
      num = 1;
      Serial.println(num);
      // but actually the LED is on; this is because
      // it is active low on the ESP-01)
    } else {
      digitalWrite(ledPin, LOW);  // Turn the LED off by making the voltage HIGH
      num = 0;
      Serial.println(num);
    }
  }

}

void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Create a random client ID

    /* 以下に自分のIDを入力する */
    String clientId = "2021-20";
    /* 以上に自分のIDを入力する */

    clientId += String(random(0xffff), HEX);
    // Attempt to connect
    if (client.connect(clientId.c_str(), "osakahitech-group3", "tUXBCD8RHObKtxEm")) {
      Serial.println("connected");
      // Once connected, publish an announcement...
      //      client.publish("outTopic", "hello world");

      client.publish("301/group-3", msg);
      Serial.println(num);

      // ... and resubscribe
      //      client.subscribe("inTopic");
      client.subscribe("301/#");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void setup() {
  pinMode(ledPin, OUTPUT);     // Initialize the BUILTIN_LED pin as an output
  pinMode(swPin, INPUT); // Switch デジタル入力
  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
}

void loop() {

  int b = digitalRead(swPin);

  if (b == 1 && flag == 0) {
    flag = 1;
    count++;
    if (num < 1) {
      num++;
      Serial.println(num);
      snprintf (msg, MSG_BUFFER_SIZE, "y1", value); // 文字の一列目が個別のIDとして使われる。この場合は「y」
      client.publish("301/group-3", msg);
    } else {
      num = 0;
      Serial.println(num);
      snprintf (msg, MSG_BUFFER_SIZE, "y0", value); // 文字の一列目が個別のIDとして使われる。この場合は「y」
      client.publish("301/group-3", msg);
    }
  } else if (b == 0) {
    flag = 0;
  }
  delay(10);

  if (!client.connected()) {
    reconnect();
  }
  client.loop();
  //
  //  unsigned long now = millis();
  //  if (now - lastMsg > 2000) {
  //    lastMsg = now;
  //    ++value;
  //    //    snprintf (msg, MSG_BUFFER_SIZE, "hello world #%ld", value);
  //    //    snprintf (msg, MSG_BUFFER_SIZE, "yAmAshitA #%ld", value);
  //    if (num == 1) {
  //      snprintf (msg, MSG_BUFFER_SIZE, "y1", value); // 文字の一列目が個別のIDとして使われる。この場合は「y」
  //      Serial.println(msg);
  //    }
  //    if (num == 0) {
  //      snprintf (msg, MSG_BUFFER_SIZE, "y0", value); // 文字の一列目が個別のIDとして使われる。この場合は「y」
  //      Serial.println(msg);
  //    }
  //    //    snprintf (msg, MSG_BUFFER_SIZE, "1", value);
  //    Serial.print("Publish message: ");
  //    Serial.println(msg);
  //    //    client.publish("outTopic", msg);
  //    client.publish("301/group-3", msg);

  //  }
}