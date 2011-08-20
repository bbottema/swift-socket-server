package org.codemonkey.util;

import java.io.Serializable;

/**
 * Guarantees an object can produce and populate itself with a string value, not unlike deserialization.
 * <p>
 * This class is like {@link Serializable} except it's a much simpler version where the object itself provides the serialization code.
 * 
 * @author Benny Bottema
 * @param <T> The type of object produced by {@link #deserialize(String)}.
 * @see #deserialize(String)
 * @since 1.0
 */
public interface SimpleSerialization<T> {
	/**
	 * Makes the object convert itself to a string representation.
	 * 
	 * @return A <code>String</code> representation of the current <code>Object</code> that can be used to recreate an instance of the same
	 *         type using {@link #deserialize(String)}.
	 */
	String serialize();

	/**
	 * The general contract of <code>deserialize</code> is:<br />
	 * Instance <code>T a</code> equals <code>T b</code> where <code>b = new T().deserialize(a.serialize())</code>.
	 * 
	 * @param value A String value as produced by {@link #serialize()}.
	 * @return A populated Object from a String produced by {@link #serialize()}.
	 */
	T deserialize(String value);
}