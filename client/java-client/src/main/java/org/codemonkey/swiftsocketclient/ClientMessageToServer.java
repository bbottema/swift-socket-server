package org.codemonkey.swiftsocketclient;

import org.codemonkey.util.DatagramEncoder;

/**
 * Provides a default {@link DatagramEncoder} implementation that returns an empty string. The {@link #encode()} method implemented can be
 * used (by not overriding it in sub classes) when only the message id is important (for example with Ping Pong messages, which have no
 * content).
 * <p>
 * With this base class a message may encode itself to a <code>String</code>. This interface is used to translate messages into so called <a
 * href="http://gpwiki.org/index.php/Binary_Packet">Binary Packets</a>, or datagrams.
 * <p>
 * A response string needs to built up in the following format:<br />
 * message code (3), encoded string
 * <p>
 * The following example could of a login response: <code>005OK</code><br />
 * The following example could of a pong notification: <code>999</code><br />
 * 
 * @author Benny Bottema
 * @see SwiftSocketClient#addServerResponse(ServerMessageToClient)
 * @see SwiftSocketClient#getNextServerResponse()
 * @see DatagramEncoder#encode()
 * @since 1.0
 */
public abstract class ClientMessageToServer implements DatagramEncoder {

	/**
	 * Default implementation which returns an empty string. This is useful for messages that have no parameters, just a message code.
	 * 
	 * @see DatagramEncoder#encode()
	 */
	@Override
	public String encode() {
		return "";
	}
}