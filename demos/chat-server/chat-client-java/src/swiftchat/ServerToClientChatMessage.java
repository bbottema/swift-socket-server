package swiftchat;

import org.codemonkey.swiftsocketclient.ServerMessageToClient;

/**
 * For receiving a chat message from the server.
 */
public class ServerToClientChatMessage implements ServerMessageToClient<ChatClient> {

	private String message;
	
	/**
	 * Convert the encoded string into whatever object it represents. In this case it should be 
	 * converted to a chat message, which is of type string, so no decoding, conversion or deserialization needed.
	 */
	@Override
	public void decode(String requestStr) {
		this.message = requestStr;
	}

	/**
	 * Once the Swift client framework processed the request from an encoded message into an executable 'command',
	 * it is made available through the method client.getNextServerResponse() (please see serverResponseHandler in ChatClient.as).
	 * 
	 * This way this Executable object is invoked and the invoker, the ChatClient, can pass in a reference to itself, the 
	 * controller as seen here.
	 */
	@Override
	public void execute(ChatClient chatClient) {
		chatClient.updateDiscussion(message);
	}
}