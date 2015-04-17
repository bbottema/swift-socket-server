## Receiving messages ##

**Here's a quick overview of the lifecycle of a message:**

When a Swift server or client receives a message, it will look the first three characters for the code, matches this code to a message type [you registered](RegisteringMessageTypes.md), then Swift will instantiate this type and call `decode(message)` on it. If successful, we now have a fully executable message ready to be processed. Swift will internally store this message in a queue until you ask for it.

<font color='grey' size='1'>sidenote: It is important to understand the concept of the 'executable message': Once a message object has been created and it decoded the TCP/UDP message content, it becomes <i>executable</i> (see <a href='http://en.wikipedia.org/wiki/Command_pattern'>Command Pattern</a> on Wikipedia). This literally means Swift can invoke <code>message.execute(context)</code>, where the context is very important as it functions as the controller through which the message exerts its influence on the server state / client representation.</font>

**At this point there are three ways to retrieve the message from the Swift server or client.**
  1. You can preregister a 'context' object against which Swift will automatically execute all messages of a specific type against
  1. You can fetch the messages from swift and execute them yourself
  1. You can use the [World Client](WorldServer.md) to handle the messages in a combination of the above two options: the messages are fetched by the world server and automatically executed against the World Context you defined. The difference with option 1 is that the messages are manually executed in a separate thread, like in option 2 and allows the server to run its course without having to wait for messages to be done executing.

**Following are examples of all three approaches:** (all work for both server and client)

1). pre-register an execution context so Swift will execute all messages of a specific type automatically.

```
SwiftSocketClient client = new SwiftSocketClient("localhost", 4444);
client.registerExecutionContext(ServerToClientChatMessage.class, this);
client.start();
```

2). actively keep polling the server for messages

```
server = new SwiftSocketServer(4444);
server.start();
		
while (server.isRunning()) {
	ServerUtil.defaultSleep(); // we don't need to use 100% cpu (performs a Thread.sleep())
	if (server.hasClientMessages()) {
		ClientMessageToServer request = server.getNextClientMessage();
		try {
			request.execute(this);
		} catch (Exception e) {
			// error, but catch to prevent server from crashing
			System.err.println(String.format("error executing request '%s' - %s", request.getClass(), e.getMessage()));
			e.printStackTrace();
		}
	}
}
```

3). use the World Server to process messages for us

```
WorldServer<MyWorldContext> server = new WorldServer<MyWorldContext>(4444, this, INITIAL_FPS);
server.start();
// here 'this' is an instance of MyWorldContext and functions as execution context
```