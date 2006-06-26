/*
 * Created on Jun 23, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GenerateBannerViews {
	static final int NUM_BANNERVIEWS = 10000;
	static int num_serves = 10000;
	
	public static void main(String[] args) {
		UserDatabase uDb = null;
		BannerViewDatabase bvDb = null;
		
		boolean generateViews = false;
		if (generateViews){
			AbstractAdBlasterUniverse ac;
			ac = new AdBlasterDbUniverse();
			AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(ac);

			AdBlasterInstance instance1 = AdBlasterInstance.randomInstance(num_serves, ac);
			instance1.fillInstance(pol);
			BannerViewBinding instanceBinding = new BannerViewBinding(ac, instance1);
			instance1.makeMeADatabase();

			//AdBlasterDbInstance instance2 = new AdBlasterDbInstance(ac);
			//instanceBinding = new BannerViewBinding(ac, instance2);
			//instance2.test();
			System.out.println("Filling...");
			//instance2.fillInstance(pol);
			System.out.println("done.");

			System.exit(0);
			
		}
	}
}