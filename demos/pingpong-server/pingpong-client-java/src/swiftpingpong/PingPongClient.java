package swiftpingpong;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codemonkey.swiftsocketclient.ClientType;
import org.codemonkey.swiftsocketclient.SwiftSocketClient;

/**
 * This simple PingPong client demo demonstrates how the built in PingPong mechanism responds to server Ping requests.
 * <p>
 * This can be useful if you need to drop clients that have frozen up or when round-trip delays become larger than a set time-out. This is
 * different from a client that has closed the socket, which is detected immediately.
 * <p>
 * The client needs to support sending back Pong answers to Ping requests from the server. This concerns an empty message with the code 999
 * (so the client receives strings with "999" and sends "999" back).
 * 
 * @author Benny Bottema
 */
public class PingPongClient {

	@SuppressWarnings("javadoc")
	public static void main(final String[] args) {
		System.out.println("---------------------------");
		System.out.println("- Starting PingPongClient -");
		System.out.println("---------------------------");

		// make sure we'll get some logging visible for this demo
		Logger.getLogger("org.codemonkey.swiftsocketclient.ClientMessageToServerPingPong").setLevel(Level.DEBUG);
		Logger.getLogger("org.codemonkey.swiftsocketclient.ServerMessageToClientPingPong").setLevel(Level.DEBUG);

		SwiftSocketClient server = new SwiftSocketClient("localhost", 4444, ClientType.TCP);
		server.start();
	}
}