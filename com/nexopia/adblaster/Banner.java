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
	
	Banner(){
		interests = new Interests();
		id = 0; //TODO Banners need an ID we can track them by
		max_hits = (int) (Math.random() * 1000);
	}
	
	Banner(int id) {
		this.interests = new Interests();
		this.id = id;
		this.profit = 0;
	}
	
	int getID() {
		return id;
	}
}