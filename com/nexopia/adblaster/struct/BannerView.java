/**
 * 
 */
package com.nexopia.adblaster.struct;

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
	
	public BannerView(int index, int uid, int bid, int t, byte size, int page){
		this.uid = uid; 
		this.bid = bid; 
		this.time = t; 
		this.size = size;
		this.page = page;
		this.index = index;
	}
	
	
	public BannerView(String bannerViewString) {
		String words[] = bannerViewString.split(" ");
		uid = Integer.valueOf(words[0]);
		bid = Integer.valueOf(words[1]);
		time = Integer.valueOf(words[2]);
		size = Byte.valueOf(words[3]);
		page = Integer.valueOf(words[4]);
	}


	public void setBanner(Banner b) {
		if (b == null)
			this.bid = -1;
		else 
			this.bid = b.id;
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
		return "Banner ID: " + ((bid == -1)?null:bid) + " - Time: " + time + " User ID: " + uid + " Size: " + size; 
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