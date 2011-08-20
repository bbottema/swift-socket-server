package org.codemonkey.swiftsocketserver;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * Defines the type of server, {@link #UDP} or {@link #TCP}. Determines which {@link ServerEndpoint} is being used. The server behaves
 * exactly the same in both modes, except the message transport types are being switched (without breaking API). In UDP mode, TCP mode is
 * being emulated (see {@link ServerEndpoint} for more info on this).
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public enum ServerType {
	/**
	 * Creates a {@link ServerEndpointUDP} with an embedded {@link DatagramSocket}.
	 * 
	 * @see ServerEndpointUDP
	 * @see DatagramSocket#DatagramSocket(int)
	 */
	UDP {
		/**
		 * @see ServerType#UDP
		 */
		@Override
		ServerEndpoint createServerEndpoint(final int port)
				throws SocketException {
			return new ServerEndpointUDP(new DatagramSocket(port));
		}
	},
	/**
	 * Creates a {@link ServerEndpointTCP} with an embedded {@link ServerSocket}.
	 * 
	 * @see ServerEndpointTCP
	 * @see ServerSocket#ServerSocket(int)
	 */
	TCP {
		/**
		 * @see ServerType#TCP
		 */
		@Override
		ServerEndpoint createServerEndpoint(final int port)
				throws IOException {
			return new ServerEndpointTCP(new ServerSocket(port));
		}
	};

	/**
	 * Creates a server endpoint embedding a transport type specific socket (UDP or TCP).
	 * 
	 * @param port The port on which the server endpoint socket should list.
	 * @return A UDP or TCP {@link ServerEndpoint}.
	 * @throws SocketException Thrown when creating an UDP {@link DatagramSocket} went wrong.
	 * @throws IOException Thrown when creating a TCP {@link ServerSocket} went wrong.
	 */
	abstract ServerEndpoint createServerEndpoint(int port)
			throws SocketException, IOException;
}
