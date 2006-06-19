/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;



class User{
	String name;
	Interests i;
	int id;
	User(String s){
		name = s;
		i = new Interests();
		id = 0; //TODO Thomas we shouldn't care about the user's name, just their ID 
	}
	
	User(int id) {
		this.id = id;
		i = new Interests();
		name = ""; //TODO username questions arrises here again
	}
	
	int getID() {
		return id;
	}
}