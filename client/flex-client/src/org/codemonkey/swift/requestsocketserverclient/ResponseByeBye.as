package org.codemonkey.swift.requestsocketserverclient {

	public class ResponseByeBye extends ServerResponse {
		public override function decode(requestStr:String):void {
			if (requestStr.length > 0) {
				throw new UnknownRequestError("invalid byebye response, reason: message too long");
			}
		}

		public override function execute(controller:Object):void {
			trace("server shut down");
		}
	}
}
