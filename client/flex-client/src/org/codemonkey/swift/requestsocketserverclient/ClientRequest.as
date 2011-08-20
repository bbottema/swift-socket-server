package org.codemonkey.swift.requestsocketserverclient{
	
	import org.codemonkey.swift.util.Executable;
	
	public class ClientRequest implements Executable {
		public static const REQUESTCODE_LENGTH:Number = 3;
		
		public function encode():String {
			throw new Error("unimplemented method: encode()");
		}
		
		public function execute(controller:Object):void {
			throw new Error("unimplemented method: execute()");
		}
	}
}
