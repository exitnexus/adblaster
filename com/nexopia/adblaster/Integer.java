package com.nexopia.adblaster;

import java.util.Stack;

public class Integer {
	//static portion
	private static Stack<Integer> pool;
	public static int MIN_VALUE = java.lang.Integer.MIN_VALUE;
	public static int MAX_VALUE = java.lang.Integer.MAX_VALUE;
	
	public static int parseInt(String s) {
		return java.lang.Integer.parseInt(s);
	}
	
	public static Integer valueOf(int i) {
		if (!pool.isEmpty()) {
			Integer recycledInt = pool.pop();
			recycledInt.setInt(i);
			return recycledInt;
		} else {
			return new Integer(i);
		}
	}
	
	//non-static portion
	private int i;
	public Integer(int i) {
		this.i = i;
	}
	
	public int intValue() {
		return i;
	}
	
	public void setInt(int i) {
		this.i = i;
	}
	
	public void free() {
		pool.add(this);
	}
	
	public boolean equals(Object o) {
		if (o != null && (o.getClass().equals(this.getClass()))) {
			Integer i = (Integer)o;
			return this.i == i.i;
		}
		return false;
	}
	
}
