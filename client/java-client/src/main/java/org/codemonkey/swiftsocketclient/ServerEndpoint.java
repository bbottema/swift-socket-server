package org.codemonkey.swiftsocketclient;

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
 * Abstraction of the server socket. Acts as a wrapper for {@link Socket} and {@link DatagramSocket} to provide uniform behavior.
 * 
 * @author Benny Bottema
 * @see ServerEndpointUDP#handleDatagramPacket(DatagramPacket)
 * @since 1.0
 */
abstract class ServerEndpoint {
	/**
	 * Calls the close method on the embedded socket, or in case of UDP, nothing happens.
	 * 
	 * @throws IOException Thrown by {@link ServerEndpointTCP#close()}
	 */
	abstract void close()
			throws IOException;

	abstract InetAddress getInetAddress();

	abstract void send(String message)
			throws IOException;

	abstract String readLine()
			throws IOException;

	abstract void read()
			throws IOException;
}

/**
 * Simply delegates all calls to the {@link Socket}. This is because the {@link ServerEndpointUDP} is modeled after the TCP version.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
class ServerEndpointTCP extends ServerEndpoint {

	private final Socket socket;

	/**
	 * The input stream from the server on which we're waiting for data. Data is being read with {@link BufferedReader#readLine()}, so
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
	public ServerEndpointTCP(final Socket socket)
			throws IOException {
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	/**
	 * Calls {@link Socket#close()}.
	 */
	@Override
	void close()
			throws IOException {
		socket.close();
	}

	/**
	 * @return {@link Socket#getInetAddress()}.
	 */
	@Override
	InetAddress getInetAddress() {
		return socket.getInetAddress();
	}

	/**
	 * Writes message to {@link Socket#getOutputStream()} using a {@link PrintWriter}.
	 */
	@Override
	void send(final String message)
			throws IOException {
		try {
			final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.write(message);
			out.flush();
		} catch (final IOException e) {
			Logger.getLogger(ServerEndpoint.class).error("outputstream could not be obtained (tried to use dropped connection perhaps?)");
			throw e;
		}
	}

	/**
	 * @return Data received from the socket, by calling <code>{@link #reader}.readLine()</code>.
	 */
	@Override
	String readLine()
			throws IOException {
		return reader.readLine();
	}

	/**
	 * Calls <code>{@link #reader}.read()</code>
	 */
	@Override
	void read()
			throws IOException {
		reader.read();
	}
}

/**
 * Server endpoint that handles UDP traffic and maintains a list of known and new datagram messages.
 * 
 * @author Benny Bottema
 * @see ServerEndpointUDP#handleDatagramPacket(DatagramPacket)
 * @since 1.0
 */
class ServerEndpointUDP extends ServerEndpoint {

	private final DatagramSocket datagramSocket;
	private final LinkedList<DatagramPacket> queue;
	private final InetAddress serverInetAddress;
	private final SocketAddress serverSocketAddress;

	/**
	 * Constructor; initializes handshake by requesting an acknowledgment command to a handshake connection request.
	 * 
	 * @param datagramSocket The {@link DatagramSocket} to send messages to the server with.
	 * @param inetAddress The host address of the server.
	 * @param socketAddress The host and address and port of the server.
	 */
	public ServerEndpointUDP(final DatagramSocket datagramSocket, final InetAddress inetAddress, final SocketAddress socketAddress) {
		this.datagramSocket = datagramSocket;
		serverInetAddress = inetAddress;
		serverSocketAddress = socketAddress;
		queue = new LinkedList<DatagramPacket>();
	}

	/**
	 * Does nothing, only useful for TCP socket.
	 */
	@Override
	void close()
			throws IOException {
		//
	}

	/**
	 * Returns the server's {@link InetAddress} for logging purposes. In case of UDP {@link SocketAddress} is actually used to identify and
	 * communicate with the server.
	 */
	@Override
	InetAddress getInetAddress() {
		return serverInetAddress;
	}

	/**
	 * Sends a message in the form of a {@link DatagramPacket}, using {@link DatagramSocket#send(DatagramPacket)}.
	 */
	@Override
	void send(final String message)
			throws IOException {
		final DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), serverSocketAddress);
		datagramSocket.send(packet);
	}

	/**
	 * Adds a datagram packet to the {@link #queue} to be processed, which is done by calling {@link #readLine()}. This is so that it mimics
	 * how TCP messages are read, molded into a uniform abstraction ({@link ServerEndpoint}).
	 * <p>
	 * The difference between TCP and UDP is that UDP messages are pushed, by TCP messages can be pulled by the system. And so we need to
	 * cache (or 'queue') the UDP messages until they are pulled by the client the same way TCP messages are being pulled.
	 * 
	 * @param datagramPacket The {@link DatagramPacket} to add to the queue waiting for {@link #readLine()} calls the same way a TCP
	 *            messages are retrieved by the client.
	 */
	public void handleDatagramPacket(final DatagramPacket datagramPacket) {
		queue.add(datagramPacket);
	}

	/**
	 * Returns a String line from next {@link DatagramPacket} in the {@link #queue}. This is done by using a {@link BufferedReader} to read
	 * {@link DatagramPacket#getData()}. If there are no datagram packets to convert to a string line, this method blocks until a datagram
	 * packet is received or the socket has been closed.
	 */
	@Override
	String readLine()
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

	@Override
	void read()
			throws IOException {
		//
	}
}