package org.codemonkey.swiftsocketserver;

/**
 * Exception type thrown when a client-to-server message was not recognized by its identifier. Contains a reference to the
 * {@link ClientContext} in question.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
@SuppressWarnings({ "javadoc", "serial" })
public final class UnknownMessageException extends RuntimeException {

	private final ClientContext clientContext;

	public UnknownMessageException(final ClientContext clientContext, final String errorMessage) {
		super(errorMessage);
		this.clientContext = clientContext;
	}

	public ClientContext getClientContext() {
		return clientContext;
	}
}