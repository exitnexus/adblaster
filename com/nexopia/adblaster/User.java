/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.Random;



class User {
	Interests interests;
	int id;
	byte age;
	byte sex;
	short location;
	
	User(int id, UserDatabase db) {
		try {
			User u = db.getUser(id);
			id = u.id;
			age = u.age;
			sex = u.sex;
			location = u.location;
			interests = u.interests;
		} catch (Exception e) {
			this.id = id;
			this.interests = new Interests();
		}
	}
	
	User(int id, byte age, byte sex, short location, String interests) {
		this(id,age,sex,location,new Interests(interests));
	}

	User(int id, byte age, byte sex, short location, Interests interests) {
		this.id = id;
		this.age = age;
		this.sex = sex;
		this.location = location;
		this.interests = interests;
	}

	int getID() {
		return id;
	}
	static String sexes[] = {"u","m","f"};
	
	public String toString(){
		return new Integer(id).toString() + ":" + age + ":" + sexes[sex];
	}
	byte getAge() {
		return age;
	}
	void setAge(byte age) {
		this.age = age;
	}
	Interests getInterests() {
		return interests;
	}
	void setInterests(Interests interests) {
		this.interests = interests;
	}
	short getLocation() {
		return location;
	}
	void setLocation(short location) {
		this.location = location;
	}
	byte getSex() {
		return sex;
	}
	void setSex(byte sex) {
		this.sex = sex;
	}

	/**
	 * @return
	 */
	public static User generateRandomUser() {
		int id;
		byte sex;
		byte age;
		short location;
		Interests interests;
		
		Random r = new Random();
		id = r.nextInt(Integer.MAX_VALUE);
		if (r.nextBoolean()) {
			sex = 1; //male
		} else {
			sex = 2; //female
		}
		age = (byte)(14+r.nextInt(90));
		location = (short)r.nextInt(60);
		interests = Interests.generateRandomInterests();
		User u = new User(id, age, sex, location, interests);
		return u;
	}
}