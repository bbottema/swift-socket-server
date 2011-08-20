package org.codemonkey.swiftsocketclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * Abstraction of the client socket. Acts as a wrapper for {@link Socket} and {@link DatagramSocket} to provide uniform behavior.
 * 
 * @author Benny Bottema
 * @see ClientEndpointTCP
 * @see ClientEndpointUDP
 * @since 1.0
 */
interface ClientEndpoint {
	/**
	 * All subclasses use the logger anchored to <code>ClientEndpoint.class</code>.
	 */
	public static final Logger LOGGER = Logger.getLogger(ClientEndpoint.class);

	/**
	 * @return An endpoint that acts similarly for a TCP and UDP server.
	 * @throws IOException Thrown by {@link ClientEndpointTCP#getServerEndpoint()}
	 */
	ServerEndpoint getServerEndpoint()
			throws IOException;

	/**
	 * @return Whether an explicitly call has been made to {@link ClientEndpoint#close()} or close on the passed in socket (both TCP and
	 *         UDP).
	 */
	boolean isClosed();

	/**
	 * Call the close method on the embedded socket (works for both TCP and UDP).
	 * 
	 * @throws IOException Thrown by {@link ClientEndpointTCP#close()}
	 */
	void close()
			throws IOException;
}

/**
 * Wrapper that delegates all calls directly to the decorated {@link Socket}. The Client Endpoint abstraction mimics how TCP deals with a
 * server and so the TCP client endpoint variant adds no extra functionality, except that it returns the server endpoint instead of the
 * {@link Socket} instance itself (see {@link #getServerEndpoint()}.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
class ClientEndpointTCP implements ClientEndpoint {

	private final Socket clientSocket;

	/**
	 * Constructor; simply stores a reference to the client socket.
	 * 
	 * @param clientSocket The {@link Socket} on which we connect new clients.
	 */
	public ClientEndpointTCP(final Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	/**
	 * Returns a new {@link ServerEndpointTCP} {@link Socket} decorator (which passed in the constructor).
	 * 
	 * @throws IOException Thrown by {@link ServerEndpointTCP#ServerEndpointTCP(Socket)}.
	 */
	@Override
	public ServerEndpoint getServerEndpoint()
			throws IOException {
		return new ServerEndpointTCP(clientSocket);
	}

	/**
	 * @return {@link Socket#isClosed()}
	 */
	@Override
	public boolean isClosed() {
		return clientSocket.isClosed();
	}

	/**
	 * Calls {@link Socket#close()}.
	 * 
	 * @throws IOException Thrown by {@link Socket#close()}.
	 */
	@Override
	public void close()
			throws IOException {
		clientSocket.close();
	}
}

/**
 * Wrapper that adds TCP like server handling to the UDP {@link DatagramSocket}.
 * 
 * @author Benny Bottema
 * @see #ClientEndpointUDP
 * @see #getServerEndpoint()
 * @see DatagramPacketReceiver
 * @since 1.0
 */
class ClientEndpointUDP implements ClientEndpoint {

	/**
	 * This should probably be tuned at some point to allow for larger datagram packets.
	 */
	private static final int PACKET_BUFFER_SIZE = 1024;

	/**
	 * Contains the that is the first value we expect from the server as an acknowledgment answer to '{@value #CONNECT}'.
	 * <p>
	 * Value: {@value #ACK}.
	 */
	public static final String ACK = "ACK";
	/**
	 * Contains the value that is the command sent to the server when making first contact, so that the server knows where to send the UDP
	 * messages.
	 * <p>
	 * Value: {@value #CONNECT}.
	 */
	public static final String CONNECT = "CONNECT";

	/**
	 * The client socket on which we'll send and receive messages on in the form of UDP datagram packets.
	 */
	private final DatagramSocket datagramSocket;

	/**
	 * The server endpoint, assigned when first UDP contact has been made. We can't know the address before that, as the server is
	 * identified by the UDP packet.
	 */
	private ServerEndpointUDP server;

	/**
	 * When initializing a connection to the server, we're performing a handshake which needs to be acknowledged first. Since UDP does not
	 * guarantee the order of incoming message, we might need to buffer unrelated message until the acknowledgment arrives.
	 */
	private boolean handshakeAcknowledged;

	/**
	 * This queue buffers incoming messages while the handshake is being confirmed by the server.
	 */
	private final LinkedList<DatagramPacket> bufferQueue;

	/**
	 * Constructor; starts a new <code>Thread</code> listening for new datagram packets. If a new server sends a message, it will be
	 * returned on {@link #getServerEndpoint()}. In either case the datagram is passed on to the server endpoint.
	 * 
	 * @param datagramSocket A DatagramSocket to listen to UDP messages.
	 * @param host The host to which the {@link DatagramPacket} objects need to be sent to.
	 * @param port The host to which the {@link DatagramPacket} objects need to be sent to.
	 */
	public ClientEndpointUDP(final DatagramSocket datagramSocket, final String host, final int port) {
		this.datagramSocket = datagramSocket;
		handshakeAcknowledged = false;
		bufferQueue = new LinkedList<DatagramPacket>();
		initializeHandshake(host, port);
		new Thread(new DatagramPacketReceiver()).start();
		// block the client until connection has been completely established
		while (!handshakeAcknowledged) {
			ServerUtil.defaultSleep();
		}
	}

	/**
	 * Performs a handshake with the server by sending a '{@value #CONNECT}' command. The response '{@value #ACK}' is handled by
	 * {@link DatagramPacketReceiver#run()}.
	 * 
	 * @param host The host to which the {@link DatagramPacket} objects need to be sent to.
	 * @param port The host to which the {@link DatagramPacket} objects need to be sent to.
	 */
	private void initializeHandshake(final String host, final int port) {
		LOGGER.info("offering server a handshake");
		final InetSocketAddress serverAddress = new InetSocketAddress(host, port);
		try {
			datagramSocket.send(new DatagramPacket(CONNECT.getBytes(), CONNECT.length(), serverAddress));
		} catch (final SocketException e) {
			throw new RuntimeException("unable to create UDP datagram packet", e);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Continuously receives {@link DatagramPacket} objects and delegates these to a {@link ServerEndpoint} to emulate TCP {@link Socket}
	 * behavior where a <code>Socket</code> per server receives messages from a connected client. Except there will always be one socket
	 * associated with this client endpoint, since there can only be one server instance.
	 * <p>
	 * If the server has not been assigned using a {@link ServerEndpointUDP}, we will do so with the first datagram packet we receive, which
	 * must also be an {@value ServerEndpointUDP#ACK}. If confirmed, the {@link DatagramSocket} is embedded in a {@link ServerEndpointUDP}.
	 * By abstracting this behavior in 'server endpoints', we can now for both TCP Socket messages and datagram packets act as though the
	 * endpoint received it directly from a client and so have consistent and uniform behavior for handling TCP and UDP messages. The only
	 * downside to this is that we cannot detect a client dropping the connection (so PingPong mode is advised).
	 * 
	 * @author Benny Bottema
	 * @see #verifyAcknowledgment(DatagramPacket)
	 * @since 1.0
	 */
	private class DatagramPacketReceiver implements Runnable {
		@Override
		public void run() {
			// a DatagramSocket will only close if the client is stopping gracefully
			while (!datagramSocket.isClosed()) {
				final DatagramPacket datagramPacket = new DatagramPacket(new byte[PACKET_BUFFER_SIZE], PACKET_BUFFER_SIZE);
				try {
					datagramSocket.receive(datagramPacket);
				} catch (final IOException e) {
					LOGGER.error("error waiting for new datagram packet in UDP client endpoint", e);
					continue; // should we continue? if we don't we'll have a complete server crash on our hands
				}
				if (LOGGER.isTraceEnabled()) {
					String messageReceived = new String(datagramPacket.getData());
					String msg = "datagram packet received from %s, message: %s";
					LOGGER.trace(String.format(msg, datagramPacket.getSocketAddress(), messageReceived));
				}
				if (server == null) {
					final String msg = "received UDP datagram packet from new server endpoint [%s]";
					LOGGER.debug(String.format(msg, datagramPacket.getSocketAddress()));
					server = new ServerEndpointUDP(datagramSocket, datagramPacket.getAddress(), datagramPacket.getSocketAddress());
				} else {
					final String msg = "received UDP datagram packet from known server endpoint [%s]";
					LOGGER.debug(String.format(msg, datagramPacket.getSocketAddress()));
				}
				if (handshakeAcknowledged) {
					server.handleDatagramPacket(datagramPacket);
				} else {
					verifyAcknowledgment(datagramPacket);
				}
			}
		}
	}

	/**
	 * Verifies that the server verifies the handshake request with an '{@value #ACK}' reply to an '{@value ClientEndpointUDP#CONNECT}'. If
	 * it isn't, the message will be buffered in {@link #bufferQueue} until it is.
	 * 
	 * @param datagramPacket The response message from the server that must contain the acknowledgment value.
	 */
	private boolean verifyAcknowledgment(final DatagramPacket datagramPacket) {
		final String message = new String(datagramPacket.getData()).trim();
		if (!message.equals(ACK)) {
			final String msg = "expected acknowledgment '%s' back, but received: '%s'. This message will be buffered.";
			LOGGER.debug(String.format(msg, ACK, message));
			return handshakeAcknowledged = false;
		} else {
			LOGGER.debug(String.format("server acknowledged handshake with '%s'", message));
			LOGGER.info(String.format("handshake confirmed", message));
			if (!bufferQueue.isEmpty()) {
				LOGGER.debug("handling messages from the handshake buffer queue...");
				for (DatagramPacket bufferedDatagramPacket : bufferQueue) {
					server.handleDatagramPacket(bufferedDatagramPacket);
				}
			}
			return handshakeAcknowledged = true;
		}
	}

	/**
	 * Returns a server endpoint in case a datagram packet is received from a previously unknown server. Otherwise, nothing is returning and
	 * this method remains blocking (like {@link ServerSocket#accept()}) until a server sends a datagram packet.
	 * <p>
	 * TCP messages received are inherently coupled to a (server) <code>Socket</code>. To mimic this behavior {@link ClientEndpointUDP}
	 * keeps track of the first connected server and will assume all future communication is with the same server binding.
	 */
	@Override
	public ServerEndpoint getServerEndpoint() {
		while (server == null && !datagramSocket.isClosed()) {
			ServerUtil.defaultSleep();
		}
		return server;
	}

	/**
	 * @return {@link DatagramSocket#isClosed()}
	 */
	@Override
	public boolean isClosed() {
		return datagramSocket.isClosed();
	}

	/**
	 * Calls {@link DatagramSocket#close()}
	 */
	@Override
	public void close() {
		datagramSocket.close();
	}
}