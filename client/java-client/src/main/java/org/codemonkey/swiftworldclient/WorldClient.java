package org.codemonkey.swiftworldclient;

import org.apache.log4j.Logger;
import org.codemonkey.swiftsocketclient.ClientMessageToServer;
import org.codemonkey.swiftsocketclient.ClientType;
import org.codemonkey.swiftsocketclient.ServerMessageToClient;
import org.codemonkey.swiftsocketclient.ServerUtil;
import org.codemonkey.swiftsocketclient.SwiftSocketClient;
import org.codemonkey.swiftsocketclient.UnknownMessageException;

/**
 * The World Client class adds a thin layer around the {@link SwiftSocketClient} to ease the management of a server-client interaction, or
 * simulation / game state. It does not handle clients or connections, only client messages in the context of a specific world
 * implementation.
 * <p>
 * The World Client allows us to execute decoded server messages against a known context (your game class for example), so that the
 * executable message can then invoke specific methods on your {@link WorldContext} implementation.
 * <p>
 * This class keeps polling the client for unprocessed messages and executes them when they become available.
 * 
 * @param <T> A specific {@link WorldContext} implementation, such a some game or a simulation.
 * @author Benny Bottema
 * @since 1.0
 */
public final class WorldClient<T extends WorldContext> {

	private static final Logger LOGGER = Logger.getLogger(WorldClient.class);

	/**
	 * The client that we simply start and listen to for new messages. All registered message types are delegated to this client instance.
	 */
	final SwiftSocketClient client;

	/**
	 * The {@link WorldContext} implementation against which all executable messages are executed against.
	 */
	private final T context;

	/**
	 * Starts a new default swift socket client on the given host and port, executing new messages as they become available.
	 * 
	 * @param host The port on which the server should listen.
	 * @param port The port on which the server should listen.
	 * @param context The context on which client messages are executed against.
	 * @see SwiftSocketClient#SwiftSocketClient(String, int)
	 */
	public WorldClient(final String host, final int port, final T context) {
		this.context = context;
		client = new SwiftSocketClient(host, port);
	}

	/**
	 * Starts a new swift socket client of a specific type (TCP / UDP) on the given port, executing new messages as they become available.
	 * 
	 * @param host The port on which the server should listen.
	 * @param port The port on which the server should listen.
	 * @param context The context on which client messages are executed against.
	 * @param clientType The type of server {@link ClientType} (TCP or UDP).
	 * @see SwiftSocketClient#SwiftSocketClient(String, int, ClientType)
	 */
	public WorldClient(final String host, final int port, final T context, final ClientType clientType) {
		this.context = context;
		client = new SwiftSocketClient(host, port, clientType);
	}

	/**
	 * Registers a client-to-server message on the given unique identifier on the message socket client, by delegating to
	 * {@link SwiftSocketClient#registerClientMessageToServerType(int, Class)}.
	 * 
	 * @param messageId The unique identifier for this message type.
	 * @param messageType A {@link ClientMessageToServer} sub type.
	 */
	public void registerClientMessageToServerType(final int messageId, final Class<? extends ClientMessageToServer> messageType) {
		client.registerClientMessageToServerType(messageId, messageType);
	}

	/**
	 * Registers a server-to-client message on the given unique identifier on the message socket client, by delegating to
	 * {@link SwiftSocketClient#registerServerMessageToClientType(int, Class)}.
	 * 
	 * @param messageId The unique identifier for this message type.
	 * @param messageType A {@link ServerMessageToClient} sub type.
	 */
	public void registerServerMessageToClientId(final int messageId, final Class<? extends ServerMessageToClient<?>> messageType) {
		client.registerServerMessageToClientType(messageId, messageType);
	}

	/**
	 * Calls {@link WorldContext#initWorld()} before starting the server, after which the server starts handling client connections and
	 * incoming messages.
	 * <p>
	 * Waits for until a connection to the server has been established.
	 */
	public void start() {
		// 1. starting universe
		LOGGER.info("loading universe...");
		context.initWorld();
		LOGGER.info("universe loaded. Welcome.\n");

		// 2. startup server
		client.start();

		new Thread(new InvisibleServerRunner()).start();

		while (!client.isConnected()) {
			ServerUtil.defaultSleep();
		}
	}

	private class InvisibleServerRunner implements Runnable {

		@Override
		@SuppressWarnings("unchecked")
		public void run() {
			// 3. poll for new messages
			while (client.isRunning()) {
				if (client.hasServerResponses()) {
					@SuppressWarnings("rawtypes")
					final ServerMessageToClient response = client.getNextServerResponse();
					try {
						response.execute(context);
					} catch (final UnknownMessageException e) {
						LOGGER.error(String.format("received invalid message from server (message: \"%s\")", e.getInvalidMessage()));
						LOGGER.debug("\t\t" + e);
					} catch (final Exception e) {
						// severe error, but catch to prevent server from crashing
						LOGGER.error(String.format("error executing message '%s'", response));
						LOGGER.debug("\t\t" + e);
					}
				} else {
					ServerUtil.defaultSleep();
				}
			}
		}

	}

	/**
	 * Stops the Swift Socket Server by delegating this call to {@link SwiftSocketClient#stop()}.
	 */
	public void stop() {
		client.stop();
	}

	/**
	 * Sends a {@link ClientMessageToServer} to the server using {@link SwiftSocketClient#sendMessage(ClientMessageToServer)}.
	 * 
	 * @param serverMessage The message to send to the client associated with it.
	 */
	public void sendMessage(final ClientMessageToServer serverMessage) {
		client.sendMessage(serverMessage);
	}
}