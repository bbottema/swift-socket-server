package org.codemonkey.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codemonkey.javareflection.FieldUtils;
import org.codemonkey.javareflection.FieldUtils.BeanRestriction;
import org.codemonkey.javareflection.FieldUtils.Visibility;
import org.codemonkey.javareflection.FieldWrapper;
import org.codemonkey.javareflection.JReflect;
import org.codemonkey.javareflection.ValueConverter;
import org.codemonkey.javareflection.ValueConverter.IncompatibleTypeException;

/**
 * Helper utility for interpreting encoded strings, including complete reflective decoding of objects. In effect this behavior mimics
 * serialization (see {@link Serializable}), except that the result is a hyper portable and ultra small serialized string.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public final class DecoderUtil {

	/**
	 * Separates the length of a value from its value. Needed to be able to occupy multiply characters for denoting the value's length.
	 */
	private static final String VALUE_SEPERATOR = "|";
	/**
	 * The <code>null</code> notation for datagrams.
	 */
	private static final String ENCODED_NULL = "-";

	/**
	 * Decodes a datagram into a list of objects, where the first token should be the number of encoded objects. For each count a decodable
	 * object {@link DatagramDecoder} or 'common' type is reflectively being instantiated, which in case of a <code>DatagramDecoder</code>
	 * then becomes responsible for decoding a nested part of the encoded datagram value.
	 * 
	 * @param <T> Any subtype that is either a <code>DatagramDecoder</code> type or a common type (String, Number, char etc. etc.).
	 * @param encodedString The datagram encoded value, starting with the number of items, where each item may consist of a group of items
	 *            (in case of a <code>DatagramDecoder</code>).
	 * @param _class Any subtype that is either a <code>DatagramDecoder</code> type or a common type (String, Number, char etc. etc.).
	 * @return A list of decoded objects, which can be instances of a <code>DatagramDecoder</code> implementation, or a list of common
	 *         objects (a list of <code>Boolean</code> values for example).
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> List<T> genericDecodeList(final StringBuilder encodedString, final Class<T> _class) {
		if (!encodedString.toString().startsWith(ENCODED_NULL)) {
			final List<T> decodedObjects = new ArrayList<T>();
			final int count = countNext(encodedString);
			for (int i = 0; i < count; i++) {
				if (DatagramDecoder.class.isAssignableFrom(_class)) {
					final T decoder = JReflect.newInstanceSimple(_class);
					((DatagramDecoder) decoder).decode(encodedString);
					decodedObjects.add(decoder);
				} else if (ValueConverter.isCommonType(_class)) { // Detect if we need to do manual common conversion, which is only
																	// applicable if there is no parent object to set the value on
																	// directly (in case of a Collection). So with things like lists we
																	// need to decode common types ourselves, since you can't for
																	// example reflectively fill a String object unless you make a
																	// detection script that detects these exceptions per common type
																	// exception so that you can pass it into the constructor or
																	// whatever that type needs (which we won't because this is complex
																	// enough already).
					final String stringValue = extractValue(encodedString);
					decodedObjects.add((T) ValueConverter.convert(stringValue, _class));
				} else {
					final T subject = JReflect.newInstanceSimple(_class);
					reflectiveDecode(subject, encodedString, _class);
					decodedObjects.add(subject);
				}
			}
			return decodedObjects;
		} else {
			// no support for null values within collection (we won't be able to detect whether the list should be null or the item
			// within the collection)
			final String value = extractValue(encodedString);
			assert value == null : "extractValue should have returned null!";
			return null;
		}
	}

	/**
	 * Decodes a datagram reflectively by inspecting the given <code>Class</code> for all it fields, which are assumed to take part in the
	 * decoding process. The values to apply to these fields are determined by {@link #extractValues(StringBuilder, int)}.<br />
	 * <br />
	 * This works only if the fields are encoded in the same order, which is only guaranteed by reflectively encoding using
	 * {@link #reflectiveDecode(Object, StringBuilder, Class)}<br />
	 * <br />
	 * 
	 * @param subject The object that reflectively receives all the values found.
	 * @param encodedString The encoded datagram value.
	 * @param specificClass The specific type that should be introspected for its fields.
	 * @see #convertValue(String, Class)
	 */
	public static void reflectiveDecode(final Object subject, final StringBuilder encodedString, final Class<?> specificClass) {
		final Map<Class<?>, List<FieldWrapper>> fieldMap = FieldUtils.collectFields(specificClass, specificClass,
				EnumSet.allOf(Visibility.class), EnumSet.of(BeanRestriction.YES_SETTER));
		final List<FieldWrapper> fields = fieldMap.get(specificClass);
		for (int i = 0; i < fields.size(); i++) {
			final FieldWrapper fieldWrapper = fields.get(i);
			final Field field = fieldWrapper.getField();
			final Object convertedValue;
			if (Collection.class.isAssignableFrom(field.getType())) {
				convertedValue = genericDecodeList(encodedString,
						(Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
			} else {
				convertedValue = convertValue(extractValue(encodedString), field.getType());
			}
			try {
				fieldWrapper.getSetter().invoke(subject, convertedValue);
			} catch (final IllegalArgumentException e) {
				final String msg = String.format("unable to encode property '%s'", field.getName());
				throw new RuntimeException(msg, e);
			} catch (final IllegalAccessException e) {
				final String msg = String.format("unable to encode property '%s'", field.getName());
				throw new RuntimeException(msg, e);
			} catch (final InvocationTargetException e) {
				final String msg = String.format("unable to encode property '%s'", field.getName());
				throw new RuntimeException(msg, e);
			}
		}
	}

	/**
	 * Performs a manual check for {@link SimpleSerialization} to convert the value, otherwise a reflection sweep is performed to convert
	 * the value, if possible at all.
	 * 
	 * @param value The value to convert.
	 * @param targetType The type the value should be converted to.
	 * @return A converted value using {@link SimpleSerialization} or reflection.
	 * @see ValueConverter#convert(String, Class) Used to find the right converted value for the encoded datagram token.
	 */
	private static Object convertValue(final String value, final Class<?> targetType) {
		if (Arrays.asList(targetType.getInterfaces()).contains(SimpleSerialization.class)) {
			try {
				if (targetType.isEnum()) {
					final Object[] consts = targetType.getEnumConstants();
					return ((SimpleSerialization<?>) consts[0]).deserialize(value);
				} else {
					final SimpleSerialization<?> object = (SimpleSerialization<?>) targetType.newInstance();
					return object.deserialize(value);
				}
			} catch (final InstantiationException e) {
				final String msg = "%s targetType does support reflective conversion ([%s] missing no-args constructor?)";
				throw new IncompatibleTypeException(String.format(msg, SimpleSerialization.class.getSimpleName(), targetType.getName()), e);
			} catch (final IllegalAccessException e) {
				final String msg = "%s targetType does support reflective conversion ([%s] missing public constructor?)";
				throw new IncompatibleTypeException(String.format(msg, SimpleSerialization.class.getSimpleName(), targetType.getName()), e);
			}
		}
		return ValueConverter.convert(value, targetType);
	}

	/**
	 * Shortcut method that calls {@link #extractValues(StringBuilder, int)} with argument of 1 and returns the first (and only) result
	 * value.
	 * 
	 * @param encodedString A datagram encoded string of which to interpret a single value from.
	 * @return The first singular encoded value of the given datagram encoded string.
	 * @see #extractValues(StringBuilder, int)
	 */
	public static String extractValue(final StringBuilder encodedString) {
		return extractValues(encodedString, 1).get(0);
	}

	/**
	 * Extracts a given number of values from the datagram and returns the remaining encoded values.
	 * 
	 * @param encodedString The datagram that contains at least the expected given number of encoded values.
	 * @param count The expected number of encoded values.
	 * @return The datagram minus the 'deserialized' values. Does <strong>consider</strong> lists sizes!
	 */
	public static List<String> extractValues(final StringBuilder encodedString, final int count) {
		final LinkedList<String> result = new LinkedList<String>();
		for (int i = 0; i < count; i++) {
			final int valueLength = countNext(encodedString);
			final String encodedValue = encodedString.substring(0, valueLength);
			result.add(encodedValue.equals(ENCODED_NULL) ? null : encodedValue);
			encodedString.delete(0, valueLength);
		}
		return result;
	}

	/**
	 * Returns the encoded length of the next token <em>and removes this indication so that the encoded string starts with the token for
	 * further processing</em>.
	 * 
	 * @param encodedString The string containing the datagram encoded values including length notations.
	 * @return The length of the next token.
	 */
	public static int countNext(final StringBuilder encodedString) {
		if (encodedString == null || encodedString.length() == 0) {
			return 0;
		} else if (encodedString.toString().startsWith(ENCODED_NULL)) {
			return 1;
		} else {
			final String count = encodedString.substring(0, encodedString.indexOf(VALUE_SEPERATOR));
			final int valueStart = encodedString.indexOf(VALUE_SEPERATOR) + 1;
			encodedString.delete(0, valueStart);
			return Integer.valueOf(count);
		}
	}

}