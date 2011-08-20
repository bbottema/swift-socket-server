package org.codemonkey.swiftsocketserver;

/**
 * Message that decodes pong datagrams. This message is on the server level and therefore only interacts with the {@link ClientHandler} and
 * is filtered from the client messages queue in the server.
 * 
 * @author Benny Bottema
 * @see ClientMessageToServer
 * @since 1.0
 */
final class ClientMessageToServerPingPong extends ClientMessageToServer<ClientHandler> {

	public ClientMessageToServerPingPong(final ClientContext clientContext) {
		super(clientContext);
	}

	/**
	 * Only validates on zero length, since there is no extra information required for a pong notification.
	 * 
	 * @param messageContent The message string or binary packet that should be empty for <code>ClientMessageToServerPingPong</code>.
	 * @see ClientMessageToServer#decode(String)
	 */
	@Override
	protected void decode(final String messageContent) {
		if (messageContent.length() > 0) {
			throw new RuntimeException("invalid pong message, reason: message too long");
		}
	}

	/**
	 * Simply delegates the pong event to the {@link ClientHandler}.
	 * 
	 * @param handler The {@link ClientHandler} instance to which the pong notification can be delegated to.
	 * @see ClientHandler#pong()
	 * @see org.codemonkey.swiftsocketserver.ClientMessageToServer#execute(java.lang.Object)
	 */
	@Override
	public void execute(final ClientHandler handler) {
		handler.pong();
	}
}
