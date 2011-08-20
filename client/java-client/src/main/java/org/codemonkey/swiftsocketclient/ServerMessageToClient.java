package org.codemonkey.swiftsocketclient;

import org.codemonkey.util.Executable;

/**
 * Defines a base for decodable and executable message types.
 * <p>
 * With this base class, a message can decode a <code>String</code> to fill itself. This interface is used to translate so called <a
 * href="http://gpwiki.org/index.php/Binary_Packet">Binary Packets</a>, or datagrams, into {@link Executable} messages. This means an
 * instance of {@link ClientMessageToServer} is very much a command-like object.
 * <p>
 * A response string needs to built up in the following format:<br />
 * message id (3), encoded string
 * <p>
 * The following example could of a login response: <code>0035admin7letmein</code><br />
 * The following example could of a pong notification: <code>999</code><br />
 * <p>
 * It is up to the implementing class to decode this information and utilize it as an executable object.
 * <p>
 * 
 * @author Benny Bottema
 * @param <Controller> The type to which the executable can defer to (also see {@link Executable#execute(Object)}).
 * @see Executable The interface used by the server to execute the client message after it has been decoded.
 * @see SwiftSocketClient#addServerResponse(ServerMessageToClient)
 * @see SwiftSocketClient#getNextServerResponse()
 * @since 1.0
 */
public interface ServerMessageToClient<Controller> extends Executable<Controller, UnknownMessageException> {
	/**
	 * Populates itself by decoding the given message content. How this is done is entirely up to the sub class implementation, as long as
	 * it is decoded the same way it was encoded.
	 * 
	 * @param messageContent The string containing a message id and the message content to be decoded.
	 */
	public abstract void decode(final String messageContent);
}