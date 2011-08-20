package org.codemonkey.swiftsocketclient;

/**
 * Represents a server Ping message and when executed responds with a Pong message via {@link ServerHandler#pong()}.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
final class ServerMessageToClientPingPong implements ServerMessageToClient<ServerHandler> {
	
	/**
	 * Validates that there is indeed no content in this message, since only the message id is relevant.
	 */
	@Override
	public void decode(final String messageContent) {
		if (messageContent.length() > 0) {
			throw new RuntimeException("invalid pong message, reason: message too long");
		}
	}

	/**
	 * Simply delegates the ping event to the {@link ServerHandler}.
	 * 
	 * @param handler The {@link ServerHandler} instance to which the pong notification can be delegated to.
	 * @see ServerHandler#pong()
	 * @see ServerMessageToClient#execute(Object)
	 */
	@Override
	public void execute(final ServerHandler handler) {
		handler.pong();
	}
}