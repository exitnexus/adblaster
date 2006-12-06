/**
 * 
 */
package com.nexopia.adblaster.struct;

import com.nexopia.adblaster.AbstractAdBlasterInstance;
import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.UserFlatFileReader;


/**
 * BannerView objects should know their instance, and inform their instances when they
 * change.
 */
public class BannerView{
	private int uid;
	private int bid;
	private int time;
	private int index;
	private byte size;
	private int page;
	//private AbstractAdBlasterInstance inst;
	private BannerDatabase bannerDB;
	private UserFlatFileReader userDB;
	public String comment = "";
	
	public BannerView(AbstractAdBlasterInstance instance, int index, int uid, int bid, int t, byte size, int page){
		//TODO: Set bannerDB and userDB from the instance here.
		this.uid = uid; this.bid = bid; time = t; 
		this.size = size;
		this.page = page;
		this.index = index;
	}
	
	
	public BannerView(String bannerViewString, BannerDatabase bannerDB, UserFlatFileReader userDB) {
		this.bannerDB = bannerDB;
		this.userDB = userDB;
		String words[] = bannerViewString.split(" ");
		uid = Integer.valueOf(words[0]);
		bid = Integer.valueOf(words[1]);
		time = Integer.valueOf(words[2]);
		size = Byte.valueOf(words[3]);
		page = Integer.valueOf(words[4]);
	}


	public Banner getBanner() {
		if (bid == -1){
			return null;
		}
		return bannerDB.getBannerByID(this.bid);
	}
	
	
	public void setBanner(Banner b) {
		//TODO: Determine how this line should be dealt with.
		//this.inst.notifyChange(this, b);
		if (b == null)
			this.bid = -1;
		else 
			this.bid = b.id;
	}

	public User getUser() {
		return userDB.getUser(this.uid);
	}

	public int getTime() {
		return time;
	}

	public int getIndex(){
		return index;
	}
	
	public String toString(){
		return "Banner ID: " + ((bid == -1)?null:bid) + " - Time: " + time + " User ID: " + uid + " Size: " + size; 
	}

	public void setBannerWithoutFire(Banner b) {
		if (b == null){
			this.bid = -1;
		} else {
			this.bid = b.id;
		}
	}

	public int getUserID() {
		return this.uid;
	}

	public byte getSize() {
		return this.size;
	}

	public int getPage() {
		return this.page;
	}
	public int getBannerId(){
		return this.bid;
	}

}