package com.nexopia.adblaster;

import java.awt.AWTEvent;
import java.util.Collection;
import java.util.Random;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public abstract class AbstractAdBlasterUniverse {
	
	abstract protected void init(int u_num, int b_num);
	
	abstract public Banner getBanner(int i);

	abstract public int getBannerCount();

	abstract public User getUser(int i);

	abstract public int getUserCount();

	abstract public Collection getBanners();
	
	public Banner getRandomBannerMatching(Banner bvb, User u, int t, AdBlasterInstance instance) {
		Banner match = null;
		while (match == null){
			Banner b = getBanner((int)(Math.random()*getBannerCount()));
			if (instance.isValidBannerForUser(u, b) && (instance.count(b) < b.getMaxHits())){
				match = b;
			}
		}
		return match;	
	}
	
}
