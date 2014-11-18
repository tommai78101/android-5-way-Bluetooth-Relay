![Imgur](http://i.imgur.com/aZYUgtK.png)

###android-5-way-Bluetooth-Relay

===

#### Abstract

This app does Bluetooth communications up to 5 devices, logically. When all devices (5 at most) are connected, they can initiate the chat relay and send messages to each other, just like Messenger. Can only send text messages, does not support anything else.

===

#### Background

In order to continue using a smartphone for personal use, one must deliver a research project that delves in communications technology assigned by my professor, Professor Reen-Cheng Wang. Since I had lost my own smartphone (HTC Wildfire S) on my way home, I used this opportunity to obtain a HTC Evo 3D (a school property). 

Professor Wang assigned me to think up a good project for researching on the basis of communications so that he can provide a good reason to lend that HTC Evo 3D to me (a student) on the grounds of educational purposes. I decided to create a Bluetooth app just to extend the leasing of a borrowed HTC Evo 3D from my school, hence, the birth of this project.

For the entire Winter Break, I studied the Bluetooth protocols, how it works, and many online searches for relevant codes. Thanks to Stack Overflow, most of the project is done in a breeze. Except for the UUID part, which is the most important key for a Bluetooth handshake.

The UUID must be unique, and that all devices must use the same UUID to connect to the Bluetooth sockets opened by the devices. I learned this the hard way, without even realizing that randomly generated UUID cannot be used for every future Bluetooth sessions across multple clients/servers, unless they are all generated to be the same.

===

#### Description

| Button Name | What does it do? |
|:---:|:---:|
| Start Server | Starts up a BroadcastReceiver that accepts incoming connections. |
| Start Client | Starts up a BroadcastReceiver that connects to any devices accepting outgoing connections. |
| Cancel | Cancels all scanning, broadcasting, and receiving actions. |
| Send Output | Sends a dummy packet to see if the connections have completed their handshakes and is receiving/sending packets normally. |
| Scan for Devices | Scans for all nearby Bluetooth devices that allows them to be seen. Targeted devices should be Android-based. |
| Clear Logs | Cleans up the log messages that appears on the screen. |

#### Instructions

Before use, you require at least 2 Android smartphones, and at most 5 Android smartphones. Each device must have this application installed.

To start, one of the devices must initiate the server thread by pressing the **Start Server** button. Other devices should then initiate client threads by pressing the **Start Client** on each devices. Any one of these actions will start scanning for nearby Bluetooth devices. Please give a moment for the devices to discover other devices, and once they are discovered, the application will automatically initiate the handshake for acknowledgement.

When the connections have been connected, you will see the device name in a Toast message, and from there, you can start chatting.

All chat messages should be seen by all devices that are connected to the server device. 

#### Known Issues

Due to insufficient number of test devices, as well as being restricted to Android 4.4 KitKat, I cannot fix bugs or issues in such state. If anyone is willing to contribute, extend, or donate old Android devices to me, it is very appreciated.