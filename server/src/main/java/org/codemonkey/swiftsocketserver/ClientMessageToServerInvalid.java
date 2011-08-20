package org.codemonkey.swiftsocketserver;

/**
 * Wrapper class of which instances substitute bad client-to-server messages. Created when decoding a message datagram failed.
 * <p>
 * The error is communicated when the message is being executed by the server, but does not throw an exception.
 * 
 * @author Benny Bottema
 * @see ClientHandler
 * @since 1.0
 */
class ClientMessageToServerInvalid extends ClientMessageToServer<Object> {

	private String message = "";

	private final Exception failureCause;

	public ClientMessageToServerInvalid(final ClientContext clientContext, final Exception failureCause) {
		super(clientContext);
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
	 * {@inheritDoc}
	 */
	@Override
	public void execute(final Object server)
			throws UnknownMessageException {
		throw new UnknownMessageException(getClientContext(), String.format("%s (%s)", message, failureCause));
	}
}