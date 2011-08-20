package org.codemonkey {
	
	import flash.display.SimpleButton;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.text.TextField;
	import flash.ui.Keyboard;
	import org.codemonkey.swift.requestsocketserverclient.RequestSocketClient;
	
	import flash.display.MovieClip;
	import flash.net.Socket;
	import flash.events.ProgressEvent;

	/**
	 * This is a very basic client demonstration. This client should connect to a Swift server with the PingPong feature turned on.
	 * This client will simply endlessly react to the server's Ping requests with a Pong response.
	 * 
	 * If you close the client the server should notice the client closed the connection and drop the client on the server side.
	 */
	public class PingPongClient extends Sprite {
		
		public function PingPongClient():void {
			trace("starting Swift PingPong Client demo");
			var client:RequestSocketClient = new RequestSocketClient("localhost", 4444);
			client.start();
		}
	}
}
