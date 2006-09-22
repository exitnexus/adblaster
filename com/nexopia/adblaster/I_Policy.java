package com.nexopia.adblaster;

public interface I_Policy {

	public abstract void upgradePolicy(AbstractAdBlasterInstance instance,
			AdBlasterThreadedOperation op);

	public abstract Banner getBestBanner(AbstractAdBlasterInstance instance,
			BannerView bv);

	public abstract Float getCoefficient(ServablePropertyHolder bannerByIndex);

	public abstract double getPriority(Banner b, int uid, int time, BannerServer server);


}