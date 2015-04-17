# Swift Socket Server #

Swift is a Java socket server that simplifies client-server communication by defining a simple framework in which to define your messages and how they are handled.

One aspect of this is the standardized form of marshalling: communicating messages in the form of [easily converted binary packets](CommunicatingBinaryPackets.md). In addition, the Swift server already contains some client management functionality, such as a '[Ping Pong](AboutPingPong.md)' mechanism that detects dropped or unresponsive clients, [client-server handshake](ClientServerHandshake.md), client-server [graceful disconnection](GracefulDisconnection.md) and [message queuing](ReceivingMessages.md). It supports both [TCP and UDP](TCPandUDP.md), without any of the API changing: under the hood UDP is managed to emulate TCP behavior in regards to server-client messaging and client synchronization.

There is a small extension available called '[World Server](WorldServer.md)' that simplifies integrating the Swift messaging server into your running simulation, or 'World' (like a game for example).

This project contains the Java server, [Java client](Main#Swift_Java_Client.md) and a [Flex/Flash client](AboutFlashClient.md) library (swc).

### [Setting up a Swift server](Main.md) ###