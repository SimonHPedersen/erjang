/**
 * This file is part of Erjang - A JVM-based Erlang VM
 *
 * Copyright (c) 2009 by Trifork
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package erjang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class EString extends ESeq implements CharSequence {

	private static final Charset ISO_LATIN_1 = Charset.forName("ISO-8859-1");

	private static final EString EMPTY = new EString("");

	final byte[] data;
	final int off;
	private int hash = -1;

	public EString(String value) {
		this.hash = value.hashCode();
		this.data = value.getBytes(ISO_LATIN_1);
		this.off = 0;
	}

	public EString testString() {
		return this;
	}

	EString(byte[] data, int off) {
		this.data = data;
		this.off = off;
	}

	/**
	 * @param array
	 * @param arrayOffset
	 * @param len
	 */
	public static EString make(byte[] array, int arrayOffset, int len) {
		if (len == array.length - arrayOffset) {
			return new EString(array, arrayOffset);
		} else {
			byte[] copy = new byte[len];
			System.arraycopy(array, arrayOffset, copy, 0, len);
			return new EString(array, len);
		}
	}

	/**
	 * @param list
	 */
	public static EString make(ECons list) {
		EString s;
		if ((s = list.testString()) != null) {
			return s;

		} else if (list.isNil()) {
			return EString.EMPTY;

		} else {
			ByteArrayOutputStream barr = new ByteArrayOutputStream();

			EObject tail = list;
			while ((list = tail.testNonEmptyList()) != null) {

				EObject head = list.head();

				ESmall intval;
				if ((intval = head.testSmall()) == null) {
					throw ERT.badarg();
				}

				int byteValue = intval.value & 0xff;
				if (intval.value != byteValue) {
					throw ERT.badarg();
				}

				barr.write(byteValue);
				tail = list.tail();
			}

			return new EString(barr.toByteArray(), 0);
		}
	}

	@Override
	public int hashCode() {
		if (hash == -1) {
			hash = stringValue().hashCode();
		}
		return hash;
	}

	public String stringValue() {
		return new String(data, off, data.length - off, ISO_LATIN_1);
	}

	public boolean equalsExactly(EObject rhs) {
		ENil nil;
		int length = length();

		if ((nil = rhs.testNil()) != null) {
			return length == 0;
		}

		EString str;
		if ((str = rhs.testString()) != null) {
			EString es = str;

			if (length != es.length())
				return false;

			for (int i = 0; i < length; i++) {
				if (charAt(i) != es.charAt(i))
					return false;
			}

			return true;

		}

		ESeq seq;
		if ((seq = rhs.testWellformedList()) != null) {

			int i = 0;

			while (i < length) {

				if (seq.testNil() != null)
					return false;

				if (!seq.head().equalsExactly(new ESmall(charAt(i)))) {
					return false;
				}

				seq = seq.tail();

			}

		}

		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String) {
			return ((String) obj).equals(stringValue());
		}

		if (!(obj instanceof EObject)) {
			return false;
		}

		return compare_same((EObject) obj) == 0;

	}

	@Override
	public char charAt(int index) {
		return (char) (data[off + index] & 0xff);
	}

	@Override
	public int length() {
		return data.length - off;
	}

	@Override
	public CharSequence subSequence(final int start, final int end) {
		if (end == length())
			return new EString(data, off + start);
		return new SubSequence(start, end - start);
	}

	public class SubSequence implements CharSequence {

		private final int offset;
		private final int length;

		public SubSequence(int start, int length) {
			this.offset = start;
			this.length = length;
			EString.this.check_subseq(offset, length);
		}

		@Override
		public char charAt(int index) {
			return EString.this.charAt(offset + index);
		}

		@Override
		public int length() {
			return length;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new SubSequence(this.offset + start, end - start);
		}

	}

	void check_subseq(int offset, int length) {
		if (offset < 0 || length < 0 || (offset + length) > length())
			throw new IllegalArgumentException();
	}

	@Override
	public String toString() {
		return '"' + stringValue() + '"';
	}

	public static EString fromString(String s) {
		return new EString(s);
	}

	private static final Type ESTRING_TYPE = Type.getType(EString.class);
	private static final Type STRING_TYPE = Type.getType(String.class);

	@Override
	public Type emit_const(MethodVisitor fa) {

		Type type = ESTRING_TYPE;

		fa.visitLdcInsn(this.stringValue());
		fa.visitMethodInsn(Opcodes.INVOKESTATIC, type.getInternalName(),
				"fromString", "(" + STRING_TYPE.getDescriptor() + ")"
						+ type.getDescriptor());

		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see erjang.ESeq#cons(erjang.EObject)
	 */
	@Override
	public EList cons(EObject h) {
		return new EList(h, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see erjang.ESeq#tail()
	 */
	@Override
	public ESeq tail() {
		if (off == data.length)
			return ERT.NIL;
		return new EString(data, off + 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see erjang.ECons#head()
	 */
	@Override
	public ESmall head() {
		return new ESmall(data[off] & 0xff);
	}

	@Override
	public ENil testNil() {
		return length() == 0 ? ERT.NIL : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see erjang.ESeq#testWellformedList()
	 */
	@Override
	public ESeq testWellformedList() {
		return this;
	}

	public ECons testNonEmptyList() {
		return length() == 0 ? null : this;
	}

	public ECons testCons() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see erjang.ECons#compare_same(erjang.EObject)
	 */
	@Override
	int compare_same(EObject rhs) {
		if (rhs.isNil())
			return 1;

		int length = length();

		EString str;
		if ((str = rhs.testString()) != null) {
			EString es = str;
			int length2 = str.length();
			int limit = Math.min(length, length2);
			for (int i = 0; i < limit; i++) {
				char ch1 = charAt(i);
				char ch2 = es.charAt(i);
				if (ch1 < ch2)
					return -1;
				if (ch1 > ch2)
					return 1;
			}

			if (length > length2)
				return 1;
			if (length < length2)
				return -1;
			return 0;

		}

		ECons seq;
		if ((seq = rhs.testCons()) != null) {

			int i = 0;

			while (i < length) {

				if ((seq.testNil()) != null) {
					return -1; // I AM SHORTER
				}

				int cmp = (new ESmall(charAt(i++))).compareTo(seq.head());
				if (cmp != 0)
					return cmp;

				EObject res = seq.tail();

				if ((seq = res.testCons()) != null) {
					continue;
				}

				return -res.compareTo(new EString(data, i));
			}

		}

		return -rhs.compareTo(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see erjang.ECons#prepend(erjang.ECons)
	 */
	@Override
	public ECons prepend(ECons list) {
		EString other = list.testString();
		if (other != null) {
			byte[] out = new byte[length() + other.length()];
			System.arraycopy(other.data, other.off, out, 0, other.length());
			System.arraycopy(this.data, this.off, out, other.length(), this
					.length());
			return EString.make(out, 0, out.length);
		} else {
			return super.prepend(list);
		}
	}

	/**
	 * @return
	 */
	public EBitString asBitString() {
		return new EBinary(data, off, length());
	}

	/**
	 * @param eObject
	 * @return
	 */
	public static EString make(EObject eObject) {
		ESeq str;
		if ((str = eObject.testSeq()) != null)
			return make(str);

		EAtom am = eObject.testAtom();
		if (am != null) {
			return fromString(am.toString());
		}

		throw ERT.badarg();
	}

	/**
	 * @param bin
	 * @return
	 */
	public static EString fromBinary(EBinary bin) {
		if ((bin.bitOff % 8) == 0) {
			// TODO: bin also has a bitlength, which may differ from
			// bin.data.length*8
			return new EString(bin.data, bin.bitOff / 8);
		}

		throw new NotImplemented();
	}

	/**
	 * @return the contents of this string as a java.nio.ByteBuffer
	 */
	public boolean collectIOList(List<ByteBuffer> out) {
		if (length() != 0) {
			out.add(ByteBuffer.wrap(data, off, data.length - off));
		}
		return true;
	}

	/**
	 * @param buf
	 * @return
	 */
	public static ESeq make(ByteBuffer data) {
		if (data == null || data.remaining() == 0)
			return ERT.NIL;
		return EString.make(data.array(), data.arrayOffset() + data.position(),
				data.remaining());
	}

	/**
	 * @param strbuf
	 * @return
	 */
	public static EString make(byte[] strbuf) {
		return make(strbuf, 0, strbuf.length);
	}

	/**
	 * @param i
	 * @return
	 */
	public static boolean isValidCodePoint(int cp) {
		return (cp >>> 16) <= 0x10 // in 0..10FFFF; Unicode range
				&& (cp & ~0x7FF) != 0xD800 // not in D800..DFFF; surrogate range
				&& (cp & ~1) != 0xFFFE; // not in FFFE..FFFF; non-characters
	}

	public static ESeq read(EInputStream ei) throws IOException {
		return ei.read_string();
	}
}
