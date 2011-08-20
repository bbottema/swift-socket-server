package swiftchat;

import org.codemonkey.swiftsocketserver.ClientContext;
import org.codemonkey.swiftsocketserver.ClientMessageToServer;
import org.codemonkey.swiftsocketserver.ServerUtil;
import org.codemonkey.swiftsocketserver.SwiftSocketServer;

/**
 * Starts a new chat server on port 4444 (default is TCP, but UDP works just as well).
 * 
 * @author Benny Bottema
 */
public class ChatServerDemo {

	/**
	 * Entry point to run the chat server demo.
	 */
	public static void main(final String[] args) {
		System.out.println("-----------------------");
		System.out.println("- Starting ChatServer -");
		System.out.println("-----------------------");
		new ChatServerDemo();
	}

	/**
	 * This is it, the server that does the magic
	 */
	private final SwiftSocketServer server;

	/**
	 * Creates a new server running on port 4444 and registers two message types with the server that we expect to communicate with clients:
	 * <ul>
	 * <li>{@link ClientToServerChatMessage}: chat messages we receive from clients</li>
	 * <li>{@link ServerToClientChatMessage}: chat messages we send to clients</li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	private ChatServerDemo() {
		server = new SwiftSocketServer(4444);
		server.registerClientMessageToServerType(1, ClientToServerChatMessage.class);
		server.registerServerMessageToClientId(1, ServerToClientChatMessage.class);
		server.start();

		// The following while loop manually processes all received client messages
		// The World Server extension does this for you

		// NOTE: you can skip this while loop and let the server execute them (this approached is used in the chat demo client)
		while (server.isRunning()) {
			ServerUtil.defaultSleep(); // we don't need to use 100% cpu
			if (server.hasClientMessages()) {
				@SuppressWarnings("rawtypes")
				final ClientMessageToServer request = server.getNextClientMessage();
				try {
					request.execute(this);
				} catch (final Exception e) {
					// severe error, but catch to prevent server from crashing
					System.err.println(String.format("error executing request '%s' - %s", request.getClass(), e.getMessage()));
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Called by {@link ClientToServerChatMessage#execute(ChatServerDemo)} when a client sends a chat message to this server. The server
	 * simply broadcasts this message to all connected clients using {@link ServerToClientChatMessage} (by calling
	 * {@link SwiftSocketServer#sendMessage(org.codemonkey.swiftsocketserver.ServerMessageToClient)}).
	 * 
	 * @param fromClient The client that is responsible for this method being called on the server.
	 * @param message The chat message actually send by the client.
	 */
	public void updateDiscussion(final ClientContext fromClient, final String message) {
		for (final ClientContext userContext : server.getAllClientContexts()) {
			if (userContext.isActive()) {
				server.sendMessage(new ServerToClientChatMessage(userContext, message));
			}
		}
	}
}