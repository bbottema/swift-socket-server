package org.codemonkey.swiftsocketserver;

import java.io.IOException;
import java.net.BindException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A low level message server that mediates messages from and to clients and the server in the form of 'datagrams' (custom serialized
 * messages identified by a unique signature). See {@link ClientMessageToServer} for further details on these messages.
 * <p>
 * This class manages some server basics such as the server {@link Socket}, {@link ServerType} to create the server endpoint with, the port
 * on which this server listens and the server state (stopped, stopping, running) as well as a maintaining a flag that indicated the use of
 * ping / pong mode. This class also contains the lists of message types mapped to the unique identifiers known to both the clients and
 * server.
 * <p>
 * Furthermore, this class receives new client connections and assigns {@link ClientHandler} instances (see {@link #clientHandlers}). The
 * client handlers offer executable messages from clients back for the server to queue them and execute them sequentially (see
 * {@link #clientMessages}).
 * <p>
 * This server embodies the client listening function regardless of the type of server (UDP / TCP) in a uniform way. The differences between
 * UDP and TCP are managed by the {@link #serverType}. Everything that this server is used for can be done with either TCP or UDP without
 * the user having to change anything. Just provide the right {@link ServerType} and the server changes without breaking API.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public class SwiftSocketServer {

	private static final Logger LOGGER = Logger.getLogger(SwiftSocketServer.class);

	/**
	 * The internal message id, {@value #MESSAGE_ID_PINGPONG}, used to communicate ping pong messages.
	 */
	private static final int MESSAGE_ID_PINGPONG = 999;
	/**
	 * The internal message id, {@value #MESSAGE_ID_BYEBYE}, used to recognize an incoming Bye Bye notification.
	 */
	private static final int MESSAGE_ID_BYEBYE = 998;

	private final int port;

	/**
	 * The {@link ServerType} used to create the TCP/UDP {@link ServerEndpoint} with.
	 */
	private final ServerType serverType;

	/**
	 * the server endpoint takes care of sending message to clients and receiving incoming client sockets. Created when starting the server
	 * as a TCP or UDP endpoint.
	 * 
	 * @see ServerEndpoint
	 */
	private ServerEndpoint serverEndpoint;

	/**
	 * Used to identify a {@link ClientMessageToServer} type that can handle a binary packet.
	 */
	private final Map<Integer, Class<? extends ClientMessageToServer<?>>> clientMessageToServerTypeList;

	/**
	 * Contains context objects mapped directly against a executable client message type. Usage of this list is optional.
	 * <p>
	 * This mechanism automates executing messages using a pre-registered context object. Client messages that are registered in this list
	 * will not be queued in the {@link #clientMessages} queue for manual processing.
	 */
	private final Map<Class<? extends ClientMessageToServer<?>>, Object> executionContexts;

	/**
	 * Used to prepend the right binary packet code for a given {@link ServerMessageToClient} type.
	 */
	private final Map<Class<? extends ServerMessageToClient>, Integer> serverMessageToClientIdList;

	/**
	 * A queue that contains unprocessed messages from any client.
	 */
	private final LinkedList<ClientMessageToServer<?>> clientMessages;

	/**
	 * A list of simultaneous client connection handlers, for receiving {@link ClientMessageToServer} messages and sending
	 * {@link ServerMessageToClient} messages.
	 */
	private final Map<ClientContext, ClientHandler> clientHandlers;

	/**
	 * Indicates whether stop() has been called and the server socket has been closed.
	 */
	private boolean stopping;

	/**
	 * Default turned off in the constructor.
	 */
	private boolean pingPongMode;

	/**
	 * Interval in milliseconds for ping messages to the client. Default set to 2500ms in the constructor.
	 */
	private int pingPongIntervalMs;

	/**
	 * Timeout in milliseconds for a pong message to the client. Default set to 5000ms in the constructor.
	 */
	private int pingPongTimeoutMs;

	/**
	 * Default constructor, calls {@link #SwiftSocketServer(int, ServerType)} with TCP {@link ServerType}.
	 * 
	 * @param port The port on which to start listening.
	 */
	public SwiftSocketServer(final int port) {
		this(port, ServerType.TCP);
	}

	/**
	 * Creates a swift socket server and registers a couple of basic message types (message invalid, 'Ping Pong' and 'Bye Bye'). Also sets
	 * default Ping Pong interval and timeout. The Ping Pong mode is turned off by default.
	 * <ul>
	 * <li>Message invalid: sent to the client when a message code was not recognized</li>
	 * <li>Ping message: sent when {@link #setPingPongMode(boolean, int, int)} feature is turned on</li>
	 * <li>ByeBye message: sent as an ACK message when a client is gracefully disconnecting by sending a Bye Bye message</li>
	 * </ul>
	 * 
	 * @param port The port on which to start listening.
	 * @param serverType The {@link ServerType} strategy used to communicate.
	 */
	public SwiftSocketServer(final int port, final ServerType serverType) {
		this.port = port;
		this.serverType = serverType;
		pingPongMode = false;
		pingPongIntervalMs = 2500;
		pingPongTimeoutMs = 5000;
		clientMessageToServerTypeList = new HashMap<Integer, Class<? extends ClientMessageToServer<?>>>();
		serverMessageToClientIdList = new HashMap<Class<? extends ServerMessageToClient>, Integer>();
		clientMessages = new LinkedList<ClientMessageToServer<?>>();
		clientHandlers = Collections.synchronizedMap(new HashMap<ClientContext, ClientHandler>());
		executionContexts = new HashMap<Class<? extends ClientMessageToServer<?>>, Object>();

		registerClientMessageToServerType(MESSAGE_ID_PINGPONG, ClientMessageToServerPingPong.class);
		registerClientMessageToServerType(MESSAGE_ID_BYEBYE, ClientMessageToServerByeBye.class);

		registerServerMessageToClientId(MESSAGE_ID_PINGPONG, ServerMessageToClientPingPong.class);
	}

	/**
	 * Sets PingPong mode, use to poll clients for time-outs. Clients will need to support this as well. When changed, will only apply the
	 * new setting to <strong>new</strong> clients.
	 * 
	 * @param pingPongMode Flag indicating use of ping pong mode yes/no.
	 */
	public void setPingPongMode(final boolean pingPongMode) {
		this.pingPongMode = pingPongMode;
	}

	/**
	 * Sets PingPong mode, use to poll clients for time-outs. Clients will need to support this as well. When changed, will only apply the
	 * new setting to <strong>new</strong> clients.
	 * 
	 * @param pingPongMode Flag indicating use of ping pong mode yes/no.
	 * @param pingPongIntervalMs Interval in milliseconds for ping messages to the client.
	 * @param pingPongTimeoutMs Timeout in milliseconds for a pong message to the client.
	 */
	public void setPingPongMode(final boolean pingPongMode, final int pingPongIntervalMs, final int pingPongTimeoutMs) {
		this.pingPongMode = pingPongMode;
		this.pingPongIntervalMs = pingPongIntervalMs;
		this.pingPongTimeoutMs = pingPongTimeoutMs;
	}

	/**
	 * Registers a client-to-server message on the given unique identifier. Both the client and server must use the same identifier to be
	 * able to decode the message on the server.
	 * 
	 * @param messageId The unique identifier for this message type.
	 * @param messageType A {@link ClientMessageToServer} sub type.
	 */
	public void registerClientMessageToServerType(final int messageId, final Class<? extends ClientMessageToServer<?>> messageType) {
		clientMessageToServerTypeList.put(messageId, messageType);
	}

	/**
	 * Registers a server-to-client message on the given unique identifier. Both the client and server must use the same identifier to be
	 * able to decode the message on the client.
	 * 
	 * @param messageId The unique identifier for this message type.
	 * @param messageType A {@link ServerMessageToClient} sub type.
	 */
	public void registerServerMessageToClientId(final int messageId, final Class<? extends ServerMessageToClient> messageType) {
		serverMessageToClientIdList.put(messageType, messageId);
	}

	/**
	 * Registers a default execution context for a client-to-server message type. Incoming messages of the given type are not queued in
	 * {@link #clientMessages}, but executed directly using the registered execution context. This way the user doesn't have to manually
	 * process these executable client messages.
	 * 
	 * @param messageType The type of message that will be matched and executed using the given context object.
	 * @param executionContext The execution context object used to execute executable client messages against.
	 */
	public void registerExecutionContext(final Class<? extends ClientMessageToServer<?>> messageType, final Object executionContext) {
		executionContexts.put(messageType, executionContext);
	}

	/**
	 * Thread safe method that starts the client thread, listening for new messages and sending messages.
	 * 
	 * @see InvisibleServerRunner
	 */
	public void start() {
		LOGGER.info("booting Swift message server. Hold on...");
		if (serverType == ServerType.UDP && !pingPongMode) {
			LOGGER.warn("WARNING: running UDP server without ping pong mode: if a client connection drops, we won't know!");
		}
		stopping = false;
		new Thread(new InvisibleServerRunner()).start();
	}

	/**
	 * @return Whether there are unprocessed client messages waiting in queue to be executed.
	 */
	public boolean hasClientMessages() {
		return clientMessages.size() > 0;
	}

	/**
	 * @return The next queues unprocessed client messages to be executed (see {@link LinkedList#poll()}).
	 */
	public ClientMessageToServer<?> getNextClientMessage() {
		return clientMessages.poll();
	}

	/**
	 * Removes a {@link ClientContext} and associated {@link ClientHandler} from the list of known clients.
	 * 
	 * @param client The client we need to purge along with its connection handler.
	 */
	protected void disposeOfClient(final ClientContext client) {
		synchronized (this) {
			synchronized (client) {
				synchronized (clientHandlers) {
					client.setClientSaidByeBye(true);
					clientHandlers.remove(client);
					try {
						client.getClientEndpoint().close();
					} catch (final IOException e) {
						LOGGER.error("error disposing client: socket IOException: " + e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Thread that assigns {@link #serverEndpoint} with a new server endpoint and starts handling new messages.
	 * <p>
	 * Since {@link Runnable} is a public interface and we don't want the <code>run</code> method exposed directly, we'll use an
	 * intermediate invisible class (outside the parent class at least) that implements it.
	 * 
	 * @author Benny Bottema
	 * @since 1.0
	 */
	private class InvisibleServerRunner implements Runnable {
		@Override
		public void run() {
			try {
				serverEndpoint = serverType.createServerEndpoint(port);
				final String msg = "server booting done. Waiting for clients @ port %s (%s)...";
				LOGGER.info(String.format(msg, port, serverType.name()));
				handleMessages(serverEndpoint);
			} catch (final BindException e) {
				LOGGER.debug(e);
				LOGGER.info("server already running! Quiting...");
				stop();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Listens for new clients on the server endpoint. Creates a new {@link ClientHandler} for each new {@link ClientEndpoint}.
	 * 
	 * @param serverEndpoint The server endpoint on which we expect client messages.
	 * @throws IOException Thrown by {@link ServerEndpoint#getClientEndpoint()}.
	 */
	private void handleMessages(final ServerEndpoint serverEndpoint)
			throws IOException {
		while (!isStopping()) {
			try {
				final ClientEndpoint clientEndpoint = serverEndpoint.getClientEndpoint();
				if (!isStopping()) {
					final ClientContext clientContext = new ClientContext(clientEndpoint);
					final ClientHandler clientHandler = new ClientHandler(this, clientContext, pingPongMode, pingPongIntervalMs,
							pingPongTimeoutMs);
					clientHandlers.put(clientContext, clientHandler);
					new Thread(clientHandler).start();
				}
			} catch (final SocketException e) {
				LOGGER.error("server socket crashed, shutting server down...", e);
				stop();
			}
		}
	}

	/**
	 * Finds the right client handler based on the message's client context and sends the message to it.
	 * 
	 * @param message The message to send to the client associated with it.
	 * @see ClientHandler#sendMessage(ServerMessageToClient)
	 */
	public void sendMessage(final ServerMessageToClient message) {
		clientHandlers.get(message.getClientContext()).sendMessage(message);
	}

	/**
	 * @param message The message to send to all clients (ignoring clientContext on the message).
	 * @see SwiftSocketServer#broadcastMessage(ServerMessageToClient)
	 */
	public void broadcastMessage(final ServerMessageToClient message) {
		synchronized (clientHandlers) {
			for (final ClientContext clientcontext : getAllClientContexts()) {
				if (!clientcontext.isClientSaidByeBye()) {
					clientHandlers.get(clientcontext).sendMessage(message);
				}
			}
		}
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
	protected void addClientMessage(final ClientMessageToServer<?> message) {
		if (executionContexts.containsKey(message.getClass())) {
			final Object contextObject = executionContexts.get(message.getClass());
			((ClientMessageToServer<? super Object>) message).execute(contextObject);
		} else {
			clientMessages.offer(message);
		}
	}

	/**
	 * @param messageId The message id for which to find the registered message type.
	 * @return The client-to-server message type associated with the given message id, or {@link ClientMessageToServerInvalid} if not found.
	 */
	protected Class<? extends ClientMessageToServer<?>> getClientMessageToServerType(final int messageId) {
		final Class<? extends ClientMessageToServer<?>> messageType = clientMessageToServerTypeList.get(messageId);
		return (messageType != null) ? messageType : ClientMessageToServerInvalid.class;
	}

	/**
	 * @param serverMessageToClientType The message type to identify.
	 * @return The unique identifier registered for the given message type.
	 */
	public int getServerMessageId(final Class<? extends ServerMessageToClient> serverMessageToClientType) {
		return serverMessageToClientIdList.get(serverMessageToClientType);
	}

	/**
	 * @return All client contexts currently known. These clients are not necessarily active.
	 */
	public Collection<ClientContext> getAllClientContexts() {
		return clientHandlers.keySet();
	}

	/**
	 * Thread safe method to make the server initialize stopping procedure. This means setting a flag and letting client handlers manage
	 * disconnecting the clients, while closing the server socket simultaneously.
	 */
	public void stop() {
		stopping = true;
		shutdown();
	}

	private void shutdown() {
		if (serverEndpoint != null && !serverEndpoint.isClosed()) {
			try {
				serverEndpoint.close();
			} catch (final IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @return Whether the server is not stopping.
	 */
	public boolean isRunning() {
		return !isStopping();
	}
}