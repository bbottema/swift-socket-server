package org.codemonkey.swiftsocketclient;

/**
 * Wrapper class of which instances substitute bad client-to-server messages. Created when decoding a message datagram failed.
 * <p>
 * The error is communicated when the message is being executed by the server, but does not throw an exception.
 * 
 * @see ServerHandler#createDecodedExecutableResponse(String)
 * @author Benny Bottema
 * @since 1.0
 */
class ServerMessageToClientInvalid implements ServerMessageToClient<Object> {

	private String message = "";

	private final Exception failureCause;

	public ServerMessageToClientInvalid(final ServerContext serverContext, final Exception failureCause) {
		this.failureCause = failureCause;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decode(final String message) {
		this.message = message;
	}

	/**
	 * Throws a {@link UnknownMessageException} with the current <code>ServerMessageToClientInvalid</code> instance.
	 */
	@Override
	public void execute(final Object server)
			throws UnknownMessageException {
		throw new UnknownMessageException(message, failureCause);
	}
}