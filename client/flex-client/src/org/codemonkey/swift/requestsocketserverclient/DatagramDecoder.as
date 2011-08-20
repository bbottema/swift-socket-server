package org.codemonkey.swift.requestsocketserverclient {
	import org.codemonkey.swift.util.StringBuilder;
	
	/**
	 * Interface for marking objects as datagram decoders. An encoded strings is a string that contains a number that denotes the length of a
	 * value followed by the value itself. Lists are prepended witht the number of items in that list followed by a seperator.
	 * 
	 * @author Benny Bottema
	 */
	public interface DatagramDecoder {
		/**
		 * Creates a concrete implementation decoded from the given string (binary packet / datagram) of the current server response.
		 * 
		 * @param encodedString The string that can be decoded into a concrete class instance.
		 * @return The remaining datagram left to be decoded.
		 */
		function decode(encodedString:StringBuilder):void;
	}
}