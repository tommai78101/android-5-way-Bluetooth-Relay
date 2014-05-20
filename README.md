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
| Start Server | Starts up a BroadcastReceiver. |
| Start Client | Starts up a 