## TCP and UDP support ##

Swift supports both UDP and TCP server modes.

A unique feature in Swift is the fact that it emulates TCP when running UDP: the messages are sent using UDP, but the server-client connections and messages are handled by the server and client the same way as with TCP messages. This means that both UDP and TCP messages work exactly the same way from the user perspective, so the user won't notice any difference in dealing with the messages and client-server interaction.

The Swift server uses a couple of mechanisms for this.

  * In UDP mode Swift performs a client to server [handshake](ClientServerHandshake.md) so the server knows about its client and the client knows the server knows, ergo a _virtual_ connection has been established.
  * A [Ping Pong](AboutPingPong.md) mechanism can optionally be used to detect client time-outs. For UDP this is crucial, since unlike with TCP you cannot know when a client goes away or the connection drops otherwise.

```
// server
SwiftSocketServer server = new SwiftSocketServer(4444, ServerType.TCP);
SwiftSocketServer server = new SwiftSocketServer(4444, ServerType.UDP);

// client
SwiftSocketClient client = new SwiftSocketClient(serverHost, 4444, ClientType.TCP);
SwiftSocketClient client = new SwiftSocketClient(serverHost, 4444, ClientType.UDP);
```