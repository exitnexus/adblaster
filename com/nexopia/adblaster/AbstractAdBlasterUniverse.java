package com.nexopia.adblaster;

import java.util.Collection;

public abstract class AbstractAdBlasterUniverse {
	
	abstract public Banner getBannerByIndex(int i);
	abstract public Banner getBannerByID(int i);

	abstract public int getBannerCount();

	abstract public User getUser(int i);

	abstract public int getUserCount();

	abstract public Collection getBanners();
	
	public Banner getRandomBannerMatching(BannerView bv, AbstractAdBlasterInstance instance) {
		Banner match = null;
		while (match == null){
			int index = (int)(Math.random()*getBannerCount());
			Banner b = getBannerByIndex(index);
			if (b == null){
				System.err.println("There is an error here (null banner in the database at " + index);
			}
			if (b != null && instance.isValidBannerForView(bv, b) && (instance.count(b) < b.getMaxHits())){
				match = b;
			}
		}
		return match;	
	}

}
