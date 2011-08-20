package swiftchat;

import org.codemonkey.swiftsocketserver.ClientContext;
import org.codemonkey.swiftsocketserver.ClientMessageToServer;
import org.codemonkey.swiftsocketserver.UnknownMessageException;

/**
 * @author Benny Bottema
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
	public void execute(final ChatServerDemo controller)
			throws UnknownMessageException {
		controller.updateDiscussion(getClientContext(), message);
	}
}