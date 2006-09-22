package com.nexopia.adblaster;

import java.util.Vector;

import com.nexopia.adblaster.Campaign.CampaignDB;

public class OldPolicy implements I_Policy {

	CampaignDB cdb;

	OldPolicy(CampaignDB cd) {
		cdb = cd;
	}

	public Banner getBestBanner(AbstractAdBlasterInstance instance,
			BannerView bv) {
		throw new UnsupportedOperationException();
	}

	public Float getCoefficient(ServablePropertyHolder bannerByIndex) {
		throw new UnsupportedOperationException();
	}

	public void upgradePolicy(AbstractAdBlasterInstance instance,
			AdBlasterThreadedOperation op) {
		throw new UnsupportedOperationException();
	}


	/*
	 * weighting based on (1 + priority) * (2 - (1-(time since last view/max
	 * view rate)) * (3 - ((views today)/(max views per day))) * (3 - ((clicks
	 * today)/(max clicks per day)))
	 */
	public double getPriority(Banner b, int uid, int time, BannerServer server) {
		double priority = b.getCoefficient() + 1;

		double period = Math.min(b.getLimitByPeriod(), b.campaign
				.getLimitByPeriod());
		if (period == 0)
			period = Math.max(b.getLimitByPeriod(), b.campaign
					.getLimitByPeriod());

		double viewsperuser = Math.min(b.getViewsPerUser(), b.campaign
				.getViewsPerUser());
		if (viewsperuser == 0)
			viewsperuser = Math.max(b.getViewsPerUser(), b.campaign
					.getViewsPerUser());

		double viewsperday = Math.min(b.getViewsPerDay(), b.campaign
				.getViewsPerDay())
				/ server.numservers;
		if (viewsperday == 0)
			viewsperday = Math.max(b.getViewsPerDay(), b.campaign
					.getViewsPerDay())
					/ server.numservers;

		double clicksperday = Math.min(b.getClicksperday(), b.campaign
				.getClicksperday())
				/ server.numservers;
		if (clicksperday == 0)
			clicksperday = Math.max(b.getClicksperday(), b.campaign
					.getClicksperday())
					/ server.numservers;

		if (viewsperuser != 0) {
			int[] viewTimes = server.getViewsForUser(uid, b);
			if (viewTimes.length != 0 && viewTimes[viewTimes.length - 1] != 0
					&& period != 0) {
				priority *= 2 - Math.max(1
						- ((time - viewTimes[viewTimes.length - 1]) / period)
						* viewsperuser, 0);
			} else {
				priority *= 2;
			}
		}

		if (viewsperday != 0) {
			priority *= (3 - server.bannerstats.get(b).dailyviews / viewsperday);
		}

		if (clicksperday != 0) {
			priority *= (3 - server.bannerstats.get(b).dailyviews / clicksperday);
		}

		return priority;
	}

}
