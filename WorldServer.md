<font color='grey' size='1'>demo application: Clock World Server (<a href='http://code.google.com/p/swift-socket-server/source/browse/trunk/demos/clock-worldserver/'>/trunk/demos/clock-worldserver</a>)</font>

## About the World Server ##

The World Server package is a tiny extension of the Swift Socket Server package. It too has both a client and server. It adds a small layer around the Socket server that standardizes how you can run a world simulation, like a game for example.

You can also use a Swift client with a World server or a Swift server and a World client. They are compatible, because the World extension only acts as a decoration to automate some tasks.

**The heartbeat loop**

The World Server adds a heart beat loop (or game loop) that keeps updating your 'context' on a regular interval, which keeps the world running. Within this heart beat the world server also polls the server for [messages](ReceivingMessages.md) and executes these automatically.

The difference between the World Server and World Client is that the client doesn't update the context in a loop (only when receiving requests from the server). It does process messages automatically.

**About the FPS in the heartbeat loop**

When creating a World server, you define the frame per seconds that it should process, or rather: the number of times per second it should call _update()_ on your world context object.

On each update, the World server will provide the current FPS scaling factor, which is simply 1 / FPS or: the number of seconds per frame. You can include this factor in your calculations as a delta modifier meaning how the higher the FPS the smaller the changes and the lower the FPS the bigger the changes. This way you can provide a consistent simulation progression while changing the FPS.

**Using only the World Client to execute messages**

As described in [Receiving Messages](ReceivingMessages.md), you can use the World Client for handling messages for you. This also works if you are just running a standard Swift server.

## Usage: ##

Using the World server is straightforward. Create your 'context' class by implementing [World Context](http://swift-socket-server.googlecode.com/svn/trunk/server/javadoc/users/org/codemonkey/swiftworldserver/WorldContext.html), create a World Server instance and pass in this context. The rest is the same as a standard Swift server.

**Define your World context:**

For a clean interface to be used by your messages, for this example we'll define a separate interface.

```
interface MyWorldContext extends WorldContext {
	void calledFromMessageA();
	void performActionB();
	void shootMissle(Ship ship, Ship target);
}
```

**Implement your World context:**

```
public class MyWorld implements MyWorldContext {

	/*
	 * from WorldContext
	 */

	@Override
	public void initWorld() {
		// initialize your world simulation (game)
	}

	@Override
	public void updateWorld(double deltaFactor) {
		// perform whatever update on your server optionally using the delta factor
	}

	/*
	 * from MyWorldContext
	 */

	public void calledFromMessageA() {
		// do something as requested by messageA
	};

	public void performActionB() {
		// change the world state some way
	};

	public void shootMissle(Ship ship, Ship target) {
		// initiate missle launch for ship towards target
	};
}
```

**Start the server:**

```
WorldServer<MyWorldContext> server = new WorldServer<MyWorldContext>(4444, myWorld, 4);
server.start();

// or, for a client

WorldClient<MyWorldContext> client = new WorldClient<MyWorldContext>(serverHost, 4444, myWorld);
client.start();
```