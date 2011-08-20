package org.codemonkey.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codemonkey.javareflection.FieldUtils;
import org.codemonkey.javareflection.FieldUtils.BeanRestriction;
import org.codemonkey.javareflection.FieldUtils.Visibility;
import org.codemonkey.javareflection.FieldWrapper;

/**
 * Helper utility for encoding (integers, strings etc.,lists), using java reflection.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public final class EncoderUtil {

	/**
	 * Separates the length of a value from its value. Needed to be able to occupy multiply characters for denoting the value's length.
	 */
	public static final String VALUE_SEPERATOR = "|";
	/**
	 * The <code>null</code> notation for datagrams.
	 */
	public static final String ENCODED_NULL = "-";

	/**
	 * // FIXME JavaDoc does not reflect function
	 * <p>
	 * Combines the length of a given string and the value into a datagram notation. <code>null</code> values are represented as '
	 * {@value #ENCODED_NULL}'.
	 * 
	 * @param values A list of values to encode in a datagram string.
	 * @return A sequence of encoded values.
	 * @see DatagramEncoder#encode()
	 */
	public static String encode(final Object... values) {
		final StringBuilder coded = new StringBuilder("");
		for (final Object value : values) {
			if (value != null) {
				final String encodedValue;
				if (value instanceof DatagramEncoder) {
					encodedValue = ((DatagramEncoder) value).encode();
					coded.append(encodedValue);
				} else if (value instanceof SimpleSerialization<?>) {
					encodedValue = ((SimpleSerialization<?>) value).serialize();
					coded.append(encodedValue.length() + VALUE_SEPERATOR + encodedValue);
				} else if (value instanceof Collection) {
					final Collection<?> collection = (Collection<?>) value;
					encodedValue = encode(collection.toArray());
					coded.append(collection.size() + VALUE_SEPERATOR + encodedValue);
				} else {
					encodedValue = String.valueOf(value);
					coded.append(encodedValue.length() + VALUE_SEPERATOR + encodedValue);
				}
			} else {
				coded.append(ENCODED_NULL);
			}
		}
		return coded.toString();
	}

	/**
	 * Used to encode a list as a list (containing objects) instead of a collection of individual objects, like {@link #encode(Object...)} .
	 * <p>
	 * Combines the size of a given list and the value into a datagram notation. <code>null</code> values are represented as '
	 * {@value #ENCODED_NULL}'.
	 * 
	 * @param <T> A {@link DatagramEncoder} sub type that can encode itself to a String.
	 * @param encoders A list of {@link DatagramEncoder} instances to encode in a datagram string.
	 * @return A sequence of encoded integers.
	 * @see DatagramEncoder#encode()
	 */
	public static <T extends DatagramEncoder> String encode(final List<T> encoders) {
		if (encoders.contains(null)) {
			throw new RuntimeException("cannot encode a null list-item, only properties are allowed to be null");
		}
		final StringBuilder coded = new StringBuilder(encoders.size() + VALUE_SEPERATOR);
		for (final DatagramEncoder encoder : encoders) {
			coded.append(encoder.encode());
		}
		return coded.toString();
	}

	/**
	 * Reflectively resolves all fields on the subject which have a <em>getter</em> method of any type of visibility.
	 * <p>
	 * The <code>boundaryMarkerClass</code> argument controls how far up the tree we need to go to find fields to include in the encoding.
	 * You may use <code>reflectiveEncode(subject, subject.getClass());</code> to only encode the fields declared by the subject's class
	 * itself.
	 * 
	 * @param subject The object on which to invoke field queries.
	 * @param boundaryMarkerClass Needed to be able to get fields from super classes of a subject as well if required.
	 * @return A datagram encoded string containing all fields of the <code>boundaryMarkerClass</code>.
	 */
	public static String reflectiveEncode(final Object subject, final Class<?> boundaryMarkerClass) {
		final Map<Class<?>, List<FieldWrapper>> fields = FieldUtils.collectFields(boundaryMarkerClass, boundaryMarkerClass,
				EnumSet.allOf(Visibility.class), EnumSet.of(BeanRestriction.YES_GETTER));
		final List<Object> values = new LinkedList<Object>();
		for (final List<FieldWrapper> fieldWrapperList : fields.values()) {
			for (final FieldWrapper fieldWrapper : fieldWrapperList) {
				try {
					values.add(fieldWrapper.getGetter().invoke(subject));
				} catch (final IllegalArgumentException e) {
					final String msg = String.format("unable to encode property '%s'", fieldWrapper.getField().getName());
					throw new RuntimeException(msg, e);
				} catch (final IllegalAccessException e) {
					final String msg = String.format("unable to encode property '%s'", fieldWrapper.getField().getName());
					throw new RuntimeException(msg, e);
				} catch (final InvocationTargetException e) {
					final String msg = String.format("unable to encode property '%s'", fieldWrapper.getField().getName());
					throw new RuntimeException(msg, e);
				}
			}
		}
		return encode(values.toArray());
	}
}