## Registering message types ##

Swift Socket Server uses a concept called [binary packets](CommunicatingBinaryPackets.md). To communicate these messages, you need to write your own encoding/decoding logic (you are ofcourse free to delegate to Java's standard serialization to handle this). You do this by implementing either of two interfaces: [ClientMessageToServer](http://swift-socket-server.googlecode.com/svn/trunk/server/javadoc/users/org/codemonkey/swiftsocketserver/ClientMessageToServer.html) or [ServerMessageToClient](http://swift-socket-server.googlecode.com/svn/trunk/server/javadoc/users/org/codemonkey/swiftsocketserver/ServerMessageToClient.html).

The interface names imply directionality which is reflected by the interfaces: for the Swift server a `ClientMessageToServer` involves a _decode_ and _execute_ method and `ServerMessageToClient` involves an _encode_ message. For the Swift client it's vice versa.

### Define the message types ###

To implement a chat server-client, like the one in the [chat demo](http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/chat-server), both the server and client need to minimally provide two message types: one for receiving a chat message and one for sending a chat message.

Here are all four implementations:

Server side:

  * receiving chat message from client: [ClientToServerChatMessage.java](http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/chat-server/chat-server-java/src/swiftchat/ClientToServerChatMessage.java) <font color='grey'>(swiftsocketserver.ClientMessageToServer)</font>
  * sending chat message to client: [ServerToClientChatMessage.java](http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/chat-server/chat-server-java/src/swiftchat/ServerToClientChatMessage.java) <font color='grey'>(swiftsocketserver.ServerMessageToClient)</font>

Client side:

  * receiving chat message from server: [ServerToClientChatMessage.java](http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/chat-server/chat-client-java/src/swiftchat/ServerToClientChatMessage.java) <font color='grey'>(swiftsocketclient.ServerMessageToClient)</font>
  * sending chat message to server: [ClientToServerChatMessage.java](http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/chat-server/chat-client-java/src/swiftchat/ClientToServerChatMessage.java) <font color='grey'>(swiftsocketclient.ClientMessageToServer)</font>

In this scenario, the messages are so simple, we don't need to do anything special for encoding/decoding. The message content to be encoded/decoded is also the result.

### Register the message types ###

After defining the message type, they need to be registered with the server/client thereby coupling them to a unique id. This id is used to recognize which message type belongs to which incoming encoded message.

Server side:

```
SwiftSocketServer server = new SwiftSocketServer(4444);
server.registerClientMessageToServerType(1, ClientToServerChatMessage.class);
server.registerServerMessageToClientId(1, ServerToClientChatMessage.class);
server.start();
```

Client side:

```
String serverAddress = "localhost";
SwiftSocketClient client = new SwiftSocketClient(serverAddress, 4444);
client.registerClientMessageToServerType(1, ClientToServerChatMessage.class);
client.registerServerMessageToClientType(1, ServerToClientChatMessage.class);
client.start();
```

The next step is to [send messages](SendingMessages.md) or to [process incoming messages](ReceivingMessages.md).