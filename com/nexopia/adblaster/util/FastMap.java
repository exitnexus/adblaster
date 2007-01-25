/**
 * 
 */
package com.nexopia.adblaster.util;

import java.util.HashMap;
import java.util.Set;

public class FastMap <K, V> {
	private HashMap <K,V>map;
	
	public FastMap(){
		map = new HashMap<K,V>();
	}
	
	public FastMap(K key, V val){
		map = new HashMap<K,V>();
		map.put(key, val);
	}
	
	public V getOrCreate(K k, Class defaultValue) {
		V elem = this.get(k);
		if(elem == null){
			try {
				elem = (V) defaultValue.newInstance();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			this.put(k,elem);
		}
		return elem;
	}
	HashMap getMap(){
		return map;
	}
	public void put(K k, V v){
		map.put(k,v);
	}
	public V get(K k){
		return map.get(k);
	}

	public int size() {
		return map.size();
	}
	
	public Set<K> keySet() {
		return map.keySet();
	}
	
}