#include <Wire.h>

#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

void setup();
void loop();

const int ledPin =  13;      // the number of the LED pin

void setup()
{
    // set the digital pin as output:
    pinMode(ledPin, OUTPUT);  
  
	acc.powerOn();
}

void loop()
{
	byte msg[3];

	if (acc.isConnected()) {
		int len = acc.read(msg, sizeof(msg), 1);

		if (len > 0) {
                    if (msg[0] == 0x3) {
						analogWrite(ledPin, msg[2] ? 255 : 0);
                    }
		}
	} 

	delay(10);
}