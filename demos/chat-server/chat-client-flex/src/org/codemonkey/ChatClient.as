package org.codemonkey {
	
	import flash.display.SimpleButton;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.text.TextField;
	import flash.ui.Keyboard;
	import org.codemonkey.swift.requestsocketserverclient.RequestSocketClient;
	import org.codemonkey.chatprotocol.*;
	
	import flash.display.MovieClip;
	import flash.net.Socket;
	import flash.events.ProgressEvent;

	/**
	 * This is a very basic chat client demo. There are two custom request types registered with 
	 * the requestsocketserver-client instance:
	 * 
	 * - ClientToServerChatMessage: used to send chat messages to the server
	 * - ServerToClientChatMessage: used to receive chat messages from the server
	 * 
	 * Both are registered with code 1, which means the server needs to know this code as well.
	 */
	public class ChatClient extends Sprite {
		
		/**
		 * This is the client we'll use to communicate with the server.
		 */
		private var client:RequestSocketClient;

		/**
		 * Chat messages *received from the server* so far.
		 */
		private var discussion:Array = new Array();
		
		// basic chat client GUI
		private var currentMessage:TextField;
		private var history:TextField;
		
		/**
		 * Starts the client and registers the Chat server-client vocabulary. Finally, it will 
		 * continuously listen to the client for server requests/responses to be executed in chat client 
		 * context.
		 */
		public function ChatClient():void {
			trace("starting Swift Chat Client demo");
			
			stageGui();
			
			client = new RequestSocketClient("localhost", 4444);
			client.registerRequest(1, ClientToServerChatMessage);
			client.registerResponse(1, ServerToClientChatMessage);
			client.start();
			
			stage.addEventListener(Event.ENTER_FRAME, serverResponseHandler);
		}
		
		/**
		 * Continuously checks if there is a server messages to execute.
		 */
		private function serverResponseHandler(e:Event):void {
			if (client.hasServerResponses()) {
				client.getNextServerResponse().execute(this);
			}
		}
		
		private function stageGui():void {
			var introText:TextField = new TextField();
			introText.text = "This is a *very* basic chat client: type some text and simply press 'Enter'.";
			introText.text = introText.text + "\n";
			introText.text = introText.text + "(if the text appears in the chatbox, the server works!)";
			introText.width = 500;
			introText.height = 40;
			introText.mouseEnabled = false;
			addChild(introText);
			
			history = new TextField();
			history.y = introText.height;
			history.width = 500;
			history.height = 100;
			history.border = true;
			history.mouseEnabled = false;
			addChild(history);
			
			currentMessage = new TextField();
			currentMessage.y = introText.height + history.height;
			currentMessage.width = 500;
			currentMessage.height = 20;
			currentMessage.type = "input";
			currentMessage.border = true;
			currentMessage.text = "";
			currentMessage.addEventListener(KeyboardEvent.KEY_DOWN, enterMessage);
			addChild(currentMessage);
		}
		
		/**
		 * When text is entered and the 'Enter' buttib pressed, a request is sent to the 
		 * server with this message.
		 */
		private function enterMessage(event:KeyboardEvent):void {
			if (event.keyCode == Keyboard.ENTER) {
				client.sendRequest(new ClientToServerChatMessage(currentMessage.text));
				currentMessage.text = "";
			}
		}
		
		/**
		 * Called from ServerToClientChatMessage.Execute() when a chat message was received from the server.
		 */
		public function updateDiscussion(message:String):void {
			discussion.push(message);
			
			// the text field is only large enough to show 6 lines at a time and there is no scrollbar
			var lastSixMessages:Array = discussion.slice(Math.max(discussion.length - 6, 0), discussion.length);
			history.text = "";
			for each (var message:String in lastSixMessages) {
				history.appendText(message + "\n");
			}
		}
	}
}
