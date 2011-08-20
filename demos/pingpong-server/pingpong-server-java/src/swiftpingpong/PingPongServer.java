package swiftpingpong;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codemonkey.swiftsocketserver.ServerType;
import org.codemonkey.swiftsocketserver.SwiftSocketServer;

/**
 * This simple PingPong server demo demonstrates how the built in PingPong polling mechanism keeps track of clients timing out.
 * <p>
 * This can be useful if you need to drop clients that have frozen up or when round-trip delays become larger than a set time-out. This is
 * different from a client that has closed the socket, which is detected immediately.
 * <p>
 * The client needs to support sending back Pong answers to Ping requests from the server. This concerns an empty message with the code 999
 * (so the client receives strings with "999" and sends "999" back).
 * 
 * @author Benny Bottema
 */
public class PingPongServer {

	@SuppressWarnings("javadoc")
	public static void main(final String[] args) {
		System.out.println("---------------------------");
		System.out.println("- Starting PingPongServer -");
		System.out.println("---------------------------");

		// make sure we'll get some logging visible for this demo
		Logger.getLogger("org.codemonkey.swiftsocketserver.ClientMessageToServerPingPong").setLevel(Level.DEBUG);
		Logger.getLogger("org.codemonkey.swiftsocketserver.ServerMessageToClientPingPong").setLevel(Level.DEBUG);

		SwiftSocketServer server = new SwiftSocketServer(4444, ServerType.TCP);
		server.setPingPongMode(true);
		server.start();
	}
}