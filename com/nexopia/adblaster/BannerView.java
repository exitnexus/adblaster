/**
 * 
 */
package com.nexopia.adblaster;


class BannerView{
	User u;
	Banner b;
	int time;
	BannerView(User u, Banner b, int t){
		this.u = u; this.b = b; time = t;
	}
	
	Banner getBanner() {
		return b;
	}
	
	User getUser() {
		return u;
	}
	
	int getTime() {
		return time;
	}
	BannerView copy(){
		return new BannerView(u,b,time);
	}
	public String toString(){
		return u.toString() + " : " + b.toString() + ":" + time;
	}
}