/**
 * 
 */
package com.nexopia.adblaster.struct;

/**
 * BannerView objects should know their instance, and inform their instances when they
 * change.
 */
public class BannerView implements Cloneable {
	private int uid;
	private int bid;
	private int time;
	private int index;
	private byte size;
	private int page;
	private int passback;
	private int pageID;
	
	
	public BannerView(int index, int uid, int bid, int t, byte size, int page, int pageID){
		this.uid = uid; 
		this.bid = bid; 
		this.time = t; 
		this.size = size;
		this.page = page;
		this.index = index;
		this.passback = 0;
		this.pageID = pageID;
	}
	
	public BannerView(int index, int uid, int bid, int t, byte size, int page, int passback, int pageID){
		this.uid = uid; 
		this.bid = bid; 
		this.time = t; 
		this.size = size;
		this.page = page;
		this.index = index;
		this.passback = passback;
		this.pageID = pageID;
	}
	
	public BannerView clone() {
		try {
			return (BannerView)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	
	public BannerView(String bannerViewString) {
		String words[] = bannerViewString.split(" ");
		uid = Integer.parseInt(words[0]);
		bid = Integer.parseInt(words[1]);
		time = Integer.parseInt(words[2]);
		size = Byte.parseByte(words[3]);
		page = Integer.parseInt(words[4]);
		passback = Integer.parseInt(words[5]);
		pageID = Integer.parseInt(words[6]);
	}


	public void setBanner(Banner b) {
		if (b == null)
			setBannerID(0);
		else 
			setBannerID(b.id);
	}
	
	public void setBannerID(int bannerid) {
		this.bid = bannerid;
	}

	public int getTime() {
		return time;
	}

	public int getIndex(){
		return index;
	}
	
	public String toString(){
		return "Banner ID: " + ((bid == 0)?null:bid) + " - Time: " + time + " User ID: " + uid + " Size: " + size; 
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

	public int getPassback() {
		return passback;
	}

	public void setPassback(int passback) {
		this.passback = passback;
	}

	public int getPageID() {
		return this.pageID;
	}

}