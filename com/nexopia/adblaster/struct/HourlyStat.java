/**
 * 
 */
package com.nexopia.adblaster.struct;

import com.nexopia.adblaster.BannerServer;

public class HourlyStat {
	public static final int MEMORY_USAGE = BannerServer.BANNER_SLIDE_SIZE*8+30;
	private int[] views = new int[BannerServer.BANNER_SLIDE_SIZE];
	private int[] clicks = new int[BannerServer.BANNER_SLIDE_SIZE];
	private int current_pos = 0;
	
	public double getClickRate() {
		int totalviews = 0;
		int totalclicks = 0;
		for (int i=0; i < views.length; i++) {
			totalviews += views[i];
			totalclicks += clicks[i];
		}
		double clickrate = Math.max(BannerServer.BANNER_MIN_CLICKRATE, (double)totalviews/totalclicks);
		clickrate = Math.min(clickrate, BannerServer.BANNER_MAX_CLICKRATE);
		return clickrate;
	}

	public void shift() {
		current_pos = (current_pos+1)%BannerServer.BANNER_SLIDE_SIZE;
		views[current_pos] = 0;
		clicks[current_pos] = 0;
	}
	
	public void view() {
		views[current_pos]++;
	}
	public void click() {
		clicks[current_pos]++;
	}
	
}