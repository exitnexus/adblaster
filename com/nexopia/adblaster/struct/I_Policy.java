package com.nexopia.adblaster.struct;

import com.nexopia.adblaster.AbstractAdBlasterInstance;
import com.nexopia.adblaster.AdBlasterThreadedOperation;
import com.nexopia.adblaster.BannerServer;

public interface I_Policy {

	public abstract double getPriority(Banner b, int uid, int time, BannerServer server);
	public abstract Banner getBestBanner(AbstractAdBlasterInstance instance, BannerView bv);

}