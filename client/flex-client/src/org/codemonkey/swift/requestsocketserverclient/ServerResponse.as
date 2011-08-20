package org.codemonkey.swift.requestsocketserverclient{
	
	import org.codemonkey.swift.util.Executable;
	
	public class ServerResponse implements Executable {
		public static const REQUESTCODE_LENGTH:Number = 3;
		
		public function decode(requestStr:String):void {
			throw new Error("unimplemented method: decode()");
		}
		
		public function execute(controller:Object):void {
			throw new Error("unimplemented method: execute()");
		}
	}
}
