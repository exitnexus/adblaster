package com.nexopia.adblaster;

import java.awt.AWTEvent;

public abstract class AbstractAdBlasterUniverse {
	private User u[];
	private Banner b[];

	protected void init(int u_num, int b_num){
		u = new User[u_num];
		b = new Banner[b_num];
	}
	
	public Banner getBanner(int i) {
		return b[i];
	}
	public int getBannerCount(){
		return b.length;
	}
	protected void setBanner(int i, Banner banner) {
		b[i] = banner;
	}

	public User getUser(int i) {
		return u[i];
	}

	public int getUserCount(){
		return u.length;
	}
	protected void setUser(int i, User user) {
		u[i] = user;
	}

}
