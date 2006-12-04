/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster.struct;

import java.util.Random;

import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.Interests;



public class User {
	Interests interests;
	int id;
	byte age;
	byte sex;
	short location;
	static final Random rand = new Random();
	
	public User() {
		interests = null;
		id=0;
		age=0;
		sex=0;
		location=0;
	}
	
	public User(String databaseString) {
		String words[] = databaseString.split(" ");
		id = Integer.parseInt(words[0]);
		age = Byte.parseByte(words[1]);
		sex = Byte.parseByte(words[2]);
		location = Short.parseShort(words[3]);
		interests = new Interests(words[4], false);
	}
	
	public User(int id, byte age, byte sex, short location, String interests) {
		this(id,age,sex,location,new Interests(interests, false));
		if (this.interests.negate == true){
			throw new UnsupportedOperationException();
		}
	}

	public void fill(int userid, int age, int sex, int location, String interests) {
		this.id = userid;
		this.age = (byte)age;
		this.sex = (byte)sex;
		this.location = (short)location;
		if (this.interests == null) {
			this.interests = new Interests(interests, false);
		} else {
			this.interests.fill(interests, true);
		}
		if (this.interests.negate == true){
			throw new UnsupportedOperationException();
		}
	}
	
	User(int id, byte age, byte sex, short location, Interests interests) {
		this.id = id;
		this.age = age;
		this.sex = sex;
		this.location = location;
		this.interests = interests;
		if (this.interests.negate == true){
			throw new UnsupportedOperationException();
		}
	}

	public int getID() {
		return id;
	}
	static String sexes[] = {"u","m","f"};
	
	public String toString(){
		return new Integer(id).toString() + ":" + age + ":" + sexes[sex];
	}
	public byte getAge() {
		return age;
	}
	void setAge(byte age) {
		this.age = age;
	}
	public Interests getInterests() {
		return interests;
	}
	void setInterests(Interests interests) {
		this.interests = interests;
	}
	public short getLocation() {
		return location;
	}
	void setLocation(short location) {
		this.location = location;
	}
	public byte getSex() {
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
		
		id = rand.nextInt(Integer.MAX_VALUE);
		if (rand.nextBoolean()) {
			sex = 1; //male
		} else {
			sex = 2; //female
		}
		age = (byte)(14+rand.nextInt(90));
		location = (short)rand.nextInt(60);
		interests = Interests.generateRandomInterests();
		User u = new User(id, age, sex, location, interests);
		return u;
	}

	public String databaseString() {
		return id + " " + age + " " + sex + " " + location + " " + interests;
	}	
}