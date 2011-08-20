package org.codemonkey.swiftsocketserver;

import org.codemonkey.util.DatagramEncoder;

/**
 * Provides a default {@link DatagramEncoder} implementation that returns an empty string. The {@link #encode()} method implemented can be
 * used (by not overriding it in sub classes) when only the message id is important (for example with Ping Pong messages, which have no
 * content).
 * <p>
 * This class also contains a reference to the current {@link ClientContext} which will receive this message.
 * <p>
 * With this base class a message may encode itself to a <code>String</code>. This interface is used to translate messages into so called <a
 * href="http://gpwiki.org/index.php/Binary_Packet">Binary Packets</a>, or datagrams.
 * <p>
 * A response string needs to built up in the following format:<br />
 * message id (3), encoded string
 * <p>
 * The following example could of a login response: <i>0052OK</i><br />
 * The following example could of a pong notification: <i>{@value SwiftSocketServer#MESSAGE_ID_PINGPONG}</i><br />
 * 
 * @author Benny Bottema
 * @see SwiftSocketServer#addClientMessage(ClientMessageToServer)
 * @see SwiftSocketServer#getNextClientMessage()
 * @see DatagramEncoder#encode()
 * @since 1.0
 */
public abstract class ServerMessageToClient implements DatagramEncoder {

	/**
	 * Holds the information about the whereabouts of the connected client that was responsible for the current message.
	 */
	private final ClientContext clientContext;

	/**
	 * Constructor; simply stores a reference to the client associated with this message.
	 * 
	 * @param clientContext The client associated with this message.
	 */
	protected ServerMessageToClient(final ClientContext clientContext) {
		this.clientContext = clientContext;
	}

	/**
	 * @return {@link #clientContext}
	 */
	public ClientContext getClientContext() {
		return clientContext;
	}

	/**
	 * Default implementation which returns an empty string. This is useful for messages that have no parameters, just a message code.
	 * 
	 * @return An empty string.
	 * @see DatagramEncoder#encode()
	 */
	@Override
	public String encode() {
		return "";
	}
}