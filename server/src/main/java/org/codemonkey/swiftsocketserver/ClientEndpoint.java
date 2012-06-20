package org.codemonkey.swiftsocketserver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * Abstraction of the client socket. Acts as a wrapper for {@link Socket} and {@link DatagramSocket} to provide uniform behavior. <br />
 * See {@link ServerEndpoint} for the as server version.
 * 
 * @author Benny Bottema
 * @see ClientEndpointUDP#handleDatagramPacket(DatagramPacket)
 * @since 1.0
 */
interface ClientEndpoint {
	/**
	 * Calls the close method on the embedded socket, or in case of UDP, remove itself from parent {@link ServerEndpointUDP} list of known
	 * clients.
	 * 
	 * @throws IOException Thrown by {@link ServerEndpointTCP#close()}
	 */
	void close()
			throws IOException;

	InetAddress getInetAddress();

	void send(String message)
			throws IOException;

	String readLine()
			throws IOException;

	void read()
			throws IOException;

	/**
	 * @return Whether the endpoint connection has been explicitly closed.
	 */
	boolean isClosed();
}

/**
 * Simply delegates all calls to the {@link Socket}. This is because the {@link ClientEndpointUDP} is modeled after the TCP version:
 * {@link ClientEndpointTCP} is a decorator for a TCP Socket, so that both the both the TCP and UDP endpoints implement the same interface.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
class ClientEndpointTCP implements ClientEndpoint {

	private final Socket socket;

	/**
	 * The input stream from the client on which we're waiting for data. Data is being read with {@link BufferedReader#readLine()}, so
	 * incoming messages need to end with a newline character '\n'. In addition, to work with Flex clients, an additional NULL character
	 * '\0' needs to be added.
	 */
	private final BufferedReader reader;

	/**
	 * Creates a {@link BufferedReader} for the socket's input stream.
	 * 
	 * @param socket The open socket on which to listen for TCP messages.
	 * @throws IOException Thrown by {@link Socket#getInputStream()}.
	 */
	public ClientEndpointTCP(final Socket socket)
			throws IOException {
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	/**
	 * Calls {@link Socket#close()}.
	 * 
	 * @throws IOException Thrown by {@link Socket#close()}.
	 */
	@Override
	public void close()
			throws IOException {
		socket.close();
	}

	/**
	 * @return {@link Socket#isClosed()}.
	 */
	@Override
	public boolean isClosed() {
		return socket.isClosed();
	}

	/**
	 * @return {@link Socket#getInetAddress()}.
	 */
	@Override
	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}

	/**
	 * Writes message to {@link Socket#getOutputStream()} using a {@link PrintWriter}.
	 * 
	 * @param message The message to send to the client.
	 * @throws IOException Thrown by {@link Socket#getOutputStream()}.
	 */
	@Override
	public void send(final String message)
			throws IOException {
		try {
			final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.write(message);
			out.flush();
		} catch (final IOException e) {
			final Logger LOGGER = Logger.getLogger(ClientEndpoint.class);
			LOGGER.error("outputstream could not be obtained (tried to use dropped connection perhaps?)");
			throw e;
		}
	}

	/**
	 * @return Data received from the socket, by calling <code>{@link #reader}.readLine()</code>.
	 * @throws IOException Thrown by {@link BufferedReader#readLine()}.
	 */
	@Override
	public String readLine()
			throws IOException {
		return reader.readLine();
	}

	/**
	 * Calls <code>{@link #reader}.read()</code>.
	 * 
	 * @throws IOException Thrown by {@link BufferedReader#read()}.
	 */
	@Override
	public void read()
			throws IOException {
		reader.read();
	}
}

/**
 * Client endpoint that handles UDP traffic and maintains a list of known and new datagram messages.
 * 
 * @author Benny Bottema
 * @see ClientEndpointUDP#handleDatagramPacket(DatagramPacket)
 * @since 1.0
 */
class ClientEndpointUDP implements ClientEndpoint {

	private static final Logger LOGGER = Logger.getLogger(ClientEndpoint.class);

	/**
	 * Contains the value that is the command received from a client when making first contact, so that the server knows where to send the
	 * UDP messages.
	 * <p>
	 * Value: {@value #CONNECT}.
	 */
	public static final String CONNECT = "CONNECT";

	/**
	 * Contains the that is the first value we must send to the client as an acknowledgment answer to '{@value #CONNECT}'.
	 * <p>
	 * Value: {@value #ACK}.
	 */
	public static final String ACK = "ACK";

	private final ServerEndpointUDP serverEndpointUDP;
	private final DatagramSocket datagramSocket;
	private final LinkedList<DatagramPacket> queue;
	private final InetAddress clientInetAddress;
	private final SocketAddress clientSocketAddress;

	/**
	 * Constructor; performs a handshake verification by responding with an acknowledgment command to a handshake connection request.
	 * 
	 * @param serverEndpointUDP The server endpoint from which we can remove clients if necessary.
	 * @param datagramSocket The {@link DatagramSocket} to send messages to the client with.
	 * @param datagramPacket The initial {@link DatagramPacket} received from the client that should be a handshake request.
	 * @see #verifyHandshake(DatagramPacket)
	 */
	public ClientEndpointUDP(final ServerEndpointUDP serverEndpointUDP, final DatagramSocket datagramSocket,
			final DatagramPacket datagramPacket) {
		this.serverEndpointUDP = serverEndpointUDP;
		this.datagramSocket = datagramSocket;
		clientInetAddress = datagramPacket.getAddress();
		clientSocketAddress = datagramPacket.getSocketAddress();
		queue = new LinkedList<DatagramPacket>();
		verifyHandshake(datagramPacket);
	}

	/**
	 * Verifies that the first message to the server is an '{@value #CONNECT}' so we can complete the handshake by sending an '{@value #ACK}
	 * '
	 * 
	 * @param datagramPacket The request message from the client that must contain the connect handshake value.
	 */
	private void verifyHandshake(final DatagramPacket datagramPacket) {
		final String message = new String(datagramPacket.getData()).trim();
		if (!message.equals(CONNECT)) {
			final String msg = "expected handshake '%s' back, but received: '%s'";
			throw new UnknownMessageException(new ClientContext(this), String.format(msg, CONNECT, message));
		} else {
			LOGGER.debug("received handshake request from " + clientSocketAddress + ": " + message);
			try {
				LOGGER.debug("sending handshake acknowledgment to " + clientSocketAddress + ": " + ACK);
				send(ACK);
			} catch (final IOException e) {
				final String msg = "unable establish first contact with client '%s' with acknowledgment response '%s'!";
				throw new RuntimeException(String.format(msg, clientInetAddress, ACK), e);
			}
		}
	}

	/**
	 * Removes this client endpoint from the server endpoint's list of known clients.
	 */
	@Override
	public void close() {
		serverEndpointUDP.removeClient(clientSocketAddress);
	}

	/**
	 * @return {@link Socket#isClosed()}.
	 */
	@Override
	public boolean isClosed() {
		return serverEndpointUDP.hasClient(clientSocketAddress);
	}

	/**
	 * Returns the client's {@link InetAddress} for logging purposes. In case of UDP {@link SocketAddress} is actually used to identify and
	 * communicate with the server.
	 * 
	 * @return {@link #clientInetAddress}.
	 */
	@Override
	public InetAddress getInetAddress() {
		return clientInetAddress;
	}

	/**
	 * Sends a message in the form of a {@link DatagramPacket}, using {@link DatagramSocket#send(DatagramPacket)}.
	 * 
	 * @param message The message to send to client.
	 * @throws IOException Thrown by {@link DatagramPacket#DatagramPacket(byte[], int, SocketAddress)} and
	 *             {@link DatagramSocket#send(DatagramPacket)}.
	 */
	@Override
	public void send(final String message)
			throws IOException {
		final DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), clientSocketAddress);
		datagramSocket.send(packet);
	}

	/**
	 * Adds a datagram packet to the {@link #queue} to be processed, which is done by calling {@link #readLine()}. This is so that it mimics
	 * how TCP messages are read, molded into a uniform abstraction ({@link ClientEndpoint}).
	 * <p>
	 * The difference between TCP and UDP is that UDP messages are pushed, by TCP messages can be pulled by the system. And so we need to
	 * cache (or 'queue') the UDP messages until they are pulled by the server the same way TCP messages are being pulled.
	 * 
	 * @param datagramPacket The {@link DatagramPacket} to add to the queue waiting for {@link #readLine()} calls the same way a TCP
	 *            messages are retrieved by the server.
	 */
	public void handleDatagramPacket(final DatagramPacket datagramPacket) {
		queue.add(datagramPacket);
	}

	/**
	 * Returns a String line from the next {@link DatagramPacket} in the {@link #queue}. This is done by using a {@link BufferedReader} to
	 * read {@link DatagramPacket#getData()}. If there are no datagram packets to convert to a string line, this method blocks until a
	 * datagram packet is received or the socket has been closed.
	 * 
	 * @return A complete datagram packet value read using {@link BufferedReader#readLine()} so that it detects the newline and zero/NULL
	 *         character.
	 * @throws IOException Thrown by {@link BufferedReader#readLine()}.
	 */
	@Override
	public String readLine()
			throws IOException {
		while (!datagramSocket.isClosed()) {
			if (queue.size() > 0) {
				final DatagramPacket datagram = queue.removeFirst();
				final BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(new ByteArrayInputStream(datagram.getData())));
				return bufferedReader.readLine();
			} else {
				ServerUtil.defaultSleep();
			}
		}
		// socket.close() will be called when the SwiftSocketServer is stopping,
		// returning null won't cause NullPointerException in that case
		return null;
	}

	/**
	 * Does nothing, since the entire message is conveyed by a {@link DatagramPacket} and we don't need the extra read for get out the NULL
	 * character (which was needed in case of Flash clients on TCP sockets).
	 */
	@Override
	public void read() {
		//
	}
}