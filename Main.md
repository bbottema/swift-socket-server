# Swift Socket Server #

Swift is a Java socket server that simplifies client-server communication by defining a simple simple framework in which to define your messages and how they are handled.

One aspect of this is the standardized form of marshalling: communicating messages in the form of [easily converted binary packets](CommunicatingBinaryPackets.md). In addition, the Swift server already contains some client management functionality, such as a '[Ping Pong](AboutPingPong.md)' mechanism that detects dropped or unresponsive clients, [client-server handshake](ClientServerHandshake.md), client-server [graceful disconnection](GracefulDisconnection.md) and [message queuing](ReceivingMessages.md). It supports both [TCP and UDP](TCPandUDP.md), without any of the API changing: under the hood UDP is managed to emulate TCP behavior in regards to server-client messaging and client synchronization.

There is a small extension available called '[World Server](WorldServer.md)' that simplifies integrating the Swift messaging server into your running simulation, or 'World' (like a game for example).

This project contains the Java server, [Java client](Main#Swift_Java_Client.md) and a [Flex/Flash client](AboutFlashClient.md) library (swc).

<font color='grey' size='1'>
<ul><li><a href='http://swift-socket-server.googlecode.com/svn/trunk/server/javadoc/users/index.html'>Server Javadoc</a>
</li><li><a href='http://swift-socket-server.googlecode.com/svn/trunk/client/java-client/javadoc/users/index.html'>Client Javadoc</a>
</font></li></ul>

## Setting up a server ##

  1. #### [Starting the server](StartingTheServer.md) ####
  1. #### [Register message types](RegisteringMessageTypes.md) ####
  1. #### [Send messages](SendingMessages.md) ####
  1. #### [Receive messages](ReceivingMessages.md) ####

## Swift Java Client ##

From the user perspective, the Swift client requires no documentation in addition to that of the server. The client API is simply exactly like the server except for the following features:

  * The [message interfaces](RegisteringMessageTypes.md) are reversed:
    * the ClientToServer message encodes instead of decodes and cant be executed by the client
    * the ServerToClient message decodes instead of encodes and can be executed by the client
  * The [World Client](WorldServer.md) has no update method, which only the server needs to progress the world 'simulation'
  * The client initiates a connection to the server
  * Only the server can send a _[Ping](AboutPingPong.md)_ message, while the client can only respond with a _Pong_ message

Besides those items, the client behaves like a server in that you register your messages the same way and execute them the same way. This is the whole point of this project: simplify how messages are communicated. Simply define and register them on both ends using the same code and the server and client can communicate these.

## Swift Flex Client ##

  * [About the Flex/Flash client library](AboutFlashClient.md)

## Automating encoding / decoding using Java reflection ##

Swift server and client contain some utility classes that handles the minimalistic encoding / decoding of [binary packets](CommunicatingBinaryPackets.md) using Java reflection. These are the `DatagramEncoder` and `DatagramDecoder`.

They have been extensively tested using junit and have been used successfully in an experimental game. However, the state of these classes is still experimental, or alpha rather.

These utilities will encode an object or field the same way you would to create a binary packet with the lengths and content encoded in the string. The `DatagramEncoder` and `DatagramDecoder` handle lists as well as enums. The objective is to fully automate the encoding/decoding while maintaining the minimalistic foot print of the binary packet concept.

## Required libraries ##

See: [NOTICE.txt](http://code.google.com/p/swift-socket-server/source/browse/trunk/NOTICE.txt)

Swift Socket Server uses the [Java reflection](http://code.google.com/p/java-reflection/) library to encode / decode message using Java reflection (see section above).

## Some factoids ##

**Maven dependency**

```
<dependency>
    <groupId>org.codemonkey.swiftsocketserver</groupId>
    <artifactId>swift-server</artifactId>
    <version>1.0</version>
</dependency>
```
```
<dependency>
    <groupId>org.codemonkey.swiftsocketserver</groupId>
    <artifactId>swift-client</artifactId>
    <version>1.0</version>
</dependency>
```
**Project statistics**

&lt;wiki:gadget url="http://www.ohloh.net/p/585735/widgets/project\_factoids.xml" border="0" width="400"/&gt;
&lt;wiki:gadget url="http://www.ohloh.net/p/585735/widgets/project\_languages.xml" border="0" width="400" height="200"/&gt;