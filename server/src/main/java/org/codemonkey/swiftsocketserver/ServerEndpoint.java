package org.codemonkey.swiftsocketserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Abstraction of the server socket. Acts as a wrapper for {@link ServerSocket} and {@link DatagramSocket} to provide uniform behavior.
 * 
 * @author Benny Bottema
 * @see ServerEndpointTCP
 * @see ServerEndpointUDP
 * @since 1.0
 */
abstract class ServerEndpoint {

	/**
	 * All subclasses use the logger anchored to <code>ServerEndpoint.class</code>.
	 */
	protected static final Logger LOGGER = Logger.getLogger(ServerEndpoint.class);

	/**
	 * @return An endpoint that acts similarly for TCP and UDP clients.
	 * @throws IOException Thrown by {@link ServerEndpointTCP#getClientEndpoint()}
	 */
	abstract ClientEndpoint getClientEndpoint()
			throws IOException;

	/**
	 * @return Whether an explicitly call has been made to {@link ServerEndpoint#close()} or close on the passed in socket (both TCP and
	 *         UDP).
	 */
	abstract boolean isClosed();

	/**
	 * Call the close method on the embedded socket (works for both TCP and UDP).
	 * 
	 * @throws IOException Thrown by {@link ServerEndpointTCP#close()}
	 */
	abstract void close()
			throws IOException;
}

/**
 * Wrapper that delegates all calls directly to the decorated {@link ServerSocket}. The Server Endpoint abstraction mimics how TCP deals
 * with clients and so the TCP server endpoint variant adds no extra functionality, except that it returns client endpoints instead of
 * {@link Socket} instances (see {@link #getClientEndpoint()}.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
class ServerEndpointTCP extends ServerEndpoint {

	private final ServerSocket serverSocket;

	/**
	 * Constructor; simply stores a reference to the server socket.
	 * 
	 * @param serverSocket The {@link ServerSocket} on which we connect new clients.
	 */
	public ServerEndpointTCP(final ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	/**
	 * Returns a new {@link ClientEndpointTCP} {@link Socket} decorator (received from {@link ServerSocket#accept()}).
	 */
	@Override
	ClientEndpoint getClientEndpoint()
			throws IOException {
		return new ClientEndpointTCP(serverSocket.accept());
	}

	/**
	 * @return {@link ServerSocket#isClosed()}
	 */
	@Override
	boolean isClosed() {
		return serverSocket.isClosed();
	}

	/**
	 * Calls {@link ServerSocket#close()}
	 */
	@Override
	void close()
			throws IOException {
		serverSocket.close();
	}
}

/**
 * Wrapper that adds TCP like client handling to the UDP {@link DatagramSocket}.
 * 
 * @author Benny Bottema
 * @see #ServerEndpointUDP
 * @see #getClientEndpoint
 * @see DatagramPacketReceiver
 * @since 1.0
 */
class ServerEndpointUDP extends ServerEndpoint {

	/**
	 * This should probably be tuned at some point to allow for larger datagram packets.
	 */
	private static final int PACKET_BUFFER_SIZE = 1024;

	/**
	 * The server socket we're accepting client messages on in the form of UDP datagram packets.
	 */
	private final DatagramSocket datagramSocket;

	/**
	 * The list of known clients, based on the {@link SocketAddress} of previously received {@link DatagramPacket} objects.
	 */
	private final Map<SocketAddress, ClientEndpointUDP> knownClients;

	/**
	 * The list of new clients ready to be returned when asked for a new client endpoint.
	 */
	private final List<ClientEndpointUDP> newClients;

	/**
	 * Constructor; starts a new <code>Thread</code> listening for new datagram packets. If a new client sends a message, it will be
	 * returned on {@link #getClientEndpoint()}. In either case the datagram is passed on to the client endpoint being enqueued.
	 * 
	 * @param datagramSocket A DatagramSocket to listen to UDP messages.
	 */
	public ServerEndpointUDP(final DatagramSocket datagramSocket) {
		this.datagramSocket = datagramSocket;
		knownClients = new HashMap<SocketAddress, ClientEndpointUDP>();
		newClients = new ArrayList<ClientEndpointUDP>();
		new Thread(new DatagramPacketReceiver()).start();
	}

	/**
	 * Returns a new client endpoint in case a datagram packet is received from an unknown client. Otherwise, nothing is returning and this
	 * method remains blocking (like {@link ServerSocket#accept()}) until a new client sends a datagram packet.
	 * <p>
	 * TCP messages received are inherently coupled to a (client) <code>Socket</code>. To mimic this behavior {@link ServerEndpointUDP}
	 * keeps track of known clients (by having received datapackets from them before) and when receiving new datagram packets, we'll try to
	 * match the sender address to a known client. Once matched, we let the associated client endpoint deal with the datagram packet, as a
	 * TCP Socket would have done. If the datagram packet could not be matched to a known client, a new client is trying to communicate with
	 * us, and so we can return a new <code>ClientEndpoint</code>.
	 */
	@Override
	ClientEndpoint getClientEndpoint() {
		while (newClients.isEmpty() && !datagramSocket.isClosed()) {
			ServerUtil.defaultSleep();
		}
		if (newClients.size() > 0) {
			return newClients.remove(0);
		} else {
			// socket.close() will be called when the SwiftSocketServer is stopping,
			// returning null won't cause NullPointerException in that case
			return null;
		}
	}

	/**
	 * Receives {@link DatagramPacket} objects and delegates these to an {@link ClientEndpoint} to emulate TCP {@link Socket} behavior where
	 * a <code>Socket</code> per client receives messages from a connected client.
	 * <p>
	 * By abstracting this behavior in 'client endpoints', we can now for both TCP Socket messages and datagram packets act as though the
	 * endpoint received it directly from a client and so have consistent and uniform behavior for handling TCP and UDP messages. The only
	 * downside to this is that we cannot detect a client dropping the connection (so PingPong mode is advised).
	 * 
	 * @author Benny Bottema
	 * @since 1.0
	 */
	private class DatagramPacketReceiver implements Runnable {
		@Override
		public void run() {
			// a DatagramSocket will only close if the server is stopping gracefully or a client time-out was detected using PingPong mode
			while (!datagramSocket.isClosed()) {
				final DatagramPacket datagramPacket = new DatagramPacket(new byte[PACKET_BUFFER_SIZE], PACKET_BUFFER_SIZE);
				try {
					datagramSocket.receive(datagramPacket);
				} catch (final IOException e) {
					LOGGER.error("error waiting for new datagram packet in UDP server endpoint", e);
					continue; // should we continue? if we don't we'll have a complete server crash on our hands
				}
				synchronized (knownClients) {
					if (!knownClients.containsKey(datagramPacket.getSocketAddress())) {
						LOGGER.debug(String.format("received UDP datagram packet from unknown client endpoint [%s]",
								datagramPacket.getSocketAddress()));
						final ClientEndpointUDP clientEndpoint = new ClientEndpointUDP(ServerEndpointUDP.this, datagramSocket,
								datagramPacket);
						knownClients.put(datagramPacket.getSocketAddress(), clientEndpoint);
						newClients.add(clientEndpoint);
					} else {
						String debugMsg = "received UDP datagram packet from known client endpoint [%s]";
						LOGGER.debug(String.format(debugMsg, datagramPacket.getSocketAddress()));
						if (LOGGER.isTraceEnabled()) {
							String messageReceived = new String(datagramPacket.getData());
							String traceMsg = "datagram packet received from %s, message: %s";
							LOGGER.trace(String.format(traceMsg, datagramPacket.getSocketAddress(), messageReceived));
						}
						knownClients.get(datagramPacket.getSocketAddress()).handleDatagramPacket(datagramPacket);
					}
				}
			}
		}
	}

	/**
	 * Removes a client from the list of known clients. Most likely because it was explicitly closed by calling
	 * {@link ClientEndpointUDP#close()}.
	 * 
	 * @param clientSocketAddress The socket address which identifies the {@link ClientEndpointUDP} to be removed.
	 */
	public void removeClient(final SocketAddress clientSocketAddress) {
		knownClients.remove(clientSocketAddress);
	}

	/**
	 * Returns whether a client is known to this (UDP) server endpoint.
	 * 
	 * @param clientSocketAddress The address by which the client is identified.
	 * @return Whether {@link #knownClients} contains the given socket address.
	 */
	public boolean hasClient(final SocketAddress clientSocketAddress) {
		return knownClients.containsKey(clientSocketAddress);
	}

	/**
	 * @return {@link DatagramSocket#isClosed()}
	 */
	@Override
	boolean isClosed() {
		return datagramSocket.isClosed();
	}

	/**
	 * Calls {@link DatagramSocket#close()}
	 */
	@Override
	void close() {
		datagramSocket.close();
	}
}