## About the Ping Pong mode ##

Swift socket server has a mechanism built in that invalidates clients based on response time-outs, called 'Ping Pong mode'.

For UDP this is very important as you cannot detect connection problems to a client, since physically there is no connection, only incidental messages (see [datagram packets](http://download.oracle.com/javase/tutorial/networking/datagrams/index.html) at Oracle). Concerning detecting disconnecting clients, see [Graceful Disconnection](GracefulDisconnection.md).

For TCP and indeed in general the ping pong mechanism has some benefits as well: it can be used to detect lag to a client, frozen connections or crashed clients that didn't close the connection.

If turned on, the server will by default use an interval of 2500ms with a response timeout of 5000ms. You can easily override this however:

```
SwiftSocketServer server = new SwiftSocketServer(port);
server.setPingPongMode(true);
// or:
server.setPingPongMode(true, 10000, 5000); // check every 10 seconds, give 5 seconds to respond
server.start();
```