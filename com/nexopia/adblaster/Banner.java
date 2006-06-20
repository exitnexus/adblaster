/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;



class Banner{
	Interests interests;
	int id;
	public double profit;
	int max_hits;
	
	static int count = 0;
	public static int counter(){
		return count++;
	}
	
	Banner(){
		interests = new Interests();
		id = counter(); //TODO Banners need an ID we can track them by
		max_hits = (int) (Math.pow((Math.random()-0.5) * 2,2) * 1000);
		this.profit = Math.random();
	}
	
	Banner(int id) {
		this();
		this.id = id;
	}
	
	int getID() {
		return id;
	}
	public String toString(){
		return "" + this.id + "," + this.profit;
	}
}