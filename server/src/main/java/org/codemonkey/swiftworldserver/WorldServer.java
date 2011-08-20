package org.codemonkey.swiftworldserver;

import org.apache.log4j.Logger;
import org.codemonkey.swiftsocketserver.ClientMessageToServer;
import org.codemonkey.swiftsocketserver.ServerMessageToClient;
import org.codemonkey.swiftsocketserver.ServerType;
import org.codemonkey.swiftsocketserver.ServerUtil;
import org.codemonkey.swiftsocketserver.SwiftSocketServer;
import org.codemonkey.swiftsocketserver.UnknownMessageException;

/**
 * The World Server class adds a thin layer around the {@link SwiftSocketServer} to ease the management of a world state, or simulation /
 * game state. It does not handle clients or connections, only client messages in the context of a specific world implementation.
 * <p>
 * The most important aspect of this layer is the heart beat implementation that invokes updates on the given world context, based on a
 * given frame per second. This FPS number determines how often the world or simulation is updated, while providing a delta factor
 * indicating the number of seconds per frame as a scale. For example, with an FPS of 4, every 250ms an update is invoked with a value of
 * 0,25. Lag is not accounted for (ie. the scale is not increased when an update takes too long). A higher FPS means faster but smaller
 * updates, while lower FPS means less updates which are larger each time (also see {@link #secondsPerFrame}).
 * <p>
 * The World Server allows us to execute decoded client messages against a known context (your game class for example), so that the
 * executable message can then invoke specific methods on your {@link WorldContext} implementation.
 * <p>
 * This class keeps polling the server for unprocessed messages and executes them when they become available.
 * 
 * @param <T> A specific {@link WorldContext} implementation, such a some game or a simulation.
 * @author Benny Bottema
 * @since 1.0
 */
public final class WorldServer<T extends WorldContext> {

	private static final Logger LOGGER = Logger.getLogger(WorldServer.class);

	/**
	 * The server that we simply start and listen to for new messages. All registered message types are delegated to this server instance.
	 */
	final SwiftSocketServer server;

	/**
	 * The scaling factor calculated based on the frame per second passed in the constructor of this class.
	 * <p>
	 * You may use this to determine the size of the change set in your world simulation, as it changes proportionally and in conjunction
	 * with the FPS. Higher FPS means smaller changes each frame, while lower FP means larger changes each frame. This way you may throttle
	 * the FPs, while keeping simulation progress constant.
	 */
	private double secondsPerFrame;

	/**
	 * The {@link WorldContext} implementation against which all executable messages are executed against.
	 */
	private final T context;

	/**
	 * Starts a new default TCP swift socket server on the given port, performing updates based on the given frame per second.
	 * 
	 * @param port The port on which the server should listen.
	 * @param context The context on which client messages are executed against.
	 * @param framesPerSecond The number of updates per second your world should perform.
	 * @see SwiftSocketServer#SwiftSocketServer(int)
	 */
	public WorldServer(final int port, final T context, final double framesPerSecond) {
		this(port, context, framesPerSecond, ServerType.TCP);
	}

	/**
	 * Starts a new swift socket server of a specific type (TCP / UDP) on the given port, performing updates based on the given frame per
	 * second.
	 * 
	 * @param port The port on which the server should listen.
	 * @param context The context on which client messages are executed against.
	 * @param framesPerSecond The number of updates per second your world should perform.
	 * @param serverType The type of server {@link ServerType} (TCP or UDP).
	 * @see SwiftSocketServer#SwiftSocketServer(int, ServerType)
	 */
	public WorldServer(final int port, final T context, final double framesPerSecond, final ServerType serverType) {
		this.context = context;
		setFramesPerSecond(framesPerSecond);
		server = new SwiftSocketServer(port, serverType);
	}

	/**
	 * Delegates to {@link SwiftSocketServer#setPingPongMode(boolean)}.
	 * <p>
	 * Note: When changed, will only apply the new setting to <strong>new</strong> clients.
	 * 
	 * @param pingPongMode Flag indicated if ping pong mode should be turned on yes /no.
	 */
	public void setPingPongMode(final boolean pingPongMode) {
		server.setPingPongMode(pingPongMode);
	}

	/**
	 * Delegates to {@link SwiftSocketServer#setPingPongMode(boolean, int, int)}.
	 * <p>
	 * Note: When changed, will only apply the new setting to <strong>new</strong> clients.
	 * 
	 * @param pingPongMode Flag indicated if ping pong mode should be turned on yes /no.
	 * @param pingPongIntervalMs Interval in milliseconds for ping messages to the client.
	 * @param pingPongTimeoutMs Timeout in milliseconds for a pong message to the client.
	 */
	public void setPingPongMode(final boolean pingPongMode, final int pingPongIntervalMs, final int pingPongTimeoutMs) {
		server.setPingPongMode(pingPongMode, pingPongIntervalMs, pingPongTimeoutMs);
	}

	/**
	 * Registers a client-to-server message on the given unique identifier on the swift socket server, by delegating to
	 * {@link SwiftSocketServer#registerClientMessageToServerType(int, Class)}.
	 * 
	 * @param messageId The unique identifier for this message type.
	 * @param messageType A {@link ClientMessageToServer} sub type.
	 */
	public void registerClientMessageToServerType(final int messageId, final Class<? extends ClientMessageToServer<T>> messageType) {
		server.registerClientMessageToServerType(messageId, messageType);
	}

	/**
	 * Registers a server-to-client message on the given unique identifier on the swift socket server, by delegating to
	 * {@link SwiftSocketServer#registerServerMessageToClientId(int, Class)}.
	 * 
	 * @param messageId The unique identifier for this message type.
	 * @param messageType A {@link ServerMessageToClient} sub type.
	 */
	public void registerServerMessageToClientId(final int messageId, final Class<? extends ServerMessageToClient> messageType) {
		server.registerServerMessageToClientId(messageId, messageType);
	}

	/**
	 * Calls {@link WorldContext#initWorld()} before starting the server, after which the server starts handling client connections,
	 * incoming messages and world updates.
	 */
	@SuppressWarnings("unchecked")
	public void start() {
		// 1. starting universe
		LOGGER.info("loading universe...");
		context.initWorld();
		LOGGER.info("universe loaded. Welcome.\n");

		// 2. startup server
		server.start();

		// 3. run simulation
		while (server.isRunning()) {
			final long now = System.currentTimeMillis();
			while (System.currentTimeMillis() - now < secondsPerFrame * 1000) {
				if (server.hasClientMessages()) {
					@SuppressWarnings("rawtypes")
					final ClientMessageToServer message = server.getNextClientMessage();
					try {
						message.execute(context);
					} catch (final UnknownMessageException e) {
						LOGGER.error(String.format("received invalid message from client '%s'", e.getClientContext()));
						LOGGER.debug("\t\t" + e);
					} catch (final Exception e) {
						// severe error, but catch to prevent server from crashing
						LOGGER.error(String.format("error executing message '%s'", message));
						LOGGER.debug("\t\t" + e);
					}
				} else {
					ServerUtil.defaultSleep();
				}
			}
			context.updateWorld(secondsPerFrame);
		}
	}

	/**
	 * Changes how often the server invokes {@link WorldContext#updateWorld(double)} and how big the delta factor is (by recalculating
	 * seconds-per-frame).
	 * 
	 * @param framesPerSecond The number of frames per second (world updates per second).
	 * @see WorldServer
	 */
	public void setFramesPerSecond(final double framesPerSecond) {
		this.secondsPerFrame = 1d / framesPerSecond;
		LOGGER.info(String.format("setting frame per setting to: %s (delta factor now: %s)", framesPerSecond, secondsPerFrame));
	}

	/**
	 * Stops the Swift Socket Server by delegating this call to {@link SwiftSocketServer#stop()}.
	 */
	public void stop() {
		server.stop();
	}

	/**
	 * Sends a message to the client context stored in the message by delegating this call to
	 * {@link SwiftSocketServer#sendMessage(ServerMessageToClient)}.
	 * 
	 * @param serverMessage The message to send to the client associated with it.
	 */
	public void sendMessage(final ServerMessageToClient serverMessage) {
		server.sendMessage(serverMessage);
	}

	/**
	 * Send a message to all connected clients ignoring the client context in the message, by delegating this call to
	 * {@link SwiftSocketServer#broadcastMessage(ServerMessageToClient)}.
	 * 
	 * @param serverMessage The message to send to all clients (ignoring clientContext on the message).
	 */
	public void broadcastMessage(final ServerMessageToClient serverMessage) {
		server.broadcastMessage(serverMessage);
	}
}