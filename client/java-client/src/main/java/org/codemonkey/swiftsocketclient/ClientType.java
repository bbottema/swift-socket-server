package org.codemonkey.swiftsocketclient;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Defines the type of client, {@link #UDP} or {@link #TCP}. Determines which {@link ClientEndpoint} is being used. The client behaves
 * exactly the same in both modes, except the message transport types are being switched (without breaking API). In UDP mode, TCP mode is
 * being emulated (see {@link ClientEndpoint} for more info on this).
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public enum ClientType {
	/**
	 * Creates a {@link ClientEndpointUDP} with an embedded {@link DatagramSocket}.
	 * 
	 * @see ClientEndpointUDP
	 * @see DatagramSocket#DatagramSocket()
	 */
	UDP {
		/**
		 * @see ClientType#UDP
		 */
		@Override
		ClientEndpoint createClientEndpoint(final String host, final int port)
				throws SocketException {
			return new ClientEndpointUDP(new DatagramSocket(), host, port);
		}
	},
	/**
	 * Creates a {@link ClientEndpointTCP} with an embedded {@link Socket}.
	 * 
	 * @see ClientEndpointTCP
	 * @see Socket#Socket(String, int)
	 */
	TCP {

		/**
		 * @see ClientType#TCP
		 */
		@Override
		ClientEndpoint createClientEndpoint(final String host, final int port)
				throws IOException {
			return new ClientEndpointTCP(new Socket(host, port));
		}
	};

	/**
	 * Creates a transport type specific {@link ClientEndpoint}, either TCP or UDP.
	 */
	abstract ClientEndpoint createClientEndpoint(String host, int port)
			throws SocketException, IOException;
}