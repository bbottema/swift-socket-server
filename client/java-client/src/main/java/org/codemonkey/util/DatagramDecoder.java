package org.codemonkey.util;

/**
 * Interface for marking objects as datagram decoders. An encoded strings is a string that contains a number that denotes the length of a
 * value followed by the value itself. Lists are prepended with the number of items in that list followed by a separator.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public interface DatagramDecoder {

	/**
	 * Populates the current implementation instance fields with values decoded from the given string (binary packet / datagram) of the
	 * current server response.
	 * 
	 * @param encodedString The string that can be decoded into a concrete class instance.
	 */
	void decode(StringBuilder encodedString);
}