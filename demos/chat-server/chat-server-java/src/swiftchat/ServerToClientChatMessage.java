package swiftchat;

import org.codemonkey.swiftsocketserver.ClientContext;
import org.codemonkey.swiftsocketserver.ServerMessageToClient;

/**
 * @author Benny Bottema
 */
public class ServerToClientChatMessage extends ServerMessageToClient {

	private final String message;

	protected ServerToClientChatMessage(final ClientContext clientContext, final String message) {
		super(clientContext);
		this.message = message;
	}

	@Override
	public String encode() {
		return message;
	}
}