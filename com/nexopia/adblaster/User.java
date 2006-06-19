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
	
	User(String s){
		interests = new Interests();
		id = 0; //TODO Thomas we shouldn't care about the user's name, just their ID 
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