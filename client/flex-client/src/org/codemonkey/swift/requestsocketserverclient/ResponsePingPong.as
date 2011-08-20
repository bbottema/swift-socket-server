package org.codemonkey.swift.requestsocketserverclient {
	
	public class ResponsePingPong extends ServerResponse{
		public override function decode(requestStr:String):void {
			if (requestStr.length > 0) {
				throw new UnknownRequestError("invalid ping request, reason: message too long");
			}
		}

		public override function execute(controller:Object):void {
			const requestSocketClient:RequestSocketClient = controller as RequestSocketClient;
			requestSocketClient.pong();
		}
	}
}
