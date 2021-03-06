package com.nexopia.adblaster;

import java.util.Vector;

import com.nexopia.adblaster.db.CampaignDB;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.util.LowMemMap.LowMemArray;

public class OldPolicy implements I_Policy {

	CampaignDB cdb;

	OldPolicy(CampaignDB cd) {
		cdb = cd;
	}

	public Banner getBestBanner(AbstractAdBlasterInstance instance,
			BannerView bv) {
		throw new UnsupportedOperationException();
	}

	/*
	 * weighting based on (1 + priority) * (2 - (1-(time since last view/max
	 * view rate)) * (3 - ((views today)/(max views per day))) * (3 - ((clicks
	 * today)/(max clicks per day)))
	 */
	public double getPriority(Banner b, int uid, int time, BannerServer server) {
		double priority = b.getCoefficient() + 1;

		double period = Math.min(b.getLimitByPeriod(), b.getCampaign()
				.getLimitByPeriod());
		if (period == 0)
			period = Math.max(b.getLimitByPeriod(), b.getCampaign()
					.getLimitByPeriod());

		double viewsperuser = Math.min(b.getViewsPerUser(), b.getCampaign()
				.getViewsPerUser());
		if (viewsperuser == 0)
			viewsperuser = Math.max(b.getViewsPerUser(), b.getCampaign()
					.getViewsPerUser());

		double viewsperday = Math.min(b.getViewsPerDay(), b.getCampaign().getViewsPerDay())/server.numservers();
		if (viewsperday == 0)
			viewsperday = Math.max(b.getViewsPerDay(), b.getCampaign().getViewsPerDay())/server.numservers();

		double clicksperday = Math.min(b.getClicksperday(), b.getCampaign().getClicksperday())/server.numservers();
		if (clicksperday == 0)
			clicksperday = Math.max(b.getClicksperday(), b.getCampaign().getClicksperday())/server.numservers();

		if (viewsperuser != 0) {
			LowMemArray viewTimes = server.getViewsForUser(uid, b);
			if (viewTimes.size() != 0 && viewTimes.get(viewTimes.size() - 1) != 0
					&& period != 0) {
				priority *= 2 - Math.max(1
						- ((time - viewTimes.get(viewTimes.size() - 1)) / period)
						* viewsperuser, 0);
			} else {
				priority *= 2;
			}
		}

		if (viewsperday != 0) {
			priority *= (3 - server.getBannerStat(b).getDailyViews() / viewsperday);
		}

		if (clicksperday != 0) {
			priority *= (3 - server.getBannerStat(b).getDailyViews() / clicksperday);
		}

		return priority;
	}

}
