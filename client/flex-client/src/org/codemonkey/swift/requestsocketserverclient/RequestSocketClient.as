package org.codemonkey.swift.requestsocketserverclient{

	import flash.events.ProgressEvent;
	import flash.net.Socket;
	import flash.utils.Dictionary;
	import flash.utils.getDefinitionByName;
	import flash.utils.getQualifiedClassName;

	public class RequestSocketClient {

		private static const EOF:String = "\n\0";
		private var host:String;
		private var port:Number;

		private var responseTypes:Dictionary;
		private var requestIds:Dictionary;
		private var serverResponses:Array;

		private var socket:Socket;

		public function RequestSocketClient(host:String, port:Number):void {
			this.host = host;
			this.port = port;
			responseTypes = new Dictionary();
			requestIds = new Dictionary();
			serverResponses = new Array();
			
			registerRequest(999, RequestPingPong);
			registerRequest(998, RequestByeBye);
			
			registerResponse(999, ResponsePingPong);
			registerResponse(998, ResponseByeBye);
		}

		public function registerRequest(requestId:Number, requestType:Class):void {
			requestIds[requestType] = requestId;
		}

		public function registerResponse(responseId:Number, responseType:Class):void {
			responseTypes[responseId] = responseType;
		}

		public function start():void {
			socket = new Socket(host,port);
			socket.addEventListener(ProgressEvent.SOCKET_DATA, handleResponse);
		}

		private function handleResponse(event:ProgressEvent):void {
			const rawResponseStr:String = socket.readUTFBytes(socket.bytesAvailable);
			// exclude the newline \n character
			const responseStr:String = rawResponseStr.substring(0, rawResponseStr.length - 1);
			const response:ServerResponse = resolveResponse(responseStr);
			if (response is ResponsePingPong) {
				const responsePingPong:ResponsePingPong = response as ResponsePingPong;
				responsePingPong.execute(this);
			} else if (response is RequestByeBye) {
				const requestByeBye:RequestByeBye = response as RequestByeBye;
				requestByeBye.execute(this);
			} else {
				serverResponses.push(response);
			}
		}

		public function hasServerResponses():Boolean {
			return serverResponses.length > 0;
		}

		public function getNextServerResponse():ServerResponse {
			return serverResponses.shift();
		}

		protected function resolveResponse(responseMessage:String):ServerResponse {
			const requestId:Number = new Number(responseMessage.substring(0, ServerResponse.REQUESTCODE_LENGTH));
			const responseBody:String = responseMessage.substring(ServerResponse.REQUESTCODE_LENGTH);
			const responseType:Class = responseTypes[requestId];
			trace("receiving response from server: " + requestId + ": " + responseType);
			const serverResponse:ServerResponse = new responseType();
			serverResponse.decode(responseBody);
			return serverResponse;
		}

		public function pong():void {
			const request:ClientRequest = new RequestPingPong();
			sendRequest(request);
		}

		public function sendRequest(clientRequest:ClientRequest):void {
			var requestType:Class = getDefinitionByName(getQualifiedClassName(clientRequest)) as Class;
			sendClientRequest(socket, requestIds[requestType], clientRequest);
		}
		
		private function sendClientRequest(server:Socket, requestId:Number, clientRequest:ClientRequest):void {
			trace("sending request to server: " + requestId + ": " + getQualifiedClassName(clientRequest));
			var requestCode:String = requestId.toString();
			requestCode = (requestId < 10 ? '0' : '') + requestCode;
			requestCode = (requestId < 100 ? '0' : '') + requestCode;
			server.writeUTFBytes(requestCode + clientRequest.encode() + EOF);
			socket.flush();
		}

		public function stop():void {
			trace("waving server goodbye");
			sendRequest(new RequestByeBye());
		}

		public function byebye():void {
			trace("server acknowledged, stopping request client");
			socket.close();
			socket = null;
		}

		public function isRunning():Boolean {
			return socket != null;
		}
	}
}
