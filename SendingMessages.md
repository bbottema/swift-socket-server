## Sending messages ##

Assuming you have your [message types](RegisteringMessageTypes.md) set up, you can send a message in three ways.

  1. You can send a message to a specific client. This could be a response to a message through which you've obtained the [ClientContext](http://swift-socket-server.googlecode.com/svn/trunk/server/javadoc/users/org/codemonkey/swiftsocketserver/ClientContext.html).
  1. You can broadcast a message to all clients
  1. You can manually loop through all known client contexts

**Following are examples of all three approaches:**

1). Send a message to a specific client:

```
// called by a ClientToServerFooMessage
public void sendBarUpdateToClient(ClientContext clientContext) {
	swiftserver.sendMessage(new ServerMessageToClientBar(clientContext, theUpdate));
	// or:
	worldServer.sendMessage(new ServerMessageToClientBar(clientContext, theUpdate));
}
```

2). Broadcast a message to all clients:

  * <font color='grey' size='1'>From the <a href='http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/clock-worldserver/clock-worldserver-server-java#clock-worldserver-server-java%2Fsrc%2Fclockworldserver'>Clock world server demo</a></font>

```
server.broadcastMessage(new ServerMessageToClientTimeUpdate(null, timeRepresentation));
```

3). Manually loop through all known client contexts:

  * <font color='grey' size='1'>From the <a href='http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/chat-server/chat-server-java/src/swiftchat'>Chat server demo</a></font>

```
public void updateDiscussion(final ClientContext fromClient, final String message) {
		for (ClientContext userContext : server.getAllClientContexts()) {
			// isActive() is optional
			if (userContext.isActive()) {
				server.sendMessage(new ServerToClientChatMessage(userContext, message));
			}
		}
	}
```