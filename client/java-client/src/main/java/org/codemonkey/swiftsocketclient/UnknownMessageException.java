package org.codemonkey.swiftsocketclient;

/**
 * Exception type thrown when a client-to-server message was not recognized by its identifier. Contains a reference to the
 * {@link ServerContext} in question.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
@SuppressWarnings({ "javadoc", "serial" })
public final class UnknownMessageException extends RuntimeException {

	private final String invalidMessage;

	public UnknownMessageException(final String message, final Exception failureCause) {
		super(String.format("%s (%s)", message, failureCause));
		this.invalidMessage = message;
	}

	public String getInvalidMessage() {
		return invalidMessage;
	}
}