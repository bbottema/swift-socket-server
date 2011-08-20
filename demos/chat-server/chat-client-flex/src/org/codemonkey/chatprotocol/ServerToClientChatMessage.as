package org.codemonkey.chatprotocol {
	
	import org.codemonkey.ChatClient;
	import org.codemonkey.swift.requestsocketserverclient.ServerResponse;
	
	/**
	 * This class is used to define a vocabulary with the server. The server should know the exact same 
	 * type (as in: the server should be able to encode a message the same way the client decodes it).
	 */
	public class ServerToClientChatMessage extends ServerResponse {
		
		private var message:String;
		
		/**
		 * Convert the encoded string into whatever object it represents. In this case it should be 
		 * converted to a chat message, which is of type string, so no decoding, conversion or deserialization needed.
		 */
		public override function decode(requestStr:String):void {
			this.message = requestStr;
		}

		/**
		 * Once the Swift client framework processed the request from an encoded message into an executable 'command',
		 * it is made available through the method client.getNextServerResponse() (please see serverResponseHandler in ChatClient.as).
		 * 
		 * This way this Executable object is invoked and the invoker, the ChatClient, can pass in a reference to itself, the 
		 * controller as seen here.
		 */
		public override function execute(controller:Object):void {
			const chatClient:ChatClient = controller as ChatClient;
			chatClient.updateDiscussion(message);
		}
	}
}
