/*
 * Created on Jun 23, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GenerateBannerViews {
	//static final int NUM_BANNERVIEWS = 10000;
	static int num_serves = 2000000;
	
	public static void main(String[] args) {
		//UserDatabase uDb = null;
		//BannerViewDatabase bvDb = null;
		AdBlasterDbUniverse ac = new AdBlasterDbUniverse("test");
		I_Policy pol = AdBlasterPolicy.randomPolicy(ac);

		System.out.println("Making random instance.");
		AdBlasterInstance instance1 = AdBlasterInstance.randomInstance(num_serves, ac);
		System.out.println("Filling random instance.");
		instance1.fillInstance(pol);
		AdBlaster.instanceBinding = new BannerViewBinding(ac, instance1);
		System.out.println("Making DB");
		instance1.makeMeADatabase();

		//AdBlasterDbInstance instance2 = new AdBlasterDbInstance(ac);
		//instanceBinding = new BannerViewBinding(ac, instance2);
		//instance2.test();
		//instance2.fillInstance(pol);
		System.out.println("done.");

	}
}