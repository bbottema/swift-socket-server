package org.codemonkey.swiftsocketclient;

/**
 * Message that handles graceful disconnection attempts by the client. There will be no response to this message.
 * <p>
 * Provides no additional implementation details, as there is no content associated with a 'Bye Bye' message; only the message id is
 * relevant.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
class ClientMessageToServerByeBye extends ClientMessageToServer {
}