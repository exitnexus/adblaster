package com.nexopia.adblaster.util;

import java.util.Arrays;
import java.util.HashMap;

import com.nexopia.adblaster.struct.ServablePropertyHolder;

/**
 * 
 * @author Tom
 *
 */
public class LowMemMap {
	
	/**
	 * Fixed size array
	 * @author Tom
	 *
	 */
	public class LowMemArray{
		private int start_index;
		private LowMemArray(int _index){
			start_index = _index * subset_size;
		}
		public int size(){
			return subset_size;
		}
		public int get(int i){
			return data[start_index + i];
		}
		public void set(int key, int value){
			data[start_index + key] = value;
		}
	}
	
	int[] data;
	IntIntHashMap indices;
	public final int default_size = 8; 
	int subset_size;
	int num_elts;
	
	public LowMemMap(int _size){
		indices = new IntIntHashMap();
		subset_size = _size;
		data = new int[default_size * _size];
		num_elts = 0;
	}
	
	public void grow(){
		int[] newdata = new int[data.length * 2];
		for (int i = 0; i < data.length; i++){
			newdata[i] = data[i];
		}
		this.data = newdata;
	}
	
	public void put(int key){
		indices.put(key, num_elts);
		num_elts++;
		if ((num_elts+1) * subset_size > data.length){
			this.grow();
		}
	}
	
	public LowMemArray getOrCreate(int key){
		if (!indices.containsKey(key)){
			this.put(key);
		}
		
		int index = indices.get(key);
		
		return new LowMemArray(index);
		
	}
	
	public int memory_usage() {
		int bytes = 26;
		bytes += data.length*4;
		bytes += indices.size()*5;
		return bytes;
	}
	
	
}

