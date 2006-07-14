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
	private Banner b;
	private int time;
	private int index;
	private byte size;
	private int page;
	private AbstractAdBlasterInstance inst;
	public String comment = "";
	
	BannerView(AbstractAdBlasterInstance instance, int index, User u, Banner b, int t, byte size, int page){
		this.uid = u.getID(); this.b = b; time = t; inst = instance;
		this.size = size;
		this.page = page;
		this.index = index;
	}
	
	BannerView(AbstractAdBlasterInstance instance, int index, int id, Banner b, int t, byte size, int page){
		this.uid = id; this.b = b; time = t; inst = instance;
		this.size = size;
		this.page = page;
		this.index = index;
	}
	
	
	Banner getBanner() {
		return b;
	}
	
	
	public void setBanner(Banner b) {
		this.inst.notifyChange(this, b);
		this.b = b;
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
		return "Banner ID: " + ((b == null)?null:b.getID()) + " - Time: " + time + " User ID: " + uid + " Size: " + size; 
	}

	public void setBannerWithoutFire(Banner b) {
		this.b = b;
	}

	/**
	 * @return
	 */
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