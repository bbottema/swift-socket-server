package org.codemonkey.swift.requestsocketserverclient  {
	import org.codemonkey.swift.util.StringBuilder;
	import flash.utils.describeType;
	
	/**
	 * helper utility for decoding (integers, strings etc.,lists).
	 * 
	 * @author Benny Bottema
	 */
	public class DatagramUtil {

		/**
		 * Seperates the length of a value from its value. Needed to beable to occupy multiply characters for denoting the value's length.
		 */
		private static const VALUE_SEPERATOR:String = "|";
		/**
		 * The <code>null</code> notation for datagrams.
		 */
		private static const ENCODED_NULL:String = "-";

		public function DatagramUtil() {
			throw new Error("The utility class DatagramUtil cannot be instantiated!");
		}

		/**
		 * Extracts a given number of values from the datagram and returns the remaining encoded values.
		 * 
		 * @param encodedString The datagram that contains at least the expected given number of encoded values.
		 * @param count The expected number of encoded values.
		 * @return The datagram minus the 'deserialized' values. Does <strong>consider</strong> lists sizes!
		 */
		public static function values(encodedString:StringBuilder, count:Number):Array {
			var result:Array = new Array();
			for (var i:Number = 0; i < count; i++) {
				var valueLength:Number = countNext(encodedString);
				var encodedValue:String = encodedString.substring(0, valueLength);
				result.push(encodedValue);
				encodedString.remove(0, valueLength);
			}
			return result;
		}

		public static function countNext(encodedString:StringBuilder):Number {
			if (encodedString == null || encodedString.length() == 0) {
				return 0;
			} else if (encodedString.indexOf(ENCODED_NULL) == 0) {
				return 1;
			} else {
				var count:String = encodedString.substring(0, encodedString.indexOf(VALUE_SEPERATOR));
				var valueStart:Number = encodedString.indexOf(VALUE_SEPERATOR) + 1;
				encodedString.remove(0, valueStart);
				return new Number(count);
			}
		}

		public static function genericDecodeList(encodedString:StringBuilder, _class:Class):Array {
			var decodedObjects:Array = new Array();
			var count:Number = countNext(encodedString);
			for (var i:Number = 0; i < count; i++) {
				var decoder:DatagramDecoder = new _class();
				decoder.decode(encodedString);
				decodedObjects.add(decoder);
			}
			return decodedObjects;
		}

		public static function reflectiveDecode(subject:Object, encodedString:StringBuilder, specificClass:Class):void {
			var classAsXML:XML = describeType(specificClass);
			var properties:Array = classAsXML.variable;
			var values:Array = values(encodedString, properties.length);

			for (var i:Number = 0; i < properties.length; i++) {
				var property:Object = properties.get(i);
				var convertedValue:Object = convert(values.get(i), property.type);
				subject[property.name] = convertedValue;
			}
		}
		
		private static function convert(value:String, type:Class):Object {
			throw new Error("not implemented");
		}
	}
}