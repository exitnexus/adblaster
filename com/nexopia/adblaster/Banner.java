/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;



class Banner{
	Interests i;
	int id;
	Banner(){
		i = new Interests();
		id = 0; //TODO Banners need an ID we can track them by
	}
	
	Banner(int id) {
		this.i = new Interests();
		this.id = id;
	}
	
	int getID() {
		return id;
	}
}