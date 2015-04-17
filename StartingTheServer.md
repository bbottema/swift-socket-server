## Starting the server ##

This section explains the default behavior and optional settings of the Swift server.

First an example of how start a new server:

```
SwiftSocketServer server = new SwiftSocketServer(port);
server.start();
```

This server now runs in TCP mode and without using the [Ping Pong](AboutPingPong.md) mode. If we wish to change both settings, here is how:

```
SwiftSocketServer server = new SwiftSocketServer(port, ServerType.UDP);
server.setPingPongMode(true);
server.start();
```

These are all the settings of the Swift Socket Server (but you can tweak the Ping Pong settings). The [Swift World](WorldServer.md) server has some more.

The next step is to [register message types](RegisteringMessageTypes.md) so that the server recognizes these incoming and enqueue them for processing. After that we need to start [processing those messages](ReceivingMessages.md).