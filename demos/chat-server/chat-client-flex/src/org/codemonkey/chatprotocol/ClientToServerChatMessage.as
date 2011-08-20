package org.codemonkey.chatprotocol {
	
	import org.codemonkey.swift.requestsocketserverclient.ClientRequest;

	/**
	 * This class is used to define a vocabulary with the server. The server should know the exact same 
	 * type (as in: the server should be able to decode a message the same way the client encoded it).
	 */
	public class ClientToServerChatMessage extends ClientRequest {
		
		private var chatMessage:String;
		
		public function ClientToServerChatMessage(chatMessage:String):void {
			this.chatMessage = chatMessage;
		}
		
		/**
		 * This method is called by the Swift client framework when it's sending a request to the server. 
		 * Here you make a string representation of whatever it is you want to send to the server.
		 * 
		 * In this case this is a chatmessage, which already is a String, so no encoding, conversion or serialization 
		 * needed.
		 */
		public override function encode():String {
			return chatMessage;
		}
	}
}
