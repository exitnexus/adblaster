package com.nexopia.adblaster;

public interface I_Policy {

	public abstract void upgradePolicy(AbstractAdBlasterInstance instance);

	public abstract Banner getBestBanner(AbstractAdBlasterInstance instance,
			BannerView bv);

	public abstract Float getCoefficient(Banner bannerByIndex);

}