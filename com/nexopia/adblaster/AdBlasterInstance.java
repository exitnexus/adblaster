package com.nexopia.adblaster;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;


/*
 * A representation of all data we store for one day, along with some functions to
 * fill with learning data.
 */
public class AdBlasterInstance extends AbstractAdBlasterInstance{
	
	public AdBlasterInstance(AbstractAdBlasterUniverse ac) {
		super(ac);
	}

	public void fillInstance(AdBlasterPolicy pol){
		for (int i = 0; i < getViewCount(); i++){
			setBannerView(i, null);
		}
		long time = System.currentTimeMillis();
		for (int i = 0; i < getViewCount(); i++){
			if ((System.currentTimeMillis() - time) > 5000){
				System.out.println("..." + ((float)i/(float)getViewCount())*100 + "% complete.");
				time = System.currentTimeMillis();
			}
			//Banner b = pol.getBestBanner(this, bv);
			Banner b = campaign.getRandomBannerMatching(getBannerForView(i), getUserForView(i), getTimeForView(i), this);
			setBannerView(i, b);
		}
	}

	public AbstractAdBlasterInstance copy() {
		AdBlasterInstance instance = new AdBlasterInstance(this.campaign);
		//xxx:clear out original instance.views
		for (int i = 0; i < this.getViewCount(); i++){
			instance.addView(new BannerView(getUserForView(i), getBannerForView(i), getTimeForView(i)));
		}
		return instance;

	}
	
}
