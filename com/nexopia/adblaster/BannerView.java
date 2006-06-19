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
}