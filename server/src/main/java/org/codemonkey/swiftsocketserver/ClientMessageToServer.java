package org.codemonkey.swiftsocketserver;

import org.codemonkey.util.Executable;

/**
 * Defines a base for decodable and executable message types. This class also keeps a reference to the current {@link ClientContext} that
 * sent the message.
 * <p>
 * With this base class, a message can decode a <code>String</code> to fill itself. This interface is used to translate so called <a
 * href="http://gpwiki.org/index.php/Binary_Packet">Binary Packets</a>, or datagrams, into {@link Executable} messages. This means an
 * instance of {@link ClientMessageToServer} is very much a command-like object.
 * <p>
 * A response string needs to built up in the following format:<br />
 * message id (3), encoded string
 * <p>
 * The following example could of a login response: <code>0035admin7letmein</code><br />
 * The following example could of a pong notification: <code>{@value SwiftSocketServer#MESSAGE_ID_PINGPONG}</code><br />
 * <p>
 * It is up to the implementing class to decode this information and utilize it as an executable object.
 * <p>
 * 
 * @author Benny Bottema
 * @param <Controller> The type to which the executable can defer to (also see {@link Executable#execute(Object)}).
 * @see Executable The interface used by the server to execute the client message after it has been decoded.
 * @see SwiftSocketServer#addClientMessage(ClientMessageToServer)
 * @see SwiftSocketServer#getNextClientMessage()
 * @since 1.0
 */
public abstract class ClientMessageToServer<Controller> implements Executable<Controller, UnknownMessageException> {

	/**
	 * Holds the information about the whereabouts of the connected client that was responsible for the current message.
	 */
	private final ClientContext clientContext;

	/**
	 * Constructor; simply stores a reference to the client associated with this message.
	 * 
	 * @param clientContext The client associated with this message.
	 */
	protected ClientMessageToServer(final ClientContext clientContext) {
		this.clientContext = clientContext;
	}

	/**
	 * @return The id of the client associated with this message (more important for responding to client messages).
	 */
	protected final ClientContext getClientContext() {
		return clientContext;
	}

	/**
	 * Translates a message string or binary packet to the contents of the current {@link ClientMessageToServer} object, ready to be
	 * executed.
	 * 
	 * @param datagramMessage The binary packet that contains information specific for the current {@link ClientMessageToServer}.
	 */
	protected abstract void decode(final String datagramMessage);
}