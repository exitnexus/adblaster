/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;



class User {
	Interests interests;
	int id;
	
	static int userid = 0;
	
	User(String s){
		interests = new Interests();
		id = userid++; 
	}
	
	User(int id) {
		this.id = id;
		interests = new Interests();
	}
	
	int getID() {
		return id;
	}
	
	public String toString(){
		return new Integer(id).toString();
	}
}