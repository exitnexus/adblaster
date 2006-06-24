/**
 * 
 */
package com.nexopia.adblaster;


/**
 * BannerView objects should know their instance, and inform their instances when they
 * change.
 */
class BannerView{
	private User u;
	private Banner b;
	private int time;
	private int index;
	AbstractAdBlasterInstance inst;
	
	BannerView(AbstractAdBlasterInstance instance, int index, User u, Banner b, int t){
		this.u = u; this.b = b; time = t; inst = instance;
		this.index = index;
	}
	
	Banner getBanner() {
		return b;
	}
	
	
	public void setBanner(Banner b) {
		this.b = b;
		this.inst.notifyChange(this, b);
	}

	User getUser() {
		return u;
	}

	public void setU(User u) {
		this.u = u;
		this.inst.notifyChangeUser(this);
	}

	int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
		this.inst.notifyChangeTime(this);
	}

	public int getIndex(){
		return index;
	}
	
	public String toString(){
		return "Banner ID: " + b.getID() + " - Time: " + time + " User ID: " + u.getID(); 
	}
}