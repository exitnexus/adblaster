package com.nexopia.adblaster.struct;

import java.util.Comparator;

import com.nexopia.adblaster.AbstractAdBlasterInstance;

public class BannerPayRateComparator implements Comparator<Banner> {
	AbstractAdBlasterInstance instance;
	
	public BannerPayRateComparator(AbstractAdBlasterInstance instance) {
		this.instance = instance;
	}
	
	public int compare(Banner b1, Banner b2) {
		int payrate1 = b1.getPayrate(instance);
		int payrate2 = b2.getPayrate(instance);
		if (payrate1 < payrate2) {
			return -1;
		} else if (payrate1 == payrate2) {
			return 0;
		} else {
			return 1;
		}
	}

}
