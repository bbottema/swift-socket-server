package org.codemonkey.swiftsocketserver;

import org.apache.log4j.Logger;

/**
 * Message that handles graceful disconnection attempts by the client. This message is on the server level and therefore only interacts with
 * the {@link ClientHandler} and is filtered from the client message queue in the server.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
class ClientMessageToServerByeBye extends ClientMessageToServer<ClientHandler> {

	public ClientMessageToServerByeBye(final ClientContext clientContext) {
		super(clientContext);
	}

	/**
	 * Validates that there is indeed no content in this message, since only the message id is relevant.
	 */
	@Override
	protected final void decode(final String message) {
		if (message.length() > 0) {
			throw new RuntimeException("invalid byebye message, reason: message too long");
		}
	}

	/**
	 * Calls {@link ClientHandler#clientSaysByeBye()}.
	 */
	@Override
	public final void execute(final ClientHandler handler) {
		handler.clientSaysByeBye();
		Logger.getRootLogger().debug(String.format("client @%s said byebye", getClientContext()));
	}
}