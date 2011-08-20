package org.codemonkey.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

/**
 * @author Benny Bottema
 */
@SuppressWarnings("javadoc")
public class DatagramEncoderUtilTest {

	@Test
	public void testEncodeIntegers() {
		final Integer int1 = 4;
		final Integer int2 = 566;
		final Integer int3 = 0;
		final Integer int4 = null;
		final Integer int5 = 9999;
		final String encoded1 = EncoderUtil.encode(int1);
		final String encoded2 = EncoderUtil.encode(int2);
		final String encoded3 = EncoderUtil.encode(int3);
		final String encoded4 = EncoderUtil.encode(int4);
		final String encoded5 = EncoderUtil.encode(int5);
		final String encoded6 = EncoderUtil.encode(int1, int2, int3, int4, int5);

		assertEquals("1|4", encoded1);
		assertEquals("3|566", encoded2);
		assertEquals("1|0", encoded3);
		assertEquals("-", encoded4);
		assertEquals("4|9999", encoded5);
		assertEquals("1|43|5661|0-4|9999", encoded6);
	}

	@Test
	public void testEncodeNumbersAsStrings() {
		final String string1 = "4";
		final String string2 = "566";
		final String string3 = "0";
		final String string4 = null;
		final String string5 = "9999";

		final String encoded1 = EncoderUtil.encode(string1);
		final String encoded2 = EncoderUtil.encode(string2);
		final String encoded3 = EncoderUtil.encode(string3);
		final String encoded4 = EncoderUtil.encode(string4);
		final String encoded5 = EncoderUtil.encode(string5);
		final String encoded6 = EncoderUtil.encode(string1, string2, string3, string4, string5);

		assertEquals("1|4", encoded1);
		assertEquals("3|566", encoded2);
		assertEquals("1|0", encoded3);
		assertEquals("-", encoded4);
		assertEquals("4|9999", encoded5);
		assertEquals("1|43|5661|0-4|9999", encoded6);
	}

	@Test
	public void testEncodeStrings() {
		final String string1 = "this is a test";
		final String string2 = null;
		final String string3 = "uhm";
		final String string4 = "";

		final String encoded1 = EncoderUtil.encode(string1);
		final String encoded2 = EncoderUtil.encode(string2);
		final String encoded3 = EncoderUtil.encode(string3);
		final String encoded4 = EncoderUtil.encode(string4);
		final String encoded5 = EncoderUtil.encode(string1, string2, string3, string4);

		assertEquals("14|this is a test", encoded1);
		assertEquals("-", encoded2);
		assertEquals("3|uhm", encoded3);
		assertEquals("0|", encoded4);
		assertEquals("14|this is a test-3|uhm0|", encoded5);
	}

	@Test
	public void testEncodeMiscIncludingDatagramEncoderImplementor() {
		final String value1 = "this is a test";
		final Integer value2 = 338;
		final DatagramEncoder value3 = new TestableEncoderDecoderSimpleValue("blahblah");
		final SimpleSerialization<TestableSimpleSerialization> value4 = new TestableSimpleSerialization().deserialize("testvalue");
		final Object value5 = null;

		final String encoded = EncoderUtil.encode(value1, value2, value3, value4, value5);

		assertEquals("14|this is a test3|3388|blahblah9|testvalue-", encoded);
	}

	@Test
	public void testEncodeList() {
		final List<DatagramEncoder> encoders = new ArrayList<DatagramEncoder>();
		encoders.add(new TestableEncoderDecoderSimpleValue("44"));
		encoders.add(new TestableEncoderDecoderSimpleValue(null));
		encoders.add(null);
		encoders.add(new TestableEncoderDecoderSimpleValue("3"));
		try {
			EncoderUtil.encode(encoders);
			fail("should not be able to encode a null list-item");
		} catch (final RuntimeException e) {
			// OK
		}
		encoders.remove(null);
		final String encoded = EncoderUtil.encode(encoders);
		assertEquals("3|2|44-1|3", encoded);
		final List<TestableEncoderDecoderSimpleValue> decodedEncoders = DecoderUtil.genericDecodeList(new StringBuilder(encoded),
				TestableEncoderDecoderSimpleValue.class);
		assertEquals(encoders, decodedEncoders);
	}

	@Test
	public void testEncodeListNested() {
		final List<DatagramEncoder> encoders = new ArrayList<DatagramEncoder>();
		encoders.add(new TestableEncoderDecoderNestedList(Arrays.asList("44", "44")));
		encoders.add(new TestableEncoderDecoderNestedList(null));
		encoders.add(null);
		encoders.add(new TestableEncoderDecoderNestedList(Arrays.asList("3", "3")));
		try {
			EncoderUtil.encode(encoders);
			fail("should not be able to encode a null list-item");
		} catch (final RuntimeException e) {
			// OK
		}
		encoders.remove(null);
		final String encoded = EncoderUtil.encode(encoders);
		assertEquals("3|2|2|442|44-2|1|31|3", encoded);
		final List<TestableEncoderDecoderNestedList> decodedEncoders = DecoderUtil.genericDecodeList(new StringBuilder(encoded),
				TestableEncoderDecoderNestedList.class);
		assertEquals(encoders, decodedEncoders);
	}

	@Test
	public void testReflectiveEncode() {
		final TestableEncoderReflective subject = new TestableEncoderReflective();
		subject.intfield = 50;
		subject.stringfield = "testvalue";
		final String encodedValue = EncoderUtil.reflectiveEncode(subject, TestableEncoderReflective.class);
		assertEquals("2|509|testvalue", encodedValue);
	}

	public static class TestableEncoderDecoderSimpleValue implements DatagramEncoder, DatagramDecoder {
		private String value;

		public TestableEncoderDecoderSimpleValue() {
			// needed for reflective decoding
		}

		public TestableEncoderDecoderSimpleValue(final String encoding) {
			value = encoding;
		}

		@Override
		public void decode(final StringBuilder encodedString) {
			DecoderUtil.reflectiveDecode(this, encodedString, TestableEncoderDecoderSimpleValue.class);
		}

		@Override
		public String encode() {
			return EncoderUtil.reflectiveEncode(this, TestableEncoderDecoderSimpleValue.class);
		}

		@Override
		public String toString() {
			return value != null ? value.toString() : null;
		}

		@Override
		public boolean equals(final Object obj) {
			final TestableEncoderDecoderSimpleValue other = (TestableEncoderDecoderSimpleValue) obj;
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

		public void setValue(final String encoding) {
			value = encoding;
		}

		public String getValue() {
			return value;
		}
	}

	public static class TestableEncoderDecoderNestedList implements DatagramEncoder, DatagramDecoder {
		private List<String> values;

		public TestableEncoderDecoderNestedList() {
			// needed for reflective decoding
		}

		public TestableEncoderDecoderNestedList(final List<String> encoding) {
			values = encoding;
		}

		@Override
		public void decode(final StringBuilder encodedString) {
			DecoderUtil.reflectiveDecode(this, encodedString, TestableEncoderDecoderNestedList.class);
		}

		@Override
		public String encode() {
			return EncoderUtil.reflectiveEncode(this, TestableEncoderDecoderNestedList.class);
		}

		@Override
		public String toString() {
			return values != null ? values.toString() : null;
		}

		@Override
		public boolean equals(final Object obj) {
			final TestableEncoderDecoderNestedList other = (TestableEncoderDecoderNestedList) obj;
			if (values == null) {
				if (other.values != null) {
					return false;
				}
			} else if (!values.equals(other.values)) {
				return false;
			}
			return true;
		}

		public void setValues(final List<String> encoding) {
			values = encoding;
		}

		public List<String> getValues() {
			return values;
		}
	}

	private static class TestableSimpleSerialization implements SimpleSerialization<TestableSimpleSerialization> {
		private String testvalue;

		@Override
		public TestableSimpleSerialization deserialize(final String testvalue) {
			final TestableSimpleSerialization testableFromString = new TestableSimpleSerialization();
			testableFromString.testvalue = testvalue;
			return testableFromString;
		}

		@Override
		public String serialize() {
			return testvalue;
		}
	}

	/**
	 * @author Benny Bottema
	 */
	public static class TestableEncoderReflective implements DatagramEncoder {

		private Integer intfield;
		private String stringfield;

		public void setIntfield(final Integer intfield) {
			this.intfield = intfield;
		}

		public void setStringfield(final String stringfield) {
			this.stringfield = stringfield;
		}

		public Integer getIntfield() {
			return intfield;
		}

		public String getStringfield() {
			return stringfield;
		}

		@Override
		public String encode() {
			throw new NotImplementedException();
		}
	}
}