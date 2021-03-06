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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import erjang.driver.EPortControl;
import erjang.m.ets.EMatchContext;
import erjang.m.ets.ETermPattern;

public abstract class EObject implements Comparable<EObject> {

	public ECons cons(EObject h)
	{
		ESmall sm;
		if ((sm=h.testSmall()) != null && ((sm.value&0xff)==sm.value)) {
			return new EBinList((byte) sm.value, this);
		} else {
			return new EPair(h, this);
		}
	}
	
	public ERef testReference() {
		return null;
	}
	

	public EString testString()	{
		return null;
	}
	
	public EFun testFunction() {
		return null;
	}
	
	public EFun testFunction2(int nargs) {
		return null;
	}
	
	public EAtom testAtom() {
		return null;
	}

	public ECons testNonEmptyList() {
		return null;
	}
	
	public ESeq testWellformedList() {
		return null;
	}
	
	public ETuple testTuple() {
		return null;
	}

	public ESeq testSeq() {
		return null;
	}

	public EPID testPID() {
		return null;
	}

	public int asInt() {
		throw new RuntimeException("cannot convert " + this + " to int");
	}

	public ENumber testNumber() {
		return null;
	}

	public ECons testCons() {
		return null;
	}

	public EInteger testInteger() {
		return null;
	}

	public ENil testNil() {
		return null;
	}
	
	public boolean isNil() { return testNil() != null; }
	public boolean isBoolean() { return this==ERT.TRUE || this==ERT.FALSE; }

	public EAtom testBoolean() {
		return null;
	}

	public EBinary testBinary() {
		return null;
	}


	/**
	 * @return this if this object is an instance of EPort, otherwise null
	 */
	public EPort testPort() {
		return null;
	}

	/**
	 * @return this if this object is an instance of ESmall, otherwise null
	 */
	public ESmall testSmall() {
		return null;
	}
	

	/**
	 * @return
	 */
	public EDouble testFloat() {
		return null;
	}
	
	public boolean collectIOList(List<ByteBuffer> out) {
		return false;
	}
	
	public Type emit_const(MethodVisitor mv) {
		throw new NotImplemented("emit_const for "+this.getClass().getName());
	}

	//
	// 
	//
	
	@BIF(name="-")
	public ENumber negate() { throw ERT.badarg(this); }

	@BIF(name="+")
	public ENumber add(EObject rhs, boolean guard) { if (guard) return null; throw ERT.badarg(this, rhs); }
	public ENumber add(int lhs, boolean guard) { if (guard) return null; throw ERT.badarg(lhs, this); }
	public ENumber add(double lhs, boolean guard) { if (guard) return null; throw ERT.badarg(lhs, this); }
	public ENumber add(BigInteger lhs, boolean guard) { if (guard) return null; throw ERT.badarg(lhs, this); }

	@BIF(name="-")
	public ENumber subtract(EObject rhs, boolean guard) { if (guard) return null; throw ERT.badarg(this, rhs); }
	public ENumber subtract(int rhs) { throw ERT.badarg(this, rhs); }
	ENumber r_subtract(int lhs) { throw ERT.badarg(lhs, this); }
	ENumber r_subtract(double lhs) { throw ERT.badarg(lhs, this); }
	ENumber r_subtract(BigInteger lhs) { throw ERT.badarg(lhs, this); }
	
	@BIF(name="div")
	public EInteger idiv(EObject rhs) { throw ERT.badarg(this, rhs); }
	public EInteger idiv(int rhs) { throw ERT.badarg(this, rhs); }
	EInteger r_idiv(int lhs) { throw ERT.badarg(lhs, this); }
	EInteger r_idiv(BigInteger lhs) { throw ERT.badarg(lhs, this); }

	@BIF(name="rem")
	public EInteger irem(EObject rhs) { throw ERT.badarg(this, rhs); }
	EInteger r_irem(int lhs) { throw ERT.badarg(lhs, this); }
	EInteger r_irem(BigInteger lhs) { throw ERT.badarg(lhs, this); }

	@BIF(name="/")
	public EDouble divide(EObject rhs) { throw ERT.badarg(this, rhs); }
	EDouble r_divide(int lhs) { throw ERT.badarg(lhs, this); }
	EDouble r_divide(double lhs) { throw ERT.badarg(lhs, this); }
	EDouble r_divide(BigInteger lhs) { throw ERT.badarg(lhs, this); }

	@BIF(name="*")
	public ENumber multiply(EObject rhs) { throw ERT.badarg(this, rhs); }
	public ENumber multiply(int lhs) { throw ERT.badarg(lhs, this); }
	public ENumber multiply(double lhs) { throw ERT.badarg(lhs, this); }
	public ENumber multiply(BigInteger lhs) { throw ERT.badarg(lhs, this); }

	@BIF(name="bsr")
	public EInteger bsr(EObject rhs) { throw ERT.badarg(this, rhs); }
	EInteger r_bsr(int lhs) { throw ERT.badarg(lhs, this); }
	EInteger r_bsr(BigInteger lhs) { throw ERT.badarg(lhs, this); }

	@BIF(name="bsl")
	public EInteger bsl(EObject rhs) { throw ERT.badarg(this, rhs); }
	public EInteger bsl(int rhs) { throw ERT.badarg(this, rhs); }
	EInteger r_bsl(int lhs) { throw ERT.badarg(lhs, this); }
	EInteger r_bsl(BigInteger lhs) { throw ERT.badarg(lhs, this); }
	
	@BIF(name="band")
	public EInteger band(EObject rhs) { throw ERT.badarg(this, rhs); }
	public EInteger band(int lhs) { throw ERT.badarg(lhs, this); }
	public EInteger band(BigInteger lhs) { throw ERT.badarg(lhs, this); }

	@BIF(name="bor")
	public EInteger bor(EObject rhs) { throw ERT.badarg(this, rhs); }
	public EInteger bor(int lhs) { throw ERT.badarg(lhs, this); }
	public EInteger bor(BigInteger lhs) { throw ERT.badarg(lhs, this); }

	@BIF(name="bxor")
	public EInteger bxor(EObject rhs) { throw ERT.badarg(this, rhs); }
	public EInteger bxor(int lhs) { throw ERT.badarg(lhs, this); }
	public EInteger bxor(BigInteger lhs) { throw ERT.badarg(lhs, this); }

	@BIF(name="bnor")
	public EInteger bnot() { throw ERT.badarg(this); }

	// extra convenience
	
	public EDouble divide(double rhs) { throw ERT.badarg(this,rhs); }
	public EInteger irem(int rhs) { throw ERT.badarg(this,rhs); }

	public boolean equals(Object other) {
		if (other == this) return true;
		if (other instanceof EObject) {
			return compareTo((EObject) other) == 0;
		} else {
			return false;
		}
	}
	
	public boolean equals(EObject other) {
		if (other == this) return true;
		return compareTo(other) == 0;
	}
	
	public int compareTo(EObject rhs) {
		if (rhs == this) return 0;
		int cmp1 = cmp_order();
		int cmp2 = rhs.cmp_order();
		if ( cmp1 == cmp2 ) {
			return compare_same(rhs);
		} else if (cmp1 < cmp2) {
			return -1;
		} else {
			return 1;
		}
	}

	/** Compare two objects that have same cmp_order */
	int compare_same(EObject rhs) { throw new Error("cannot compare"); }
	
	int r_compare_same(ESmall lhs) { throw new NotImplemented(); }
	int r_compare_same(EBig lhs) { throw new NotImplemented(); }
	int r_compare_same(EDouble lhs) { throw new NotImplemented(); }
	int r_compare_same(EInternalPID lhs) { throw new NotImplemented(); }
	
	public boolean equalsExactly(EObject rhs) {
		return compare_same(rhs) == 0;
	}

	boolean r_compare_same_exactly(ESmall lhs) { return false; }
	boolean r_compare_same_exactly(EBig lhs) { return false; }
	boolean r_compare_same_exactly(EDouble lhs) { return false; }
	
	
	/** used as compare-order for "non-erlang terms", such as 
	 *  compiled ets queries and the tail marker */
	public static final int CMP_ORDER_ERJANG_INTERNAL = -1;
	public static final int CMP_ORDER_NUMBER = 0;
	public static final int CMP_ORDER_ATOM = 1;
	public static final int CMP_ORDER_REFERENCE = 2;
	public static final int CMP_ORDER_FUN = 3;
	public static final int CMP_ORDER_PORT = 4;
	public static final int CMP_ORDER_PID = 5;
	public static final int CMP_ORDER_TUPLE = 6;
	public static final int CMP_ORDER_LIST = 7;
	public static final int CMP_ORDER_BITSTRING = 8;
	
	/** 
	 * 	number[0] < atom[1] < reference[2] < fun[3] < port[4] < pid[5] < tuple[6] < list[7] < bit string[8]
	 * @return
	 */
	int cmp_order() { throw new Error("cannot compare"); }


	/**
	 * @param o2
	 * @return
	 */
	public EAtom ge(EObject o2) {
		return ERT.box ( this.compareTo(o2) >= 0 );
	}


	/**
	 * @return
	 */
	public EBitString testBitString() {
		return null;
	}

	/**
	 * @return non-null if this is an internal port
	 */
	public EInternalPort testInternalPort() {
		return null;
	}

	/**
	 * @return
	 */
	public EPortControl testPortControl() {
		return null;
	}

	/**
	 * @return
	 */
	public EHandle testHandle() {
		return null;
	}

	/**
	 * @return
	 */
	public EInternalPID testInternalPID() {
		return null;
	}

	/**
	 * @return true if this term matches the given matcher
	 */
	public boolean match(ETermPattern matcher, EMatchContext r) {
		return false;
	}

	/**
	 * @param out
	 * @return
	 */
	public ETermPattern compileMatch(Set<Integer> out) {
		// this should continue to be "not implemented".  
		// subclasses should provide an implementation.
		throw new NotImplemented();
	}


}
