package org.codemonkey.swift.util {

	public class StringBuilder {
		
		private var value:String;
		
		public function StringBuilder(value:String) {
			this.value = value;
		}
		
		public function toString():String {
			return value;
		}
		
		public function remove(start:Number, end:Number):void {
			value = value.substring(0, start) + value.substring(end, value.length);
		}
		
		public function substring(start:Number, end:Number):String {
			return value.substring(start, end);
		}
		
		public function length():Number {
			return value.length;
		}
		
		public function indexOf(part:String):Number {
			return value.indexOf(part);
		}
	}
}