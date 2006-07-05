package com.nexopia.adblaster;

import java.util.*;
/**
 * Convenience class.  Not fully implemented, feel free to improve.
 */

public class Tuple<T1, T2> {
	private Vector<T1> tuple1;
	private Vector<T2> tuple2;
	Tuple(T1 o1, T2 o2){
		tuple1 = new Vector<T1>();
		tuple2 = new Vector<T2>();
		tuple1.add(o1);
		tuple2.add(o2);
	}
	public T1 getFirst(int i) {
		return tuple1.get(i);
	}
	public T2 getSecond(int i) {
		return tuple2.get(i);
	}
}
