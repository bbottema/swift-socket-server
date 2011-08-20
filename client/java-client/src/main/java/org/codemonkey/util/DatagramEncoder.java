package org.codemonkey.util;

/**
 * Interface for marking objects encodable as datagram. An encoded strings is a string that contains a number that denotes the length of a
 * value followed by the value itself.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public interface DatagramEncoder {

	/**
	 * Creates a response string (binary packet) of the current server response. It does not know the response code, which is assigned
	 * externally and prepended to the response string created by this method.
	 * 
	 * @return The response <code>String</code> or binary packet, without the response code.
	 */
	String encode();
}