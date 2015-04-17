## Gracefully disconnecting ##

Built into the Swift server and client is a simple pre-registered message type, that tells the other party it is going to drop the connection. This is the so called 'Bye Bye' message and is used to gracefully disconnect from the other party.

This way we don't have to wait for a connection time-out or some exception to occur and the server will know everything is alright. In the case of a UDP server, this function is crucial to detect dropping clients as there is no physical connection between the server and its clients (also see [Ping Pong mode](AboutPingPong.md)).

This message is triggered when calling the _stop()_ method on either server or client:

```
SwiftSocketServer server = new SwiftSocketServer(4444);
SwiftSocketClient client = new SwiftSocketClient("localhost", 4444);
server.start();
client.start();

(..)

server.stop();
client.stop();
```

In the unlikely case that both the server and client will both go away at the same time, whichever is first to send the Bye Bye message, it simply will have closed the socket before receiving the Bye Bye message of the other party and whichever is the last to send may receive the Bye Bye message and cancel sending his own Bye Bye request. This makes sure that neither side will time-out after one of the parties said 'Bye Bye'.