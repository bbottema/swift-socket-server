package org.codemonkey.swiftsocketclient;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A low level message client that mediates messages from and to a server and the client in the form of 'datagrams' (custom serialized
 * messages identified by a unique signature). See {@link ServerMessageToClient} for further details on these messages.
 * <p>
 * This class manages some client basics such as the client {@link Socket}, {@link ClientType} to create the client endpoint with, the host
 * and port on which this server listens and the client state (stopped, stopping, running). This class also contains the lists of message
 * types mapped to the unique identifiers known to both the client and server.
 * <p>
 * Furthermore, this class receives a new server connection and assigns a {@link ServerHandler} instance (see {@link #serverHandler}). The
 * server handler offers executable messages from the server back for the client to queue them and execute them sequentially (see
 * {@link #serverMessages}).
 * <p>
 * This server embodies the server listening function regardless of the type of client (UDP / TCP) in a uniform way. The differences between
 * UDP and TCP are managed by the {@link #clientType}. Everything that this client is used for can be done with either TCP or UDP without
 * the user having to change anything. Just provide the right {@link ClientType} and the client changes without breaking API.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public class SwiftSocketClient {

	private static final Logger LOGGER = Logger.getLogger(SwiftSocketClient.class);

	/**
	 * The internal message id, {@value #MESSAGE_ID_PINGPONG}, used to communicate ping pong messages.
	 */
	private static final int MESSAGE_ID_PINGPONG = 999;
	/**
	 * The internal message id, {@value #MESSAGE_ID_BYEBYE}, used to send a Bye Bye notification.
	 */
	private static final int MESSAGE_ID_BYEBYE = 998;

	private final String host;

	private final int port;

	private final ClientType clientType;

	private ClientEndpoint clientEndpoint;

	/**
	 * Used to identify a {@link ServerMessageToClient} type that can handle a binary packet.
	 */
	private final Map<Integer, Class<? extends ServerMessageToClient<?>>> serverMessageToClientTypeList;

	/**
	 * Contains context objects mapped directly against a executable client message type. Usage of this list is optional.
	 * <p>
	 * This mechanism automates executing messages using a pre-registered context object. Client messages that are registered in this list
	 * will not be queued in the {@link #serverMessages} queue for manual processing.
	 */
	private final Map<Class<? extends ServerMessageToClient<?>>, Object> executionContexts;

	/**
	 * Used to prepend the right binary packet code for a given {@link ClientMessageToServer} type.
	 */
	private final Map<Class<? extends ClientMessageToServer>, Integer> clientMessageToServerIdList;

	/**
	 * A queue that contains unprocessed messages from the server.
	 */
	private final LinkedList<ServerMessageToClient<?>> serverMessages;

	/**
	 * A server connection handler, for receiving {@link ServerMessageToClient} messages and sending {@link ClientMessageToServer} messages.
	 */
	private ServerHandler serverHandler;

	private boolean stopping;

	/**
	 * Default constructor, calls {@link #SwiftSocketClient(String, int, ClientType)} with TCP {@link ClientType}.
	 * 
	 * @param host The host address on which to start listening.
	 * @param port The port on which to start listening.
	 */
	public SwiftSocketClient(final String host, final int port) {
		this(host, port, ClientType.TCP);
	}

	/**
	 * Creates a swift socket client and registers a couple of basic message types (message invalid, pingpong and byebye).
	 * <ul>
	 * <li>Message invalid: sent to the server when a message code was not recognized</li>
	 * <li>Pong message: sent when a Ping message was received from the server</li>
	 * <li>ByeBye message: sent to signal the server the client has been closed (gracefully disconnecting instead of just dropping the
	 * connection)</li>
	 * </ul>
	 * 
	 * @param host The host address on which to start listening.
	 * @param port The port on which to start listening.
	 * @param clientType The {@link ClientType} strategy used to communicate.
	 */
	public SwiftSocketClient(final String host, final int port, final ClientType clientType) {
		this.host = host;
		this.port = port;
		this.clientType = clientType;

		serverMessageToClientTypeList = new HashMap<Integer, Class<? extends ServerMessageToClient<?>>>();
		clientMessageToServerIdList = new HashMap<Class<? extends ClientMessageToServer>, Integer>();
		serverMessages = new LinkedList<ServerMessageToClient<?>>();
		executionContexts = new HashMap<Class<? extends ServerMessageToClient<?>>, Object>();

		registerClientMessageToServerType(MESSAGE_ID_PINGPONG, ClientMessageToServerPingPong.class);
		registerClientMessageToServerType(MESSAGE_ID_BYEBYE, ClientMessageToServerByeBye.class);

		registerServerMessageToClientType(MESSAGE_ID_PINGPONG, ServerMessageToClientPingPong.class);
	}

	/**
	 * Registers a client-to-server message on the given unique identifier. Both the client and server must use the same identifier to be
	 * able to decode the message on the server.
	 * 
	 * @param messageId The unique identifier for this message type.
	 * @param messageType A {@link ClientMessageToServer} sub type.
	 */
	public void registerClientMessageToServerType(final int messageId, final Class<? extends ClientMessageToServer> messageType) {
		clientMessageToServerIdList.put(messageType, messageId);
	}

	/**
	 * Registers a server-to-client message on the given unique identifier. Both the client and server must use the same identifier to be
	 * able to decode the message on the client.
	 * 
	 * @param messageId The unique identifier for this message type.
	 * @param messageType A {@link ServerMessageToClient} sub type.
	 */
	public void registerServerMessageToClientType(final int messageId, final Class<? extends ServerMessageToClient<?>> messageType) {
		serverMessageToClientTypeList.put(messageId, messageType);
	}

	/**
	 * Registers a default execution context for a server-to-client message type. Incoming messages of the given type are not queued in
	 * {@link #serverMessages}, but executed directly using the registered execution context. This way the user doesn't have to manually
	 * process these executable client messages.
	 * 
	 * @param messageType The type of message that will be matched and executed using the given context object.
	 * @param executionContext The execution context object used to execute executable client messages against.
	 */
	public void registerExecutionContext(final Class<? extends ServerMessageToClient<?>> messageType, final Object executionContext) {
		executionContexts.put(messageType, executionContext);
	}

	/**
	 * Thread safe method that starts the client thread, listening for new messages and sending messages.
	 * 
	 * @see InvisibleClientRunner
	 */
	public void start() {
		LOGGER.info(String.format("booting Swift message client (%s). Hold on...", clientType.name()));
		stopping = false;
		new Thread(new InvisibleClientRunner()).start();
	}

	/**
	 * Whether there are unprocessed server messages waiting in queue to be executed.
	 * 
	 * @return Whether {@link #serverMessages} contains any entries.
	 */
	public boolean hasServerResponses() {
		return serverMessages.size() > 0;
	}

	/**
	 * Returns the queue's next unprocessed server message to be executed.
	 * 
	 * @return The first entry of {@link #serverMessages} using {@link LinkedList#poll()}.
	 */
	public ServerMessageToClient<?> getNextServerResponse() {
		return serverMessages.poll();
	}

	/**
	 * Thread that assigns {@link #clientEndpoint} with a new client endpoint and starts handling new messages.
	 * <p>
	 * Since {@link Runnable} is a public interface and we don't want the <code>run</code> method exposed directly, we'll use an
	 * intermediate invisible class (outside the parent class at least) that implements it.
	 * 
	 * @author Benny Bottema
	 * @since 1.0
	 */
	private class InvisibleClientRunner implements Runnable {
		@Override
		public void run() {
			try {
				clientEndpoint = clientType.createClientEndpoint(host, port);
				final String msg = "client booting done. Connection to server established @ port %s (%s)";
				LOGGER.info(String.format(msg, port, clientType.name()));
				handleMessages(clientEndpoint);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Listens for a server to appear on the server endpoint. Creates a new {@link ServerHandler} for the {@link ServerEndpoint}.
	 * 
	 * @param clientEndpoint The client endpoint on which we expect server messages.
	 * @throws IOException Thrown by {@link ClientEndpoint#getServerEndpoint()}.
	 */
	private void handleMessages(final ClientEndpoint clientEndpoint)
			throws IOException {
		try {
			final ServerEndpoint serverEndpoint = clientEndpoint.getServerEndpoint();
			if (!isStopping()) {
				final ServerContext serverContext = new ServerContext(serverEndpoint);
				serverHandler = new ServerHandler(this, serverContext);
				new Thread(serverHandler).start();
			}
		} catch (final SocketException e) {
			LOGGER.error("client socket crashed, shutting client down...", e);
			stop();
		}
	}

	/**
	 * Sends the message to using {@link ServerHandler#sendMessage(ClientMessageToServer)}.
	 * 
	 * @param message The message to send to the server.
	 */
	public void sendMessage(final ClientMessageToServer message) {
		serverHandler.sendMessage(message);
	}

	/**
	 * @return {@link #stopping}
	 */
	private boolean isStopping() {
		return stopping;
	}

	/**
	 * Adds a message to the queue for later processing, unless a known context object has been registered for this object in
	 * {@link #executionContexts}.
	 * 
	 * @param message The message to be executed now if registered with {@link #executionContexts}, or later manually.
	 */
	@SuppressWarnings("unchecked")
	protected void addServerResponse(final ServerMessageToClient<?> message) {
		if (executionContexts.containsKey(message.getClass())) {
			final Object contextObject = executionContexts.get(message.getClass());
			((ServerMessageToClient<? super Object>) message).execute(contextObject);
		} else {
			serverMessages.offer(message);
		}
	}

	/**
	 * @param messageId The message id for which to find the registered message type.
	 * @return The server-to-client message type associated with the given message id, or {@link ServerMessageToClientInvalid} if not found.
	 */
	protected Class<? extends ServerMessageToClient<?>> getServerMessageToClientType(final int messageId) {
		final Class<? extends ServerMessageToClient<?>> messageType = serverMessageToClientTypeList.get(messageId);
		return (messageType != null) ? messageType : ServerMessageToClientInvalid.class;
	}

	/**
	 * @param clientMessageToServerType The message type to identify.
	 * @return The unique identifier registered for the given message type.
	 */
	public int getClientMessageId(final Class<? extends ClientMessageToServer> clientMessageToServerType) {
		return clientMessageToServerIdList.get(clientMessageToServerType);
	}

	/**
	 * Thread safe method to make the client initialize stopping procedure. This means sending a {@link ClientMessageToServerByeBye}, while
	 * closing the client socket simultaneously.
	 */
	public void stop() {
		LOGGER.info("waving server goodbye");
		sendMessage(new ClientMessageToServerByeBye());
		stopping = true;
		shutdown();
	}

	private void shutdown() {
		try {
			clientEndpoint.close();
		} catch (final IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * @return Whether the server is not stopping.
	 */
	public boolean isRunning() {
		return !isStopping();
	}

	/**
	 * @return Whether we have an active connection to the server.
	 */
	public boolean isConnected() {
		return serverHandler != null;
	}

	/**
	 * Cleans up the server handler in case the connection was dropped.
	 */
	public void removeServerHandler() {
		serverHandler = null;
	}
}