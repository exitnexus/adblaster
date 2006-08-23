/**
 * 
 */
package com.nexopia.adblaster;


/**
 * BannerView objects should know their instance, and inform their instances when they
 * change.
 */
class BannerView{
	private int uid;
	private int bid;
	private int time;
	private int index;
	private byte size;
	private int page;
	private AbstractAdBlasterInstance inst;
	public String comment = "";
	
	protected BannerView(AbstractAdBlasterInstance instance, int index, int uid, int bid, int t, byte size, int page){
		this.uid = uid; this.bid = bid; time = t; inst = instance;
		this.size = size;
		this.page = page;
		this.index = index;
	}
	
	
	Banner getBanner() {
		if (bid == -1){
			return null;
		}
		return inst.universe.getBannerByID(this.bid);
	}
	
	
	public void setBanner(Banner b) {
		this.inst.notifyChange(this, b);
		this.bid = b.id;
	}

	User getUser() {
		return inst.universe.getUser(this.uid);
	}

	int getTime() {
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

}