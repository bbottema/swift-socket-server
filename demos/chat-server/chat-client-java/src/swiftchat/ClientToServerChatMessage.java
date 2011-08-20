package swiftchat;

import org.codemonkey.swiftsocketclient.ClientMessageToServer;

/**
 * This class is used to define a vocabulary with the server. The server should know the exact same 
 * type (as in: the server should be able to decode a message the same way the client encoded it).
 */
public class ClientToServerChatMessage extends ClientMessageToServer {
	
	private String chatMessage;
	
	public ClientToServerChatMessage(String chatMessage) {
		this.chatMessage = chatMessage;
	}
	
	/**
	 * This method is called by the Swift client framework when it's sending a request to the server. 
	 * Here you make a string representation of whatever it is you want to send to the server.
	 * 
	 * In this case this is a chat message, which already is a String, so no encoding, conversion or serialization 
	 * needed.
	 */
	@Override
	public String encode() {
		return chatMessage;
	}
}