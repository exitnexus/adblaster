package com.nexopia.adblaster;

import java.util.Stack;

import java.io.*;

public class Integer {
	//static portion
	private static Stack<Integer> pool = new Stack<Integer>();
	public static final int MIN_VALUE = java.lang.Integer.MIN_VALUE;
	public static final Integer NEGATE = new Integer(MIN_VALUE);
	public static final int MAX_VALUE = java.lang.Integer.MAX_VALUE;
	public static final Integer IDENTITY = new Integer(0);
	
	/*
	static {
		Thread t;
		t = new Thread(){
			public void run() {
				while (true){
					try {
						sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Pool size: " + pool.size());
				}
			}
		};
		t.start();
	}
	*/
	public static int parseInt(String s) {
		return java.lang.Integer.parseInt(s);
	}
	
	static Integer[] cache = new Integer[500];
	static { 
		for (int i = 0; i < 500; i++){
			cache[i] = new Integer(i);
		}
	}
	
	public static Integer valueOf(int i) {
		if (i >= 0 && i < 500){
			return cache[i];
		}
		synchronized (pool) {
			if (!pool.isEmpty()) {
				Integer recycledInt = pool.pop();
				recycledInt.setInt(i);
				return recycledInt;
			} else {
				return new Integer(i);
			}
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
		if (i < 0 || i >= 500)
		//if (pool.size() < 1000){
			pool.push(this);
		//}
	}
	
	public boolean equals(Object o) {
		if (o != null && (o.getClass().equals(this.getClass()))) {
			Integer i = (Integer)o;
			return this.i == i.i;
		}
		return false;
	}
	
	public String toString(){
		return "" + i;
	}
	
	public int hashCode(){
		return i;
	}
	
	public static void main(String args[]){
		
		try {
			File f = new File("newbanner.log");
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader buf = new BufferedReader(isr);
			String s = "";
			int i = 0;
			while ((s = buf.readLine()) != null){
				if (s.indexOf("get") >= 0){
					i++;
				}
			}
			System.out.println(i);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true){
			for (int i = 0; i < 99999; i++){
				Integer in = Integer.valueOf(i);
				in.free();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
