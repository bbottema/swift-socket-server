<font color='grey' size='1'>
<ul><li>demo application 1: Ping Pong Flex Client (<a href='http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/pingpong-server/pingpong-client-flex#pingpong-client-flex%2Fsrc%2Forg%2Fcodemonkey'>/trunk/demos/pingpong-server/pingpong-client-flex</a>)<br>
</li><li>demo application 2: Chat Flex Client (<a href='http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/chat-server/chat-client-flex#chat-client-flex%2Fsrc%2Forg%2Fcodemonkey'>/trunk/demos/chat-server/chat-client-flex</a>)<br>
</font></li></ul>

## About the Flash client ##

In addition to the Java client, there is a Flash/Flex client for the Swift server as well. It contains the basics of the Java client and supports all standard Swift functions.

It is distributed in the form of a [swc](http://stackoverflow.com/questions/1340866/what-is-a-flash-swc-file-and-how-is-it-used) file.

### Usage: ###

For more elaborate examples, check out the demo applications listed at the top of this page.

<font color='grey' size='1'>Note that while the java client and server had some naming conventions applied to them, the Flex client library still uses the old terms. This can be particularly confusing when determining message direction: old terms are 'requests' and 'responses' but in which direction isn't clear. The new terms are 'ClientToServerMessage' and 'ServerToClientMessage'.</font>

**To start a Flash client:**

```
var client:RequestSocketClient = new RequestSocketClient(serverHost, 4444);
client.registerRequest(1, ClientToServerChatMessage);
client.registerResponse(1, ServerToClientChatMessage);
client.start();
```

**To process messages**

```
stage.addEventListener(Event.ENTER_FRAME, serverResponseHandler);

/**
 * Continuously checks if there is a server messages to execute.
 */
private function serverResponseHandler(e:Event):void {
	if (client.hasServerResponses()) {
		client.getNextServerResponse().execute(this);
	}
}
```