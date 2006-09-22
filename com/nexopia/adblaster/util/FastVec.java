/**
 * 
 */
package com.nexopia.adblaster.util;

import java.util.Iterator;
import java.util.Vector;

class FastVec <T> implements Iterable{
	Vector <T>v;
	public FastVec(T obj){
		v = new Vector<T>();
		v.add(obj);
	}
	public FastVec add(T obj){
		v.add(obj);
		return this;
	}
	public Iterator iterator() {
		return v.iterator();
	}
	public boolean isEmpty(){
		return v.isEmpty();
	}
	
}