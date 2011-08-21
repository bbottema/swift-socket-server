package swiftchat;

import org.codemonkey.swiftsocketserver.ClientContext;
import org.codemonkey.swiftsocketserver.ClientMessageToServer;

/**
 * For receiving a chat message from the client.
 */
public class ClientToServerChatMessage extends ClientMessageToServer<ChatServerDemo> {

	/**
	 * @see ClientMessageToServer#ClientMessageToServer
	 */
	public ClientToServerChatMessage(final ClientContext clientContext) {
		super(clientContext);
	}

	private String message;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void decode(final String requestStr) {
		message = requestStr;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(final ChatServerDemo controller) {
		controller.updateDiscussion(getClientContext(), message);
	}
}