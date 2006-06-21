package com.nexopia.adblaster;

import java.awt.AWTEvent;

public abstract class AbstractAdBlasterUniverse {
	protected User u[];
	protected Banner b[];
	protected int num_interests;
	public Banner getBanner(int i) {
		return b[i];
	}
	public int getBannerCount(){
		return b.length;
	}
	public User getUser(int i) {
		return u[i];
	}
	public int getUserCount(){
		return u.length;
	}
	public int getNumInterests() {
		return num_interests;
	}

}
