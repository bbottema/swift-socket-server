package org.codemonkey.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.codemonkey.util.DatagramDecoderUtilTest.TestableDecoderEnum.EnumSimpleSerialization;
import org.junit.Test;

/**
 * @author Benny Bottema
 */
@SuppressWarnings("javadoc")
public class DatagramDecoderUtilTest {

	@Test
	public void testGenericDecodeList() {
		final String ENCODEDLIST = "3|2|995|abcde4|12342|AB-1|2";
		final StringBuilder encodedString = new StringBuilder(ENCODEDLIST + ENCODEDLIST);
		final List<TestableDecoder> list = DecoderUtil.genericDecodeList(encodedString, TestableDecoder.class);
		// only one list should've been decoded and thereby removed
		assertEquals(ENCODEDLIST, encodedString.toString());
		assertEquals(3, list.size());
		assertEquals((Integer) 99, list.get(0).intfield);
		assertEquals("abcde", list.get(0).stringfield);
		assertEquals((Integer) 1234, list.get(1).intfield);
		assertEquals("AB", list.get(1).stringfield);
		assertNull(list.get(2).intfield);
		assertEquals("2", list.get(2).stringfield);
	}

	@Test
	public void testDecodeListSimple() {
		final StringBuilder encodedString = new StringBuilder("2|1|a3|123");
		final List<TestableSimple> list = DecoderUtil.genericDecodeList(encodedString, TestableSimple.class);
		assertEquals(2, list.size());
		assertEquals("a", list.get(0).value);
		assertEquals("123", list.get(1).value);
	}

	@Test
	public void testReflectiveDecode() {
		final StringBuilder encodedString = new StringBuilder("2|995|abcde");
		final TestableDecoder subject = new TestableDecoder();
		DecoderUtil.reflectiveDecode(subject, encodedString, TestableDecoder.class);
		assertEquals("", encodedString.toString());
		assertEquals((Integer) 99, subject.intfield);
		assertEquals("abcde", subject.stringfield);
	}

	@Test
	public void testReflectiveDecodeSimpleSerializable() {
		final StringBuilder encodedString = new StringBuilder("2|99");
		final TestableObjectWithSimpleSerializer subject = new TestableObjectWithSimpleSerializer();
		DecoderUtil.reflectiveDecode(subject, encodedString, TestableObjectWithSimpleSerializer.class);
		assertEquals("", encodedString.toString());
		assertNotNull(subject.value);
		assertEquals("99", subject.value.value);
	}

	@Test
	public void testReflectiveDecodeSimpleSerializableEnum() {
		final StringBuilder encodedString = new StringBuilder("1|2");
		final TestableDecoderEnum subject = new TestableDecoderEnum();
		DecoderUtil.reflectiveDecode(subject, encodedString, TestableDecoderEnum.class);
		assertEquals("", encodedString.toString());
		assertSame(EnumSimpleSerialization.TWO, subject.value);
	}

	@Test
	public void testReflectiveDecodeListSimpleSerializableEnum() {
		final StringBuilder encodedString = new StringBuilder("2|1|21|1");
		final List<TestableDecoderEnum> list = DecoderUtil.genericDecodeList(encodedString, TestableDecoderEnum.class);
		assertEquals(2, list.size());
		assertEquals("", encodedString.toString());
		assertSame(EnumSimpleSerialization.TWO, list.get(0).value);
		assertSame(EnumSimpleSerialization.ONE, list.get(1).value);
	}

	@Test
	public void testCountNext() {
		assertEquals(0, DecoderUtil.countNext(null));

		StringBuilder encodedString = new StringBuilder("");
		assertEquals(0, DecoderUtil.countNext(encodedString));
		assertEquals("", encodedString.toString());
		encodedString = new StringBuilder("-");
		assertEquals(1, DecoderUtil.countNext(encodedString));
		assertEquals("-", encodedString.toString());
		encodedString = new StringBuilder("2|");
		assertEquals(2, DecoderUtil.countNext(encodedString));
		assertEquals("", encodedString.toString());
		encodedString = new StringBuilder("1|");
		assertEquals(1, DecoderUtil.countNext(encodedString));
		assertEquals("", encodedString.toString());
		encodedString = new StringBuilder("10|");
		assertEquals(10, DecoderUtil.countNext(encodedString));
		assertEquals("", encodedString.toString());
		encodedString = new StringBuilder("444|");
		assertEquals(444, DecoderUtil.countNext(encodedString));
		assertEquals("", encodedString.toString());
	}

	@Test
	public void testValues() {
		final StringBuilder encodedString = new StringBuilder("2|214|1234---1|3");
		final List<String> result = DecoderUtil.extractValues(encodedString, 4);
		assertEquals("-1|3", encodedString.toString());
		assertEquals(4, result.size());
		assertEquals("21", result.get(0));
		assertEquals("1234", result.get(1));
		assertNull(result.get(2));
		assertNull(result.get(3));
	}

	public static class TestableSimple {

		private String value;

		public void setValue(final String value) {
			this.value = value;
		}

	}

	/**
	 * @author Benny Bottema
	 */
	public static class TestableDecoder implements DatagramDecoder {

		private Integer intfield;
		private String stringfield;

		public void setIntfield(final Integer intfield) {
			this.intfield = intfield;
		}

		public void setStringfield(final String stringfield) {
			this.stringfield = stringfield;
		}

		@Override
		public void decode(final StringBuilder encodedString) {
			DecoderUtil.reflectiveDecode(this, encodedString, TestableDecoder.class);
		}
	}

	/**
	 * @author Benny Bottema
	 */
	public static class TestableSimpleSerializer implements SimpleSerialization<TestableSimpleSerializer> {

		private String value;

		@Override
		public String serialize() {
			return value;
		}

		@Override
		public TestableSimpleSerializer deserialize(final String value) {
			final TestableSimpleSerializer serializer = new TestableSimpleSerializer();
			serializer.value = value;
			return serializer;
		}
	}

	public static class TestableObjectWithSimpleSerializer {

		private TestableSimpleSerializer value;

		public void setValue(final TestableSimpleSerializer value) {
			this.value = value;
		}
	}

	/**
	 * @author Benny Bottema
	 */
	public static class TestableDecoderEnum {

		public static enum EnumSimpleSerialization implements SimpleSerialization<EnumSimpleSerialization> {
			ONE("1"), TWO("2");

			private final String code;

			private EnumSimpleSerialization(final String code) {
				this.code = code;
			}

			@Override
			public String serialize() {
				return code;
			}

			@Override
			public EnumSimpleSerialization deserialize(final String value) {
				return value.equals(ONE.code) ? ONE : TWO;
			}
		}

		private EnumSimpleSerialization value;

		public void setValue(final EnumSimpleSerialization value) {
			this.value = value;
		}
	}
}