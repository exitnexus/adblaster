package com.nexopia.adblaster;

import java.util.Vector;

public interface I_AdBlasterInstance {

	public abstract void fillInstance(AdBlasterPolicy pol);

	public abstract boolean isValidBannerForUser(User u, Banner b);

	public abstract BannerView randomView(AdBlasterUniverse campaign);

	/**
	 * For a particular instance, get a list of all of the banners that were not served
	 * that could have made a profit.
	 * @return A vector of banners.
	 */
	public abstract Vector getUnserved();

	public abstract float totalProfit();

	public abstract AdBlasterInstance copy();

}